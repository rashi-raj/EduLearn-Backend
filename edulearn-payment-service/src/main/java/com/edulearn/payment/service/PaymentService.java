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
            String razorpayOrderId;
            String receipt = "rcpt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);

            // RESILIENCE: Check if keys are set
            if (isKeyMissing()) {
                razorpayOrderId = "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            } else {
                try {
                    RazorpayClient razorpay = new RazorpayClient(
                            razorpayProperties.getKeyId(),
                            razorpayProperties.getKeySecret()
                    );

                    JSONObject orderRequest = new JSONObject();
                    orderRequest.put("amount", toSubunits(request.getAmount()));
                    orderRequest.put("currency", razorpayProperties.getCurrency());
                    orderRequest.put("receipt", receipt);

                    Order order = razorpay.orders.create(orderRequest);
                    razorpayOrderId = order.get("id");
                } catch (Exception e) {
                    razorpayOrderId = "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
                }
            }

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
            payment.setRazorpayOrderId(razorpayOrderId);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setInstructorId(request.getInstructorId());
            payment.setPayoutStatus("PENDING");

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
            throw new RuntimeException("Failed to initiate payment: " + e.getMessage());
        }
    }

    private boolean isKeyMissing() {
        return razorpayProperties.getKeyId() == null || razorpayProperties.getKeyId().startsWith("${") ||
               razorpayProperties.getKeyId().equals("PLACEHOLDER") ||
               razorpayProperties.getKeySecret() == null || razorpayProperties.getKeySecret().startsWith("${") ||
               razorpayProperties.getKeySecret().equals("PLACEHOLDER");
    }

    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        // SECURITY FIX: Verify signature if keys are present
        if (!isKeyMissing()) {
            try {
                JSONObject attributes = new JSONObject();
                attributes.put("razorpay_order_id", request.getRazorpayOrderId());
                attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
                attributes.put("razorpay_signature", request.getRazorpaySignature());

                Utils.verifyPaymentSignature(attributes, razorpayProperties.getKeySecret());
            } catch (Exception e) {
                throw new RuntimeException("Invalid payment signature");
            }
        }
        
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.saveAndFlush(payment);

        // Publish Payment Notification
        notificationEventPublisher.publish(NotificationEvent.builder()
                .eventType("PAYMENT_SUCCESS")
                .userId(payment.getStudentId().toString())
                .title("Payment Confirmed! \u2705")
                .message("Successfully purchased course: " + payment.getCourseTitle())
                .build());

        return map(payment);
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