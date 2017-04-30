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
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import org.threeten.bp.LocalDate;
import java.util.List;

/**
 * Selection listener which highlights and returns a single date.
 */
public class SingleSelectionListener implements SelectionListener {
  private final DatePickerView datePickerView;
  private final Highlight highlight;

  private LocalDate selection;

  public SingleSelectionListener(DatePickerView datePickerView, @ColorInt int highlightColor) {
    this.datePickerView = datePickerView;
    highlight = new SingleHighlight(highlightColor);
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

  private static class SingleHighlight extends Highlight {

    private final int color;

    private final RectEvaluator rectEvaluator = new RectEvaluator();
    private final Xfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);

    SingleHighlight(int color) {
      this.color = color;
    }

    @NonNull @Override
    protected Drawable createDrawable() {
      final ShapeDrawable d = new ShapeDrawable(new OvalShape());
      d.getPaint().setColor(color);
      d.getPaint().setXfermode(xfermode);
      return d;
    }

    @Override
    protected void onAdd(Drawable drawable, List<BoundedRect> regions) {
      expand(drawable, bounds(regions));
    }

    @Override
    protected void onShow(Drawable drawable, List<BoundedRect> regions) {
      drawable.setBounds(bounds(regions));
      drawable.setAlpha(255);
    }

    @Override
    protected void onChange(Drawable drawable, List<BoundedRect> regions) {
      // TODO support change animation?
    }

    @Override
    protected void onRemove(Drawable drawable, List<BoundedRect> regions) {
      contract(drawable, bounds(regions));
    }

    private Rect bounds(List<BoundedRect> regions) {
      final Rect bounds = new Rect(regions.get(0).rect);
      if (bounds.width() > bounds.height()) {
        bounds.inset((bounds.width() - bounds.height()) >> 1, 0);
      } else if (bounds.height() > bounds.width()) {
        bounds.inset(0, (bounds.height() - bounds.width()) >> 1);
      }
      return bounds;
    }

    private Rect zero(Rect bounds) {
      final int cx = bounds.centerX();
      final int cy = bounds.centerY();
      return new Rect(cx, cy, cx, cy);
    }

    private void expand(Drawable drawable, Rect bounds) {
      final AnimatorSet set = new AnimatorSet();
      set.playTogether(ObjectAnimator.ofInt(drawable, "alpha", 0, 255),
          ObjectAnimator.ofObject(drawable, "bounds", rectEvaluator, zero(bounds), bounds));
      set.setInterpolator(Utils.linearOutSlowInInterpolator());
      set.setDuration(225);
      set.start();
    }

    private void contract(Drawable drawable, Rect bounds) {
      final int cx = bounds.centerX();
      final int cy = bounds.centerY();
      final AnimatorSet set = new AnimatorSet();
      set.playTogether(ObjectAnimator.ofInt(drawable, "alpha", 255, 0),
          ObjectAnimator.ofObject(drawable, "bounds", rectEvaluator, bounds, zero(bounds)));
      set.setInterpolator(Utils.fastOutLinearInInterpolator());
      set.setDuration(195);
      set.start();
    }
  }
}
