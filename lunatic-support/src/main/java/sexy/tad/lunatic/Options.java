package sexy.tad.lunatic;

import java.util.Locale;

/**
 * Options for the DatePickerView.
 */
public class Options {
    final Lunatic.Date min;
    final Lunatic.Date max;
    final int firstDayOfWeek;
    final Locale locale;
    final String headerFormat;
    final String weekdayFormat;
    final String[] weekdayLabels;

    Options(Lunatic.Date min,
            Lunatic.Date max,
            int firstDayOfWeek,
            Locale locale,
            String headerFormat,
            String weekdayFormat,
            String[] weekdayLabels) {
        this.min = min;
        this.max = max;
        this.firstDayOfWeek = firstDayOfWeek;
        this.locale = locale;
        this.headerFormat = headerFormat;
        this.weekdayFormat = weekdayFormat;
        this.weekdayLabels = weekdayLabels;
    }

}
