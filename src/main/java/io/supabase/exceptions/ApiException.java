package io.supabase.exceptions;

public class ApiException extends Exception {
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiException(String message) {
        super(message);
    }
}
