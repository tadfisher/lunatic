package sexy.tad.lunatic;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.jdk8.Jdk8Methods;
import org.threeten.bp.temporal.WeekFields;

/**
 * Assorted utilities.
 */
class Utils {

  /**
   * Returns an offset to align week start with a day of month.
   *
   * @param dow the day of the week; 1 through 7
   * @return an offset in days to align a day with the start of the first 'full' week
   */
  public static int startOfWeekOffset(WeekFields weekDef, DayOfWeek dow) {
    // offset of first day corresponding to the day of week in first 7 days (zero origin)
    return Jdk8Methods.floorMod(dow.getValue() - weekDef.getFirstDayOfWeek().getValue(), 7);
  }

  private Utils() {
    throw new UnsupportedOperationException("No instances!");
  }
}
