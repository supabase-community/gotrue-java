package io.supabase.data;

import lombok.Data;

@Data
public class CircularDependentA {
    // to create a circular dependency
    public CircularDependentB b;
}
