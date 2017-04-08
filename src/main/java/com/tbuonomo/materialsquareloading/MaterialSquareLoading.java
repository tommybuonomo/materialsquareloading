package com.tbuonomo.materialsquareloading;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

/**
 * Created by tommy on 02/07/16.
 */
public class MaterialSquareLoading extends RelativeLayout {
  private static final int DEFAULT_OUTER_COLOR = Color.parseColor("#1A237E");
  private static final int DEFAULT_INNER_COLOR = Color.parseColor("#01579B");
  public static final int DEFAULT_DURATION_ROTATION_INNER = 4862;
  public static final int DEFAULT_DURATION_ROTATION_OUTER = 6028;

  private CardView innerSquare, outerSquare;
  private int outerSize;
  private int innerSize;
  private boolean sizeChanged;
  private boolean requestStartAnimation;
  private int outerRotationDuration;
  private int innerRotationDuration;

  public MaterialSquareLoading(Context context) {
    super(context);
    init(context, null);
  }

  public MaterialSquareLoading(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public MaterialSquareLoading(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    inflateSelf();
    setUpAttrs(attrs);

    if (!isInEditMode()) {
      setVisibility(INVISIBLE);
    }
  }

  private void setUpAttrs(AttributeSet attrs) {
    int innerColor = DEFAULT_INNER_COLOR;
    int outerColor = DEFAULT_OUTER_COLOR;
    float innerRadius = innerSquare.getRadius();
    float outerRadius = outerSquare.getRadius();
    outerRotationDuration = DEFAULT_DURATION_ROTATION_OUTER;
    innerRotationDuration = DEFAULT_DURATION_ROTATION_INNER;
    if (attrs != null) {
      TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MaterialSquareLoading);
      innerColor = a.getColor(R.styleable.MaterialSquareLoading_innerColor, DEFAULT_INNER_COLOR);
      outerColor = a.getColor(R.styleable.MaterialSquareLoading_outerColor, DEFAULT_OUTER_COLOR);
      innerRadius = a.getDimension(R.styleable.MaterialSquareLoading_innerRadius, innerSquare.getRadius());
      outerRadius = a.getDimension(R.styleable.MaterialSquareLoading_outerRadius, outerSquare.getRadius());
      innerRotationDuration = a.getInt(R.styleable.MaterialSquareLoading_rotationInnerDuration, DEFAULT_DURATION_ROTATION_INNER);
      outerRotationDuration = a.getInt(R.styleable.MaterialSquareLoading_rotationOuterDuration, DEFAULT_DURATION_ROTATION_OUTER);
      a.recycle();
    }

    innerSquare.setCardBackgroundColor(innerColor);
    outerSquare.setCardBackgroundColor(outerColor);

    innerSquare.setRadius(innerRadius);
    outerSquare.setRadius(outerRadius);
  }

  private void inflateSelf() {
    inflate(getContext(), R.layout.material_square_loading_layout, this);

    outerSquare = (CardView) findViewById(R.id.material_square1);
    innerSquare = (CardView) findViewById(R.id.material_square2);
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    setUpSquareSize(Math.min(h, w));
    sizeChanged = true;

    if (requestStartAnimation) {
      show();
    }
  }

