package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Entity class for a user
 * Contains ID, email, name, dob, password
 */
@Entity
@Table(name= "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = true)
    private String dob;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = true)
    private String lastName;

    @Column(nullable = false)
    private boolean noLastName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String profileImagePath;

    @Column(nullable = true)
    private String token;

    @Column(nullable = true)
    private LocalDateTime tokenExpiry;

    @Column(nullable = false)
    private int inappropriateWarningCount;

    @Column()
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<Authority> userRoles;

    @Column(name = "enabled")
    private boolean enabled;

    private Integer profileImageSize;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean blocked;

    @Column()
    private LocalDateTime blockedEndDate;

    @Transient
    private String passwordRepeat;

    @Transient
    private MultipartFile profileImage;

    /**
     * JPA required no-args constructor
     */
    public User() {}


    /**
     * Creates a new User
     * @param email email of the user
     * @param dob date of birth of the user
     * @param firstName first name of the user
     * @param lastName last name of the user
     * @param noLastName boolean, true if the user has no last name
     * @param password1 password of the user
     * @param password2 repeated password
     */
    public User(String email, String dob, String firstName, String lastName, Boolean noLastName, String password1, String password2) {
        this.email = email;
        this.dob = dob;
        this.firstName = firstName;
        this.lastName = lastName;
        this.noLastName = noLastName;
        this.password = password1;
        this.passwordRepeat = password2;
        this.profileImagePath = null;    //No profile image initially
        this.profileImageSize = 0;       //Profile image size is zero initially
        this.inappropriateWarningCount = 0;  //No inappropriate strikes initially
        this.enabled=false;              //User account is not enabled initially
        this.blocked = false;          //User is not blocked initially
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }

    private String oldPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public boolean getNoLastName() {
        return noLastName;
    }

    /**
     * if true, sets users last name to null
     * @param noLastName boolean, true if user has no lastname
     */
    public void setNoLastName(boolean noLastName) {
        this.noLastName = noLastName;
        if (noLastName) {
            lastName = null;
        }
    }

    public MultipartFile getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(MultipartFile profileImage) {
        this.profileImage = profileImage;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public LocalDateTime getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(LocalDateTime resetTokenExpiry) {
        this.tokenExpiry = resetTokenExpiry;
    }

    public int getInappropriateWarningCount() {
        return inappropriateWarningCount;
    }

    public void increaseInappropriateWarningCount() {
        this.inappropriateWarningCount += 1;
    }

    /**
     * Resets the counter to zero.
     * Used once ban is over.
     */
    public void resetInappropriateWarningCount() {
        this.inappropriateWarningCount = 0;
    }

    /**
     * Adds authorities for a given user, doesn't remove existing authorities.
     * @param authority
     */
    public void grantAuthority(String authority) {
        if ( userRoles == null )
            userRoles = new ArrayList<>();
        userRoles.add(new Authority(authority));
    }

    public void setEnabled(boolean val) {
        this.enabled = val;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    /**
     * Get a list of authorities that the user has
     * @return a list of authorities that the user has
     */
    public List<GrantedAuthority> getAuthorities(){
        List<GrantedAuthority> authorities = new ArrayList<>();
        this.userRoles.forEach(authority -> authorities.add(new SimpleGrantedAuthority(authority.getRole())));
        return authorities;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }
    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
        try {
            this.profileImageSize =
                    Files.readAllBytes(Path.of(System.getProperty("user.dir") + "/src/main/resources/user-images/" +profileImagePath)).length;
        } catch (IOException e) {
            this.profileImageSize = 0;
        }
    }

    public Integer getProfileImageSize() {
        return profileImageSize;
    }

    /**
     * Gets the user image
     * @return the user image as a base64 string
     * @throws IOException if the image cannot be read
     */
    public String getProfileImageBase64() throws IOException {
        if (profileImagePath == null || profileImagePath.isEmpty()) {
            return "../../img/profileicon.png";
        }
        String extension = profileImagePath.substring(profileImagePath.lastIndexOf("."));
        if (extension.equals(".svg")) {
            return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(Path.of(System.getProperty("user.dir") + "/src/main/resources/user-images/" +profileImagePath)));
        } else {
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(Path.of(System.getProperty("user.dir") + "/src/main/resources/user-images/" +profileImagePath)));
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email=" + email + '\'' +
                ", date of birth ='" + dob + '\'' +
                ", first name='" + firstName + '\'' +
                ", last name='" + lastName + '\'' +
                ", no last name='" + noLastName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean isBlocked) {
        this.blocked = isBlocked;
    }

    public boolean hasReachedInappropriateWarningLimit() {
        return this.inappropriateWarningCount == 5;
    }

    public void setInappropriateWarningCount(int inappropriateCount) {
        this.inappropriateWarningCount = inappropriateCount;
    }

    public LocalDateTime getBlockedEndDate() {
        return this.blockedEndDate;
    }

    public void setBlockedEndDate(LocalDateTime date) {
        this.blockedEndDate = date;
    }
}

