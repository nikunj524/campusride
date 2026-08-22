package com.campusride.authservice;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

@TestConfiguration
public class TestMailConfig {

    @Bean
    JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }
}
