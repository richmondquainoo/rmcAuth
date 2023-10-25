package com.example.authvento.service;

import com.example.authvento.rabbitmq.Email;
import com.example.authvento.repository.EmailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailService{

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final EmailRepository repository;

    private final static Logger LOGGER = LoggerFactory
            .getLogger(EmailService.class);


    JavaMailSender mailSender;


    public Object send(String to, String email) throws MessagingException {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(email, true);
            helper.setTo(to);
            helper.setSubject("Confirm your email");
            helper.setFrom("amfohrichie@gmail.com");
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            LOGGER.error("failed to send email", e);
            throw new IllegalStateException("failed to send email");
        }
        return null;
    }

    public void sendHTMLEmail(Email email) throws MailException, MessagingException, UnsupportedEncodingException, UnsupportedEncodingException {
        Context context = new Context();
        context.setVariable("email", email);

        MimeMessage mail = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail, true);

        String process = templateEngine.process("index", context);
        helper.setSubject("Confirm your email");
        helper.setFrom("amfohrichie@gmail.com");
        helper.setTo(email.getRecipientEmail());
        helper.setSubject(email.getSubject());
        helper.setText(process,true);

        javaMailSender.send(mail);
    }

    public Email save(Email entity) {
        return repository.save(entity);
    }

}
