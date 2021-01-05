package io.supabase;

import io.jsonwebtoken.ExpiredJwtException;
import io.supabase.data.dto.*;
import io.supabase.data.jwt.ParsedToken;
import io.supabase.exceptions.ApiException;
import io.supabase.exceptions.JwtSecretNotFoundException;
import io.supabase.exceptions.MalformedHeadersException;
import io.supabase.exceptions.UrlNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;

class GoTrueClientTest {
    private final String url = "http://localhost:9999";
    private GoTrueClient client;

    @BeforeEach
    void setup_each() {
        try {
            client = new GoTrueClient(url);
        } catch (UrlNotFoundException | MalformedHeadersException e) {
            // should never get here
            Assertions.fail();
        }
        // to ensure that there is nothing specified
        System.clearProperty("gotrue.url");
        System.clearProperty("gotrue.headers");
        System.clearProperty("gotrue.jwt.secret");
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
        Assertions.assertThrows(UrlNotFoundException.class, GoTrueClient::new);
    }

    @Test
    void constructor() {
        Assertions.assertThrows(UrlNotFoundException.class, GoTrueClient::new);
    }

    @Test
    void constructor_url() {
        Assertions.assertDoesNotThrow(() -> new GoTrueClient(url));
        Assertions.assertThrows(UrlNotFoundException.class, () -> new GoTrueClient((String) null));
    }

    @Test
    void constructor_headers() {
        Map<String, String> headers = new HashMap<String, String>() {{
            put("SomeHeader", "SomeValue");
            put("Another", "3");
        }};
        Assertions.assertThrows(UrlNotFoundException.class, () -> new GoTrueClient(headers));

        System.setProperty("gotrue.url", url);
        Assertions.assertDoesNotThrow(() -> new GoTrueClient((Map<String, String>) null));
        Assertions.assertDoesNotThrow(() -> new GoTrueClient(headers));
    }

    @Test
    void constructor_url_headers() {
        Assertions.assertThrows(UrlNotFoundException.class, () -> new GoTrueClient(null, null));

        Map<String, String> headers = new HashMap<String, String>() {{
            put("SomeHeader", "SomeValue");
            put("Another", "3");
        }};
        Assertions.assertDoesNotThrow(() -> new GoTrueClient(url, headers));
        Assertions.assertDoesNotThrow(() -> new GoTrueClient(url, null));
        Assertions.assertDoesNotThrow(() -> new GoTrueClient(url, new HashMap<>()));

        System.setProperty("gotrue.url", url);
        Assertions.assertDoesNotThrow(() -> new GoTrueClient(null, null));
    }

