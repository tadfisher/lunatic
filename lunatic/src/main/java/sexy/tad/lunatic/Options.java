package sexy.tad.lunatic;

import android.os.Parcelable;
import com.google.auto.value.AutoValue;
import java.util.Locale;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.TextStyle;
import org.threeten.bp.temporal.WeekFields;

/**
 * Options for the DatePickerView.
 */
@AutoValue
public abstract class Options implements Parcelable {
  public abstract LocalDate min();
  public abstract LocalDate max();
  public abstract String headerPattern();
  public abstract WeekFields weekFields();
  public abstract TextStyle weekdayStyle();
  abstract Locale locale();

  DateTimeFormatter buildHeaderFormatter() {
    return new DateTimeFormatterBuilder()
        .appendPattern(headerPattern())
        .toFormatter(locale());
  }

  String[] buildWeekdayNames() {
    String[] names = new String[7];
    int firstDayOfWeek = weekFields().getFirstDayOfWeek().getValue();
    for (int i = 0; i < 7; i++) {
      // Populate week names based on the locale-specific definition of a week;
      // i.e. {"M", "T", "W", ...} for the ISO8601 locale.
      names[Utils.startOfWeekOffset(weekFields(), DayOfWeek.of(i + 1))] =
          DayOfWeek.of(i + 1).getDisplayName(weekdayStyle(), locale());
    }
    return names;
  }

  /**
   * Create a new builder with default values set from the specified locale.
   */
  public static Builder builder(Locale locale) {
    if (locale == null) {
      throw new NullPointerException("locale == null");
    }

    return new AutoValue_Options.Builder()
        .locale(locale)
        .headerPattern("MMMM yyyy")
        .weekFields(WeekFields.of(locale))
        .weekdayStyle(TextStyle.NARROW);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder min(LocalDate min);
    public abstract Builder max(LocalDate max);
    public abstract Builder headerPattern(String headerPattern);
    public abstract Builder weekFields(WeekFields weekFields);
    public abstract Builder weekdayStyle(TextStyle weekdayStyle);
    abstract Builder locale(Locale locale);
    public abstract Options build();
  }
}
