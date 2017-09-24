package lunatic;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.AbsSavedState;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import codes.tad.lunatic.R;
import java.util.ArrayList;
import java.util.Arrays;
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;

public class DatePickerView extends RecyclerView implements SelectionListener {
  Options options;
  Interval interval;
  ArrayList<DateFilter> filters;
  ArrayList<SelectionListener> listeners;
  int monthViewResId;
  MonthAdapter adapter;
  DateFilterInternal filter;
  boolean invalidateAdapter;
  SelectionTree selections;
  Bundle highlights;

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
    selections = new SelectionTree();
    highlights = new Bundle();
  }

  public void setOptions(@NonNull Options options) {
    if (options.equals(this.options)) {
      return;
    }
    this.options = options;
    interval = new Interval(options.min(), options.max());
    invalidateAdapter();
  }

  public boolean addFilter(DateFilter filter) {
    if (filters != null && filters.contains(filter)) {
      return false;
    }
    if (filters == null) {
      filters = new ArrayList<>();
    }
    filters.add(filter);
    if (adapter != null) {
      adapter.notifyDataSetChanged();
    }
    return true;
  }

  public boolean removeFilter(DateFilter filter) {
    if (filters == null) {
      return false;
    }
    boolean removed = filters.remove(filter);
    if (removed && adapter != null) {
      adapter.notifyDataSetChanged();
    }
    return removed;
  }

  public void clearFilters() {
    if (filters == null) {
      return;
    }
    if (filters.isEmpty()) {
      return;
    }
    filters.clear();
    if (adapter != null) {
      adapter.notifyDataSetChanged();
    }
  }

  public boolean addListener(SelectionListener listener) {
    if (listeners != null && listeners.contains(listener)) {
      return false;
    }
    if (listeners == null) {
      listeners = new ArrayList<>();
    }
    listeners.add(listener);
    return true;
  }

  public boolean removeListener(SelectionListener listener) {
    if (listeners == null) {
      return false;
    }
    return listeners.remove(listener);
  }

  public void clearListeners() {
    if (listeners == null) {
      return;
    }
    listeners.clear();
  }

  public void setHighlight(String tag, Highlight highlight) {
    highlights.putParcelable(tag, highlight);
    // TODO replace selections
  }

  public void select(String tag, LocalDate date) {
    select(tag, date, date, null);
  }

  public void select(String tag, LocalDate start, LocalDate end) {
    select(tag, start, end, null);
  }

  public void select(String tag, LocalDate date, @Nullable Highlight highlight) {
    select(tag, date, date, highlight);
  }

  public void select(String tag, LocalDate start, LocalDate end, @Nullable Highlight highlight) {
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("invalid date interval: " + new Interval(start, end));
    }
    if (highlight == null) {
      highlight = highlights.getParcelable(tag);
      if (highlight == null) {
        throw new IllegalArgumentException("no highlight set for tag: " + tag);
      }
    } else {
      highlights.putParcelable(tag, highlight);
    }

    final Selection[] old = selections.findByTag(tag);
    for (Selection s : old) {
      if (s != null) {
        selections.remove(s);
      }
    }

    final Selection selection = new Selection(tag, new Interval(start, end), highlight);
    selections.set(selection, start.toEpochDay(), end.toEpochDay());
  }

  public void clear(String tag) {
    for (Selection selection : selections.values) {
      if (selection.tag.equals(tag)) {
        selections.remove(selection);
      }
    }
  }

  public void clear() {
    selections.clear();
  }

  private void invalidateAdapter() {
    if (invalidateAdapter) {
      return;
    }
    invalidateAdapter = true;
    post(invalidateAdapterRunnable);
  }

  @Override
  public void onDateClicked(LocalDate date) {
    if (listeners != null) {
      for (SelectionListener listener : listeners) {
        listener.onDateClicked(date);
      }
    }
  }

  @Override protected Parcelable onSaveInstanceState() {
    final Parcelable superState = super.onSaveInstanceState();
    final SavedState ss = new SavedState(superState);
    ss.selections = selections;
    ss.highlights = highlights;
    return ss;
  }

  @Override protected void onRestoreInstanceState(Parcelable state) {
    if (state instanceof SavedState) {
      final SavedState ss = (SavedState) state;
      super.onRestoreInstanceState(ss.getSuperState());
      selections = ss.selections;
      highlights = ss.highlights;
      invalidateAdapter();
    } else {
      super.onRestoreInstanceState(state);
    }
  }

  private final Runnable invalidateAdapterRunnable = () -> {
    if (!invalidateAdapter) {
      return;
    }
    if (interval == null || options == null) {
      return;
    }
    invalidateAdapter = false;

    if (adapter != null) {
      selections.removeListener(adapter);
    }

    adapter = new MonthAdapter(getContext(), monthViewResId, interval, options.now(),
        options.weekFields(), options.buildHeaderFormatter(), options.buildWeekdayNames(),
        filter, DatePickerView.this, selections);
    setAdapter(adapter);
    selections.addListener(adapter);
  };

  class DateFilterInternal {
    private final boolean[] enabledDays = new boolean[31];

    boolean[] getEnabledDates(YearMonth month) {
      // By default, days are enabled.
      Arrays.fill(enabledDays, true);

      final int length = month.lengthOfMonth();

      // Pass through to any client filter.
      if (filters != null) {
        for (DateFilter filter : filters) {
          for (int i = 0; i < length; i++) {
            enabledDays[i] = enabledDays[i] && filter.isEnabled(month.atDay(i + 1));
          }
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

  static class SavedState extends AbsSavedState {
    SelectionTree selections;
    Bundle highlights;

    SavedState(Parcelable superState) {
      super(superState);
    }

    SavedState(Parcel source, ClassLoader loader) {
      super(source, loader);
      selections = source.readParcelable(loader);
      highlights = source.readBundle(loader);
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeParcelable(selections, flags);
      dest.writeBundle(highlights);
    }

    public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
      @Override public SavedState createFromParcel(Parcel source, ClassLoader loader) {
        return new SavedState(source, loader);
      }

      @Override public SavedState createFromParcel(Parcel source) {
        return new SavedState(source, null);
      }

      @Override public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };
  }
}
