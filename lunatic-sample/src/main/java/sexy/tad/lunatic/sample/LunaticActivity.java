package sexy.tad.lunatic.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import sexy.tad.lunatic.DatePickerView;
import sexy.tad.lunatic.Options;

public class LunaticActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Options options = new Options.Builder()
                .setMin(LocalDate.of(2015, Month.JANUARY, 1))
                .setMax(LocalDate.of(2099, Month.DECEMBER, 31))
                .setFirstDayOfWeek(DayOfWeek.SUNDAY)
                .setWeekdayFormat("ccc")
                .build();

        DatePickerView datePickerView = (DatePickerView) findViewById(R.id.date_picker);
        datePickerView.setOptions(options);
    }
}
