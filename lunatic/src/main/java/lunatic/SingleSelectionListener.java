package lunatic;

import org.threeten.bp.LocalDate;

/**
 * Selection listener which highlights and returns a single date.
 */
public class SingleSelectionListener implements SelectionListener {
  private final DatePickerView datePickerView;
  private final Highlight highlight;

  private LocalDate selection;

  public SingleSelectionListener(DatePickerView datePickerView, Highlight highlight) {
    this.datePickerView = datePickerView;
    this.highlight = highlight;
  }

  @Override
  public void onDateClicked(LocalDate date) {
    if (date.equals(selection)) {
      return;
    }
    selection = date;
    datePickerView.setHighlight(highlight, date);
  }

  public LocalDate getSelection() {
    return selection;
  }
}
