package lunatic;

import android.graphics.Rect;

public class BoundedRect {

  public final Rect rect;

  public boolean openStart;

  public boolean openEnd;

  BoundedRect() {
    rect = new Rect();
  }
}
