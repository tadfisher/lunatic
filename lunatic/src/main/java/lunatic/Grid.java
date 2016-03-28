package lunatic;

import android.graphics.Rect;

/**
 * Created by tad on 2/16/16.
 */
public class Grid {

    public final int rows;
    public final int cols;
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

    public void rect(int x0, int y0, int x1, int y1, Rect r) {
        if (x0 > x1 || y0 > y1) {
            throw new IllegalArgumentException("Nope.");
        }
        r.set(left(x0), top(y0), right(x1), bottom(y1));
    }
}
