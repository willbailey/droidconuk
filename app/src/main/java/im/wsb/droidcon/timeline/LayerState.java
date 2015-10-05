// Copyright 2004-present Facebook. All Rights Reserved.

package im.wsb.droidcon.timeline;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.util.HashMap;
import java.util.Map;

/**
 * LayerState is a bag of properties used by a {@link Renderer} to draw a shape, text, or image to a
 * Canvas. LayerStates with matching {@link LayerState#name} fields are tweened when progressing
 * through the {@link Timeline} to create transitions.
 */
public class LayerState {

  public static Resources RESOURCES;
  private Map<Integer, Bitmap> BITMAPS = new HashMap<>();

  private static final String EMPTY_NAME = "empty";
  /* name */
  public String name;

  /* Image */
  public Bitmap bitmap;
  public boolean flip;
  public boolean mirror;

  /* rotation */
  public float rotate = 0;

  /* Scale */
  public float scale = 1;
  public float px = 0;
  public float py = 0;

  /* Text */
  public String text;
  public float textSize;
  public Layout.Alignment textAlignment = Layout.Alignment.ALIGN_CENTER;
  private StaticLayout mTextLayout;
  private final TextPaint mTextPaint;
  private boolean mTextLayoutClean;

  /* Position */
  public float left;
  public float top;
  public float right;
  public float bottom;

  /* Style */
  public float alpha = 1;
  public int color = Color.BLACK;
  public float cornerRadius = 0;

  public LayerState() {
    this(EMPTY_NAME);
  }

  public LayerState(String name) {
    this.name = name;
    mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
  }

  public LayerState reset() {
    name = null;
    bitmap = null;
    flip = false;
    mirror = false;
    text = null;
    textSize = 0;
    textAlignment = Layout.Alignment.ALIGN_CENTER;
    mTextPaint.reset();
    mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    mTextLayoutClean = false;
    left = 0;
    top = 0;
    right = 0;
    bottom = 0;
    alpha = 1;
    color = Color.BLACK;
    cornerRadius = 0;
    scale = 1;
    px = 0;
    py = 0;
    return this;
  }

  /**
   * Add the LayerState to the provided frame.
   * @param frame the frame to add to.
   * @return this LayerState for chaining.
   */
  public LayerState addTo(Frame frame) {
    frame.addLayerState(this);
    return this;
  }

  public LayerState copy() {
    return copy(new LayerState(name));
  }

  public LayerState copy(LayerState copy) {
    copy.reset();
    copy.name = name;

    // There may be a better way to do this, but I just share the text layout between states unless
    // something dirties it. When this happens a new text layout is created it.
    copy.mTextLayout = mTextLayout;
    copy.mTextLayoutClean = mTextLayoutClean;
    copy.mTextPaint.setTextSize(textSize);
    copy.mTextPaint.setColor(color);

    copy.bitmap = bitmap;
    copy.alpha = alpha;
    copy.color = color;
    copy.flip = flip;
    copy.mirror = mirror;
    copy.text = text;
    copy.textSize = textSize;
    copy.textAlignment = textAlignment;
    copy.left = left;
    copy.top = top;
    copy.right = right;
    copy.bottom = bottom;
    copy.cornerRadius = cornerRadius;
    copy.scale = scale;
    copy.px = px;
    copy.py = py;

    return copy;
  }

