package lunatic;

import static org.threeten.bp.format.DateTimeFormatter.ISO_LOCAL_DATE;

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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof Interval)) {
      return false;
    }

    Interval that = (Interval) obj;
    return this.start.equals(that.start)
        && this.end.equals(that.end);
  }

  @Override
  public String toString() {
    return "(" + start.format(ISO_LOCAL_DATE) + ", " + end.format(ISO_LOCAL_DATE) + ")";
  }
}
