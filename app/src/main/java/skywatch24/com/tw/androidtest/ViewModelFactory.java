package skywatch24.com.tw.androidtest;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class ViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {

    private Application application;
    private DataSource dataSource;

    /**
     * Creates a {@code AndroidViewModelFactory}
     *
     * @param application an application to pass in {@link AndroidViewModel}
     */
    public ViewModelFactory(@NonNull Application application, DataSource ds) {
        super(application);
        dataSource = ds;
        this.application = application;
    }


    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(application, dataSource);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
