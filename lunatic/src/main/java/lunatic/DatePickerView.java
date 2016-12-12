package lunatic;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import codes.tad.lunatic.R;
import java.util.Arrays;
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;

public class DatePickerView extends RecyclerView {
  private Options options;
  private Interval interval;
  private DateFilterInternal filter;
  private DateFilter filterDelegate;
  private SelectionListener listenerDelegate;
  private int monthViewResId;

  private boolean invalidateAdapter;

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
  }

  public void setOptions(Options options) {
    if (options == this.options) {
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

      setAdapter(new MonthAdapter(getContext(), monthViewResId, interval, options.now(),
          options.weekFields(), options.buildHeaderFormatter(), options.buildWeekdayNames(),
          filter, listener));
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

  private final SelectionListener listener = new SelectionListener() {
    @Override public void onDateSelected(LocalDate date) {
      if (listenerDelegate != null) {
        listenerDelegate.onDateSelected(date);
      }
    }
  };
}
