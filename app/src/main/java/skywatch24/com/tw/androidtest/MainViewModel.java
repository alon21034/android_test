package skywatch24.com.tw.androidtest;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;

public class MainViewModel extends AndroidViewModel {

    private MutableLiveData<Boolean> is_dirty_data;
    private MutableLiveData<Boolean> show_spinner;
    private MutableLiveData<Boolean> status_data;

    private DataSource data_source;
    private Observer<Boolean> status_observer;

    public MainViewModel(@NonNull Application application, DataSource data_source) {
        super(application);

        this.data_source = data_source;
        is_dirty_data = new MutableLiveData<>();
        show_spinner = new MutableLiveData<>();
        status_data = new MutableLiveData<>();

        // init value
        status_data.setValue(false);

        // update from server
        show_spinner.setValue(true);

        status_observer = b -> {
            status_data.setValue(b);
            show_spinner.setValue(false);
        };
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        data_source.getStatusValue().removeObserver(status_observer);
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

    public void updateStatus() {
        show_spinner.setValue(true);
        data_source.updateStatus();
    }

    public void loadStatus() {
        data_source.loadStatus();
    }

}
