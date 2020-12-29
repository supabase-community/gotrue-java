package io.supabase.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.supabase.exceptions.ApiException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RestUtils {
    private static final ObjectMapper mapper = new ObjectMapper();


    private RestUtils() {
    }

    public static <R> R put(Object body, Class<R> responseClass, Map<String, String> headers, String url) throws ApiException {
        RestTemplate rest = new RestTemplate();
        try {
            HttpEntity<String> entity = toEntity(body, headers);

            return rest.exchange(url, HttpMethod.PUT, entity, responseClass).getBody();
        } catch (RestClientResponseException e) {
            throw new ApiException("Put failed", e);
        } catch (JsonProcessingException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("ObjectMapping failed: %s", e.getMessage()));
        }
        return null;
    }

    public static <R> R get(Class<R> responseClass, Map<String, String> headers, String url) throws ApiException {
        RestTemplate rest = new RestTemplate();
        try {
            HttpEntity<String> entity = toEntity(headers);
            ResponseEntity<R> res = rest.exchange(url, HttpMethod.GET, entity, responseClass);
            return res.getBody();
        } catch (RestClientResponseException e) {
            throw new ApiException("Get failed", e);
        }
    }

    public static void post(Map<String, String> headers, String url) throws ApiException {
        RestTemplate rest = new RestTemplate();
        try {
            HttpEntity<String> entity = toEntity(headers);
            rest.postForObject(url, entity, Void.class);
        } catch (RestClientResponseException e) {
            throw new ApiException("Post failed", e);
        }
    }

    public static <R> R post(Object body, Class<R> responseClass, Map<String, String> headers, String url) throws ApiException {
        RestTemplate rest = new RestTemplate();
        try {
            HttpEntity<String> entity = toEntity(body, headers);
            return rest.postForObject(url, entity, responseClass);
        } catch (RestClientResponseException e) {
            throw new ApiException("Post failed", e);
        } catch (JsonProcessingException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("ObjectMapping failed: %s", e.getMessage()));
        }
        return null;
    }

    private static HttpEntity<String> toEntity(Object object, Map<String, String> headers) throws JsonProcessingException {
        return toEntity(mapper.writeValueAsString(object), headers);
    }

    private static HttpEntity<String> toEntity(Map<String, String> headers) {
        return toEntity(null, headers);
    }

    private static HttpEntity<String> toEntity(String jsonBody, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::add);
        return new HttpEntity<>(jsonBody, httpHeaders);
    }
}
