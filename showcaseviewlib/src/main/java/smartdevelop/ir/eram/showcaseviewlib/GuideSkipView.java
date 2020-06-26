package smartdevelop.ir.eram.showcaseviewlib;

import android.content.Context;
import android.graphics.Canvas;
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

    private static final int PADDING = 12;

    private Paint mPaint;
    private Paint mStrokePaint;
    private RectF mRect;

    private Button mSkipButton;

    GuideSkipView(Context context) {
        super(context);

        float density = getResources().getDisplayMetrics().density;

        mRect = new RectF();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        mStrokePaint.setStrokeWidth(0);

        setPadding(PADDING, PADDING, PADDING, PADDING);

        mSkipButton = new Button(context);
        mSkipButton.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, getStatusBarSize(), 0, 0);
        addView(mSkipButton, layoutParams);
    }

    public void setWithSkipButton(boolean withSkipButton) {
        int visibility;
        if (withSkipButton) visibility = View.VISIBLE;
        else visibility = View.GONE;

        setVisibility(visibility);
    }

    public void setPosition(SkipButtonPosition position) {
        int gravity = Gravity.TOP | Gravity.LEFT;
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
        }
        setPositionAsGravity(gravity);
    }

    private void setPositionAsGravity(int gravity) {
        LayoutParams params = (LayoutParams) getLayoutParams();
        params.gravity = gravity;
        setLayoutParams(params);
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

    // TODO customizable colors
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
