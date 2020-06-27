package smartdevelop.ir.eram.showcaseviewlib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Spannable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Mohammad Reza Eram  on 20/01/2018.
 */

class GuideMessageView extends LinearLayout {

    private static final int COLOR = Color.WHITE;
    private static final int PADDING = 10;
    private static final int CORNERS = 15;
    private static final int SPACE_BETWEEN = 6;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mRect = new RectF();

    private TextView mTitleTextView;
    private TextView mContentTextView;

    // TODO customize padding and margins
    GuideMessageView(Context context) {
        super(context);

        setWillNotDraw(false);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        initPaints();

        initViews();

        setColor(COLOR);
    }

    private void initPaints() {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        mStrokePaint.setStrokeWidth(0);
    }

    private void initViews() {
        float density = getResources().getDisplayMetrics().density;

        final int padding = (int) (PADDING * density);
        final int spacingBetween = (int) (SPACE_BETWEEN * density);

        setPadding(padding, padding, padding, padding);

        mTitleTextView = new TextView(getContext());
        mTitleTextView.setGravity(Gravity.CENTER);
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        mTitleTextView.setTextColor(Color.BLACK);
        LayoutParams titleLayoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleLayoutParams.setMargins(0, 0, 0, spacingBetween);
        addView(mTitleTextView, titleLayoutParams);

        mContentTextView = new TextView(getContext());
        mContentTextView.setTextColor(Color.BLACK);
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        mContentTextView.setGravity(Gravity.CENTER);
        LayoutParams contentLayoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        addView(mContentTextView, contentLayoutParams);
    }

    public void setTitle(String title) {
        if (title == null || title.isEmpty()) {
            mTitleTextView.setVisibility(View.GONE);
            return;
        } else {
            mTitleTextView.setVisibility(View.VISIBLE);
        }

        mTitleTextView.setText(title);
    }

    public void setContentText(String content) {
        mContentTextView.setText(content);
    }

    public void setContentSpan(Spannable content) {
        mContentTextView.setText(content);
    }

    public void setContentTypeFace(Typeface typeFace) {
        mContentTextView.setTypeface(typeFace);
    }

    public void setTitleTypeFace(Typeface typeFace) {
        mTitleTextView.setTypeface(typeFace);
    }

    public void setTitleTextSize(int size) {
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setContentTextSize(int size) {
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
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

    public void setTitleColor(int color) {
        mTitleTextView.setTextColor(color);
    }

    public void setContentColor(int color) {
        mContentTextView.setTextColor(color);
    }

    private int[] location = new int[2];

    // TODO customizable radius
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.getLocationOnScreen(location);

        mRect.set(CORNERS,
                CORNERS,
                getWidth() - CORNERS,
                getHeight() - CORNERS
        );

        canvas.drawRoundRect(mRect, CORNERS, CORNERS, mPaint);

        if (mStrokePaint.getStrokeWidth() > 0) {
            canvas.drawRoundRect(mRect, CORNERS, CORNERS, mStrokePaint);
        }
    }
}
