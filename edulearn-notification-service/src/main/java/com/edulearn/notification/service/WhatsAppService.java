package com.edulearn.notification.service;

import com.edulearn.notification.config.TwilioConfig;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    private final TwilioConfig twilioConfig;

    public void sendWhatsAppMessage(String toMobile, String messageBody) {
        try {
            String formattedTo = toMobile;
            // If it's a 10-digit number, assume India (+91)
            if (toMobile.length() == 10 && toMobile.matches("\\d+")) {
                formattedTo = "+91" + toMobile;
            } else if (!toMobile.startsWith("+")) {
                formattedTo = "+" + toMobile;
            }
            
            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + formattedTo),
                    new PhoneNumber(twilioConfig.getWhatsappNumber()),
                    messageBody
            ).create();

            log.info("WhatsApp message sent successfully to {}. SID: {}", toMobile, message.getSid());
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", toMobile, e.getMessage());
        }
    }
}
