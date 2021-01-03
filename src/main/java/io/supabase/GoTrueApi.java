package io.supabase;

import io.supabase.data.dto.*;
import io.supabase.exceptions.ApiException;
import io.supabase.exceptions.UrlNotFoundException;
import io.supabase.utils.RestUtils;

import java.util.HashMap;
import java.util.Map;

public class GoTrueApi {
    protected String url;
    protected Map<String, String> headers;

    protected GoTrueApi(String url, Map<String, String> headers) throws UrlNotFoundException {
        if (url == null || url.isEmpty()) {
            throw new UrlNotFoundException();
        }
        this.url = url;
        this.headers = headers;
    }

    /**
     * Send an magic-link to a given email.
     *
     * @param email the email the link should be sent to.
     * @throws ApiException
     */
    public void magicLink(String email) throws ApiException {
        String urlMagicLink = String.format("%s/magiclink", url);

        EmailDto emailDto = new EmailDto();
        emailDto.setEmail(email);

        RestUtils.post(emailDto, headers, urlMagicLink);
    }

    /**
     * Send a password-recovery link to a given email.
     *
     * @param email the email a recovery link should be sent to.
     * @throws ApiException
     */
    public void recoverPassword(String email) throws ApiException {
        String urlRecover = String.format("%s/recover", url);

        EmailDto emailDto = new EmailDto();
        emailDto.setEmail(email);

        RestUtils.post(emailDto, headers, urlRecover);
    }

    /**
     * Get the settings from the gotrue server.
     *
     * @return settings from the gotrue server.
     * @throws ApiException
     */
    public SettingsDto getSettings() throws ApiException {
        String urlSettings = String.format("%s/settings", url);

        return RestUtils.get(SettingsDto.class, headers, urlSettings);
    }

    /**
     * Generates the relevant login URL for a third-party provider.
     *
     * @param provider One of the providers supported by GoTrue.
     * @return the url for the given provider
     */
    public String getUrlForProvider(String provider) {
        return String.format("%s/authorize?provider=%s", url, provider);
    }

    /**
     * Update a user.
     *
     * @param jwt           A valid JWT.
     * @param attributesDto The data you want to update
     * @return details of the updated user.
     * @throws ApiException
     */
    public UserUpdatedDto updateUser(String jwt, UserAttributesDto attributesDto) throws ApiException {
        String urlUser = String.format("%s/user", url);

        return RestUtils.put(attributesDto, UserUpdatedDto.class, headersWithJWT(jwt), urlUser);
    }

    /**
     * Generates a new JWT
     *
     * @param refreshToken A valid refresh token that was returned on login.
     * @return The updated information with the refreshed token
     * @throws ApiException
     */
    public AuthenticationDto refreshAccessToken(String refreshToken) throws ApiException {
        String urlToken = String.format("%s/token?grant_type=refresh_token", url);
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
        refreshTokenDto.setRefreshToken(refreshToken);

        return RestUtils.post(refreshTokenDto, AuthenticationDto.class, headers, urlToken);
    }

    /**
     * Gets details about the user.
     *
     * @param jwt A valid, logged-in JWT.
     * @return UserDto details about the user.
     * @throws ApiException
     */
    public UserDto getUser(String jwt) throws ApiException {
        String urlUser = String.format("%s/user", url);

        return RestUtils.get(UserDto.class, headersWithJWT(jwt), urlUser);
    }

    /**
     * Removes a logged-in session.
     *
     * @param jwt A valid, logged-in JWT.
     * @throws ApiException
     */
    public void signOut(String jwt) throws ApiException {
        String urlLogout = String.format("%s/logout", url);
        RestUtils.post(headersWithJWT(jwt), urlLogout);
    }

    /**
     * Logs in an existing user using their email address.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return Details about the authentication.
     * @throws ApiException
     */
    public AuthenticationDto signInWithEmail(String email, String password) throws ApiException {
        CredentialsDto credentials = new CredentialsDto();
        credentials.setEmail(email);
        credentials.setPassword(password);
        return signInWithEmail(credentials);
    }

    /**
     * Logs in an existing user using their email address.
     *
     * @param credentials Object with the email and the password of the user.
     * @return Details about the authentication.
     * @throws ApiException
     */
    public AuthenticationDto signInWithEmail(CredentialsDto credentials) throws ApiException {
        String urlToken = String.format("%s/token?grant_type=password", url);

        return RestUtils.post(credentials, AuthenticationDto.class, headers, urlToken);
    }

    /**
     * Creates a new user using their email address.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return Details about the authentication.
     * @throws ApiException
     */
    public AuthenticationDto signUpWithEmail(String email, String password) throws ApiException {
        CredentialsDto credentials = new CredentialsDto();
        credentials.setEmail(email);
        credentials.setPassword(password);
        return signUpWithEmail(credentials);
    }

    /**
     * Creates a new user using their email address.
     *
     * @param credentials Object with the email and the password of the user.
     * @return Details about the authentication.
     * @throws ApiException
     */
    public AuthenticationDto signUpWithEmail(CredentialsDto credentials) throws ApiException {
        String urlSignup = String.format("%s/signup", url);

        return RestUtils.post(credentials, AuthenticationDto.class, headers, urlSignup);
    }

    /**
     * Get the default headers plus the Authorization header.
     *
     * @param jwt the token to be added to the headers.
     * @return the default headers plus the Authorization header.
     */
    private Map<String, String> headersWithJWT(String jwt) {
        Map<String, String> newHeaders = new HashMap<>(headers);
        newHeaders.put("Authorization", String.format("Bearer %s", jwt));
        return newHeaders;
    }
}
