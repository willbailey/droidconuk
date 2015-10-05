package im.wsb.droidcon.presentation.slides;

import java.util.Map;

import im.wsb.droidcon.timeline.LayerState;

public class SandwichSlide extends BaseSlide {

  public SandwichSlide(String title, String subtitle, int backgroundResId) {
    super(title, subtitle, backgroundResId);
  }

  @Override
  protected void getInitialLayerStates(Map<String, LayerState> layers) {
    layers.get("image").setAlpha(0);
    layers.get("title").setAlpha(0).offsetTop(-getHeight() / 2f);
    layers.get("subtitle").setAlpha(0).offsetTop(getHeight() / 2f);
  }

  @Override
  protected void getPresentedLayerStates(Map<String, LayerState> layers) {
  }

  @Override
  protected void getExitedLayerStates(Map<String, LayerState> layers) {
    layers.get("image").setAlpha(0);
    layers.get("title").setAlpha(0).offsetTop(-getHeight() / 2f);
    layers.get("subtitle").setAlpha(0).offsetTop(getHeight() / 2f);
  }

}
