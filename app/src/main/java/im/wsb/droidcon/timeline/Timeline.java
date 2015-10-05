// Copyright 2004-present Facebook. All Rights Reserved.

package im.wsb.droidcon.timeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;

/**
 * Timeline holds an ordered list of keyframes as {@link Frame} objects.
 * {@link Timeline#getTweenedFrame()} returns an interpolated Frame for the provided progress value,
 * which should be a float value in the range of 0 to the number of key frames in the Timeline.
 */
public abstract class Timeline implements SpringListener, Timer.Listener {

  public interface Listener {
    void onProgressChanged(Timeline timeline);
    void onSettleOnFrame(Timeline timeline);
    void onFinished(Timeline timeline);
  }

  protected final List<Frame> mKeyFrames = new ArrayList<>();
  private final Context mContext;
  private final Spring mProgress;
  private final CopyOnWriteArraySet<Listener> mListeners;
  private final Timer mTimer;
  private final Resources mResources;
  private boolean mUserInteracting;
  private long mUserInteractionStoppedAt;
  private boolean mUserInteractedSinceLastSettle;
  private float mLastInterpolationDiscrepancy;
  private Frame mCurrentKeyFrame;
  private Frame mFirstKeyFrame;
  private Frame mLastKeyFrame;

  public Timeline(Context context) {
    mContext = context;
    mResources = context.getResources();
    mProgress = SpringSystem
        .create()
        .createSpring()
        .setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(30, 6.2f))
        .addListener(this);
    mListeners = new CopyOnWriteArraySet<>();
    mTimer = new Timer();
    mTimer.addListener(this);
  }

  public void startTimer() {
    mTimer.reset().start();
  }

  public void stopTimer() {
    mTimer.reset();
  }

  /**
   * Tell the Timeline that the user is currently interacting with it.
   * @param userInteracting whether or not the user is interacting
   */
  public void setUserInteracting(boolean userInteracting) {
    mUserInteracting = userInteracting;
    if (!mUserInteracting) {
      mUserInteractionStoppedAt = SystemClock.uptimeMillis();
    } else {
      mUserInteractedSinceLastSettle = false;
    }
  }

  /**
   * Check if the user is currently interacting with the Timeline.
   * @return whether or not the user is interacting
   */
  public boolean getUserInteracting() {
    return mUserInteracting;
  }

  /**
   * Get the content for this Timeline.
   * @return the context object
   */
  public Context getContext() {
    return mContext;
  }

  /**
   * Add keyframes to the timeline as {@link Frame} objects.
   * @param bounds the bounds of the container that frames can be rendered in.
   */
  public abstract void configure(Rect bounds);

  /**
   * Reset progress on the timeline to 0.
   */
  public Timeline reset() {
    mCurrentKeyFrame = mFirstKeyFrame;
    mTimer.reset();
    mProgress.setCurrentValue(0);
    return this;
  }

  /**
   * Get the current progress state of the timeline.
   * @return the progress in the range of 0 - number of keyframes - 1.
   */
  public float getProgress() {
    return (float) mProgress.getCurrentValue();
  }

  /**
   * Increment the current progress by the specified amount.
   * @param progressIncrement the amount to increment by
   */
  public void incrementProgress(float progressIncrement) {
    if (mLastInterpolationDiscrepancy > 0) {
      progressIncrement += mLastInterpolationDiscrepancy;
      mLastInterpolationDiscrepancy = 0;
    }
    mProgress.setCurrentValue(mProgress.getCurrentValue() + progressIncrement);
  }

  /**
   * Animate to the current or next frame based on the provided velocity and current progress.
   * @param velocity
   */
  public void animateProgressWithVelocity(float velocity) {
    mProgress.setVelocity(velocity);
    float progress = (float) mProgress.getCurrentValue();

    if (velocity == 0) {
      if (progress > 0.5) {
        mProgress.setEndValue(1);
      } else if (progress > -0.5) {
        mProgress.setEndValue(0);
      } else {
        mProgress.setEndValue(-1);
      }
    } else {
      if (progress > 0.5) {
        mProgress.setEndValue(1);
      } else if (progress > 0) {
        mProgress.setEndValue(velocity > 0 ? 1 : 0);
      } else if (progress == 0) {
        mProgress.setEndValue(velocity > 0 ? 1 : -1);
      } else {
        mProgress.setEndValue(velocity > 0 ? 0 : -1);
      }
    }
  }

  /**
   * Check if we have at least one key frame.
   * @return true if ready to be acted upon
   */
  public boolean ready() {
    return !mKeyFrames.isEmpty();
  }

  /**
   * Number of key frames in the timeline.
   * @return the number of key frames
   */
  public int frameCount() {
    return mKeyFrames.size();
  }

  public Frame makeKeyFrame() {
    Frame frame = new Frame();
    addKeyFrame(frame);
    return frame;
  }

  public Frame makeKeyFrame(Frame template) {
    Frame frame = template.copyLayerStates();
    addKeyFrame(frame);
    return frame;
  }

  /**
   * Just add the frame to the key frames
   * @param frame the frame to add
   * @return the Timeline for chaining
   */
  public Timeline addKeyFrame(Frame frame) {
    if (mKeyFrames.isEmpty()) {
      mFirstKeyFrame = frame;
      mCurrentKeyFrame = mFirstKeyFrame;
    }
    if (!mKeyFrames.isEmpty()) {
      Frame prior = mKeyFrames.get(mKeyFrames.size() - 1);
      frame.prior = prior;
      prior.next = frame;
    }
    frame.index = mKeyFrames.size();
    mLastKeyFrame = frame;
    mKeyFrames.add(frame);
    return this;
  }

  /**
   * Add a collection of {@link LayerState} objects as a new keyframe.
   * @param layerStates the LayerState objects to add to the new Frame.
   * @return the created frame
   */
  public Frame addKeyFrameFromLayerStates(LayerState... layerStates) {
    Frame frame = new Frame();
    for (LayerState layerState : layerStates) {
      frame.addLayerState(layerState);
    }
    mKeyFrames.add(frame);
    return frame;
  }

  /**
   * Get the key frame at the provided index.
   * @param i index of the key frame.
   * @return the Frame
   */
  public Frame getKeyFrame(int i) {
    return mKeyFrames.get(i);
  }

  /**
   * Get the current Key frame.
   * @return the current key frame.
   */
  public Frame getCurrentKeyFrame() {
    return mCurrentKeyFrame;
  }

  /**
   * Get the next Key frame.
   * @return the next key frame.
   */
  public Frame getNextKeyFrame() {
    return mCurrentKeyFrame.next == null ? mCurrentKeyFrame : mCurrentKeyFrame.next;
  }

  /**
   * Get the final key frame of the timeline.
   * @return the final key frame
   */
  public Frame getLastKeyFrame() {
    return mLastKeyFrame;
  }

  /**
   * Get the starting key frame of the timeline.
   * @return the first key frame
   */
  public Frame getFirstKeyFrame() {
    return mFirstKeyFrame;
  }

  /**
   * Get the interpolated current frame based on the current progress
   * @return the interpolated frame
   */
  public Frame getTweenedFrame() {
    if (mKeyFrames.isEmpty()) {
      return null;
    }

    float currentValue = (float) mProgress.getCurrentValue();

    Frame currentFrame = mCurrentKeyFrame;
    Frame low = currentFrame.prior != null ? currentFrame.prior : currentFrame;
    Frame mid = currentFrame;
    Frame high = currentFrame.next != null ? currentFrame.next : currentFrame;

    float progress = currentValue;

    if (progress >= 0) {
      low = mid;
    } else {
      high = mid;
      progress = (float) SpringUtil.mapValueFromRangeToRange(progress, -1, 0, 0, 1);
    }

    float interpolatedProgress = progress;
    if (high.timingFunction != null &&
        mProgress.isAtRest() &&
        !(mUserInteracting) &&
        !mUserInteractedSinceLastSettle) {
      interpolatedProgress = high.timingFunction.getInterpolation(progress);
    }
    mLastInterpolationDiscrepancy = interpolatedProgress - progress;

    return Util.tweenFrames(low, high, interpolatedProgress);
  }

  /* SpringListener */
  @Override
  public void onSpringUpdate(Spring spring) {
    for (Listener listener : mListeners) {
      listener.onProgressChanged(this);
    }
  }

  @Override
  public void onSpringAtRest(Spring spring) {
    float currentValue = (float) spring.getCurrentValue();
    Frame toBeFrame = mCurrentKeyFrame;
    if (currentValue == 1) {
      toBeFrame = mCurrentKeyFrame.getNext();
    } else if (currentValue == -1) {
      toBeFrame = mCurrentKeyFrame.getPrior();
    }

    if (toBeFrame != mCurrentKeyFrame) {
      mCurrentKeyFrame = toBeFrame;
    }
    mProgress.setCurrentValue(0);

    if (mUserInteracting) {
      // Don't fire these events if the user is interacting.
      return;
    }

    mUserInteractedSinceLastSettle = false;

    // Notify that we stopped at a frame.
    for (Listener listener : mListeners) {
      listener.onSettleOnFrame(this);
      if (getCurrentKeyFrame().duration > 0) {
        startTimer();
      }
    }

    if (spring.getCurrentValue() != frameCount() - 1) {
      return;
    }

    // Notify that we stopped at the last frame.
    for (Listener listener : mListeners) {
      listener.onFinished(this);
    }

  }

  @Override
  public void onSpringActivate(Spring spring) {
  }

  @Override
  public void onSpringEndStateChange(Spring spring) {
  }

  /* Listener Management */
  public Timeline addListener(Listener listener) {
    mListeners.add(listener);
    return this;
  }

  public Timeline removeListener(Listener listener) {
    mListeners.remove(listener);
    return this;
  }

  public Timeline clearListeners() {
    mListeners.clear();
    return this;
  }

  @Override
  public void tick(long sinceLastTick, long sinceStart, long started, long now) {
    if (mUserInteractedSinceLastSettle ||
        (mUserInteracting) ||
        !mProgress.isAtRest()) {
      // Don't resume timed playback if the user recently interacted.
      return;
    }

    Frame keyFrame = getNextKeyFrame();
    long duration = keyFrame.duration;

    if (duration == 0) {
      // Don't play forward progress if we aren't on a frame with a duration. These frames are
      // pauses where the user must continue.
      return;
    }

    float progress = (float) mProgress.getCurrentValue();
    float progressIncrement = Math.min((float) sinceLastTick / (float) duration, 1);
    float newProgress = Math.min(progress + progressIncrement, 1);
    mProgress.setCurrentValue(newProgress);
    mProgress.setCurrentValue(progress);

    if (newProgress == 1) {
      onSpringAtRest(mProgress);
    }
  }
}
