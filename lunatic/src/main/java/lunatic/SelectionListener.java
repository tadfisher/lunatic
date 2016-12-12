package lunatic;

import org.threeten.bp.LocalDate;

public interface SelectionListener {
  void onDateSelected(LocalDate date);
}
