//package com.melody.web.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AlarmEmailService {
//
//    private final JavaMailSender mailSender;
//
//    @Value("${spring.mail.username:}")
//    private String fromEmail;
//
//    @Value("${alarm.email.to:admin@melody-ticket.com}")
//    private String toEmail;
//
//    public void sendAlarm(String subject, String content) {
//        if (fromEmail.isEmpty()) {
//            log.warn("邮件未配置，跳过告警邮件发送: {}", subject);
//            return;
//        }
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(fromEmail);
//        message.setTo(toEmail);
//        message.setSubject(subject);
//        message.setText(content);
//
//        mailSender.send(message);
//        log.info("告警邮件已发送至: {}", toEmail);
//    }
//}
