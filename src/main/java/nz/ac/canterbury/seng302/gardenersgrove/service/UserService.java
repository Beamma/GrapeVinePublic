package nz.ac.canterbury.seng302.gardenersgrove.service;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ForgotPasswordDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ResetPasswordDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.utility.ImageTools;
import nz.ac.canterbury.seng302.gardenersgrove.utility.UserImageStorageProperties;
import nz.ac.canterbury.seng302.gardenersgrove.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import nz.ac.canterbury.seng302.gardenersgrove.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.Objects;
import java.util.logging.Level;

/**
 * The user service class. Works with the user repository.
 */
@Service
public class UserService {
    @Autowired
    private VerificationTokenRepository tokenRepository;

    Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private FriendService friendService;

    /**
     * Constructor for the user service.
     * @param userRepository For storing the users
     * @param passwordEncoder For encrypting passwords
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user for testing
     */
    @PostConstruct
    public void createDummy() {
        try {
            User oldUser1 = getUserByEmail("john.doe@example.com");
            if (oldUser1 != null) {
                userRepository.delete(oldUser1);
            }
            User oldUser2 = getUserByEmail("jane@email.com");
            if (oldUser2 != null) {
                userRepository.delete(oldUser2);
            }
            User user1 =
                    new User("john.doe@example.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
            User user2 =
                    new User("jane@email.com", "2000-01-01", "Jane", "Doe", false, "Password1!", "Password1!");
            user1.setEnabled(true);
            user2.setEnabled(true);
            addUser(user1);
            addUser(user2);
            logger.info("Dummy users added to the database");
        } catch (Exception e) {
            logger.info("Error adding dummy values");
        }
    }

    /**
     * Gets all Users from persistence
     * @return all Users currently saved in persistence
     */
    public List<User> getUser() {
        return userRepository.findAll();
    }

    public User getById(String idString) {
        if (! idString.matches("-?\\d+")) {
            return null;
        }

        Long id = Long.parseLong(idString);
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Adds a user to persistence
     * @param user object to persist
     */
    public void addUser(User user) {

        // Clear last name if no last name ticked
        if (user.getNoLastName() && !(user.getLastName() == null)) {
            user.setLastName(null);
        }

        // Hash the password before saving to the database
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        // Save the user to the database
        userRepository.save(user);
    }

    /**
     * Edits a user
     * @param newUser object to update to
     * @return user to be authenticated
     */
    public User editUser(User newUser) {

        // Get the current user
        User oldUser = getCurrentUser();

        // Update the fields of the old user with the new information
        oldUser.setEmail(newUser.getEmail());
        oldUser.setDob(newUser.getDob());
        oldUser.setFirstName(newUser.getFirstName());
        oldUser.setLastName(newUser.getLastName());
        oldUser.setNoLastName(newUser.getNoLastName());
        // storeUserImage stores the image, sets the users image path and returns void
        String userId = oldUser.getId().toString();
        if (newUser.getProfileImage().getSize() != 0) {
            storeUserImage(newUser.getProfileImage());
        } else if (cachedUserImageExists(userId)) {
            storeCachedUserImage(userId, userId);
        }
        logger.info("New profile image path: " + oldUser.getProfileImagePath());

        // Save the updated user to the repository
        userRepository.save(oldUser);

        return oldUser;
    }

    /**
     * Updates a user.
     * Used for updating a users ban.
     *
     * @param user object to update to
     */
    public void updateUser(User user) {
        userRepository.save(user);
    }

    /**
     * Edits a users password
     * @param newUser object to update to
     * @return user to be authenticated
     */
    public User editUserPassword(User newUser) {
        // Get the current user
        User oldUser = getCurrentUser();
        // Update password if changed
        if (newUser.getPassword() != null && !newUser.getPassword().isEmpty()) {
            oldUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }

        // Save the updated user to the repository
        userRepository.save(oldUser);

        return oldUser;
    }

    /**
     * Created a token for the user if they exist
     * @param forgotPasswordDTO The forgot password DTO
     * @return The user
     */
    public User forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        // Get the user associated with token
        User user = getUserByEmail(forgotPasswordDTO.getEmail());

        // Return if
        if (user == null) {
            return null;
        }

        // Create token and expiry
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDateTime = LocalDateTime.now().plusMinutes(10);

        // Set values
        user.setToken(token);
        user.setTokenExpiry(expiryDateTime);

        // Save the updated user
        userRepository.save(user);

        return user;
    }

    /**
     * Resets the password of a user and clears the token
     * @param resetPasswordDTO The reset password DTO
     * @return The user
     */
    public User resetPassword(ResetPasswordDTO resetPasswordDTO) {
        // Get the user associated with token
        User user = getUserByToken(resetPasswordDTO.getToken());

        // Set and hash the password
        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getPassword()));

