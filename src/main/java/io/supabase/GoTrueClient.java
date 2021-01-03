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
     * @throws UrlNotFoundException      if the gotrue url is not specified.
     * @throws MalformedHeadersException if the default headers are specified but in an invalid format.
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
     * @throws UrlNotFoundException      if the gotrue url is not specified.
     * @throws MalformedHeadersException if the default headers are specified but in an invalid format.
     */
    public static GoTrueClient i() throws UrlNotFoundException, MalformedHeadersException {
        return getInstance();
    }


    /**
     * Parses a jwt token.
     *
     * @param jwt token to be parsed.
     * @return the parsed token.
     * @throws JwtSecretNotFoundException if the jwt secret is not specified
     * @throws JwtException               if the given token is expired, malformed, unsupported or wrongly signed
     * @throws IllegalArgumentException   if the jwt token is not specified.
     */
    public ParsedToken parseJwt(String jwt) throws JwtSecretNotFoundException {
        if (jwt == null || jwt.isEmpty()) {
            throw new IllegalArgumentException("The JWT token is required!");
        }
        return ClientUtils.parseJwt(jwt);
    }


    /**
     * Checks whether a jwt is valid.
     *
     * @param jwt token to be validated.
     * @return whether the given token is valid.
     * @throws JwtSecretNotFoundException if the jwt secret is not specified
     * @throws IllegalArgumentException   if the jwt token is not specified.
     */
    public boolean validate(String jwt) throws JwtSecretNotFoundException {
        if (jwt == null || jwt.isEmpty()) {
            throw new IllegalArgumentException("The JWT token is required!");
        }
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
     * @throws IllegalArgumentException if you are currently not logged in.
     */
    public UserDto getCurrentUser() {
        if (currentAuth == null) {
            throw new IllegalArgumentException("You need to be logged in to use this method!");
        }
        return currentAuth.getUser();
    }

    /**
     * Logs in an existing user using either their email address and password.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return Details about the authentication.
     * @throws ApiException             if the underlying http request throws an error of any kind.
     * @throws IllegalArgumentException if the either or both email and password are not specified.
     */
    public AuthenticationDto signIn(String email, String password) throws ApiException {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Email and password are required!");
        }
        currentAuth = api.signInWithEmail(email, password);
        return currentAuth;
    }

    /**
     * Logs in an existing user using their email address.
     *
     * @param credentials Object with the email and/or the password or the provider of the user.
     * @return Details about the authentication.
     * @throws ApiException             if the underlying http request throws an error of any kind.
     * @throws IllegalArgumentException if the the credentials are not specified.
     */
    public AuthenticationDto signIn(CredentialsDto credentials) throws ApiException {
        if (credentials == null || credentials.getEmail() == null || credentials.getPassword() == null) {
            throw new IllegalArgumentException("The credentials are required!");
        }
        return signIn(credentials.getEmail(), credentials.getPassword());
    }

    /**
     * Creates a new user using their email address.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return Details about the authentication.
     * @throws ApiException             if the underlying http request throws an error of any kind.
     * @throws IllegalArgumentException if the either or both email and password are not specified.
     */
    public AuthenticationDto signUp(String email, String password) throws ApiException {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Email and password are required!");
        }
        currentAuth = api.signUpWithEmail(email, password);
        return currentAuth;
    }

    /**
     * Creates a new user using their email address.
     *
     * @param credentials Object with the email and the password of the user.
     * @return Details about the authentication.
     * @throws ApiException             if the underlying http request throws an error of any kind.
     * @throws IllegalArgumentException if the the credentials are not specified.
     */
    public AuthenticationDto signUp(CredentialsDto credentials) throws ApiException {
        if (credentials == null || credentials.getEmail() == null || credentials.getPassword() == null) {
            throw new IllegalArgumentException("The credentials are required!");
        }
        currentAuth = api.signUpWithEmail(credentials);
        return currentAuth;
    }

    /**
     * Update the currently logged in user
     *
     * @param attributes The data you want to update
     * @return details of the updated user.
     * @throws ApiException             if the underlying http request throws an error of any kind.
     * @throws IllegalArgumentException if you are currently not logged in.
     * @throws IllegalArgumentException if the attributes are not specified.
     */
    public UserUpdatedDto update(UserAttributesDto attributes) throws ApiException {
        if (currentAuth == null) {
            throw new IllegalArgumentException("You need to be logged in to use this method!");
        }
        if (attributes == null) {
            throw new IllegalArgumentException("The attributes are required!");
        }
        return api.updateUser(currentAuth.getAccessToken(), attributes);
    }

    /**
     * Update attributes of given user.
     *
     * @param jwt        of the user you want to update.
     * @param attributes The data you want to update
     * @return details of the updated user.
     * @throws ApiException             if the underlying http request throws an error of any kind.
     * @throws IllegalArgumentException if the jwt token is not specified.
     * @throws IllegalArgumentException if the attributes are not specified.
     */
    public UserUpdatedDto update(String jwt, UserAttributesDto attributes) throws ApiException {
        if (jwt == null || jwt.isEmpty()) {
            throw new IllegalArgumentException("The JWT token is required!");
        }
        if (attributes == null) {
            throw new IllegalArgumentException("The attributes are required!");
        }
        return api.updateUser(jwt, attributes);
    }

    /**
     * Signs out the current user, if there is a logged in user.
     *
     * @throws ApiException             if the underlying http request throws an error of any kind.
     * @throws IllegalArgumentException if you are currently not logged in.
     */
    public void signOut() throws ApiException {
        if (currentAuth == null) {
            throw new IllegalArgumentException("You need to be logged in order to log out!");
        }
        api.signOut(currentAuth.getAccessToken());
    }

    /**
     * Signs out the user of the given jwt.
     *
     * @param jwt A valid jwt.
     * @throws ApiException             if the underlying http request throws an error of any kind.
     * @throws IllegalArgumentException if the jwt token is not specified.
     */
    public void signOut(String jwt) throws ApiException {
        if (jwt == null || jwt.isEmpty()) {
            throw new IllegalArgumentException("The JWT token is required!");
        }
        api.signOut(jwt);
    }

    /**
     * Get the settings from the gotrue server.
     *
     * @return settings from the gotrue server.
     * @throws ApiException             if the underlying http request throws an error of any kind.
     */
    public SettingsDto settings() throws ApiException {
        return api.getSettings();
    }

    /**
     * Generates a new JWT, for current user.
     *
     * @return The updated information with the refreshed token
     * @throws ApiException             if the underlying http request throws an error of any kind.
     * @throws IllegalArgumentException if you are currently not logged in.
     */
    public AuthenticationDto refresh() throws ApiException {
        if (currentAuth == null) {
            throw new IllegalArgumentException("You need to be logged in to use this method!");
        }
        return api.refreshAccessToken(currentAuth.getRefreshToken());
    }

    /**
     * Gets details about the user.
     *
     * @param jwt A valid, logged-in JWT.
     * @return UserDto details about the user.
     * @throws ApiException             if the underlying http request throws an error of any kind.
     * @throws IllegalArgumentException if the jwt token is not specified.
     */
    public UserDto getUser(String jwt) throws ApiException {
        if (jwt == null || jwt.isEmpty()) {
            throw new IllegalArgumentException("The JWT token is required!");
        }
        return api.getUser(jwt);
    }


    /**
     * Generates a new JWT.
     *
     * @param refreshToken A valid refresh token that was returned on login.
     * @return The updated information with the refreshed token
     * @throws ApiException             if the underlying http request throws an error of any kind.
     * @throws IllegalArgumentException if the refresh token is not specified.
     */
    public AuthenticationDto refresh(String refreshToken) throws ApiException {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("The refresh token is required!");
        }
        return api.refreshAccessToken(refreshToken);
    }

    /**
     * Send a password-recovery link to a given email.
     *
     * @param email the email a recovery link should be sent to.
     * @throws ApiException             if the underlying http request throws an error of any kind.
     * @throws IllegalArgumentException if the email is not specified.
     */
    public void recover(String email) throws ApiException {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("The email is required!");
        }
        api.recoverPassword(email);
    }
}
