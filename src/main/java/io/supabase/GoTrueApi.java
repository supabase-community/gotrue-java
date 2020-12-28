package io.supabase;

import io.supabase.data.dto.*;
import io.supabase.utils.RestUtils;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

public class GoTrueApi {
    protected String url;
    protected Map<String, String> headers;

    public GoTrueApi(String url, Map<String, String> headers) {
        if (url == null || url.isEmpty()) {
            throw new RuntimeException("The GoTrue url is not defined");
        }
        this.url = url;
        this.headers = headers;
    }


    /**
     * Generates the relevant login URL for a third-party provider.
     *
     * @param provider One of the providers supported by GoTrue.
     * @return
     */
    public String getUrlForProvider(String provider) {
        return String.format("%s/authorize?provider=%s", url, provider);
    }

    /**
     * @param jwt           A valid JWT.
     * @param attributesDto The data you want to update
     * @return
     */
    public UserUpdatedDto updateUser(String jwt, UserAttributesDto attributesDto) {
        String _url = String.format("%s/user", url);

        return RestUtils.put(attributesDto, UserUpdatedDto.class, headersWithJWT(jwt), _url);
    }

    /**
     * Generates a new JWT
     *
     * @param refreshToken A valid refresh token that was returned on login.
     * @return The updated information with the refreshed token
     */
    public AuthenticationDto refreshAccessToken(String refreshToken) {
        String _url = String.format("%s/token?grant_type=refresh_token", url);
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
        refreshTokenDto.setRefreshToken(refreshToken);

        return RestUtils.post(refreshTokenDto, AuthenticationDto.class, headers, _url);
    }

    /**
     * Gets details about the user.
     *
     * @param jwt A valid, logged-in JWT.
     * @return UserDto details about the user.
     */
    public UserDto getUser(String jwt) {
        String _url = String.format("%s/user", url);

        return RestUtils.get(UserDto.class, headersWithJWT(jwt), _url);
    }

    /**
     * Removes a logged-in session.
     *
     * @param jwt A valid, logged-in JWT.
     * @throws RestClientResponseException
     */
    public void signOut(String jwt) throws RestClientResponseException {
        String _url = String.format("%s/logout", url);
        RestUtils.post(headersWithJWT(jwt), _url);
    }

    /**
     * Logs in an existing user using their email address.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return SignUpResponseDto
     * @throws RestClientResponseException
     */
    public AuthenticationDto signInWithEmail(String email, String password) throws RestClientResponseException {
        CredentialsDto credentials = new CredentialsDto();
        credentials.setEmail(email);
        credentials.setPassword(password);
        return signInWithEmail(credentials);
    }

    /**
     * Logs in an existing user using their email address.
     *
     * @param credentials Object with the email and the password of the user.
     * @return TokenResponseDto
     * @throws RestClientResponseException
     */
    public AuthenticationDto signInWithEmail(CredentialsDto credentials) throws RestClientResponseException {
        String _url = String.format("%s/token?grant_type=password", url);

        return RestUtils.post(credentials, AuthenticationDto.class, headers, _url);
    }

    /**
     * Creates a new user using their email address.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return SignUpResponseDto
     * @throws RestClientResponseException
     */
    public AuthenticationDto signUpWithEmail(String email, String password) throws RestClientResponseException {
        CredentialsDto credentials = new CredentialsDto();
        credentials.setEmail(email);
        credentials.setPassword(password);
        return signUpWithEmail(credentials);
    }

    /**
     * Creates a new user using their email address.
     *
     * @param credentials Object with the email and the password of the user.
     * @return SignUpResponseDto
     * @throws RestClientResponseException
     */
    public AuthenticationDto signUpWithEmail(CredentialsDto credentials) throws RestClientResponseException {
        String _url = String.format("%s/signup", url);

        return RestUtils.post(credentials, AuthenticationDto.class, headers, _url);
    }

    private Map<String, String> headersWithJWT(String jwt) {
        Map<String, String> _headers = new HashMap<>(headers);
        _headers.put("Authorization", String.format("Bearer %s", jwt));
        return _headers;
    }
}
