package lunatic;

import org.threeten.bp.YearMonth;

/**
 * Created by tad on 3/27/16.
 */
public interface DateFilter {
  void setEnabledDates(YearMonth month, boolean[] enabledDays);
}
