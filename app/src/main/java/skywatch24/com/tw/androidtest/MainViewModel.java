package skywatch24.com.tw.androidtest;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainViewModel extends AndroidViewModel {

    private MutableLiveData<Boolean> is_dirty_data;
    private MutableLiveData<Boolean> show_spinner;
    private MutableLiveData<Boolean> status_data;

    private RequestQueue request_queue;

    public MainViewModel(@NonNull Application application) {
        super(application);

        is_dirty_data = new MutableLiveData<>();
        show_spinner = new MutableLiveData<>();
        status_data = new MutableLiveData<>();

        request_queue = Volley.newRequestQueue(application.getApplicationContext());

        // init value
        status_data.setValue(false);

        // update from server
        getStatusValue();
    }

    public LiveData<Boolean> getIsDirtyData() {
        return is_dirty_data;
    }

    public LiveData<Boolean> shoSpinner() {
        return show_spinner;
    }

    public LiveData<Boolean> getStatus() {
        return status_data;
    }

    public void setStatus(boolean status) {
        is_dirty_data.setValue(true);
        status_data.setValue(status);
    }

    public void getStatusValue() {
        show_spinner.setValue(true);
        String url = String.format("https://service.skywatch24.com/api/v2/devices/%s/locknotification?api_key=%s", "49209", "b9a939c776adbe973d56bbf5654fc470");
        StringRequest request = new StringRequest(url, response -> {
            try {
                JSONObject json = new JSONObject(response);
                boolean b = json.optString("passcode").equals("1");
                status_data.setValue(b);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                show_spinner.setValue(false);
            }
        }, Throwable::printStackTrace);
        request_queue.add(request);
    }

    public void updateStatus() {
        show_spinner.setValue(true);
        String url = String.format("https://service.skywatch24.com/api/v2/devices/%s/locknotification", "49209");
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            show_spinner.setValue(false);
            is_dirty_data.setValue(false);
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
