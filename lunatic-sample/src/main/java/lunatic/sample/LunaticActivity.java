package lunatic.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import codes.tad.lunatic.sample.R;
import java.util.Locale;
import lunatic.DatePickerView;
import lunatic.Options;
import org.threeten.bp.DayOfWeek;

public class LunaticActivity extends AppCompatActivity {
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Options options = Options.builder(Locale.getDefault()).build();

    DatePickerView datePickerView = (DatePickerView) findViewById(R.id.date_picker);
    datePickerView.setOptions(options);
    datePickerView.setFilter(date -> {
      switch (DayOfWeek.from(date)) {
        case SATURDAY:
        case SUNDAY:
          return false;
        default:
          return true;
      }
    });
  }
}
