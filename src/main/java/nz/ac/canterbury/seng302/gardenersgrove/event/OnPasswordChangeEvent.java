package nz.ac.canterbury.seng302.gardenersgrove.event;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

public class OnPasswordChangeEvent extends ApplicationEvent {

    private String appUrl;
    private Locale locale;
    private User user;

    public OnPasswordChangeEvent(
            User user, Locale locale, String appUrl) {
        super(user);

        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
    }

    public User getUser() {
        return this.user;
    }

    public String getAppUrl() {
        return this.appUrl;
    }

    public Locale getLocale() {
        return this.locale;
    }
}