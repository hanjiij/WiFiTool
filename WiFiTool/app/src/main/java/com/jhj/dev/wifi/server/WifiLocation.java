package com.jhj.dev.wifi.server;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.jhj.dev.wifi.server.mydatabase.MyDatabase;
import com.jhj.dev.wifi.server.util.InitMacSQL;
import com.jhj.dev.wifi.server.util.SetWifiPoint;

import java.util.List;
import java.util.Map;
import java.util.Timer;

/**
 * @author 韩吉
 */
public class WifiLocation extends Activity {

    SetWifiPoint setWifiPoint;
    Button open, close, print, breakwifiButton, star;
    TextView textView;
    String wifiname = "sa323"; // 指定需要显示的WiFi的名称
    List<Map<String, Double>> point; // wifi列表,XY坐标信息
    List<List<ScanResult>> wifiList; // wifi列表强度信息

    ProgressDialog islocationProgressDialog, searchingProgressDialog;

    boolean isLocationSuccess = false;

    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_location);

        setWifiPoint = new SetWifiPoint(getApplicationContext(), 175);

        open = (Button) findViewById(R.id.open);
        close = (Button) findViewById(R.id.close);
        print = (Button) findViewById(R.id.print);
        breakwifiButton = (Button) findViewById(R.id.breakwifi);
        star = (Button) findViewById(R.id.star);


        textView = (TextView) findViewById(R.id.text);

        open.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // setWifiPoint.StartSetWifiPoint();
                // open.setText("正在记录数据");
                // open.setEnabled(false);

                new InitMacSQL(WifiLocation.this);

            }
        });

        close.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // setWifiPoint.SetSalculate();
                // open.setText("开启");
                // open.setEnabled(true);
                // print.setEnabled(true);
                //
                // textView.setText("");
                //
                // for (int i = 0; i < point.size(); i++) {
                //
                // textView.append("第" + (i + 1) + "步的信号强度："
                // + getwifinameLevel(wifiList.get(i), wifiname)
                // + "坐标：(" + point.get(i).get("X") + ","
                // + point.get(i).get("Y") + ")\n");
                // }

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        System.out.println("开始：" + System.currentTimeMillis());

                        SQLiteDatabase db =
                                new MyDatabase(WifiLocation.this, "Points").getWritableDatabase();

                        Cursor cursor = db.rawQuery("select * from MacInfoSql  where mac=?",
                                                    new String[]{"00-03-93"});
                        cursor.moveToNext();
                        System.out.println(cursor.getString(1));

                        System.out.println("结束：" + System.currentTimeMillis());

                    }
                }).start();

            }
        });

        print.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                SQLiteDatabase db =
                        new MyDatabase(getApplicationContext(), "Points").getWritableDatabase();

                Cursor cursor =
                        db.query("PointsSql", new String[]{"ssid", "mac", "Lat", "Lng", "Level"},
                                 null, null, null, null, null);

                while (cursor.moveToNext()) {

                    String ssidString = cursor.getString(0);
                    String macString = cursor.getString(1);
                    String LatString = cursor.getString(2);
                    String LngString = cursor.getString(3);
                    String levelString = cursor.getString(4);

                    textView.append(
                            ssidString + "==" + macString + "=" + LatString + "=" + LngString +
                            "=" + levelString + "\n");
                }
            }
        });

        breakwifiButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


            }
        });

        star.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                //				startSearchWifi();
            }
        });

    }


    /**
     * @param wifi wifi列表数据
     * @param name 所要查找WiFi强度的ssid
     * @return 返回信号强度值，未找到则返回0
     */
    public int getwifinameLevel(List<ScanResult> wifi, String name) {
        for (ScanResult scanResult : wifi) {
            if (scanResult.SSID.equals(name)) {
                return scanResult.level;
            }
        }
        return 0;
    }
}
