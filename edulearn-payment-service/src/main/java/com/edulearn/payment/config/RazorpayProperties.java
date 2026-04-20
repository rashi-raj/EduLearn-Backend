package com.edulearn.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "razorpay")
@Getter
@Setter
public class RazorpayProperties {
    private String keyId;
    private String keySecret;
    private String currency;
}