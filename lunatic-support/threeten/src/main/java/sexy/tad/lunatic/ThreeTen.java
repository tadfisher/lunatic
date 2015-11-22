package sexy.tad.lunatic;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;
import org.threeten.bp.temporal.ChronoUnit;

/**
 * ThreeTen implementation of the Lunatic API.
 */
public class ThreeTen {

    public static ThreeTenOptionsBuilder newOptionsBuilder(Options options) {
        return new ThreeTenOptionsBuilder(options);
    }

    public static ThreeTenOptionsBuilder newOptionsBuilder() {
        return newOptionsBuilder(null);
    }

    public static Lunatic.DateFilter newDateFilter(DateFilter dateFilter) {
        return new ThreeTenDateFilterAdapter(dateFilter);
    }

    public static Lunatic.SelectionListener newSelectionListener(SelectionListener listener) {
        return new ThreeTenSelectionListenerAdapter(listener);
    }

    public static abstract class DateFilter {
        public abstract void setEnabledDates(YearMonth month, boolean[] enabledDays);
    }

    public static abstract class SelectionListener {
        public abstract void onDateSelected(LocalDate date);
    }

    static class ThreeTenDateFilterAdapter implements Lunatic.DateFilter {
        private final DateFilter dateFilter;

        public ThreeTenDateFilterAdapter(DateFilter dateFilter) {
            this.dateFilter = dateFilter;
        }

        @Override
        public void setEnabledDates(Lunatic.YearMonth month, boolean[] enabledDays) {
            dateFilter.setEnabledDates(ThreeTenYearMonth.from(month), enabledDays);
        }
    }

    static class ThreeTenSelectionListenerAdapter implements Lunatic.SelectionListener {
        private final SelectionListener listener;

        public ThreeTenSelectionListenerAdapter(SelectionListener listener) {
            this.listener = listener;
        }

        @Override
        public void onDateSelected(Lunatic.Date date) {
            listener.onDateSelected(ThreeTenDate.from(date));
        }
    }

    public static class ThreeTenOptionsBuilder extends OptionsBuilder<LocalDate> {
        ThreeTenOptionsBuilder(Options options) {
            super(options);
        }

        @Override
        public ThreeTenOptionsBuilder setMin(LocalDate min) {
            setMin(ThreeTenDate.to(min));
            return this;
        }

        @Override
        public ThreeTenOptionsBuilder setMax(LocalDate max) {
            setMax(ThreeTenDate.to(max));
            return this;
        }

        public ThreeTenOptionsBuilder setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
            super.setFirstDayOfWeek(firstDayOfWeek.getValue());
            return this;
        }
    }

    static class ThreeTenDate implements Lunatic.Date {
        private final LocalDate localDate;

        static LocalDate from(Lunatic.Date date) {
            return LocalDate.of(date.year(), date.month(), date.day());
        }

        static Lunatic.Date to(LocalDate localDate) {
            return new ThreeTenDate(localDate);
        }

        public ThreeTenDate(LocalDate localDate) {
            this.localDate = localDate;
        }

        @Override
        public int year() {
            return localDate.getYear();
        }

        @Override
        public int month() {
            return localDate.getMonthValue();
        }

        @Override
        public int day() {
            return localDate.getDayOfMonth();
        }

        @Override
        public int weekday() {
            return localDate.getDayOfWeek().getValue();
        }

        @Override
        public Lunatic.Date plusDays(int days) {
            return to(localDate.plusDays(days));
        }

        @Override
        public Lunatic.YearMonth yearMonth() {
            return ThreeTenYearMonth.to(YearMonth.from(localDate));
        }

        @Override
        public int daysBetween(Lunatic.Date other) {
            return (int) ChronoUnit.DAYS.between(localDate, from(other));
        }

        @Override
        public int compareTo(Lunatic.Date other) {
            return localDate.compareTo(from(other));
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
    }

    static class ThreeTenYearMonth implements Lunatic.YearMonth {
        private final YearMonth yearMonth;

        static YearMonth from(Lunatic.YearMonth yearMonth) {
            return YearMonth.of(yearMonth.year(), yearMonth.month());
        }

        static Lunatic.YearMonth to(YearMonth yearMonth) {
            return new ThreeTenYearMonth(yearMonth);
        }

        public ThreeTenYearMonth(YearMonth yearMonth) {
            this.yearMonth = yearMonth;
        }

        @Override
        public int year() {
            return yearMonth.getYear();
        }

        @Override
        public int month() {
            return yearMonth.getMonthValue();
        }

        @Override
        public int days() {
            return yearMonth.lengthOfMonth();
        }

        @Override
        public Lunatic.Date day(int dayOfMonth) {
            return new ThreeTenDate(LocalDate.of(year(), month(), dayOfMonth));
        }

        @Override
        public Lunatic.YearMonth plusMonths(int months) {
            return to(yearMonth.plusMonths(months));
        }

        @Override
        public int monthsBetween(Lunatic.YearMonth other) {
            return (int) ChronoUnit.MONTHS.between(yearMonth, from(other));
        }

        @Override
        public int compareTo(Lunatic.YearMonth other) {
            return yearMonth.compareTo(from(other));
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
