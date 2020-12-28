package io.supabase;

import io.supabase.data.dto.AuthenticationDto;
import io.supabase.data.dto.CredentialsDto;
import io.supabase.utils.ClientUtils;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

public class GoTrueClient {
    private final GoTrueApi api;
    private final String url;
    private final Map<String, String> headers;

    public GoTrueClient(String url, Map<String, String> headers) {
        this.url = url != null ? url : ClientUtils.loadUrl();
        this.headers = headers != null ? headers : ClientUtils.loadHeaders();
        api = new GoTrueApi(url, headers);
    }

    public GoTrueClient(Map<String, String> headers) {
        this.url = ClientUtils.loadUrl();
        this.headers = headers != null ? headers : ClientUtils.loadHeaders();
        api = new GoTrueApi(url, headers);
    }

    public GoTrueClient(String url) {
        this.url = url != null ? url : ClientUtils.loadUrl();
        this.headers = ClientUtils.loadHeaders();
        api = new GoTrueApi(url, headers);
    }

    public GoTrueClient() {
        url = ClientUtils.loadUrl();
        headers = ClientUtils.loadHeaders();
        api = new GoTrueApi(url, headers);
    }

    /**
     * Logs in an existing user using their email address.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return Details about the authentication.
     * @throws RestClientResponseException
     */
    public AuthenticationDto signIn(String email, String password) throws RestClientResponseException {
        return api.signInWithEmail(email, password);
    }

    /**
     * Logs in an existing user using their email address.
     *
     * @param credentials Object with the email and the password of the user.
     * @return Details about the authentication.
     * @throws RestClientResponseException
     */
    public AuthenticationDto signIn(CredentialsDto credentials) throws RestClientResponseException {
        return api.signInWithEmail(credentials);
    }

    /**
     * Creates a new user using their email address.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return Details about the authentication.
     * @throws RestClientResponseException
     */
    public AuthenticationDto signUp(String email, String password) throws RestClientResponseException {
        return api.signUpWithEmail(email, password);
    }

    /**
     * Creates a new user using their email address.
     *
     * @param credentials Object with the email and the password of the user.
     * @return Details about the authentication.
     * @throws RestClientResponseException
     */
    public AuthenticationDto signUp(CredentialsDto credentials) throws RestClientResponseException {
        return api.signUpWithEmail(credentials);
    }
}
