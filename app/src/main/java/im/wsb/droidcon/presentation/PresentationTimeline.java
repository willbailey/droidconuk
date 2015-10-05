package im.wsb.droidcon.presentation;

import android.content.Context;
import android.graphics.Rect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import im.wsb.droidcon.Deck;
import im.wsb.droidcon.presentation.slides.Slide;
import im.wsb.droidcon.timeline.Frame;
import im.wsb.droidcon.timeline.Timeline;

public class PresentationTimeline extends Timeline {

  public PresentationTimeline(Context context) {
    super(context);
  }

  @Override
  public void configure(Rect bounds) {
    List<Slide>  slides = new ArrayList<>();
    for (Field field : Deck.class.getDeclaredFields()) {
      try {
        Object value = field.get(null);
        if (value instanceof Slide) {
          slides.add((Slide) value);
        }
      } catch (IllegalAccessException e) {
        // ignore
      }
    }

    Slide priorSlide = null;
    Slide currentSlide = null;

    for (int i = 0; i < slides.size() + 2; i++) {

      Frame frame = makeKeyFrame();
      if (priorSlide != null) {
        frame.addLayerStates(priorSlide.getExitedLayerStates());
      }

      if (currentSlide != null) {
        frame.addLayerStates(currentSlide.getPresentedLayerStates());
        priorSlide = currentSlide;
      }

      Slide nextSlide = null;
      if (i < slides.size()) {
        nextSlide = slides.get(i);
        frame.addLayerStates(nextSlide.getInitialLayerStates());
      }
      currentSlide = nextSlide;
    }
  }
}
