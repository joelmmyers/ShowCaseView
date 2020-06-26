package smartdevelop.ir.eram.showcaseviewlib;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Spannable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

/**
 * Created by Mohammad Reza Eram on 20/01/2018.
 */

public class GuideView extends FrameLayout {

    static final String TAG = "GuideView";

    private static final int INDICATOR_HEIGHT = 40;
    private static final int MESSAGE_VIEW_PADDING = 5;
    private static final int SIZE_ANIMATION_DURATION = 700;
    private static final int APPEARING_ANIMATION_DURATION = 400;
    private static final int CIRCLE_INDICATOR_SIZE = 6;
    private static final int LINE_INDICATOR_WIDTH_SIZE = 3;
    private static final int STROKE_CIRCLE_INDICATOR_SIZE = 3;
    private static final int RADIUS_SIZE_TARGET_RECT = 15;
    private static final int MARGIN_INDICATOR = 15;

    private static final int DIM_COLOR_DEFAULT = 0x99000000;
    private static final int CIRCLE_INNER_INDICATOR_COLOR = 0xffcccccc;
    private static final int CIRCLE_INDICATOR_COLOR = Color.WHITE;
    private static final int LINE_INDICATOR_COLOR = Color.WHITE;

    private final Paint dimPaint = new Paint();
    private final Paint linePaint = new Paint();
    private final Paint circlePaint = new Paint();
    private final Paint circleInnerPaint = new Paint();
    private final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private View target;
    private RectF targetRect;
    private final Rect selfRect = new Rect();

    private float density, stopY;
    private boolean isMessageAtTop;
    private boolean mIsShowing;
    private int yMessageView;

    private float startYLineAndCircle;
    private float circleIndicatorSize;
    private float circleIndicatorSizeFinal;
    private float circleInnerIndicatorSize;
    private float lineIndicatorWidthSize;
    private int messageViewPadding;
    private float guideMargin;
    private float circleStrokeWidth;
    private float indicatorHeight;

    private boolean isPerformedAnimationSize = false;

    private GuideListener mGuideListener;
    private Gravity mGravity;
    private DismissType dismissType;
    private GuideMessageView mMessageView;

    public GuideView(Context context) {
        this(context, null);
    }

