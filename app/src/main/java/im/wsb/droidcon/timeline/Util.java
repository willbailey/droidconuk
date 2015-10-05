// Copyright 2004-present Facebook. All Rights Reserved.

package im.wsb.droidcon.timeline;

import android.animation.ArgbEvaluator;
import android.util.Log;

import com.facebook.rebound.SpringUtil;

/**
 * Util is a collection of helpers methods for tweening Frame and LayerState objects.
 */
public abstract class Util {

  private static final ArgbEvaluator colorEvaluator = new ArgbEvaluator();
  private static final LayerStatePool LAYER_STATE_POOL = new LayerStatePool(100);
  private static final Frame TWEEN_FRAME = new Frame();

  /**
   * Interpolate all of the {@link LayerState} objects that have matching names between the current
   * and next {@link Frame} and return a new Frame that holds the tweened values. Note that this
   * handles both forward and backward progress through the com.instagram.layout.nux.timeline, but
   * progress is always normalized to 0 - 1.
   * @param current the current or starting frame
   * @param next the next or ending frame
   * @param progress the 0 - 1 progress
   * @return the tweened frame between the supplied key frames.
   */
  public static Frame tweenFrames(Frame current, Frame next, float progress) {

    // Return all the previously used LayerState objects to the pool.
    for (LayerState layerState : TWEEN_FRAME.layerStates.values()) {
      LAYER_STATE_POOL.release(layerState);
    }
    for (LayerState layerState : TWEEN_FRAME.removedLayerStates) {
      LAYER_STATE_POOL.release(layerState);
    }

    // We use a single common frame for conveying the tween state to the renderer.
    TWEEN_FRAME.reset();

    for (LayerState nextLayerState : next.layerStates.values()) {
      if (current.layerStates.containsKey(nextLayerState.name)) {
        LayerState currentLayerState = current.layerStates.get(nextLayerState.name);
        TWEEN_FRAME.addLayerState(tweenLayerState(currentLayerState, nextLayerState, progress));
      } else if (progress > 0.5f) {
        // TODO: This makes a next LayerState visible halfway through the progress always if it
        //       wasn't in the prior frame. It might be better to add a way to configure this.
        TWEEN_FRAME.addLayerState(nextLayerState.copy(LAYER_STATE_POOL.acquire()));
      }
    }

    return TWEEN_FRAME;
  }

  /**
   * Interpolate between the current and next frame based on a 0 - 1 progress field. Note that this
   * handles both forward and backward progress through the com.instagram.layout.nux.timeline,
   * but progress is always normalized to 0 - 1.
   * @param current the starting or current frame
   * @param next the ending or next frame
   * @param progress the 0 - 1 normalized progress
   * @return a LayerState with the tweenable values tweened for the provied progress.
   */
  public static LayerState tweenLayerState(LayerState current, LayerState next, float progress) {
    // Copy the current frame state into a LayerState acquired from the reuse pool.
    LayerState tweenState = current.copy(LAYER_STATE_POOL.acquire());

    // We don't tween these values.
    tweenState.text = next.text;
    tweenState.bitmap = next.bitmap;
    tweenState.flip = progress > 0.5 ? next.flip : current.flip;
    tweenState.mirror = progress > 0.5 ? next.mirror : current.mirror;

    tweenState.color = tweenColor(progress, current.color, next.color);
    tweenState.textSize = tweenFloat(progress, current.textSize, next.textSize);
    tweenState.left = tweenFloat(progress, current.left, next.left);
    tweenState.top = tweenFloat(progress, current.top, next.top);
    tweenState.right = tweenFloat(progress, current.right, next.right);
    tweenState.bottom = tweenFloat(progress, current.bottom, next.bottom);
    tweenState.alpha = tweenAlpha(progress, current.alpha, next.alpha);
    tweenState.cornerRadius = tweenFloat(progress, current.cornerRadius, next.cornerRadius);
    tweenState.scale = tweenFloat(progress, current.scale, next.scale);
    tweenState.px = tweenFloat(progress, current.px, next.px);
    tweenState.py = tweenFloat(progress, current.py, next.py);
    tweenState.rotate = tweenFloat(progress, current.rotate, next.rotate);

    return tweenState;
  }

  /* Interpolation Helpers */

  public static float tweenFloat(float progress, float start, float end) {
    return (float) SpringUtil.mapValueFromRangeToRange(progress, 0, 1, start, end);
  }

  public static int tweenColor(float progress, int start, int end) {
    progress = (float) SpringUtil.clamp(progress, 0, 1);
    return (int) colorEvaluator.evaluate(progress, start, end);
  }

  public static float tweenAlpha(float progress, float start, float end) {
    progress = (float) SpringUtil.clamp(progress, 0, 1);
    return (float) SpringUtil.mapValueFromRangeToRange(progress, 0, 1, start, end);
  }

  /**
   * Pool of LayerState objects to reduce allocations while animations are running.
   */
  private static class LayerStatePool extends SimplePool<LayerState> {

    private static final String TAG = LayerStatePool.class.getSimpleName();
    private static final boolean DEBUG_POOL = false;

    public LayerStatePool(int maxPoolSize) {
      super(maxPoolSize);
    }

    @Override
    public LayerState acquire() {
      LayerState instance = super.acquire();
      if (DEBUG_POOL) {
        Log.d(TAG, "ACQUIRE pool size: " + mPoolSize + " creating? " + (instance == null));
      }
      return instance != null ? instance : new LayerState();
    }

    @Override
    public boolean release(LayerState instance) {
      instance.reset();
      boolean res = super.release(instance);
      if (DEBUG_POOL) {
        Log.d(TAG, "RELEASE pool size: " + mPoolSize);
      }
      return res;
    }

  }

}
