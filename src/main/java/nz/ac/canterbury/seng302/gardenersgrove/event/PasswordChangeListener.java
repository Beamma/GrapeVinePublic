package nz.ac.canterbury.seng302.gardenersgrove.event;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.util.UUID;
@Component
public class PasswordChangeListener implements
        ApplicationListener<OnPasswordChangeEvent> {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService service;

    @Override
    public void onApplicationEvent(OnPasswordChangeEvent event) {
        this.confirmPasswordChange(event);
    }

    public void confirmPasswordChange(OnPasswordChangeEvent event) {
        User user = event.getUser();

        String recipientAddress = user.getEmail();
        String subject = "Password Change confirmation";
        Context context = new Context();
        emailService.sendEmail(recipientAddress, subject, "password-change-template", context);
        user.setToken(null);
        user.setTokenExpiry(null);
    }

    public void setServices(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.service = userService;
    }
}