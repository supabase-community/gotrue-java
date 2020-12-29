package io.supabase;

import io.jsonwebtoken.JwtException;
import io.supabase.data.dto.*;
import io.supabase.data.jwt.ParsedToken;
import io.supabase.exceptions.ApiException;
import io.supabase.exceptions.JwtSecretNotFoundException;
import io.supabase.exceptions.MalformedHeadersException;
import io.supabase.exceptions.UrlNotFoundException;
import io.supabase.utils.ClientUtils;

import java.util.Map;

public class GoTrueClient {
    private static GoTrueClient client;
    private final GoTrueApi api;
    private final String url;
    private final Map<String, String> headers;
    private AuthenticationDto currentAuth;

    protected GoTrueClient(String url, Map<String, String> headers) throws UrlNotFoundException, MalformedHeadersException {
        this.url = url != null ? url : ClientUtils.loadUrl();
        this.headers = headers != null ? headers : ClientUtils.loadHeaders();
        api = new GoTrueApi(url, headers);
    }

    protected GoTrueClient(Map<String, String> headers) throws UrlNotFoundException, MalformedHeadersException {
        this.url = ClientUtils.loadUrl();
        this.headers = headers != null ? headers : ClientUtils.loadHeaders();
        api = new GoTrueApi(url, headers);
    }

    protected GoTrueClient(String url) throws UrlNotFoundException, MalformedHeadersException {
        this.url = url != null ? url : ClientUtils.loadUrl();
        this.headers = ClientUtils.loadHeaders();
        api = new GoTrueApi(url, headers);
    }

    protected GoTrueClient() throws UrlNotFoundException, MalformedHeadersException {
        url = ClientUtils.loadUrl();
        headers = ClientUtils.loadHeaders();
        api = new GoTrueApi(url, headers);
    }


    /**
     * Get a GoTrueClient singleton.
     *
     * @return singleton of GoTrueClient.
     */
    public static GoTrueClient getInstance() throws UrlNotFoundException, MalformedHeadersException {
        if (client == null) {
            client = new GoTrueClient();
        }
        return client;
    }

    /**
     * Get a GoTrueClient singleton.
     * Shorthand for getInstance.
     *
     * @return singleton of GoTrueClient.
     */
    public static GoTrueClient I() throws UrlNotFoundException, MalformedHeadersException {
        return getInstance();
    }


    /**
     * Parses a jwt token.
     *
     * @param jwt token to be parsed.
     * @return the parsed token.
     * @throws JwtException
     */
    public ParsedToken parseJwt(String jwt) throws JwtSecretNotFoundException {
        return ClientUtils.parseJwt(jwt);
    }


    /**
     * Checks whether a jwt is valid.
     *
     * @param jwt token to be validated.
     * @return
     */
    public boolean validate(String jwt) throws JwtSecretNotFoundException {
        try {
            ClientUtils.parseJwt(jwt);
            // no error -> valid
            return true;
        } catch (JwtException e) {
            return false;
        }
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
     */
    public AuthenticationDto signIn(String email, String password) throws ApiException {
        currentAuth = api.signInWithEmail(email, password);
        return currentAuth;
    }

    /**
     * Logs in an existing user using their email address.
     *
     * @param credentials Object with the email and the password of the user.
     * @return Details about the authentication.
     */
    public AuthenticationDto signIn(CredentialsDto credentials) throws ApiException {
        currentAuth = api.signInWithEmail(credentials);
        return currentAuth;
    }

    /**
     * Creates a new user using their email address.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return Details about the authentication.
     */
    public AuthenticationDto signUp(String email, String password) throws ApiException {
        currentAuth = api.signUpWithEmail(email, password);
        return currentAuth;
    }

    /**
     * Creates a new user using their email address.
     *
     * @param credentials Object with the email and the password of the user.
     * @return Details about the authentication.
     */
    public AuthenticationDto signUp(CredentialsDto credentials) throws ApiException {
        currentAuth = api.signUpWithEmail(credentials);
        return currentAuth;
    }

    /**
     * Update the currently logged in user
     *
     * @param attributes The data you want to update
     * @return details of the updated user.
     */
    public UserUpdatedDto update(UserAttributesDto attributes) throws ApiException {
        if (currentAuth == null) return null;
        return api.updateUser(currentAuth.getAccessToken(), attributes);
    }

    /**
     * Update attributes of given user.
     *
     * @param jwt        of the user you want to update.
     * @param attributes The data you want to update
     * @return details of the updated user.
     */
    public UserUpdatedDto update(String jwt, UserAttributesDto attributes) throws ApiException {
        if (jwt == null) return null;
        return api.updateUser(jwt, attributes);
    }

    /**
     * Signs out the current user, if there is a logged in user.
     */
    public void signOut() throws ApiException {
        if (currentAuth == null) return;
        api.signOut(currentAuth.getAccessToken());
    }

    /**
     * Signs out the user of the given jwt.
     *
     * @param jwt A valid jwt.
     */
    public void signOut(String jwt) throws ApiException {
        if (jwt == null) return;
        api.signOut(currentAuth.getAccessToken());
    }

    /**
     * Get the settings from the gotrue server.
     *
     * @return settings from the gotrue server.
     */
    public SettingsDto settings() throws ApiException {
        return api.getSettings();
    }

    /**
     * Generates a new JWT, for current user.
     *
     * @return The updated information with the refreshed token
     */
    public AuthenticationDto refresh() throws ApiException {
        return api.refreshAccessToken(currentAuth.getRefreshToken());
    }

    /**
     * Gets details about the user.
     *
     * @param jwt A valid, logged-in JWT.
     * @return UserDto details about the user.
     */
    public UserDto getUser(String jwt) throws ApiException {
        return api.getUser(jwt);
    }


    /**
     * Generates a new JWT.
     *
     * @param refreshToken A valid refresh token that was returned on login.
     * @return The updated information with the refreshed token
     */
    public AuthenticationDto refresh(String refreshToken) throws ApiException {
        return api.refreshAccessToken(refreshToken);
    }
}
