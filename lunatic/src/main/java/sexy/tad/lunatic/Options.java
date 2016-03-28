package sexy.tad.lunatic;

import android.os.Parcelable;
import com.google.auto.value.AutoValue;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

/**
 * Options for the DatePickerView.
 */
@AutoValue
public abstract class Options implements Parcelable {
  public abstract LocalDate min();
  public abstract LocalDate max();
  public abstract DayOfWeek firstDayOfWeek();
  public abstract Locale locale();
  public abstract String headerFormat();
  public abstract String weekdayFormat();

  public static Builder builder() {
    return new AutoValue_Options.Builder()
        .firstDayOfWeek(DayOfWeek.SUNDAY)
        .locale(Locale.getDefault())
        .headerFormat("MMMM yyyy")
        .weekdayFormat("ccccc");
  }

  public String[] weekdayLabels() {
    // TODO fix this
    String[] weekdayLabels = new String[7];
    SimpleDateFormat df = new SimpleDateFormat(weekdayFormat(), locale());
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
    cal.set(2015, Calendar.JANUARY, 1, 0, 0, 0);
    for (int i = 1; i <= 7; i++) {
      cal.set(Calendar.DAY_OF_WEEK, i);
      weekdayLabels[(i + 4) % 7] = df.format(cal.getTime());
    }
    return weekdayLabels;
  }

  /**
   * Created by tad on 11/19/15.
   */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder min(LocalDate min);
    public abstract Builder max(LocalDate max);
    public abstract Builder firstDayOfWeek(DayOfWeek firstDayOfWeek);
    public abstract Builder locale(Locale locale);
    public abstract Builder headerFormat(String headerFormat);
    public abstract Builder weekdayFormat(String weekdayFormat);
    public abstract Options build();
  }
}
