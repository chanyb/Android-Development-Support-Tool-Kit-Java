package chanyb.android.java;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

    public static int GET = Request.Method.GET;
    public static int POST = Request.Method.POST;
    public static int PUT = Request.Method.PUT;
    public static int DELETE = Request.Method.DELETE;

    private Map<String, String> headers;
    private Listener listener;

    public void setHeaders(Map<String, String> _headers) {
        headers = _headers;
    }


    /**
     * @param method GET | POST | PUT | DELETE
     * @param url Destination URL
     * @param object params
     * @param listener Success and Error Listener interface implementation
     */
    public void addRequest(int method, String url, JSONObject object, Listener listener) {
        this.listener = listener;

        JsonObjectRequest newRequest = new JsonObjectRequest(method, url, object, successListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }
        };
        requestQueue.add(newRequest);
    }

    public interface Listener {
        void onSuccess();
        void onError();
    }

    Response.Listener successListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            if(listener != null) {
                listener.onSuccess();
                listener = null;
            }
        }
    };

    Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if(listener != null) {
                listener.onError();
                listener = null;
            }
        }
    };


}
