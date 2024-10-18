package nz.ac.canterbury.seng302.gardenersgrove.event;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.EnvironmentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.util.Random;

@Component
public class RegistrationListener implements
        ApplicationListener<OnRegistrationCompleteEvent> {

    @Autowired
    private UserService service;

    @Autowired
    private EnvironmentUtils environmentUtils;

    @Autowired
    private EmailService emailService;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
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

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = getRandomNumberString();
        service.createVerificationToken(user, token);

        String recipientAddress = user.getEmail();
        String subject = "Registration Confirmation";

        Context context = new Context();
        context.setVariable("token", token);
        emailService.sendEmail(recipientAddress, subject, "email-template", context);
    }

    /**
     * Setter for the services
     * FOR TESTING ONLY
     * @param emailService the email service being set
     * @param userService the user service being set
     */
    public void setServices(EmailService emailService, UserService userService, EnvironmentUtils environmentUtils) {
        this.emailService = emailService;
        this.service = userService;
        this.environmentUtils = environmentUtils;
    }
}