package io.supabase;

import io.supabase.data.dto.AuthenticationDto;
import io.supabase.data.dto.UserDto;
import io.supabase.data.dto.UserUpdatedDto;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

public class Utils {
    // source https://stackoverflow.com/a/7201825
    protected static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }

    protected static void assertAuthDto(AuthenticationDto dto) {
        Assertions.assertNotNull(dto);
        Assertions.assertNotNull(dto.getAccessToken());
        Assertions.assertTrue(dto.getExpiresIn() > 0);
        Assertions.assertNotNull(dto.getRefreshToken());
        Assertions.assertNotNull(dto.getTokenType());
        Assertions.assertNotNull(dto.getUser());
        Assertions.assertNotNull(dto.getUser().getId());
        assertUserDto(dto.getUser());
    }

    protected static void assertUserUpdatedDto(UserUpdatedDto user) {
        Assertions.assertNotNull(user.getNewEmail());
        Assertions.assertNotNull(user.getEmailChangeSentAt());
        assertUserDto(user);
    }

    protected static void assertUserDto(UserDto user) {
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
