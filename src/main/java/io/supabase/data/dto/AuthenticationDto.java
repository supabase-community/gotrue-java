package io.supabase.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthenticationDto {
    @JsonProperty("access_token")
    String accessToken;
    @JsonProperty("token_type")
    String tokenType;
    @JsonProperty("expires_in")
    int expiresIn;
    @JsonProperty("refresh_token")
    String refreshToken;
    @JsonProperty("user")
    UserDto user;
}
