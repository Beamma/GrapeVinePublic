# SENG302 Team 900 Project

Gardening site using ```gradle```, ```Spring Boot```, ```Thymeleaf```, and ```GitLab CI```.

## Our Project

The Gardener's Grove is a site that allows users to create and manage their gardens and plants, and to keep track of
gardens they are interested in. Users can add plants, images, and more to the gardens they create, as well as connecting
with friends, tagging their gardens, and browsing either newly created gardens, or gardens that match their searches.

Users can also create posts about their gardens or plants, and even upload images with their posts. Users may also
interact with other users' posts, by liking and commenting on posts, and liking other users' comments.

## How To Access the Project

You can access our project on either our [production server](https://csse-seng302-team900.canterbury.ac.nz/prod/) or
our [development server](https://csse-seng302-team900.canterbury.ac.nz/test/)

### User Accounts

| Account Type | Email                | Password   |
|--------------|----------------------|------------|
| User         | john.doe@example.com | Password1! |
| User         | jane@email.com       | Password1! |

## How To Run Locally

### 1 - Set Up Project Resources

Add a .env file to the resources folder containing the following properties:

```
EMAIL_USERNAME=
EMAIL_PASSWORD=
DB_USERNAME=
DB_PASSWORD=
WEATHER_API_KEY=
LOCATION_API_KEY=
PROFANITY_FILTER_API_KEY=
AGORA_APP_ID=
AGORA_CERT=
CUSTOMER_KEY=
CUSTOMER_SECRET=
```

### 2 - Running the Project

From the root directory ...

On Linux:

```
./gradlew bootRun
```

On Windows:

```
gradlew bootRun
```

By default, the application will run on local port 8080 [http://localhost:8080](http://localhost:8080)

### 3 - Using the Application

To use the application, open [http://localhost:8080](http://localhost:8080)
> - Follow the instructions on the home page to log in or create an account
> - For more help, see
    our [User Manual](https://docs.google.com/document/d/1kxYIpkpsRv_hdBEkd0a5hhGmkYkXUwBFnTNMlABz3Gg)

## How to Run Tests

> To run the tests for the application, right-click the test directory and then click run on the folder. All the tests
> for the application will be run.
> Alternatively, run ```./gradlew check```

## Dependencies

### Basics

- `org.springframework.boot:spring-boot-starter`
- `org.springframework.boot:spring-boot-starter-thymeleaf`
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-starter-data-jpa`
- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework.security:spring-security-test`

### Mail

- `org.springframework.boot:spring-boot-starter-mail`

### Testing

- `io.cucumber:cucumber-java:7.15.0`
- `io.cucumber:cucumber-junit-platform-engine:7.15.0`
- `org.junit.platform:junit-platform-suite:1.10.2`
- `org.springframework.boot:spring-boot-starter-test`

## Contributors

- This project was made in conjunction with the University Of Canterbury SENG302 Course:  https://courseinfo.canterbury.ac.nz/GetCourseDetails.aspx?course=SENG302
- Isaac Ure
- Joel Bremner
- Sam Miller
- Benjamin Ross
- Joseph Hendry
- Leila Paul
- Harriet Melton
- Alex Long

## Intellectual Property

Intellectual Property is owned by the above mentioned contributers and the University Of Canterbury

## References

- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring JPA docs](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Thymeleaf Docs](https://www.thymeleaf.org/documentation.html)
- [Learn resources](https://learn.canterbury.ac.nz/course/view.php?id=17797&section=8)

## License

Distributed under the GNU General Public License. See LICENSE for more information.
