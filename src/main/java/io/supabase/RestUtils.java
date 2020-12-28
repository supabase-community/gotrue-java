package io.supabase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RestUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <R> R get(Class<R> responseClass, Map<String, String> headers, String url) {
        RestTemplate rest = new RestTemplate();
        try {
            HttpEntity entity = toEntity(headers);
            ResponseEntity<R> res = rest.exchange(url, HttpMethod.GET, entity, responseClass);
            return res.getBody();
        } catch (RestClientResponseException e) {
            Logger.getGlobal().log(Level.INFO, String.format("Get failed : %s", e.getMessage()));
            throw e;
        }
    }

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
            return rest.postForObject(url, entity, responseClass);
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
        headers.forEach(httpHeaders::add);
        return new HttpEntity(jsonBody, httpHeaders);
    }
}
