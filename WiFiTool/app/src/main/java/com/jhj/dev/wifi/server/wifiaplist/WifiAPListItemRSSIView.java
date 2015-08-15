package com.jhj.dev.wifi.server.wifiaplist;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.jhj.dev.wifi.server.R;
import com.jhj.dev.wifi.server.WifiMan;

/**
 * @author 江华健
 */
public class WifiAPListItemRSSIView extends View {

    private Resources resources;
    /**
     * 信号值框架画笔
     */
    private Paint framePaint;

    /**
     * 信号值长条画笔
     */
    private Paint stripPaint_parent;

    /**
     * 信号值长条画笔
     */
    private Paint stripPaint_child;

    /**
     * 信号值数字画笔
     */
    private Paint RSSIValuePaint;

    /**
     * 框架绘制数据
     */
    private RectF frameRect;

    /**
     * 信号值长条的宽度
     */
    private float rSSIStripW;

    private float rSSIStripH;

    /**
     * Wifi管理员
     */
    private WifiMan wifiMan;

    /**
     * 是否是组
     */
    private boolean isGroup;

    /**
     * 接入点在显示列表里的位置
     */
    private int[] positions;

    public WifiAPListItemRSSIView(Context context)
    {
        super(context);

    }

    public WifiAPListItemRSSIView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        wifiMan = WifiMan.getInstance(context);
        resources = context.getResources();
        rSSIStripW = resources.getDimension(R.dimen.ap_list_item_rssi_Strip_width);
        rSSIStripH = resources.getDimension(R.dimen.ap_list_item_rssi_Strip_height);
        initDrawData(rSSIStripW, rSSIStripH);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        //System.out.println("w--->"+w+",h--->"+h);


    }

    private void initDrawData(float viewW, float viewH)
    {
        framePaint = new Paint();
        framePaint.setARGB(255, 220, 220, 220);
        framePaint.setAntiAlias(true);
        framePaint.setStyle(Paint.Style.STROKE);

        frameRect = new RectF(0, 0, viewW, viewH);

        stripPaint_parent = new Paint();
        stripPaint_parent.setARGB(255, 0, 139, 0);
        stripPaint_parent.setAntiAlias(true);
        stripPaint_parent.setStyle(Paint.Style.FILL);

        stripPaint_child = new Paint(stripPaint_parent);
        stripPaint_child.setARGB(255, 74, 112, 139);

        RSSIValuePaint = new Paint();
        RSSIValuePaint.setARGB(255, 220, 220, 220);
        RSSIValuePaint.setAntiAlias(true);
        RSSIValuePaint.setTypeface(Typeface.create("Courier New", Typeface.NORMAL));
        RSSIValuePaint
                .setTextSize(getResources().getDimension(R.dimen.ap_list_item_rssi_text_size));
    }


    /**
     * @param isGroup  是否拥有同名接入点
     * @param position 接入点在列表里的位置
     */
    public void setIsGroup(boolean isGroup, int... position)
    {
        this.isGroup = isGroup;
        this.positions = position;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        drawRSSIStripFrame(canvas);
        drawRSSIStrip(canvas);
    }

    /**
     * 绘制信号值长条框架
     *
     * @param canvas 画布
     */
    private void drawRSSIStripFrame(Canvas canvas)
    {
        //		System.out.println("getWidth()--->"+getWidth()+",getHeight()--->"+getHeight());
        //canvas.drawLines(framePts, framePaint);

        canvas.drawRoundRect(frameRect,
                             resources.getDimension(R.dimen.ap_list_item_rssi_Strip_x_radius),
                             resources.getDimension(R.dimen.ap_list_item_rssi_Strip_y_radius),
                             framePaint);

    }

    /**
     * 绘制信号值长条
     *
     * @param canvas 画布
     */
    private void drawRSSIStrip(Canvas canvas)
    {
        int apRSSI = wifiMan.getWifiAPRSSI(isGroup, positions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //since api 21
            canvas.drawRoundRect(frameRect.left, frameRect.top,
                                 frameRect.left + (100 - Math.abs(apRSSI)) / 100f * rSSIStripW,
                                 frameRect.bottom,
                                 resources.getDimension(R.dimen.ap_list_item_rssi_Strip_x_radius),
                                 resources.getDimension(R.dimen.ap_list_item_rssi_Strip_y_radius),
                                 isGroup ? stripPaint_parent : stripPaint_child);
        } else {
            canvas.drawRoundRect(
                    new RectF(frameRect.left, frameRect.top, frameRect.left + frameRect.left +
                                                             (100 - Math.abs(apRSSI)) / 100f *
                                                             rSSIStripW, frameRect.bottom),
                    resources.getDimension(R.dimen.ap_list_item_rssi_Strip_x_radius),
                    resources.getDimension(R.dimen.ap_list_item_rssi_Strip_y_radius),
                    isGroup ? stripPaint_parent : stripPaint_child);
        }
        String rSSI = apRSSI + " dBm";
        float rSSIX = (rSSIStripW - RSSIValuePaint.measureText(rSSI)) / 2;
        float rSSIY =
                (frameRect.height() - (RSSIValuePaint.descent() + RSSIValuePaint.ascent())) / 2;
        canvas.drawText(apRSSI + " dBm", rSSIX, rSSIY, RSSIValuePaint);

    }

}
