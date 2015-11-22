package sexy.tad.lunatic;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

/**
 * Java java.util.* implementation of the Lunatic API.
 */
public class JavaUtil {
    private static final TimeZone DEFAULT_ZONE = TimeZone.getTimeZone("UTC");

    public static JavaUtilOptionsBuilder newOptionsBuilder(Options options) {
        return new JavaUtilOptionsBuilder(options);
    }

    public static JavaUtilOptionsBuilder newOptionsBuilder() {
        return newOptionsBuilder(null);
    }

    public Lunatic.DateFilter newDateFilter(DateFilter filter) {
        return new JavaUtilDateFilterAdapter(filter);
    }

    public Lunatic.SelectionListener newSelectionListener(SelectionListener listener) {
        return new JavaUtilSelectionListenerAdapter(listener);
    }

    public static abstract class DateFilter {
        public abstract void setEnabledDates(int year, int month, boolean[] enabledDays);
    }

    public static abstract class SelectionListener {
        public abstract void onDateSelected(int year, int month, int day);
    }

    static class JavaUtilDateFilterAdapter implements Lunatic.DateFilter {
        private final DateFilter filter;

        JavaUtilDateFilterAdapter(DateFilter dateFilter) {
            this.filter = dateFilter;
        }

        @Override
        public void setEnabledDates(Lunatic.YearMonth month, boolean[] enabledDays) {
            filter.setEnabledDates(month.year(), month.month(), enabledDays);
        }
    }

    static class JavaUtilSelectionListenerAdapter implements Lunatic.SelectionListener {
        private final SelectionListener listener;

        JavaUtilSelectionListenerAdapter(SelectionListener selectionListener) {
            this.listener = selectionListener;
        }

        @Override
        public void onDateSelected(Lunatic.Date date) {
            listener.onDateSelected(date.year(), date.month(), date.day());
        }
    }

    public static class JavaUtilOptionsBuilder extends OptionsBuilder<Date> {
        JavaUtilOptionsBuilder(Options options) {
            super(options);
        }

        @Override
        public JavaUtilOptionsBuilder setMin(Date min) {
            setMin(JavaDate.to(min));
            return this;
        }

        @Override
        public JavaUtilOptionsBuilder setMax(Date max) {
            setMax(JavaDate.to(max));
            return this;
        }

        @Override
        public JavaUtilOptionsBuilder setFirstDayOfWeek(int firstDayOfWeek) {
            // Translate Calendar constant to ISO
            super.setFirstDayOfWeek((firstDayOfWeek + 5) % 7 + 1);
            return this;
        }
    }

    static class JavaDate implements Lunatic.Date {
        // FIXME run everything in UTC.

        private final Calendar calendar;

        static Calendar from(Lunatic.Date date, TimeZone zone) {
            Calendar calendar = Calendar.getInstance(zone);
            calendar.clear();
            //noinspection MagicConstant
            calendar.set(date.year(), date.month() - 1, date.day());
            return calendar;
        }

        static Lunatic.Date to(Date date) {
            return new JavaDate(date, DEFAULT_ZONE);
        }

        static Lunatic.Date to(Calendar calendar) {
            return new JavaDate(calendar);
        }

        public JavaDate(Date date, TimeZone zone) {
            // Go through this dance to ensure extraneous calendar fields are unset.
            Calendar temp = Calendar.getInstance(zone);
            temp.clear();
            temp.setTime(date);

            calendar = Calendar.getInstance(zone);
            calendar.clear();
            calendar.set(YEAR, temp.get(YEAR));
            calendar.set(MONTH, temp.get(MONTH));
            calendar.set(DAY_OF_MONTH, temp.get(DAY_OF_MONTH));
        }

        public JavaDate(Calendar calendar) {
            this.calendar = Calendar.getInstance(calendar.getTimeZone());
            this.calendar.clear();
            //noinspection MagicConstant
            this.calendar.set(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH));
        }

        JavaDate(int year, int month, int day, TimeZone zone) {
            calendar = Calendar.getInstance(zone);
            calendar.clear();
            calendar.set(year, month - 1, day);
        }

