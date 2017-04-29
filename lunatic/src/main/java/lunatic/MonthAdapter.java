package lunatic;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static lunatic.Highlight.Op.ADD;
import static lunatic.Highlight.Op.REMOVE;
import static org.threeten.bp.temporal.ChronoUnit.MONTHS;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.WeekFields;
import java.util.List;

class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder>
    implements SparseIntervalTree.Listener<Highlight> {
  private final int monthViewResId;
  private final String monthViewLayoutName;

  private final Interval interval;
  private final LocalDate now;
  private final WeekFields weekFields;
  private final DateTimeFormatter headerFormatter;
  private final String[] weekdayNames;
  private final DatePickerView.DateFilterInternal filter;
  private final SelectionListener listener;
  private final SparseIntervalTree<Highlight> highlights;

  MonthAdapter(Context context, int monthViewResId, Interval interval, LocalDate now,
      WeekFields weekFields, DateTimeFormatter headerFormatter, String[] weekdayNames,
      DatePickerView.DateFilterInternal filter, SelectionListener listener,
      SparseIntervalTree<Highlight> highlights) {

    this.monthViewResId = monthViewResId;
    this.interval = interval;
    this.now = now;
    this.weekFields = weekFields;
    this.headerFormatter = headerFormatter;
    this.weekdayNames = weekdayNames;
    this.filter = filter;
    this.listener = listener;
    this.highlights = highlights;

    monthViewLayoutName = context.getResources().getResourceName(this.monthViewResId);

    setHasStableIds(true);
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
    Highlight[] h =
        highlights.find(yearMonth.atDay(1).toEpochDay(), yearMonth.atEndOfMonth().toEpochDay());
    holder.bindMonth(yearMonth, now, filter.getEnabledDates(yearMonth), h);
  }

  @Override
  public void onBindViewHolder(MonthViewHolder holder, int position, List<Object> payloads) {
    if (payloads == null || payloads.isEmpty()) {
      onBindViewHolder(holder, position);
      return;
    }

    //noinspection unchecked
    List<HighlightOp> ops = (List<HighlightOp>)(List<?>) payloads;
    for (HighlightOp op : ops) {
      holder.bindHighlight(op);
    }
  }

  @Override public int getItemCount() {
    return interval.months();
  }

  @Override public long getItemId(int position) {
    return getItemId(getMonth(position));
  }

  private long getItemId(YearMonth yearMonth) {
    return yearMonth.atDay(1).toEpochDay();
  }

  private YearMonth getMonth(int position) {
    return interval.startMonth.plusMonths(position);
  }

  private int getPosition(YearMonth yearMonth) {
    long pos = MONTHS.between(interval.startMonth, yearMonth);
    if (pos < 0 || pos >= getItemCount()) {
      return NO_POSITION;
    }
    return (int) pos;
  }

  @Override
  public void onAdded(Highlight value, long start, long end) {
    notifyHighlight(new HighlightOp(value, ADD),
        value.interval.startMonth, value.interval.endMonth);
  }

  @Override
  public void onChanged(Highlight value, long oldStart, long oldEnd, long newStart, long newEnd) {
    // TODO support CHANGE op
    notifyHighlight(new HighlightOp(value, REMOVE),
        YearMonth.from(LocalDate.ofEpochDay(oldStart)),
        YearMonth.from(LocalDate.ofEpochDay(oldEnd)));
    notifyHighlight(new HighlightOp(value, ADD),
        value.interval.startMonth, value.interval.endMonth);
  }

  @Override
  public void onRemoved(Highlight value, long start, long end) {
    notifyHighlight(new HighlightOp(value, REMOVE),
        value.interval.startMonth, value.interval.endMonth);
  }

  private void notifyHighlight(HighlightOp op, YearMonth startMonth, YearMonth endMonth) {
    int startPosition = getPosition(startMonth);
    int length = (int) MONTHS.between(startMonth, endMonth) + 1;
    notifyItemRangeChanged(startPosition, length, op);
  }

  static class HighlightOp {

    final Highlight highlight;
    final Highlight.Op op;

    HighlightOp(Highlight highlight, Highlight.Op op) {
      this.highlight = highlight;
      this.op = op;
    }
  }

  static class MonthViewHolder extends RecyclerView.ViewHolder {
    private final MonthView monthView;

    MonthViewHolder(final MonthView monthView) {
      super(monthView);
      this.monthView = monthView;
    }

    void bindMonth(final YearMonth month, final LocalDate now, final boolean[] enabledDays, final
     Highlight[] highlights) {
      monthView.bind(month, now, enabledDays, highlights);
    }

    void bindHighlight(HighlightOp op) {
      switch (op.op) {
        case ADD:
          monthView.addHighlight(op.highlight);
          break;
        case REMOVE:
          monthView.removeHighlight(op.highlight);
          break;
      }
    }
  }
}
