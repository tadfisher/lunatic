package lunatic;

import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;
import org.threeten.bp.temporal.ChronoUnit;

/**
 * Interval between two abstract dates.
 */
class Interval {
  public final LocalDate start;
  public final LocalDate end;
  public final YearMonth startMonth;
  public final YearMonth endMonth;

  public Interval(LocalDate start, LocalDate end) {
    this.start = start;
    this.end = end;

    startMonth = YearMonth.from(start);
    endMonth = YearMonth.from(end);
  }

  public int days() {
    return (int) ChronoUnit.DAYS.between(start, end);
  }

  public int months() {
    return (int) ChronoUnit.MONTHS.between(start, end) + 1;
  }

  boolean contains(LocalDate date) {
    return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
  }
}
