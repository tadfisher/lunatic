package lunatic.sample;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import codes.tad.lunatic.sample.R;
import java.util.Locale;
import lunatic.CircleHighlight;
import lunatic.DatePickerView;
import lunatic.DotHighlight;
import lunatic.Options;
import lunatic.SelectionListener;
import lunatic.SingleSelectionListener;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

public class LunaticActivity extends AppCompatActivity {
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    DatePickerView datePickerView = findViewById(R.id.date_picker);

    Options options = Options.builder(Locale.getDefault()).build();
    datePickerView.setOptions(options);

    SelectionListener primaryListener = new SingleSelectionListener(datePickerView,
        "primary",
        new CircleHighlight(ContextCompat.getColor(this, R.color.colorAccent)));
    datePickerView.addListener(primaryListener);

    final int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f,
        getResources().getDisplayMetrics());

    SelectionListener secondaryListener = new SingleSelectionListener(datePickerView,
        "secondary",
        new DotHighlight(ContextCompat.getColor(this, R.color.colorAccentLight), size)) {
      @Override public void onDateClicked(LocalDate date) {
        if (date.equals(getSelection())) {
          return;
        }
        selection = date;
        datePickerView.clear(tag);
        for (int i = 0; i < 5; i++) {
          date = date.plusDays(3);
          datePickerView.select(tag, date, highlight);
        }
      }
    };
    datePickerView.addListener(secondaryListener);

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
