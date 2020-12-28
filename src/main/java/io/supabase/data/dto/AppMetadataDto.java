package io.supabase.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AppMetadataDto {
    @JsonProperty("provider")
    String provider;
}
