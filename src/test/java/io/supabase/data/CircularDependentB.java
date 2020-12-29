package io.supabase.data;

import lombok.Data;

@Data
public class CircularDependentB {
    // to create a circular dependency
    public CircularDependentA a;
}
