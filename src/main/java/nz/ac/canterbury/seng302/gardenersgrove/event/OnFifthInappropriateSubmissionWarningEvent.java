package nz.ac.canterbury.seng302.gardenersgrove.event;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

/**
 * Event representing fifth profane submission (tag, post, comment)
 */
public class OnFifthInappropriateSubmissionWarningEvent extends ApplicationEvent {
    private String appUrl;
    private Locale locale;
    private User user;

    /**
     * Fifth inappropriate tag/post/comment event
     * @param user the user associated with the fifth inappropriate submission event
     * @param locale the locale in which the event is occurring
     * @param appUrl the URL
     */
    public OnFifthInappropriateSubmissionWarningEvent(
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