  private LayerState setBitmap(Bitmap bitmap, boolean matchFrame) {
    this.bitmap = bitmap;
    if (matchFrame) {
      setFrame(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }
    return this;
  }

  public LayerState setBitmap(int resId) {
    return setBitmap(resId, true);
  }

  public LayerState setBitmap(int resId, boolean matchFrame) {
    if (!BITMAPS.containsKey(resId)) {
      Bitmap bitmap = BitmapFactory.decodeResource(RESOURCES, resId);
      BITMAPS.put(resId, bitmap);
    }
    return setBitmap(BITMAPS.get(resId), matchFrame);
  }

  public LayerState setText(String text) {
    this.text = text;
    mTextLayoutClean = false;
    return this;
  }

  public LayerState setTextSize(float textSize) {
    this.textSize = textSize;
    mTextPaint.setTextSize(textSize);
    mTextLayoutClean = false;
    return this;
  }

  public LayerState setTextAlignment(Layout.Alignment textAlignment) {
    this.textAlignment = textAlignment;
    mTextLayoutClean = false;
    return this;
  }

  public Layout.Alignment getTextAlignment() {
    return textAlignment;
  }

  public LayerState layoutText() {
    if (text == null || mTextLayoutClean) {
      return this;
    }

    mTextLayout =
        new StaticLayout(text, mTextPaint, (int) width(), getTextAlignment(), 1, 0, true);
    bottom = top + mTextLayout.getHeight();
    mTextLayoutClean = true;

    return this;
  }

  public StaticLayout getTextLayout() {
    if (!mTextLayoutClean) {
      layoutText();
    }

    // Ensure the TextPaint is updated.
    mTextPaint.setColor(color);
    mTextPaint.setAlpha(Math.round(alpha * 255));
    mTextPaint.setTextSize(textSize);

    return mTextLayout;
  }

  public LayerState setColor(int color) {
    this.color = color;
    mTextPaint.setColor(color);
    return this;
  }

  public LayerState setFrame(float left, float top, float right, float bottom) {
    this.left = left;
    this.top = top;
    this.bottom = bottom;
    this.right = right;
    return this;
  }

  public LayerState scaleFromCenter(float scale) {
    return scale(scale, width() / 2f, height() / 2f);
  }

  public LayerState scale(float scale, float px, float py) {
    this.scale = scale;
    this.px = px;
    this.py = py;
    return this;
  }

  public LayerState rotateFromCenter(float degrees) {
    rotate(degrees, width() / 2f, height() / 2f);
    return this;
  }

  public LayerState rotate(float degrees, float px, float py) {
    this.rotate = degrees;
    this.px = px;
    this.py = py;
    return this;
  }

  public float width() {
    return right - left;
  }

  public float height() {
    return bottom - top;
  }

  public LayerState copyFrame(LayerState src) {
    return setFrame(src.left, src.top, src.right, src.bottom);
  }

  public LayerState alignLeft(LayerState target, float offsetX) {
    return offsetTo(target.left, top).offsetBy(offsetX, 0);
  }

  public LayerState alignLeftOf(LayerState target, float offsetX) {
    return offsetTo(target.right, top).offsetBy(offsetX, 0);
  }

  public LayerState alignTop(LayerState target, float offsetY) {
    return offsetTo(left, target.top).offsetBy(0, offsetY);
  }

  public LayerState alignAbove(LayerState target, float offsetY) {
    return offsetTo(left, target.top).offsetBy(0, -height()).offsetBy(0, offsetY);
  }

  public LayerState alignRight(LayerState target, float offsetX) {
    return offsetTo(target.right, top).offsetBy(-width(), 0).offsetBy(offsetX, 0);
  }

  public LayerState alignRightOf(LayerState target, float offsetX) {
    return offsetTo(target.left, top).offsetBy(-width(), 0).offsetBy(offsetX, 0);
  }

  public LayerState alignBottom(LayerState target, float offsetY) {
    return offsetTo(left, target.bottom).offsetBy(0, -height()).offsetBy(0, offsetY);
  }

  public LayerState alignBelow(LayerState target, float offsetY) {
    return offsetTo(left, target.bottom).offsetBy(0, offsetY);
  }

  public LayerState alignTopLeft(LayerState target, float offsetX, float offsetY) {
    return alignTop(target, offsetY).alignLeft(target, offsetX);
  }

  public LayerState alignBottomLeft(LayerState target, float offsetX, float offsetY) {
    return alignBottom(target, offsetY).alignLeft(target, offsetX);
  }

  public LayerState alignTopRight(LayerState target, float offsetX, float offsetY) {
    return alignTop(target, offsetY).alignRight(target, offsetX);
  }

  public LayerState alignBottomRight(LayerState target, float offsetX, float offsetY) {
    return alignBottom(target, offsetY).alignRight(target, offsetX);
  }

  public LayerState centerInLayer(LayerState container) {
    return offsetTo(container.centerX(), container.centerY())
        .offsetBy(-width() / 2f, -height() / 2f);
  }

  public LayerState offsetTo(float x, float y) {
    return offsetXTo(x).offsetYTo(y);
  }

  public LayerState offsetXTo(float x) {
    float width = right - left;
    left = x;
    right = x + width;
    return this;
  }

  public LayerState offsetYTo(float y) {
    float height = bottom - top;
    top = y;
    bottom = y + height;
    return this;
  }

  public LayerState offsetBy(float x, float y) {
    left += x;
    top += y;
    right += x;
    bottom += y;
    return this;
  }

  public LayerState offsetLeft(float val) {
    left += val;
    return this;
  }

  public LayerState offsetTop(float val) {
    top += val;
    return this;
  }

  public LayerState offsetRight(float val) {
    right += val;
    return this;
  }

  public LayerState offsetBottom(float val) {
    bottom += val;
    return this;
  }

  public LayerState centerInExtent(float containerWidth, float containerHeight) {
    return centerXInExtent(containerWidth).centerYInExtent(containerHeight);
  }

  public LayerState centerXInExtent(float containerWidth) {
    int halfContainer = Math.round(containerWidth / 2f);
    return offsetTo(halfContainer - width() / 2f, top);
  }

  public LayerState centerYInExtent(float containerHeight) {
    int halfContainer = Math.round(containerHeight / 2f);
    return offsetTo(left, halfContainer - height() / 2f);
  }

  public float centerX() {
    return width() / 2f + left;
  }

  public float centerY() {
    return height() / 2f + top;
  }

  public LayerState setAlpha(float alpha) {
    this.alpha = alpha;
    return this;
  }

  public LayerState setCornerRadius(float cornerRadius) {
    this.cornerRadius = cornerRadius;
    return this;
  }

  public LayerState setFlip(boolean flip) {
    this.flip = flip;
    return this;
  }

  public LayerState setMirror(boolean mirror) {
    this.mirror = mirror;
    return this;
  }

}
