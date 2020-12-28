package io.supabase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.supabase.data.dto.CredentialsDto;
import io.supabase.data.dto.AuthenticationDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoTrueApi {
    private final ObjectMapper mapper = new ObjectMapper();
    protected String url;
    protected Map<String, String> headers;

    public GoTrueApi(String url, Map<String, String> headers) {
        this.url = url;
        this.headers = headers;
    }

    /**
     * Logs in an existing user using their email address.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return SignUpResponseDto
     * @throws RestClientResponseException
     */
    public AuthenticationDto signInWithEmail(String email, String password) {
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
        String urlSignup = String.format("%s/token?grant_type=password", url);
        RestTemplate rest = new RestTemplate();
        try {
            // create entity with the given credentials and headers
            HttpEntity entity = toEntity(credentials);
            AuthenticationDto responseDto = rest.postForObject(urlSignup, entity, AuthenticationDto.class);
            return responseDto;
        } catch (RestClientResponseException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("signIn failed: %s", e.getMessage()));
            if (e.getRawStatusCode() == HttpStatus.BAD_REQUEST.value()) {
                Logger.getGlobal().log(Level.INFO, String.format("signIn failed : %s", e.getMessage()));
                throw e;
            } else {
                Logger.getGlobal().log(Level.WARNING, String.format("signIn failed: %s", e.getMessage()));
            }
        } catch (JsonProcessingException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("ObjectMapping failed: %s", e.getMessage()));
        }
        return null;
    }

    /**
     * Creates a new user using their email address.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return SignUpResponseDto
     * @throws RestClientResponseException
     */
    public AuthenticationDto signUpWithEmail(String email, String password) {
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
        String urlSignup = String.format("%s/signup", url);
        RestTemplate rest = new RestTemplate();
        try {
            HttpEntity entity = toEntity(credentials);
            AuthenticationDto responseDto = rest.postForObject(urlSignup, entity, AuthenticationDto.class);
            return responseDto;
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == HttpStatus.BAD_REQUEST.value()) {
                Logger.getGlobal().log(Level.INFO, String.format("signup failed : %s", e.getMessage()));
                throw e;
            } else {
                Logger.getGlobal().log(Level.WARNING, String.format("signup failed: %s", e.getMessage()));
            }
        } catch (JsonProcessingException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("ObjectMapping failed: %s", e.getMessage()));
        }
        return null;
    }

    private HttpEntity toEntity(Object object) throws JsonProcessingException {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach((k, v) -> httpHeaders.add(k, v));
        HttpEntity entity = new HttpEntity(mapper.writeValueAsString(object), httpHeaders);
        return entity;
    }

}
