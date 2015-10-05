package im.wsb.droidcon.presentation.slides;

import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import im.wsb.droidcon.presentation.PresentationApplication;
import im.wsb.droidcon.timeline.LayerState;

public abstract class Slide {

  protected final Map<String, LayerState> mLayers = new LinkedHashMap<>();
  private final int mWidth;
  private final int mHeight;
  private final LayerState mFrame;
  private final DisplayMetrics mDisplayMetrics;
  private final LayerState mContentFrame;
  private String mUid;

  public Slide() {
    mDisplayMetrics =
        PresentationApplication.getsInstance().getResources().getDisplayMetrics();
    mUid = UUID.randomUUID().toString();
    mWidth = mDisplayMetrics.widthPixels;
    mHeight = mDisplayMetrics.heightPixels;
    mFrame = new LayerState().setFrame(0, 0, mWidth, mHeight);
    mContentFrame = new LayerState().setFrame(px(20), px(20), mWidth - px(20), mHeight - px(20));
  }

  public int getWidth() {
    return mWidth;
  }

  public int getHeight() {
    return mHeight;
  }

  public LayerState getFrameLayer() {
    return mFrame;
  }

  public LayerState getContentFrameLayer() {
    return mContentFrame;
  }

  private Map<String, LayerState> getLayers() {
    Map<String, LayerState> copy = new LinkedHashMap<>();
    for (LayerState layerState : mLayers.values()) {
      copy.put(layerState.name.replace(mUid, ""), layerState.copy());
    }
    return copy;
  }

  public Slide defineLayers() {
    defineLayers(mLayers);
    return this;
  }

  public Collection<LayerState> getInitialLayerStates() {
    Map<String, LayerState> layers = getLayers();
    getInitialLayerStates(layers);
    return layers.values();
  }

  public Collection<LayerState> getPresentedLayerStates() {
    Map<String, LayerState> layers = getLayers();
    getPresentedLayerStates(layers);
    return layers.values();
  }

  public Collection<LayerState> getExitedLayerStates() {
    Map<String, LayerState> layers = getLayers();
    getExitedLayerStates(layers);
    return layers.values();
  }

  protected abstract void defineLayers(Map<String, LayerState> layers);

  protected abstract void getInitialLayerStates(Map<String, LayerState> layers);

  protected abstract void getPresentedLayerStates(Map<String, LayerState> layers);

  protected abstract void getExitedLayerStates(Map<String, LayerState> layers);

  /* Utils */

  protected String id(String name) {
    return mUid + name;
  }

  protected float px(float dp) {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        mDisplayMetrics);
  }

  protected LayerState makeLayer(String name) {
    return new LayerState(id(name));
  }

  protected LayerState makeLayer() {
    return new LayerState(UUID.randomUUID().toString());
  }
}
