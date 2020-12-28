package io.supabase;

import io.supabase.utils.ClientUtils;

import java.util.Map;

public class GoTrueClient {
    private final GoTrueApi api;
    private final String url;
    private final Map<String, String> headers;

    public GoTrueClient(String url, Map<String, String> headers) {
        this.url = url != null ? url : ClientUtils.loadUrl();
        this.headers = headers != null ? headers : ClientUtils.loadHeaders();
        api = new GoTrueApi(url, headers);
    }

    public GoTrueClient() {
        url = ClientUtils.loadUrl();
        headers = ClientUtils.loadHeaders();
        api = new GoTrueApi(url, headers);
    }


}
