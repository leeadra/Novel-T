package com.exam.novelt3_1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.CubicLineChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Custom_HRZoneGraph extends View {
    private static final int MARGIN_LEFT = 50;
    private static final int MARGIN_RIGHT = 10;
    private static final int MARGIN_TOP = 10;
    private static final int MARGIN_BOTTOM = 10;

    private Context context;

    private XYSeries xySeries;                                      // x, y coordination pair

    private XYSeries[] xyLowGuideSeries;                              // min x, y coordination pair
    private XYSeries[] xyHighGuideSeries;                             // max x, y coordination pair

    public GraphicalView graphicalView;                             //
    private XYMultipleSeriesRenderer xyMultipleSeriesRenderer;
    private XYMultipleSeriesDataset xyMultipleSeriesDataset;

    public Custom_HRZoneGraph(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        buildGraph(0xFFFFFF00);
    }

    private Rect rect = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(rect);
        graphicalView.draw(canvas);
    }

    public void buildGraph(int colorID) {
        xySeries = new XYSeries("HR");
        xyLowGuideSeries = new XYSeries[50];
        xyHighGuideSeries = new XYSeries[50];      // create 3 xy series

        xyMultipleSeriesDataset = new XYMultipleSeriesDataset();
        xyMultipleSeriesDataset.addSeries(xySeries);

        for(int i = 0; i < 50; i ++) {
            xyLowGuideSeries[i] = new XYSeries("Low Zone " + i + " Guide");
            xyHighGuideSeries[i] = new XYSeries("High Zone " + i + " Guide");
            xyMultipleSeriesDataset.addSeries(xyLowGuideSeries[i]);
            xyMultipleSeriesDataset.addSeries(xyHighGuideSeries[i]);
        }

        xyMultipleSeriesRenderer = buildRenderer(colorID);          // create graph renderer

        final String[] types = new String[]{CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE};
        graphicalView = ChartFactory.getCombinedXYChartView(context, xyMultipleSeriesDataset, xyMultipleSeriesRenderer, types);                                             // set graph type to combined chart
        graphicalView.setBackgroundColor(Color.TRANSPARENT);

        xyMultipleSeriesRenderer.setClickEnabled(false);

        setTextSize(30);
    }

    protected XYMultipleSeriesRenderer buildRenderer(int color) {
        XYSeriesRenderer xySeriesRenderer = new XYSeriesRenderer();
        xySeriesRenderer.setColor(color);
        xySeriesRenderer.setLineWidth(3);

        XYSeriesRenderer[] xyLowGuideSeriesRenderer = new XYSeriesRenderer[50];
        for(int i = 0; i < 50; i ++) {
            xyLowGuideSeriesRenderer[i] = new XYSeriesRenderer();
            xyLowGuideSeriesRenderer[i].setColor(0xFFFFFFFF);
            xyLowGuideSeriesRenderer[i].setLineWidth(3);
        }

        XYSeriesRenderer[] xyHighGuideSeriesRenderer = new XYSeriesRenderer[50];
        for(int i = 0; i < 50; i ++) {
            xyHighGuideSeriesRenderer[i] = new XYSeriesRenderer();
            xyHighGuideSeriesRenderer[i].setColor(0xFFFFFFFF);
            xyHighGuideSeriesRenderer[i].setLineWidth(3);
        }

        XYMultipleSeriesRenderer xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
        xyMultipleSeriesRenderer.addSeriesRenderer(xySeriesRenderer);
        for(int i = 0; i < 50; i ++) {
            xyMultipleSeriesRenderer.addSeriesRenderer(xyLowGuideSeriesRenderer[i]);
            xyMultipleSeriesRenderer.addSeriesRenderer(xyHighGuideSeriesRenderer[i]);
        }

        xyMultipleSeriesRenderer.setPanEnabled(false, false);       // disable the scroll to x and y axis
        xyMultipleSeriesRenderer.setZoomEnabled(false, false);      // disable the zoom to x and y axis

        xyMultipleSeriesRenderer.setXTitle("Time (min.)");                              // set x axis title
        xyMultipleSeriesRenderer.setYTitle("Heart Rate (bpm)");                         // set y axis title

        xyMultipleSeriesRenderer.setShowAxes(true);
        xyMultipleSeriesRenderer.setShowLabels(true);
        xyMultipleSeriesRenderer.setShowLegend(false);

        xyMultipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);

        //tXYMultipleSeriesRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        xyMultipleSeriesRenderer.setApplyBackgroundColor(true);
        xyMultipleSeriesRenderer.setMargins(new int[] { MARGIN_TOP, MARGIN_LEFT, MARGIN_BOTTOM, MARGIN_RIGHT });
        xyMultipleSeriesRenderer.setBackgroundColor(Color.BLACK);

        return xyMultipleSeriesRenderer;
    }

    public void setTextSize(int size) {
        xyMultipleSeriesRenderer.setAxisTitleTextSize(size);
        xyMultipleSeriesRenderer.setLabelsTextSize(size);

        xyMultipleSeriesRenderer.setMargins(new int[] { 10, size * 3, 10, 10 });

        redrawGraph();
    }

    private final List<Long> times = new LinkedList<>();
    private final List<Integer> heartRates = new LinkedList<>();
    private long firstTimeMilli;
    private long lastTimeMilli;

    private boolean isLive;
    private final Runnable updateGraphRunnable = new Runnable() {
        @Override
        public void run() {
            final double xUnit =  1000. * 60.;    // 1 min
            final long timeRange = 60 * 60 * 1000;  // 1 hour

            isLive = true;
            while (isLive) {
                double minY = Double.MAX_VALUE;
                double maxY = Double.MIN_VALUE;

                try {
                    synchronized (times) {
                        try {
                            times.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        long time = times.get(times.size() - 1);
                        long startTime = time - timeRange;

                        while (times.size() > 0) {
                            long t = times.get(0);
                            if (t < startTime) {
                                times.remove(0);
                                heartRates.remove(0);
                            } else {
                                break;
                            }
                        }

                        xySeries.clear();

                        long firstTime = times.get(0);
                        firstTimeMilli = firstTime;
                        lastTimeMilli = time;
                        for (int i = 0; i < times.size(); i++) {
                            double x = (times.get(i) - firstTime) / xUnit;
                            double y = heartRates.get(i);

                            xySeries.add(x, y);

                            if (minY > y && y != 0) {
                                minY = y;
                            }
                            if (maxY < y) {
                                maxY = y;
                            }
                        }
                    }

                    double firstX = xySeries.getX(0);
                    double lastX = xySeries.getX(xySeries.getItemCount() - 1);

                    double yHeight = (maxY - minY);
                    if (yHeight < 40) {
                        yHeight = 40;
                    }
                    double yMin = minY - yHeight * 0.2;
                    if (yMin < 0) {
                        yMin = 0;
                    }

                    if(SelfControlActivity.isStarted) {
                        xyMultipleSeriesRenderer.setYAxisMin(60);
                        xyMultipleSeriesRenderer.setYAxisMax(maxY + yHeight * 0.2);
                        xyMultipleSeriesRenderer.setYLabels(5);
                        xyMultipleSeriesRenderer.setXAxisMin(0);
                        xyMultipleSeriesRenderer.setXAxisMax(lastX);
                        xyMultipleSeriesRenderer.setXLabels(15);
                    }

                    redrawGraph();
                } catch (Exception e) {

                }
            }
        }
    };


    private void redrawGraph() {
        invalidate();
    }

    private Thread updateGraphThread;

    int the_number_of_zone;
    public void onDrawZone(String[][] params, float zone_factor) {
        the_number_of_zone = Integer.parseInt(params[0][1]);
        for(int i = 2; i <= 1 + the_number_of_zone; i++) {
            xyLowGuideSeries[i-2].add(3 + (Integer.parseInt(params[i][0]) * zone_factor), Integer.parseInt(params[i][3]));
            xyLowGuideSeries[i-2].add(3 + (Integer.parseInt(params[i][1]) * zone_factor), Integer.parseInt(params[i][3]));
            xyHighGuideSeries[i-2].add(3 + (Integer.parseInt(params[i][0]) * zone_factor), Integer.parseInt(params[i][4]));
            xyHighGuideSeries[i-2].add(3 + (Integer.parseInt(params[i][1]) * zone_factor), Integer.parseInt(params[i][4]));
        }
        xyMultipleSeriesRenderer.setXAxisMin(0);
        xyMultipleSeriesRenderer.setXAxisMax(36);
        xyMultipleSeriesRenderer.setXLabels(19);

        xyMultipleSeriesRenderer.setYAxisMin(60);
        xyMultipleSeriesRenderer.setYAxisMax(200);
        xyMultipleSeriesRenderer.setYLabels(10);
        redrawGraph();
    }

    public void add(int heartRate) {
        if (updateGraphThread == null) {
            updateGraphThread = new Thread(updateGraphRunnable);
            updateGraphThread.start();
        }

        long time = System.currentTimeMillis();
        synchronized (times) {
            times.add(time);
            heartRates.add(heartRate);

            times.notifyAll();
        }
    }

    public void addAll(ArrayList<Integer> HRs, long[] time) {
        synchronized(times) {
            if(HRs != null && time != null) {
                heartRates.addAll(HRs);
                for(int i = 0; i < time.length; i++) {
                    times.add(time[i]);
                }
                onShowResult();
                times.notifyAll();
            }
        }
    }

    public void onShowResult() {
        final double xUnit =  1000. * 60.;    // 1 min
        final long timeRange = 60 * 60 * 1000;  // 1 hour

        int size = times.size();
        int k = 0;
        while (size != 0) {
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;

            try {
                synchronized (times) {
                    long time = times.get(times.size() - 1);
                    long startTime = time - timeRange;

                    while (times.size() > 0) {
                        long t = times.get(0);
                        if (t < startTime) {
                            times.remove(0);
                            heartRates.remove(0);
                        } else {
                            break;
                        }
                    }

                    xySeries.clear();

                    long firstTime = times.get(0);
                    firstTimeMilli = firstTime;
                    lastTimeMilli = time;
                    for (int i = 0; i < times.size(); i++) {
                        double x = (times.get(i) - firstTime) / xUnit;
                        double y = heartRates.get(i);

                        xySeries.add(x, y);

                        if (minY > y && y != 0) {
                            minY = y;
                        }
                        if (maxY < y) {
                            maxY = y;
                        }
                    }
                }

                double firstX = xySeries.getX(0);
                double lastX = xySeries.getX(xySeries.getItemCount() - 1);

                double yHeight = (maxY - minY);
                if (yHeight < 40) {
                    yHeight = 40;
                }
                double yMin = minY - yHeight * 0.2;
                if (yMin < 0) {
                    yMin = 0;
                }

                if(CustomProtocolActivity.isStarted) {
                    onDrawZone(CustomProtocolActivity.params, CustomProtocolActivity.zone_factor);

                    xyMultipleSeriesRenderer.setXAxisMin(0);
                    xyMultipleSeriesRenderer.setXAxisMax(36);
                    xyMultipleSeriesRenderer.setXLabels(19);

                    xyMultipleSeriesRenderer.setYAxisMin(60);
                    xyMultipleSeriesRenderer.setYAxisMax(200);
                    xyMultipleSeriesRenderer.setYLabels(10);
                }

                if(SelfControlActivity.isStarted) {
                    xyMultipleSeriesRenderer.setYAxisMin(60);
                    xyMultipleSeriesRenderer.setYAxisMax(maxY + yHeight * 0.2);
                    xyMultipleSeriesRenderer.setYLabels(5);
                    xyMultipleSeriesRenderer.setXAxisMin(firstX);
                    xyMultipleSeriesRenderer.setXAxisMax(lastX);
                    xyMultipleSeriesRenderer.setXLabels(15);
                }

                size -= 1;
                k += 1;
            } catch (Exception e) {

            }
        }
    }
}
