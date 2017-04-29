package lunatic;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;
import java.util.Arrays;
import codes.tad.lunatic.R;

public class DatePickerView extends RecyclerView {
  Options options;
  Interval interval;
  DateFilter filterDelegate;
  SelectionListener listenerDelegate;
  int monthViewResId;
  MonthAdapter adapter;
  DateFilterInternal filter;
  boolean invalidateAdapter;
  SparseIntervalTree<Highlight> highlights;

  public DatePickerView(Context context) {
    this(context, null);
  }

  public DatePickerView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.lunatic_datePickerStyle);
  }

  public DatePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    TypedArray a = context.getTheme().obtainStyledAttributes(
        attrs, R.styleable.lunatic_DatePickerView, defStyleAttr, R.style.lunatic_DatePickerView);

    monthViewResId = a.getResourceId(R.styleable.lunatic_DatePickerView_lunatic_monthView,
        R.layout.lunatic_month_view);

    a.recycle();

    setLayoutManager(new LinearLayoutManager(context));
    setItemAnimator(null);

    filter = new DateFilterInternal();
    highlights = SparseIntervalTree.create(Highlight.class);
  }

  public void setOptions(@NonNull Options options) {
    if (options.equals(this.options)) {
      return;
    }
    this.options = options;
    interval = new Interval(options.min(), options.max());
    invalidateAdapter();
  }

  public void setFilter(DateFilter filter) {
    if (filter == filterDelegate) {
      return;
    }
    filterDelegate = filter;
    invalidateAdapter();
  }

  public void setListener(SelectionListener listener) {
    if (listener == listenerDelegate) {
      return;
    }
    listenerDelegate = listener;
    invalidateAdapter();
  }

  public void setHighlight(Highlight highlight, LocalDate date) {
    setHighlight(highlight, date, date);
  }

  public void setHighlight(Highlight highlight, LocalDate start, LocalDate end) {
    if (!interval.contains(start) || !interval.contains(end)) {
      throw new IllegalArgumentException("highlight is outside of date range " + interval);
    }

    if (start.isAfter(end)) {
      throw new IllegalArgumentException("invalid date interval: " + new Interval(start, end));
    }

    highlight.setInterval(start, end);
    highlights.set(highlight, start.toEpochDay(), end.toEpochDay());
  }

  public void removeHighlight(Highlight highlight) {
    highlights.remove(highlight);
  }

  public void clearHighlights() {
    highlights.clear();
  }

  private void invalidateAdapter() {
    if (invalidateAdapter) {
      return;
    }
    invalidateAdapter = true;
    post(invalidateAdapterRunnable);
  }

  private final Runnable invalidateAdapterRunnable = new Runnable() {
    @Override public void run() {
      if (!invalidateAdapter) {
        return;
      }
      if (interval == null || options == null) {
        return;
      }
      invalidateAdapter = false;

      if (adapter != null) {
        highlights.removeListener(adapter);
      }

      adapter = new MonthAdapter(getContext(), monthViewResId, interval, options.now(),
          options.weekFields(), options.buildHeaderFormatter(), options.buildWeekdayNames(),
          filter, listener, highlights);
      setAdapter(adapter);
      highlights.addListener(adapter);
    }
  };

  class DateFilterInternal {
    private final boolean[] enabledDays = new boolean[31];

    boolean[] getEnabledDates(YearMonth month) {
      // By default, days are enabled.
      Arrays.fill(enabledDays, true);

      final int length = month.lengthOfMonth();

      // Pass through to any client filter.
      if (filterDelegate != null) {
        for (int i = 0; i < length; i++) {
          enabledDays[i] = filterDelegate.isEnabled(month.atDay(i + 1));
        }
      }

      // In all cases, disable dates outside of our view interval.
      if (month.equals(interval.startMonth)) {
        int start = interval.start.getDayOfMonth() - 1;
        for (int i = 0; i < start; i++) {
          enabledDays[i] = false;
        }
      }
      if (month.equals(interval.endMonth)) {
        int end = interval.end.getDayOfMonth();
        for (int i = end; i < enabledDays.length; i++) {
          enabledDays[i] = false;
        }
      }
      return Arrays.copyOf(enabledDays, length);
    }
  }

  final SelectionListener listener = new SelectionListener() {
    @Override public void onDateClicked(LocalDate date) {
      if (listenerDelegate != null) {
        listenerDelegate.onDateClicked(date);
      }
    }
  };
}
