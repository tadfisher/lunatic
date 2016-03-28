package sexy.tad.lunatic;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import java.util.Locale;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.YearMonth;

/**
 *
 */
class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {
  private final int monthViewResId;
  private final String monthViewLayoutName;

  private final Interval interval;
  private final DayOfWeek firstDayOfWeek;
  private final Locale locale;
  private final String headerFormat;
  private final String[] weekdayLabels;
  private final DateFilter filter;
  private final SelectionListener listener;

  MonthAdapter(Context context, int monthViewResId, Interval interval, DayOfWeek firstDayOfWeek,
      Locale locale, String headerFormat, String[] weekdayLabels, DateFilter filter,
      SelectionListener listener) {

    this.monthViewResId = monthViewResId;
    this.interval = interval;
    this.firstDayOfWeek = firstDayOfWeek;
    this.locale = locale;
    this.headerFormat = headerFormat;
    this.weekdayLabels = weekdayLabels;
    this.filter = filter;
    this.listener = listener;

    monthViewLayoutName = context.getResources().getResourceName(this.monthViewResId);
  }

  @Override public MonthViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = View.inflate(parent.getContext(), monthViewResId, null);
    if (!(view instanceof MonthView)) {
      throw new IllegalStateException(
          String.format("Layout '%s' must contain a MonthView or a subclass!",
              monthViewLayoutName));
    }

    MonthView monthView = (MonthView) view;
    monthView.setStaticOptions(firstDayOfWeek, locale, headerFormat, weekdayLabels, listener);

    return new MonthViewHolder((MonthView) view);
  }

  @Override public void onBindViewHolder(MonthViewHolder holder, int position) {
    holder.bindMonth(getMonth(position));
  }

  @Override
  public void onBindViewHolder(MonthViewHolder holder, int position, List<Object> payloads) {
    // TODO bind highlight changes via payload
    super.onBindViewHolder(holder, position, payloads);
  }

  @Override public int getItemCount() {
    return interval.months();
  }

  private YearMonth getMonth(int position) {
    return interval.startMonth.plusMonths(position);
  }

  class MonthViewHolder extends RecyclerView.ViewHolder {
    private MonthView mMonthView;

    public MonthViewHolder(MonthView view) {
      super(view);
      mMonthView = view;
    }

    void bindMonth(YearMonth month) {
      boolean[] enabledDays = new boolean[month.lengthOfMonth()];
      filter.setEnabledDates(month, enabledDays);
      mMonthView.bind(month, enabledDays);
    }
  }
}
