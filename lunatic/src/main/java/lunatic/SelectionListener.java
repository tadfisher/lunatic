package lunatic;

import org.threeten.bp.LocalDate;

public interface SelectionListener {
  void onDateClicked(LocalDate date);
}
