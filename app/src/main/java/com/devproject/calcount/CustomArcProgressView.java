package com.devproject.calcount;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CustomArcProgressView extends View {
    private Paint progressPaint;
    private Paint backgroundPaint;
    private int progress = 0;
    private int max = 5000; // default max

    public CustomArcProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        progressPaint = new Paint();
        progressPaint.setColor(Color.parseColor("#FF6F00"));
        progressPaint.setStrokeWidth(50f);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#E6DCF6"));
        backgroundPaint.setStrokeWidth(50f);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int size = Math.min(getWidth(), getHeight()) - 60;
        int left = (getWidth() - size) / 2;
        int top = (getHeight() - size) / 2;
        RectF rect = new RectF(left, top, left + size, top + size);

        float startAngle = 135f;
        float sweepAngle = 270f;

        canvas.drawArc(rect, startAngle, sweepAngle, false, backgroundPaint);

        float progressAngle = sweepAngle * progress / (float) max;
        canvas.drawArc(rect, startAngle, progressAngle, false, progressPaint);
    }

    // Normal progress update (without animation)
    public void setProgress(int value) {
        this.progress = Math.min(value, max);
        invalidate();
    }

    // Animated progress update
    public void setProgressWithAnimation(int value) {
        int newValue = Math.min(value, max);

        ValueAnimator animator = ValueAnimator.ofInt(this.progress, newValue);
        animator.setDuration(800); // 0.8 second animation
        animator.addUpdateListener(animation -> {
            this.progress = (int) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }
}
