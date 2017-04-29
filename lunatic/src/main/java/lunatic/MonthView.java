package lunatic;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import codes.tad.lunatic.R;

public class MonthView extends View {

  private static final int SANS = 1;

  private static final int SERIF = 2;

  private static final int MONOSPACE = 3;

  private static final int DAY_PAINT = 0;

  private static final int MONTH_PAINT = 1;

  private static final int WEEKDAY_PAINT = 2;

  private static final int[] STATE_ACTIVATED = new int[]{android.R.attr.state_activated};

  private static final int[] STATE_ENABLED = new int[]{android.R.attr.state_enabled};

  private static final int[] STATE_PRESSED = new int[]{android.R.attr.state_pressed};

  private static final int[] STATE_DISABLED = new int[]{-android.R.attr.state_enabled};

  private static final int[] STATE_ENABLED_ACTIVATED = new int[]{
      android.R.attr.state_enabled,
      android.R.attr.state_activated
  };

  private static final int[] STATE_ENABLED_PRESSED = new int[]{
      android.R.attr.state_enabled,
      android.R.attr.state_pressed
  };

  private static final int SELECTED_HIGHLIGHT_ALPHA = 0xB0;

  private final TextPaint dayPaint = new TextPaint();

  private final TextPaint monthPaint = new TextPaint();

  private final TextPaint weekdayPaint = new TextPaint();

  private final Paint daySelectorPaint = new Paint();

  private final Paint dayHighlightPaint = new Paint();

  private final Paint dayHighlightSelectorPaint = new Paint();

  private final Paint gridPaint = new Paint();

  private final boolean[] textAllCaps = new boolean[3];

  private final float[] textOffsetY = new float[3];

  private final Rect bounds = new Rect();

  private Grid dayGrid;

  private int cellCount;

  private int cellOffset;

  private int rowCount;

  private int weekdayHeight;

  private int monthHeight;

  private int offsetX;

  private boolean drawGrid;

  private ColorStateList dayTextColor;

  private WeekFields weekFields;

  private DateTimeFormatter headerFormatter;

  private String[] weekdayLabels;

  private SelectionListener listener;

  private YearMonth month;

  private int today;

  private String yearMonthLabel;

  private boolean[] enabledDays;

  private ArrayList<Highlight> highlights;

  private ArrayList<BoundedRect> highlightedRects;

  private ArrayList<Drawable> highlightDrawables;

  private ArrayList<Drawable> removedHighlightDrawables;

  public MonthView(Context context) {
    this(context, null);
  }

