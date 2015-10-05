// Copyright 2004-present Facebook. All Rights Reserved.

package im.wsb.droidcon.timeline;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * A Frame contains an ordered map of LayerStates and meta data about the timing for that frame in
 * autoplay mode. Matching LayerState objects are tweened when moving from one key frame to the
 * next. A tween Frame is the result of this tweening operation, which can be displayed by the
 * {@link Renderer}.
 */
public class Frame {

  private static final Interpolator DEFAULT_TIMING_FUNCTION = new LinearInterpolator();

  public final Map<String, LayerState> layerStates = new LinkedHashMap<>();
  public final Set<LayerState> removedLayerStates = new HashSet<>();
  // TODO: These will be used for performing timer driven autoplay animation.
  public long duration;
  public Interpolator timingFunction = DEFAULT_TIMING_FUNCTION;
  public Frame prior;
  public Frame next;
  public int index;

  /**
   * Create a copy of a frame that can then be modified and added to the timeline.
   * @return a copy of this frame.
   */
  public Frame copyLayerStates() {
    Frame copy = new Frame();
    for (LayerState layerState : layerStates.values()) {
      copy.addLayerState(layerState.copy());
    }
    return copy;
  }

  public Frame reset() {
    duration = 0;
    timingFunction = DEFAULT_TIMING_FUNCTION;
    layerStates.clear();
    return this;
  }

  public Frame setTimingFunction(Interpolator interpolator) {
    timingFunction = interpolator;
    return this;
  }

  public Frame setManualAdvance() {
    this.duration = 0;
    return this;
  }

  public Frame setDuration(long duration) {
    this.duration = duration;
    return this;
  }

  public Frame setDelay(long delay) {

    return this;
  }
  public Frame addLayerStates(Collection<LayerState> layerStates) {
    for (LayerState layerState : layerStates) {
      this.layerStates.put(layerState.name, layerState);
    }
    return this;
  }

  public Frame addLayerStates(LayerState... layerStates) {
    for (LayerState layerState : layerStates) {
      this.layerStates.put(layerState.name, layerState);
    }
    return this;
  }

  public Frame addLayerState(LayerState layerState) {
    layerStates.put(layerState.name, layerState);
    return this;
  }

  public Frame removeLayerState(String name) {
    LayerState layerState = getLayer(name);
    layerStates.remove(name);
    removedLayerStates.add(layerState);
    return this;
  }

  /**
   * Retrieve a named LayerState
   * @param name the name of the layer state to get
   * @return the LayerState or null
   */
  public LayerState getLayer(String name) {
    return layerStates.get(name);
  }

  /**
   * Bring the requested layer state to the front in z order.
   * @param name the name of the layer
   * @return the layer that was moved
   */
  public LayerState bringToFront(String name) {
    LayerState layer = getLayer(name);
    return removeLayerState(name).addLayerState(layer).getLayer(name);
  }

  public Frame getNext() {
    return next != null ? next : this;
  }

  public Frame getPrior() {
    return prior != null ? prior : this;
  }
}
