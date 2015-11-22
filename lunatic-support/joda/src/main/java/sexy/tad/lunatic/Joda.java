package sexy.tad.lunatic;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.YearMonth;

/**
 * Joda-time implementation of the Lunatic API.
 */
public class Joda {

    public static JodaOptionsBuilder newOptionsBuilder(Options options) {
        return new JodaOptionsBuilder(options);
    }

    public static JodaOptionsBuilder newOptionsBuilder() {
        return newOptionsBuilder(null);
    }

    public static Lunatic.DateFilter newDateFilter(DateFilter filter) {
        return new JodaDateFilterAdapter(filter);
    }

    public static Lunatic.SelectionListener newSelectionListener(SelectionListener listener) {
        return new JodaSelectionListenerAdapter(listener);
    }

    public static abstract class DateFilter {
        public abstract void setEnabledDates(YearMonth month, boolean[] enabledDays);
    }

    public static abstract class SelectionListener {
        public abstract void onDateSelected(LocalDate date);
    }

    public static class JodaOptionsBuilder extends OptionsBuilder<LocalDate> {
        public JodaOptionsBuilder(Options options) {
            super(options);
        }

        @Override
        public JodaOptionsBuilder setMin(LocalDate min) {
            setMin(JodaDate.to(min));
            return this;
        }

        @Override
        public JodaOptionsBuilder setMax(LocalDate max) {
            setMax(JodaDate.to(max));
            return this;
        }
    }

    static class JodaDateFilterAdapter implements Lunatic.DateFilter {
        private final DateFilter filter;

        public JodaDateFilterAdapter(DateFilter filter) {
            this.filter = filter;
        }

        @Override
        public void setEnabledDates(Lunatic.YearMonth month, boolean[] enabledDays) {
            filter.setEnabledDates(JodaYearMonth.from(month), enabledDays);
        }
    }

    static class JodaSelectionListenerAdapter implements Lunatic.SelectionListener {
        private final SelectionListener listener;

        JodaSelectionListenerAdapter(SelectionListener selectionListener) {
            this.listener = selectionListener;
        }

        @Override
        public void onDateSelected(Lunatic.Date date) {
            listener.onDateSelected(JodaDate.from(date));
        }
    }

    /**
     * Joda LocalDate wrapper.
     */
    static class JodaDate implements Lunatic.Date {
        private final LocalDate localDate;

        static LocalDate from(Lunatic.Date date) {
            return new LocalDate(date.year(), date.month(), date.day());
        }

        static Lunatic.Date to(LocalDate localDate) {
            return new JodaDate(localDate);
        }

        public JodaDate(LocalDate localDate) {
            this.localDate = localDate;
        }

        @Override
        public int year() {
            return localDate.getYear();
        }

        @Override
        public int month() {
            return localDate.getMonthOfYear();
        }

        @Override
        public int day() {
            return localDate.getDayOfMonth();
        }

        @Override
        public int weekday() {
            return localDate.getDayOfWeek();
        }

        @Override
        public Lunatic.YearMonth yearMonth() {
            return new JodaYearMonth(
                    new YearMonth(localDate.getYear(), localDate.getMonthOfYear()));
        }

        @Override
        public Lunatic.Date plusDays(int days) {
            return new JodaDate(localDate.plusDays(days));
        }

        @Override
        public int daysBetween(Lunatic.Date other) {
            return new Period(localDate, from(other), PeriodType.days()).getDays();
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

    /**
     * Joda YearMonth wrapper.
     */
    static class JodaYearMonth implements Lunatic.YearMonth {
        private final YearMonth yearMonth;

        private static YearMonth from(Lunatic.YearMonth yearMonth) {
            return new YearMonth(yearMonth.year(), yearMonth.month());
        }

        public JodaYearMonth(YearMonth yearMonth) {
            this.yearMonth = yearMonth;
        }

        @Override
        public int year() {
            return yearMonth.getYear();
        }

        @Override
        public int month() {
            return yearMonth.getMonthOfYear();
        }

        @Override
        public int days() {
            return yearMonth.toLocalDate(1).dayOfMonth().getMaximumValue();
        }

        @Override
        public Lunatic.Date day(int dayOfMonth) {
            return new JodaDate(new LocalDate(year(), month(), dayOfMonth));
        }

        @Override
        public Lunatic.YearMonth plusMonths(int months) {
            return new JodaYearMonth(yearMonth.plusMonths(months));
        }

        @Override
        public int monthsBetween(Lunatic.YearMonth other) {
            return new Period(yearMonth, from(other), PeriodType.months()).getMonths();
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
