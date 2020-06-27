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
import android.widget.FrameLayout;

import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;
import smartdevelop.ir.eram.showcaseviewlib.config.HighlightingShape;
import smartdevelop.ir.eram.showcaseviewlib.config.MessageGravity;
import smartdevelop.ir.eram.showcaseviewlib.config.SkipButtonPosition;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideSkipListener;

/**
 * Created by Mohammad Reza Eram on 20/01/2018.
 */

public class GuideView extends FrameLayout {

    static final String TAG = "GuideView";

    private static final int INDICATOR_HEIGHT = 40;

    private static final int SIZE_ANIMATION_DURATION = 700;
    private static final int SHOW_HIDE_ANIMATION_DURATION = 400;

    private static final int CIRCLE_INDICATOR_SIZE = 6;
    private static final int LINE_INDICATOR_WIDTH_SIZE = 3;
    private static final int STROKE_CIRCLE_INDICATOR_SIZE = 3;
    private static final int RADIUS_SIZE_TARGET_RECT = 15;
    private static final int MARGIN_INDICATOR = 15;

    private static final HighlightingShape HIGHLIGHTING_SHAPE = HighlightingShape.RECTANGLE;
    private static final int DIM_COLOR = 0x99000000;
    private static final int CIRCLE_INNER_INDICATOR_COLOR = 0xffcccccc;
    private static final int CIRCLE_INDICATOR_COLOR = Color.WHITE;
    private static final int LINE_INDICATOR_COLOR = Color.WHITE;
    private static final SkipButtonPosition SKIP_BUTTON_POSITION = SkipButtonPosition.TOP_LEFT;

    private final Paint dimPaint = new Paint();
    private final Paint linePaint = new Paint();
    private final Paint circleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint circleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private View target;
    private RectF targetRect;
    private final Rect selfRect = new Rect();

    private float density, lineStartY;
    private boolean isMessageAtTop;
    private boolean mIsShowing;
    private int yMessageView;

    private float startYLineAndCircle;
    private HighlightingShape highlightingShape;
    private float highlightingRadius;
    private float circleIndicatorSize;
    private float circleIndicatorSizeFinal;
    private float circleInnerIndicatorSize;
    private float lineHeight;
    private float guideMargin;

    private boolean isPerformedAnimationSize = false;

    private GuideListener mGuideListener;
    private GuideSkipListener mGuideSkipListener;
    private Gravity mGravity;
    private MessageGravity messageGravity;
    private DismissType dismissType;

    private GuideMessageView mMessageView;
    private GuideSkipView mSkipView;

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

        setAlpha(0f);
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;

        initParams();
        initPaints();

