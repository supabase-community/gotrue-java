package io.supabase;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class GoTrueClientTest {
    @Test
    void testLoadProperties_no_url() {
        // no env vars nor system properties
        Assertions.assertThrows(RuntimeException.class, () -> new GoTrueClient());
    }

    @Test
    void testLoadProperties() {
        System.setProperty("gotrue.url", "http://localhost:9999");
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
                put("GOTRUE_URL", "http://localhost:9999");
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

}
