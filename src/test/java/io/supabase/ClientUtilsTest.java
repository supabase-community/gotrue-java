package io.supabase;

import io.supabase.exceptions.JwtSecretNotFoundException;
import io.supabase.exceptions.MalformedHeadersException;
import io.supabase.utils.ClientUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;

public class ClientUtilsTest {
    @AfterEach
    void tearDown() {
        System.clearProperty("gotrue.headers");
        System.clearProperty("gotrue.url");
        System.clearProperty("gotrue.jwt.secret");
    }

    @Test
    void constructor() {
        try {
            Constructor<ClientUtils> c = ClientUtils.class.getDeclaredConstructor();
            c.setAccessible(true);
            AtomicReference<ClientUtils> cUtils = new AtomicReference<>(null);
            Assertions.assertDoesNotThrow(() -> cUtils.set(c.newInstance()));
            Assertions.assertNotNull(cUtils.get());
        } catch (NoSuchMethodException e) {
            Assertions.fail();
        }
    }

    @Test
    void loadHeaders_malformed() {
        // empty but existing headers
        System.setProperty("gotrue.headers", "");
        Assertions.assertThrows(MalformedHeadersException.class, ClientUtils::loadHeaders);

        System.setProperty("gotrue.headers", ":asdfasdf,asdf");
        Assertions.assertThrows(MalformedHeadersException.class, ClientUtils::loadHeaders);
    }

    @Test
    void loadHeaders_valid() {
        // empty but existing headers
        System.setProperty("gotrue.headers", "A=B");
        Assertions.assertDoesNotThrow(ClientUtils::loadHeaders);

        System.setProperty("gotrue.headers", "A=B,   B123=123123");
        Assertions.assertDoesNotThrow(ClientUtils::loadHeaders);
    }

    @Test
    void parseJwt_no_secret() {
        Assertions.assertThrows(JwtSecretNotFoundException.class, () -> ClientUtils.parseJwt("asdf"));
    }

    @Test
    void getJwtSecret_null() {
        try {
            Method m = ClientUtils.class.getDeclaredMethod("getJwtSecret");
            m.setAccessible(true);
            Assertions.assertNull(m.invoke(null));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Assertions.fail();
        }
    }

    @Test
    void getJwtSecret_env() {
        try {
            Method m = ClientUtils.class.getDeclaredMethod("getJwtSecret");
            m.setAccessible(true);
            withEnvironmentVariable("GOTRUE_JWT_SECRET", "superSecretJwtToken")
                    .execute(() -> Assertions.assertEquals("superSecretJwtToken", m.invoke(null)));
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    void getJwtSecret_prop() {
        try {
            Method m = ClientUtils.class.getDeclaredMethod("getJwtSecret");
            m.setAccessible(true);
            System.setProperty("gotrue.jwt.secret", "superSecretJwtToken");
            Assertions.assertEquals("superSecretJwtToken", m.invoke(null));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Assertions.fail();
        }
    }
}
