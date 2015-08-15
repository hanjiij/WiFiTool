package com.jhj.dev.wifi.server.model;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.jhj.dev.wifi.server.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author 江华健
 */
public class GUIDrawData {
    /**
     * 信号折线图的绘制路径
     */
    public static final Path BROKENLINEPATH = new Path();
    /**
     * 高亮显示接入点的绘制路径
     */
    public static final Path BROKENLINEHIGHLIGHTPATH = new Path();
    /**
     * 随机获取折线的颜色
     */
    private static final Random COLORRANDOM = new Random();
    /**
     * 1280*720 16:9 4.3inch
     */

    private static GUIDrawData drawParam;
    public float yIntervalDivideRSSI;

    public int mapWidthDivideXInterval;

    public float rssiMapDrawBLOffX;

    /**
     * 信号指示值的横坐标
     */
    public float valueX;

    /**
     * 信号指示值的纵坐标集
     */
    public float valueY[];
    /**
     * 绘制框架的数据集
     */
    public RectF rssiMapRect = new RectF();
    private Context appContext;
    private Resources resources;
    /**
     * 过滤后的信号地图数据
     */
    private WifiFilteredMapData filteredMapData;
    /**
     * 存放所有AP的画笔Map
     */
    private Map<String, Paint> paintMap = new HashMap<String, Paint>();
    /**
     * 绘制信号折线的的路径效果
     */
    private PathEffect corPathEffect;
    /**
     * 高亮绘制接入点折线图的画笔
     */
    private Paint brokenLineHighLightPaint = new Paint();
    /**
     * 坐标框架画笔
     */
    private Paint framePaint = new Paint();
    /**
     * 信号指示辅助虚线画笔
     */
    private Paint dashLinePaint = new Paint();
    /**
     * 虚线路径
     */
    private ArrayList<Path> dashLinePaths = new ArrayList<Path>();
    /**
     * 绘制信号指示值画笔
     */
    private Paint rssiValuePaint = new Paint();

	/*
     *
	 * */
    /**
     * 信号指示数值集
     */
    private String[] rssiValues;
    /**
     * 绘制信号单位画笔
     */
    private Paint rssiUnitPaint = new Paint();
    /**
     * 信号单位文本
     */
    private String rssiUnit;
    /**
     * 绘制信号单位路径
     */
    private Path rssiUnitPath = new Path();

    public GUIDrawData(Context context)
    {
        appContext = context.getApplicationContext();
        resources = appContext.getResources();

        filteredMapData = WifiFilteredMapData.getInstance(context);

        initDrawData();
    }

    public static GUIDrawData getInstance(Context context)
    {
        if (drawParam == null) {
            drawParam = new GUIDrawData(context);
        }
        return drawParam;
    }

    public float getyIntervalDivideRSSI()
    {
        return yIntervalDivideRSSI;
    }

    public int getMapWidthDivideXInterval()
    {
        return mapWidthDivideXInterval;
    }

    public float getRssiMapDrawBLOffX()
    {
        return rssiMapDrawBLOffX;
    }

    /**
     * 创建wifi信号折线图画笔
     */
    public void createBrokenLinePaints(List<LinkedList<Integer>> apList)
    {
        for (LinkedList<Integer> ap : apList) {
            String apBSSID = filteredMapData.getFilteredAPBSSID(ap);
            if (!paintMap.containsKey(apBSSID)) {
                Paint brokenLinePaint = new Paint();
                setBrokenLinePaint(brokenLinePaint);
                paintMap.put(apBSSID, brokenLinePaint);
            }

        }

    }

    /**
     * 折线画笔的基本设置
     *
     * @param brokenLinePaint 折线画笔
     */
    private void setBrokenLinePaint(Paint brokenLinePaint)
    {
        brokenLinePaint.setARGB(255, COLORRANDOM.nextInt(256), COLORRANDOM.nextInt(256),
                                COLORRANDOM.nextInt(256));
        brokenLinePaint.setStyle(Style.STROKE);
        brokenLinePaint.setAntiAlias(true);
        brokenLinePaint.setStrokeWidth(resources.getDimension(R.dimen.rssi_map_brokenLine_width));
        brokenLinePaint.setPathEffect(corPathEffect);
    }

    /**
     * 通过指定接入点获取折线画笔
     *
     * @param ap 指定接入点
     * @return 与该接入点对应的折线画笔
     */
    public Paint getBorkenLinePaint(LinkedList<Integer> ap)
    {
        return paintMap.get(filteredMapData.getFilteredAPBSSID(ap));

    }

    /**
     * 通过指定接入点获取折线画笔的颜色
     *
     * @param ap 指定接入点
     * @return 折线画笔的颜色
     */
    public int getBrokenLineColor(LinkedList<Integer> ap)
    {
        return paintMap.get(filteredMapData.getFilteredAPBSSID(ap)).getColor();
    }

    /**
     * 通过指定接入点获取高亮绘制接入点折线图的画笔
     *
     * @param ap 指定接入点
     * @return 高亮绘制接入点折线图的画笔
     */
    public Paint getBrokenLineHighLightPaint(LinkedList<Integer> ap)
    {
        brokenLineHighLightPaint.set(getBorkenLinePaint(ap));
        brokenLineHighLightPaint.setStyle(Paint.Style.FILL);
        brokenLineHighLightPaint.setAlpha(100);
        return brokenLineHighLightPaint;
    }

    /**
     * 获取框架的绘制画笔
     *
     * @return 框架的绘制画笔
     */
    public Paint getFramePaint()
    {
        return framePaint;
    }

    public ArrayList<Path> getDashLinePaths()
    {
        return dashLinePaths;
    }

