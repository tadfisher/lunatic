package sexy.tad.lunatic.sample;

import android.app.Application;
import com.jakewharton.threetenabp.AndroidThreeTen;

/**
 * Created by tad on 11/19/15.
 */
public class LunaticApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
