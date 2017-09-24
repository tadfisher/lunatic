package lunatic.sample;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import codes.tad.lunatic.sample.R;
import java.util.Locale;
import lunatic.CircleHighlight;
import lunatic.DatePickerView;
import lunatic.Options;
import lunatic.SelectionListener;
import lunatic.SingleSelectionListener;
import org.threeten.bp.DayOfWeek;

public class LunaticActivity extends AppCompatActivity {
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    DatePickerView datePickerView = findViewById(R.id.date_picker);

    Options options = Options.builder(Locale.getDefault()).build();
    datePickerView.setOptions(options);

    SelectionListener listener = new SingleSelectionListener(datePickerView,
        "primary",
        new CircleHighlight(ContextCompat.getColor(this, R.color.colorAccent)));
    datePickerView.addListener(listener);

    datePickerView.addFilter(date -> {
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