        @Override
        public int year() {
            return calendar.get(YEAR);
        }

        @Override
        public int month() {
            return calendar.get(MONTH) + 1;
        }

        @Override
        public int day() {
            return calendar.get(DAY_OF_MONTH);
        }

        @Override
        public int weekday() {
            int weekday =  calendar.get(DAY_OF_WEEK) - 1;
            return weekday == 0 ? 7 : weekday;
        }

        @Override
        public Lunatic.Date plusDays(int days) {
            calendar.add(DAY_OF_YEAR, days);
            return to(calendar);
        }

        @Override
        public Lunatic.YearMonth yearMonth() {
            return JavaYearMonth.to(calendar);
        }

        @Override
        public int daysBetween(Lunatic.Date other) {
            Calendar o = from(other, calendar.getTimeZone());
            int years = o.get(YEAR) - calendar.get(YEAR);
            int leapDays = getLeapYears(other.year()) - getLeapYears(year());
            return leapDays + years * 365 + o.get(DAY_OF_YEAR) - calendar.get(DAY_OF_YEAR);
        }

        @Override
        public int compareTo(Lunatic.Date other) {
            return calendar.compareTo(from(other, calendar.getTimeZone()));
        }

        @Override
        public boolean equals(Lunatic.Date other) {
            return compareTo(other) == 0;
        }

        @Override
        public boolean before(Lunatic.Date other) {
            return compareTo(other) < 0;
        }

        @Override
        public boolean after(Lunatic.Date other) {
            return compareTo(other) > 0;
        }

        private int getLeapYears(int year) {
            return year / 4 - year / 100 + year / 400;
        }
    }

    static class JavaYearMonth implements Lunatic.YearMonth {
        private final Calendar calendar;

        static Calendar from(Lunatic.YearMonth yearMonth, TimeZone zone) {
            Calendar calendar = Calendar.getInstance(zone);
            calendar.clear();
            //noinspection MagicConstant
            calendar.set(YEAR, yearMonth.year());
            calendar.set(MONTH, yearMonth.month() - 1);
            return calendar;
        }

        static Lunatic.YearMonth to(Calendar calendar) {
            return new JavaYearMonth(calendar);
        }

        public JavaYearMonth(Calendar calendar) {
            this.calendar = Calendar.getInstance(calendar.getTimeZone());
            this.calendar.clear();
            this.calendar.set(YEAR, calendar.get(YEAR));
            this.calendar.set(MONTH, calendar.get(MONTH));
        }

        JavaYearMonth(int year, int month, TimeZone zone) {
            calendar = Calendar.getInstance(zone);
            calendar.clear();
            calendar.set(YEAR, year);
            calendar.set(MONTH, month - 1);
        }

        @Override
        public int year() {
            return calendar.get(YEAR);
        }

        @Override
        public int month() {
            return calendar.get(MONTH) + 1;
        }

        @Override
        public int days() {
            return calendar.getActualMaximum(DAY_OF_MONTH);
        }

        @Override
        public Lunatic.Date day(int dayOfMonth) {
            return new JavaDate(year(), month(), dayOfMonth, calendar.getTimeZone());
        }

        @Override
        public Lunatic.YearMonth plusMonths(int months) {
            Calendar temp = (Calendar) calendar.clone();
            temp.add(MONTH, months);
            return to(temp);
        }

        @Override
        public int monthsBetween(Lunatic.YearMonth other) {
            Calendar o = from(other, calendar.getTimeZone());
            int years = o.get(YEAR) - calendar.get(YEAR);
            return years * 12 + o.get(MONTH) - calendar.get(MONTH);
        }

        @Override
        public int compareTo(Lunatic.YearMonth other) {
            Calendar o = from(other, calendar.getTimeZone());
            return calendar.compareTo(o);
        }

        @Override
        public boolean equals(Lunatic.YearMonth other) {
            return compareTo(other) == 0;
        }

        @Override
        public boolean before(Lunatic.YearMonth other) {
            return compareTo(other) < 0;
        }

        @Override
        public boolean after(Lunatic.YearMonth other) {
            return compareTo(other) > 0;
        }
    }
}
