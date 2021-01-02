package io.supabase.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailCredentialsDto {
    @JsonProperty("email")
    String email;
    @JsonProperty("password")
    String password;
}
