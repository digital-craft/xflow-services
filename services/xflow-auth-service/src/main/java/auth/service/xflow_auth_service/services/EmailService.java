package auth.service.xflow_auth_service.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;

    @Value("${spring.mail.username:noreply@xflow.com}")
    private String defaultFromEmail;

    @Async
    public void sendHtmlEmail(String from, String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage, 
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, 
                    StandardCharsets.UTF_8.name()
            );

            String sender = (from == null || from.isBlank()) ? defaultFromEmail : from;

            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("email-delivery-failed", e);
        }
    }

    public void sendHtmlEmailSync(String from, String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage, 
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, 
                    StandardCharsets.UTF_8.name()
            );

            String sender = (from == null || from.isBlank()) ? defaultFromEmail : from;

            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("email-delivery-failed", e);
        }
    }

    @Async
    public void sendTemplateEmail(String from, String to, String subject, String templatePath, Map<String, String> templateVariables) {
        try {
            Resource resource = resourceLoader.getResource(templatePath);
            String htmlContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            if (templateVariables != null) {
                for (Map.Entry<String, String> entry : templateVariables.entrySet()) {
                    String placeholder = "{{" + entry.getKey() + "}}";
                    htmlContent = htmlContent.replace(placeholder, entry.getValue());
                }
            }

            sendHtmlEmail(from, to, subject, htmlContent);

        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("email-delivery-failed", e);
        }
    }

    public void sendTemplateEmailSync(String from, String to, String subject, String templatePath, Map<String, String> templateVariables) {
        try {
            Resource resource = resourceLoader.getResource(templatePath);
            String htmlContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            if (templateVariables != null) {
                for (Map.Entry<String, String> entry : templateVariables.entrySet()) {
                    String placeholder = "{{" + entry.getKey() + "}}";
                    htmlContent = htmlContent.replace(placeholder, entry.getValue());
                }
            }

            sendHtmlEmailSync(from, to, subject, htmlContent);

        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("email-delivery-failed", e);
        }
    }
}