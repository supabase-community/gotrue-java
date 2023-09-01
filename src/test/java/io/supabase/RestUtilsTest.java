package io.supabase;

import io.supabase.data.CircularDependentA;
import io.supabase.data.CircularDependentB;
import io.supabase.exceptions.ApiException;
import io.supabase.utils.RestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class RestUtilsTest {

    private final String BASE_URL = "http://localhost:3000";
    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void sendRequest_get_headers() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        Assertions.assertThrows(ApiException.class, () ->
                RestUtils.sendRequest(HttpMethod.GET, null, Object.class, headers, BASE_URL + "/test")
        );
    }

    @Test
    void sendRequest_post() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        CircularDependentA a = new CircularDependentA();
        CircularDependentB b = new CircularDependentB();
        a.setB(b);
        b.setA(a);

        Assertions.assertThrows(ApiException.class, () ->
                RestUtils.sendRequest(HttpMethod.POST, a, CircularDependentA.class, headers, BASE_URL + "/test")
        );
    }

    @Test
    void sendRequest_post_headers() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        CircularDependentA a = new CircularDependentA();
        CircularDependentB b = new CircularDependentB();
        a.setB(b);
        b.setA(a);

        Assertions.assertThrows(ApiException.class, () ->
                RestUtils.sendRequest(HttpMethod.POST, a, CircularDependentA.class, headers, BASE_URL + "/test")
        );
    }

    @Test
    void sendRequest_put_json() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        CircularDependentA a = new CircularDependentA();
        CircularDependentB b = new CircularDependentB();
        a.setB(b);
        b.setA(a);

        Assertions.assertThrows(ApiException.class, () ->
                RestUtils.sendRequest(HttpMethod.PUT, a, CircularDependentA.class, headers, BASE_URL + "/test")
        );
    }

    @Test
    void sendRequest_put() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        Assertions.assertThrows(ApiException.class, () ->
                RestUtils.sendRequest(HttpMethod.PUT, null, Object.class, headers, BASE_URL + "/test")
        );
    }

    @Test
    void toEntity_nulls() {
        try {
            Method m = RestUtils.class.getDeclaredMethod("toEntity", String.class, Map.class);
            m.setAccessible(true);
            Assertions.assertDoesNotThrow(() -> m.invoke(null, null, null));
        } catch (NoSuchMethodException e) {
            Assertions.fail();
        }
    }

    @Test
    void toEntity() {
        try {
            Method m = RestUtils.class.getDeclaredMethod("toEntity", String.class, Map.class);
            m.setAccessible(true);
            Assertions.assertDoesNotThrow(() -> m.invoke(null, "{\"a\":1}", new HashMap<>()));
        } catch (NoSuchMethodException e) {
            Assertions.fail();
        }
    }
}
