package io.supabase.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.supabase.exceptions.ApiException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RestUtils {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final RestTemplate rest = new RestTemplate();


    private RestUtils() {
    }


    /**
     * Sends a Put request.
     *
     * @param body          the body of the request, will be parsed to json.
     * @param responseClass the class of the response.
     * @param headers       the headers that will be sent with the request.
     * @param url           the url the request will be sent to.
     * @param <R>           the type of the response.
     * @return the response of the request parsed from json to R.
     * @throws ApiException
     */
    public static <R> R put(Object body, Class<R> responseClass, Map<String, String> headers, String url) throws ApiException {
        try {
            HttpEntity<String> entity = toEntity(body, headers);
            return rest.exchange(url, HttpMethod.PUT, entity, responseClass).getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            throw new ApiException("Put failed", e);
        } catch (JsonProcessingException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("ObjectMapping failed: %s", e.getMessage()));
            return null;
        }
    }

    /**
     * Sends a Get request.
     *
     * @param responseClass the class of the response.
     * @param headers       the headers that will be sent with the request.
     * @param url           the url the request will be sent to.
     * @param <R>           the type of the response.
     * @return the response of the request parsed from json to R.
     * @throws ApiException
     */
    public static <R> R get(Class<R> responseClass, Map<String, String> headers, String url) throws ApiException {
        try {
            HttpEntity<String> entity = toEntity(headers);
            ResponseEntity<R> res = rest.exchange(url, HttpMethod.GET, entity, responseClass);
            return res.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            throw new ApiException("Get failed", e);
        }
    }


    /**
     * Sends a Post request.
     *
     * @param headers the headers that will be sent with the request.
     * @param url     the url the request will be sent to.
     * @throws ApiException
     */
    public static void post(Map<String, String> headers, String url) throws ApiException {
        try {
            HttpEntity<String> entity = toEntity(headers);
            rest.postForObject(url, entity, Void.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            throw new ApiException("Post failed", e);
        }
    }

    /**
     * Sends a Post request.
     *
     * @param body    the body of the request, will be parsed to json.
     * @param headers the headers that will be sent with the request.
     * @param url     the url the request will be sent to.
     * @throws ApiException
     */
    public static void post(Object body, Map<String, String> headers, String url) throws ApiException {
        post(body, Void.class, headers, url);
    }

    /**
     * Sends a Post request.
     *
     * @param body          the body of the request, will be parsed to json.
     * @param responseClass the class of the response.
     * @param headers       the headers that will be sent with the request.
     * @param url           the url the request will be sent to.
     * @param <R>           the type of the response.
     * @return the response of the request parsed from json to R.
     * @throws ApiException
     */
    public static <R> R post(Object body, Class<R> responseClass, Map<String, String> headers, String url) throws ApiException {
        try {
            HttpEntity<String> entity = toEntity(body, headers);
            return rest.postForObject(url, entity, responseClass);
        } catch (RestClientResponseException | ResourceAccessException e) {
            throw new ApiException("Post failed", e);
        } catch (JsonProcessingException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("ObjectMapping failed: %s", e.getMessage()));
            return null;
        }
    }

    private static HttpEntity<String> toEntity(Object object, Map<String, String> headers) throws JsonProcessingException {
        return toEntity(mapper.writeValueAsString(object), headers);
    }

    private static HttpEntity<String> toEntity(Map<String, String> headers) {
        return toEntity(null, headers);
    }

    private static HttpEntity<String> toEntity(String jsonBody, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers = (headers != null) ? headers : new HashMap<>();
        headers.forEach(httpHeaders::add);
        return new HttpEntity<>(jsonBody, httpHeaders);
    }
}
