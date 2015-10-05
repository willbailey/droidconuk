// Copyright 2004-present Facebook. All Rights Reserved.

package im.wsb.droidcon.timeline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Renderer draws to the provided canvas every {@link LayerState} in the provided {@link Frame}.
 */
public class Renderer {

  private final Paint mPaint;
  private final Context mContext;
  private final Rect mTempRect;
  private final RectF mTempRectF;

  public Renderer(Context context) {
    mContext = context;
    mPaint = new Paint();
    mTempRect = new Rect();
    mTempRectF = new RectF();
  }

  public void render(Canvas canvas, Frame frame) {
    for (LayerState layerState : frame.layerStates.values()) {
      renderLayerState(canvas, layerState);
    }
  }

  private void renderLayerState(Canvas canvas, LayerState layerState) {
    if (layerState.bitmap != null) {
      renderAsImageLayer(canvas, layerState);
    } else if (layerState.text != null) {
      renderAsTextLayer(canvas, layerState);
    } else {
      renderAsColorLayer(canvas, layerState);
    }
  }

  private void renderAsColorLayer(Canvas canvas, LayerState layerState) {
    mPaint.reset();
    mPaint.setColor(layerState.color);
    mPaint.setStyle(Paint.Style.FILL);
    mPaint.setAntiAlias(true);
    mPaint.setAlpha(Math.round(layerState.alpha * 255));

    if (layerState.cornerRadius == 0) {
      canvas.drawRect(layerState.left, layerState.top, layerState.right, layerState.bottom, mPaint);
    } else {
      mTempRectF.set(layerState.left, layerState.top,layerState.right, layerState.bottom);
      canvas.drawRoundRect(mTempRectF, layerState.cornerRadius, layerState.cornerRadius, mPaint);
    }
  }

  private void renderAsImageLayer(Canvas canvas, LayerState layerState) {
    mPaint.reset();
    mPaint.setFilterBitmap(true);
    mPaint.setAntiAlias(true);
    mPaint.setAlpha(Math.round(layerState.alpha * 255));

    int imageWidth = layerState.bitmap.getWidth();
    int imageHeight = layerState.bitmap.getHeight();
    float imageAspect = (float) imageWidth / (float) imageHeight;

    float frameWidth = layerState.width();
    float frameHeight = layerState.height();
    float frameAspect = frameWidth / frameHeight;

    // Find the aspect ratio matching center cropped rectangle in the image bounds for the rendering
    // frame.
    if (imageAspect > frameAspect) {
      // constrain width
      int diff = Math.round((imageWidth - (imageHeight * frameAspect)) / 2f);
      int left = diff;
      int right = imageWidth - diff;
      mTempRect.set(left, 0, right, imageHeight);
    } else {
      // constrain height
      int diff = Math.round((imageHeight - (imageWidth / frameAspect)) / 2f);
      int top = diff;
      int bottom = imageHeight - diff;
      mTempRect.set(0, top, imageWidth, bottom);
    }

    if (layerState.flip) {
      mTempRect.set(mTempRect.left, mTempRect.bottom, mTempRect.right, mTempRect.top);
    } else if (layerState.mirror) {
      mTempRect.set(mTempRect.right, mTempRect.top, mTempRect.left, mTempRect.bottom);
    }

    // Target rectangle is just the frame of the LayerState.
    mTempRectF.set(layerState.left, layerState.top, layerState.right, layerState.bottom);

    // Aspect ratio cropped matching rectangle of the image is scaled to fill the LayerState
    // rectangle.
    canvas.save();
    canvas.scale(layerState.scale, layerState.scale, layerState.px, layerState.py);
    canvas.rotate(layerState.rotate, layerState.px, layerState.py);
    canvas.drawBitmap(layerState.bitmap, mTempRect, mTempRectF, mPaint);
    canvas.restore();
  }

  private void renderAsTextLayer(Canvas canvas, LayerState layerState) {
    canvas.save();
    float halfHeight = layerState.getTextLayout().getHeight() / 2f;
    canvas.translate(layerState.left, layerState.top - halfHeight);
    canvas.scale(layerState.scale, layerState.scale, layerState.px, layerState.py);
    canvas.rotate(layerState.rotate, layerState.px, layerState.py);
    layerState.getTextLayout().draw(canvas);
    canvas.restore();
  }
}
