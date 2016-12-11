package lunatic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import codes.tad.lunatic.R;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.WeekFields;

/**
 * Created by tad on 11/16/15.
 */
public class MonthView extends View {

  // These must remain in ascending order!
  @SuppressLint("InlinedApi") private static final int[] TEXT_APPEARANCE_ATTRS = {
      android.R.attr.textSize, android.R.attr.typeface, android.R.attr.textStyle,
      android.R.attr.textColor, android.R.attr.textAllCaps, android.R.attr.fontFamily,
      android.R.attr.elegantTextHeight, android.R.attr.letterSpacing,
      android.R.attr.fontFeatureSettings
  };

  private static final int TEXT_SIZE = 0;
  private static final int TYPEFACE = 1;
  private static final int TEXT_STYLE = 2;
  private static final int TEXT_COLOR = 3;
  private static final int TEXT_ALL_CAPS = 4;
  private static final int FONT_FAMILY = 5;
  private static final int ELEGANT_TEXT_HEIGHT = 6;
  private static final int LETTER_SPACING = 7;
  private static final int FONT_FEATURE_SETTINGS = 8;

  private static final int SANS = 1;
  private static final int SERIF = 2;
  private static final int MONOSPACE = 3;

  private static final int DAY_PAINT = 0;
  private static final int MONTH_PAINT = 1;
  private static final int WEEKDAY_PAINT = 2;

  private static final int[] STATE_ACTIVE = new int[] { android.R.attr.state_active };
  private static final int[] STATE_ENABLED = new int[] { android.R.attr.state_enabled };

  private final Paint dayPaint = new Paint();
  private final Paint monthPaint = new Paint();
  private final Paint weekdayPaint = new Paint();
  private final Paint gridPaint = new Paint();

  private final boolean[] textAllCaps = new boolean[3];
  private final int[] textOffsetY = new int[3];

  private final Rect bounds = new Rect();
  private Grid dayGrid;

  private int cellCount;
  private int cellOffset;
  private int rowCount;

  private int dayWidth;
  private int dayHeight;
  private int weekdayHeight;
  private int monthHeight;

  private int offsetX;
  private boolean drawGrid;

  private int textColorActive;
  private int textColorEnabled;
  private int textColorDisabled;

  private WeekFields weekFields;
  private DateTimeFormatter headerFormatter;
  private String[] weekdayLabels;
  private SelectionListener listener;

  private String yearMonthLabel;
  private boolean[] enabledDays;

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

    dayWidth = a.getDimensionPixelSize(R.styleable.lunatic_MonthView_lunatic_dayWidth, 0);
    dayHeight = a.getDimensionPixelSize(R.styleable.lunatic_MonthView_lunatic_dayHeight, 0);
    weekdayHeight = a.getDimensionPixelSize(R.styleable.lunatic_MonthView_lunatic_weekDayHeight, 0);
    monthHeight = a.getDimensionPixelSize(R.styleable.lunatic_MonthView_lunatic_monthHeight, 0);

    drawGrid = a.getBoolean(R.styleable.lunatic_MonthView_lunatic_drawGrid, false);
    gridPaint.setColor(a.getColor(R.styleable.lunatic_MonthView_lunatic_gridColor, 0xffcccccc));
    gridPaint.setStrokeWidth(a.getDimension(R.styleable.lunatic_MonthView_lunatic_gridStroke, 1f));

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

