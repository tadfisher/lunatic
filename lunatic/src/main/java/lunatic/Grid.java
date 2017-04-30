package lunatic;

class Grid {

  final int rows;
  final int cols;
  final int cw;
  final int ch;
  final int hw;
  final int hh;

  Grid(final int rows, final int cols, final int cellWidth, final int cellHeight) {
    this.rows = rows;
    this.cols = cols;
    this.cw = cellWidth;
    this.ch = cellHeight;
    hw = cellWidth / 2;
    hh = cellHeight / 2;
  }

  public int width() {
    return cw * cols;
  }

  public int height() {
    return ch * rows;
  }

  public int left(int x) {
    return x * cw;
  }

  public int right(int x) {
    return x * cw + cw;
  }

  public int top(int y) {
    return y * ch;
  }

  public int bottom(int y) {
    return y * ch + ch;
  }

  public int centerX(int x) {
    return x * cw + hw;
  }

  public int centerY(int y) {
    return y * ch + hh;
  }

  public int row(int index) {
    return index / cols;
  }

  public int col(int index) {
    return index % cols;
  }

  public int offsetAtPixel(int x, int y) {
    int col = x / cw;
    int row = y / ch;
    if (col < 0 || col >= cols || row < 0 || row >= rows) {
      return -1;
    }
    return row * cols + col;
  }
}
