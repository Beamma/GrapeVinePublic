package nz.ac.canterbury.seng302.gardenersgrove.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;


// Class taken from https://stackoverflow.com/questions/48246874/add-flash-attribute-to-logout-in-spring-security
// and modified to include the "blocked" attribute in the flash map
// answered by: https://stackoverflow.com/users/4751173/glorfindel

/**
 * Custom Logout Success Handler
 * Overrides the default logout success handler to include the attribute "blocked" in the flash map
 * without this, the blocked attribute would not be passed to the login page
 */
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    /**
     * Called when the user logs out successfully
     * @param request the request
     * @param response the response
     * @param authentication the authentication object
     * @throws IOException if the response can not be redirected
     * @throws ServletException if the response can not be redirected
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException, ServletException {
        final FlashMap flashMap = new FlashMap();
        flashMap.put("blocked", request.getParameterMap().get("blocked"));
        final FlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);
        response.sendRedirect("auth/login");
        super.handle(request, response, authentication);
    }
}