    @Test
    @SuppressWarnings("unchecked")
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
            Assertions.assertEquals("SomeValue", headers.get("SomeHeader"));
            Assertions.assertTrue(headers.containsKey("Another"));
            Assertions.assertEquals("3", headers.get("Another"));
        } catch (NoSuchFieldException | IllegalAccessException | UrlNotFoundException | MalformedHeadersException e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    void loadProperties_env() {
        try {
            withEnvironmentVariable("GOTRUE_URL", url)
                    .execute(this::constructorWithEnv_url);
            withEnvironmentVariable("GOTRUE_HEADERS", "SomeHeader=SomeValue, Another=3")
                    .execute(this::constructorWithEnv_headers);
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    void constructorWithEnv_url() {
        // does not throw -> url is defined
        Assertions.assertDoesNotThrow((ThrowingSupplier<GoTrueClient>) GoTrueClient::new);
    }

    @SuppressWarnings("unchecked")
    void constructorWithEnv_headers() {
        try {
            // set url so it doesnt throw
            System.setProperty("gotrue.url", url);
            Assertions.assertDoesNotThrow((ThrowingSupplier<GoTrueClient>) GoTrueClient::new);

            GoTrueClient c = new GoTrueClient();
            Field headersField = GoTrueClient.class.getDeclaredField("headers");
            headersField.setAccessible(true);
            Object headersObj = headersField.get(c);
            Assertions.assertTrue(headersObj instanceof Map);
            Map<String, String> headers = (Map<String, String>) headersField.get(c);
            Assertions.assertNotNull(headers);
            Assertions.assertTrue(headers.containsKey("SomeHeader"));
            Assertions.assertEquals("SomeValue", headers.get("SomeHeader"));
            Assertions.assertTrue(headers.containsKey("Another"));
            Assertions.assertEquals("3", headers.get("Another"));
        } catch (IllegalAccessException | NoSuchFieldException | UrlNotFoundException | MalformedHeadersException e) {
            Assertions.fail();
        }
    }

    @Test
    void signUpWithEmail() {
        AuthenticationDto r = null;
        try {
            r = client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertAuthDto(r);
    }

    @Test
    void signUpWithEmail_nulls() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signUp(null, "secret"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signUp(null, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signUp("example@domain.com", null));
    }

    @Test
    void signUpWithEmail_credentials() {
        CredentialsDto credentials = new CredentialsDto();
        credentials.setEmail("email@example.com");
        credentials.setPassword("secret");

        AuthenticationDto r = null;
        try {
            r = client.signUp(credentials);
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertAuthDto(r);
    }

    @Test
    void signUpWithEmail_credentials_nulls() {
        CredentialsDto credentials = new CredentialsDto();
        credentials.setEmail(null);
        credentials.setPassword(null);

        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signUp(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signUp(credentials));
        credentials.setEmail("example@domain.com");
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signUp(credentials));
        credentials.setEmail(null);
        credentials.setPassword("secret");
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signUp(credentials));

    }

    @Test
    void signInWithEmail() {
        AuthenticationDto r = null;
        try {
            // create a user
            client.signUp("email@example.com", "secret");
            // login with said user
            r = client.signIn("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertAuthDto(r);
    }

    @Test
    void signInWithEmail_nullsEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signIn(null, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signIn("example@domain.com", null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signIn(null, "secret"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signIn("", ""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signIn("example@domain.com", ""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signIn("", "secret"));
    }

    @Test
    void signInWithEmail_credentials() {
        CredentialsDto credentials = new CredentialsDto();
        credentials.setEmail("email@example.com");
        credentials.setPassword("secret");

        AuthenticationDto r = null;
        try {
            // create a user
            client.signUp(credentials);
            // login with said user
            r = client.signIn(credentials);
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertAuthDto(r);
    }

    @Test
    void signInWithEmail_credentials_nulls() {
        CredentialsDto credentials = new CredentialsDto();
        credentials.setEmail(null);
        credentials.setPassword(null);

        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signIn(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signIn(credentials));
        credentials.setEmail("example@domain.com");
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signIn(credentials));
        credentials.setEmail(null);
        credentials.setPassword("secret");
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signIn(credentials));
    }

    @Test
    void updateUser_email() {
        // create a user
        try {
            client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }

        UserAttributesDto attr = new UserAttributesDto();
        attr.setEmail("newemail@example.com");

        UserUpdatedDto user = null;
        try {
            user = client.update(attr);
        } catch (ApiException e) {
            Assertions.fail();
        }

        Utils.assertUserUpdatedDto(user);
        Assertions.assertNotNull(user.getUserMetadata());
        Assertions.assertEquals(user.getNewEmail(), attr.getEmail());

        try {
            user = client.update(client.getCurrentAuth().getAccessToken(), attr);
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertUserUpdatedDto(user);
        Assertions.assertNotNull(user.getUserMetadata());
        Assertions.assertEquals(user.getNewEmail(), attr.getEmail());
    }

    @Test
    void updateUser_nullEmpty() {
        UserAttributesDto attr = new UserAttributesDto();
        attr.setEmail("newemail@example.com");
        // not logged in
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.update(attr));

        // create a user
        try {
            client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }

        Assertions.assertThrows(IllegalArgumentException.class, () -> client.update(null));

        Assertions.assertThrows(IllegalArgumentException.class, () -> client.update(null, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.update("", null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.update(client.getCurrentAuth().getAccessToken(), null));
    }

    @Test
    void updateUser_email_jwt_given() {
        // create a user
        AuthenticationDto r = null;
        try {
            r = client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }

        UserAttributesDto attr = new UserAttributesDto();
        attr.setEmail("newemail@example.com");

        UserUpdatedDto user = null;
        try {
            user = client.update(r.getAccessToken(), attr);
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertUserUpdatedDto(user);
        Assertions.assertNotNull(user.getUserMetadata());
        Assertions.assertEquals(user.getNewEmail(), attr.getEmail());
    }

    @Test
    void signOut() {
        // create a user to get a valid JWT, that is saved
        try {
            client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }

        Assertions.assertDoesNotThrow(() -> client.signOut());
    }

    @Test
    void signOut_nullEmpty() {
        // not logged in
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signOut());
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signOut(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.signOut(""));
    }


    @Test
    void signOut_jwt() {
        // create a user to get a valid JWT
        AuthenticationDto r = null;
        try {
            r = client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        String jwt = r.getAccessToken();

        Assertions.assertDoesNotThrow(() -> client.signOut(jwt));
    }

    @Test
    void getSettings() {
        SettingsDto s = null;
        try {
            s = client.settings();
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertSettingsDto(s);
    }

    @Test
    void refresh() {
        // create a user to get a valid refreshToken
        AuthenticationDto r = null;
        try {
            r = client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        String token = r.getRefreshToken();

        AuthenticationDto a = null;
        try {
            a = client.refresh(token);
        } catch (ApiException e) {
            Assertions.fail();
        }

        Utils.assertAuthDto(a);
        Assertions.assertNotEquals(r.getAccessToken(), a.getAccessToken());
        Assertions.assertNotEquals(r.getRefreshToken(), a.getRefreshToken());
    }

    @Test
    void refresh_current() {
        // create a user to get a valid refreshToken
        AuthenticationDto r = null;
        try {
            r = client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }

        AuthenticationDto a = null;
        try {
            a = client.refresh();
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertAuthDto(a);
        Assertions.assertNotEquals(r.getAccessToken(), a.getAccessToken());
        Assertions.assertNotEquals(r.getRefreshToken(), a.getRefreshToken());
    }

    @Test
    void refresh_nullInvalid() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.refresh(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.refresh());

        String token = "noValidToken";
        Assertions.assertThrows(ApiException.class, () -> client.refresh(token));

        Assertions.assertThrows(IllegalArgumentException.class, () -> client.refresh(""));
    }

    @Test
    void getUser() {
        // create a user to get a valid JWT
        AuthenticationDto r = null;
        try {
            r = client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        String jwt = r.getAccessToken();

        UserDto user = null;
        try {
            user = client.getUser(jwt);
        } catch (ApiException e) {
            Assertions.fail();
        }
        Utils.assertUserDto(user);
        Assertions.assertNotNull(user.getUserMetadata());
    }

    @Test
    void getUser_invalidJWT() {
        String jwt = "somethingThatIsNotAValidJWT";
        Assertions.assertThrows(ApiException.class, () -> client.getUser(jwt));
    }

    @Test
    void getUser_nullEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.getUser(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.getUser(""));
    }


    @Test
    void getCurrentUser() {
        // create a user
        AuthenticationDto r = null;
        try {
            r = client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        UserDto user = client.getCurrentUser();
        Assertions.assertEquals(r.getUser(), user);
    }


    @Test
    void getCurrentUser_null() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.getCurrentUser());
    }

    @Test
    void getCurrentAuth() {
        // create a user
        AuthenticationDto r = null;
        try {
            r = client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }

        Assertions.assertEquals(r, client.getCurrentAuth());
    }

    @Test
    void getCurrentAuth_null() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.getCurrentAuth());
    }

    @Test
    void getInstance() {
        // no url specified
        Assertions.assertThrows(UrlNotFoundException.class, GoTrueClient::getInstance);
    }

    @Test
    void getInstance_url() {
        System.setProperty("gotrue.url", url);
        Assertions.assertDoesNotThrow(GoTrueClient::getInstance);
    }

    @Test
    void I() {
        // no url specified
        Assertions.assertThrows(UrlNotFoundException.class, GoTrueClient::i);
    }

    @Test
    void I_url() {
        System.setProperty("gotrue.url", url);
        Assertions.assertDoesNotThrow(GoTrueClient::i);
    }

    @Test
    void parseJwt() {
        // provide secret
        System.setProperty("gotrue.jwt.secret", "superSecretJwtToken");
        // create a user
        AuthenticationDto r = null;
        try {
            r = client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        ParsedToken parsedToken = null;
        try {
            parsedToken = client.parseJwt(r.getAccessToken());
        } catch (JwtSecretNotFoundException e) {
            // should not happen
            Assertions.fail();
        }
        Utils.assertParsedToken(parsedToken);
    }

    @Test
    void parseJwt_metadata_provided() {
        // provide secret
        System.setProperty("gotrue.jwt.secret", "superSecretJwtToken");
        AuthenticationDto r = null;
        UserAttributesDto attr = new UserAttributesDto();
        attr.setData(new HashMap<String, Object>() {{
            put("name", "UserName");
            put("age", 33);
        }});
        try {
            client.signUp("email@example.com", "secret");
            client.update(attr);
            r = client.signIn("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }

        ParsedToken parsedToken = null;
        try {
            parsedToken = client.parseJwt(r.getAccessToken());
        } catch (JwtSecretNotFoundException e) {
            Assertions.fail();
        }
        Utils.assertParsedToken(parsedToken);
    }

    @Test
    void parseJwt_no_secret() {
        try {
            // create a user
            AuthenticationDto r = client.signUp("email@example.com", "secret");
            Assertions.assertThrows(JwtSecretNotFoundException.class, () -> client.parseJwt(r.getAccessToken()));
        } catch (ApiException e) {
            Assertions.fail();
        }
    }

    @Test
    void parseJwt_expired() {
        System.setProperty("gotrue.jwt.secret", "superSecretJwtToken");
        // some old token
        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1MDkyMDI4ODMsInN1YiI6ImE5NDJiM2QxLTNhNTItNDQ1Ny04YzRmLTg4ZDA3YzJkYmUzMCIsImVtYWlsIjoiZW1haWxAZXhhbXBsZS5jb20iLCJhcHBfbWV0YWRhdGEiOnsicHJvdmlkZXIiOiJlbWFpbCJ9LCJ1c2VyX21ldGFkYXRhIjpudWxsLCJyb2xlIjoiIn0.-DVqBKAqUkcj59rXWqgSkOFegAVTWg1u5slyaUBM_ZU";

        Assertions.assertThrows(ExpiredJwtException.class, () -> client.parseJwt(jwt));
    }

    @Test
    void parseJwt_nullEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.parseJwt(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.parseJwt(""));
    }

    @Test
    void validate() {
        // provide secret
        System.setProperty("gotrue.jwt.secret", "superSecretJwtToken");
        // create a user
        AuthenticationDto r = null;
        try {
            r = client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }

        try {
            Assertions.assertTrue(client.validate(r.getAccessToken()));
        } catch (JwtSecretNotFoundException e) {
            // should not happen
            Assertions.fail();
        }
    }

    @Test
    void validate_expired() {
        System.setProperty("gotrue.jwt.secret", "superSecretJwtToken");
        // some old token
        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1MDkyMDI4ODMsInN1YiI6ImE5NDJiM2QxLTNhNTItNDQ1Ny04YzRmLTg4ZDA3YzJkYmUzMCIsImVtYWlsIjoiZW1haWxAZXhhbXBsZS5jb20iLCJhcHBfbWV0YWRhdGEiOnsicHJvdmlkZXIiOiJlbWFpbCJ9LCJ1c2VyX21ldGFkYXRhIjpudWxsLCJyb2xlIjoiIn0.-DVqBKAqUkcj59rXWqgSkOFegAVTWg1u5slyaUBM_ZU";

        try {
            Assertions.assertFalse(client.validate(jwt));
        } catch (JwtSecretNotFoundException e) {
            // should not happen
            Assertions.fail();
        }
    }

    @Test
    void validate_nullEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.validate(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.validate(""));
    }

    @Test
    void recoverPassword() {
        AuthenticationDto r = null;
        try {
            // create a user
            r = client.signUp("email@example.com", "secret");
        } catch (ApiException e) {
            Assertions.fail();
        }
        final AuthenticationDto finalR = r;
        // send recovery link to user
        Assertions.assertDoesNotThrow(() -> client.recover(finalR.getUser().getEmail()));
    }

    @Test
    void recoverPassword_no_user() {
        try {
            client.recover("email@example.com");
            // should throw an exception
            Assertions.fail();
        } catch (ApiException e) {
            // there is no user with the given email
            Assertions.assertTrue(e.getCause().getMessage().startsWith("404 Not Found"));
        }
    }


    @Test
    void recoverPassword_nullEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.recover(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.recover(""));
    }
}
