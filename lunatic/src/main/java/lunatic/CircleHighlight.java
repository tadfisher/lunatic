package lunatic;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class CircleHighlight extends Highlight {

  private final int color;

  private final Rect bounds = new Rect();
  private final Xfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);
  private final RectEvaluator rectEvaluator = new RectEvaluator();

  private AnimatorSet inAnimator;
  private ObjectAnimator inAlpha;
  private ObjectAnimator inScale;

  private AnimatorSet outAnimator;
  private ObjectAnimator outAlpha;
  private ObjectAnimator outScale;

  public CircleHighlight(int color) {
    this.color = color;
  }

  @NonNull @Override protected Drawable createDrawable() {
    final ShapeDrawable d = new ShapeDrawable(new OvalShape());
    d.getPaint().setColor(color);
    d.getPaint().setXfermode(xfermode);
    return d;
  }

  @Override protected void onAdd(Drawable drawable, BoundedGrid grid) {
    grid.rect(grid.startIndex(), bounds);

    if (inAnimator == null) {
      inAlpha = new ObjectAnimator();
      inAlpha.setPropertyName("alpha");
      inAlpha.setIntValues(0, 255);

      inScale = new ObjectAnimator();
      inScale.setPropertyName("bounds");

      inAnimator = new AnimatorSet();
      inAnimator.setDuration(225);
      inAnimator.setInterpolator(Utils.linearOutSlowInInterpolator());
    }

    inAlpha.setTarget(drawable);
    inScale.setTarget(drawable);
    inScale.setObjectValues(Utils.centerRect(bounds), Utils.squareRect(bounds));
    inScale.setEvaluator(rectEvaluator);
    inAnimator.playTogether(inAlpha, inScale);
    inAnimator.start();
  }

  @Override protected void onShow(Drawable drawable, BoundedGrid grid) {
    drawable.setBounds(Utils.squareRect(grid.rect(grid.startIndex())));
    drawable.setAlpha(255);
  }

  @Override protected void onChange(Drawable drawable, BoundedGrid grid) {
    // TODO support change animation?
  }

  @Override protected void onRemove(Drawable drawable, BoundedGrid grid) {
    grid.rect(grid.startIndex(), bounds);

    if (outAnimator == null) {
      outAlpha = new ObjectAnimator();
      outAlpha.setPropertyName("alpha");
      outAlpha.setIntValues(255, 0);

      outScale = new ObjectAnimator();
      outScale.setPropertyName("bounds");

      outAnimator = new AnimatorSet();
      outAnimator.setDuration(225);
      outAnimator.setInterpolator(Utils.fastOutLinearInInterpolator());
    }

    outAlpha.setTarget(drawable);
    outScale.setTarget(drawable);
    outScale.setObjectValues(Utils.squareRect(bounds), Utils.centerRect(bounds));
    outScale.setEvaluator(rectEvaluator);
    outAnimator.playTogether(outAlpha, outScale);
    outAnimator.start();
  }

  @Override public void writeToParcel(Parcel out, int flags) {
    out.writeInt(color);
  }

  public static final Parcelable.Creator<CircleHighlight> CREATOR =
      new Parcelable.Creator<CircleHighlight>() {
        @Override public CircleHighlight createFromParcel(Parcel in) {
          return new CircleHighlight(in.readInt());
        }

        @Override public CircleHighlight[] newArray(int size) {
          return new CircleHighlight[size];
        }
      };
}