package lunatic;

import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public abstract class Highlight implements Parcelable {

  /**
   * Override to set the relative order in which this highlight is drawn. Highlights with higher
   * priorities are drawn after highlights with lower priorities.
   * <p>
   * The default priority is {@code 0}.
   */
  public int priority() {
    return 0;
  }

  @NonNull protected abstract Drawable createDrawable();

  protected void onAdd(Drawable drawable, BoundedGrid grid) {
    onShow(drawable, grid);
  }

  protected abstract void onShow(Drawable drawable, BoundedGrid grid);

  protected abstract void onChange(Drawable drawable, BoundedGrid grid);

  protected abstract void onRemove(Drawable drawable, BoundedGrid grid);

  @Override public int describeContents() {
    return 0;
  }
}