  public MonthView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.lunatic_monthViewStyle);
  }

  public MonthView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    TypedArray a = context.getTheme().obtainStyledAttributes(
        attrs, R.styleable.lunatic_MonthView, defStyleAttr, R.style.lunatic_MonthView);

    int dayWidth = a.getDimensionPixelSize(R.styleable.lunatic_MonthView_lunatic_dayWidth, 0);
    int dayHeight = a.getDimensionPixelSize(R.styleable.lunatic_MonthView_lunatic_dayHeight, 0);
    weekdayHeight = a.getDimensionPixelSize(R.styleable.lunatic_MonthView_lunatic_weekDayHeight, 0);
    monthHeight = a.getDimensionPixelSize(R.styleable.lunatic_MonthView_lunatic_monthHeight, 0);

    drawGrid = a.getBoolean(R.styleable.lunatic_MonthView_lunatic_drawGrid, false);
    gridPaint.setColor(a.getColor(R.styleable.lunatic_MonthView_lunatic_gridColor, 0xffcccccc));
    gridPaint.setStrokeWidth(a.getDimension(R.styleable.lunatic_MonthView_lunatic_gridStroke, 1f));

    final ColorStateList daySelectorColor =
        a.getColorStateList(R.styleable.lunatic_MonthView_lunatic_daySelectorColor);
    final int activatedColor;
    if (daySelectorColor == null) {
      activatedColor = a.getColor(R.styleable.lunatic_MonthView_lunatic_daySelectorColor, 0);
    } else {
      activatedColor = daySelectorColor.getColorForState(STATE_ENABLED_ACTIVATED, 0);
    }
    daySelectorPaint.setColor(activatedColor);
    dayHighlightSelectorPaint.setColor(activatedColor);
    dayHighlightSelectorPaint.setAlpha(SELECTED_HIGHLIGHT_ALPHA);

    final ColorStateList dayHighlightColor =
        a.getColorStateList(R.styleable.lunatic_MonthView_lunatic_dayHighlightColor);
    final int pressedColor;
    if (dayHighlightColor == null) {
      pressedColor = a.getColor(R.styleable.lunatic_MonthView_lunatic_dayHighlightColor, 0);
    } else {
      pressedColor = dayHighlightColor.getColorForState(STATE_ENABLED_PRESSED, 0);
    }
    dayHighlightPaint.setColor(pressedColor);

    int textAppearanceDayRes =
        a.getResourceId(R.styleable.lunatic_MonthView_lunatic_dateTextAppearance, 0);
    int textAppearanceWeekdayRes =
        a.getResourceId(R.styleable.lunatic_MonthView_lunatic_weekDayTextAppearance, 0);
    int textAppearanceMonthRes =
        a.getResourceId(R.styleable.lunatic_MonthView_lunatic_monthTextAppearance, 0);

    a.recycle();

    dayGrid = new Grid(6, 7, dayWidth, dayHeight);

    setPaintTextAppearance(DAY_PAINT, dayPaint, textAppearanceDayRes);
    setPaintTextAppearance(MONTH_PAINT, monthPaint, textAppearanceMonthRes);
    setPaintTextAppearance(WEEKDAY_PAINT, weekdayPaint, textAppearanceWeekdayRes);

    setDefaultPaintFlags(monthPaint);
    setDefaultPaintFlags(weekdayPaint);
    setDefaultPaintFlags(dayPaint);
    setDefaultPaintFlags(daySelectorPaint);
    setDefaultPaintFlags(dayHighlightPaint);
    setDefaultPaintFlags(dayHighlightSelectorPaint);

    yearMonthLabel = "";

    if (isInEditMode()) {
      bindFakeMonth();
    }
  }

  @SuppressWarnings("unused")
  public void setTypeface(Typeface tf) {
    setPaintTypeface(dayPaint, tf);
    setPaintTypeface(weekdayPaint, tf);
    setPaintTypeface(monthPaint, tf);
  }

  void setStaticOptions(WeekFields weekFields, DateTimeFormatter headerFormatter,
      String[] weekdayLabels, SelectionListener listener) {
    this.weekFields = weekFields;
    this.headerFormatter = headerFormatter;

    this.weekdayLabels = new String[7];
    System.arraycopy(weekdayLabels, 0, this.weekdayLabels, 0, 7);
    if (textAllCaps[WEEKDAY_PAINT]) {
      for (int i = 0; i < 7; i++) {
        this.weekdayLabels[i] = this.weekdayLabels[i].toUpperCase();
      }
    }

    this.listener = listener;
  }

  void bind(final YearMonth month, final LocalDate now, final boolean[] enabledDays,
      final Highlight[] highlights) {
    this.month = month;

    cellCount = enabledDays.length;
    cellOffset = Utils.startOfWeekOffset(weekFields, month.atDay(1).getDayOfWeek());
    rowCount = (int) Math.ceil((double) (cellCount + cellOffset) / 7d);
    this.enabledDays = enabledDays;

    yearMonthLabel = headerFormatter.format(month);
    if (textAllCaps[MONTH_PAINT]) {
      yearMonthLabel = yearMonthLabel.toUpperCase();
    }

    today = YearMonth.from(now).equals(month)
        ? now.getDayOfMonth()
        : -1;

    clearHighlights();
    for (Highlight h : highlights) {
      addHighlight(h, false);
    }

    requestLayout();
    invalidate();
  }

  private void bindFakeMonth() {
    cellCount = 30;
    cellOffset = 0;
    rowCount = (int) Math.ceil((double) (cellCount + cellOffset) / 7d);

    enabledDays = new boolean[cellCount];
    for (int i = 0; i < cellCount; i++) {
      enabledDays[i] = i % 12 != 0;
    }

    weekFields = WeekFields.SUNDAY_START;
    yearMonthLabel = "November 2015";
    weekdayLabels = new String[]{"S", "M", "T", "W", "T", "F", "S"};
    today = 13;

    requestLayout();
    invalidate();
  }

  void addHighlight(Highlight highlight) {
    addHighlight(highlight, true);
  }

  private void addHighlight(Highlight highlight, boolean animate) {
    final boolean openStart = highlight.interval.startMonth.isBefore(month);
    final boolean openEnd = highlight.interval.endMonth.isAfter(month);

    final int start = openStart ? 1 : highlight.interval.start.getDayOfMonth();
    final int end = openEnd ? cellCount : highlight.interval.end.getDayOfMonth();

    if (highlights == null) {
      highlights = new ArrayList<>(1);
    }
    final int count = highlights.size();
    highlights.add(highlight);

    final int rectsFrom = count * 6;
    final int rectsTo = count * 6 + 6;

    int invalidatedFrom = -1;
    int invalidatedTo = -1;

    if (highlightedRects == null) {
      highlightedRects = new ArrayList<>(6);
    } else {
      highlightedRects.ensureCapacity(count * 6);
    }

    for (int i = rectsFrom; i < rectsTo; i++) {
      final BoundedRect r;
      if (highlightedRects.size() <= i) {
        r = new BoundedRect();
        highlightedRects.add(r);
      } else {
        r = highlightedRects.get(i);
      }
      dayGrid.rowRect(i % 6, start + cellOffset - 1, end + cellOffset - 1, r);
      if (!r.rect.isEmpty()) {
        if (invalidatedFrom == -1) {
          invalidatedFrom = i;
        }
        invalidatedTo = i + 1;
      }
    }

    final List<BoundedRect> regions = highlightedRects.subList(invalidatedFrom, invalidatedTo);

    if (highlightDrawables == null) {
      highlightDrawables = new ArrayList<>();
    }

    Drawable d = highlight.createDrawable();
    d.setCallback(this);
    highlightDrawables.add(d);

    if (animate) {
      highlight.onAdd(d, regions);
    } else {
      highlight.onShow(d, regions);
    }
  }

  void removeHighlight(Highlight highlight) {
    if (highlights == null) {
      return;
    }

    final int index = highlights.indexOf(highlight);
    final Drawable d = highlightDrawables.remove(index);

    int invalidatedFrom = -1;
    int invalidatedTo = -1;
    for (int i = index * 6; i < index * 6 + 6; i++) {
      final BoundedRect r = highlightedRects.get(i);
      if (!r.rect.isEmpty()) {
        if (invalidatedFrom == -1) {
          invalidatedFrom = i;
        }
        invalidatedTo = i + 1;
      }
    }
    highlight.onRemove(d, highlightedRects.subList(invalidatedFrom, invalidatedTo));

    highlights.remove(index);
    for (int i = 0; i < 6; i++) {
      highlightedRects.remove(index * 6);
    }

    if (removedHighlightDrawables == null) {
      removedHighlightDrawables = new ArrayList<>();
    }
    removedHighlightDrawables.add(d);
  }

  private void clearHighlights() {
    if (highlights != null) {
      highlights.clear();
    }
    if (highlightDrawables != null) {
      highlightDrawables.clear();
    }
    if (removedHighlightDrawables != null) {
      removedHighlightDrawables.clear();
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    final int x = (int) (event.getX() + 0.5f);
    final int y = (int) (event.getY() + 0.5f);

    final int action = event.getAction();
    switch (action) {
      case ACTION_DOWN:
      case ACTION_MOVE:
        final int touchedItem = dayAtPixel(x, y);
        // TODO draw item highlight
        if (action == ACTION_DOWN && touchedItem < 0) {
          return false;
        }
        break;
      case ACTION_UP:
        final int day = dayAtPixel(x, y);
        if (day > 0 && isDayEnabled(day)) {
          listener.onDateClicked(month.atDay(day));
        }
        break;
      case ACTION_CANCEL:
        break;
    }
    return true;
  }

  @Override
  protected int getSuggestedMinimumWidth() {
    return getPaddingLeft() + getPaddingRight() + dayGrid.width();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
        resolveSize(monthHeight
            + weekdayHeight
            + dayGrid.bottom(rowCount - 1)
            + getPaddingTop()
            + getPaddingBottom(), heightMeasureSpec));
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    bounds.left = getPaddingLeft();
    bounds.top = getPaddingTop();
    bounds.right = w - getPaddingRight();
    bounds.bottom = h - getPaddingBottom();

    // Center view in bounds
    offsetX = (bounds.width() - dayGrid.width()) / 2;
  }

  @Override
  protected boolean verifyDrawable(@NonNull Drawable who) {
    return highlightDrawables != null && highlightDrawables.contains(who)
        || removedHighlightDrawables != null && removedHighlightDrawables.contains(who)
        || super.verifyDrawable(who);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    drawMonth(canvas);
    drawWeekdayLabels(canvas);
    drawDayGrid(canvas);
    drawDayLabels(canvas);
    drawHighlights(canvas);
  }

  protected void drawMonth(Canvas canvas) {
    canvas.save();
    canvas.translate(bounds.centerX(),
        bounds.top + (monthHeight / 2) + textOffsetY[MONTH_PAINT]);
    canvas.drawText(yearMonthLabel, 0, 0, monthPaint);
    canvas.restore();
  }

  protected void drawWeekdayLabels(Canvas canvas) {
    canvas.save();
    canvas.translate(offsetX,
        bounds.top + monthHeight + (weekdayHeight / 2) + textOffsetY[WEEKDAY_PAINT]);

    for (int col = 0; col < 7; col++) {
      canvas.drawText(
          weekdayLabels[col],
          dayGrid.centerX(col),
          0,
          weekdayPaint);
    }

    canvas.restore();
  }

  protected void drawDayGrid(Canvas canvas) {
    if (!drawGrid) {
      return;
    }

    canvas.save();
    canvas.translate(offsetX, bounds.top + monthHeight + weekdayHeight);

    for (int row = 0; row < rowCount; row++) {
      int top = dayGrid.top(row);
      canvas.drawLine(0, top, dayGrid.width(), top, gridPaint);
    }
    int bottom = dayGrid.bottom(rowCount - 1);
    canvas.drawLine(0, bottom, dayGrid.width(), bottom, gridPaint);

    float adjust = gridPaint.getStrokeWidth() / 2;
    int top = dayGrid.top(0);
    bottom = dayGrid.bottom(rowCount - 1);
    for (int col = 0; col < 8; col++) {
      int left = dayGrid.left(col);
      canvas.drawLine(left - adjust, top - adjust, left, bottom + adjust, gridPaint);
    }
    int right = dayGrid.right(6);
    canvas.drawLine(right - adjust, top - adjust, right, bottom + adjust, gridPaint);

    canvas.restore();
  }

  protected void drawDayLabels(Canvas canvas) {
    canvas.save();
    canvas.translate(offsetX, bounds.top + monthHeight + weekdayHeight);

    for (int row = 0; row < dayGrid.rows; row++) {
      for (int col = 0; col < 7; col++) {
        int day = dayAt(row, col);
        if (day < 1 || day > cellCount) {
          continue;
        }
        drawDayLabel(canvas, day,
            dayGrid.centerX(col),
            dayGrid.centerY(row) + textOffsetY[DAY_PAINT]);
      }
    }

    canvas.restore();
  }

  protected void drawDayLabel(Canvas canvas, int dayOfMonth, float x, float y) {
    int[] stateSet;

    if (isDayEnabled(dayOfMonth)) {
      stateSet = STATE_ENABLED;
    } else {
      stateSet = STATE_DISABLED;
    }

    final int textColor;
    if (today == dayOfMonth) {
      textColor = daySelectorPaint.getColor();
    } else {
      textColor = dayTextColor.getColorForState(stateSet, 0);
    }
    dayPaint.setColor(textColor);

    canvas.drawText(String.valueOf(dayOfMonth), x, y, dayPaint);
  }

  private void drawHighlights(Canvas canvas) {
    if (highlightDrawables == null && removedHighlightDrawables == null) {
      return;
    }

    canvas.save();
    canvas.translate(offsetX, bounds.top + monthHeight + weekdayHeight);

    if (highlightDrawables != null) {
      for (Drawable d : highlightDrawables) {
        d.draw(canvas);
      }
    }

    if (removedHighlightDrawables != null) {
      for (Drawable d : removedHighlightDrawables) {
        d.draw(canvas);
      }
    }

    canvas.restore();
  }

  /**
   * Return the day number at the specified calendar grid coordinate.
   * <p />
   * Note: This result is 1-indexed!
   */
  private int dayAt(int row, int col) {
    return (row * 7) + col - cellOffset + 1;
  }

  private int dayAtPixel(int x, int y) {
    int day = dayGrid.offsetAtPixel(x - offsetX,
        y - bounds.top - monthHeight - weekdayHeight) - cellOffset + 1;
    if (day < 1 || day > cellCount) {
      return -1;
    }
    return day;
  }

  private boolean isDayEnabled(int dayOfMonth) {
    return enabledDays[dayOfMonth - 1];
  }

  protected void setPaintTextAppearance(int paintIndex, TextPaint paint, int textAppearanceResId) {
    if (paint == null || textAppearanceResId <= 0) {
      return;
    }

    TypedArray a = getContext().getTheme().obtainStyledAttributes(
        textAppearanceResId, R.styleable.lunatic_TextAppearance);

    int textSize = 15;
    int typefaceIndex = -1;
    int styleIndex = 0;
    ColorStateList textColor = null;
    boolean allCaps = false;
    String fontFamily = null;
    boolean elegant = false;
    float letterSpacing = 0;
    String fontFeatureSettings = null;

    for (int i = 0; i < a.getIndexCount(); i++) {
      int attr = a.getIndex(i);
      if (attr == R.styleable.lunatic_TextAppearance_android_textSize) {
        textSize = a.getDimensionPixelSize(attr, textSize);
      } else if (attr == R.styleable.lunatic_TextAppearance_android_typeface) {
        typefaceIndex = a.getInt(attr, typefaceIndex);
      } else if (attr == R.styleable.lunatic_TextAppearance_android_textStyle) {
        styleIndex = a.getInt(attr, styleIndex);
      } else if (attr == R.styleable.lunatic_TextAppearance_android_textColor) {
        textColor = a.getColorStateList(attr);
      } else if (attr == R.styleable.lunatic_TextAppearance_android_textAllCaps) {
        allCaps = a.getBoolean(attr, allCaps);
      } else if (attr == R.styleable.lunatic_TextAppearance_android_fontFamily) {
        fontFamily = a.getString(attr);
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        if (attr == R.styleable.lunatic_TextAppearance_android_elegantTextHeight) {
          elegant = a.getBoolean(attr, elegant);
        } else if (attr == R.styleable.lunatic_TextAppearance_android_letterSpacing) {
          letterSpacing = a.getFloat(attr, letterSpacing);
        } else if (attr == R.styleable.lunatic_TextAppearance_android_fontFeatureSettings) {
          fontFeatureSettings = a.getString(attr);
        }
      }
    }

    a.recycle();

    paint.setTextSize(textSize);

    if (textColor != null) {
      paint.setColor(textColor.getDefaultColor());
      if (paintIndex == DAY_PAINT) {
        dayTextColor = textColor;
      }
    }
    textAllCaps[paintIndex] = allCaps;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      paint.setElegantTextHeight(elegant);
      paint.setLetterSpacing(letterSpacing);
      paint.setFontFeatureSettings(fontFeatureSettings);
    }

    setPaintTypefaceFromAttrs(paint, fontFamily, typefaceIndex, styleIndex);

    paint.setTextAlign(Paint.Align.CENTER);
    textOffsetY[paintIndex] = -(paint.ascent() + paint.descent()) / 2f;
  }

  private void setPaintTypefaceFromAttrs(Paint paint, String familyName, int typefaceIndex,
      int styleIndex) {
    Typeface tf = null;
    if (familyName != null) {
      tf = Typeface.create(familyName, styleIndex);
      if (tf != null) {
        setPaintTypeface(paint, tf);
        return;
      }
    }
    switch (typefaceIndex) {
      case SANS:
        tf = Typeface.SANS_SERIF;
        break;
      case SERIF:
        tf = Typeface.SERIF;
        break;

      case MONOSPACE:
        tf = Typeface.MONOSPACE;
        break;
    }

    setPaintTypeface(paint, tf, styleIndex);
  }

  protected void setPaintTypeface(Paint paint, Typeface tf, int style) {
    if (style > 0) {
      if (tf == null) {
        tf = Typeface.defaultFromStyle(style);
      } else {
        tf = Typeface.create(tf, style);
      }

      setPaintTypeface(paint, tf);

      // Fake styles.
      int typefaceStyle = tf != null ? tf.getStyle() : 0;
      int need = style & ~typefaceStyle;
      paint.setFakeBoldText((need & Typeface.BOLD) != 0);
      paint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
    } else {
      paint.setFakeBoldText(false);
      paint.setTextSkewX(0);
      setPaintTypeface(paint, tf);
    }
  }

  protected void setPaintTypeface(Paint paint, Typeface tf) {
    if (paint.getTypeface() != tf) {
      paint.setTypeface(tf);
      requestLayout();
      invalidate();
    }
  }

  protected void setDefaultPaintFlags(Paint paint) {
    paint.setAntiAlias(true);
    paint.setStyle(Paint.Style.FILL);
  }
}
