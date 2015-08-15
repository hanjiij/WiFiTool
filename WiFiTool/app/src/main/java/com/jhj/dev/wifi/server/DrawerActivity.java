package com.jhj.dev.wifi.server;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.jhj.dev.wifi.server.MyDrawView.OnListenerCount;

/**
 * @author 吉鹏
 */
public class DrawerActivity extends Activity {

    MyDrawView myDrawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_drawer);

        myDrawView = (MyDrawView) findViewById(R.id.myDrawView1);
        myDrawView.setActivity(this);

        myDrawView.setOnListenerCount(new OnListenerCount() {

            @Override
            public void OnListenerCountChanged(int date) {
                // TODO Auto-generated method stub
                Toast.makeText(DrawerActivity.this, "当前邻居数量：" + date, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
