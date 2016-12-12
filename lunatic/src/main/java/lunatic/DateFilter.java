package lunatic;

import org.threeten.bp.LocalDate;

public interface DateFilter {
  boolean isEnabled(LocalDate date);
}
