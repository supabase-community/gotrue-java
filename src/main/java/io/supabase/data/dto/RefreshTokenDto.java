package io.supabase.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RefreshTokenDto {
    @JsonProperty("refresh_token")
    String refreshToken;
}
