package io.supabase.data.resources;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Credentials {
    String email;
    String password;
    String provider;
}
