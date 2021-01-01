package io.supabase;

import io.supabase.data.dto.*;
import io.supabase.exceptions.ApiException;
import io.supabase.exceptions.UrlNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

class GoTrueApiTest {
    private static final String url = "http://localhost:9999";
    private static final Map<String, String> headers = new HashMap<>();
    private static GoTrueApi api;

    @BeforeAll
    static void setup() {
        try {
            api = new GoTrueApi(url, headers);
        } catch (UrlNotFoundException e) {
            // should never get here
            Assertions.fail();
        }
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
    void constructor_valid() {
        Assertions.assertDoesNotThrow(() -> new GoTrueApi(url, headers));
    }

    @Test
    void constructor_invalid() {
        Assertions.assertThrows(UrlNotFoundException.class, () -> new GoTrueApi(null, null));
        Assertions.assertThrows(UrlNotFoundException.class, () -> new GoTrueApi("", null));
    }

    @Test
    void signUpWithEmail() {
        AuthenticationDto r = null;
        try {
            r = api.signUpWithEmail("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertAuthDto(r);
    }

    @Test
    void signUpWithEmail_creds() {
        CredentialsDto creds = new CredentialsDto();
        creds.setEmail("email@example.com");
        creds.setPassword("secret");

        AuthenticationDto r = null;
        try {
            r = api.signUpWithEmail(creds);
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertAuthDto(r);
    }

    @Test
    void signUpWithEmail_AlreadyExists() {
        try {
            api.signUpWithEmail("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        Assertions.assertThrows(ApiException.class, () -> api.signUpWithEmail("email@example.com", "secret"));
    }

    @Test
    void signInWithEmail() {
        AuthenticationDto r = null;
        try {
            // create a user
            api.signUpWithEmail("email@example.com", "secret");

            // login with said user
            r = api.signInWithEmail("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertAuthDto(r);
    }

    @Test
    void signInWithEmail_creds() {
        AuthenticationDto r = null;
        try {
            // create a user
            api.signUpWithEmail("email@example.com", "secret");

            // login with said user
            CredentialsDto creds = new CredentialsDto();
            creds.setEmail("email@example.com");
            creds.setPassword("secret");
            r = api.signInWithEmail(creds);
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertAuthDto(r);
    }

    @Test
    void signInWithEmail_wrongPass() {
        // create a user
        try {
            api.signUpWithEmail("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        // login with said user
        Assertions.assertThrows(ApiException.class, () -> api.signInWithEmail("email@example.com", "notSecret"));
    }

    @Test
    void signOut() {
        // create a user to get a valid JWT
        AuthenticationDto r = null;
        try {
            r = api.signUpWithEmail("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        String jwt = r.getAccessToken();

        Assertions.assertDoesNotThrow(() -> api.signOut(jwt));
    }

    @Test
    void signOut_invalidJWT() {
        String jwt = "somethingThatIsNotAValidJWT";
        Assertions.assertThrows(ApiException.class, () -> api.signOut(jwt));
    }

    @Test
    void getUser() {
        UserDto user = null;
        try {
            // create a user to get a valid JWT
            AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");

            String jwt = r.getAccessToken();

            user = api.getUser(jwt);
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertUserDto(user);
        Assertions.assertNotNull(user.getUserMetadata());
    }

    @Test
    void getUser_invalidJWT() {
        String jwt = "somethingThatIsNotAValidJWT";
        Assertions.assertThrows(ApiException.class, () -> api.getUser(jwt));
    }

    @Test
    void refreshAccessToken() {
        AuthenticationDto r = null;
        AuthenticationDto a = null;
        try {
            // create a user to get a valid refreshToken
            r = api.signUpWithEmail("email@example.com", "secret");

            String token = r.getRefreshToken();

            a = api.refreshAccessToken(token);
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertAuthDto(a);
        Assertions.assertNotEquals(r.getAccessToken(), a.getAccessToken());
        Assertions.assertNotEquals(r.getRefreshToken(), a.getRefreshToken());
    }

    @Test
    void refreshAccessToken_invalidToken() {
        String token = "noValidToken";
        Assertions.assertThrows(ApiException.class, () -> api.refreshAccessToken(token));
    }

    @Test
    void updateUser_email() {
        UserAttributesDto attr = null;
        UserUpdatedDto user = null;
        try {
            // create a user
            AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");

            attr = new UserAttributesDto();
            attr.setEmail("newemail@example.com");

            user = api.updateUser(r.getAccessToken(), attr);
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertUserUpdatedDto(user);
        Assertions.assertEquals(user.getNewEmail(), attr.getEmail());
        Assertions.assertNotNull(user.getUserMetadata());
    }

    @Test
    void updateUser_password() {
        UserUpdatedDto user = null;
        try {
            // create a user
            AuthenticationDto r = api.signUpWithEmail("email@example.com", "secret");

            UserAttributesDto attr = new UserAttributesDto();
            attr.setPassword("pass");

            user = api.updateUser(r.getAccessToken(), attr);
        } catch (ApiException e) {
            Assertions.fail();
        }
        // normal assert because there is no new email attribute
        Utils.assertUserDto(user);
        Assertions.assertNotNull(user.getUserMetadata());
    }

    @Test
    void getUrlForProvider() {
        String url = api.getUrlForProvider("Github");
        Assertions.assertNotNull(url);
        Assertions.assertTrue(url.endsWith("/authorize?provider=Github"));
    }

    @Test
    void getSettings() {
        SettingsDto s = null;
        try {
            s = api.getSettings();
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertSettingsDto(s);
    }

    @Test
    void recoverPassword() {
        AuthenticationDto r = null;
        try {
            // create a user
            r = api.signUpWithEmail("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        final AuthenticationDto finalR = r;
        // send recovery link to user
        Assertions.assertDoesNotThrow(() -> api.recoverPassword(finalR.getUser().getEmail()));
    }

    @Test
    void recoverPassword_no_user() {
        try {
            api.recoverPassword("email@example.com");
            // should throw an exception
            Assertions.fail();
        } catch (ApiException e) {
            // there is no user with the given email
            Assertions.assertTrue(e.getCause().getMessage().startsWith("404 Not Found"));
        }
    }

    @Test
    void magicLink() {
        AuthenticationDto r = null;
        try {
            // create a user
            r = api.signUpWithEmail("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        final AuthenticationDto finalR = r;
        // send recovery link to user
        Assertions.assertDoesNotThrow(() -> api.magicLink(finalR.getUser().getEmail()));
    }

    @Test
    void magicLink_no_user() {
        // there does not already have to be an user registered with the email
        Assertions.assertDoesNotThrow(() -> api.magicLink("email@example.com"));
    }
}
