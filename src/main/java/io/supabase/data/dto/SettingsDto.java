package io.supabase.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

@Getter
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
