package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class MockConfigurationSteps {

    @Autowired
    private ProfanityFilterService profanityFilterService;

    @Given("The profanity filter service is down")
    public void the_profanity_filter_service_is_down() throws Exception {
        Mockito.when(profanityFilterService.isTextProfane(Mockito.anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(500)));
    }

    @Given("The profanity filter service is up")
    public void the_profanity_filter_service_is_up() throws Exception {
        List<String> profaneWords = List.of("shit", "fuck", "ass");
        Mockito.doAnswer(i -> profaneWords.stream().anyMatch(w -> ((String) i.getArgument(0)).contains(w)))
                .when(profanityFilterService).isTextProfane(Mockito.anyString());
    }

}
