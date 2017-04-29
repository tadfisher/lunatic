package lunatic;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.ColorInt;
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

    private final ShapeDrawable circle;

    private final Rect zero = new Rect();
    private final RectEvaluator rectEvaluator = new RectEvaluator();

    SingleHighlight(int color) {
      circle = new ShapeDrawable(new OvalShape());
      circle.getPaint().setColor(color);
    }

    @Override
    protected Drawable bind(List<BoundedRect> regions, Op op) {
      if (regions.isEmpty()) {
        throw new IllegalArgumentException("regions is empty");
      }

      // We only support a single selection.
      final BoundedRect region = regions.get(0);

      // Square the bounds.
      final Rect rect = region.rect;
      if (rect.width() > rect.height()) {
        rect.inset((rect.width() - rect.height()) >> 1, 0);
      } else if (rect.height() > rect.width()) {
        rect.inset(0, (rect.height() - rect.width()) >> 1);
      }

      switch (op) {
        case ADD:
          expand(circle, region.rect);
          break;
        case REMOVE:
          contract(circle, region.rect);
          break;
        case SHOW:
          circle.setAlpha(255);
          circle.setBounds(region.rect);
          break;
      }

      return circle;
    }

    private void expand(Drawable drawable, Rect targetBounds) {
      final int cx = targetBounds.centerX();
      final int cy = targetBounds.centerY();
      zero.set(cx, cy, cx, cy);
      final AnimatorSet set = new AnimatorSet();
      set.playTogether(ObjectAnimator.ofInt(drawable, "alpha", 0, 255),
          ObjectAnimator.ofObject(drawable, "bounds", rectEvaluator, zero, targetBounds));
      set.setInterpolator(Utils.linearOutSlowInInterpolator());
      set.setDuration(225);
      set.start();
    }

    private void contract(Drawable drawable, Rect sourceBounds) {
      final int cx = sourceBounds.centerX();
      final int cy = sourceBounds.centerY();
      zero.set(cx, cy, cx, cy);
      final AnimatorSet set = new AnimatorSet();
      set.playTogether(ObjectAnimator.ofInt(drawable, "alpha", 255, 0),
          ObjectAnimator.ofObject(drawable, "bounds", rectEvaluator, sourceBounds, zero));
      set.setInterpolator(Utils.fastOutLinearInInterpolator());
      set.setDuration(195);
      set.start();
    }
  }
}
