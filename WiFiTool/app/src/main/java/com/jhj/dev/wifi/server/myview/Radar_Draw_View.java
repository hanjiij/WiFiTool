package com.jhj.dev.wifi.server.myview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author 韩吉
 */
public class Radar_Draw_View extends ImageView {

    float[] points = {0, 0};
    String[] nameStrings = {""};
    Canvas canvas = null;

    public Radar_Draw_View(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public Radar_Draw_View(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public Radar_Draw_View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void draw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.draw(canvas);

        this.canvas = canvas;
        Paint paint = new Paint();
        paint.setColor(0xff22b10e);
        paint.setAntiAlias(true);

        Paint paintname = new Paint();
        paintname.setColor(0xff000000);
        paintname.setTextSize(10);
        paintname.setAntiAlias(true);

        for (int i = 0; i < points.length; i += 2) {
            canvas.drawCircle(points[i], points[i + 1], 5, paint);
            canvas.drawText(nameStrings[i / 2].equals("") ? "隐藏" : nameStrings[i / 2],
                            points[i] - 5, points[i + 1] - 6, paintname);
        }
    }

    public void flush(float[] points, String[] nameStrings) {
        this.points = points;
        this.nameStrings = nameStrings;
        invalidate();
    }
}
