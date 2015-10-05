package im.wsb.droidcon;

import im.wsb.droidcon.presentation.slides.SandwichSlide;
import im.wsb.droidcon.presentation.slides.ScaleUpSlide;
import im.wsb.droidcon.presentation.slides.Slide;
import im.wsb.droidcon.presentation.slides.PopupSlide;
import im.wsb.droidcon.presentation.slides.TitleSlide;
import im.wsb.droidcon.presentation.slides.TwirlSlide;

public abstract class Deck {

  public static Slide SLIDE1 = new TitleSlide("Physics UI", "Filipe Abrantes & Will Bailey", R.drawable.space1);

  public static Slide SLIDE2 = new PopupSlide("Title 2", "subtitle 2", R.drawable.space2);

  public static Slide SLIDE3 = new SandwichSlide("Title 3", "subtitle 3", R.drawable.space3);

  public static Slide SLIDE4 = new ScaleUpSlide("Title 4", "subtitle 4", R.drawable.space4);

  public static Slide SLIDE5 = new SandwichSlide("Title 5", "subtitle 5", R.drawable.space3);

  public static Slide SLIDE6 = new TwirlSlide("Title 6", "subtitle 6", R.drawable.space2);

  public static Slide SLIDE7 = new TwirlSlide("Title 7", "subtitle 7", R.drawable.space1);

}
