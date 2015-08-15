package com.jhj.dev.wifi.server.util;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author 韩吉
 */
@SuppressLint("SdCardPath")
public class InitMacSQL {

    static String DATABASES_DIR = "/data/data/com.jhj.dev.wifi.server/databases";
    static String DATABASE_NAME = "Points";
    Context contexts;

    public InitMacSQL(Context context) {

        contexts = context;

        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                copyDatabaseFile(contexts, true);

                System.err.println("数据库写入成功");
            }
        }).start();
    }

    public static void copyDatabaseFile(Context context, boolean isfored) {

        File dir = new File(DATABASES_DIR);

        if (!dir.exists() || isfored) {
            try {
                dir.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File dest = new File(dir, DATABASE_NAME);
        if (dest.exists() && !isfored) {
            return;
        }

        try {
            if (dest.exists()) {
                dest.delete();
            }

            dest.createNewFile();
            // InputStream in=context.getResources().openRawResource(R.)

            InputStream in = context.getAssets().open("Points");
            int size = in.available();
            byte buf[] = new byte[size];
            in.read(buf);
            in.close();
            FileOutputStream out = new FileOutputStream(dest);
            out.write(buf);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
