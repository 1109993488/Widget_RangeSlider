package com.blingbling.widget.rangeslider;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by BlingBling on 2018/7/15.
 */
public class BGMRangeSlider extends View {

    private RectF mBackgroundRect;
    private RectF mThumbRect;

    private Bitmap mBackground;

    private Paint mPaint;

    private float mThumbBlock;

    private int mThumbMax;
    private int mThumbProgress;
    private int mThumbSize;
    private int mThumbProgressTem;

    private float mLastX = -1;

    public BGMRangeSlider(Context context) {
        this(context, null);
    }

    public BGMRangeSlider(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BGMRangeSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BGMRangeSlider);
        mBackground = BitmapFactory.decodeResource(getResources(), a.getResourceId(R.styleable.BGMRangeSlider_slider_background, 0));
        final int ThumbColor = a.getColor(R.styleable.BGMRangeSlider_slider_thumb_color, 0);
        mThumbMax = a.getInt(R.styleable.BGMRangeSlider_slider_thumb_max, 100);
        mThumbSize = a.getInt(R.styleable.BGMRangeSlider_slider_thumb_size, 10);
        mThumbProgress = a.getInt(R.styleable.BGMRangeSlider_slider_thumb_progress, 0);
        a.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(ThumbColor);

        setThumbProgress(mThumbProgress);
        if (mThumbSize > mThumbMax) {
            mThumbSize = mThumbMax;
        }
    }

    @Override
    public void setBackground(Drawable background) {
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mBackgroundRect == null) {
            mBackgroundRect = new RectF();
        }
        if (mThumbRect == null) {
            mThumbRect = new RectF();
        }
        mBackgroundRect.set(0, 0, w, h);
        changeBlock();
    }

    private void changeBlock() {
        if (getMeasuredWidth() != 0) {
            mThumbBlock = 1.0f * getMeasuredWidth() / mThumbMax;
            final float size = mThumbBlock * mThumbSize;
            mThumbRect.set(0, 0, size, getMeasuredWidth());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mThumbRect.left = (mThumbProgress + mThumbProgressTem) * mThumbBlock;
        mThumbRect.right = mThumbRect.left + mThumbSize * mThumbBlock;

        int sc = canvas.saveLayer(mBackgroundRect, mPaint, Canvas.ALL_SAVE_FLAG);

        canvas.drawBitmap(mBackground, null, mBackgroundRect, mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawRect(mThumbRect, mPaint);

        mPaint.setXfermode(null);
        canvas.restoreToCount(sc);
    }

    public void setThumbMax(int max) {
        if (max < mThumbSize) {
            mThumbMax = mThumbSize;
        }
        mThumbMax = max;
        if (mThumbProgress + mThumbSize > max) {
            mThumbProgress = max - mThumbSize;
        }
        changeBlock();
        postInvalidate();
    }

    public void setThumbProgress(int progress) {
        if (progress < 0) {
            mThumbProgress = 0;
        } else if (progress + mThumbSize > mThumbMax) {
            mThumbProgress = mThumbMax - mThumbSize;
        } else {
            mThumbProgress = progress;
        }
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                boolean isTouchInThumb = isTouchInThumb(event);
                if (isTouchInThumb) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mLastX = event.getX();
                } else {
                    mLastX = -1;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mThumbProgressTem != 0) {
                    mThumbProgress = mThumbProgress + mThumbProgressTem;
                    if (mRangeChangeListener != null) {
                        mRangeChangeListener.onChanged(mThumbProgress, mThumbProgress + mThumbSize, false);
                    }
                    mThumbProgressTem = 0;
                    postInvalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mLastX != -1) {
                    int thumbProgressTem = moveThumb(event.getX() - mLastX);

                    if (mThumbProgressTem != thumbProgressTem) {
                        mThumbProgressTem = thumbProgressTem;
                        postInvalidate();

                        if (mRangeChangeListener != null) {
                            final int progress = mThumbProgress + mThumbProgressTem;
                            mRangeChangeListener.onChanged(progress, progress + mThumbSize, true);
                        }
                    }
                }
                break;
        }
        return mLastX != -1;
    }

    private boolean isTouchInThumb(MotionEvent event) {
        if (event.getX() >= mThumbRect.left
                && event.getX() <= mThumbRect.right) {
            return true;
        }
        return false;
    }

    private int moveThumb(float x) {
        int thumbProgressTem = (int) (x / mThumbBlock);
        final int progress = mThumbProgress + thumbProgressTem;
        if (progress < 0) {
            thumbProgressTem = 0 - mThumbProgress;
        } else if (progress > mThumbMax - mThumbSize) {
            thumbProgressTem = mThumbMax - mThumbSize - mThumbProgress;
        }
        return thumbProgressTem;
    }

    private OnRangeChangeListener mRangeChangeListener;

    public void setOnRangeChangeListener(OnRangeChangeListener listener) {
        this.mRangeChangeListener = listener;
    }

    public interface OnRangeChangeListener {
        void onChanged(int startTime, int endTime, boolean touch);
    }
}