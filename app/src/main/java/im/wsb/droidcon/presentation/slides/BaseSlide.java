package im.wsb.droidcon.presentation.slides;

import android.graphics.Color;
import android.text.Layout;

import java.util.Map;

import im.wsb.droidcon.timeline.LayerState;

public abstract class BaseSlide extends Slide {

  private final String mTitle;
  private final int mBackgroundResId;
  private final String mSubtitle;

  public BaseSlide(String title, String subtitle, int backgroundResId) {
    super();
    mTitle = title;
    mSubtitle = subtitle;
    mBackgroundResId = backgroundResId;
    defineLayers();
  }

  @Override
  protected void defineLayers(Map<String, LayerState> layers) {
    if (mBackgroundResId != 0) {
      LayerState imageLayer = makeLayer("image")
          .setBitmap(mBackgroundResId)
          .copyFrame(getFrameLayer());
      layers.put(imageLayer.name, imageLayer);
    }

    LayerState contentFrame = getContentFrameLayer();
    LayerState titleLayer = makeLayer("title")
        .setFrame(contentFrame.left, 0, contentFrame.right, 0)
        .setText(mTitle)
        .setColor(Color.WHITE)
        .setTextSize(px(48))
        .setTextAlignment(Layout.Alignment.ALIGN_CENTER)
        .centerInLayer(getContentFrameLayer())
        .offsetTop(px(-24));
    layers.put(titleLayer.name, titleLayer);

    LayerState subtitleLayer = makeLayer("subtitle")
        .setFrame(contentFrame.left, 0, contentFrame.right, 0)
        .setText(mSubtitle)
        .setColor(Color.WHITE)
        .setTextSize(px(24))
        .setTextAlignment(Layout.Alignment.ALIGN_CENTER)
        .centerInLayer(getContentFrameLayer())
        .offsetTop(px(24));
    layers.put(subtitleLayer.name, subtitleLayer);
  }

}
