package io.supabase;

import io.supabase.exceptions.JwtSecretNotFoundException;
import io.supabase.exceptions.MalformedHeadersException;
import io.supabase.utils.ClientUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClientUtilsTest {
    @AfterEach
    void tearDown() {
        System.clearProperty("gotrue.headers");
        System.clearProperty("gotrue.url");
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
}
