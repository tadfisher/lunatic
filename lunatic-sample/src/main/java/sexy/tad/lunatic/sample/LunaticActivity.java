package sexy.tad.lunatic.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.util.Locale;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import sexy.tad.lunatic.DatePickerView;
import sexy.tad.lunatic.Options;

public class LunaticActivity extends AppCompatActivity {
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Options options = Options.builder(new Locale("es", "ES"))
        .min(LocalDate.of(2016, Month.MARCH, 1))
        .max(LocalDate.of(2099, Month.DECEMBER, 31))
        .build();

    DatePickerView datePickerView = (DatePickerView) findViewById(R.id.date_picker);
    datePickerView.setOptions(options);
  }
}
