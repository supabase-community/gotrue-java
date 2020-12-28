package io.supabase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RestUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void post(Map<String, String> headers, String url) {
        RestTemplate rest = new RestTemplate();
        try {
            HttpEntity entity = toEntity(headers);
            rest.postForObject(url, entity, Void.class);
        } catch (RestClientResponseException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Post failed: %s", e.getMessage()));
            throw e;
        }
    }

    public static <R> R post(Object body, Class<R> responseClass, Map<String, String> headers, String url) {
        RestTemplate rest = new RestTemplate();
        try {
            HttpEntity entity = toEntity(body, headers);
            R res = rest.postForObject(url, entity, responseClass);
            return res;
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == HttpStatus.BAD_REQUEST.value()) {
                Logger.getGlobal().log(Level.INFO, String.format("Post failed : %s", e.getMessage()));
                throw e;
            } else {
                Logger.getGlobal().log(Level.WARNING, String.format("Post failed: %s", e.getMessage()));
            }
        } catch (JsonProcessingException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("ObjectMapping failed: %s", e.getMessage()));
        }
        return null;
    }

    private static HttpEntity toEntity(Object object, Map<String, String> headers) throws JsonProcessingException {
        return toEntity(mapper.writeValueAsString(object), headers);
    }

    private static HttpEntity toEntity(Map<String, String> headers) {
        return toEntity(null, headers);
    }

    private static HttpEntity toEntity(String jsonBody, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach((k, v) -> httpHeaders.add(k, v));
        HttpEntity entity = new HttpEntity(jsonBody, httpHeaders);
        return entity;
    }
}
