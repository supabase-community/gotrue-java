package io.supabase;

import io.supabase.data.dto.AuthenticationDto;
import io.supabase.data.dto.SettingsDto;
import io.supabase.data.dto.UserDto;
import io.supabase.data.dto.UserUpdatedDto;
import io.supabase.data.jwt.ParsedToken;
import org.junit.jupiter.api.Assertions;

public class Utils {

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

    protected static void assertSettingsDto(SettingsDto s) {
        Assertions.assertNotNull(s);
        Assertions.assertNotNull(s.getAutoconfirm());
        Assertions.assertNotNull(s.getExternal());
        Assertions.assertNotNull(s.getDisableSignup());
    }

    protected static void assertParsedToken(ParsedToken t) {
        Assertions.assertNotNull(t);
        Assertions.assertNotNull(t.getExp());
        Assertions.assertNotNull(t.getSub());
    }


}