  private void setUpSquareSize(int size) {
    double hypotenuse = Math.sqrt(2 * size * size);
    int realSize = size - (int) (hypotenuse - size);

    LayoutParams outerParams = (LayoutParams) outerSquare.getLayoutParams();
    outerSize = outerParams.width = realSize;
    outerParams.height = realSize;

    LayoutParams innerParams = (LayoutParams) innerSquare.getLayoutParams();
    innerSize = innerParams.width = realSize / 2;
    innerParams.height = realSize / 2;
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, widthMeasureSpec);
  }

  public void show() {
    if (!sizeChanged) {
      requestStartAnimation = true;
    } else {
      startGlobalAnimation();
    }
  }

  public void hide() {
    endGlobalAnimation();
  }

  private void endGlobalAnimation() {
    cancelViewTagAnimator(this);
    // SCALE ENTER
    ValueAnimator scaleAnimator = ValueAnimator.ofFloat(1, 0);
    scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        Float scale = (Float) animation.getAnimatedValue();
        setScaleX(scale);
        setScaleY(scale);
      }
    });

    scaleAnimator.setDuration(400);
    scaleAnimator.setInterpolator(new AccelerateInterpolator());

    scaleAnimator.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        cancelViewTagAnimator(outerSquare);
        cancelViewTagAnimator(innerSquare);
        setVisibility(INVISIBLE);
      }
    });

    scaleAnimator.start();
    setTag(scaleAnimator);
  }

  private void startGlobalAnimation() {

    // OUTER SQUARE
    final ValueAnimator outerAnimator = createCosinusValueAnimator(outerSize, outerSize * 0.8f, new CosinusAnimatorUpdateListener() {
      @Override public void onCosinusAnimatorUpdate(float value) {
        LayoutParams params = (LayoutParams) outerSquare.getLayoutParams();
        params.width = (int) value;
        params.height = (int) value;
        outerSquare.requestLayout();
      }
    });

    outerAnimator.setDuration(2000);
    outerAnimator.setInterpolator(new LinearInterpolator());
    outerAnimator.setRepeatCount(ValueAnimator.INFINITE);

    ValueAnimator outerRotateAnimator = ObjectAnimator.ofFloat(outerSquare, View.ROTATION, 0, 360);
    outerRotateAnimator.setInterpolator(new LinearInterpolator());
    outerRotateAnimator.setDuration(outerRotationDuration);
    outerRotateAnimator.setRepeatCount(ValueAnimator.INFINITE);

    AnimatorSet outerAnimatorSet = new AnimatorSet();
    outerAnimatorSet.playTogether(outerRotateAnimator, outerAnimator);

    outerSquare.setTag(outerAnimatorSet);
    outerAnimatorSet.start();

    // INNER SQUARE
    final ValueAnimator innerAnimator = createCosinusValueAnimator(innerSize * 0.8f, innerSize, new CosinusAnimatorUpdateListener() {
      @Override public void onCosinusAnimatorUpdate(float value) {
        LayoutParams params = (LayoutParams) innerSquare.getLayoutParams();
        params.width = (int) value;
        params.height = (int) value;
        innerSquare.requestLayout();
      }
    });

    innerAnimator.setDuration(2000);
    innerAnimator.setInterpolator(new LinearInterpolator());
    innerAnimator.setRepeatCount(ValueAnimator.INFINITE);

    ValueAnimator innerRotateAnimator = ObjectAnimator.ofFloat(innerSquare, View.ROTATION, 0, -360);
    innerRotateAnimator.setInterpolator(new LinearInterpolator());
    innerRotateAnimator.setDuration(innerRotationDuration);
    innerRotateAnimator.setRepeatCount(ValueAnimator.INFINITE);

    AnimatorSet innerAnimatorSet = new AnimatorSet();
    innerAnimatorSet.playTogether(innerRotateAnimator, innerAnimator);

    innerSquare.setTag(innerAnimatorSet);
    innerAnimatorSet.start();

    // SCALE ENTER
    ValueAnimator scaleAnimator = ValueAnimator.ofFloat(0, 1);
    scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        Float offset = (Float) animation.getAnimatedValue();
        setScaleX(offset);
        setScaleY(offset);
        setRotation(-(1 - offset) * 50);
      }
    });

    scaleAnimator.setDuration(400);
    scaleAnimator.setInterpolator(new DecelerateInterpolator());

    scaleAnimator.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationStart(Animator animation) {
        super.onAnimationStart(animation);
        setVisibility(VISIBLE);
      }
    });

    scaleAnimator.start();

    setTag(scaleAnimator);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    cancelViewTagAnimator(outerSquare);
    cancelViewTagAnimator(innerSquare);
    cancelViewTagAnimator(this);
  }

  public static ValueAnimator createCosinusValueAnimator(final float start, final float end, final CosinusAnimatorUpdateListener listener) {
    ValueAnimator valueAnimator = ValueAnimator.ofFloat((float) (-Math.PI), (float) (Math.PI));
    valueAnimator.setInterpolator(new LinearInterpolator());
    if (listener != null) {
      valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override public void onAnimationUpdate(ValueAnimator animation) {
          double offset = (Math.cos((float) animation.getAnimatedValue()) + 1) / 2;
          float value = (float) (start + (end - start) * offset);
          listener.onCosinusAnimatorUpdate(value);
        }
      });
    }
    return valueAnimator;
  }

  public static void cancelViewTagAnimator(View view) {
    if (view != null && view.getTag() != null && view.getTag() instanceof Animator) {
      ((Animator) view.getTag()).cancel();
    }
  }

  public interface CosinusAnimatorUpdateListener {
    void onCosinusAnimatorUpdate(float value);
  }

  public interface AnimationListener {
    void onAnimationFinished();
  }
}
