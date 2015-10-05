package im.wsb.droidcon.presentation;

import android.app.Application;

import im.wsb.droidcon.timeline.LayerState;

public class PresentationApplication extends Application {

  private static PresentationApplication sInstance;

  @Override
  public void onCreate() {
    super.onCreate();
    sInstance = this;
    LayerState.RESOURCES = getResources();
  }

  public static PresentationApplication getsInstance() {
    return sInstance;
  }
}
