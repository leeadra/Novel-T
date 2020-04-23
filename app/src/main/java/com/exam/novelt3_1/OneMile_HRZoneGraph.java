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

public class OneMile_HRZoneGraph extends View {
    private static final int MARGIN_LEFT = 50;
    private static final int MARGIN_RIGHT = 10;
    private static final int MARGIN_TOP = 10;
    private static final int MARGIN_BOTTOM = 10;

    private Context context;

    private XYSeries xySeries;                                      // x, y coordination pair

    private XYSeries xyLowGuideSeries1, xyLowGuideSeries2, xyLowGuideSeries3, xyLowGuideSeries4, xyLowGuideSeries5;                              // min x, y coordination pair
    private XYSeries xyHighGuideSeries1, xyHighGuideSeries2, xyHighGuideSeries3, xyHighGuideSeries4, xyHighGuideSeries5;                             // max x, y coordination pair

    public GraphicalView graphicalView;                             //
    private XYMultipleSeriesRenderer xyMultipleSeriesRenderer;
    private XYMultipleSeriesDataset xyMultipleSeriesDataset;

    public interface ISelectedTargetTimeListener {
        void onSelectedTargetChanged(long selectedTargetTimeMilli);
    }
    private ISelectedTargetTimeListener listener;
    public void setOnSelectedTargetTimeListener(ISelectedTargetTimeListener observer) {
        this.listener = observer;
    }

    public OneMile_HRZoneGraph(Context context, AttributeSet attrs) {
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
        xyLowGuideSeries1 = new XYSeries("Low Zone1 Guide");
        xyHighGuideSeries1 = new XYSeries("High Zone1 Guide");      // create 3 xy series
        xyLowGuideSeries2 = new XYSeries("Low Zone2 Guide");
        xyHighGuideSeries2 = new XYSeries("High Zone2 Guide");
        xyLowGuideSeries3 = new XYSeries("Low Zone3 Guide");
        xyHighGuideSeries3 = new XYSeries("High Zone3 Guide");
        xyLowGuideSeries4 = new XYSeries("Low Zone4 Guide");
        xyHighGuideSeries4 = new XYSeries("High Zone4 Guide");
        xyLowGuideSeries5 = new XYSeries("Low Zone5 Guide");
        xyHighGuideSeries5 = new XYSeries("High Zone5 Guide");

        xyMultipleSeriesDataset = new XYMultipleSeriesDataset();
        xyMultipleSeriesDataset.addSeries(xySeries);
        xyMultipleSeriesDataset.addSeries(xyLowGuideSeries1);
        xyMultipleSeriesDataset.addSeries(xyHighGuideSeries1);       // create data set for graph, then add the 3 xy series
        xyMultipleSeriesDataset.addSeries(xyLowGuideSeries2);
        xyMultipleSeriesDataset.addSeries(xyHighGuideSeries2);
        xyMultipleSeriesDataset.addSeries(xyLowGuideSeries3);
        xyMultipleSeriesDataset.addSeries(xyHighGuideSeries3);
        xyMultipleSeriesDataset.addSeries(xyLowGuideSeries4);
        xyMultipleSeriesDataset.addSeries(xyHighGuideSeries4);
        xyMultipleSeriesDataset.addSeries(xyLowGuideSeries5);
        xyMultipleSeriesDataset.addSeries(xyHighGuideSeries5);

        xyMultipleSeriesRenderer = buildRenderer(colorID);          // create graph renderer

        final String[] types = new String[]{CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE,
                CubicLineChart.TYPE, CubicLineChart.TYPE};
        graphicalView = ChartFactory.getCombinedXYChartView(
                context,
                xyMultipleSeriesDataset,
                xyMultipleSeriesRenderer,
                types);                                             // set graph type to combined chart
        graphicalView.setBackgroundColor(Color.TRANSPARENT);

        xyMultipleSeriesRenderer.setClickEnabled(true);
        xyMultipleSeriesRenderer.setSelectableBuffer(1);

        setTextSize(20);
    }

