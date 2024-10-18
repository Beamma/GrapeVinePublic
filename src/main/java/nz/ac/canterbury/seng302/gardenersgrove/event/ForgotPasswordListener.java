package nz.ac.canterbury.seng302.gardenersgrove.event;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.util.Random;

@Component
public class ForgotPasswordListener implements
        ApplicationListener<OnForgotPasswordEvent> {

    @Autowired
    private UserService service;

    @Autowired
    private MessageSource messages;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailService emailService;

    @Override
    public void onApplicationEvent(OnForgotPasswordEvent event) {
        this.forgotPasswordConfirmation(event);
    }

    /**
     * Generates user confirmation string
     * Source: https://stackoverflow.com/a/51324081
     * @return a random 6-digit number as a string
     */
    public String getRandomNumberString() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        return String.format("%06d", number);
    }

    private void forgotPasswordConfirmation(OnForgotPasswordEvent event) {
        User user = event.getUser();
        String token = user.getToken();

        String recipientAddress = user.getEmail();
        String subject = "Forgot Password request";
        String confirmationUrl
                = event.getAppUrl() + "/auth/resetPassword?token=" + token;
        String confirmationLink = "http://localhost:8080" + confirmationUrl;

        Context context = new Context();
        context.setVariable("confirmationURL", confirmationLink);
        emailService.sendEmail(recipientAddress, subject, "forgot-password-template", context);
    }

    /**
     * Setter for the services
     * FOR TESTING ONLY
     * @param emailService the email service being set
     * @param userService the user service being set
     */
    public void setServices(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.service = userService;
    }
}