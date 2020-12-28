package io.supabase;

import io.supabase.data.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class GoTrueApiTest {
    private static final String url = "http://localhost:9999";
    private static final Map<String, String> headers = new HashMap<>();
    private static GoTrueApi api;

    @BeforeAll
    static void setup() {
        api = new GoTrueApi(url, headers);
    }

    @BeforeEach
    void setupEach() {
        // to ensure that there is nothing specified
        System.clearProperty("gotrue.url");
        System.clearProperty("gotrue.headers");
    }

    @AfterEach
    void tearDown() {
        // to ensure that the tests dont affect each other
        RestTemplate rest = new RestTemplate();
        rest.delete("http://localhost:3000/users");
    }

    @Test
    void signUpWithEmail() {
        AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");
        Utils.assertAuthDto(r);
    }

    @Test
    void signUpWithEmail_creds() {
        CredentialsDto creds = new CredentialsDto();
        creds.setEmail("email@example.com");
        creds.setPassword("secret");

        AuthenticationDto r = api.signUpWithEmail(creds);
        Utils.assertAuthDto(r);
    }

    @Test
    void signUpWithEmail_AlreadyExists() {
        api.signUpWithEmail("email@example.com", "secret");
        Assertions.assertThrows(RestClientResponseException.class, () -> api.signUpWithEmail("email@example.com", "secret"));
    }

    @Test
    void signInWithEmail() {
        // create a user
        api.signUpWithEmail("email@example.com", "secret");
        // login with said user
        AuthenticationDto r = api.signInWithEmail("email@example.com", "secret");
        Utils.assertAuthDto(r);
    }

    @Test
    void signInWithEmail_creds() {
        // create a user
        api.signUpWithEmail("email@example.com", "secret");
        // login with said user
        CredentialsDto creds = new CredentialsDto();
        creds.setEmail("email@example.com");
        creds.setPassword("secret");
        AuthenticationDto r = api.signInWithEmail(creds);
        Utils.assertAuthDto(r);
    }

    @Test
    void signInWithEmail_wrongPass() {
        // create a user
        api.signUpWithEmail("email@example.com", "secret");
        // login with said user
        Assertions.assertThrows(RestClientResponseException.class, () -> api.signInWithEmail("email@example.com", "notSecret"));
    }

    @Test
    void signOut() {
        // create a user to get a valid JWT
        AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");
        String jwt = r.getAccessToken();

        Assertions.assertDoesNotThrow(() -> api.signOut(jwt));
    }

    @Test
    void signOut_invalidJWT() {
        String jwt = "somethingThatIsNotAValidJWT";
        Assertions.assertThrows(RestClientResponseException.class, () -> api.signOut(jwt));
    }

    @Test
    void getUser() {
        // create a user to get a valid JWT
        AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");
        String jwt = r.getAccessToken();

        UserDto user = api.getUser(jwt);
        Utils.assertUserDto(user);
    }

    @Test
    void getUser_invalidJWT() {
        String jwt = "somethingThatIsNotAValidJWT";
        Assertions.assertThrows(RestClientResponseException.class, () -> api.getUser(jwt));
    }

    @Test
    void refreshAccessToken() {
        // create a user to get a valid refreshToken
        AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");
        String token = r.getRefreshToken();

        AuthenticationDto a = api.refreshAccessToken(token);
        Utils.assertAuthDto(a);
        Assertions.assertNotEquals(r.getAccessToken(), a.getAccessToken());
        Assertions.assertNotEquals(r.getRefreshToken(), a.getRefreshToken());
    }

    @Test
    void refreshAccessToken_invalidToken() {
        String token = "noValidToken";
        Assertions.assertThrows(RestClientResponseException.class, () -> api.refreshAccessToken(token));
    }

    @Test
    void updateUser_email() {
        // create a user
        AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");

        UserAttributesDto attr = new UserAttributesDto();
        attr.setEmail("newemail@example.com");

        UserUpdatedDto user = api.updateUser(r.getAccessToken(), attr);
        Utils.assertUserUpdatedDto(user);
        Assertions.assertEquals(user.getNewEmail(), attr.getEmail());
    }

    @Test
    void updateUser_password() {
        // create a user
        AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");

        UserAttributesDto attr = new UserAttributesDto();
        attr.setPassword("pass");

        UserUpdatedDto user = api.updateUser(r.getAccessToken(), attr);

        // normal assert because there is no new email
        Utils.assertUserDto(user);
    }

    @Test
    void getUrlForProvider() {
        String url = api.getUrlForProvider("Github");
        Assertions.assertNotNull(url);
        Assertions.assertTrue(url.endsWith("/authorize?provider=Github"));
    }

}