    protected XYMultipleSeriesRenderer buildRenderer(int color) {
        XYSeriesRenderer xySeriesRenderer = new XYSeriesRenderer();
        xySeriesRenderer.setColor(color);
        xySeriesRenderer.setLineWidth(3);

        XYSeriesRenderer xyLowGuideSeriesRenderer1 = new XYSeriesRenderer();
        xyLowGuideSeriesRenderer1.setColor(0xFFFFFFFF);
        xyLowGuideSeriesRenderer1.setLineWidth(3);

        XYSeriesRenderer xyHighGuideSeriesRenderer1 = new XYSeriesRenderer();
        xyHighGuideSeriesRenderer1.setColor(0xFFFFFFFF);
        xyHighGuideSeriesRenderer1.setLineWidth(3);

        XYSeriesRenderer xyLowGuideSeriesRenderer2 = new XYSeriesRenderer();
        xyLowGuideSeriesRenderer2.setColor(0xFFFFFFFF);
        xyLowGuideSeriesRenderer2.setLineWidth(3);

        XYSeriesRenderer xyHighGuideSeriesRenderer2 = new XYSeriesRenderer();
        xyHighGuideSeriesRenderer2.setColor(0xFFFFFFFF);
        xyHighGuideSeriesRenderer2.setLineWidth(3);

        XYSeriesRenderer xyLowGuideSeriesRenderer3 = new XYSeriesRenderer();
        xyLowGuideSeriesRenderer3.setColor(0xFFFFFFFF);
        xyLowGuideSeriesRenderer3.setLineWidth(3);

        XYSeriesRenderer xyHighGuideSeriesRenderer3 = new XYSeriesRenderer();
        xyHighGuideSeriesRenderer3.setColor(0xFFFFFFFF);
        xyHighGuideSeriesRenderer3.setLineWidth(3);

        XYSeriesRenderer xyLowGuideSeriesRenderer4 = new XYSeriesRenderer();
        xyLowGuideSeriesRenderer4.setColor(0xFFFFFFFF);
        xyLowGuideSeriesRenderer4.setLineWidth(3);

        XYSeriesRenderer xyHighGuideSeriesRenderer4 = new XYSeriesRenderer();
        xyHighGuideSeriesRenderer4.setColor(0xFFFFFFFF);
        xyHighGuideSeriesRenderer4.setLineWidth(3);

        XYSeriesRenderer xyLowGuideSeriesRenderer5 = new XYSeriesRenderer();
        xyLowGuideSeriesRenderer5.setColor(0xFFFFFFFF);
        xyLowGuideSeriesRenderer5.setLineWidth(3);

        XYSeriesRenderer xyHighGuideSeriesRenderer5 = new XYSeriesRenderer();
        xyHighGuideSeriesRenderer5.setColor(0xFFFFFFFF);
        xyHighGuideSeriesRenderer5.setLineWidth(3);

        XYMultipleSeriesRenderer xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
        xyMultipleSeriesRenderer.addSeriesRenderer(xySeriesRenderer);
        xyMultipleSeriesRenderer.addSeriesRenderer(xyLowGuideSeriesRenderer1);
        xyMultipleSeriesRenderer.addSeriesRenderer(xyHighGuideSeriesRenderer1);
        xyMultipleSeriesRenderer.addSeriesRenderer(xyLowGuideSeriesRenderer2);
        xyMultipleSeriesRenderer.addSeriesRenderer(xyHighGuideSeriesRenderer2);
        xyMultipleSeriesRenderer.addSeriesRenderer(xyLowGuideSeriesRenderer3);
        xyMultipleSeriesRenderer.addSeriesRenderer(xyHighGuideSeriesRenderer3);
        xyMultipleSeriesRenderer.addSeriesRenderer(xyLowGuideSeriesRenderer4);
        xyMultipleSeriesRenderer.addSeriesRenderer(xyHighGuideSeriesRenderer4);
        xyMultipleSeriesRenderer.addSeriesRenderer(xyLowGuideSeriesRenderer5);
        xyMultipleSeriesRenderer.addSeriesRenderer(xyHighGuideSeriesRenderer5);

        //xyMultipleSeriesRenderer.setShowGrid(true);

        xyMultipleSeriesRenderer.setPanEnabled(false, false);       // disable the scroll to x and y axis
        xyMultipleSeriesRenderer.setZoomEnabled(false, false);      // disable the zoom to x and y axis

        xyMultipleSeriesRenderer.setXTitle("Distance (km)");                              // set x axis title
        xyMultipleSeriesRenderer.setYTitle("Heart Rate (bpm)");                         // set y axis title

        xyMultipleSeriesRenderer.setShowAxes(true);
        xyMultipleSeriesRenderer.setShowLabels(true);
        xyMultipleSeriesRenderer.setShowLegend(false);

        xyMultipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        xyMultipleSeriesRenderer.setXLabelsColor(Color.WHITE);
        xyMultipleSeriesRenderer.setYLabelsColor(0, Color.WHITE);
        xyMultipleSeriesRenderer.setGridColor(Color.WHITE);

        xyMultipleSeriesRenderer.setApplyBackgroundColor(true);
        xyMultipleSeriesRenderer.setMargins(new int[] { MARGIN_TOP, MARGIN_LEFT, MARGIN_BOTTOM, MARGIN_RIGHT });
        xyMultipleSeriesRenderer.setBackgroundColor(Color.TRANSPARENT);
        xyMultipleSeriesRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));

        return xyMultipleSeriesRenderer;
    }

    public void setTextSize(int size) {
        xyMultipleSeriesRenderer.setAxisTitleTextSize(size);
        xyMultipleSeriesRenderer.setLabelsTextSize(size);

        xyMultipleSeriesRenderer.setMargins(new int[] { 10, (int)(size * 2.8), 1, 10 });

        redrawGraph();
    }

    private List<Float> distances = new LinkedList<>();
    private final List<Integer> heartRates = new LinkedList<>();
    private float firstTimeMilli;
    private float lastTimeMilli;

    private boolean isLive;
    private final Runnable updateGraphRunnable = new Runnable() {
        @Override
        public void run() {
            //final double xUnit =  1000. * 60.;    // 1 min
            //final long timeRange = 60 * 60 * 1000;  // 1 hour

            isLive = true;
            while (isLive) {
                double minY = Double.MAX_VALUE;
                double maxY = Double.MIN_VALUE;

                try {
                    synchronized (distances) {
                        try {
                            distances.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        float time = distances.get(distances.size() - 1);
                        //float startTime = time - timeRange;

                        /*while (distances.size() > 0) {
                            float t = distances.get(0);
                            if (t < startTime) {
                                distances.remove(0);
                                heartRates.remove(0);
                            } else {
                                break;
                            }
                        }*/

                        xySeries.clear();

                        float firstTime = distances.get(0);
                        firstTimeMilli = firstTime;
                        lastTimeMilli = time;
                        for (int i = 0; i < distances.size(); i++) {
                            // double x = (distances.get(i) - firstTime) / xUnit;
                            double x = (distances.get(i) - firstTime);
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

                    xyLowGuideSeries1.clear();
                    xyHighGuideSeries1.clear();
                    xyLowGuideSeries2.clear();
                    xyHighGuideSeries2.clear();
                    xyLowGuideSeries3.clear();
                    xyHighGuideSeries3.clear();
                    xyLowGuideSeries4.clear();
                    xyHighGuideSeries4.clear();
                    xyLowGuideSeries5.clear();
                    xyHighGuideSeries5.clear();

                    if(OneMileTestActivity.isStarted) {
                        xyLowGuideSeries1.add(0, OneMileTestActivity.std_fifty);
                        xyLowGuideSeries1.add(0.32, OneMileTestActivity.std_fifty);
                        xyHighGuideSeries1.add(0, OneMileTestActivity.std_sixty);
                        xyHighGuideSeries1.add(0.32, OneMileTestActivity.std_sixty);
                        xyLowGuideSeries2.add(0.32, OneMileTestActivity.std_sixty);
                        xyLowGuideSeries2.add(0.64, OneMileTestActivity.std_sixty);
                        xyHighGuideSeries2.add(0.32, OneMileTestActivity.std_seventy);
                        xyHighGuideSeries2.add(0.64, OneMileTestActivity.std_seventy);
                        xyLowGuideSeries3.add(0.64, OneMileTestActivity.std_seventy);
                        xyLowGuideSeries3.add(0.96, OneMileTestActivity.std_seventy);
                        xyHighGuideSeries3.add(0.64, OneMileTestActivity.std_eighty);
                        xyHighGuideSeries3.add(0.96, OneMileTestActivity.std_eighty);
                        xyLowGuideSeries4.add(0.96, OneMileTestActivity.std_eighty);
                        xyLowGuideSeries4.add(1.28, OneMileTestActivity.std_eighty);
                        xyHighGuideSeries4.add(0.96, OneMileTestActivity.std_ninety);
                        xyHighGuideSeries4.add(1.28, OneMileTestActivity.std_ninety);
                        xyLowGuideSeries5.add(1.28, OneMileTestActivity.std_ninety);
                        xyLowGuideSeries5.add(1.60, OneMileTestActivity.std_ninety);
                        xyHighGuideSeries5.add(1.28, OneMileTestActivity.std_hundred);
                        xyHighGuideSeries5.add(1.60, OneMileTestActivity.std_hundred);
                    }

                    xyMultipleSeriesRenderer.setXAxisMin(firstX);
                    xyMultipleSeriesRenderer.setXAxisMax(lastX);
                    xyMultipleSeriesRenderer.setXLabels(16);

                    double yHeight = (maxY - minY);
                    if (yHeight < 40) {
                        yHeight = 40;
                    }
                    double yMin = minY - yHeight * 0.2;
                    if (yMin < 0) {
                        yMin = 0;
                    }

                    xyMultipleSeriesRenderer.setYAxisMin(90);
                    xyMultipleSeriesRenderer.setYAxisMax(OneMileTestActivity.std_hundred + 10);
                    xyMultipleSeriesRenderer.setXAxisMin(0);
                    xyMultipleSeriesRenderer.setXAxisMax(1.6);
                    xyMultipleSeriesRenderer.setYLabels(10);

                    //Log.d(TAG, "Y: " + minY + " ~ " + maxY);

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

    public void add(int heartRate, float dis) {
        if (updateGraphThread == null) {
            updateGraphThread = new Thread(updateGraphRunnable);
            updateGraphThread.start();
        }

        //long time = System.currentTimeMillis();
        synchronized (distances) {
            distances.add(dis);
            heartRates.add(heartRate);

            distances.notifyAll();
        }
    }

    public void onShowResult() {
        int size = distances.size();
        int k = 0;

        while (size != 0) {
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;

            try {
                synchronized (distances) {
                    float distance = distances.get(k);

                    xySeries.clear();

                    float firstTime = distances.get(0);
                    firstTimeMilli = firstTime;
                    lastTimeMilli = distance;
                    for (int i = 0; i < distances.size(); i++) {
                        // double x = (distances.get(i) - firstTime) / xUnit;
                        double x = (distances.get(i) - firstTime);
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

                xyLowGuideSeries1.clear();
                xyHighGuideSeries1.clear();
                xyLowGuideSeries2.clear();
                xyHighGuideSeries2.clear();
                xyLowGuideSeries3.clear();
                xyHighGuideSeries3.clear();
                xyLowGuideSeries4.clear();
                xyHighGuideSeries4.clear();
                xyLowGuideSeries5.clear();
                xyHighGuideSeries5.clear();

                xyLowGuideSeries1.add(0, OneMileTestActivity.std_fifty);
                xyLowGuideSeries1.add(0.32, OneMileTestActivity.std_fifty);
                xyHighGuideSeries1.add(0, OneMileTestActivity.std_sixty);
                xyHighGuideSeries1.add(0.32, OneMileTestActivity.std_sixty);
                xyLowGuideSeries2.add(0.32, OneMileTestActivity.std_sixty);
                xyLowGuideSeries2.add(0.64, OneMileTestActivity.std_sixty);
                xyHighGuideSeries2.add(0.32, OneMileTestActivity.std_seventy);
                xyHighGuideSeries2.add(0.64, OneMileTestActivity.std_seventy);
                xyLowGuideSeries3.add(0.64, OneMileTestActivity.std_seventy);
                xyLowGuideSeries3.add(0.96, OneMileTestActivity.std_seventy);
                xyHighGuideSeries3.add(0.64, OneMileTestActivity.std_eighty);
                xyHighGuideSeries3.add(0.96, OneMileTestActivity.std_eighty);
                xyLowGuideSeries4.add(0.96, OneMileTestActivity.std_eighty);
                xyLowGuideSeries4.add(1.28, OneMileTestActivity.std_eighty);
                xyHighGuideSeries4.add(0.96, OneMileTestActivity.std_ninety);
                xyHighGuideSeries4.add(1.28, OneMileTestActivity.std_ninety);
                xyLowGuideSeries5.add(1.28, OneMileTestActivity.std_ninety);
                xyLowGuideSeries5.add(1.60, OneMileTestActivity.std_ninety);
                xyHighGuideSeries5.add(1.28, OneMileTestActivity.std_hundred);
                xyHighGuideSeries5.add(1.60, OneMileTestActivity.std_hundred);

                xyMultipleSeriesRenderer.setXAxisMin(firstX);
                xyMultipleSeriesRenderer.setXAxisMax(lastX);
                xyMultipleSeriesRenderer.setXLabels(6);

                double yHeight = (maxY - minY);
                if (yHeight < 40) {
                    yHeight = 40;
                }
                double yMin = minY - yHeight * 0.2;
                if (yMin < 0) {
                    yMin = 0;
                }

                xyMultipleSeriesRenderer.setYAxisMin(60);
                xyMultipleSeriesRenderer.setYAxisMax(OneMileTestActivity.std_hundred + 10);
                xyMultipleSeriesRenderer.setXAxisMin(0);
                xyMultipleSeriesRenderer.setXAxisMax(1.6);

                //Log.d(TAG, "Y: " + minY + " ~ " + maxY);

                //redrawGraph();
                size -= 1;
                k++;
            } catch (Exception e) {

            }
        }
    }

    public void addAll(ArrayList<Integer> HRs, float[] dis) {
        synchronized(distances) {
            heartRates.addAll(HRs);
            for(int i = 0; i < dis.length; i++) {
                distances.add(dis[i]);
            }
            onShowResult();
            distances.notifyAll();
        }
    }
}
