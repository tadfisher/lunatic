package sexy.tad.lunatic;

/**
 * Represents a selection of one date, or a range bounded by two dates.
 */
class Selection extends Interval {
    public Selection(Lunatic.Date date) {
        super(date, date);
    }

    public Selection(Lunatic.Date start, Lunatic.Date end) {
        super(start, end);
    }

    boolean isInterval() {
        return days() > 1;
    }
}
