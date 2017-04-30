package lunatic;

import android.graphics.Rect;
import android.support.annotation.NonNull;

public class BoundedGrid extends Grid {

  private int startIndex;
  private int endIndex;
  private boolean openStart;
  private boolean openEnd;

  BoundedGrid(Grid grid) {
    super(grid.rows, grid.cols, grid.cw, grid.ch);
  }

  public int startIndex() {
    return startIndex;
  }

  public int endIndex() {
    return endIndex;
  }

  public boolean openStart() {
    return openStart;
  }

  public boolean openEnd() {
    return openEnd;
  }

  public int cellWidth() {
    return cw;
  }

  public int cellHeight() {
    return ch;
  }

  public int rangeLeft() {
    if (row(endIndex) > row(startIndex)) {
      return 0;
    }
    return left(col(startIndex));
  }

  public int rangeTop() {
    return top(row(startIndex));
  }

  public int rangeRight() {
    if (row(endIndex) > row(startIndex)) {
      return right(cols);
    }
    return right(endIndex);
  }

  public int rangeBottom() {
    return bottom(row(endIndex));
  }

  public Rect rect(int index) {
    Rect rect = new Rect();
    rect(index, rect);
    return rect;
  }

  public void rect(int index, @NonNull Rect rect) {
    final int col = col(index);
    final int row = row(index);
    rect.set(left(col), top(row), right(col), bottom(row));
  }

  // Private setters

  BoundedGrid startIndex(int startIndex) {
    this.startIndex = startIndex;
    return this;
  }

  BoundedGrid endIndex(int endIndex) {
    this.endIndex = endIndex;
    return this;
  }

  BoundedGrid openStart(boolean openStart) {
    this.openStart = openStart;
    return this;
  }

  BoundedGrid openEnd(boolean openEnd) {
    this.openEnd = openEnd;
    return this;
  }
}
