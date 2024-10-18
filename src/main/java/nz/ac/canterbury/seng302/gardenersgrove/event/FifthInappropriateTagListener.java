package nz.ac.canterbury.seng302.gardenersgrove.event;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

/**
 * Listens for fifth profanity warning
 */
@Component
public class FifthInappropriateTagListener {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService service;

    /**
     * Send email on fifth inappropriate content submitted by the user
     * @param event the event (fifth inappropriate submission warning)
     */
    @Async
    @EventListener
    public void fifthInappropriateTagWarning(OnFifthInappropriateSubmissionWarningEvent event) {
        User user = event.getUser();

        String recipientAddress = user.getEmail();
        String subject = "Fifth inappropriate content submission warning";
        Context context = new Context();
        emailService.sendEmail(recipientAddress, subject, "fifth-inappropriate-content-warning", context);
        user.setToken(null);
        user.setTokenExpiry(null);
    }

    public void setServices(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.service = userService;
    }
}