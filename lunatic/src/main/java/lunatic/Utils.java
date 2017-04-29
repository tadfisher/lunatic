package lunatic;

import android.os.Build;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.jdk8.Jdk8Methods;
import org.threeten.bp.temporal.WeekFields;

/**
 * Assorted utilities.
 */
class Utils {

  private static Interpolator fastOutLinearIn;
  private static Interpolator linearOutSlowIn;

  /**
   * Returns an offset to align week start with a day of month.
   *
   * @param dow the day of the week; 1 through 7
   * @return an offset in days to align a day with the start of the first 'full' week
   */
  static int startOfWeekOffset(WeekFields weekDef, DayOfWeek dow) {
    // offset of first day corresponding to the day of week in first 7 days (zero origin)
    return Jdk8Methods.floorMod(dow.getValue() - weekDef.getFirstDayOfWeek().getValue(), 7);
  }

  static Interpolator fastOutLinearInInterpolator() {
    if (fastOutLinearIn != null) {
      return fastOutLinearIn;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      fastOutLinearIn = new PathInterpolator(0.4f, 0, 1, 1);
    } else {
      final Class<?> interpolatorClass;
      ClassLoader classLoader = Utils.class.getClassLoader();
      try {
        interpolatorClass =
            classLoader.loadClass("android.support.v4.view.animation.FastOutLinearInInterpolator");
        fastOutLinearIn = (Interpolator) interpolatorClass.newInstance();
      } catch (Exception e) {
        fastOutLinearIn = new DecelerateInterpolator();
      }
    }

    return fastOutLinearIn;
  }

  static Interpolator linearOutSlowInInterpolator() {
    if (linearOutSlowIn != null) {
      return linearOutSlowIn;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      linearOutSlowIn = new PathInterpolator(0, 0, 0.2f, 1);
    } else {
      final Class<?> interpolatorClass;
      ClassLoader classLoader = Utils.class.getClassLoader();
      try {
        interpolatorClass =
            classLoader.loadClass("android.support.v4.view.animation.LinearOutSlowInInterpolator");
        linearOutSlowIn = (Interpolator) interpolatorClass.newInstance();
      } catch (Exception e) {
        linearOutSlowIn = new AccelerateInterpolator();
      }
    }

    return linearOutSlowIn;
  }

  private Utils() {
    throw new UnsupportedOperationException("No instances.");
  }
}
