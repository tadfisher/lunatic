package lunatic;

import android.os.Parcel;
import android.os.Parcelable;
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;
import org.threeten.bp.temporal.ChronoUnit;

import static org.threeten.bp.format.DateTimeFormatter.ISO_LOCAL_DATE;

/**
 * Interval between two abstract dates.
 */
class Interval implements Parcelable {
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

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel out, int flags) {
    out.writeLong(start.toEpochDay());
    out.writeLong(end.toEpochDay());
  }

  public static final Creator<Interval> CREATOR = new Creator<Interval>() {
    @Override public Interval createFromParcel(Parcel in) {
      return new Interval(LocalDate.ofEpochDay(in.readLong()), LocalDate.ofEpochDay(in.readLong()));
    }

    @Override public Interval[] newArray(int size) {
      return new Interval[size];
    }
  };
}
