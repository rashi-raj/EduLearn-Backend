package com.edulearn.notification.service;

import com.edulearn.notification.config.TwilioConfig;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WhatsAppService Unit Tests")
class WhatsAppServiceTest {

    @Mock
    private TwilioConfig twilioConfig;

    @InjectMocks
    private WhatsAppService whatsappService;

    @Test
    void sendWhatsAppMessage_shouldWork() {
        try (MockedStatic<Message> messageMockedStatic = mockStatic(Message.class)) {
            MessageCreator creator = mock(MessageCreator.class);
            Message message = mock(Message.class);

            when(twilioConfig.getWhatsappNumber()).thenReturn("whatsapp:+123456");
            messageMockedStatic.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString()))
                    .thenReturn(creator);
            when(creator.create()).thenReturn(message);
            when(message.getSid()).thenReturn("SID123");

            whatsappService.sendWhatsAppMessage("9876543210", "Hello");

            verify(twilioConfig).getWhatsappNumber();
            messageMockedStatic.verify(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), eq("Hello")));
        }
    }

    @Test
    void sendWhatsAppMessage_shouldHandleError() {
        when(twilioConfig.getWhatsappNumber()).thenThrow(new RuntimeException("Twilio Down"));

        // Should not throw exception, just log it
        assertDoesNotThrow(() -> whatsappService.sendWhatsAppMessage("9876543210", "Hello"));
    }
}
