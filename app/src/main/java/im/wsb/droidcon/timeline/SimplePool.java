// Copyright 2004-present Facebook. All Rights Reserved.

package im.wsb.droidcon.timeline;

import android.support.v4.util.Pools;

// Copied from Android SDK to expose mPoolSize for debugging.

/**
 * Simple (non-synchronized) pool of objects.
 *
 * @param <T> The pooled type.
 */
public class SimplePool<T> implements Pools.Pool<T> {
  private final Object[] mPool;

  protected int mPoolSize;

  /**
   * Creates a new instance.
   *
   * @param maxPoolSize The max pool size.
   *
   * @throws IllegalArgumentException If the max pool size is less than zero.
   */
  public SimplePool(int maxPoolSize) {
    if (maxPoolSize <= 0) {
      throw new IllegalArgumentException("The max pool size must be > 0");
    }
    mPool = new Object[maxPoolSize];
  }

  @Override
  @SuppressWarnings("unchecked")
  public T acquire() {
    if (mPoolSize > 0) {
      final int lastPooledIndex = mPoolSize - 1;
      T instance = (T) mPool[lastPooledIndex];
      mPool[lastPooledIndex] = null;
      mPoolSize--;
      return instance;
    }
    return null;
  }

  @Override
  public boolean release(T instance) {
    if (isInPool(instance)) {
      throw new IllegalStateException("Already in the pool!");
    }
    if (mPoolSize < mPool.length) {
      mPool[mPoolSize] = instance;
      mPoolSize++;
      return true;
    }
    return false;
  }

  private boolean isInPool(T instance) {
    for (int i = 0; i < mPoolSize; i++) {
      if (mPool[i] == instance) {
        return true;
      }
    }
    return false;
  }
}
