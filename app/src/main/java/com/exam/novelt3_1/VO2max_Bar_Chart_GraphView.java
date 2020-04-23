package com.exam.novelt3_1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class VO2max_Bar_Chart_GraphView extends View {
    public static boolean BAR = true;
    public static boolean LINE = false;

    Paint paint;
    float[] values;
    String[] yLabels, xLabels;
    String title;
    boolean type;

    public VO2max_Bar_Chart_GraphView(Context context, float[] values, String title, String[] yLabels, String[] xLabels, boolean type) {
        super(context);

        if(values == null) values = new float[0];
        else this.values = values;

        if(title == null) title = "";
        else this.title = title;

        if(yLabels == null) this.yLabels = new String[0];
        else this.yLabels = yLabels;

        if(xLabels == null) this.xLabels = new String[0];
        else this.xLabels = xLabels;

        this.type = type;
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float border = 20;
        float horstart = border * 2;
        float height = getHeight();
        float width = getWidth() - 1;
        float max = getMax();
        float min = getMin();
        float diff = max - min;
        float graphheight = height - (2 * border);
        float graphwidth = width - (2 * border);

        paint.setTextAlign(Paint.Align.CENTER);
        int vers = xLabels.length - 1;
        for (int i = 0; i < xLabels.length; i++) {
            paint.setColor(Color.TRANSPARENT);
            float y = ((graphheight / vers) * i) + border;
            canvas.drawLine(horstart, y, width, y, paint);
            paint.setColor(Color.WHITE);
            canvas.drawText(xLabels[i], 0, y, paint);
        }
        int hors = yLabels.length - 1;
        for (int i = 0; i < yLabels.length; i++) {
            paint.setColor(Color.TRANSPARENT);
            float x = ((graphwidth / hors) * i) + horstart;
            canvas.drawLine(x, height - border, x, border, paint);
            paint.setTextAlign(Paint.Align.CENTER);
            if (i==yLabels.length-1)
                paint.setTextAlign(Paint.Align.RIGHT);
            if (i==0)
                paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(Color.WHITE);
            canvas.drawText(yLabels[i], x, height - 4, paint);
        }

        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(title, (graphwidth / 2) + horstart, border - 4, paint);

        if (max != min) {
            paint.setColor(Color.rgb(91, 155, 213));
            if (type == BAR) {
                float datalength = values.length;
                float colwidth = (width - (2 * border)) / datalength;
                for (int i = 0; i < values.length; i++) {
                    float val = values[i] - min;
                    float rat = val / diff;
                    float h = graphheight * rat;
                    canvas.drawRect((i * colwidth) + horstart, (border - h) + graphheight, ((i * colwidth) + horstart) + (colwidth - 1), height - (border - 1), paint);
                }
            } else {
                float datalength = values.length;
                float colwidth = (width - (2 * border)) / datalength;
                float halfcol = colwidth / 2;
                float lasth = 0;
                for (int i = 0; i < values.length; i++) {
                    float val = values[i] - min;
                    float rat = val / diff;
                    float h = graphheight * rat;
                    if (i > 0)
                        canvas.drawLine(((i - 1) * colwidth) + (horstart + 1) + halfcol, (border - lasth) + graphheight, (i * colwidth) + (horstart + 1) + halfcol, (border - h) + graphheight, paint);
                    lasth = h;
                }
            }
        }
    }

    private float getMax() {
        float largest = Integer.MIN_VALUE;
        for (int i = 0; i < values.length; i++)
            if (values[i] > largest)
                largest = values[i];
        return largest;
    }

    private float getMin() {
        float smallest = Integer.MAX_VALUE;
        for (int i = 0; i < values.length; i++)
            if (values[i] < smallest)
                smallest = values[i];
        return smallest;
    }
}
