package io.supabase.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
