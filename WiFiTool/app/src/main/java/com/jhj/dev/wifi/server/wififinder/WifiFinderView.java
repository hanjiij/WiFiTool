package com.jhj.dev.wifi.server.wififinder;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.jhj.dev.wifi.server.R;


/**
 * @author 江华健
 */
public class WifiFinderView extends SurfaceView
        implements WifiFinderFragment.OnSpecAPRSSIChangeListener
{

    private static final int ARCANIMDURATION = 500;
    private static final float ARCSTARTANGLE = 270;
    private SurfaceHolder surfaceHolder;
    private RefreshPieChartTask refreshPieChartTask;
    private Path scalePath = new Path();
    private Path rSSIPath = new Path();
    private AnimatorSet arcAnimSet;
    private ValueAnimator arcAnim;
    private ObjectAnimator arcColorAnim;
    private int color = 0xffff0000;
    private int currentSpecRSSI = -100;
    private RectF rect;
    private RectF arcRect;
    private Paint circlePaint;

    private Paint arcPaint;

    private Paint scalePaint;

    private float scaleMargin;

    private String rSSIUnit;
    private String[] rSSIScales;
    private float sweepAngle = 0;

    private float cx;

    private float cy;

    private float radius;

    private float margin_horizontal;

    private float margin_top;

    public WifiFinderView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(new SurfaceListener());
    }

    @Override
    protected void onAttachedToWindow()
    {
        Resources resources = getResources();
        rSSIUnit = resources.getString(R.string.rssi_pie_chart_unit);
        rSSIScales = resources.getStringArray(R.array.rssi_pie_chart_circle_scale_indicator);
        scaleMargin = resources.getDimension(R.dimen.rssi_pie_chart_circle_scale_text_margin);
        DisplayMetrics outMetrics = new DisplayMetrics();
        getDisplay().getMetrics(outMetrics);
        int phoneWidthPixels = outMetrics.widthPixels;
        System.out.println("widthPixels---->" + outMetrics.widthPixels);

        scalePaint = new Paint();
        scalePaint.setARGB(255, 136, 136, 136);
        scalePaint
                .setTextSize(resources.getDimension(R.dimen.rssi_pie_chart_circle_scale_text_size));
        scalePaint.setAntiAlias(true);
        scalePaint.setTypeface(Typeface.create("Courier New", Typeface.NORMAL));

        margin_top = scalePaint.measureText(rSSIScales[0]) +
                     resources.getDimension(R.dimen.rssi_pie_chart_top_margin);
        cx = phoneWidthPixels / 2.0f;
        cy = resources.getDimension(R.dimen.rssi_pie_chart_radius) + margin_top;
        radius = resources.getDimension(R.dimen.rssi_pie_chart_radius);
        margin_horizontal = (phoneWidthPixels - radius * 2) / 2.0f;

        circlePaint = new Paint();
        circlePaint.setARGB(255, 136, 136, 136);
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint
                .setStrokeWidth(resources.getDimension(R.dimen.rssi_pie_chart_circle_stroke_width));


        arcPaint = new Paint();
        arcPaint.setStyle(Paint.Style.FILL);
        arcPaint.setAntiAlias(true);
        rect = new RectF(margin_horizontal, margin_top, radius * 2 + margin_horizontal,
                         radius * 2 + margin_top);
        arcRect = new RectF(margin_horizontal, margin_top, radius * 2 + margin_horizontal,
                            radius * 2 + margin_top);
        super.onAttachedToWindow();
    }

    private void refershPieChartView(float sweepAngle)
    {
        if (surfaceHolder.getSurface() != null) {
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                canvas.drawARGB(255, 48, 48, 48);
                drawPieChartView(canvas, sweepAngle);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }

    }

    public void init()
    {
        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawARGB(255, 48, 48, 48);
        drawPieChartView(canvas, 0);
        surfaceHolder.unlockCanvasAndPost(canvas);


        refreshPieChartTask = new RefreshPieChartTask();
        refreshPieChartTask.start();
        refreshPieChartTask.getLooper();


        arcAnim = ValueAnimator.ofFloat();
        arcAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                refreshPieChartTask.refreshView((float) animation.getAnimatedValue());
            }
        });
        arcColorAnim = ObjectAnimator.ofInt(arcPaint, "color", 0xff0000ff, 0xffff0000);
        arcColorAnim.setEvaluator(new ArgbEvaluator());

        arcAnimSet = new AnimatorSet();
        arcAnimSet.play(arcColorAnim).with(arcAnim);
        arcAnimSet.setDuration(ARCANIMDURATION);

    }

    private void drawPieChartView(Canvas canvas, float sweepAngle)
    {
        //绘制外框圆
        canvas.drawCircle(cx, cy, radius, circlePaint);

        //绘制环绕圆的刻度（精确）
        double angle = 0;
        double offAngle = Math.asin((Math.abs(scalePaint.ascent()) - scalePaint.descent()) / 2 /
                                    (radius + scaleMargin));
        for (int i = 0; i < rSSIScales.length; i++) {
            scalePath.reset();
            float txtW = scalePaint.measureText(rSSIScales[i]);
            scalePath.moveTo((float) (cx + (radius + scaleMargin) * Math.sin(angle + offAngle)),
                             (float) (cy - (radius + scaleMargin) * Math.cos(angle + offAngle)));
            scalePath.lineTo(i == 0 || i == 4 ? (float) (cx + (radius + scaleMargin) *
                                                              Math.sin(angle + offAngle))
                                              : (float) (cx + (radius + scaleMargin + txtW) *
                                                              Math.sin(angle + offAngle)),
                             (float) (cy -
                                      (radius + scaleMargin + txtW) * Math.cos(angle + offAngle)));
            canvas.drawTextOnPath(rSSIScales[i], scalePath, 0, 0, scalePaint);
            angle += Math.toRadians(45);
        }

        //绘制信号值单位
        canvas.drawText(rSSIUnit, cx - radius, cy - radius, scalePaint);

        //绘制信号大小扇形图
        canvas.drawArc(arcRect, ARCSTARTANGLE, sweepAngle, true, arcPaint);

        //绘制当前信号值(精确)
        rSSIPath.reset();
        float txt_rSSI_w = scalePaint.measureText(currentSpecRSSI + "");
        double offAngle_rSSI =
                Math.asin((Math.abs(scalePaint.ascent()) - scalePaint.descent()) / 2 /
                          ((radius - txt_rSSI_w) / 2));
        double realSweepAngle = Math.toRadians(sweepAngle / 2) + offAngle_rSSI;
        float startX = (float) (cx + (radius - txt_rSSI_w) / 2 * Math.sin(realSweepAngle));
        float startY = (float) (cy - (radius - txt_rSSI_w) / 2 * Math.cos(realSweepAngle));
        float endX = sweepAngle == 0 ? (float) (cx + (radius - txt_rSSI_w) / 2 *
                                                     Math.sin(realSweepAngle)) : (float) (cx +
                                                                                          ((radius -
                                                                                            txt_rSSI_w) /
                                                                                           2 +
                                                                                           txt_rSSI_w) *
                                                                                          Math.sin(
                                                                                                  realSweepAngle));
        float endY =
                (float) (cy - ((radius - txt_rSSI_w) / 2 + txt_rSSI_w) * Math.cos(realSweepAngle));
        rSSIPath.moveTo(startX, startY);
        rSSIPath.lineTo(endX, endY);
        canvas.drawTextOnPath(currentSpecRSSI + "", rSSIPath, 0, 0, scalePaint);
    }

    private String getHexString(int value) {
        String hexString = Integer.toHexString(value);
        if (hexString.length() == 1) {
            hexString = "0" + hexString;
        }
        return hexString;
    }

    @Override
    public void onSpecAPRSSIChanged(int specAPRSSI)
    {
        currentSpecRSSI = specAPRSSI;

        float sweepAngle = 360 / 80f * (100 - Math.abs(specAPRSSI));

        int currentColor = Color.parseColor("#" + getHexString(255) + getHexString(
                specAPRSSI >= -55 ? 0
                                  : specAPRSSI >= -70 ? 255 - 255 / 50 * (70 - Math.abs(specAPRSSI))
                                                      : 255) + getHexString(specAPRSSI >= -55 ? 255
                                                                                              :
                                                                            specAPRSSI >= -70 ? 255
                                                                                              :
                                                                            specAPRSSI >= -85 ?
                                                                            255 / 15 * (85 -
                                                                                        Math.abs(
                                                                                                specAPRSSI))
                                                                                              : 0) +
                                            getHexString(0));

        arcAnim.setFloatValues(this.sweepAngle, sweepAngle);

        arcColorAnim.setIntValues(this.color, currentColor);
        System.out.println("specAPRSSI---->" + specAPRSSI + " ,sweepAngle--->" + sweepAngle);

        //		arcPaint.setARGB(
        //				255,
        //				specAPRSSI >= -50 ? 0
        //						: 255,
        //				specAPRSSI >= -50 ? 255 : 255 / 50 * (100 - Math
        //						.abs(specAPRSSI)), 0);

        arcAnimSet.start();

        this.color = currentColor;
        this.sweepAngle = sweepAngle;
    }

    private class RefreshPieChartTask extends HandlerThread {
        private static final String TASKNAME = "RefreshPieChartTask";
        private static final int MSG_REFRESHVIEW = 1;
        private Handler refresViewHandler;

        public RefreshPieChartTask()
        {
            super(TASKNAME);
        }

        private void refreshView(float sweepAngle)
        {
            refresViewHandler.obtainMessage(MSG_REFRESHVIEW, sweepAngle).sendToTarget();
        }

        @Override
        protected void onLooperPrepared()
        {
            refresViewHandler = new Handler() {

                @Override
                public void handleMessage(Message msg)
                {
                    if (msg.what == MSG_REFRESHVIEW) {
                        float sweepAngle = (float) msg.obj;
                        refershPieChartView(sweepAngle);
                    }
                    super.handleMessage(msg);
                }
            };
            super.onLooperPrepared();
        }

    }

    private class SurfaceListener implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder)
        {
            init();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder)
        {
            arcAnimSet.cancel();
            refreshPieChartTask.quit();
        }

    }

}
