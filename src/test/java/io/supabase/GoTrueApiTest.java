package io.supabase;

import io.supabase.data.dto.AuthenticationDto;
import io.supabase.data.dto.UserDto;
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
        Assertions.assertNotNull(r);
        Assertions.assertNotNull(r.getAccessToken());
        Assertions.assertTrue(r.getExpiresIn() > 0);
        Assertions.assertNotNull(r.getRefreshToken());
        Assertions.assertNotNull(r.getTokenType());
        Assertions.assertNotNull(r.getUser());
        Assertions.assertNotNull(r.getUser().getId());
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
        Assertions.assertNotNull(r);
        Assertions.assertNotNull(r.getAccessToken());
        Assertions.assertTrue(r.getExpiresIn() > 0);
        Assertions.assertNotNull(r.getRefreshToken());
        Assertions.assertNotNull(r.getTokenType());
        Assertions.assertNotNull(r.getUser());
        Assertions.assertNotNull(r.getUser().getId());
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
        Assertions.assertNotNull(user);
        Assertions.assertNotNull(user.getId());
        Assertions.assertNotNull(user.getAud());
        Assertions.assertNotNull(user.getEmail());
        Assertions.assertNotNull(user.getCreatedAt());
        Assertions.assertNotNull(user.getRole());
        Assertions.assertNotNull(user.getLastSignInAt());
        Assertions.assertNotNull(user.getConfirmedAt());
        Assertions.assertNotNull(user.getCreatedAt());
        Assertions.assertNotNull(user.getUpdatedAt());
    }

    @Test
    void testGetUser_invalidJWT() {
        String jwt = "somethingThatIsNotAValidJWT";
        Assertions.assertThrows(RestClientResponseException.class, () -> api.getUser(jwt));
    }
}
