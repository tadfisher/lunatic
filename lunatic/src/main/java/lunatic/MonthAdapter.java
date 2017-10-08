package lunatic;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.WeekFields;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static lunatic.Selection.Op.ADD;
import static lunatic.Selection.Op.REMOVE;
import static org.threeten.bp.temporal.ChronoUnit.MONTHS;

class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder>
    implements SparseIntervalTree.Listener<Selection> {
  private final int monthViewResId;
  private final String monthViewLayoutName;

  private final Interval interval;
  private final LocalDate now;
  private final WeekFields weekFields;
  private final DateTimeFormatter headerFormatter;
  private final String[] weekdayNames;
  private final DatePickerView.DateFilterInternal filter;
  private final SelectionListener listener;
  private final SparseIntervalTree<Selection> selections;

  MonthAdapter(Context context, int monthViewResId, Interval interval, LocalDate now,
      WeekFields weekFields, DateTimeFormatter headerFormatter, String[] weekdayNames,
      DatePickerView.DateFilterInternal filter, SelectionListener listener,
      SelectionTree selections) {

    this.monthViewResId = monthViewResId;
    this.interval = interval;
    this.now = now;
    this.weekFields = weekFields;
    this.headerFormatter = headerFormatter;
    this.weekdayNames = weekdayNames;
    this.filter = filter;
    this.listener = listener;
    this.selections = selections;

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
    Selection[] sel =
        selections.find(yearMonth.atDay(1).toEpochDay(), yearMonth.atEndOfMonth().toEpochDay());
    holder.bindMonth(yearMonth, now, filter.getEnabledDates(yearMonth), sel);
  }

  @Override
  public void onBindViewHolder(MonthViewHolder holder, int position, List<Object> payloads) {
    if (payloads == null || payloads.isEmpty()) {
      onBindViewHolder(holder, position);
      return;
    }

    //noinspection unchecked
    List<SelectionOp> ops = (List<SelectionOp>)(List<?>) payloads;
    Set<Selection> seen = new LinkedHashSet<>();
    for (int i = ops.size() - 1; i >= 0; i--) {
      final SelectionOp op = ops.get(i);
      if (!seen.contains(op.selection)) {
        seen.add(op.selection);
        holder.bindHighlight(op);
      }
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
  public void onAdded(Selection selection, long start, long end) {
    notifySelection(new SelectionOp(selection, ADD),
        selection.interval.startMonth, selection.interval.endMonth);
  }

  @Override
  public void onChanged(Selection selection, long oldStart, long oldEnd, long newStart,
      long newEnd) {
    // TODO support CHANGE op
    notifySelection(new SelectionOp(selection, REMOVE),
        YearMonth.from(LocalDate.ofEpochDay(oldStart)),
        YearMonth.from(LocalDate.ofEpochDay(oldEnd)));
    notifySelection(new SelectionOp(selection, ADD),
        selection.interval.startMonth, selection.interval.endMonth);
  }

  @Override
  public void onRemoved(Selection selection, long start, long end) {
    notifySelection(new SelectionOp(selection, REMOVE),
        selection.interval.startMonth, selection.interval.endMonth);
  }

  private void notifySelection(SelectionOp op, YearMonth startMonth, YearMonth endMonth) {
    int startPosition = getPosition(startMonth);
    int length = (int) MONTHS.between(startMonth, endMonth) + 1;
    notifyItemRangeChanged(startPosition, length, op);
  }

  static class SelectionOp {
    final Selection selection;
    final Selection.Op op;

    SelectionOp(Selection selection, Selection.Op op) {
      this.selection = selection;
      this.op = op;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SelectionOp that = (SelectionOp) o;

      if (!selection.equals(that.selection)) return false;
      return op == that.op;
    }

    @Override public int hashCode() {
      int result = selection.hashCode();
      result = 31 * result + op.hashCode();
      return result;
    }
  }

  static class MonthViewHolder extends RecyclerView.ViewHolder {
    private final MonthView monthView;

    MonthViewHolder(final MonthView monthView) {
      super(monthView);
      this.monthView = monthView;
    }

    void bindMonth(final YearMonth month, final LocalDate now, final boolean[] enabledDays,
        final Selection[] selections) {
      monthView.bind(month, now, enabledDays);
      for (Selection s : selections) {
        monthView.addSelection(s, false);
      }
    }

    void bindHighlight(SelectionOp op) {
      switch (op.op) {
        case ADD:
          monthView.addSelection(op.selection);
          break;
        case REMOVE:
          monthView.removeSelection(op.selection);
          break;
      }
    }
  }
}
