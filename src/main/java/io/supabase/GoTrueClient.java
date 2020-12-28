package io.supabase;

import io.supabase.data.dto.*;
import io.supabase.utils.ClientUtils;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

public class GoTrueClient {
    private final GoTrueApi api;
    private final String url;
    private final Map<String, String> headers;
    private AuthenticationDto currentAuth;

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
     * Gets the currently logged in user.
     *
     * @return Details of the current user.
     */
    public UserDto getCurrentUser() {
        return currentAuth.getUser();
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
        currentAuth = api.signInWithEmail(email, password);
        return currentAuth;
    }

    /**
     * Logs in an existing user using their email address.
     *
     * @param credentials Object with the email and the password of the user.
     * @return Details about the authentication.
     * @throws RestClientResponseException
     */
    public AuthenticationDto signIn(CredentialsDto credentials) throws RestClientResponseException {
        currentAuth = api.signInWithEmail(credentials);
        return currentAuth;
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
        currentAuth = api.signUpWithEmail(email, password);
        return currentAuth;
    }

    /**
     * Creates a new user using their email address.
     *
     * @param credentials Object with the email and the password of the user.
     * @return Details about the authentication.
     * @throws RestClientResponseException
     */
    public AuthenticationDto signUp(CredentialsDto credentials) throws RestClientResponseException {
        currentAuth = api.signUpWithEmail(credentials);
        return currentAuth;
    }

    /**
     * Update the currently logged in user
     *
     * @param attributes The data you want to update
     * @return
     */
    public UserUpdatedDto update(UserAttributesDto attributes) throws RestClientResponseException {
        if (currentAuth == null) return null;
        return api.updateUser(currentAuth.getAccessToken(), attributes);
    }

    /**
     * Update attributes of given user.
     *
     * @param jwt        of the user you want to update.
     * @param attributes The data you want to update
     * @return
     * @throws RestClientResponseException
     */
    public UserUpdatedDto update(String jwt, UserAttributesDto attributes) throws RestClientResponseException {
        if (jwt == null) return null;
        return api.updateUser(jwt, attributes);
    }
}
