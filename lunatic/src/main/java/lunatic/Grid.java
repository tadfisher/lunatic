package lunatic;

class Grid {

  final int rows;
  final int cols;
  private final int cw;
  private final int ch;
  private final int hw;
  private final int hh;

  Grid(final int rows, final int cols, final int cellWidth, final int cellHeight) {
    this.rows = rows;
    this.cols = cols;
    this.cw = cellWidth;
    this.ch = cellHeight;
    hw = cellWidth / 2;
    hh = cellHeight / 2;
  }

  int width() {
    return cw * cols;
  }

  int height() {
    return ch * rows;
  }

  int left(int x) {
    return x * cw;
  }

  int right(int x) {
    return x * cw + cw;
  }

  int top(int y) {
    return y * ch;
  }

  int bottom(int y) {
    return y * ch + ch;
  }

  int centerX(int x) {
    return x * cw + hw;
  }

  int centerY(int y) {
    return y * ch + hh;
  }

  int row(int offset) {
    return offset / cols;
  }

  int col(int offset) {
    return offset % cols;
  }

  void rowRect(int row, int indexStart, int indexEnd, BoundedRect boundedRect) {
    int startY = row(indexStart);
    int endY = row(indexEnd);
    if (row < startY || row > endY) {
      boundedRect.rect.setEmpty();
      return;
    }

    int startX = row == startY ? col(indexStart) : 0;
    int endX = row == endY ? col(indexEnd) : cols - 1;
    boundedRect.rect.set(left(startX), top(row), right(endX), bottom(row));
    boundedRect.openStart = row != startY;
    boundedRect.openEnd = row != endY;
  }

  int offsetAtPixel(int x, int y) {
    int col = x / cw;
    int row = y / ch;
    if (col < 0 || col >= cols || row < 0 || row >= rows) {
      return -1;
    }
    return row * cols + col;
  }
}
