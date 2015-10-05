// Copyright 2004-present Facebook. All Rights Reserved.

package im.wsb.droidcon.presentation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import im.wsb.droidcon.timeline.Renderer;
import im.wsb.droidcon.timeline.Timeline;

public class PresentationView extends FrameLayout implements
    GestureDetector.OnGestureListener,
    Timeline.Listener,
    View.OnClickListener {

  private static final double MAX_FLING_VELOCITY_RATIO = 3;

  private final PresentationTimeline mTimeline;
  private final GestureDetector mGestureDetector;
  private final Renderer mRenderer;
  private final MainActivity mActivity;
  private float mLastVelocityX;

  public PresentationView(Context context) {
    this(context, null);
  }

  public PresentationView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PresentationView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mActivity = (MainActivity) getContext();

    setWillNotDraw(false);
    setBackgroundColor(Color.BLACK);
    mTimeline = new PresentationTimeline(context);
    mTimeline.addListener(this);
    mGestureDetector = new GestureDetector(context, this);
    mRenderer = new Renderer(context);

    getViewTreeObserver().addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
            Rect bounds = new Rect(0, 0, getWidth(), getHeight());
            mTimeline.configure(bounds);
          }
        });
    setOnClickListener(this);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (!mTimeline.ready()) {
      return;
    }
    mRenderer.render(canvas, mTimeline.getTweenedFrame());
  }

  /* OnTouchListener */
  @Override
  public boolean onTouchEvent(MotionEvent event) {

    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
      mLastVelocityX = 0;
      mTimeline.setUserInteracting(true);
    }
    boolean res = mGestureDetector.onTouchEvent(event);
    if (event.getActionMasked() == MotionEvent.ACTION_UP ||
        event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
    }
    return res || super.onTouchEvent(event);
  }

  /* Timeline.Listener */
  @Override
  public void onProgressChanged(Timeline timeline) {
    invalidate();
  }

  @Override
  public void onSettleOnFrame(Timeline timeline) {
  }

  @Override
  public void onFinished(Timeline timeline) {
  }

  @Override
  public void onClick(View v) {
  }

  @Override
  public boolean onDown(MotionEvent e) {
    return false;
  }

  @Override
  public void onShowPress(MotionEvent e) {

  }

  @Override
  public boolean onSingleTapUp(MotionEvent e) {
    float vel = e.getRawX() > getWidth() / 2f ? 1 : -1;
    mTimeline.animateProgressWithVelocity(vel);
    return false;
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    return false;
  }

  @Override
  public void onLongPress(MotionEvent e) {

  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    return false;
  }
}