    if (isInEditMode()) {
      bindFakeMonth();
    }
  }

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

  void bind(final YearMonth month, final boolean[] enabledDays) {
    cellCount = enabledDays.length;
    cellOffset = Utils.startOfWeekOffset(weekFields, month.atDay(1).getDayOfWeek());
    rowCount = (int) Math.ceil((double) (cellCount + cellOffset) / 7d);
    this.enabledDays = enabledDays;

    yearMonthLabel = headerFormatter.format(month);
    if (textAllCaps[MONTH_PAINT]) {
      yearMonthLabel = yearMonthLabel.toUpperCase();
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
    weekdayLabels = new String[] { "S", "M", "T", "W", "T", "F", "S" };

    requestLayout();
    invalidate();
  }

  @Override protected int getSuggestedMinimumWidth() {
    return getPaddingLeft() + getPaddingRight() + dayWidth * 7;
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
        resolveSize(monthHeight
            + weekdayHeight
            + dayGrid.bottom(rowCount - 1)
            + getPaddingTop()
            + getPaddingBottom(), heightMeasureSpec));
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    bounds.left = getPaddingLeft();
    bounds.top = getPaddingTop();
    bounds.right = w - getPaddingRight();
    bounds.bottom = h - getPaddingBottom();

    // Center view in bounds
    offsetX = (bounds.width() - dayWidth * 7) / 2;
  }

  @Override protected void onDraw(Canvas canvas) {
    drawMonth(canvas);
    drawWeekdayLabels(canvas);
    drawDayGrid(canvas);
    drawDayLabels(canvas);
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

  protected void drawDayLabel(Canvas canvas, int day, int x, int y) {
    if (enabledDays[day - 1]) {
      dayPaint.setColor(textColorEnabled);
    } else {
      dayPaint.setColor(textColorDisabled);
    }

    canvas.drawText(String.valueOf(day), x, y, dayPaint);
  }

  /**
   * Return the day number at the specified calendar grid coordinate.
   * <p />
   * Note: This result is 1-indexed!
   */
  private int dayAt(int row, int col) {
    return (row * 7) + col - cellOffset + 1;
  }

  protected void setPaintTextAppearance(int paintIndex, Paint paint, int textAppearanceResId) {
    if (paint == null || textAppearanceResId <= 0) {
      return;
    }

    TypedArray a =
        getContext().getTheme().obtainStyledAttributes(textAppearanceResId, TEXT_APPEARANCE_ATTRS);

    int textSize = 15;
    int typefaceIndex = -1;
    int styleIndex = -1;
    ColorStateList textColor = null;
    boolean allCaps = false;
    String fontFamily = null;
    boolean elegant = false;
    float letterSpacing = 0;
    String fontFeatureSettings = null;

    for (int i = 0; i < a.getIndexCount(); i++) {
      int attr = a.getIndex(i);
      switch (attr) {
        case TEXT_SIZE:
          textSize = a.getDimensionPixelSize(attr, textSize);
          break;
        case TYPEFACE:
          typefaceIndex = a.getInt(attr, typefaceIndex);
          break;
        case TEXT_STYLE:
          styleIndex = a.getInt(attr, styleIndex);
          break;
        case TEXT_COLOR:
          textColor = a.getColorStateList(attr);
          break;
        case TEXT_ALL_CAPS:
          allCaps = a.getBoolean(attr, allCaps);
          break;
        case FONT_FAMILY:
          fontFamily = a.getString(attr);
          break;
        case ELEGANT_TEXT_HEIGHT:
          elegant = a.getBoolean(attr, elegant);
          break;
        case LETTER_SPACING:
          letterSpacing = a.getFloat(attr, letterSpacing);
          break;
        case FONT_FEATURE_SETTINGS:
          fontFeatureSettings = a.getString(attr);
          break;
      }
    }

    a.recycle();

    paint.setTextSize(textSize);

    if (textColor != null) {
      paint.setColor(textColor.getDefaultColor());
      if (paintIndex == DAY_PAINT && textColor.isStateful()) {
        // TODO pull ?textColorPrimary, etc
        textColorActive =
            textColor.getColorForState(STATE_ACTIVE, android.R.color.holo_blue_bright);
        textColorEnabled = textColor.getColorForState(STATE_ENABLED, android.R.color.black);
        textColorDisabled = textColor.getDefaultColor();
      }
    }
    textAllCaps[paintIndex] = allCaps;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      paint.setElegantTextHeight(elegant);
      paint.setLetterSpacing(letterSpacing);
      paint.setFontFeatureSettings(fontFeatureSettings);
    }

    setPaintTypefaceFromAttrs(paint, fontFamily, typefaceIndex, styleIndex);

    textOffsetY[paintIndex] = -(paint.getFontMetricsInt().ascent / 2);
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
    paint.setTextAlign(Paint.Align.CENTER);
  }
}