    /**
     * 获取绘制虚线画笔
     *
     * @return 虚线画笔
     */
    public Paint getDashLinePaint()
    {
        return dashLinePaint;
    }

    public String[] getRssiValues()
    {
        return rssiValues;
    }

    /**
     * 获取信号值绘制画笔
     *
     * @return 信号指示值绘制画笔
     */
    public Paint getRSSIValuePaint()
    {
        return rssiValuePaint;
    }

    public String getRssiUnit()
    {
        return rssiUnit;
    }

    public Path getRssiUnitPath()
    {
        return rssiUnitPath;
    }

    /**
     * 获取绘制信号单位文本画笔
     *
     * @return 绘制信号单位文本画笔
     */
    public Paint getRSSIUnitPaint()
    {
        return rssiUnitPaint;
    }

    /**
     * 获取坐标框架绘制数据
     *
     * @return
     */
    public RectF getRssiMapRect()
    {
        return rssiMapRect;
    }

    /**
     * 获取信号指示值的横坐标
     *
     * @return
     */
    public float getValueX()
    {
        return valueX;
    }

    /**
     * 获取信号指示值的纵坐标集
     *
     * @return
     */
    public float[] getValueY()
    {
        return valueY;
    }

    /**
     * 初始化绘制数据
     */
    private void initDrawData()
    {
        rssiMapRect.left = resources.getDimension(R.dimen.rssi_map_rect_left);
        rssiMapRect.top = resources.getDimension(R.dimen.rssi_map_rect_top);
        rssiMapRect.right = resources.getDimension(R.dimen.rssi_map_rect_right);
        rssiMapRect.bottom = resources.getDimension(R.dimen.rssi_map_rect_bottom);

        float yCoordInterval = resources.getDimension(R.dimen.rssi_map_yCoordInterval);
        float rssiMapRectWidth = resources.getDimension(R.dimen.rssi_map_rect_width);
        float rssiMapRectHeight = resources.getDimension(R.dimen.rssi_map_rect_height);
        yIntervalDivideRSSI = yCoordInterval / 10;
        mapWidthDivideXInterval = (int) (rssiMapRectWidth / 60);
        rssiMapDrawBLOffX = resources.getDimension(R.dimen.rssi_map_draw_brokenLine_offX);
        corPathEffect = new CornerPathEffect(
                resources.getDimension(R.dimen.rssi_map_brokenLine_pathEffect_corner_radius));
        rssiValues = resources.getStringArray(R.array.rssi_map_value_indicator);
        rssiUnit = resources.getString(R.string.rssiUnit);


        framePaint.setARGB(255, 136, 136, 136);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(resources.getDimension(R.dimen.rssi_map_axis_width));

        PathEffect dashPathEffect = new DashPathEffect(
                new float[]{resources.getDimension(R.dimen.rssi_map_dash_interval),
                            resources.getDimension(R.dimen.rssi_map_dash_interval)}, 0);
        dashLinePaint.setStyle(Paint.Style.STROKE);
        dashLinePaint.setStrokeWidth(0);
        dashLinePaint.setARGB(255, 136, 136, 136);
        dashLinePaint.setPathEffect(dashPathEffect);
        TypedArray dashPts = resources.obtainTypedArray(R.array.rssi_map_dash_pts_arr);
        for (int i = 0; i < dashPts.length(); i++) {
            Path path = new Path();
            path.moveTo(dashPts.getDimension(i, 0), dashPts.getDimension(++i, 0));
            path.lineTo(dashPts.getDimension(++i, 0), dashPts.getDimension(++i, 0));
            dashLinePaths.add(path);
        }


        rssiValuePaint.setARGB(255, 136, 136, 136);
        rssiValuePaint
                .setTextSize(resources.getDimension(R.dimen.rssi_map_rssiValueIndicator_text_size));
        rssiValuePaint.setTypeface(Typeface.create("Courier New", Typeface.NORMAL));
        rssiValuePaint.setAntiAlias(true);
        valueX = rssiMapRect.left - rssiValuePaint.measureText("-80") -
                 resources.getDimension(R.dimen.rssi_map_rssiValueIndicator_value_right_margin);
        float rssiValue_ninty_y = rssiMapRect.top + rssiMapRectHeight - yCoordInterval -
                                  (rssiValuePaint.descent() + rssiValuePaint.ascent()) / 2;
        valueY = new float[]{rssiValue_ninty_y, rssiValue_ninty_y - yCoordInterval * 1,
                             rssiValue_ninty_y - yCoordInterval * 2,
                             rssiValue_ninty_y - yCoordInterval * 3,
                             rssiValue_ninty_y - yCoordInterval * 4,
                             rssiValue_ninty_y - yCoordInterval * 5,
                             rssiValue_ninty_y - yCoordInterval * 6};


        rssiUnitPaint.setARGB(255, 136, 136, 136);
        rssiUnitPaint.setTypeface(Typeface.create("Courier New", Typeface.NORMAL));
        rssiUnitPaint.setTextSize(resources.getDimension(R.dimen.rssi_map_rssiUnit_text_size));
        rssiUnitPaint.setAntiAlias(true);

        float rssiUnitX = (valueX - (rssiUnitPaint.descent() + rssiUnitPaint.ascent())) / 2;
        float rssiUnitHeight = rssiUnitPaint.measureText(rssiUnit);
        float rssiUnitY = rssiMapRect.bottom - (rssiMapRectHeight - rssiUnitHeight) / 2;
        rssiUnitPath.moveTo(rssiUnitX, rssiUnitY);
        rssiUnitPath.lineTo(rssiUnitX, rssiUnitY - rssiUnitHeight);
    }

}
