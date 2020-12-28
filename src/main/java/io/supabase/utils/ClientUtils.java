package io.supabase.utils;

import java.util.HashMap;
import java.util.Map;

public class ClientUtils {
    public static String loadUrl() {
        String url = System.getenv("GOTRUE_URL");
        if (url == null) {
            url = System.getProperty("gotrue.url");
        }
        return url;
    }

    public static Map<String, String> loadHeaders() {
        Map<String, String> res = new HashMap<>();
        String headers = System.getenv("GOTRUE_HEADERS");

        if (headers == null) {
            headers = System.getProperty("gotrue.headers");
        }
        if (headers != null) {
            String[] arr = headers.split("[\\s,;-]+");
            if (arr.length > 0) {
                String[] s;
                for (String value : arr) {
                    s = value.split("[=:]+");
                    res.put(s[0], s[1]);
                }
            }
        }
        return res;
    }
}
