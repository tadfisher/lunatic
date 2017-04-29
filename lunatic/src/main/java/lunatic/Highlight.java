package lunatic;

import android.graphics.drawable.Drawable;
import org.threeten.bp.LocalDate;
import java.util.List;

public abstract class Highlight {

  public enum Op { ADD, CHANGE, REMOVE, SHOW }

  Interval interval;

  /**
   * Override to set the relative order in which this highlight is drawn. Highlights with higher
   * priorities are drawn after highlights with lower priorities.
   * <p>
   * The default priority is {@code 0}.
   */
  public int priority() {
    return 0;
  }

  /**
   * Called when the date picker has assigned a new single date to this highlight.
   */
  public void onDateChanged(LocalDate date) {}

  /**
   * Called when the date picker assigns a new date interval to this highlight.
   */
  public void onIntervalChanged(LocalDate start, LocalDate end) {
    if (start.equals(end)) {
      onDateChanged(start);
    }
  }

  /**
   * Provide a Drawable to display for contiguous regions in this highlight's date interval.
   * <p>
   * <em>Note:</em> This drawable should be unique per displayed instance. For example, a
   * highlight whose interval spans one or more month boundaries should return a new Drawable
   * instance each time this method is called.
   */
  protected abstract Drawable bind(List<BoundedRect> regions, Op op);

  void setInterval(LocalDate start, LocalDate end) {
    interval = new Interval(start, end);
    onIntervalChanged(start, end);
  }
}
