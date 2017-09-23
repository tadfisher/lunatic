package lunatic;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import org.threeten.bp.LocalDate;

public abstract class Highlight {

  enum Op { ADD, CHANGE, REMOVE, SHOW }

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

  @NonNull protected abstract Drawable createDrawable();

  protected void onAdd(Drawable drawable, BoundedGrid grid) {
    onShow(drawable, grid);
  }

  protected abstract void onShow(Drawable drawable, BoundedGrid grid);

  protected abstract void onChange(Drawable drawable, BoundedGrid grid);

  protected abstract void onRemove(Drawable drawable, BoundedGrid grid);

  void setInterval(LocalDate start, LocalDate end) {
    interval = new Interval(start, end);
    onIntervalChanged(start, end);
  }
}
