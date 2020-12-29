package io.supabase.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class UserUpdatedDto extends UserDto {
    @JsonProperty("new_email")
    String newEmail;
    @JsonProperty("email_change_sent_at")
    Date emailChangeSentAt;
}
