package sexy.tad.lunatic.sample;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by tad on 11/19/15.
 */
public class LunaticApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        AndroidThreeTen.init(this);
    }
}
