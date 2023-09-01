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

public class RestUtils {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final RestTemplate rest = new RestTemplate();

    private RestUtils() {
    }

    /**
     * Sends an HTTP request.
     *
     * @param method        the HTTP method (GET, POST, PUT, DELETE, etc.).
     * @param body          the body of the request, will be parsed to JSON.
     * @param responseClass the class of the response.
     * @param headers       the headers that will be sent with the request.
     * @param url           the URL the request will be sent to.
     * @param <R>           the type of the response.
     * @return the response of the request parsed from JSON to R.
     * @throws ApiException if an exception is thrown.
     */
    public static <R> R sendRequest(HttpMethod method, Object body, Class<R> responseClass,
                                    Map<String, String> headers, String url) throws ApiException {
        try {
            HttpEntity<String> entity = toEntity(body, headers);
            ResponseEntity<R> res = rest.exchange(url, method, entity, responseClass);
            return res.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            throw new ApiException(method + " request failed", e);
        } catch (JsonProcessingException e) {
            throw new ApiException("Object mapping failed", e);
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
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpHeaders.add(entry.getKey(), entry.getValue());
        }
        return new HttpEntity<>(jsonBody, httpHeaders);
    }
}
