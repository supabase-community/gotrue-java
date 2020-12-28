package io.supabase;

import io.supabase.data.dto.AuthenticationDto;
import io.supabase.data.dto.CredentialsDto;
import io.supabase.data.dto.UserAttributesDto;
import io.supabase.data.dto.UserUpdatedDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class GoTrueClientTest {
    private final String url = "http://localhost:9999";

    @BeforeEach
    void setup() {
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
    void testLoadProperties_no_url() {
        // no env vars nor system properties
        Assertions.assertThrows(RuntimeException.class, () -> new GoTrueClient());
    }

    @Test
    void testContructor() {
        Assertions.assertThrows(RuntimeException.class, () -> new GoTrueClient());
    }

    @Test
    void testContructor_url() {
        Assertions.assertDoesNotThrow(() -> new GoTrueClient(url));
    }

    @Test
    void testContructor_url_headers() {
        Map<String, String> headers = new HashMap<>() {{
            put("SomeHeader", "SomeValue");
            put("Another", "3");
        }};
        Assertions.assertDoesNotThrow(() -> new GoTrueClient(url, headers));
    }

    @Test
    void testContructor_headers() {
        Map<String, String> headers = new HashMap<>() {{
            put("SomeHeader", "SomeValue");
            put("Another", "3");
        }};
        Assertions.assertThrows(RuntimeException.class, () -> new GoTrueClient(headers));
    }

    @Test
    void testLoadProperties() {
        System.setProperty("gotrue.url", url);
        Assertions.assertDoesNotThrow(() -> new GoTrueClient());

        try {
            System.setProperty("gotrue.headers", "SomeHeader=SomeValue, Another=3");
            GoTrueClient c = new GoTrueClient();
            Field headersField = GoTrueClient.class.getDeclaredField("headers");
            headersField.setAccessible(true);
            Map<String, String> headers = (Map<String, String>) headersField.get(c);
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
    void testLoadProperties_env() {
        try {
            Utils.setEnv(new HashMap<>() {{
                put("GOTRUE_URL", url);
                put("GOTRUE_HEADERS", "SomeHeader=SomeValue, Another=3");
            }});
            Assertions.assertDoesNotThrow(() -> new GoTrueClient());

            GoTrueClient c = new GoTrueClient();
            Field headersField = GoTrueClient.class.getDeclaredField("headers");
            headersField.setAccessible(true);
            Map<String, String> headers = (Map<String, String>) headersField.get(c);
            Assertions.assertNotNull(headers);
            Assertions.assertTrue(headers.containsKey("SomeHeader"));
            Assertions.assertEquals(headers.get("SomeHeader"), "SomeValue");
            Assertions.assertTrue(headers.containsKey("Another"));
            Assertions.assertEquals(headers.get("Another"), "3");
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    void testSignUpWithEmail() {
        GoTrueClient c = new GoTrueClient(url);
        AuthenticationDto r = c.signUp("email@example.com", "secret");
        Utils.assertAuthDto(r);
    }

    @Test
    void testSignUpWithEmail_creds() {
        GoTrueClient c = new GoTrueClient(url);
        CredentialsDto creds = new CredentialsDto();
        creds.setEmail("email@example.com");
        creds.setPassword("secret");

        AuthenticationDto r = c.signUp(creds);
        Utils.assertAuthDto(r);
    }

    @Test
    void testSignInWithEmail() {
        GoTrueClient c = new GoTrueClient(url);
        // create a user
        c.signUp("email@example.com", "secret");
        // login with said user
        AuthenticationDto r = c.signIn("email@example.com", "secret");
        Utils.assertAuthDto(r);
    }

    @Test
    void testSignInWithEmail_creds() {
        GoTrueClient c = new GoTrueClient(url);
        CredentialsDto creds = new CredentialsDto();
        creds.setEmail("email@example.com");
        creds.setPassword("secret");

        // create a user
        c.signUp(creds);
        // login with said user
        AuthenticationDto r = c.signIn(creds);
        Utils.assertAuthDto(r);
    }

    @Test
    void testUpdateUser_email() {
        GoTrueClient c = new GoTrueClient(url);
        // create a user
        AuthenticationDto r = c.signUp("email@example.com", "secret");

        UserAttributesDto attr = new UserAttributesDto();
        attr.setEmail("newemail@example.com");

        UserUpdatedDto user = c.update(attr);
        Utils.assertUserUpdatedDto(user);
        Assertions.assertEquals(user.getNewEmail(), attr.getEmail());
    }

    @Test
    void testUpdateUser_email_jwt_given() {
        GoTrueClient c = new GoTrueClient(url);
        // create a user
        AuthenticationDto r = c.signUp("email@example.com", "secret");

        UserAttributesDto attr = new UserAttributesDto();
        attr.setEmail("newemail@example.com");

        UserUpdatedDto user = c.update(r.getAccessToken(), attr);
        Utils.assertUserUpdatedDto(user);
        Assertions.assertEquals(user.getNewEmail(), attr.getEmail());
    }
}
