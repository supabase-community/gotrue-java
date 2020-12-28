package io.supabase;

import io.supabase.data.dto.AuthenticationDto;
import io.supabase.data.dto.UserAttributesDto;
import io.supabase.data.dto.UserDto;
import io.supabase.data.dto.UserUpdatedDto;
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
        assertAuthDto(r);
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
        assertAuthDto(r);
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
        assertUserDto(user);
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
        assertAuthDto(a);
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
        assertUserUpdatedDto(user);
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
        assertUserDto(user);
    }

    @Test
    void testGetUrlForProvider(){
        String url = api.getUrlForProvider("Github");
        Assertions.assertNotNull(url);
        Assertions.assertTrue(url.endsWith("/authorize?provider=Github"));
    }


    void assertAuthDto(AuthenticationDto dto) {
        Assertions.assertNotNull(dto);
        Assertions.assertNotNull(dto.getAccessToken());
        Assertions.assertTrue(dto.getExpiresIn() > 0);
        Assertions.assertNotNull(dto.getRefreshToken());
        Assertions.assertNotNull(dto.getTokenType());
        Assertions.assertNotNull(dto.getUser());
        Assertions.assertNotNull(dto.getUser().getId());
        assertUserDto(dto.getUser());
    }

    void assertUserUpdatedDto(UserUpdatedDto user) {
        Assertions.assertNotNull(user.getNewEmail());
        Assertions.assertNotNull(user.getEmailChangeSentAt());
        assertUserDto(user);
    }

    void assertUserDto(UserDto user) {
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
}
