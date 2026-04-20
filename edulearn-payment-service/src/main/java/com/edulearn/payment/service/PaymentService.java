package com.edulearn.payment.service;

import com.edulearn.payment.config.RazorpayProperties;
import com.edulearn.payment.dto.CreatePaymentRequest;
import com.edulearn.payment.dto.CreatePaymentResponse;
import com.edulearn.payment.dto.PaymentResponse;
import com.edulearn.payment.dto.VerifyPaymentRequest;
import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.entity.PaymentStatus;
import com.edulearn.payment.event.NotificationEvent;
import com.edulearn.payment.event.NotificationEventPublisher;
import com.edulearn.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayProperties razorpayProperties;
    private final NotificationEventPublisher notificationEventPublisher;

    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        try {
            RazorpayClient razorpay = new RazorpayClient(
                    razorpayProperties.getKeyId(),
                    razorpayProperties.getKeySecret()
            );

            String receipt = "rcpt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", toSubunits(request.getAmount()));
            orderRequest.put("currency", razorpayProperties.getCurrency());
            orderRequest.put("receipt", receipt);

            Order order = razorpay.orders.create(orderRequest);

            Payment payment = new Payment();
            payment.setCourseId(request.getCourseId());
            payment.setStudentId(request.getStudentId());
            payment.setStudentName(request.getStudentName());
            payment.setStudentEmail(request.getStudentEmail());
            payment.setCourseTitle(request.getCourseTitle());
            payment.setAmount(request.getAmount());
            payment.setCurrency(razorpayProperties.getCurrency());
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setPaymentStatus(PaymentStatus.CREATED);
            payment.setReceipt(receipt);
            payment.setRazorpayOrderId(order.get("id"));
            payment.setCreatedAt(LocalDateTime.now());

            Payment saved = paymentRepository.save(payment);

            return CreatePaymentResponse.builder()
                    .paymentId(saved.getPaymentId())
                    .courseId(saved.getCourseId())
                    .studentId(saved.getStudentId())
                    .amount(saved.getAmount())
                    .currency(saved.getCurrency())
                    .paymentMethod(saved.getPaymentMethod())
                    .paymentStatus(saved.getPaymentStatus().name())
                    .razorpayOrderId(saved.getRazorpayOrderId())
                    .razorpayKeyId(razorpayProperties.getKeyId())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }

    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        try {
            Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() -> new RuntimeException("Payment order not found"));

            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean valid = Utils.verifyPaymentSignature(options, razorpayProperties.getKeySecret());

            if (!valid) {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new RuntimeException("Invalid payment signature");
            }

            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());

            Payment saved = paymentRepository.save(payment);

            notificationEventPublisher.publish(NotificationEvent.builder()
                    .eventType("PAYMENT_SUCCESS")
                    .userId(saved.getStudentId().toString())
                    .title("Payment Successful!")
                    .message("Your payment of " + saved.getAmount() + " " + saved.getCurrency() + " has been verified successfully.")
                    .build());

            return map(saved);

        } catch (Exception e) {
            throw new RuntimeException("Payment verification failed: " + e.getMessage(), e);
        }
    }

    public List<PaymentResponse> getPaymentsByStudent(UUID studentId) {
        return paymentRepository.findByStudentId(studentId)
                .stream()
                .map(this::map)
                .toList();
    }

    public boolean hasPaid(UUID studentId, UUID courseId) {
        return paymentRepository.existsByStudentIdAndCourseIdAndPaymentStatus(
                studentId, courseId, PaymentStatus.PAID
        );
    }

    private int toSubunits(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).intValueExact();
    }

    private PaymentResponse map(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .courseId(payment.getCourseId())
                .studentId(payment.getStudentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus().name())
                .receipt(payment.getReceipt())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .build();
    }
}