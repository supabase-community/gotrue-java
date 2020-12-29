package io.supabase;

import io.jsonwebtoken.ExpiredJwtException;
import io.supabase.data.jwt.ParsedToken;
import io.supabase.data.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class GoTrueClientTest {
    private final String url = "http://localhost:9999";
    private GoTrueClient client;

    @BeforeEach
    void setup_each() {
        client = new GoTrueClient(url);
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
    void loadProperties_no_url() {
        // no env vars nor system properties
        Assertions.assertThrows(RuntimeException.class, GoTrueClient::new);
    }

    @Test
    void constructor() {
        Assertions.assertThrows(RuntimeException.class, GoTrueClient::new);
    }

    @Test
    void constructor_url() {
        Assertions.assertDoesNotThrow(() -> new GoTrueClient(url));
    }

    @Test
    void constructor_url_headers() {
        Map<String, String> headers = new HashMap<>() {{
            put("SomeHeader", "SomeValue");
            put("Another", "3");
        }};
        Assertions.assertDoesNotThrow(() -> new GoTrueClient(url, headers));
    }

    @Test
    void constructor_headers() {
        Map<String, String> headers = new HashMap<>() {{
            put("SomeHeader", "SomeValue");
            put("Another", "3");
        }};
        Assertions.assertThrows(RuntimeException.class, () -> new GoTrueClient(headers));
    }

    @Test
    void loadProperties() {
        System.setProperty("gotrue.url", url);
        Assertions.assertDoesNotThrow((ThrowingSupplier<GoTrueClient>) GoTrueClient::new);

        try {
            System.setProperty("gotrue.headers", "SomeHeader=SomeValue, Another=3");
            GoTrueClient c = new GoTrueClient();
            Field headersField = GoTrueClient.class.getDeclaredField("headers");
            headersField.setAccessible(true);
            Object headersObj = headersField.get(c);
            Assertions.assertTrue(headersObj instanceof Map);
            Map<String, String> headers = (Map<String, String>) headersObj;
            Assertions.assertNotNull(headers);
            Assertions.assertTrue(headers.containsKey("SomeHeader"));
            Assertions.assertEquals(headers.get("SomeHeader"), "SomeValue");
            Assertions.assertTrue(headers.containsKey("Another"));
            Assertions.assertEquals(headers.get("Another"), "3");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    void loadProperties_env() {
        try {
            Utils.setEnv(new HashMap<>() {{
                put("GOTRUE_URL", url);
                put("GOTRUE_HEADERS", "SomeHeader=SomeValue, Another=3");
            }});
            Assertions.assertDoesNotThrow((ThrowingSupplier<GoTrueClient>) GoTrueClient::new);

            GoTrueClient c = new GoTrueClient();
            Field headersField = GoTrueClient.class.getDeclaredField("headers");
            headersField.setAccessible(true);
            Object headersObj = headersField.get(c);
            Assertions.assertTrue(headersObj instanceof Map);
            Map<String, String> headers = (Map<String, String>) headersField.get(c);
            Assertions.assertNotNull(headers);
            Assertions.assertTrue(headers.containsKey("SomeHeader"));
            Assertions.assertEquals(headers.get("SomeHeader"), "SomeValue");
            Assertions.assertTrue(headers.containsKey("Another"));
            Assertions.assertEquals(headers.get("Another"), "3");

            // so it doesnt effect the other tests
            Utils.setEnv(new HashMap<>() {{
                put("GOTRUE_URL", null);
                put("GOTRUE_HEADERS", null);
            }});
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    void signUpWithEmail() {
        AuthenticationDto r = client.signUp("email@example.com", "secret");
        Utils.assertAuthDto(r);
    }

    @Test
    void signUpWithEmail_creds() {
        CredentialsDto creds = new CredentialsDto();
        creds.setEmail("email@example.com");
        creds.setPassword("secret");

        AuthenticationDto r = client.signUp(creds);
        Utils.assertAuthDto(r);
    }

    @Test
    void signInWithEmail() {
        // create a user
        client.signUp("email@example.com", "secret");
        // login with said user
        AuthenticationDto r = client.signIn("email@example.com", "secret");
        Utils.assertAuthDto(r);
    }

    @Test
    void signInWithEmail_creds() {
        CredentialsDto creds = new CredentialsDto();
        creds.setEmail("email@example.com");
        creds.setPassword("secret");

        // create a user
        client.signUp(creds);
        // login with said user
        AuthenticationDto r = client.signIn(creds);
        Utils.assertAuthDto(r);
    }

    @Test
    void updateUser_email() {
        // create a user
        client.signUp("email@example.com", "secret");

        UserAttributesDto attr = new UserAttributesDto();
        attr.setEmail("newemail@example.com");

        UserUpdatedDto user = client.update(attr);
        Utils.assertUserUpdatedDto(user);
        Assertions.assertEquals(user.getNewEmail(), attr.getEmail());
    }

    @Test
    void updateUser_email_jwt_given() {
        // create a user
        AuthenticationDto r = client.signUp("email@example.com", "secret");

        UserAttributesDto attr = new UserAttributesDto();
        attr.setEmail("newemail@example.com");

        UserUpdatedDto user = client.update(r.getAccessToken(), attr);
        Utils.assertUserUpdatedDto(user);
        Assertions.assertEquals(user.getNewEmail(), attr.getEmail());
    }

    @Test
    void signOut() {
        // create a user to get a valid JWT, that is saved
        client.signUp("email@example.com", "secret");

        Assertions.assertDoesNotThrow(() -> client.signOut());
    }

    @Test
    void signOut_jwt() {
        // create a user to get a valid JWT
        AuthenticationDto r = client.signUp("email@example.com", "secret");
        String jwt = r.getAccessToken();

        Assertions.assertDoesNotThrow(() -> client.signOut(jwt));
    }

    @Test
    void getSettings() {
        SettingsDto s = client.settings();
        Utils.assertSettingsDto(s);
    }

    @Test
    void refresh() {
        // create a user to get a valid refreshToken
        AuthenticationDto r = client.signUp("email@example.com", "secret");
        String token = r.getRefreshToken();

        AuthenticationDto a = client.refresh(token);
        Utils.assertAuthDto(a);
        Assertions.assertNotEquals(r.getAccessToken(), a.getAccessToken());
        Assertions.assertNotEquals(r.getRefreshToken(), a.getRefreshToken());
    }

    @Test
    void refresh_current() {
        // create a user to get a valid refreshToken
        AuthenticationDto r = client.signUp("email@example.com", "secret");

        AuthenticationDto a = client.refresh();
        Utils.assertAuthDto(a);
        Assertions.assertNotEquals(r.getAccessToken(), a.getAccessToken());
        Assertions.assertNotEquals(r.getRefreshToken(), a.getRefreshToken());
    }

    @Test
    void refresh_invalid() {
        String token = "noValidToken";
        Assertions.assertThrows(RestClientResponseException.class, () -> client.refresh(token));
    }

    @Test
    void getUser() {
        // create a user to get a valid JWT
        AuthenticationDto r = client.signUp("email@example.com", "secret");
        String jwt = r.getAccessToken();

        UserDto user = client.getUser(jwt);
        Utils.assertUserDto(user);
    }

    @Test
    void getUser_invalidJWT() {
        String jwt = "somethingThatIsNotAValidJWT";
        Assertions.assertThrows(RestClientResponseException.class, () -> client.getUser(jwt));
    }

    @Test
    void getCurrentUser() {
        // create a user
        AuthenticationDto r = client.signUp("email@example.com", "secret");
        UserDto user = client.getCurrentUser();
        Assertions.assertEquals(r.getUser(), user);
    }

    @Test
    void getInstance() {
        // no url specified
        Assertions.assertThrows(RuntimeException.class, GoTrueClient::getInstance);
    }

    @Test
    void getInstance_url() {
        System.setProperty("gotrue.url", url);
        Assertions.assertDoesNotThrow(GoTrueClient::getInstance);
    }

    @Test
    void I() {
        // no url specified
        Assertions.assertThrows(RuntimeException.class, GoTrueClient::I);
    }

    @Test
    void I_url() {
        System.setProperty("gotrue.url", url);
        Assertions.assertDoesNotThrow(GoTrueClient::I);
    }

    @Test
    void parseJwt() {
        System.setProperty("gotrue.jwt.secret", "superSecretJwtToken");
        // create a user
        AuthenticationDto r = client.signUp("email@example.com", "secret");
        ParsedToken parsedToken = client.parseJwt(r.getAccessToken());
        Utils.assertParsedToken(parsedToken);
    }

    @Test
    void parseJwt_expired() {
        System.setProperty("gotrue.jwt.secret", "superSecretJwtToken");
        // some old token
        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1MDkyMDI4ODMsInN1YiI6ImE5NDJiM2QxLTNhNTItNDQ1Ny04YzRmLTg4ZDA3YzJkYmUzMCIsImVtYWlsIjoiZW1haWxAZXhhbXBsZS5jb20iLCJhcHBfbWV0YWRhdGEiOnsicHJvdmlkZXIiOiJlbWFpbCJ9LCJ1c2VyX21ldGFkYXRhIjpudWxsLCJyb2xlIjoiIn0.-DVqBKAqUkcj59rXWqgSkOFegAVTWg1u5slyaUBM_ZU";

        Assertions.assertThrows(ExpiredJwtException.class, () -> client.parseJwt(jwt));
    }

    @Test
    void validate() {
        System.setProperty("gotrue.jwt.secret", "superSecretJwtToken");
        // create a user
        AuthenticationDto r = client.signUp("email@example.com", "secret");

        Assertions.assertTrue(client.validate(r.getAccessToken()));
    }

    @Test
    void validate_expired() {
        System.setProperty("gotrue.jwt.secret", "superSecretJwtToken");
        // some old token
        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1MDkyMDI4ODMsInN1YiI6ImE5NDJiM2QxLTNhNTItNDQ1Ny04YzRmLTg4ZDA3YzJkYmUzMCIsImVtYWlsIjoiZW1haWxAZXhhbXBsZS5jb20iLCJhcHBfbWV0YWRhdGEiOnsicHJvdmlkZXIiOiJlbWFpbCJ9LCJ1c2VyX21ldGFkYXRhIjpudWxsLCJyb2xlIjoiIn0.-DVqBKAqUkcj59rXWqgSkOFegAVTWg1u5slyaUBM_ZU";

        Assertions.assertFalse(client.validate(jwt));
    }
}
