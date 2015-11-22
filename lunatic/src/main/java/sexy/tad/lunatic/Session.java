package sexy.tad.lunatic;

/**
 * TODO docs
 */
public class Session {
    final DatePickerView datePickerView;

    public static Session with(DatePickerView datePickerView) {
        Preconditions.checkNotNull(datePickerView);
        return new Session(datePickerView);
    }

    Session(DatePickerView datePickerView) {
        this.datePickerView  = datePickerView;
    }

    public Session setOptions(Options options) {
        datePickerView.setOptions(options);
        return this;
    }

    public Session setDateFilter(Lunatic.DateFilter filter) {
        datePickerView.setFilter(filter);
        return this;
    }

    public Session setSelectionListener(Lunatic.SelectionListener listener) {
        datePickerView.setListener(listener);
        return this;
    }
}