        initMessageView();
        initSkipView();
    }

    private void startAnimation() {
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

        if(isMessageAtTop) lineStartY = yMessageView;
        else lineStartY = yMessageView + mMessageView.getMeasuredHeight();
    }

    private void initParams() {
        highlightingShape = HIGHLIGHTING_SHAPE;
        highlightingRadius = RADIUS_SIZE_TARGET_RECT;
        guideMargin = MARGIN_INDICATOR * density;
        lineHeight = INDICATOR_HEIGHT * density;
        circleIndicatorSizeFinal = CIRCLE_INDICATOR_SIZE * density;
    }

    private void initPaints() {
        dimPaint.setColor(DIM_COLOR);
        dimPaint.setStyle(Paint.Style.FILL);

        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(LINE_INDICATOR_COLOR);
        linePaint.setStrokeWidth(LINE_INDICATOR_WIDTH_SIZE * density);

        circleStrokePaint.setStyle(Paint.Style.STROKE);
        circleStrokePaint.setColor(CIRCLE_INDICATOR_COLOR);
        circleStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        circleStrokePaint.setStrokeWidth(STROKE_CIRCLE_INDICATOR_SIZE * density);

        circleFillPaint.setStyle(Paint.Style.FILL);
        circleFillPaint.setColor(CIRCLE_INNER_INDICATOR_COLOR);

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
        if (highlightingShape == HighlightingShape.CIRCLE)
            highlightingRadius = (int) calculateRadius(targetRect.height(), targetRect.width());
    }

    private void initMessageView() {
        mMessageView = new GuideMessageView(getContext());

        ViewGroup.LayoutParams messageLayoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        addView(mMessageView, messageLayoutParams);
    }

    private void initSkipView() {
        mSkipView = new GuideSkipView(getContext());

        ViewGroup.LayoutParams skipLayoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        addView(mSkipView, skipLayoutParams);

        mSkipView.setPosition(SKIP_BUTTON_POSITION);

        this.mSkipView.setOnSkipListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(true);
            }
        });
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
                circleInnerIndicatorSize = (float) circleSizeAnimator.getAnimatedValue();
                postInvalidate();
            }
        });

        final ValueAnimator linePositionAnimator = ValueAnimator.ofFloat(lineStartY, startYLineAndCircle);
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

        // Draw dim background
        canvas.drawRect(selfRect, dimPaint);

        // Cut transparent shape around target view
        if (highlightingShape == HighlightingShape.CIRCLE) {
            float circleX = (targetRect.left + targetRect.right) / 2;
            float circleY = (targetRect.top + targetRect.bottom) / 2;
            canvas.drawCircle(circleX, circleY, highlightingRadius, targetPaint);
        } else {
            canvas.drawRoundRect(
                    targetRect,
                    highlightingRadius,
                    highlightingRadius,
                    targetPaint);
        }

        // Draw line and circle
        final float x = (targetRect.left / 2 + targetRect.right / 2);

        //Density is necessary to avoid padding differences
        float lineYStart = lineStartY + density;

        canvas.drawLine(x,
                startYLineAndCircle,
                x,
                lineYStart,
                linePaint);

        canvas.drawCircle(x, startYLineAndCircle, circleIndicatorSize, circleStrokePaint);
        canvas.drawCircle(x, startYLineAndCircle, circleInnerIndicatorSize, circleFillPaint);
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
        int messageWidth = mMessageView.getMeasuredWidth();
        int xMessageView;
        if (mGravity == Gravity.center) {
            xMessageView = (int) (targetRect.left - messageWidth / 2 + target.getWidth() / 2);
        } else
            xMessageView = (int) (targetRect.right) - messageWidth;

        if (isLandscape()) {
            xMessageView -= getNavigationBarSize();
        }

        if (xMessageView + messageWidth > getMeasuredWidth())
            xMessageView = getMeasuredWidth() - messageWidth;
        if (xMessageView < 0)
            xMessageView = 0;

        resolveMessagePositionY();

        return new Point(xMessageView, yMessageView);
    }

    private void resolveMessagePositionY() {
        int messageHeight = mMessageView.getMeasuredHeight();
        switch (messageGravity) {
            case AUTO: {
                if (targetRect.top + (lineHeight) > getMeasuredHeight() / 2f) {
                    isMessageAtTop = false;
                    yMessageView = (int) (targetRect.top - messageHeight - lineHeight);
                } else {
                    isMessageAtTop = true;
                    yMessageView = (int) (targetRect.top + target.getHeight() + lineHeight);
                }
                break;
            }
            case TOP: {
                isMessageAtTop = false;
                yMessageView = (int) (targetRect.top - messageHeight - lineHeight);
                break;
            }
            case BOTTOM: {
                isMessageAtTop = true;
                yMessageView = (int) (targetRect.top + target.getHeight() + lineHeight);
                break;
            }
        }

        if (yMessageView < 0)
            yMessageView = 0;
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void show() {
        if (mIsShowing) return;

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        this.setLayoutParams(layoutParams);
        this.setClickable(false);

        ((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).addView(this);

        animate()
                .alpha(1f)
                .setDuration(SHOW_HIDE_ANIMATION_DURATION);

        mIsShowing = true;
    }

    public void dismiss() {
        dismiss(false);
    }

    public void dismiss(final boolean isSkip) {
        if (!mIsShowing) return;

        animate()
                .alpha(0f)
                .setDuration(SHOW_HIDE_ANIMATION_DURATION)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        dismissSelf(isSkip);
                    }
                })
                .start();

        mIsShowing = false;
    }

    private void dismissSelf(boolean isSkip) {
        ((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).removeView(GuideView.this);
        if (isSkip) {
            if (mGuideSkipListener != null) mGuideSkipListener.onSkip(GuideView.this);
        } else {
            if (mGuideListener != null) mGuideListener.onDismiss(target);
        }
    }

    public void setHighlightingShape(HighlightingShape highlightingShape) {
        this.highlightingShape = highlightingShape;
    }

    public void setHighlightingRadius(float radius) {
        if (highlightingShape != HighlightingShape.CIRCLE) {
            highlightingRadius = radius;
        }
        invalidate();
    }

    public void setTitle(String str) {
        mMessageView.setTitle(str);
    }

    public void setMessagePadding(int padding) {
        mMessageView.setPadding(padding, padding, padding, padding);
    }

    public void setContentText(String str) {
        mMessageView.setContentText(str);
    }

    public void setLineWidth(float width) {
        linePaint.setStrokeWidth(width);
    }

    public void setLineHeight(float height) {
        lineHeight = height * density;
    }

    public void setLineColor(int color) {
        linePaint.setColor(color);
    }

    public void setCircleColor(int color) {
        circleFillPaint.setColor(color);
    }

    public void setCircleStrokeColor(int color) {
        circleStrokePaint.setColor(color);
    }

    public void setCircleStrokeWidth(float width) {
        circleStrokePaint.setStrokeWidth(width);
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

    public void setMessageGravity(MessageGravity messageGravity) {
        this.messageGravity = messageGravity;
    }

    public void setMessageBackgroundColor(int color) {
        this.mMessageView.setColor(color);
    }

    public void setMessageTitleColor(int color) {
        this.mMessageView.setTitleColor(color);
    }

    public void setMessageContentColor(int color) {
        this.mMessageView.setContentColor(color);
    }

    public void setMessageStrokeColor(int color) {
        this.mMessageView.setStrokeColor(color);
    }

    public void setMessageStrokeWidth(float width) {
        this.mMessageView.setStrokeWidth(width);
    }

    private void setWithSkipButton(boolean withSkipButton) {
        this.mSkipView.setWithSkipButton(withSkipButton);
    }

    private void setSkipButtonPosition(SkipButtonPosition position) {
        this.mSkipView.setPosition(position);
    }

    private void setSkipButtonMargin(int margin) {
        this.mSkipView.setButtonMargin(margin);
    }

    private void setSkipButtonPaddingHorizontal(int padding) {
        this.mSkipView.setButtonPaddingHorizontal(padding);
    }

    private void setSkipButtonPaddingVertical(int padding) {
        this.mSkipView.setButtonPaddingVertical(padding);
    }

    private void setSkipButtonColor(int color) {
        this.mSkipView.setColor(color);
    }

    private void setSkipButtonStrokeColor(int color) {
        this.mSkipView.setStrokeColor(color);
    }

    private void setSkipButtonStrokeWidth(float width) {
        this.mSkipView.setStrokeWidth(width);
    }

    private void setSkipButtonText(String text) {
        this.mSkipView.setButtonText(text);
    }

    private void setSkipButtonTypeface(Typeface typeface) {
        this.mSkipView.setButtonTypeface(typeface);
    }

    private void setSkipButtonTextSize(int size) {
        this.mSkipView.setButtonTextSize(size);
    }

    private void setSkipButtonTextColor(int color) {
        this.mSkipView.setButtonTextColor(color);
    }

    private float calculateRadius(float height, float width) {
        return (float) Math.sqrt(Math.pow(height, 2) + Math.pow(width, 2)) / 2;
    }

    public static class Builder {
        private View targetView;
        private String title, contentText;
        private Gravity gravity;
        private MessageGravity messageGravity;
        private DismissType dismissType;
        private Context context;
        private Spannable contentSpan;
        private Typeface titleTypeFace, contentTypeFace;
        private GuideListener guideListener;
        private int dimColor;
        private float lineHeight;
        private float lineWidth;
        private int lineColor;
        private float circleSize;
        private float circleStrokeWidth;
        private int circleColor;
        private int circleStrokeColor;
        private float highlightingRadius;
        private HighlightingShape highlightingShape;
        private int messagePadding = -1;
        private int titleTextSize;
        private int contentTextSize;
        private int messageBackgroundColor;
        private int messageTitleColor;
        private int messageContentColor;
        private int messageStrokeColor;
        private float messageStrokeWidth;
        private boolean withSkipButton;
        private GuideSkipListener guideSkipListener;
        private SkipButtonPosition skipButtonPosition;
        private int skipButtonMargin;
        private int skipButtonPaddingHorizontal;
        private int skipButtonPaddingVertical;
        private int skipButtonColor;
        private int skipButtonStrokeColor;
        private float skipButtonStrokeWidth;
        private String skipButtonText;
        private Typeface skipButtonTypeface;
        private int skipButtonTextSize;
        private int skipButtonTextColor;

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
         * Set position of message on screen in relation to highlighted view
         * If gravity is AUTO, the message position is calculated automatically
         *
         * @param messageGravity message position
         */
        public Builder setMessageGravity(MessageGravity messageGravity) {
            this.messageGravity = messageGravity;
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
        public Builder setLineHeight(float height) {
            this.lineHeight = height;
            return this;
        }

        /**
         * changing line width indicator
         *
         * @param width you can change width indicator
         */
        public Builder setLineWidth(float width) {
            this.lineWidth = width;
            return this;
        }

        //TODO Documentation
        public Builder setLineColor(int color) {
            this.lineColor = color;
            return this;
        }

        /**
         * changing circle size indicator
         *
         * @param size you can change circle size indicator
         */
        public Builder setCircleSize(float size) {
            this.circleSize = size;
            return this;
        }

        /**
         * changing stroke circle size indicator
         *
         * @param size you can change stroke circle indicator size
         */
        public Builder setCircleStrokeWidth(float size) {
            this.circleStrokeWidth = size;
            return this;
        }

        public Builder setCircleColor(int color) {
            this.circleColor = color;
            return this;
        }

        public Builder setCircleStrokeColor(int color) {
            this.circleStrokeColor = color;
            return this;
        }

        public Builder setHighlightingRadius(float radius) {
            this.highlightingRadius = radius;
            return this;
        }

        public Builder setHighlightingShape(HighlightingShape highlightedShape) {
            this.highlightingShape = highlightedShape;
            return this;
        }

        public Builder setMessagePadding(int padding) {
            this.messagePadding = padding;
            return this;
        }

        public Builder setTitleTextSize(int size) {
            this.titleTextSize = size;
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
         * Changes default message box background color
         *
         * @param color background color
         * @return builder
         */
        public Builder setMessageBackgroundColor(int color) {
            this.messageBackgroundColor = color;
            return this;
        }

        public Builder setMessageTitleColor(int color) {
            this.messageTitleColor = color;
            return this;
        }

        public Builder setMessageContentColor(int color) {
            this.messageContentColor = color;
            return this;
        }

        public Builder setMessageStrokeColor(int color) {
            this.messageStrokeColor = color;
            return this;
        }

        public Builder setMessageStrokeWidth(float width) {
            this.messageStrokeWidth = width;
            return this;
        }

        public Builder setWithSkipButton(boolean withSkipButton) {
            this.withSkipButton = withSkipButton;
            return this;
        }

        public Builder setGuideSkipListener(GuideSkipListener listener) {
            this.guideSkipListener = listener;
            return this;
        }

        public Builder setSkipButtonPosition(SkipButtonPosition position) {
            this.skipButtonPosition = position;
            return this;
        }

        public Builder setSkipButtonMargin(int margin) {
            this.skipButtonMargin = margin;
            return this;
        }

        public Builder setSkipButtonPaddingHorizontal(int padding) {
            this.skipButtonPaddingHorizontal = padding;
            return this;
        }

        public Builder setSkipButtonPaddingVertical(int padding) {
            this.skipButtonPaddingVertical = padding;
            return this;
        }

        public Builder setSkipButtonColor(int color) {
            this.skipButtonColor = color;
            return this;
        }

        public Builder setSkipButtonStrokeColor(int color) {
            this.skipButtonStrokeColor = color;
            return this;
        }

        public Builder setSkipButtonStrokeWidth(float width) {
            this.skipButtonStrokeWidth = width;
            return this;
        }

        public Builder setSkipButtonText(String text) {
            this.skipButtonText = text;
            return this;
        }

        public Builder setSkipButtonTypeface(Typeface typeface) {
            this.skipButtonTypeface = typeface;
            return this;
        }

        public Builder setSkipButtonTextSize(int size) {
            this.skipButtonTextSize = size;
            return this;
        }

        public Builder setSkipButtonTextColor(int color) {
            this.skipButtonTextColor = color;
            return this;
        }

        public GuideView build() {
            GuideView guideView = new GuideView(context);
            guideView.target = targetView;
            guideView.mGravity = gravity != null ? gravity : Gravity.auto;
            guideView.messageGravity = messageGravity != null ? messageGravity : MessageGravity.AUTO;
            guideView.dismissType = dismissType != null ? dismissType : DismissType.targetView;

            if (guideListener != null) {
                guideView.mGuideListener = guideListener;
            }
            if (dimColor != 0)
                guideView.setDimColor(dimColor);

            guideView.init();

            guideView.setTitle(title);

            if (highlightingShape != null) {
                guideView.setHighlightingShape(highlightingShape);
            }
            if (highlightingRadius != 0) {
                guideView.setHighlightingRadius(highlightingRadius);
            }
            if (circleSize != 0)
                guideView.circleIndicatorSizeFinal = circleSize;
            if (circleStrokeWidth != 0)
                guideView.setCircleStrokeWidth(circleStrokeWidth);
            if (lineColor != 0)
                guideView.setLineColor(lineColor);
            if (lineHeight != 0)
                guideView.setLineHeight(lineHeight);
            if (lineWidth != 0)
                guideView.setLineWidth(lineWidth);
            if (circleStrokeColor != 0)
                guideView.setCircleStrokeColor(circleStrokeColor);
            if (circleColor != 0)
                guideView.setCircleColor(circleColor);
            if (messagePadding >= 0)
                guideView.setMessagePadding(messagePadding);
            if (contentText != null)
                guideView.setContentText(contentText);
            if (titleTextSize != 0)
                guideView.setTitleTextSize(titleTextSize);
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
            if (messageBackgroundColor != 0) {
                guideView.setMessageBackgroundColor(messageBackgroundColor);
            }
            if (messageTitleColor != 0) {
                guideView.setMessageTitleColor(messageTitleColor);
            }
            if (messageContentColor != 0) {
                guideView.setMessageContentColor(messageContentColor);
            }
            if (messageStrokeColor != 0) {
                guideView.setMessageStrokeColor(messageStrokeColor);
            }
            if (messageStrokeWidth != 0) {
                guideView.setMessageStrokeWidth(messageStrokeWidth);
            }
            guideView.setWithSkipButton(withSkipButton);
            guideView.mGuideSkipListener = guideSkipListener;
            if (skipButtonPosition != null) {
                guideView.setSkipButtonPosition(skipButtonPosition);
            }
            if (skipButtonMargin != 0) {
                guideView.setSkipButtonMargin(skipButtonMargin);
            }
            if (skipButtonPaddingHorizontal != 0) {
                guideView.setSkipButtonPaddingHorizontal(skipButtonPaddingHorizontal);
            }
            if (skipButtonPaddingVertical != 0) {
                guideView.setSkipButtonPaddingVertical(skipButtonPaddingVertical);
            }
            if (skipButtonColor != 0) {
                guideView.setSkipButtonColor(skipButtonColor);
            }
            if (skipButtonStrokeColor != 0) {
                guideView.setSkipButtonStrokeColor(skipButtonStrokeColor);
            }
            if (skipButtonStrokeWidth != 0) {
                guideView.setSkipButtonStrokeWidth(skipButtonStrokeWidth);
            }
            if (skipButtonText != null) {
                guideView.setSkipButtonText(skipButtonText);
            }
            if (skipButtonTypeface != null) {
                guideView.setSkipButtonTypeface(skipButtonTypeface);
            }
            if (skipButtonTextSize != 0) {
                guideView.setSkipButtonTextSize(skipButtonTextSize);
            }
            if (skipButtonTextColor != 0) {
                guideView.setSkipButtonTextColor(skipButtonTextColor);
            }

            guideView.startAnimation();
            return guideView;
        }

    }
}