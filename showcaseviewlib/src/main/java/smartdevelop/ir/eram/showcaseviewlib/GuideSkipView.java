package smartdevelop.ir.eram.showcaseviewlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import smartdevelop.ir.eram.showcaseviewlib.config.SkipButtonPosition;

class GuideSkipView extends FrameLayout {

    private static final int COLOR = 0x44ffffff;
    private static final int STROKE_COLOR = Color.WHITE;
    private static final int STROKE_WIDTH = 1;
    private static final int TEXT_COLOR = Color.WHITE;

    private static final int PADDING = 12;

    private static final int PADDING_HORIZONTAL = 32;
    private static final int PADDING_VERTICAL = 10;

    private Paint mPaint;
    private Paint mStrokePaint;
    private RectF mRect;

    private Button mSkipButton;

    GuideSkipView(Context context) {
        super(context);

        setWillNotDraw(false);

        float density = getResources().getDisplayMetrics().density;

        mRect = new RectF();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        mStrokePaint.setStrokeWidth(0);

        setButtonMargin((int) (density * PADDING));

        mSkipButton = new Button(context);
        mSkipButton.setMinWidth(0);
        mSkipButton.setMinimumWidth(0);
        mSkipButton.setMinHeight(0);
        mSkipButton.setMinimumHeight(0);
        mSkipButton.setBackground(null);
        mSkipButton.setGravity(Gravity.CENTER);
        mSkipButton.setAllCaps(false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        addView(mSkipButton, layoutParams);

        setButtonPaddingHorizontal((int) (density * PADDING_HORIZONTAL));
        setButtonPaddingVertical((int) (density * PADDING_VERTICAL));
        setColor(COLOR);
        setStrokeColor(STROKE_COLOR);
        setStrokeWidth(density * STROKE_WIDTH);
        setButtonTextColor(TEXT_COLOR);
    }

    public void setWithSkipButton(boolean withSkipButton) {
        int visibility;
        if (withSkipButton) visibility = View.VISIBLE;
        else visibility = View.GONE;
        setVisibility(visibility);
    }

    public void setOnSkipListener(final OnClickListener listener) {
        mSkipButton.setOnClickListener(listener);
    }

    @SuppressLint("RtlHardcoded")
    public void setPosition(SkipButtonPosition position) {
        int gravity;
        switch (position) {
            case TOP_LEFT:
                gravity = Gravity.TOP | Gravity.LEFT;
                break;
            case TOP_RIGHT:
                gravity = Gravity.TOP | Gravity.RIGHT;
                break;
            case BOTTOM_LEFT:
                gravity = Gravity.BOTTOM | Gravity.LEFT;
                break;
            case BOTTOM_RIGHT:
                gravity = Gravity.BOTTOM | Gravity.RIGHT;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + position);
        }
        setPositionAsGravity(gravity);
    }

    private void setPositionAsGravity(int gravity) {
        LayoutParams params = (LayoutParams) getLayoutParams();
        params.gravity = gravity;
        setLayoutParams(params);
    }

    public void setButtonMargin(int margin) {
        setPadding(margin, margin + getStatusBarSize(), margin, margin);
    }

    public void setColor(int color) {
        mPaint.setAlpha(255);
        mPaint.setColor(color);
        invalidate();
    }

    public void setStrokeColor(int color) {
        mStrokePaint.setColor(color);
        invalidate();
    }

    public void setStrokeWidth(float width) {
        mStrokePaint.setStrokeWidth(width);
        invalidate();
    }

    public void setButtonPaddingHorizontal(int padding) {
        mSkipButton.setPadding(
                padding,
                mSkipButton.getPaddingTop(),
                padding,
                mSkipButton.getPaddingBottom()
        );
    }

    public void setButtonPaddingVertical(int padding) {
        mSkipButton.setPadding(
                mSkipButton.getPaddingLeft(),
                padding,
                mSkipButton.getPaddingRight(),
                padding
        );
    }

    public void setButtonText(String text) {
        mSkipButton.setText(text);
    }

    public void setButtonTypeface(Typeface typeface) {
        mSkipButton.setTypeface(typeface);
    }

    public void setButtonTextSize(int size) {
        mSkipButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setButtonTextColor(int color) {
        mSkipButton.setTextColor(color);
    }

    private int getStatusBarSize() {
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    private int[] location = new int[2];

    // TODO customizable radius
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.getLocationOnScreen(location);

        mRect.set(getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());

        canvas.drawRoundRect(mRect, 15, 15, mPaint);

        if(mStrokePaint.getStrokeWidth() > 0) {
            canvas.drawRoundRect(mRect, 15, 15, mStrokePaint);
        }
    }
}
