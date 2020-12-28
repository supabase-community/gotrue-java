package io.supabase.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class SettingsDto {
    @JsonProperty("external")
    Map<String, Boolean> external;
    @JsonProperty("external_labels")
    Object externalLabels;
    @JsonProperty("disable_signup")
    Boolean disableSignup;
    @JsonProperty("autoconfirm")
    Boolean autoconfirm;
}
