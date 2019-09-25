package skywatch24.com.tw.androidtest;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DataSource {

    private static DataSource instance;

    private RequestQueue request_queue;
    private Context context;

    private MutableLiveData<Boolean> status_data;

    private DataSource(Context context) {
        this.context = context;
        request_queue = Volley.newRequestQueue(context);

        status_data = new MutableLiveData<>();
    }

    public static DataSource getInstance(Context context) {
        synchronized (DataSource.class) {
            if (instance == null) {
                synchronized (DataSource.class) {
                    if (instance == null) {
                        instance = new DataSource(context);
                    }
                }
            }
        }

        return instance;
    }

    public LiveData<Boolean> getStatusValue() {
        return status_data;
    }

    public void loadStatus() {
        String url = String.format("https://service.skywatch24.com/api/v2/devices/%s/locknotification?api_key=%s", "49209", "b9a939c776adbe973d56bbf5654fc470");
        StringRequest request = new StringRequest(url, response -> {
            try {
                JSONObject json = new JSONObject(response);
                boolean b = json.optString("passcode").equals("1");
                status_data.setValue(b);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, Throwable::printStackTrace);
        request_queue.add(request);
    }

    public void updateStatus() {
        String url = String.format("https://service.skywatch24.com/api/v2/devices/%s/locknotification", "49209");
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {

            }, Throwable::printStackTrace) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();

                // generate params
                try {
                    JSONObject setting = new JSONObject();
                    setting.put("passcode", status_data.getValue() ? "1" : "0");
                    setting.put("fingerprint", "0");
                    setting.put("keycard", "0");

                    params.put("api_key", "b9a939c776adbe973d56bbf5654fc470");
                    params.put("lock_notification", setting.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };

        request_queue.add(request);

    }
}
