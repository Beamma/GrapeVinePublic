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
 * Listens for blocked account events
 */
@Component
public class BlockedAccountListener {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService service;

    /**
     * Send email on sixth inappropriate tag for blocked account
     * @param event the event (sixth inappropriate tag)
     */
    @EventListener
    @Async
    public void blockedAccountEmail(OnBlockedAccountEvent event) {
        User user = event.getUser();

        String recipientAddress = user.getEmail();
        String subject = "Blocked Account";
        Context context = new Context();
        emailService.sendEmail(recipientAddress, subject, "blocked-account-email", context);
        user.setToken(null);
        user.setTokenExpiry(null);
    }

    public void setServices(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.service = userService;
    }
}