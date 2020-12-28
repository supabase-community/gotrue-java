package io.supabase;

import io.supabase.data.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class GoTrueApiTest {
    private static final String url = "http://localhost:9999";
    private static final Map<String, String> headers = new HashMap<>();
    private static GoTrueApi api;
    CredentialsDto creds = new CredentialsDto();

    @BeforeAll
    static void setup() {
        api = new GoTrueApi(url, headers);
    }

    @AfterEach
    void tearDown() {
        // to ensure that the tests dont affect each other
        RestTemplate rest = new RestTemplate();
        rest.delete("http://localhost:3000/users");
    }

    @Test
    void testSignUpWithEmail() {
        AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");
        Utils.assertAuthDto(r);
    }

    @Test
    void testSignUpWithEmail_creds() {
        CredentialsDto creds = new CredentialsDto();
        creds.setEmail("email@example.com");
        creds.setPassword("secret");

        AuthenticationDto r = api.signUpWithEmail(creds);
        Utils.assertAuthDto(r);
    }

    @Test
    void testSignUpWithEmail_AlreadyExists() {
        AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");
        Assertions.assertThrows(RestClientResponseException.class, () -> api.signUpWithEmail("email@example.com", "secret"));
    }

    @Test
    void testSignInWithEmail() {
        // create a user
        api.signUpWithEmail("email@example.com", "secret");
        // login with said user
        AuthenticationDto r = api.signInWithEmail("email@example.com", "secret");
        Utils.assertAuthDto(r);
    }

    @Test
    void testSignInWithEmail_creds() {
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
    void testSignInWithEmail_wrongPass() {
        // create a user
        api.signUpWithEmail("email@example.com", "secret");
        // login with said user
        Assertions.assertThrows(RestClientResponseException.class, () -> api.signInWithEmail("email@example.com", "notSecret"));
    }

    @Test
    void testSignOut() {
        // create a user to get a valid JWT
        AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");
        String jwt = r.getAccessToken();

        Assertions.assertDoesNotThrow(() -> api.signOut(jwt));
    }

    @Test
    void testSignOut_invalidJWT() {
        String jwt = "somethingThatIsNotAValidJWT";
        Assertions.assertThrows(RestClientResponseException.class, () -> api.signOut(jwt));
    }

    @Test
    void testGetUser() {
        // create a user to get a valid JWT
        AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");
        String jwt = r.getAccessToken();

        UserDto user = api.getUser(jwt);
        Utils.assertUserDto(user);
    }

    @Test
    void testGetUser_invalidJWT() {
        String jwt = "somethingThatIsNotAValidJWT";
        Assertions.assertThrows(RestClientResponseException.class, () -> api.getUser(jwt));
    }

    @Test
    void testRefreshAccessToken() {
        // create a user to get a valid refreshToken
        AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");
        String token = r.getRefreshToken();

        AuthenticationDto a = api.refreshAccessToken(token);
        Utils.assertAuthDto(a);
        Assertions.assertNotEquals(r.getAccessToken(), a.getAccessToken());
        Assertions.assertNotEquals(r.getRefreshToken(), a.getRefreshToken());
    }

    @Test
    void testRefreshAccessToken_invalidToken() {
        String token = "noValidToken";
        Assertions.assertThrows(RestClientResponseException.class, () -> api.refreshAccessToken(token));
    }

    @Test
    void testUpdateUser_email() {
        // create a user
        AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");

        UserAttributesDto attr = new UserAttributesDto();
        attr.setEmail("newemail@example.com");

        UserUpdatedDto user = api.updateUser(r.getAccessToken(), attr);
        Utils.assertUserUpdatedDto(user);
        Assertions.assertEquals(user.getNewEmail(), attr.getEmail());
    }

    @Test
    void testUpdateUser_password() {
        // create a user
        AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");

        UserAttributesDto attr = new UserAttributesDto();
        attr.setPassword("pass");

        UserUpdatedDto user = api.updateUser(r.getAccessToken(), attr);

        // normal assert because there is no new email
        Utils.assertUserDto(user);
    }

    @Test
    void testGetUrlForProvider() {
        String url = api.getUrlForProvider("Github");
        Assertions.assertNotNull(url);
        Assertions.assertTrue(url.endsWith("/authorize?provider=Github"));
    }

}
