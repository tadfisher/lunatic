package lunatic;

import com.google.auto.value.AutoValue;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.TextStyle;
import org.threeten.bp.temporal.WeekFields;
import java.util.Locale;

/**
 * Options for the DatePickerView.
 */
@AutoValue
public abstract class Options {

  /**
   * Create a new builder with default values set from the specified locale.
   */
  public static Builder builder(Locale locale) {
    if (locale == null) {
      throw new NullPointerException("locale == null");
    }

    LocalDate now = LocalDate.now();

    return new AutoValue_Options.Builder()
        .locale(locale)
        .headerPattern("MMMM yyyy")
        .min(now.minusWeeks(1))
        .max(now.plusYears(1))
        .now(now)
        .weekFields(WeekFields.of(locale))
        .weekdayStyle(TextStyle.NARROW);
  }

  /**
   * The minimum date by which to bound the date picker view.
   */
  public abstract LocalDate min();

  /**
   * The maximum date by which to bound the date picker view.
   */
  public abstract LocalDate max();

  /**
   * The date to display as "today" in the date picker view.
   */
  public abstract LocalDate now();

  /**
   * The pattern used to format the header for each month view.
   *
   * @see DateTimeFormatterBuilder#appendPattern(String)
   */
  public abstract String headerPattern();

  /**
   * The definition of a "week" according to the locale, encompassing the first day of each
   * week and the minimum number of days in the first week of the year. The default is set
   * from the locale.
   *
   * @see WeekFields#of(Locale)
   * @see WeekFields#of(DayOfWeek, int)
   */
  public abstract WeekFields weekFields();

  /**
   * The style in which to show the weekday headers.
   *
   * @see TextStyle
   */
  public abstract TextStyle weekdayStyle();

  abstract Locale locale();

  /**
   * Create a new builder with values set from this options instance.
   */
  public Builder newBuilder() {
    return new AutoValue_Options.Builder(this);
  }

  DateTimeFormatter buildHeaderFormatter() {
    return new DateTimeFormatterBuilder()
        .appendPattern(headerPattern())
        .toFormatter(locale());
  }

  String[] buildWeekdayNames() {
    String[] names = new String[7];
    for (int i = 0; i < 7; i++) {
      // Populate week names based on the locale-specific definition of a week;
      // i.e. {"M", "T", "W", ...} for the ISO8601 locale.
      names[Utils.startOfWeekOffset(weekFields(), DayOfWeek.of(i + 1))] =
          DayOfWeek.of(i + 1).getDisplayName(weekdayStyle(), locale());
    }
    return names;
  }

  @AutoValue.Builder
  public abstract static class Builder {

    /**
     * The minimum date by which to bound the date picker view.
     */
    public abstract Builder min(LocalDate min);

    /**
     * The maximum date by which to bound the date picker view.
     */
    public abstract Builder max(LocalDate max);

    /**
     * The date to display as "today" in the date picker view.
     */
    public abstract Builder now(LocalDate now);

    /**
     * The pattern used to format the header for each month view.
     *
     * @see DateTimeFormatterBuilder#appendPattern(String)
     */
    public abstract Builder headerPattern(String headerPattern);

    /**
     * The definition of a "week" according to the locale, encompassing the first day of each
     * week and the minimum number of days in the first week of the year. The default is set
     * from the locale.
     *
     * @see WeekFields#of(Locale)
     * @see WeekFields#of(DayOfWeek, int)
     */
    public abstract Builder weekFields(WeekFields weekFields);

    /**
     * The style in which to show the weekday headers. The default is
     * {@linkplain TextStyle#NARROW NARROW}.
     *
     * @see TextStyle
     */
    public abstract Builder weekdayStyle(TextStyle weekdayStyle);

    /**
     * The locale used to format time values. This is also used to set the default values for
     * {@link #weekFields()} and {@link #weekdayStyle()}.
     */
    abstract Builder locale(Locale locale);

    /**
     * Build a new {@link Options} instance.
     */
    public abstract Options build();
  }
}