    public GuideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setup() {
        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;

        initParams();
        initPaints();

        initMessageView();

        post(new Runnable() {
            @Override
            public void run() {
                startAnimationSize();
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        initTargetViewLocation();
        setMessageLocation(resolveMessageViewLocation());

        selfRect.set(getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());

        guideMargin = (int) (isMessageAtTop ? guideMargin : -guideMargin);
        startYLineAndCircle = (isMessageAtTop ? targetRect.bottom : targetRect.top) + guideMargin;
        stopY = yMessageView + indicatorHeight;
    }

    private void initParams() {
        lineIndicatorWidthSize = LINE_INDICATOR_WIDTH_SIZE * density;
        guideMargin = MARGIN_INDICATOR * density;
        indicatorHeight = INDICATOR_HEIGHT * density;
        messageViewPadding = (int) (MESSAGE_VIEW_PADDING * density);
        circleStrokeWidth = STROKE_CIRCLE_INDICATOR_SIZE * density;
        circleIndicatorSizeFinal = CIRCLE_INDICATOR_SIZE * density;
    }

    private void initPaints() {
        dimPaint.setColor(DIM_COLOR_DEFAULT);
        dimPaint.setStyle(Paint.Style.FILL);
        dimPaint.setAntiAlias(true);

        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(LINE_INDICATOR_COLOR);
        linePaint.setStrokeWidth(lineIndicatorWidthSize);
        linePaint.setAntiAlias(true);

        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(CIRCLE_INDICATOR_COLOR);
        circlePaint.setStrokeCap(Paint.Cap.ROUND);
        circlePaint.setStrokeWidth(circleStrokeWidth);
        circlePaint.setAntiAlias(true);

        circleInnerPaint.setStyle(Paint.Style.FILL);
        circleInnerPaint.setColor(CIRCLE_INNER_INDICATOR_COLOR);
        circleInnerPaint.setAntiAlias(true);

        targetPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        targetPaint.setAntiAlias(true);
    }

    private void initTargetViewLocation() {
        int[] locationTarget = new int[2];
        target.getLocationOnScreen(locationTarget);
        targetRect = new RectF(locationTarget[0],
                locationTarget[1],
                locationTarget[0] + target.getWidth(),
                locationTarget[1] + target.getHeight());
    }

    private void initMessageView() {
        mMessageView = new GuideMessageView(getContext());
        mMessageView.setPadding(
                messageViewPadding, messageViewPadding,
                messageViewPadding, messageViewPadding);
        mMessageView.setColor(Color.WHITE);

        ViewGroup.LayoutParams messageLayoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        addView(mMessageView, messageLayoutParams);
    }

    //TODO refactor
    private int getNavigationBarSize() {
        Resources resources = getContext().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    //TODO move to utils
    private boolean isLandscape() {
        int display_mode = getResources().getConfiguration().orientation;
        return display_mode != Configuration.ORIENTATION_PORTRAIT;
    }

    //TODO cleanup
    private void startAnimationSize() {
        if (isPerformedAnimationSize) return;

        final ValueAnimator circleSizeAnimator = ValueAnimator.ofFloat(0f, circleIndicatorSizeFinal);
        circleSizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                circleIndicatorSize = (float) circleSizeAnimator.getAnimatedValue();
                circleInnerIndicatorSize = (float) circleSizeAnimator.getAnimatedValue() - density;
                postInvalidate();
            }
        });

        final ValueAnimator linePositionAnimator = ValueAnimator.ofFloat(stopY, startYLineAndCircle);
        linePositionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                startYLineAndCircle = (float) linePositionAnimator.getAnimatedValue();
                postInvalidate();
            }
        });

        linePositionAnimator.setDuration(SIZE_ANIMATION_DURATION);
        linePositionAnimator.start();
        linePositionAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                circleSizeAnimator.setDuration(SIZE_ANIMATION_DURATION);
                circleSizeAnimator.start();
                isPerformedAnimationSize = true;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if (target == null) return;

        canvas.drawRect(selfRect, dimPaint);

        final float x = (targetRect.left / 2 + targetRect.right / 2);

        canvas.drawLine(x,
                startYLineAndCircle,
                x,
                stopY,
                linePaint);

        canvas.drawCircle(x, startYLineAndCircle, circleIndicatorSize, circlePaint);
        canvas.drawCircle(x, startYLineAndCircle, circleInnerIndicatorSize, circleInnerPaint);

        canvas.drawRoundRect(targetRect, RADIUS_SIZE_TARGET_RECT, RADIUS_SIZE_TARGET_RECT, targetPaint);
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void dismiss() {
        ((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).removeView(this);
        mIsShowing = false;
        if (mGuideListener != null) {
            mGuideListener.onDismiss(target);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handleViewTouched(x, y);
            return true;
        }
        return false;
    }

    private void handleViewTouched(float x, float y) {
        switch (dismissType) {
            case outside:
                if (!isViewContains(mMessageView, x, y)) {
                    dismiss();
                }
                break;

            case anywhere:
                dismiss();
                break;

            case targetView:
                if (targetRect.contains(x, y)) {
                    target.performClick();
                    dismiss();
                }
                break;

            case selfView:
                if (isViewContains(mMessageView, x, y)) {
                    dismiss();
                }
                break;
        }
    }

    //TODO move to utils
    private boolean isViewContains(View view, float rx, float ry) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        int w = view.getWidth();
        int h = view.getHeight();

        return !(rx < x || rx > x + w || ry < y || ry > y + h);
    }

    private void setMessageLocation(Point p) {
        mMessageView.setX(p.x);
        mMessageView.setY(p.y);
        postInvalidate();
    }

    public void updateGuideViewLocation() {
        requestLayout();
    }

    private Point resolveMessageViewLocation() {
        int xMessageView;
        if (mGravity == Gravity.center) {
            xMessageView = (int) (targetRect.left - mMessageView.getWidth() / 2 + target.getWidth() / 2);
        } else
            xMessageView = (int) (targetRect.right) - mMessageView.getWidth();

        if (isLandscape()) {
            xMessageView -= getNavigationBarSize();
        }

        if (xMessageView + mMessageView.getWidth() > getWidth())
            xMessageView = getWidth() - mMessageView.getWidth();
        if (xMessageView < 0)
            xMessageView = 0;


        //set message view bottom
        if (targetRect.top + (indicatorHeight) > getHeight() / 2f) {
            isMessageAtTop = false;
            yMessageView = (int) (targetRect.top - mMessageView.getHeight() - indicatorHeight);
        }
        //set message view top
        else {
            isMessageAtTop = true;
            yMessageView = (int) (targetRect.top + target.getHeight() + indicatorHeight);
        }

        if (yMessageView < 0)
            yMessageView = 0;

        return new Point(xMessageView, yMessageView);
    }


    public void show() {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        this.setLayoutParams(layoutParams);
        this.setClickable(false);

        ((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).addView(this);

        AlphaAnimation startAnimation = new AlphaAnimation(0.0f, 1.0f);
        startAnimation.setDuration(APPEARING_ANIMATION_DURATION);
        startAnimation.setFillAfter(true);
        this.startAnimation(startAnimation);

        mIsShowing = true;
    }

    public void setTitle(String str) {
        mMessageView.setTitle(str);
    }

    public void setContentText(String str) {
        mMessageView.setContentText(str);
    }


    public void setContentSpan(Spannable span) {
        mMessageView.setContentSpan(span);
    }

    public void setTitleTypeFace(Typeface typeFace) {
        mMessageView.setTitleTypeFace(typeFace);
    }

    public void setContentTypeFace(Typeface typeFace) {
        mMessageView.setContentTypeFace(typeFace);
    }


    public void setTitleTextSize(int size) {
        mMessageView.setTitleTextSize(size);
    }

    public void setDimColor(int color) {
        dimPaint.setColor(color);
    }

    public void setContentTextSize(int size) {
        mMessageView.setContentTextSize(size);
    }


    public static class Builder {
        private View targetView;
        private String title, contentText;
        private Gravity gravity;
        private DismissType dismissType;
        private Context context;
        private Spannable contentSpan;
        private Typeface titleTypeFace, contentTypeFace;
        private GuideListener guideListener;
        private int titleTextSize;
        private int contentTextSize;
        private int dimColor;
        private float lineIndicatorHeight;
        private float lineIndicatorWidthSize;
        private float circleIndicatorSize;
        private float circleInnerIndicatorSize;
        private float strokeCircleWidth;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTargetView(View view) {
            this.targetView = view;
            return this;
        }

        /**
         * gravity GuideView
         *
         * @param gravity it should be one type of Gravity enum.
         **/
        public Builder setGravity(Gravity gravity) {
            this.gravity = gravity;
            return this;
        }

        /**
         * defining a title
         *
         * @param title a title. for example: submit button.
         **/
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * defining a description for the target view
         *
         * @param contentText a description. for example: this button can for submit your information..
         **/
        public Builder setContentText(String contentText) {
            this.contentText = contentText;
            return this;
        }

        /**
         * setting spannable type
         *
         * @param span a instance of spannable
         **/
        public Builder setContentSpan(Spannable span) {
            this.contentSpan = span;
            return this;
        }

        /**
         * setting font type face
         *
         * @param typeFace a instance of type face (font family)
         **/
        public Builder setContentTypeFace(Typeface typeFace) {
            this.contentTypeFace = typeFace;
            return this;
        }

        /**
         * adding a listener on show case view
         *
         * @param guideListener a listener for events
         **/
        public Builder setGuideListener(GuideListener guideListener) {
            this.guideListener = guideListener;
            return this;
        }

        /**
         * setting font type face
         *
         * @param typeFace a instance of type face (font family)
         **/
        public Builder setTitleTypeFace(Typeface typeFace) {
            this.titleTypeFace = typeFace;
            return this;
        }

        /**
         * the defined text size overrides any defined size in the default or provided style
         *
         * @param size title text by sp unit
         * @return builder
         */
        public Builder setContentTextSize(int size) {
            this.contentTextSize = size;
            return this;
        }

        /**
         * Changes default dim color
         *
         * @param color dim color
         * @return builder
         */
        public Builder setDimColor(int color) {
            this.dimColor = color;
            return this;
        }

        /**
         * the defined text size overrides any defined size in the default or provided style
         *
         * @param size title text by sp unit
         * @return builder
         */
        public Builder setTitleTextSize(int size) {
            this.titleTextSize = size;
            return this;
        }

        /**
         * this method defining the type of dismissing function
         *
         * @param dismissType should be one type of DismissType enum. for example: outside -> Dismissing with click on outside of MessageView
         */
        public Builder setDismissType(DismissType dismissType) {
            this.dismissType = dismissType;
            return this;
        }

        /**
         * changing line height indicator
         *
         * @param height you can change height indicator (Converting to Dp)
         */
        public Builder setIndicatorHeight(float height) {
            this.lineIndicatorHeight = height;
            return this;
        }

        /**
         * changing line width indicator
         *
         * @param width you can change width indicator
         */
        public Builder setIndicatorWidthSize(float width) {
            this.lineIndicatorWidthSize = width;
            return this;
        }

        /**
         * changing circle size indicator
         *
         * @param size you can change circle size indicator
         */
        public Builder setCircleIndicatorSize(float size) {
            this.circleIndicatorSize = size;
            return this;
        }

        /**
         * changing inner circle size indicator
         *
         * @param size you can change inner circle indicator size
         */
        public Builder setCircleInnerIndicatorSize(float size) {
            this.circleInnerIndicatorSize = size;
            return this;
        }

        /**
         * changing stroke circle size indicator
         *
         * @param size you can change stroke circle indicator size
         */
        public Builder setCircleStrokeIndicatorSize(float size) {
            this.strokeCircleWidth = size;
            return this;
        }


        public GuideView build() {
            GuideView guideView = new GuideView(context);
            guideView.target = targetView;
            guideView.mGravity = gravity != null ? gravity : Gravity.auto;
            guideView.dismissType = dismissType != null ? dismissType : DismissType.targetView;
            float density = context.getResources().getDisplayMetrics().density;

            if (guideListener != null) {
                guideView.mGuideListener = guideListener;
            }
            if (lineIndicatorHeight != 0) {
                guideView.indicatorHeight = lineIndicatorHeight * density;
            }
            if (lineIndicatorWidthSize != 0) {
                guideView.lineIndicatorWidthSize = lineIndicatorWidthSize * density;
            }
            if (circleIndicatorSize != 0) {
                guideView.circleIndicatorSize = circleIndicatorSize * density;
            }
            if (circleInnerIndicatorSize != 0) {
                guideView.circleInnerIndicatorSize = circleInnerIndicatorSize * density;
            }
            if (strokeCircleWidth != 0) {
                guideView.circleStrokeWidth = strokeCircleWidth * density;
            }

            guideView.init();

            guideView.setTitle(title);
            if (contentText != null)
                guideView.setContentText(contentText);
            if (titleTextSize != 0)
                guideView.setTitleTextSize(titleTextSize);
            if(dimColor != 0)
                guideView.setDimColor(dimColor);
            if (contentTextSize != 0)
                guideView.setContentTextSize(contentTextSize);
            if (contentSpan != null)
                guideView.setContentSpan(contentSpan);
            if (titleTypeFace != null) {
                guideView.setTitleTypeFace(titleTypeFace);
            }
            if (contentTypeFace != null) {
                guideView.setContentTypeFace(contentTypeFace);
            }

            return guideView;
        }

    }
}