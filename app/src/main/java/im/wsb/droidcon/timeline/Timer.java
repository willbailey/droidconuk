// Copyright 2004-present Facebook. All Rights Reserved.

package im.wsb.droidcon.timeline;

import java.util.concurrent.CopyOnWriteArraySet;

import android.os.SystemClock;
import android.view.Choreographer;

public class Timer implements Choreographer.FrameCallback {

  public interface Listener {
    void tick(long sinceLastTick, long sinceStart, long started, long now);
  }

  private static final long CLEARED = Long.MAX_VALUE;
  private long mLastTick;
  private long mStartedAt;
  private boolean mRunning;
  private final CopyOnWriteArraySet<Listener> mListeners = new CopyOnWriteArraySet<>();

  public void addListener(Listener listener) {
    mListeners.add(listener);
  }

  public void removeListener(Listener listener) {
    mListeners.remove(listener);
  }

  public void removeAllListeners() {
    mListeners.clear();
  }

  public Timer start() {
    long now = SystemClock.uptimeMillis();
    if (mStartedAt == CLEARED) {
      mStartedAt = now;
    }
    mLastTick = now;
    mRunning = true;
    Choreographer.getInstance().postFrameCallback(this);
    return this;
  }

  public Timer reset() {
    mStartedAt = CLEARED;
    stop();
    return this;
  }

  public Timer stop() {
    mRunning = false;
    Choreographer.getInstance().removeFrameCallback(this);
    return this;
  }

  /* Choreographer.FrameCallback */
  @Override
  public void doFrame(long frameTimeNanos) {
    if (!mRunning) {
      return;
    }
    long now = SystemClock.uptimeMillis();
    for (Listener listener : mListeners) {
      listener.tick(now - mLastTick, now - mStartedAt, mStartedAt, now);
    }
    mLastTick = now;
    Choreographer.getInstance().postFrameCallback(this);
  }

}
