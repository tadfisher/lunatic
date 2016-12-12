package lunatic;

import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;
import org.threeten.bp.temporal.ChronoUnit;

/**
 * Interval between two abstract dates.
 */
class Interval {
  final LocalDate start;
  final LocalDate end;
  final YearMonth startMonth;
  final YearMonth endMonth;

  Interval(LocalDate start, LocalDate end) {
    this.start = start;
    this.end = end;

    startMonth = YearMonth.from(start);
    endMonth = YearMonth.from(end);
  }

  public int days() {
    return (int) ChronoUnit.DAYS.between(start, end);
  }

  int months() {
    return (int) ChronoUnit.MONTHS.between(start, end) + 1;
  }

  boolean contains(LocalDate date) {
    return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
  }
}
