package com.jhj.dev.wifi.server.wifirssimap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.jhj.dev.wifi.server.model.GUIDrawData;
import com.jhj.dev.wifi.server.model.WifiMapData;
import com.jhj.dev.wifi.server.model.WifiMapData.OnRSSIChangedListener;

import java.util.LinkedList;
import java.util.List;

/**
 * @author 江华健
 */
public class WifiRSSIMapView extends SurfaceView implements OnRSSIChangedListener {
    /**
     * 刷新wifi信号任务
     */
    private RefreshWifiRSSITask refreshWifiRSSITask;

    /**
     * surface监听器
     */
    private SurfaceHolder surfaceHolder;

    /**
     * wifi信号监听器
     */
    private WifiRSSIMonitor rssiMonitor;

    /**
     * wifi信号地图绘制数据提供者
     */
    private WifiMapData mapData;


    //private WifiFilteredMapData filteredMapData;

    /**
     * 绘制数据提供者
     */
    private GUIDrawData drawData;


    /**
     * 是否高亮显示APRSSI折线图
     */
    private boolean isHighLightShow;

    /**
     * 需要高亮显示的AP折线图
     */
    private LinkedList<Integer> apRequireHL;

    /**
     * wifi信息改变监听器
     */
    private OnWifiInfoChangedListener listener;

    // private GUIDrawDataTest drawData;

    public WifiRSSIMapView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mapData = WifiMapData.getInstance(getContext());
        mapData.setOnRSSIChangedListener(WifiRSSIMapView.this);

        //filteredMapData=WifiFilteredMapData.getInstance();

        rssiMonitor = WifiRSSIMonitor.getInstance(getContext());

