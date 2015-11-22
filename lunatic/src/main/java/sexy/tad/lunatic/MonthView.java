package sexy.tad.lunatic;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by tad on 11/16/15.
 */
public class MonthView extends View {

    // These must remain in ascending order!
    @SuppressLint("InlinedApi")
    private static final int[] TEXT_APPEARANCE_ATTRS = {
            android.R.attr.textSize,
            android.R.attr.typeface,
            android.R.attr.textStyle,
            android.R.attr.textColor,
            android.R.attr.textAllCaps,
            android.R.attr.fontFamily,
            android.R.attr.elegantTextHeight,
            android.R.attr.letterSpacing,
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

    private static final int[] STATE_ACTIVE = new int[] {android.R.attr.state_active};
    private static final int[] STATE_ENABLED = new int[] {android.R.attr.state_enabled};

    private final Paint dayPaint = new Paint();
    private final Paint monthPaint = new Paint();
    private final Paint weekdayPaint = new Paint();
    private final Paint gridPaint = new Paint();

    private final boolean[] textAllCaps = new boolean[3];
    private final int[] textOffsetY = new int[3];

    private final Rect bounds = new Rect();

    private int cellCount;
    private int cellOffset;
    private int rowCount;
    private int offsetX;

    private int cellWidth;
    private int cellHeight;
    private boolean drawGrid;

    private int textColorActive;
    private int textColorEnabled;
    private int textColorDisabled;

    private int firstDayOfWeek;
    private SimpleDateFormat headerFormat;
    private String[] weekdayLabels;
    private NumberFormat dayFormat;
    private Lunatic.SelectionListener listener;

    private String yearMonthLabel;
    private boolean[] enabledDays;

    public MonthView(Context context) {
        super(context);
        init(context, null, R.attr.lunatic_monthViewStyle, R.style.lunatic_MonthView);
    }

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.lunatic_monthViewStyle, R.style.lunatic_MonthView);
    }

    public MonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, R.style.lunatic_MonthView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MonthView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.lunatic_MonthView, defStyleAttr, defStyleRes);

        int cellHeight =
                a.getDimensionPixelSize(R.styleable.lunatic_MonthView_lunatic_cellHeight, 0);
        int cellWidth =
                a.getDimensionPixelSize(R.styleable.lunatic_MonthView_lunatic_cellWidth, 0);

        boolean drawGrid =
                a.getBoolean(R.styleable.lunatic_MonthView_lunatic_drawGrid, false);
        int gridStrokeColor =
                a.getColor(R.styleable.lunatic_MonthView_lunatic_gridColor, 0xffcccccc);
        float gridStrokeWidth =
                a.getDimension(R.styleable.lunatic_MonthView_lunatic_gridStroke, 1f);

        int textAppearanceDayRes =
                a.getResourceId(R.styleable.lunatic_MonthView_lunatic_textAppearanceDay, 0);
        int textAppearanceMonthRes =
                a.getResourceId(R.styleable.lunatic_MonthView_lunatic_textAppearanceMonth, 0);
        int textAppearanceWeekdayRes =
                a.getResourceId(R.styleable.lunatic_MonthView_lunatic_textAppearanceWeekday, 0);

        a.recycle();

        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;

        this.drawGrid = drawGrid;
        gridPaint.setColor(gridStrokeColor);
        gridPaint.setStrokeWidth(gridStrokeWidth);

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

    void setStaticOptions(int firstDayOfWeek, Locale locale, String headerFormat,
            String[] weekdayLabels, Lunatic.SelectionListener listener) {
        this.firstDayOfWeek = firstDayOfWeek;
        this.headerFormat = new SimpleDateFormat(headerFormat, locale);
        dayFormat = (NumberFormat) this.headerFormat.getNumberFormat().clone();

        this.weekdayLabels = new String[7];
        System.arraycopy(weekdayLabels, 0, this.weekdayLabels, 0, 7);
        if (textAllCaps[WEEKDAY_PAINT]) {
            for (int i = 0; i < 7; i++) {
                this.weekdayLabels[i] = this.weekdayLabels[i].toUpperCase();
            }
        }

        this.listener = listener;
    }

    void bind(final Lunatic.YearMonth month, final boolean[] enabledDays) {
        cellCount = enabledDays.length;

        int firstWeekday = month.day(1).weekday();
        cellOffset = (firstWeekday < firstDayOfWeek ? (firstWeekday + 7) : firstWeekday)
                - firstDayOfWeek;

        rowCount = (int) Math.ceil((double) (cellCount + cellOffset) / 7d);
        this.enabledDays = enabledDays;

        headerFormat.getCalendar().set(month.year(), month.month() - 1, 1, 0, 0, 0);
        yearMonthLabel = headerFormat.format(headerFormat.getCalendar().getTime());

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

        firstDayOfWeek = 7;
        yearMonthLabel = "November 2015";
        weekdayLabels = new String[] { "M", "T", "W", "T", "F", "S", "S" };
        dayFormat = NumberFormat.getInstance();

        requestLayout();
        invalidate();
    }

    // TODO bind highlights

    @Override
    protected int getSuggestedMinimumWidth() {
        return getPaddingLeft() + getPaddingRight() + cellWidth * 7;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                cellHeight * (rowCount + 2) + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        bounds.left = getPaddingLeft();
        bounds.top = getPaddingTop();
        bounds.right = w - getPaddingRight();
        bounds.bottom = h - getPaddingBottom();

        if (cellWidth == 0) {
            cellWidth = bounds.width() / 7;
        }

        if (cellHeight <= 0 && rowCount > 0) {
            cellHeight = bounds.height() / rowCount;
        }

        // Center view in bounds
        offsetX = (bounds.width() - cellWidth * 7) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawGrid) {
            drawGrid(canvas);
        }
        drawHeader(canvas);
        drawWeekdayLabels(
                canvas,
                bounds.left + offsetX,
                bounds.top + cellHeight + rowCenter(0) + textOffsetY[WEEKDAY_PAINT]);
        drawDayLabels(canvas,
                bounds.left + offsetX,
                bounds.top + cellHeight * 2 + textOffsetY[DAY_PAINT]);

    }

    protected void drawHeader(Canvas canvas) {
        canvas.drawText(
                yearMonthLabel,
                bounds.centerX(),
                bounds.top + rowCenter(0) + textOffsetY[MONTH_PAINT],
                monthPaint);
    }

    protected void drawWeekdayLabels(Canvas canvas, int offsetX, int offsetY) {
        for (int col = 0; col < 7; col++) {
            canvas.drawText(
                    weekdayLabels[(col + firstDayOfWeek - 1) % 7],
                    offsetX + colCenter(col),
                    offsetY,
                    weekdayPaint);
        }
    }

    protected void drawDayLabels(Canvas canvas, int offsetX, int offsetY) {
        // Draw row-by-row.
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < 7; col++) {
                int day = dayAt(row, col);
                if (day < 1 || day > cellCount) {
                    continue;
                }
                drawDayLabel(canvas, day, offsetX + colCenter(col), offsetY + rowCenter(row));
            }
        }
    }

    protected void drawDayLabel(Canvas canvas, int day, int x, int y) {
        if (enabledDays[day - 1]) {
            dayPaint.setColor(textColorEnabled);
        } else {
            dayPaint.setColor(textColorDisabled);
        }

        canvas.drawText(dayFormat.format(day), x, y, dayPaint);
    }

    protected void drawGrid(Canvas canvas) {
        for (int row = 0; row < rowCount + 2; row++) {
            canvas.drawLine(
                    bounds.left + offsetX,
                    bounds.top + cellHeight + rowTop(row),
                    bounds.right - offsetX,
                    bounds.top + cellHeight + rowTop(row),
                    gridPaint);
        }

        float adjust = gridPaint.getStrokeWidth() / 2;
        for (int col = 0; col < 8; col++) {
            canvas.drawLine(
                    bounds.left + colLeft(col) + offsetX,
                    bounds.top + cellHeight - adjust,
                    bounds.left + colLeft(col) + offsetX,
                    bounds.bottom + adjust,
                    gridPaint);
        }
    }

    /**
     * Return the day number at the specified calendar grid coordinate.
     * <p />
     * Note: This result is 1-indexed!
     */
    private int dayAt(int row, int col) {
        return (row * 7) + col - cellOffset + 1;
    }

    private int colLeft(int col) {
        return col * cellWidth;
    }

    private int colCenter(int col) {
        return colLeft(col) + cellWidth / 2;
    }

    private int rowTop(int row) {
        return row * cellHeight;
    }

    private int rowCenter(int row) {
        return rowTop(row) + cellHeight / 2;
    }

    protected void setPaintTextAppearance(int paintIndex, Paint paint, int textAppearanceResId) {
        if (paint == null || textAppearanceResId <= 0) {
            return;
        }

        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                textAppearanceResId, TEXT_APPEARANCE_ATTRS);

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
                textColorActive = textColor.getColorForState(STATE_ACTIVE,
                        android.R.color.holo_blue_bright);
                textColorEnabled = textColor.getColorForState(STATE_ENABLED,
                        android.R.color.black);
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

    private void setPaintTypefaceFromAttrs(Paint paint, String familyName,
            int typefaceIndex, int styleIndex) {
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
