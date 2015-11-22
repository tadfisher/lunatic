package sexy.tad.lunatic;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.Arrays;

public class DatePickerView extends RecyclerView {
    private Options option;
    private Interval interval;
    private Lunatic.DateFilter filterDelegate;
    private Lunatic.SelectionListener listenerDelegate;
    private int monthViewResId;

    private boolean invalidateAdapter;

    public DatePickerView(Context context) {
        super(context);
        init(context, null, R.attr.lunatic_datePickerStyle);
    }

    public DatePickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.lunatic_datePickerStyle);
    }

    public DatePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.lunatic_DatePickerView,
                defStyleAttr,
                R.style.lunatic_DatePickerView);
        monthViewResId =
                a.getResourceId(R.styleable.lunatic_DatePickerView_lunatic_monthView, 0);
        a.recycle();

        setLayoutManager(new LinearLayoutManager(context));
        setItemAnimator(null);
    }

    void setOptions(Options options) {
        if (options == option) {
            return;
        }
        option = options;
        interval = new Interval(options.min, options.max);
        invalidateAdapter();
    }

    void setFilter(Lunatic.DateFilter filter) {
        if (filter == filterDelegate) {
            return;
        }
        filterDelegate = filter;
        invalidateAdapter();
    }

    void setListener(Lunatic.SelectionListener listener) {
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
        @Override
        public void run() {
            if (!invalidateAdapter) {
                return;
            }
            if (interval == null || option == null) {
                return;
            }
            invalidateAdapter = false;
            setAdapter(new MonthAdapter(
                    getContext(),
                    monthViewResId,
                    interval,
                    option.firstDayOfWeek,
                    option.locale,
                    option.headerFormat,
                    option.weekdayLabels,
                    filter,
                    listener));
        }
    };

    private final Lunatic.DateFilter filter = new Lunatic.DateFilter() {
        @Override
        public void setEnabledDates(Lunatic.YearMonth month, boolean[] enabledDays) {
            // By default, days are enabled.
            Arrays.fill(enabledDays, true);

            // Pass through to any client filter.
            if (filterDelegate != null) {
                filterDelegate.setEnabledDates(month, enabledDays);
            }

            // In all cases, disable dates outside of our view interval.
            if (month.equals(interval.startMonth)) {
                int start = interval.start.day() - 1;
                for (int i = 0; i < start; i++) {
                    enabledDays[i] = false;
                }
            }
            if (month.equals(interval.endMonth)) {
                int end = interval.end.day();
                for (int i = end; i < enabledDays.length; i++) {
                    enabledDays[i] = false;
                }
            }
        }
    };

    private final Lunatic.SelectionListener listener = new Lunatic.SelectionListener() {
        @Override
        public void onDateSelected(Lunatic.Date date) {
            if (listenerDelegate != null) {
                listenerDelegate.onDateSelected(date);
            }
        }
    };
}
