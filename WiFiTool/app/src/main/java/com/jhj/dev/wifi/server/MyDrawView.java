package com.jhj.dev.wifi.server;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.jhj.dev.wifi.server.fragment.FinderFragment;
import com.jhj.dev.wifi.server.util.HardwareAddress;

import java.util.ArrayList;

/**
 * @author 吉鹏
 */
public class MyDrawView extends View {

    private final int Max = 16;
    private Bitmap router;
    private Bitmap computer;
    private Paint paint;// 网线笔
    private Paint blackPaint;// 主机名笔
    private int w = 400;
    private int h = 400;
    private int counts;// 总的设备数量
    private int dateSum;
    private ArrayList<String> namelist;
    private int p;// 横向间隔
    private OnListenerCount onListenerCount;//数据监听
    private int select;


    public MyDrawView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public MyDrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
    }

    public MyDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        // 主机名画笔
        blackPaint = new Paint();
        blackPaint.setColor(Color.RED);
        blackPaint.setTextSize(25);
        // 画线
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(4);

        // 路由器贴图
        router = BitmapFactory.decodeResource(getResources(), R.drawable.router);
        computer = BitmapFactory
                .decodeResource(getResources(), R.drawable.computer);// 设备贴图

        //		System.out.println("====>w,h" + w + "--" + h);

        for (int i = 0; i < namelist.size(); i++) {
            System.out.println("主机名======>" + namelist.get(i));
        }

        try {
            if (counts <= Max) {// 当设备小于16台的时候

                setDrawLayout(counts, canvas);// 设置绘画布局
            } else {
                int more = counts - Max;
                setDrawLayout(Max, canvas);
                Toast.makeText(getContext(), "因屏幕问题有" + more + "个设备没有显示", Toast.LENGTH_SHORT)
                     .show();

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Toast.makeText(getContext(), "当前没有设备", Toast.LENGTH_SHORT).show();
        }

    }

    public void setActivity(Activity activity) {
        //获得屏幕的大小
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        w = dm.widthPixels;
        h = dm.heightPixels;

        //监听拿到的主机名
        HardwareAddress hardwareAddress = new HardwareAddress(activity);
        hardwareAddress.setOnNameListener(new HardwareAddress.OnNameListener() {

            @Override
            public void onNameArrayList(ArrayList<String> arrayList) {
                // TODO Auto-generated method stub
                namelist = arrayList;
            }
        });
        //获取局域网内设备的数量
        counts = FinderFragment.adapter.getCount() - 1;

    }

    //设置排版格式
    private void setDrawLayout(int count, Canvas canvas) {
        // TODO Auto-generated method stub
        dateSum = 0;
        if (count % 2 == 0) {
            select = 0;
            dateSum = count / 2;// 每一行的贴图数量
            p = w / dateSum;// 屏幕大小与数量匹配
            drawup(dateSum, canvas);
            drawdown(dateSum, canvas);

        } else {
            select = 1;
            dateSum = count / 2;
            p = w / (dateSum + 1);
            drawup(dateSum + 1, canvas);
            drawdown(dateSum, canvas);

        }
    }

    // 放在上面一行
    private void drawup(int dateSum, Canvas canvas) {
        // TODO Auto-generated method stub
        int s = 0;
        for (int i = 0; i < dateSum; i++) {

            canvas.drawBitmap(computer, s + 30, h / 4, paint);// h / 2 + 300
            if (namelist.get(i) != null) {
                canvas.drawText(namelist.get(i), s + 30, h / 4, blackPaint);
            } else {
                canvas.drawText("未知品牌", s + 30, h / 4, blackPaint);
            }

            canvas.drawLine(w / 2, h / 2, s + 50, h / 4 + 60, paint);
            s = s + p;

        }
    }

    // 放在下面一行
    private void drawdown(int su, Canvas canvas) {


        int sum = 0;
        int j;
        for (int i = 0; i < su; i++) {
            j = dateSum + i + select;
            canvas.drawBitmap(computer, sum + 30, 3 * h / 4, paint);
            canvas.drawLine(w / 2, h / 2, sum + 50, 3 * h / 4 - 10, paint);
            try {
                if (namelist.get(j) != null) {
                    canvas.drawText(namelist.get(j), sum + 30, 3 * h / 4, blackPaint);//
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                canvas.drawText("未知品牌", sum + 30, 3 * h / 4, blackPaint);
            }

            sum = sum + p;// 彼此间的间隔

        }
        canvas.drawBitmap(router, w / 2 - 80, h / 2 - 50, paint);//////
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        sendDates(counts);
        return super.onTouchEvent(event);
    }

    private void sendDates(int counts2) {
        // TODO Auto-generated method stub
        onListenerCount.OnListenerCountChanged(counts);
    }

    public void setOnListenerCount(OnListenerCount onListenerCount) {
        this.onListenerCount = onListenerCount;
    }

    //数据监听接口
    public interface OnListenerCount {
        void OnListenerCountChanged(int date);
    }

}
