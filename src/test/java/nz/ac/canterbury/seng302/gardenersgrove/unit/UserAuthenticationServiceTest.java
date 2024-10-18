package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.dto.ForgotPasswordDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ResetPasswordDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import nz.ac.canterbury.seng302.gardenersgrove.exception.ValidationException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.VerificationTokenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserAuthenticationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Import;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Import(UserAuthenticationService.class)
public class UserAuthenticationServiceTest {

    private UserService userService;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private VerificationTokenRepository tokenRepository;
    private  UserAuthenticationService userAuthenticationService;
    private User user;
    private BindingResult bindingResult;

    private String field;
    private Integer errorCode;
    private String defaultMessage;
    private ForgotPasswordDTO forgotPasswordDTO;
    private ResetPasswordDTO resetPasswordDTO;

    @BeforeEach
    public void setUp() {

        // Set up user authentication service
        userService = Mockito.mock(UserService.class);
        authenticationManager = Mockito.mock(AuthenticationManager.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        tokenRepository = Mockito.mock(VerificationTokenRepository.class);
        bindingResult = Mockito.mock(BindingResult.class);
        VerificationToken token = new VerificationToken("123456", user);


        userAuthenticationService = new UserAuthenticationService(userService, authenticationManager, passwordEncoder, tokenRepository);

        // Set DTOs
        forgotPasswordDTO = Mockito.mock(ForgotPasswordDTO.class);
        resetPasswordDTO = Mockito.mock(ResetPasswordDTO.class);
        Mockito.when(resetPasswordDTO.getToken()).thenReturn("ValidToken");
        Mockito.when(resetPasswordDTO.getPassword()).thenReturn("Password1!");
        Mockito.when(resetPasswordDTO.getPasswordRepeat()).thenReturn("Password1!");

        // Set up a valid user to be used in tests
        user = new User("John@email.com", "2000-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(10));

        // Mock method calls
        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(userService.editUser(user)).thenReturn(user);
        Mockito.when(userService.getUserByEmail(user.getEmail())).thenReturn(null);
        Mockito.when(userService.getUserByToken("ValidToken")).thenReturn(user);
        Mockito.when(userService.getUserByToken("InvalidToken")).thenReturn(null);
        Mockito.when(userService.resetPassword(resetPasswordDTO)).thenReturn(user);
        Mockito.when(passwordEncoder.matches(user.getPassword(), user.getPassword())).thenReturn(true);
        Mockito.when(tokenRepository.findByUser(user)).thenReturn(token);
        Mockito.doAnswer(i -> {
            field = i.getArgument(0);
            errorCode = Integer.parseInt(i.getArgument(1));
            defaultMessage = i.getArgument(2);
            return null;
        }).when(bindingResult).rejectValue(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    // ====================================== REGISTRATION TESTS ====================================== //

    // TESTS FOR FIRST NAME
    // Invalid Cases
    @Test
    public void GivenUser_FirstNameIsNull_HasErrors() {
        // Set first name invalid
        user.setFirstName(null);
        String expectedField = "firstName";
        Integer expectedCode = 401;
        String expectedErrorMessage = "First name cannot be empty and must only include letters, spaces, hyphens or apostrophes";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "    ", "\t", "\n", "-", "'", "John!", "John123", "@John"})
    public void GivenUser_FirstNameIsEmptyString_HasErrors(String firstName) {
        user.setFirstName(firstName);
        String expectedField = "firstName";
        Integer expectedCode = 401;
        String expectedErrorMessage = "First name cannot be empty and must only include letters, spaces, hyphens or apostrophes";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);
        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_FirstNameInvalidLength_HasErrors() {
        // Set first name invalid
        user.setFirstName("this string is sixty five characters long just so you know my bro");

        String expectedField = "firstName";
        Integer expectedCode = 401;
        String expectedErrorMessage = "First name must be 64 characters long or less";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    // Valid Cases
    @ParameterizedTest
    @ValueSource(strings = {"John", "JOHN", "john", "John Paul", "Hēmi", "Müller", "J'Ohn-Paul Hēmi"})
    public void GivenUser_FirstNameIsValid_NoErrors(String firstName) {
        // Set first name valid
        user.setFirstName(firstName);

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }


    @ParameterizedTest
    @ValueSource(strings = {"this string is sixty four characters long just so you know amigo", "nn", "Jo"})
    public void GivenUser_FirstName64chars_NoErrors(String firstName) {
        // Set first name valid
        user.setFirstName(firstName);

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

     // TESTS FOR LAST NAME

    @Test
    public void GivenUser_hasLastNameAndLastNameIsNull_HasErrors() {
        user.setLastName(null);
        user.setNoLastName(false);

        String expectedField = "lastName";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Last name cannot be empty and must only include letters, spaces, hyphens or apostrophes";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "        ", "\t", "\n", "-", "'", "Doe!", "Doe123", "@Doe"})
    public void GivenUser_HasLastnameAndLastNameInvalid_HasErrors(String lastName) {
        user.setLastName(lastName);
        user.setNoLastName(false);

        String expectedField = "lastName";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Last name cannot be empty and must only include letters, spaces, hyphens or apostrophes";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_HasLastNameAndLastNameInvalidLength_HasErrors() {
        user.setLastName("this string is sixty five characters long just so you know my bro");
        user.setNoLastName(false);

        String expectedField = "lastName";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Last name must be 64 characters long or less";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    // Valid Cases
    @ParameterizedTest
    @ValueSource(strings = {"DOE", "doe", "Doe", "Doe-Doe", "Doe Doe", "O'Flaherty", "Tāwhiri", "Müller", "J'Ohn-Paül Hēmi"})
    public void GivenUser_hasLastNameAndLastNameValid_NoErrors(String lastName) {
        // Set last name valid, has last name
        user.setLastName(lastName);
        user.setNoLastName(false);

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        // Check that exception was not thrown
        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"this string is sixty four characters long just so you know amigo", "nn", "Xu"})
    public void GivenUser_LastName64charsWithoutCheckbox_NoErrors(String lastName) {
        user.setLastName(lastName);
        user.setNoLastName(false);

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        // Check that exception was not thrown
        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }


    // TEST CASES FOR EMAIL
    @ParameterizedTest
    @ValueSource(strings = {"john.doe@example.com", "JOHN.DOE@EXAMPLE.COM", "JOhN.dOe@EXaMPlE.COM", "john123@example123.com", "3@example.com", "john@example.online", "john@example.co.nz"})
    public void GivenUser_EmailIsInAValidFormat_NoErrors(String email) {
        // Set email valid
        user.setEmail(email);

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        // Check that exception was not thrown
        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @Test
    public void GivenUser_EmailIsNull_HasErrors() {
        // Set email with no username
        user.setEmail(null);

        String expectedField = "email";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Email address must be in the form 'jane@doe.nz'";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"john.doe@example,com", "@example.com", "john.doe", "john.doe@example", "joh.doe@examplecom", "john.doeexample.com", "john!!@example!!.com!!", "@.com", "", "ttttttt", "john@email..com"})
    public void GivenUser_EmailIsInvalid_HasErrors(String email) {
        user.setEmail(email);

        String expectedField = "email";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Email address must be in the form 'jane@doe.nz'";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_EmailExistsAlready_HasErrors() {
        // Email already exists in database
        Mockito.when(userService.getUserByEmail(user.getEmail())).thenReturn(user);

        String expectedField = "email";
        Integer expectedCode = 401;
        String expectedErrorMessage = "This email address is already in use";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    // TEST CASES FOR DOB
    @Test
    public void GivenUser_DOBIsValid_NoErrors() {
        String validDOB = "2000-01-01";
        user.setDob(validDOB);

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        // Check that exception was not thrown
        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @Test
    public void GivenUser_DOBIsEmpty_NoErrors() {
        String validDOB = "";
        user.setDob(validDOB);

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        // Check that exception was not thrown
        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @Test
    public void GivenUser_DOBIsNull_NoErrors() {
        String validDOB = null;
        user.setDob(validDOB);

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        // Check that exception was not thrown
        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @Test
    public void GivenUser_DOBIs120_HasNoErrors() {
        LocalDate dob = LocalDate.now().minusYears(120).minusMonths(6);
        String is120 = dob.format(DateTimeFormatter.ISO_DATE);
        user.setDob(is120);

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @Test
    public void GivenUser_DOBIs119_HasNoErrors() {
        LocalDate dob = LocalDate.now().minusYears(119).minusMonths(6);
        String is120 = dob.format(DateTimeFormatter.ISO_DATE);
        user.setDob(is120);

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @Test
    public void GivenUser_DOBOlderThan120_HasErrors() {
        LocalDate dob = LocalDate.now().minusYears(121);
        String olderThan120 = dob.format(DateTimeFormatter.ISO_DATE);
        user.setDob(olderThan120);

        String expectedField = "dob";
        Integer expectedCode = 401;
        String expectedErrorMessage = "The maximum age allowed is 120 years";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_DOBAgeIs12_HasErrors() {
        LocalDate dob = LocalDate.now().minusYears(12).minusDays(364);
        String youngerThan13 = dob.format(DateTimeFormatter.ISO_DATE);
        user.setDob(youngerThan13);

        String expectedField = "dob";
        Integer expectedCode = 401;
        String expectedErrorMessage = "You must be 13 years or older to create an account";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_DOBAgeIs13_NoErrors() {
        LocalDate dob = LocalDate.now().minusYears(13);
        String is13 = dob.format(DateTimeFormatter.ISO_DATE);
        user.setDob(is13);

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1990-oct-01", "1990 10 10","1990-13-01", "99-01-13", "1990-0!-10", "1990-02-31", "1990-31-01", "1990.10.01", "1st October 1990", "1990-10-", "24/03/2003", "1990--01", "-01-10"})
    public void GivenUser_DOBInvalidFormat_HasErrors(String invalidDOB) {
        user.setDob(invalidDOB);

        String expectedField = "dob";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Date is not in valid format, DD/MM/YYYY";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    // TEST CASES FOR PASSWORD

    @ParameterizedTest
    @ValueSource(strings = {"Password1!", "Passwo1!", "pASSWORD1!", "Pa123456!"})
    public void GivenUser_PasswordIsValid_NoErrors(String validPassword) {
        user.setPassword(validPassword);
        user.setPasswordRepeat(validPassword);

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Passw1!", "password1!", "PASSWORD1!", "Password!", "Password1", ""})
    public void GivenUser_PasswordIsInvalidAndRepeated_HasErrors(String password) {
        user.setPassword(password);
        user.setPasswordRepeat(password);

        String expectedField = "password";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_PasswordValidButDontMatch_HasErrors() {
        user.setPassword("Password1!");
        user.setPasswordRepeat("Password2!");

        String expectedField = "passwordRepeat";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Passwords do not match";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_PasswordInvalidAndDontMatch_HasErrors() {
        // Set Passwords invalid and don't match
        user.setPassword("Password1");
        user.setPasswordRepeat("password2!");

        String expectedField = "password";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_BothPasswordsNull_HasErrors() {
        // Set (both) Passwords to null
        user.setPassword(null);
        user.setPasswordRepeat(null);

        String expectedField = "password";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }
    @Test
    public void GivenUser_PasswordNotNullRepeatNull_HasErrors() {
        // Set Password repeat to null
        user.setPassword("Password1!");
        user.setPasswordRepeat(null);

        String expectedField = "passwordRepeat";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Passwords do not match";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_PasswordNullRepeatNotNull_HasErrors() {
        // Set Password to null repeat is not null
        user.setPassword(null);
        user.setPasswordRepeat("Password1!");

        String expectedField = "password";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.";

        userAuthenticationService.authenticateUserRegister(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }


    // ====================================== EDIT TESTS ====================================== //

    // TESTS FOR FIRST NAME
    // Invalid Cases
    @Test
    public void GivenUser_EditFirstNameIsNull_HasErrors() {
        // Set first name invalid
        user.setFirstName(null);
        String expectedField = "firstName";
        Integer expectedCode = 401;
        String expectedErrorMessage = "First name cannot be empty and must only include letters, spaces, hyphens or apostrophes";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "    ", "\t", "\n", "-", "'", "John!", "John123", "@John"})
    public void GivenUser_EditFirstNameIsEmptyString_HasErrors(String firstName) {
        user.setFirstName(firstName);
        String expectedField = "firstName";
        Integer expectedCode = 401;
        String expectedErrorMessage = "First name cannot be empty and must only include letters, spaces, hyphens or apostrophes";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);
        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_EditFirstNameInvalidLength_HasErrors() {
        // Set first name invalid
        user.setFirstName("this string is sixty five characters long just so you know my bro");

        String expectedField = "firstName";
        Integer expectedCode = 401;
        String expectedErrorMessage = "First name must be 64 characters long or less";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    // Valid Cases
    @ParameterizedTest
    @ValueSource(strings = {"John", "JOHN", "john", "John Paul", "Hēmi", "Müller", "J'Ohn-Paul Hēmi"})
    public void GivenUser_EditFirstNameIsValid_NoErrors(String firstName) {
        // Set first name valid
        user.setFirstName(firstName);

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }


    @ParameterizedTest
    @ValueSource(strings = {"this string is sixty four characters long just so you know amigo", "nn", "Jo"})
    public void GivenUser_EditFirstName64chars_NoErrors(String firstName) {
        // Set first name valid
        user.setFirstName(firstName);

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    // TESTS FOR LAST NAME

    @Test
    public void GivenUser_EditHasLastNameAndLastNameIsNull_HasErrors() {
        user.setLastName(null);
        user.setNoLastName(false);

        String expectedField = "lastName";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Last name cannot be empty and must only include letters, spaces, hyphens or apostrophes";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "        ", "\t", "\n", "-", "'", "Doe!", "Doe123", "@Doe"})
    public void GivenUser_EditHasLastnameAndLastNameInvalid_HasErrors(String lastName) {
        user.setLastName(lastName);
        user.setNoLastName(false);

        String expectedField = "lastName";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Last name cannot be empty and must only include letters, spaces, hyphens or apostrophes";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_EditHasLastNameAndLastNameInvalidLength_HasErrors() {
        user.setLastName("this string is sixty five characters long just so you know my bro");
        user.setNoLastName(false);

        String expectedField = "lastName";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Last name must be 64 characters long or less";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    // Valid Cases
    @ParameterizedTest
    @ValueSource(strings = {"DOE", "doe", "Doe", "Doe-Doe", "Doe Doe", "O'Flaherty", "Tāwhiri", "Müller", "J'Ohn-Paül Hēmi"})
    public void GivenUser_EditHasLastNameAndLastNameValid_NoErrors(String lastName) {
        // Set last name valid, has last name
        user.setLastName(lastName);
        user.setNoLastName(false);

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        // Check that exception was not thrown
        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"this string is sixty four characters long just so you know amigo", "nn", "Xu"})
    public void GivenUser_EditLastName64charsWithoutCheckbox_NoErrors(String lastName) {
        user.setLastName(lastName);
        user.setNoLastName(false);

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        // Check that exception was not thrown
        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }


    // TEST CASES FOR EMAIL
    @ParameterizedTest
    @ValueSource(strings = {"john.doe@example.com", "JOHN.DOE@EXAMPLE.COM", "JOhN.dOe@EXaMPlE.COM", "john123@example123.com"})
    public void GivenUser_EditEmailIsInAValidFormat_NoErrors(String email) {
        // Set email valid
        user.setEmail(email);

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        // Check that exception was not thrown
        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @Test
    public void GivenUser_EditEmailIsNull_HasErrors() {
        // Set email with no username
        user.setEmail(null);

        String expectedField = "email";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Email address must be in the form 'jane@doe.nz'";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"john.doe@example,com", "@example.com", "john.doe", "john.doe@example", "joh.doe@examplecom", "john.doeexample.com", "john!!@example!!.com!!", "@.com", "", "ttttttt"})
    public void GivenUser_EditEmailIsInvalid_HasErrors(String email) {
        user.setEmail(email);

        String expectedField = "email";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Email address must be in the form 'jane@doe.nz'";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }
    @Test
    public void GivenUser_EditEmailExistsAlready_HasErrors() {
        Mockito.when(userService.getUserByEmail(Mockito.anyString())).thenReturn(user);
        User editUser = new User("edit@email.com", "2000-01-01", "Edit", "Doe", false, "Password1!", "Password1!");

        String expectedField = "email";
        Integer expectedCode = 401;
        String expectedErrorMessage = "This email address is already in use";

        userAuthenticationService.authenticateUserEdit(editUser, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }


    // TEST CASES FOR DOB
    @Test
    public void GivenUser_EditDOBIsValid_NoErrors() {
        String validDOB = "2000-01-01";
        user.setDob(validDOB);

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        // Check that an exception was not thrown
        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @Test
    public void GivenUser_EditDOBIs120_HasNoErrors() {
        LocalDate dob = LocalDate.now().minusYears(120).minusMonths(6);
        String is120 = dob.format(DateTimeFormatter.ISO_DATE);
        user.setDob(is120);

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @Test
    public void GivenUser_EditDOBIs119_HasNoErrors() {
        LocalDate dob = LocalDate.now().minusYears(119).minusMonths(6);
        String is120 = dob.format(DateTimeFormatter.ISO_DATE);
        user.setDob(is120);

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @Test
    public void GivenUser_EditDOBOlderThan120_HasErrors() {
        LocalDate dob = LocalDate.now().minusYears(121);
        String olderThan120 = dob.format(DateTimeFormatter.ISO_DATE);
        user.setDob(olderThan120);

        String expectedField = "dob";
        Integer expectedCode = 401;
        String expectedErrorMessage = "The maximum age allowed is 120 years";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_EditDOBAgeIs12_HasErrors() {
        LocalDate dob = LocalDate.now().minusYears(12).minusDays(364);
        String youngerThan13 = dob.format(DateTimeFormatter.ISO_DATE);
        user.setDob(youngerThan13);

        String expectedField = "dob";
        Integer expectedCode = 401;
        String expectedErrorMessage = "You must be 13 years or older to create an account";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_EditDOBAgeIs13_NoErrors() {
        LocalDate dob = LocalDate.now().minusYears(13);
        String is13 = dob.format(DateTimeFormatter.ISO_DATE);
        user.setDob(is13);

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1990-oct-01", "1990 10 10","1990-13-01", "99-01-13", "1990-0!-10", "1990-02-31", "1990-31-01", "1990.10.01", "1st October 1990", "1990-10-", "24/03/2003", "1990--01", "-01-10"})
    public void GivenUser_EditDOBInvalidFormat_HasErrors(String invalidDOB) {
        user.setDob(invalidDOB);

        String expectedField = "dob";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Date is not in valid format, DD/MM/YYYY";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    // TEST CASES FOR PASSWORD

    @ParameterizedTest
    @ValueSource(strings = {"Password1!", "Passwo1!", "pASSWORD1!", "Pa123456!"})
    public void GivenUser_EditPasswordIsValid_NoErrors(String validPassword) {
        user.setPassword(validPassword);
        user.setPasswordRepeat(validPassword);

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Passw1!", "password1!", "PASSWORD1!", "Password!", "Password1"})
    public void GivenUser_EditPasswordIsInvalidAndRepeated_HasErrors(String password) {
        user.setPassword(password);
        user.setPasswordRepeat(password);

        String expectedField = "password";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_EditPasswordValidButDontMatch_HasErrors() {
        user.setPassword("Password1!");
        user.setPasswordRepeat("Password2!");

        String expectedField = "passwordRepeat";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Passwords do not match";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_EditPasswordInvalidAndDontMatch_HasErrors() {
        // Set Passwords invalid and don't match
        user.setPassword("Password1");
        user.setPasswordRepeat("password2!");

        String expectedField = "password";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUser_EditPasswordNotNullRepeatNull_HasErrors() {
        // Set Password repeat to null
        user.setPassword("Password1!");
        user.setPasswordRepeat(null);

        String expectedField = "passwordRepeat";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Passwords do not match";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    // ====================================== REGISTRATION TESTS ====================================== //

    @Test
    public void GivenUser_LoginEmailIsNull_HasErrors() {
        ArrayList<String> fields = new ArrayList<>();
        ArrayList<Integer> errorCodes = new ArrayList<>();
        ArrayList<String> defaultMessages = new ArrayList<>();
        Mockito.doAnswer(i -> {
            fields.add(i.getArgument(0));
            errorCodes.add(Integer.parseInt(i.getArgument(1)));
            defaultMessages.add(i.getArgument(2));
            return null;
        }).when(bindingResult).rejectValue(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        user.setEmail(null);

        List<String> expectedFields = List.of("email", "password");
        Integer expectedCode = 401;
        List<String> expectedErrorMessages = List.of("Email address must be in the form 'jane@doe.nz'", "The email address is unknown, or the password is invalid");

        userAuthenticationService.authenticateUserLogin(user, bindingResult);

        Assertions.assertEquals(expectedFields.get(0), fields.get(0));
        Assertions.assertEquals(expectedCode, errorCodes.get(0));
        Assertions.assertEquals(expectedErrorMessages.get(0), defaultMessages.get(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"john.doe@example,com", "@example.com", "john.doe", "john.doe@example", "joh.doe@examplecom", "john.doeexample.com", "john!!@example!!.com!!", "@.com", "", "ttttttt"})
    public void GivenUser_LoginEmailIsInvalid_HasErrors(String email) {
        ArrayList<String> fields = new ArrayList<>();
        ArrayList<Integer> errorCodes = new ArrayList<>();
        ArrayList<String> defaultMessages = new ArrayList<>();
        Mockito.doAnswer(i -> {
            fields.add(i.getArgument(0));
            errorCodes.add(Integer.parseInt(i.getArgument(1)));
            defaultMessages.add(i.getArgument(2));
            return null;
        }).when(bindingResult).rejectValue(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        user.setEmail(email);

        List<String> expectedFields = List.of("email", "password");
        Integer expectedCode = 401;
        List<String> expectedErrorMessages = List.of("Email address must be in the form 'jane@doe.nz'", "The email address is unknown, or the password is invalid");

        userAuthenticationService.authenticateUserLogin(user, bindingResult);

        Assertions.assertEquals(expectedFields.get(0), fields.get(0));
        Assertions.assertEquals(expectedCode, errorCodes.get(0));
        Assertions.assertEquals(expectedErrorMessages.get(0), defaultMessages.get(0));
    }

    @Test
    public void givenUser_emailMatchesPassword_loginSucceeds() {
        user.setEnabled(true);
        Mockito.when(userService.getUserByEmail(user.getEmail())).thenReturn(user);

        userAuthenticationService.authenticateUserLogin(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);
    }

    @Test
    public void givenUser_emailDoesntMatchPassword_loginSucceeds() {
        User mockOtherUser = Mockito.mock(User.class);
        Mockito.when(mockOtherUser.getPassword()).thenReturn("OtherPassword1!");
        user.setEnabled(true);
        Mockito.when(userService.getUserByEmail(user.getEmail())).thenReturn(mockOtherUser);

        String expectedField = "password";
        Integer expectedCode = 401;
        String expectedErrorMessage = "The email address is unknown, or the password is invalid";

        userAuthenticationService.authenticateUserLogin(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUserEdit_ValidImage_UserEdited() {
        // Arrange
        // Grant user a role (since they are validated)
        user.grantAuthority("ROLE_USER");

        user.setProfileImage(ImageValidationTest.mockJpegValid);

        // Set image to be too large
        user.setProfileImagePath("src/test/resources/test-images/jpg_valid.jpg");

        // Assert
       userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertNull(field);
        Assertions.assertNull(errorCode);
        Assertions.assertNull(defaultMessage);

    }


    @Test
    public void GivenForgotPassword_EmailValid_NoErrors() {
        // Arrange
        // Set an invalid email
        Mockito.when(forgotPasswordDTO.getEmail()).thenReturn("AnEmail@example.com");

        // Assert
        // Check that no exceptions were thrown
        Assertions.assertDoesNotThrow(() -> {userAuthenticationService.authenticateForgotPassword(forgotPasswordDTO);});
    }

    @Test
    public void GivenForgotPassword_EmailNotValid_HasErrors() {
        // Arrange
        // Set an invalid email
        Mockito.when(forgotPasswordDTO.getEmail()).thenReturn("notAnEmail.com");

        // Assert
        // Check that exception was thrown
        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {userAuthenticationService.authenticateForgotPassword(forgotPasswordDTO);});

        // Check that the error message was correct
        Assertions.assertEquals("Email address must be in the form 'jane@doe.nz'", exception.getMessage());

        // Check that the error field was correct
        Assertions.assertEquals("email", exception.getField());
    }

    @Test
    public void GivenResetPassword_Valid_NoErrors() {
        // Arrange
        // Give authority
        user.grantAuthority("ROLE_USER");

        // Assert
        // Check that no exceptions were thrown
        Assertions.assertDoesNotThrow(() -> {userAuthenticationService.authenticateResetPassword(resetPasswordDTO);});
    }

    @Test
    public void GivenResetPassword_InvalidToken_HasErrors() {
        // Arrange
        // Set an invalid email
        Mockito.when(resetPasswordDTO.getToken()).thenReturn("InvalidToken");

        // Assert
        // Check that exception was thrown
        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {userAuthenticationService.authenticateResetPassword(resetPasswordDTO);});

        // Check that the error message was correct
        Assertions.assertEquals("The token is invalid or expired", exception.getMessage());

        // Check that the error field was correct
        Assertions.assertEquals("token", exception.getField());
    }

    @Test
    public void GivenResetPassword_UnsecurePassword_HasErrors() {
        // Arrange
        // Set an invalid email
        Mockito.when(resetPasswordDTO.getPassword()).thenReturn("Password");

        // Assert
        // Check that exception was thrown
        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {userAuthenticationService.authenticateResetPassword(resetPasswordDTO);});

        // Check that the error message was correct
        Assertions.assertEquals("Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.", exception.getMessage());

        // Check that the error field was correct
        Assertions.assertEquals("password", exception.getField());
    }

    @Test
    public void GivenResetPassword_MismatchPassword_HasErrors() {
        // Arrange
        // Set an invalid email
        Mockito.when(resetPasswordDTO.getPasswordRepeat()).thenReturn("Password2!");

        // Assert
        // Check that exception was thrown
        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {userAuthenticationService.authenticateResetPassword(resetPasswordDTO);});

        // Check that the error message was correct
        Assertions.assertEquals("Passwords do not match", exception.getMessage());

        // Check that the error field was correct
        Assertions.assertEquals("passwordRepeat", exception.getField());
    }

    @Test
    public void GivenUserEditImage_ValidImage_UserEdited() {
        // Arrange
        // Grant user a role (since they are validated)
        user.grantAuthority("ROLE_USER");

        user.setProfileImage(ImageValidationTest.mockJpegValid);

        // Set image to be too large
        user.setProfileImagePath("src/test/resources/test-images/jpg_valid.jpg");

        // Assert
        // Check that no errors are thrown, no fields have changed
        Assertions.assertDoesNotThrow(() -> {userAuthenticationService.authenticateUserEditProfileImage(user);});

        // Check the user service edit user function was called
        Mockito.verify(userService).editUser(user);
    }

    // Image tests
    @Test
    public void GivenUserEdit_ImageTooLarge_ThrowsException() {
        // Grant user a role (since they are validated)
        user.grantAuthority("ROLE_USER");
        user.setProfileImage(ImageValidationTest.mockJpegTooBig);
        // Set image to be too large
        user.setProfileImagePath("src/test/resources/test-images/jpg_too_big.jpg");

        String expectedField = "profileImage";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Image must be less than 10MB";

        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);

    }

    @Test
    public void GivenUserEditImage_ImageTooLarge_ThrowsException() {
        // Arrange
        // Grant user a role (since they are validated)
        user.grantAuthority("ROLE_USER");

        user.setProfileImage(ImageValidationTest.mockJpegTooBig);

        // Set image to be too large
        user.setProfileImagePath("src/test/resources/test-images/jpg_too_big.jpg");

        // Assert
        // Check that an exception is thrown
        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {userAuthenticationService.authenticateUserEditProfileImage(user);});

        // Check that the error message was correct
        Assertions.assertEquals("Image must be less than 10MB", exception.getMessage());

        // Check that the error field was correct
        Assertions.assertEquals("profileImage", exception.getField());
    }


    @Test
    public void GivenUserEdit_ImageWrongType_ThrowsException() {
        // Arrange
        // Grant user a role (since they are validated)
        user.grantAuthority("ROLE_USER");

        user.setProfileImage(ImageValidationTest.mockGifValid);

        // Set image to be a PNG
        user.setProfileImagePath("src/test/resources/test-images/gif_valid.gif");

        String expectedField = "profileImage";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Image must be of type png, jpg or svg";

        // Assert
        userAuthenticationService.authenticateUserEdit(user, bindingResult);

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    public void GivenUserEditImage_ImageWrongType_ThrowsException() {
        // Arrange
        // Grant user a role (since they are validated)
        user.grantAuthority("ROLE_USER");

        user.setProfileImage(ImageValidationTest.mockGifValid);

        // Set image to be a PNG
        user.setProfileImagePath("src/test/resources/test-images/gif_valid.gif");

        // Assert
        // Check that an exception is thrown
        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {userAuthenticationService.authenticateUserEditProfileImage(user);});

        // Check that the error message was correct
        Assertions.assertEquals("Image must be of type png, jpg or svg", exception.getMessage());

        // Check that the error field was correct
        Assertions.assertEquals("profileImage", exception.getField());
    }

    @Test
    void GivenUserLogin_AccountBlocked_ThrowsException() {
        // Arrange User
        user.grantAuthority("ROLE_USER");
        user.setEnabled(true);
        user.setBlocked(true);
        user.setBlockedEndDate(LocalDateTime.now().plusDays(6).plusHours(23).plusMinutes(59));
        Mockito.when(userService.getUserByEmail(user.getEmail())).thenReturn(user);

        // Act
        userAuthenticationService.authenticateUserLogin(user, bindingResult);

        // Assert
        String expectedField = "password";
        Integer expectedCode = 401;
        String expectedErrorMessage = "Account is blocked for 6 days, 23 hours";

        Assertions.assertEquals(expectedField, field);
        Assertions.assertEquals(expectedCode, errorCode);
        Assertions.assertEquals(expectedErrorMessage, defaultMessage);
    }

    @Test
    void GivenUserLogin_AccountBlockedPassedTime_Valid() {
        // Arrange User
        user.grantAuthority("ROLE_USER");
        user.setEnabled(true);
        user.setBlocked(true);
        user.setBlockedEndDate(LocalDateTime.now().minusDays(1));
        Mockito.when(userService.getUserByEmail(user.getEmail())).thenReturn(user);

        // Act & Assert no errors
        Assertions.assertDoesNotThrow(() -> {userAuthenticationService.authenticateUserLogin(user, bindingResult);});
        Assertions.assertFalse(user.isBlocked());
        Assertions.assertNull(user.getBlockedEndDate());
        Mockito.verify(userService).updateUser(user);
    }
}
