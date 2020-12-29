package io.supabase;

import io.supabase.data.CircularDependentA;
import io.supabase.data.CircularDependentB;
import io.supabase.exceptions.ApiException;
import io.supabase.utils.RestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class RestUtilsTest {

    @AfterEach
    void tearDown() {
        // to ensure that the tests dont affect each other
        RestTemplate rest = new RestTemplate();
        rest.delete("http://localhost:3000/users");
    }

    @Test
    void post() {
        // to raise a JsonProcessingException
        CircularDependentA a = new CircularDependentA();
        CircularDependentB b = new CircularDependentB();
        a.setB(b);
        b.setA(a);
        Assertions.assertDoesNotThrow(() -> RestUtils.post(a, CircularDependentA.class, null, "http://smth/"));
    }


    @Test
    void put_json() {
        // to raise a JsonProcessingException
        CircularDependentA a = new CircularDependentA();
        CircularDependentB b = new CircularDependentB();
        a.setB(b);
        b.setA(a);
        Assertions.assertDoesNotThrow(() -> RestUtils.put(a, CircularDependentA.class, null, "http://smth/"));
    }

    @Test
    void put() {
        // some url that does not exist
        Assertions.assertThrows(ApiException.class, () -> RestUtils.put(null, Object.class, null, "http://localhost:1/"));
    }
}
