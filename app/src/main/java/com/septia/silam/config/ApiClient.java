package com.septia.silam.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApiClient {

    private static ApiClient instance;
    private final RequestQueue queue;
    private final Context context;

    private ApiClient(Context ctx) {
        context = ctx.getApplicationContext();
        queue = Volley.newRequestQueue(context);
    }

    public static synchronized ApiClient get(Context ctx) {
        if (instance == null) instance = new ApiClient(ctx);
        return instance;
    }

    private String token() {
        SharedPreferences pref = context.getSharedPreferences("SILAM_SESSION", Context.MODE_PRIVATE);
        return pref.getString("auth_token", "");
    }

    public void request(int method, String url, JSONObject body,
                        Response.Listener<JSONObject> ok,
                        Response.ErrorListener err) {

        JsonObjectRequest req = new JsonObjectRequest(method, url, body, ok, err) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> h = new HashMap<>();
                h.put("Accept", "application/json");
                h.put("Content-Type", "application/json");
                String t = token();
                if (!t.isEmpty()) h.put("Authorization", "Bearer " + t);
                return h;
            }
        };
        // timeout 10 detik, tanpa retry (supaya tidak terkirim dobel)
        req.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        queue.add(req);
    }

    // Ambil pesan error asli dari server
    public static String errorMessage(com.android.volley.VolleyError error) {
        if (error.networkResponse == null) return "Koneksi terputus / server tidak terjangkau";
        int code = error.networkResponse.statusCode;
        if (code == 429) return "Terlalu banyak percobaan. Tunggu sebentar!";
        try {
            String body = new String(error.networkResponse.data, "UTF-8");
            return new JSONObject(body).optString("message", "HTTP " + code);
        } catch (Exception e) {
            return "HTTP " + code;
        }
    }
}