        drawData = GUIDrawData.getInstance(context);
        //drawData=GUIDrawDataTest.getInstance();

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(new SurfaceListener());

    }

    public WifiRSSIMapView(Context context)
    {
        super(context);
    }

    /**
     * 设置Wifi信息更改监听器
     *
     * @param listener Wifi信息更改监听器
     */
    public void setOnWifiInfoChangedListener(OnWifiInfoChangedListener listener)
    {
        this.listener = listener;
    }

    /**
     * 初始化信号地图和启动绘制任务
     */
    private void init()
    {
        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawARGB(255, 48, 48, 48);
        drawWifiRSSIMapCoordinate(canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);


        refreshWifiRSSITask = new RefreshWifiRSSITask();
        refreshWifiRSSITask.start();
        refreshWifiRSSITask.getLooper();


        rssiMonitor.startMonitor();

    }

    /**
     * 刷新wifi信号地图
     *
     * @param apList 接入点列表
     */
    public void refreshWifiRSSIMap(List<LinkedList<Integer>> apList)
    {
        if (surfaceHolder.getSurface() != null) {
            Canvas canvas = surfaceHolder.lockCanvas();

            canvas.drawARGB(255, 48, 48, 48);

            drawWifiRSSIMapCoordinate(canvas);

            drawWifiRSSIBrokenLines(canvas, apList);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * 设置高亮显示指定AP信号图
     *
     * @param isHightLightShow 是否高亮显示
     * @param apRequireHL      需要高亮显示的接入点
     */
    public void setIsHightLightShow(boolean isHightLightShow, LinkedList<Integer> apRequireHL)
    {
        this.isHighLightShow = isHightLightShow;
        this.apRequireHL = apRequireHL;
    }

    /**
     * 绘制信号折线图
     *
     * @param canvas 画布
     * @param apList 接入点列表
     */
    private void drawWifiRSSIBrokenLines(Canvas canvas, List<LinkedList<Integer>> apList)
    {
        drawData.createBrokenLinePaints(apList);

        listener.onWifiInfoChanged(apList);

        RectF rssiMapRect = drawData.getRssiMapRect();

        for (int i = 0; i < apList.size(); i++) {
            drawWifiRSSIBrokenLine(canvas, apList.get(i), rssiMapRect);
        }
    }

    /**
     * 设置基本折线图路径数据
     *
     * @param ap       接入点
     * @param drawPath 绘制路径效果
     */
    private void setBrokenLineBasePathData(LinkedList<Integer> ap, Path drawPath, RectF rssiMapRect)
    {
        drawPath.reset();
        drawPath.moveTo(getStartX(ap.size() - 1, ap, drawData.getRssiMapDrawBLOffX(), rssiMapRect),
                        rssiMapRect.bottom -
                        drawData.getyIntervalDivideRSSI() * (100 - Math.abs(ap.get(0))));

        for (int i = 1; i < ap.size(); i++) {
            drawPath.lineTo(
                    rssiMapRect.right - (ap.size() - i - 1) * drawData.getMapWidthDivideXInterval(),
                    rssiMapRect.bottom -
                    drawData.getyIntervalDivideRSSI() * (100 - Math.abs(ap.get(i))));
        }
    }

    /**
     * 获取绘制的起点的横坐标
     *
     * @param how  移动多少格
     * @param ap   接入点
     * @param offX 横坐标偏移量
     * @return 绘制的起点的横坐标
     */
    private float getStartX(int how, LinkedList<Integer> ap, float offX, RectF rssiMapRect)
    {
        float startX = rssiMapRect.right - how * drawData.getMapWidthDivideXInterval();
        if (ap.size() == 62) {
            float left = rssiMapRect.left;
            if (startX != left) {
                startX = left + offX;
            }
        }
        return startX;
    }

    /**
     * 绘制wifi折线图
     *
     * @param canvas 画笔
     * @param ap     接入点
     */
    private void drawWifiRSSIBrokenLine(Canvas canvas, LinkedList<Integer> ap, RectF rssiMapRect)
    {
        setBrokenLineBasePathData(ap, GUIDrawData.BROKENLINEPATH, rssiMapRect);
        canvas.drawPath(GUIDrawData.BROKENLINEPATH, drawData.getBorkenLinePaint(ap));


        if (isHighLightShow && apRequireHL == ap) {
            setBrokenLineBasePathData(ap, GUIDrawData.BROKENLINEHIGHLIGHTPATH, rssiMapRect);
            GUIDrawData.BROKENLINEHIGHLIGHTPATH.lineTo(rssiMapRect.right, rssiMapRect.bottom);
            GUIDrawData.BROKENLINEHIGHLIGHTPATH
                    .lineTo(getStartX(ap.size() - 1, ap, drawData.getRssiMapDrawBLOffX() / 2,
                                      rssiMapRect), rssiMapRect.bottom);
            GUIDrawData.BROKENLINEHIGHLIGHTPATH
                    .lineTo(getStartX(ap.size() - 1, ap, drawData.getRssiMapDrawBLOffX() / 2,
                                      rssiMapRect), rssiMapRect.bottom -
                                                    drawData.getyIntervalDivideRSSI() *
                                                    (100 - Math.abs(ap.get(0))));
            canvas.drawPath(GUIDrawData.BROKENLINEHIGHLIGHTPATH,
                            drawData.getBrokenLineHighLightPaint(ap));
        }
    }

    /**
     * 绘制Wifi信号图坐标
     *
     * @param canvas 画布
     */
    private void drawWifiRSSIMapCoordinate(Canvas canvas)
    {
        canvas.drawRect(drawData.getRssiMapRect(), drawData.getFramePaint());
        Paint dashLinePaint = drawData.getDashLinePaint();
        //绘制辅助虚线
        for (Path dashPath : drawData.getDashLinePaths()) {
            canvas.drawPath(dashPath, dashLinePaint);
        }


        //绘制RSSI指示值
        final float[] valueY = drawData.getValueY();
        final float valueX = drawData.getValueX();
        String[] rssiValues = drawData.getRssiValues();
        Paint rSSIValuePaint = drawData.getRSSIValuePaint();
        for (int i = 0; i < rssiValues.length; i++) {
            canvas.drawText(rssiValues[i], valueX, valueY[i], rSSIValuePaint);
        }
        //		float textWidth=rSSIValuePaint.measureText("-80");
        //		System.out.println("textWidth-------->"+textWidth);
        //绘制RSSI单位指示文本
        canvas.drawTextOnPath(drawData.getRssiUnit(), drawData.getRssiUnitPath(), 0, 0,
                              drawData.getRSSIUnitPaint());

        //		float unitWidth=drawData.getRSSIUnitPaint().measureText("-80");
        //		System.out.println("unitWidth-------->"+unitWidth);
    }

    /*
     * 当有新Wifi信号值时刷新信号图
     * */
    @Override
    public void onRSSIChanged(List<LinkedList<Integer>> apList)
    {
        refreshWifiRSSITask.refreshRSSI(apList);

    }

    /**
     * Wifi信息改变监听器
     */
    public interface OnWifiInfoChangedListener {
        /**
         * 当Wifi信息改变时回调该方法
         *
         * @param apList 接入点列表
         */
        void onWifiInfoChanged(List<LinkedList<Integer>> apList);
    }

    /**
     * Surface生命周期 监听器
     */
    private final class SurfaceListener implements SurfaceHolder.Callback {

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
            //停止监听
            rssiMonitor.stopMonitor();
            //终端刷新任务
            refreshWifiRSSITask.quit();

        }
    }

    /**
     * 刷新信号地图任务
     */
    private class RefreshWifiRSSITask extends HandlerThread {
        private static final String NAME = "refreshwifirssitask";
        private static final int MESSAGE_REFRESHVIEW = 0;
        //刷新界面handler
        private Handler refreshHandler;

        public RefreshWifiRSSITask()
        {
            super(NAME);

        }

        /*
         * 刷新信号图
         *
         * */
        public void refreshRSSI(List<LinkedList<Integer>> apList)
        {
            refreshHandler.obtainMessage(MESSAGE_REFRESHVIEW, apList).sendToTarget();
        }

        @Override
        protected void onLooperPrepared()
        {
            super.onLooperPrepared();
            refreshHandler = new Handler() {

                @Override
                public void handleMessage(Message msg)
                {
                    if (msg.what == MESSAGE_REFRESHVIEW) {
                        @SuppressWarnings("unchecked")
                        List<LinkedList<Integer>> apList = (List<LinkedList<Integer>>) msg.obj;

                        refreshWifiRSSIMap(apList);
                    }

                }

            };
        }
    }


}
