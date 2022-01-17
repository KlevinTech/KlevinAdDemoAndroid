package com.tencent.klevinDemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.tencent.klevinDemo.R;
import com.tencent.klevinDemo.utils.PxUtil;

/**
 * 线性的进度条，如果需要线性进度则采用LinearGradient，且绘制底色
 * <p>
 * 实现思路：只需要画一条线就行。如果需要的话，可以再绘制一条空白的100%背景进度。
 */

public class LinearProgressBar extends View {

    private Paint progressPaint;
    private int progress;
    private int totalProgress = 100;
    private boolean gradient = false;
    // private int gradientStartColor = Color.parseColor("#C0D2F1"); // 线性渐变的初始颜色，后来设计师又改成了纯色，所以现在初始颜色和结束颜色一样
    private int gradientStartColor = Color.parseColor("#3185FC"); // 线性渐变的初始颜色
    private int gradientEndColor = Color.parseColor("#3185FC"); // 线性渐变的结束颜色
    private int gradientBackgroundColor = Color.parseColor("#d8d8d8"); // 线性渐变的背景色
    private Paint gradientBackgroundPaint;
    private Paint backgroundStrokePaint;
    private Path path = new Path();
    private float[] radiusArray = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
    private RectF rectF;
    private Paint contentPaint;
    private String content;
    private Context mContext;
    public void setGradient(boolean gradient) {
        this.gradient = gradient;
    }

    public LinearProgressBar(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        init(context);
    }

    public LinearProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        init(context);
        setRoundRadius(PxUtil.dpToPx(context, 16));
    }

    private void init(Context context) {
        mContext = context;
        content = "立即下载";
        progress = 100;
        progressPaint = new Paint();
        contentPaint = new Paint();
        contentPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        contentPaint.setAntiAlias(true);
        contentPaint.setColor(context.getResources().getColor(R.color.klevin_reward_downloading_progressbar_default_content_color));
        contentPaint.setTextSize(PxUtil.dpToPx(context, 14));
        gradientBackgroundPaint = new Paint();
        backgroundStrokePaint = new Paint();
        backgroundStrokePaint.setStyle(Paint.Style.STROKE);
        backgroundStrokePaint.setAntiAlias(true);
        backgroundStrokePaint.setStrokeWidth(PxUtil.dpToPx(context, 2));
        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        rectF.set(0, 0, getWidth(), getHeight());
        path.addRoundRect(rectF, radiusArray, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
        if (progress >= 0) {
            int width = getMeasuredWidth(); // 进度条宽度
            int height = getMeasuredHeight(); // 进度条高度
            float startX = 0;
            float startY = (float) height / 2;
            float endY = startY;
            float stopX = (float) progress / totalProgress * width;

            // 渐变进度条
            if (gradient) {
                if (progress > 0 && progress < 100) {
                    // 先画背景色
                    gradientBackgroundPaint.setColor(gradientBackgroundColor);
                    drawRect(canvas, 0, 0, width, height, gradientBackgroundPaint);
                    // 再画渐变的进度条
                    progressPaint.setShader(new LinearGradient(startX, startY, stopX, endY,
                            gradientStartColor, gradientEndColor, LinearGradient.TileMode.CLAMP));
                    progressPaint.setStyle(Paint.Style.FILL);
                    drawRect(canvas, 0, 0, stopX, height, progressPaint);
                } else {
                    gradientBackgroundPaint.setColor(gradientEndColor);
                    progressPaint.setStyle(Paint.Style.FILL);
                    drawRect(canvas, 0, 0, width, height, gradientBackgroundPaint);
                }
            } else {
                // 其实这里也有背景色
                gradientBackgroundPaint.setColor(Color.parseColor("#FFF7F5"));
                drawRect(canvas, 0, 0, width, height, gradientBackgroundPaint);
                // 背景边框
                backgroundStrokePaint.setColor(Color.parseColor("#FF6740"));
                canvas.drawRoundRect(rectF, 100, 100, backgroundStrokePaint);
                // 纯色进度条
                progressPaint.setColor(Color.parseColor("#FF6740")); // 纯色进度条的颜色，视频播放的进度使用
                drawRect(canvas, 0, 0, stopX, height, progressPaint);
            }
            //获取文本的宽度，但是是一个比较粗略的结果
            if(content!=null){
                //cx,cy为圆的中心点的坐标
                int cx = width / 2;
                int cy = height / 2;

                float textWidth = contentPaint.measureText(content);
                //文字度量
                Paint.FontMetrics fontMetrics = contentPaint.getFontMetrics();
                //得到基线的位置
                float baselineY = cy + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
                //绘制
                canvas.drawText(content, cx - textWidth / 2, baselineY, contentPaint);
            }
        }
        path.reset();
    }

    private void drawRect(Canvas canvas, float left, float top, float right, float bottom, Paint
            paint) {
        rectF.set(left, top, right, bottom);
        canvas.drawRect(rectF, paint);
    }

    public void setProgress(int progress) {
        if (progress <= 0) {
            this.progress = 0;
        } else if (progress >= 100) {
            this.progress = 100;
        } else {
            this.progress = progress;
        }
        postInvalidate();
    }

    public void setProgress(int progress, String content, float textsize, int textcolor) {
        if (progress <= 0) {
            this.progress = 0;
        } else if (progress >= 100) {
            this.progress = 100;
        } else {
            this.progress = progress;
        }
        this.content = content;
        contentPaint.setColor(textcolor);
        contentPaint.setTextSize(PxUtil.dpToPx(mContext, (int) textsize));
        postInvalidate();
    }

    public void setTotalProgress(int totalProgress) {
        this.totalProgress = totalProgress;
    }

    public void setRoundRadius(float radius) {
        if (radiusArray != null && radiusArray.length > 0) {
            for (int i = 0; i < radiusArray.length; i++) {
                radiusArray[i] = radius;
            }
        }
    }
}
