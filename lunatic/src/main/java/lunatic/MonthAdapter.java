package lunatic;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.WeekFields;

class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {
  private final int monthViewResId;
  private final String monthViewLayoutName;

  private final Interval interval;
  private final LocalDate now;
  private final WeekFields weekFields;
  private final DateTimeFormatter headerFormatter;
  private final String[] weekdayNames;
  private final DatePickerView.DateFilterInternal filter;
  private final SelectionListener listener;

  MonthAdapter(Context context, int monthViewResId, Interval interval, LocalDate now,
      WeekFields weekFields, DateTimeFormatter headerFormatter, String[] weekdayNames,
      DatePickerView.DateFilterInternal filter, SelectionListener listener) {

    this.monthViewResId = monthViewResId;
    this.interval = interval;
    this.now = now;
    this.weekFields = weekFields;
    this.headerFormatter = headerFormatter;
    this.weekdayNames = weekdayNames;
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
    monthView.setStaticOptions(weekFields, headerFormatter, weekdayNames, listener);

    return new MonthViewHolder(monthView);
  }

  @Override public void onBindViewHolder(MonthViewHolder holder, int position) {
    final YearMonth yearMonth = getMonth(position);
    holder.bindMonth(yearMonth, now, filter.getEnabledDates(yearMonth));
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

  static class MonthViewHolder extends RecyclerView.ViewHolder {
    private final MonthView monthView;

    MonthViewHolder(final MonthView monthView) {
      super(monthView);
      this.monthView = monthView;
    }

    void bindMonth(final YearMonth month, final LocalDate now, final boolean[] enabledDays) {
      monthView.bind(month, now, enabledDays);
    }
  }
}