        // Reset token
        user.setToken(null);
        user.setTokenExpiry(null);

        // Save the updated user
        userRepository.save(user);

        return user;
    }

    /**
     * Gets user from email
     * @param email of the user
     * @return the user
     */
    public User getUserByEmail(String email) {
        // Retrieve user by email from the repository
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Gets user from token
     * @param token of the user
     * @return An user
     */
    public User getUserByToken(String token) {
        return userRepository.findByToken(token).orElse(null);
    }


    /**
     * Gets the current user that is logged in
     * @return the current user
     */
    public User getCurrentUser() {
        return userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
    }

    /**
     * Search users by email or full name
     * @param keyword search param
     * @param pageRequest use pagination
     * @return list of users that match search
     */
    public Page<User> searchUsers(String keyword, PageRequest pageRequest) {
        if (keyword.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$")) {
            return userRepository.findAllByEmail(keyword, pageRequest, getCurrentUser().getId().toString());
        } else {
            return userRepository.findByFullName(keyword, pageRequest, getCurrentUser().getId().toString());
        }
    }

    public void saveRegisteredUser(User user) {
        userRepository.save(user);
    }

    public User getUserProfile(String userId) throws BadRequestException {
        // Get current User
        User currentUser = getCurrentUser();

        // Check if users are friends
        if (! friendService.checkIfFriends(currentUser.getId(), Long.parseLong(userId))) {
            throw new BadRequestException("You are not friends with this user");
        } else {
            return getById(userId);
        }
    }

    public boolean validateUserId(String userId) {
        if (!userId.matches("-?\\d+")) {
            return false;
        } else if (userId.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public VerificationToken getVerificationToken(String verificationToken) {
        return tokenRepository.findByToken(verificationToken);
    }

    public void createVerificationToken(User user, String token) {
        VerificationToken myToken = new VerificationToken(token, user);
        tokenRepository.save(myToken);
    }

    /**
     * Remove user from database
     * Must remove stored token that references the user first
     * @param user to remove
     */
    public void removeUser(User user) {
        VerificationToken oldToken = tokenRepository.findByUser(user);
        tokenRepository.delete(oldToken);
        userRepository.delete(user);
    }

    private Path rootLocation;
    private Path cacheLocation;

    public void setStorageProperties(UserImageStorageProperties properties) {
        if (properties.getLocation().trim().isEmpty()) {
            throw new RuntimeException("error");
        }
        this.rootLocation = Paths.get(properties.getLocation());
        this.cacheLocation = Paths.get(properties.getCacheLocation());
        properties.createLocation();
    }

    // All image storing methods are based on the tutorial at https://spring.io/guides/gs/uploading-files/

    /**
     * store the multipart file of the users profile image to the database, cropping/resizing if necessary
     * @param file is the multipart file to store
     * @param fileName is the String of the users id
     * @param extension is the type of file
     */
    public void store(MultipartFile file, String fileName, String extension) {
        Path destinationFile;
        try {
            destinationFile = this.rootLocation.resolve(
                    Paths.get(fileName + "." + extension)).toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("error in filepath");
            }
            InputStream inputStream = file.getInputStream();
            ImageTools imageTools = new ImageTools();

            // if image bigger than 2mb, resize to 512x512 image before saving to database to save space
            if (file.getSize() > 2 * 1024 * 1024) {
                inputStream = imageTools.resize(inputStream, 512, 512, extension);
            }
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);

        } catch (RuntimeException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Store user image will store the given image to the user using the store method
     * Sets the Profile Image Path of the current user to match what is saved in the database
     * @param uploadedProfileImage is the multipart file to save
     */
    public void storeUserImage(MultipartFile uploadedProfileImage) {
        String profileImagePath = "";
        if (uploadedProfileImage != null && !uploadedProfileImage.isEmpty()) {
            // if the value being passed in exists
            logger.info(getCurrentUser().getProfileImagePath());
            if (!Objects.equals(getCurrentUser().getProfileImagePath(), null)) {
                // if there is already an image for this user then delete it
                delete(getCurrentUser().getProfileImagePath());
            }
            // get the extension of the file
            String extension = uploadedProfileImage.getContentType().substring(6);
            if (extension.contains("svg+xml")) {
                extension = "svg";
            }
            // store the file in the database with the users id as the file name
            store(uploadedProfileImage, String.format("%s", getCurrentUser().getId())
                    , extension);

            profileImagePath =
                    String.format("%s",
                            getCurrentUser().getId()) + '.' + extension;

            // set the users profile image path here
            getCurrentUser().setProfileImagePath(profileImagePath);
        }
        // else if no multipart file is found, do nothing, leave the current image as is
    }

    /**
     * Delete file from the database
     * @param filename to delete
     */
    public void delete(String filename) {
        try {
            Files.deleteIfExists(this.rootLocation.resolve(filename));
        } catch (IOException e) {
            throw new RuntimeException("error");
        }
    }

    public void removeUserProfileImage() {
        if (getCurrentUser().getProfileImagePath() != null) {
            delete(getCurrentUser().getProfileImagePath());
            getCurrentUser().setProfileImagePath(null);
            userRepository.save(getCurrentUser());
        }
    }

    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }


    /**
     * Cache an image for the user if the form needs repopulated
     * Identify the cached image by the userId
     *
     * @param userImage A multipart file of the userImage
     */
    public void cacheUserImage(MultipartFile userImage, String userId) {
        clearUserImageCache(userId);
        String extension = Objects.requireNonNull(userImage.getOriginalFilename()).split("\\.")[1];
        String fileName = "user" + userId;
        try {
            Path destinationFile = this.cacheLocation.resolve(Paths.get(fileName + "." + extension)).toAbsolutePath();
            if (!destinationFile.getParent().equals(this.cacheLocation.toAbsolutePath())) {
                throw new RuntimeException("error in filepath");
            }
            try (InputStream inputStream = userImage.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Clear any cached user image of the current user
     *
     * @param userId the id of the user
     */
    public void clearUserImageCache(String userId) {
        Path cachedPlantImage = getCachedUserImagePath(userId);
        if (cachedPlantImage != null) {
            try {
                Files.delete(cachedPlantImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Get the cached user image of the current user
     *
     * @param userId the id of the user
     * @return the path to the cached user image or null if none exist
     */
    private Path getCachedUserImagePath(String userId) {
        String fileName = "user" + userId;
        // Copilot suggested using this method to find the image without knowing the extension
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheLocation, fileName + "*")) {
            for (Path entry : stream) {
                return entry;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Function for displaying the cached user image of the current user
     *
     * @param userId the id of the user
     * @return the base64 string of the cached user image or null if none exist
     */
    public String getCachedUserImageBase64(String userId) throws IOException {
        Path imagePath = getCachedUserImagePath(userId);
        if (imagePath == null) {
            return null;
        }
        java.util.logging.Logger.getLogger(PlantService.class.getName()).log(Level.INFO, "Image path: " + cacheLocation.toString() + "/" + imagePath.getFileName());
        String extension = imagePath.toString().split("\\.")[1];
        if (extension.equals("svg")) {
            return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(
                    Files.readAllBytes(Path.of(cacheLocation.toString() + "/" + imagePath.getFileName())));
        } else {
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(
                    Files.readAllBytes(Path.of(cacheLocation.toString() + "/" + imagePath.getFileName())));
        }
    }

    /**
     * Store the cached user image of the current user to the user image directory
     * and clear the cache.
     * For use when the user has successfully submitted the form.
     *
     * @param userId the id of the user
     * @param fileName the name of the file to store
     */
    public void storeCachedUserImage(String userId, String fileName) {
        Path cachedUserImagePathImage = getCachedUserImagePath(userId);
        if (cachedUserImagePathImage != null) {
            String extension = cachedUserImagePathImage.toString().split("\\.")[1];
            try {
                Path destinationFile = this.rootLocation.resolve(Paths.get(fileName + "." + extension)).toAbsolutePath();
                logger.info("Storing cached file: " + destinationFile);
                if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                    throw new RuntimeException("error in filepath");
                }
                if (!Objects.equals(getCurrentUser().getProfileImagePath(), null)) {
                    // if there is already an image for this user then delete it
                    delete(getCurrentUser().getProfileImagePath());
                }
                Files.copy(cachedUserImagePathImage, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                clearUserImageCache(userId);
                getCurrentUser().setProfileImagePath(fileName + "." + extension);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Adds 1 to the current users inappropriate submission count
     * Checks if the user needs to be blocked, and blocks them if necessary
     */
    public User handleInappropriateSubmission(User user) {
        // Increase user inappropriate warning count
        user.increaseInappropriateWarningCount();

        // check if user needs to be blocked
        if (user.getInappropriateWarningCount() >= 6) {
            // Blocks the user for 7 days
            user.setBlocked(true);
            user.setBlockedEndDate(LocalDateTime.now().plusDays(7));
        }
        return userRepository.save(user);
    }
    
    /**
     * Check if a cached user image exists for the current user
     *
     * @param userId the id of the user
     * @return true if a cached user image exists, otherwise false
     */
    public boolean cachedUserImageExists(String userId) {
        return getCachedUserImagePath(userId) != null;
    }

    /**
     * Logs the user out by invalidating the session
     * @param user the user to log out
     * @param request the request from that user whose session is invalidated
     */
    public void logoutUser(User user, HttpServletRequest request) {
        request.getSession().invalidate();
        user.setToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);
    }
}
