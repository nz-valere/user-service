package gp2.SCRM.User.UserService.service;

import gp2.SCRM.User.UserService.dto.RegisterUserDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class  EmailService {

    private final JavaMailSender mailSender;

    private final String sender = "saintjeancrmvafc@gmail.com";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        String subject = "Password Reset Request";
        String resetUrl = "https://localhost:4200/reset-password?token=" + token;
        String content = "Click the following link to reset your password: " + resetUrl;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
    }

    public String SendPasswordtoTeacher(RegisterUserDto user, String password) {
        try {
            // Create a simple mail message
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(sender);
            mailMessage.setTo(user.getEmail());
            mailMessage.setSubject("Saint Jean Teacher Registration");

            // Email content with user details and password
            String emailBody = String.format(
                    "Hello, Dear %s,\n\n" +
                            "We are happy to inform you that You have and account in our App and.\n\n" +
                            "Here are your login credentials:\n" +
                            "📧 Email: %s\n" +
                            "🔑 Password: %s\n\n" +
                            "Please change your password after logging in.\n\n" +
                            "Best regards,\nSaint Jean Team",
                    user.getFullName(), user.getEmail(), password
            );

            mailMessage.setText(emailBody);

            // Send the mail
            mailSender.send(mailMessage);

            return "Mail Sent Successfully...";
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
            return "Error while Sending Mail";
        }
    }

    public String SendPasswordtoCandidate(RegisterUserDto user, String password) {
        try {
            // Create a simple mail message
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(sender);
            mailMessage.setTo(user.getEmail());
            mailMessage.setSubject("Saint Jean Candidate Registration");

            // Email content with user details and password
            String emailBody = String.format(
                    "Hello, Dear %s,\n\n" +
                            "We are happy to inform you that your application as a Candidate for our exam has been validated.\n\n" +
                            "Here are your login credentials:\n" +
                            "📧 Email: %s\n" +
                            "🔑 Password: %s\n\n" +
                            "Please change your password after logging in.\n\n" +
                            "Best regards,\nSaint Jean Team",
                    user.getFullName(), user.getEmail(), password
            );

            mailMessage.setText(emailBody);

            // Send the mail
            mailSender.send(mailMessage);

            return "Mail Sent Successfully...";
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
            return "Error while Sending Mail";
        }
    }


}
