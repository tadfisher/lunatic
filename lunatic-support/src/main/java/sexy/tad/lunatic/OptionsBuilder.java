package sexy.tad.lunatic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static sexy.tad.lunatic.Preconditions.checkArgument;
import static sexy.tad.lunatic.Preconditions.checkNotNull;

/**
 * Created by tad on 11/19/15.
 */
public abstract class OptionsBuilder<DateType> {
    private Lunatic.Date min;
    private Lunatic.Date max;
    private int firstDayOfWeek;
    private Locale locale;
    private String headerFormat;
    private String weekdayFormat;

    OptionsBuilder(Options options) {
        if (options != null) {
            min = options.min;
            max = options.max;
            firstDayOfWeek = options.firstDayOfWeek;
            locale = options.locale;
            headerFormat = options.headerFormat;
            weekdayFormat = options.weekdayFormat;
        }
    }

    public abstract OptionsBuilder<DateType> setMin(DateType min);

    public abstract OptionsBuilder<DateType> setMax(DateType max);

    OptionsBuilder<DateType> setMin(Lunatic.Date min) {
        this.min = min;
        return this;
    }

    OptionsBuilder<DateType> setMax(Lunatic.Date max) {
        this.max = max;
        return this;
    }

    public OptionsBuilder<DateType> setFirstDayOfWeek(int firstDayOfWeek) {
        Preconditions.checkArgument(firstDayOfWeek >= 1 && firstDayOfWeek <= 7,
                "firstDayOfWeek must be an ISO weekday number");
        this.firstDayOfWeek = firstDayOfWeek;
        return this;
    }

    public OptionsBuilder<DateType> setLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public OptionsBuilder<DateType> setHeaderFormat(String headerFormat) {
        this.headerFormat = headerFormat;
        return this;
    }

    public OptionsBuilder<DateType> setWeekdayFormat(String weekdayFormat) {
        this.weekdayFormat = weekdayFormat;
        return this;
    }

    public Options build() {
        Preconditions.checkNotNull(min, "min date cannot be null");
        Preconditions.checkNotNull(max, "max date cannot be null");

        if (firstDayOfWeek == 0) {
            firstDayOfWeek = 7;
        }

        if (locale == null) {
            locale = Locale.getDefault();
        }

        if (headerFormat == null) {
            headerFormat = "MMMM yyyy";
        }

        if (weekdayFormat == null) {
            weekdayFormat = "ccccc";
        }

        SimpleDateFormat df = new SimpleDateFormat(weekdayFormat, locale);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2015, Calendar.JANUARY, 1, 0, 0, 0);
        String[] weekdayLabels = new String[7];
        for (int i = 1; i <= 7; i++) {
            cal.set(Calendar.DAY_OF_WEEK, i);
            weekdayLabels[(i + 4) % 7] = df.format(cal.getTime());
        }

        return new Options(min, max, firstDayOfWeek, locale, headerFormat,
                weekdayFormat, weekdayLabels);
    }
}
