package sexy.tad.lunatic;

/**
 * Interval between two abstract dates.
 */
class Interval {
    public final Lunatic.Date start;
    public final Lunatic.Date end;
    public final Lunatic.YearMonth startMonth;
    public final Lunatic.YearMonth endMonth;

    public Interval(Lunatic.Date start, Lunatic.Date end) {
        this.start = start;
        this.end = end;
        this.startMonth = start.yearMonth();
        this.endMonth = end.yearMonth();
    }

    public int days() {
        return start.daysBetween(end) + 1;
    }

    public int months() {
        return startMonth.monthsBetween(endMonth) + 1;
    }

    boolean contains(Lunatic.Date date) {
        return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
    }
}
