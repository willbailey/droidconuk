package im.wsb.droidcon.presentation.slides;

import java.util.Map;

import im.wsb.droidcon.timeline.LayerState;

public class ScaleUpSlide extends BaseSlide {

  public ScaleUpSlide(String title, String subtitle, int backgroundResId) {
    super(title, subtitle, backgroundResId);
  }

  @Override
  protected void getInitialLayerStates(Map<String, LayerState> layers) {
    layers.get("image").setAlpha(0);
    layers.get("title").setAlpha(0).scaleFromCenter(0);
    layers.get("subtitle").setAlpha(0).scaleFromCenter(0);
  }

  @Override
  protected void getPresentedLayerStates(Map<String, LayerState> layers) {
    layers.get("title").setAlpha(1).scaleFromCenter(1);
    layers.get("subtitle").setAlpha(1).scaleFromCenter(1);
  }

  @Override
  protected void getExitedLayerStates(Map<String, LayerState> layers) {
    layers.get("image").setAlpha(0);
    layers.get("title").setAlpha(0).scaleFromCenter(2);
    layers.get("subtitle").setAlpha(0).scaleFromCenter(2);
  }

}
