package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.UserController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.exception.BadRequestException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.VerificationTokenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserAuthenticationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EditUserProfileFeature {

    private static UserAuthenticationService spiedUserAuthenticationService;
    private static UserService mockUserService;
    private static ArrayList<User> users;
    private static GardenService mockGardenService;
    private static User loggedInUser;
    private static User otherUser;
    private static User newDetails;
    private static List<Garden> gardens;
    private static MockMvc mockMvc;
    private RequestBuilder requestBuilder;
    private ResultActions resultActions;

    @Before
    public static void setup() throws IOException, BadRequestException {
        // Create users
        loggedInUser = new User("my@email.com", "1999-12-31", "Gerd", "Müller", false, "Password1!", "Password1!");
        loggedInUser.setId(1L);
        loggedInUser.setEnabled(true);
        otherUser = new User("other@email.com", "2003-03-15", "Robert", "Lewandowski", false, "otherPassword1!", "otherPassword1!");
        otherUser.setId(2L);
        otherUser.setEnabled(true);
        users = new ArrayList<>();
        users.add(loggedInUser);
        users.add(otherUser);

        // Create user details container for an edited profile
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.getSize()).thenReturn(0L);
        Mockito.when(file.getContentType()).thenReturn("png");
        String nonsenseBytes = "e04fd020ea3a6910a2d808002b30309d";
        Mockito.when(file.getBytes()).thenReturn(HexFormat.of().parseHex(nonsenseBytes));
        newDetails = new User("my@email.com", "1999-12-30", "New", "Name", false, "Password1!", "Password1!");
        newDetails.setProfileImage(file);

        // Create list of gardens for the navbar
        AddressDTO validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);
        gardens = List.of(new Garden("Garden name", validLocation, loggedInUser), new Garden("Another name", validLocation, loggedInUser));
        mockGardenService = Mockito.mock(GardenService.class);
        Mockito.when(mockGardenService.getGardensByUserId(1L)).thenReturn(gardens);

        // Set up mocked UserService to interact with a list of users instead of a database
        mockUserService = Mockito.mock(UserService.class);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(loggedInUser);
        Mockito.when(mockUserService.validateUserId(Mockito.anyString())).thenReturn(true);

        Mockito.doAnswer(i -> {     // Search users list instead of DB for findByEmail
            String email = i.getArgument(0);
            return users.stream().filter(u -> Objects.equals(u.getEmail(), email)).findFirst().orElse(null);
        }).when(mockUserService).getUserByEmail(Mockito.anyString());

        Mockito.doAnswer(i -> {     // Add to users list instead of DB for addUser
            User u = i.getArgument(0);
            users.add(u);
            return u;
        }).when(mockUserService).addUser(Mockito.any(User.class));

        Mockito.doAnswer(invocation -> {    // Search users list instead of DB for getUserProfile
            Long userId = Long.parseLong(invocation.getArgument(0));
            Optional<User> foundUser = users.stream().filter(u -> Objects.equals(u.getId(), userId)).findFirst();
            if (foundUser.isEmpty()) throw new BadRequestException("No such user found");
            return foundUser.get();
        }).when(mockUserService).getUserProfile(Mockito.anyString());


        // Setup user authentication service to use users list instead of db
        spiedUserAuthenticationService = Mockito.spy(new UserAuthenticationService(mockUserService,
                Mockito.spy(AuthenticationManager.class),
                Mockito.spy(PasswordEncoder.class),
                Mockito.spy(VerificationTokenRepository.class)
        ));
        Mockito.doAnswer(i -> {     // Edit user edits a user in the users list, not the db
            User newUser = i.getArgument(0);
            User oldUser = users.stream()
                    .filter(u -> Objects.equals(u.getEmail(), newUser.getEmail()))
                    .findFirst()
                    .get();
            oldUser.setEmail(newUser.getEmail());
            oldUser.setDob(newUser.getDob());
            oldUser.setFirstName(newUser.getFirstName());
            oldUser.setLastName(newUser.getLastName());
            oldUser.setNoLastName(newUser.getNoLastName());
            return oldUser;
        }).when(spiedUserAuthenticationService).editUser(Mockito.any(User.class));


        UserController userController = new UserController(spiedUserAuthenticationService, mockUserService, mockGardenService);

        // Fix from https://stackoverflow.com/a/21755562 (Circular path error)
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setViewResolvers(viewResolver)
                .build();
    }

    /**
     * Returns true if the given phrase is found exactly within the file at the given filepath
     *
     * @param filepath the path at which to find the file being searched
     * @param phrase   the phrase we are looking for
     * @return true if the phrase is found in the file, false otherwise
     */
    private boolean fileContainsPhrase(String filepath, String phrase) throws IOException {
        return Files.lines(Paths.get("src/main/resources/templates/user/edit.html"))
                .anyMatch(l -> l.contains(phrase));
    }

    @Given("I am on my profile page")
    public void i_am_on_my_user_profile_page() throws Exception {
        resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/user/profile/" + loggedInUser.getId()));
    }

    @When("I click the “Edit” button")
    public void i_click_the_edit_button() throws Exception {
        resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/user/edit/"));
    }

    @Then("I see the edit profile form")
    public void i_see_the_edit_profile_form() throws Exception {
        resultActions.andExpectAll(
                view().name("user/edit"),
                status().isOk()
        );
    }

    @Then("all my details are prepopulated except the passwords.")
    public void all_my_details_are_prepopulated_except_the_passwords() throws Exception {
        String filepath = "src/main/resources/templates/user/edit.html";

        String imagePrepopulatedPhrase = "th:src=\"${profileImage}\"";
        String emailPrepopulatedPhrase = "th:field=\"*{email}\"";
        String dobPrepopulatedPhrase = "th:field=\"*{dob}\"";
        String firstNamePrepopulatedPhrase = "th:field=\"*{firstName}\"";
        String noLastNamePrepopulatedPhrase = "th:field=\"*{noLastName}\"";
        String lastNamePrepopulatedPhrase = "th:field=\"*{lastName}\"";
        String passwordPrepopulatedPhrase = "th:field=\"*{password}\"";

        resultActions.andExpectAll(
                model().attribute("user", loggedInUser),
                model().attributeExists("profileImage")
        );

        Assertions.assertTrue(fileContainsPhrase(filepath, imagePrepopulatedPhrase));
        Assertions.assertTrue(fileContainsPhrase(filepath, emailPrepopulatedPhrase));
        Assertions.assertTrue(fileContainsPhrase(filepath, dobPrepopulatedPhrase));
        Assertions.assertTrue(fileContainsPhrase(filepath, firstNamePrepopulatedPhrase));
        Assertions.assertTrue(fileContainsPhrase(filepath, noLastNamePrepopulatedPhrase));
        Assertions.assertTrue(fileContainsPhrase(filepath, lastNamePrepopulatedPhrase));
        Assertions.assertFalse(fileContainsPhrase(filepath, passwordPrepopulatedPhrase));
    }

    @Given("I am on the edit profile form")
    public void i_am_on_the_edit_profile_form() throws Exception {
        resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/user/edit/"));
    }

    @Given("I have already indicated that I do not have a last name")
    public void i_have_already_indicated_that_i_do_not_have_a_last_name() {
        loggedInUser.setNoLastName(true);
    }

    @Then("the last name field defaults to being disabled")
    public void the_last_name_field_defaults_to_being_disabled() throws Exception {
        String filepath = "src/main/resources/templates/user/edit.html";
        String lastNamedDisabled = "th:disabled=\"${user.noLastName}\"";

        resultActions.andExpect(model().attributeExists("user"));
        User user = (User) resultActions.andReturn().getModelAndView().getModel().get("user");
        Assertions.assertTrue(user.getNoLastName());

        Assertions.assertTrue(fileContainsPhrase(filepath, lastNamedDisabled));

    }

    @Then("the I have no surname checkbox is marked as checked")
    public void the_i_have_no_surname_checkbox_is_marked_as_checked() throws Exception {
        String filepath = "src/main/resources/templates/user/edit.html";
        String lastNameCheckBoxIsChecked = "th:field=\"*{noLastName}\"";

        resultActions.andExpect(model().attributeExists("user"));
        User user = (User) resultActions.andReturn().getModelAndView().getModel().get("user");
        Assertions.assertTrue(user.getNoLastName());
        Assertions.assertTrue(fileContainsPhrase(filepath, lastNameCheckBoxIsChecked));
    }


    @Given("I enter valid values for my first name, last name, email address, and date of birth")
    public void i_enter_valid_values_for_my_first_name_last_name_email_address_and_date_of_birth() {
        requestBuilder = post("/user/edit")
                .flashAttr("user", newDetails);
    }

    @When("I click the submit button")
    public void i_click_the_submit_button() throws Exception {
        resultActions = mockMvc.perform(requestBuilder);
    }

    @Then("my new details are saved")
    public void my_new_details_are_saved() {
        User editedUser = mockUserService.getUserByEmail("my@email.com");
        Assertions.assertAll(
                () -> Assertions.assertEquals(newDetails.getEmail(), editedUser.getEmail()),
                () -> Assertions.assertEquals(loggedInUser.getId(), editedUser.getId()),
                () -> Assertions.assertEquals(newDetails.getDob(), editedUser.getDob()),
                () -> Assertions.assertEquals(newDetails.getPassword(), editedUser.getPassword()),
                () -> Assertions.assertEquals(newDetails.getFirstName(), editedUser.getFirstName()),
                () -> Assertions.assertEquals(newDetails.getLastName(), editedUser.getLastName()),
                () -> Assertions.assertEquals(newDetails.getNoLastName(), editedUser.getNoLastName())

        );
    }

    @Then("I am taken back to my profile page")
    public void i_am_taken_back_to_my_profile_page() throws Exception {
        resultActions.andExpectAll(
                redirectedUrl("/user/profile/" + loggedInUser.getId()),
                view().name("redirect:/user/profile/" + loggedInUser.getId())
        );
    }

    @Given("I click the check box marked “I have no surname”,")
    public void i_click_the_check_box_marked_i_have_no_surname() {
        newDetails.setNoLastName(true);
    }

    @Then("the last name text field is disabled")
    public void the_last_name_text_field_is_disabled() throws IOException {
        String filepath = "src/main/resources/templates/user/edit.html";
        String lastNamedDisabled = "th:disabled=\"${user.noLastName}\"";

        Assertions.assertTrue(newDetails.getNoLastName());

        Assertions.assertTrue(fileContainsPhrase(filepath, lastNamedDisabled));
    }

    @Then("any surname that was filled in will be removed from my account details when I submit the form")
    public void any_surname_that_was_filled_in_will_be_removed_from_my_account_details_when_i_submit_the_form() throws Exception {
        resultActions = mockMvc.perform(post("/user/edit")
                .flashAttr("user", newDetails));

        User editedUser = mockUserService.getUserByEmail("my@email.com");
        Assertions.assertAll(
                () -> Assertions.assertNull(editedUser.getLastName()),
                () -> Assertions.assertTrue(editedUser.getNoLastName())
        );
    }

    @Given("I enter invalid values, first name {string}, last name {string}, and no last name being {string}")
    public void i_enter_invalid_values_first_name_last_name_and_no_last_name_being(String firstName, String lastName, String noLastName) {
        newDetails.setFirstName(firstName);
        newDetails.setLastName(lastName);
        newDetails.setNoLastName(Boolean.parseBoolean(noLastName));
        requestBuilder = post("/user/edit")
                .flashAttr("user", newDetails);
    }

    @Then("an error message for the field {string} tells me the correct error message {string}")
    public void an_error_message_tells_me_the_correct_error_message(String fieldName, String errorMessage) throws Exception {
        resultActions.andExpect(model().attributeHasFieldErrorCode("user", fieldName, "401"));

        BindingResult result = (BindingResult) resultActions.andReturn()
                .getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.user"); // This line from chatGPT
        boolean errorMessageFound = result.getFieldErrors(fieldName)
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(errorMessage));
        Assertions.assertTrue(errorMessageFound);
    }


    @Given("I enter a {string} name {string} that is more than 64 characters")
    public void i_enter_a_name_that_is_more_than_64_characters(String nameType, String name) {
        if (Objects.equals(nameType, "first")) {
            newDetails.setFirstName(name);
            newDetails.setLastName("lastName");
        }
        else if (Objects.equals(nameType, "last")) {
            newDetails.setLastName(name);
            newDetails.setFirstName("First");
        }
        requestBuilder = post("/user/edit")
                .flashAttr("user", newDetails);
    }
    @Then("an error message tells me {string} {string}")
    public void an_error_message_tells_me(String nameType, String errorMessageSuffix) throws Exception {
        String fieldName = nameType + "Name";
        String name = Objects.equals(nameType, "first") ? "First" : "Last";
        String errorMessage = name + " " + errorMessageSuffix;

        resultActions.andExpect(model().attributeHasFieldErrorCode("user", fieldName, "401"));

        BindingResult result = (BindingResult) resultActions.andReturn()
                .getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.user"); // This line from chatGPT
        boolean errorMessageFound = result.getFieldErrors(fieldName)
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(errorMessage));
        Assertions.assertTrue(errorMessageFound);
    }

    @Given("I enter an empty or malformed email address {string}")
    public void i_enter_an_empty_or_malformed_email_address(String malformedEmail) {
        newDetails.setEmail(malformedEmail);
        requestBuilder = post("/user/edit")
                .flashAttr("user", newDetails);
    }

    @Given("I enter an email address associated to an account that already exists")
    public void i_enter_an_email_address_associated_to_an_account_that_already_exists() {
        newDetails.setEmail(otherUser.getEmail());
        requestBuilder = post("/user/edit")
                .flashAttr("user", newDetails);
    }

    @Given("I enter a date {string} that is not in the Aotearoa NZ format")
    public void i_enter_a_date_that_is_not_in_the_aotearoa_nz_format(String dob) {
        newDetails.setDob(dob);
        requestBuilder = post("/user/edit")
                .flashAttr("user", newDetails);
    }

    @Given("I enter a date of birth for someone younger than {int} years old")
    public void i_enter_a_date_of_birth_for_someone_younger_than_years_old(Integer minAge) {
        LocalDate dob = LocalDate.now().minusYears(minAge).plusDays(1); // One day younger than 13
        newDetails.setDob(dob.toString());
        requestBuilder = post("/user/edit")
                .flashAttr("user", newDetails);
    }

    @Given("I enter a date of birth for someone older than {int} years old")
    public void i_enter_a_date_of_birth_for_someone_older_than_years_old(Integer maxAge) {
        LocalDate dob = LocalDate.now().minusYears(maxAge + 1); // On their 121st birthday
        newDetails.setDob(dob.toString());
        requestBuilder = post("/user/edit")
                .flashAttr("user", newDetails);
    }

    @When("I click the cancel button")
    public void i_click_the_cancel_button() throws Exception {
        String filepath = "src/main/resources/templates/user/edit.html";
        String cancelButtonHref = "<span th:href=\"@{'/user/profile/' + ${user.id ?: userId}}\">Cancel</span>";

        Assertions.assertTrue(fileContainsPhrase(filepath, cancelButtonHref));
        resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/user/profile/" + loggedInUser.getId()));
    }

    @Then("I am taken to my profile page")
    public void i_am_taken_to_my_profile_page() throws Exception {
        resultActions.andExpectAll(
                view().name("user/profile")
        );
    }

    @Then("no changes have been made to my profile")
    public void no_changes_have_been_made_to_my_profile() {
        User editedUser = (User) resultActions.andReturn().getModelAndView().getModel().get("user");

        Assertions.assertAll(
                () -> Assertions.assertEquals(loggedInUser.getEmail(), editedUser.getEmail()),
                () -> Assertions.assertEquals(loggedInUser.getId(), editedUser.getId()),
                () -> Assertions.assertEquals(loggedInUser.getDob(), editedUser.getDob()),
                () -> Assertions.assertEquals(loggedInUser.getPassword(), editedUser.getPassword()),
                () -> Assertions.assertEquals(loggedInUser.getFirstName(), editedUser.getFirstName()),
                () -> Assertions.assertEquals(loggedInUser.getLastName(), editedUser.getLastName()),
                () -> Assertions.assertEquals(loggedInUser.getNoLastName(), editedUser.getNoLastName())
        );
    }
}
