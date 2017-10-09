package lunatic;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class DotHighlight extends Highlight {

  private final int color;
  private final int size;
  private final int radius;

  private final Rect bounds = new Rect();
  private final RectEvaluator rectEvaluator = new RectEvaluator();

  public DotHighlight(int color, int size) {
    this.color = color;
    this.size = size;
    radius = size / 2;
  }

  @NonNull @Override protected Drawable createDrawable() {
    final ShapeDrawable d = new ShapeDrawable(new OvalShape());
    d.getPaint().setColor(color);
    return d;
  }

  @Override protected void onAdd(Drawable drawable, BoundedGrid grid) {
    grid.rect(grid.startIndex(), bounds);

    final Rect start = Utils.centerRect(bounds);
    start.top = start.bottom = bounds.bottom - size;

    final Rect end = new Rect(start);
    end.inset(-radius, -radius);

    final AnimatorSet inAnimator = new AnimatorSet().setDuration(225);
    inAnimator.setInterpolator(Utils.linearOutSlowInInterpolator());
    inAnimator.playTogether(
        ObjectAnimator.ofInt(drawable, "alpha", 0, 255),
        ObjectAnimator.ofObject(drawable, "bounds", rectEvaluator, start, end));
    inAnimator.start();
  }

  @Override protected void onShow(Drawable drawable, BoundedGrid grid) {
    grid.rect(grid.startIndex(), bounds);
    bounds.left = bounds.centerX() - radius;
    bounds.right = bounds.left + size;
    bounds.top = bounds.bottom - size;
    drawable.setBounds(bounds);
    drawable.setAlpha(255);
  }

  @Override protected void onChange(Drawable drawable, BoundedGrid grid) {
    // TODO support change animation?
  }

  @Override protected void onRemove(Drawable drawable, BoundedGrid grid) {
    grid.rect(grid.startIndex(), bounds);

    final Rect end = Utils.centerRect(bounds);
    end.top = end.bottom = bounds.bottom - size;

    final AnimatorSet outAnimator = new AnimatorSet().setDuration(225);
    outAnimator.setInterpolator(Utils.fastOutLinearInInterpolator());
    outAnimator.playTogether(
        ObjectAnimator.ofInt(drawable, "alpha", 255, 0),
        ObjectAnimator.ofObject(drawable, "bounds", rectEvaluator, drawable.getBounds(), end));
    outAnimator.start();
  }

  @Override public void writeToParcel(Parcel out, int flags) {
    out.writeInt(color);
    out.writeInt(size);
  }

  public static final Parcelable.Creator<DotHighlight> CREATOR =
      new Parcelable.Creator<DotHighlight>() {
        @Override public DotHighlight createFromParcel(Parcel in) {
          return new DotHighlight(in.readInt(), in.readInt());
        }

        @Override public DotHighlight[] newArray(int size) {
          return new DotHighlight[size];
        }
      };
}
