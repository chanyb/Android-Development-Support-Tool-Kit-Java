package chanyb.android.java;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class RequestManager {
    private static final String TAG = "this";
    private static RequestManager instance = new RequestManager();
    private RequestManager() {
        headers = new HashMap<>();
        requestQueue = Volley.newRequestQueue(GlobalApplcation.getContext());
    }

    private static RequestQueue requestQueue;
    public static RequestManager getInstance() {
        if(instance == null) instance = new RequestManager();
        return instance;
    }

    private Map<String, String> headers;

    public void setHeaders(Map<String, String> _headers) {
        headers = _headers;
    }

    /**
     * @param method Request.Method - Get, Post, Put, Delete.
     * @param url Destination URL
     * @param object params
     * @param listener Response.Listener<T>
     * @param errorListener Response.ErrorListener
     * */
    public void addRequest(int method, String url, JSONObject object, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JsonObjectRequest newRequest = new JsonObjectRequest(method, url, object, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }
        };
        requestQueue.add(newRequest);
    }

}
