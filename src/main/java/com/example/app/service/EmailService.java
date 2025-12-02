package com.example.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class EmailService {

    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@blog.com}")
    private String fromEmail;

    @Value("${spring.mail.enabled:false}")
    private boolean emailEnabled;

    /**
     * メール送信
     * 設定が有効な場合は実際に送信、無効な場合はログ出力のみ
     */
    public void sendEmail(String to, String subject, String text) {
        if (emailEnabled && mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);
                
                mailSender.send(message);
                logger.info("Email sent successfully to: " + to);
            } catch (Exception e) {
                logger.severe("Failed to send email to " + to + ": " + e.getMessage());
                throw new RuntimeException("メール送信に失敗しました: " + e.getMessage());
            }
        } else {
            // メール機能が無効の場合はログ出力のみ
            logger.info("Email sending is disabled. Would have sent email:");
            logger.info("To: " + to);
            logger.info("Subject: " + subject);
            logger.info("Body: " + text);
        }
    }
}
