package io.supabase.data.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.supabase.data.dto.UserMetadataDto;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class ParsedToken {
    @JsonProperty("exp")
    Date exp;
    @JsonProperty("sub")
    String sub;
    @JsonProperty("email")
    String email;
    @JsonProperty("app_metadata")
    Map<String, String> appMetadata;
    @JsonProperty("user_metadata")
    UserMetadataDto userMetadata;
    @JsonProperty("role")
    String role;
}
