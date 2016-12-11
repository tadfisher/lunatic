package lunatic.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import codes.tad.lunatic.sample.R;
import java.util.Locale;
import lunatic.DatePickerView;
import lunatic.Options;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

public class LunaticActivity extends AppCompatActivity {
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Options options = Options.builder(Locale.getDefault())
        .min(LocalDate.of(2016, Month.MARCH, 1))
        .max(LocalDate.of(2099, Month.DECEMBER, 31))
        .build();

    DatePickerView datePickerView = (DatePickerView) findViewById(R.id.date_picker);
    datePickerView.setOptions(options);
  }
}
