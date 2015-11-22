package sexy.tad.lunatic;

/**
 * Date interfaces. Implemented in support packages.
 */
interface Lunatic {

    interface SelectionListener {
        void onDateSelected(Date date);
    }

    interface DateFilter {
        void setEnabledDates(YearMonth month, boolean[] enabledDays);
    }

    interface Date {

        /**
         * @return the year
         */
        int year();

        /**
         * @return the month of the year, 1-indexed
         */
        int month();

        /**
         * @return the day of the month
         */
        int day();

        /**
         * @return weekday, 1-indexed, starting with Monday
         */
        int weekday();

        /**
         * @param days number of days to add
         * @return the resulting date
         */
        Date plusDays(int days);

        /**
         * @return the year and month
         */
        YearMonth yearMonth();

        /**
         * @param other the date between which to count days
         * @return the number of days between this date and {@code other}, exclusive of both dates
         */
        int daysBetween(Date other);

        /**
         * @param other date with which to compare this date
         */
        int compareTo(Date other);
        boolean equals(Date other);
        boolean before(Date other);
        boolean after(Date other);
    }

    interface YearMonth {
        int year();
        int month();
        int days();
        Date day(int dayOfMonth);
        YearMonth plusMonths(int months);
        int monthsBetween(YearMonth other);
        int compareTo(YearMonth other);
        boolean equals(YearMonth other);
        boolean before(YearMonth other);
        boolean after(YearMonth other);
    }

}
