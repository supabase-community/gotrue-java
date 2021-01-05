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
        this.api = new GoTrueApi(this.url, this.headers);
    }

    protected GoTrueClient(Map<String, String> headers) throws UrlNotFoundException, MalformedHeadersException {
        this.url = ClientUtils.loadUrl();
        this.headers = headers != null ? headers : ClientUtils.loadHeaders();
        this.api = new GoTrueApi(this.url, headers);
    }

    protected GoTrueClient(String url) throws UrlNotFoundException, MalformedHeadersException {
        this.url = url != null ? url : ClientUtils.loadUrl();
        this.headers = ClientUtils.loadHeaders();
        this.api = new GoTrueApi(url, this.headers);
    }

    protected GoTrueClient() throws UrlNotFoundException, MalformedHeadersException {
        this.url = ClientUtils.loadUrl();
        this.headers = ClientUtils.loadHeaders();
        this.api = new GoTrueApi(this.url, this.headers);
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
        checkParam(jwt, "jwt");
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
        checkParam(jwt, "jwt");
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
        checkAuthState();
        return currentAuth.getUser();
    }


    /**
     * Gets the current authentication details.
     *
     * @return Details of the current authentication.
     * @throws IllegalArgumentException if you are currently not logged in.
     */
    public AuthenticationDto getCurrentAuth() {
        checkAuthState();
        return currentAuth;
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
        checkParam(email, "email");
        checkParam(password, "password");
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
        checkParam(credentials, "credentials");
        checkParam(credentials.getEmail(), "credentials.email");
        checkParam(credentials.getPassword(), "credentials.password");
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
        checkParam(email, "email");
        checkParam(password, "password");
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
        checkParam(credentials, "credentials");
        checkParam(credentials.getEmail(), "credentials.email");
        checkParam(credentials.getPassword(), "credentials.password");
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
        checkAuthState();
        checkParam(attributes, "attributes");
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
        checkParam(jwt, "jwt");
        checkParam(attributes, "attributes");
        return api.updateUser(jwt, attributes);
    }

    /**
     * Signs out the current user, if there is a logged in user.
     *
     * @throws ApiException             if the underlying http request throws an error of any kind.
     * @throws IllegalArgumentException if you are currently not logged in.
     */
    public void signOut() throws ApiException {
        checkAuthState();
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
        checkParam(jwt, "jwt");
        api.signOut(jwt);
    }

    /**
     * Get the settings from the gotrue server.
     *
     * @return settings from the gotrue server.
     * @throws ApiException if the underlying http request throws an error of any kind.
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
        checkAuthState();
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
        checkParam(jwt, "jwt");
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
        checkParam(refreshToken, "refreshToken");
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
        checkParam(email, "email");
        api.recoverPassword(email);
    }

    private void checkAuthState() {
        if (currentAuth == null) {
            throw new IllegalArgumentException("You need to be logged in to use this method!");
        }
    }

    private void checkParam(Object obj, String name) {
        boolean invalid = obj == null;
        if (!invalid && obj instanceof String) {
            String s = (String) obj;
            invalid = s.isEmpty();
        }
        if (invalid) throw new IllegalArgumentException(String.format("The parameter >%s< is required!", name));
    }
}
