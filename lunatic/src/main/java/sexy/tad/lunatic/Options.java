package sexy.tad.lunatic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

/**
 * Options for the DatePickerView.
 */
public class Options {
    final LocalDate min;
    final LocalDate max;
    final DayOfWeek firstDayOfWeek;
    final Locale locale;
    final String headerFormat;
    final String weekdayFormat;
    final String[] weekdayLabels;

    Options(Builder builder) {
        this.min = builder.min;
        this.max = builder.max;
        this.firstDayOfWeek = builder.firstDayOfWeek;
        this.locale = builder.locale;
        this.headerFormat = builder.headerFormat;
        this.weekdayFormat = builder.weekdayFormat;
        this.weekdayLabels = builder.weekdayLabels;
    }

    /**
     * Created by tad on 11/19/15.
     */
    public static class Builder {
        private LocalDate min;
        private LocalDate max;
        private DayOfWeek firstDayOfWeek;
        private Locale locale;
        private String headerFormat;
        private String weekdayFormat;
        private final String[] weekdayLabels = new String[7];

        public Builder() {
            // Pass
        }

        public Builder(Options options) {
            if (options != null) {
                min = options.min;
                max = options.max;
                firstDayOfWeek = options.firstDayOfWeek;
                locale = options.locale;
                headerFormat = options.headerFormat;
                weekdayFormat = options.weekdayFormat;
            }
        }

        public Builder setMin(LocalDate min) {
            this.min = min;
            return this;
        }

        public Builder setMax(LocalDate max) {
            this.max = max;
            return this;
        }

        public Builder setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
            this.firstDayOfWeek = firstDayOfWeek;
            return this;
        }

        public Builder setFirstDayOfWeek(int firstDayOfWeek) {
            return setFirstDayOfWeek(DayOfWeek.of(firstDayOfWeek));
        }

        public Builder setLocale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public Builder setHeaderFormat(String headerFormat) {
            this.headerFormat = headerFormat;
            return this;
        }

        public Builder setWeekdayFormat(String weekdayFormat) {
            this.weekdayFormat = weekdayFormat;
            return this;
        }

        public Options build() {
            Preconditions.checkNotNull(min, "min date cannot be null");
            Preconditions.checkNotNull(max, "max date cannot be null");

            if (firstDayOfWeek == null) {
                firstDayOfWeek = DayOfWeek.SUNDAY;
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
            for (int i = 1; i <= 7; i++) {
                cal.set(Calendar.DAY_OF_WEEK, i);
                weekdayLabels[(i + 4) % 7] = df.format(cal.getTime());
            }

            return new Options(this);
        }
    }
}
