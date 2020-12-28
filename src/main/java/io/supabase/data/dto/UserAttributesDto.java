package io.supabase.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserAttributesDto {
    @JsonProperty("email")
    String email;
    @JsonProperty("password")
    String password;
    @JsonProperty("email_change_token")
    String emailChangeToken;
    @JsonProperty("data")
    Object data;
}
