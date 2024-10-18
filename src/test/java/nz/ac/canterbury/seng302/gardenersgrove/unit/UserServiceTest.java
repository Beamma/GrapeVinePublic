package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.UserImageStorageProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Import(UserService.class)
class UserServiceTest {
    static String svgValidFilePath = "src/test/resources/test-images/svg_valid.svg";
    static Path svgValidPath = Paths.get(svgValidFilePath);
    static String svgValidName = "svg_valid";
    static String svgValidOriginalFileName = "svg_valid.svg";
    static String svgValidContentType = "image/svg+xml";
    static byte[] svgValidContent;
    static {
        try {
            svgValidContent = Files.readAllBytes(svgValidPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static MultipartFile mockSvgValid = new MockMultipartFile(svgValidName,
            svgValidOriginalFileName,
            svgValidContentType,
            svgValidContent);

    UserService userService;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    User user;
    User editUser;

    @BeforeEach
    public void setUp() {

        // Set up user authentication service
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
        UserImageStorageProperties userImageStorageProperties = new UserImageStorageProperties();
        userImageStorageProperties.setLocation("src/test/resources/test-images");
        userService.setStorageProperties(userImageStorageProperties);

        // Set up a valid user to be used in tests
        user = new User("John@email.com", "2000-01-01", "John", "Doe", false, "Password1!", "Password1!");
        editUser = new User(user.getEmail(), user.getDob(), user.getFirstName(), user.getLastName(), user.getNoLastName(), null, null);
        user.setId(1L);
        editUser.setId(1L);
        user.setProfileImage(mockSvgValid);
        editUser.setProfileImage(mockSvgValid);
        // Set up the mock calls
        Mockito.when(passwordEncoder.encode(user.getPassword())).thenReturn("aRandomHash1");
        Mockito.when(passwordEncoder.encode("Password2!")).thenReturn("aRandomHash2");
        Mockito.when(userRepository.findByEmail(null)).thenReturn(Optional.ofNullable(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);


        // Mock the security context
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void GivenAddUser_NoLastNameWithLastName_LastNameNull () {
        String oldLastName = user.getLastName();

        // Check that the last name is some name initially
        Assertions.assertNotNull(oldLastName);

        user.setNoLastName(true);
        userService.addUser(user);
        String newLastName = user.getLastName();

        // Check that the last name is now null
        Assertions.assertNull(newLastName);

        // Check that the no last name still remains true
        Assertions.assertTrue(user.getNoLastName());
    }

    @Test
    void GivenAddUser_NoLastNameWithNoLastName_LastNameNotChanged () {
        // Arrange
        String oldLastName = user.getLastName();

        // Act
        userService.addUser(user);
        String newLastName = user.getLastName();

        // Assert

        // Check that the last name remains the same
        Assertions.assertEquals(oldLastName, newLastName);

        // Check that the no last name remains false
        Assertions.assertFalse(user.getNoLastName());
    }

    @Test
    void GivenAddUser_PasswordHashed () {
        // Arrange
        String oldPassword = user.getPassword();

        // Act
        userService.addUser(user);
        String newPassword = user.getPassword();

        // Assert

        // Check that password has been changed
        Assertions.assertNotEquals(oldPassword, newPassword);

        // Check that the password is set to the one returned by password encoder
        Assertions.assertEquals("aRandomHash1", newPassword);

        // Check that the user repo has called with the passwored updated user
        Mockito.verify(userRepository).save(user);
    }

    @Test
    void GivenEditUser_UserSaved() {
        // Act
        User oldUser = userService.editUser(editUser);

        // Assert
        // Check user repository save called
        Mockito.verify(userRepository).save(user);
    }

    @Test
    void GivenEditUserPassword_PasswordChanged_PasswordUpdatedAndEncoded() {
        // Arrange
        editUser.setPassword("Password2!");

        // Act
        User oldUser = userService.editUserPassword(editUser);

        // Assert
        // Check that the password was updated to second hash
        Assertions.assertEquals("aRandomHash2", oldUser.getPassword());
    }

    @Test
    void GivenEditUserPassword_PasswordNull_PasswordUNotUpdated() {
        // Act
        User oldUser = userService.editUserPassword(editUser);

        // Assert
        // Check that the password was updated and encoded
        Assertions.assertEquals(user.getPassword(), oldUser.getPassword());

        // Check that the passwords are both not null
        Assertions.assertNotNull(oldUser.getPassword());
    }

    @Test
    void GivenEditUser_NewEmail_EmailUpdated() {
        // Arrange
        editUser.setEmail("newEmail@example.com");

        // Act
        User oldUser = userService.editUser(editUser);

        // Assert
        // Check that the password was updated and encoded
        Assertions.assertEquals("newEmail@example.com", oldUser.getEmail());
    }

    @Test
    void GivenEditUser_NewDob_DobUpdated() {
        // Arrange
        editUser.setDob("2000-01-02");

        // Act
        User oldUser = userService.editUser(editUser);

        // Assert
        // Check that the Date of Birth was updated
        Assertions.assertEquals("2000-01-02", oldUser.getDob());
    }

    @Test
    void GivenEditUser_NewFirstName_FirstNameUpdated() {
        // Arrange
        String newFirstName = "NotJohn";
        editUser.setFirstName(newFirstName);

        // Act
        User oldUser = userService.editUser(editUser);

        // Assert
        // Check that the First Name was updated
        Assertions.assertEquals(newFirstName, oldUser.getFirstName());
    }

    @Test
    void GivenEditUser_NewLastName_LastNameUpdated() {
        // Arrange
        String newLastName = "NotDoe";
        editUser.setLastName(newLastName);

        // Act
        User oldUser = userService.editUser(editUser);

        // Assert
        // Check that the Last Name was updated
        Assertions.assertEquals(newLastName, oldUser.getLastName());
    }

    @Test
    void GivenEditUser_NoLastNameSet_NoLastNameFlagUpdated() {
        // Arrange
        boolean newNoLastName = true; // Assuming new No Last Name flag is set
        editUser.setNoLastName(newNoLastName);

        // Act
        User oldUser = userService.editUser(editUser);

        // Assert
        // Check that the No Last Name flag was updated
        Assertions.assertEquals(newNoLastName, oldUser.getNoLastName());
    }

    @Test
    void GivenEditUser_ImageSet_ImageUpdated() {
        // Arrange
        MultipartFile newImage = ImageValidationTest.mockSvgValid;
        editUser.setProfileImage(newImage);

        // Act
        User oldUser = userService.editUser(editUser);

        // Assert
        // Check that the Image was updated
        Assertions.assertEquals((editUser.getId()) + ".svg", oldUser.getProfileImagePath());
        // Check that the size of the stored image is the same as the uploaded image
        Assertions.assertEquals(newImage.getSize(), oldUser.getProfileImage().getSize());
    }

    @Test
    void GivenUserAddsInappropriateTag_CounterIncreases_InappropriateTagCounterUpdated() {
        // Act
        editUser.increaseInappropriateWarningCount();

        // Assert
        // Check that the inappropriate tag counter was increased
        Assertions.assertEquals(1, editUser.getInappropriateWarningCount());
    }

    @Test
     void GivenUserAddsSixthInappropriateTag_UserIsBlocked() {
        // Act
        user.setInappropriateWarningCount(5);
        User newUser = userService.handleInappropriateSubmission(user);

        // Check that the user is blocked
        Assertions.assertTrue(newUser.isBlocked());
    }

    @Test
    void GivenUserAddsFifthInappropriateTag_UserIsNotBlocked() {
        // Act
        user.setInappropriateWarningCount(4);
        User newUser = userService.handleInappropriateSubmission(user);

        // Check that the user is not blocked
        Assertions.assertFalse(newUser.isBlocked());
        // Use manual/e2e test to see warning modal
    }

    @Test
    void GivenUserAddsImageOver2MB_ImageIsResized() throws IOException {
        // Arrange
        String png2mbFilePath = "src/test/resources/test-images/png_over_2mb.png";
        Path png2mbPath = Paths.get(png2mbFilePath);
        String png2mbName = "png_over_2mb";
        String png2mbOriginalFileName = "png_over_2mb.png";
        String png2mbContentType = "image/png";
        byte[] png2mbContent;
        try {
            png2mbContent = Files.readAllBytes(png2mbPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MultipartFile mockPng2mb = new MockMultipartFile(png2mbName, png2mbOriginalFileName, png2mbContentType, png2mbContent);

        // Act
        editUser.setProfileImage(mockPng2mb);
        User oldUser = userService.editUser(editUser);

        // Assert that the Image was updated
        Assertions.assertEquals((editUser.getId())+".png", oldUser.getProfileImagePath());

        // Check that the image is resized
        byte[] resizedImage = oldUser.getProfileImage().getBytes();
        Assertions.assertTrue(resizedImage.length < 2000000);

        // Delete the image
        try {
            Files.delete(Path.of("src/test/resources/test-images/"+editUser.getId()+".png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
