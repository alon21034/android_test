package skywatch24.com.tw.androidtest;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "main_activity";

    private final int ID_PASSCODE = 0;

    private LinearLayout container;

    private CustomToggleView passcode_view;
    private ProgressBar spinner;

    private boolean is_dirty;

    private RequestQueue request_queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.lock_notification_container);
        spinner = findViewById(R.id.spinner);

        request_queue = Volley.newRequestQueue(getApplicationContext());

        getTitleView(container, "Title");
        passcode_view = getToggleView(container, "passcode", ID_PASSCODE);

        // initialize
        passcode_view.setStatus(false);

        is_dirty = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        getInitialValue();
    }

    private void getInitialValue() {
        // get initial status from server
        spinner.setVisibility(View.VISIBLE);
        spinner.bringToFront();
        String url = String.format("https://service.skywatch24.com/api/v2/devices/%s/locknotification?api_key=%s", "49209", "b9a939c776adbe973d56bbf5654fc470");
        StringRequest request = new StringRequest(url, response -> {
            try {
                Log.d(TAG, response);
                JSONObject json = new JSONObject(response);
                boolean b = json.optString("passcode").equals("1");
                passcode_view.setStatus(b);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                spinner.setVisibility(View.GONE);
            }
        }, error -> {
            error.printStackTrace();
        });
        request_queue.add(request);
    }

    private void setSettingValue(boolean status) {
        spinner.setVisibility(View.VISIBLE);
        spinner.bringToFront();
        String url = String.format("https://service.skywatch24.com/api/v2/devices/%s/locknotification", "49209");
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            spinner.setVisibility(View.GONE);
        }, Throwable::printStackTrace) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();

                // generate params
                try {
                    JSONObject setting = new JSONObject();
                    setting.put("passcode", status ? "1" : "0");
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

    private TextView getTitleView(ViewGroup parent, String title) {
        TextView textView = new TextView(getApplicationContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getDP(48));
        textView.setLayoutParams(params);
        textView.setPadding(getDP(16), 0, 0, 0);
        textView.setText(title);
        textView.setTextSize(14);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTextColor(Color.BLACK);
        parent.addView(textView);
        return textView;
    }

    private class CustomToggleView extends RelativeLayout {

        private boolean status;

        private String title = "";
        private TextView title_text;
        private ImageView toggle_image;
        private int id = -1;
        public CustomToggleView(Context context) {
            super(context);

            this.status = false;

            RelativeLayout.LayoutParams container_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDP(64));
            setLayoutParams(container_params);
            title_text = new TextView(getApplicationContext());
            RelativeLayout.LayoutParams title_text_params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, getDP(20));
            title_text.setLayoutParams(title_text_params);
            title_text_params.addRule(RelativeLayout.CENTER_VERTICAL);
            title_text.setTextColor(Color.BLUE);
            title_text.setTextSize(16);
            title_text.setLines(1);
            title_text.setMaxLines(1);
            title_text.setGravity(Gravity.CENTER_VERTICAL);
            title_text.setPadding(getDP(72), 0, 0, 0);
            toggle_image = new ImageView(getApplicationContext());
            RelativeLayout.LayoutParams toggle_image_params = new RelativeLayout.LayoutParams(getDP(50), getDP(30));
            toggle_image_params.addRule(RelativeLayout.ALIGN_PARENT_END);
            toggle_image_params.addRule(RelativeLayout.CENTER_VERTICAL);
            toggle_image_params.setMargins(0, 0, getDP(16), 0);
            toggle_image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            toggle_image.setLayoutParams(toggle_image_params);
            toggle_image.setImageResource(R.drawable.ic_on);
            addView(title_text);
            addView(toggle_image);

            toggle_image.setOnClickListener(__ -> {
                this.status = !this.status;
                toggle_image.setImageResource(this.status?R.drawable.ic_on:R.drawable.ic_off);
                is_dirty = true;
                invalidateOptionsMenu();
            });

        }
        public void setTitle(String str) {
            this.title = str;
            title_text.setText(str);
        }
        public void setStatus(boolean status) {
            this.status = status;
            if (status) {
                toggle_image.setImageResource(R.drawable.ic_on);
            } else {
                toggle_image.setImageResource(R.drawable.ic_off);
            }
        }
        public boolean getStatus() {
            return this.status;
        }
        public void setId(int id) {
            this.id = id;
        }
    }

    private CustomToggleView getToggleView(ViewGroup parent, String title, int id) {
        CustomToggleView row_container = new CustomToggleView(getApplicationContext());
        row_container.setId(id);
        row_container.setTitle(title);
        parent.addView(row_container);
        return row_container;
    }

    private int getDP(int dps) {
        final float scale = getApplication().getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        prepareOptionMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private void prepareOptionMenu(Menu menu) {
        menu.clear();
        if (is_dirty) {
            getMenuInflater().inflate(R.menu.menu_done, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_done:
                Log.d(TAG, "click menu item");
                setSettingValue(passcode_view.getStatus());
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
