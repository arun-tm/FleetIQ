package com.teramatrix.fleetiq.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;

import java.sql.SQLOutput;
import java.util.ArrayList;

/**
 * Created by arun.singh on 7/22/2016.
 */
public class MyBarChart extends View {


    private Context context;

    private int viewbackgroundColor;
    private int measuredWidth;
    private int measuredHeight;
    Paint paint = new Paint();
    Paint bgLine = new Paint();
    Paint textPaint = new Paint();
    Paint textPaint_hor = new Paint();
    Paint circlePaint = new Paint();
    Paint circlePaint_inside = new Paint();
    Paint barlinepaint = new Paint();
    Paint ringValueBgPaint = new Paint();
    Paint wallpaint = new Paint();
    Path wallpath = new Path();

    private int no_col = 10;
    private int no_row = 5;

    private int x[] = new int[10];
    private int y[] = new int[5];

    private int verticalDimention[];

    private String horizontalDimention[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private String valuesOnRing[];
    private ArrayList<Integer[]> intersectionPoints = new ArrayList<>();

    private boolean shouldPopup = false;
    private boolean isWallPainted = false;
    private boolean isVerticleBackgroundGraphLineShown = false;
    Bitmap tooltip;
    private int nodeRingRadius;


    ArrayList<Region> regionArrayList = new ArrayList<Region>();
    private int toolTipBox_x,toolTipBox_y;
    private int selectedRegionIndex = 5;
    private Paint ringValueTextPaint;
    private float ringValueText_width;
    private float ringValueText_size;
    public MyBarChart(Context context) {
        super(context);
        initVariables(context);
    }

    public MyBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVariables(context);
    }

    public MyBarChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVariables(context);
    }

    private void initVariables(Context context) {
        this.context = context;

        viewbackgroundColor = Color.parseColor("#112731");


        paint.setColor(Color.parseColor("#00BCD4"));
        paint.setStrokeWidth(4);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);


        bgLine.setColor(Color.parseColor("#00BCD4"));
        bgLine.setStrokeWidth(4);
        bgLine.setFlags(Paint.ANTI_ALIAS_FLAG);

        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(22);
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        textPaint_hor.setColor(Color.GRAY);
        textPaint_hor.setTextSize(26);
        textPaint_hor.setFlags(Paint.ANTI_ALIAS_FLAG);


        circlePaint_inside.setColor(Color.parseColor("#112731"));
        circlePaint_inside.setStyle(Paint.Style.FILL);
        circlePaint_inside.setStrokeWidth(3);
        circlePaint_inside.setFlags(Paint.ANTI_ALIAS_FLAG);

        circlePaint.setColor(Color.parseColor("#00BCD4"));
        circlePaint.setStrokeWidth(3);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);


        nodeRingRadius = 7;


        barlinepaint.setColor(Color.BLACK);
        barlinepaint.setStrokeWidth(1);
        barlinepaint.setFlags(Paint.ANTI_ALIAS_FLAG);


        wallpaint.setColor(Color.parseColor("#00BCD4"));
        wallpaint.setAlpha(55);
        wallpaint.setStyle(Paint.Style.FILL);
        wallpaint.setFlags(Paint.ANTI_ALIAS_FLAG);


        ringValueBgPaint.setColor(Color.parseColor("#112731"));
        ringValueTextPaint= new Paint();
        ringValueTextPaint.setColor(Color.WHITE);
        ringValueTextPaint.setTextSize(30);
        ringValueText_width = ringValueTextPaint.measureText("200");
        ringValueText_size =ringValueTextPaint.getTextSize();
        try {
            tooltip = BitmapFactory.decodeResource(getResources(), R.drawable.ic_tooltip);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measuredWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        measuredHeight = View.MeasureSpec.getSize(heightMeasureSpec);

        int x_offset = measuredWidth / x.length;
        for (int i = 0; i < x.length; i++) {
            if (i == 0)
                x[i] = x_offset - x_offset * 1 / 4;
            else
                x[i] = x[i - 1] + x_offset;

            System.out.println("X :" + x[i]);
        }

        int y_offset = measuredHeight / y.length;
        for (int i = 0; i < y.length; i++) {
            if (i == 0)
                y[i] = 0;
            else
                y[i] = y[i - 1] + y_offset;

            System.out.println("Y :" + y[i]);
        }

        calculateCircleDrawPoints();
    }

    private void calculateCircleDrawPoints() {

        //Reset Canvas
        intersectionPoints.clear();
        wallpath.reset();

        //calculate circles on month and price intersection points
        for (int i = 0; i < x.length; i++) {
            int price_row = GeneralUtilities.generateRandomNumber(1, y.length - 1);
            Integer circleCordinate[] = new Integer[2];
            circleCordinate[0] = x[i];
            circleCordinate[1] = y[price_row];
            intersectionPoints.add(circleCordinate);
        }


        //calculate wallpath area
        wallpath.moveTo(x[0], y[y.length - 1]);// used for first point
        for (int i = 0; i < intersectionPoints.size(); i++) {
            Integer circleCordinate[] = intersectionPoints.get(i);
            wallpath.lineTo(circleCordinate[0], circleCordinate[1]);
        }

        for (int i = x.length - 1; i > 0; i--) {
            wallpath.lineTo(x[i], y[y.length - 1]);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(viewbackgroundColor);

        //Draw Vertical lines

        if (isVerticleBackgroundGraphLineShown) {
            for (int i = 0; i < x.length; i++) {
                canvas.drawLine(x[i], 0, x[i], y[y.length - 1], bgLine);
            }
        }
        //Draw Horizontal lines
        /*for (int i = 1; i < y.length; i++) {
            canvas.drawLine(x[1]-10, y[i], x[x.length - 1]+10, y[i], barlinepaint);
        }*/

        //draw price values
        int price_offset = 50;
        for (int i = 1; i < y.length; i++) {
            canvas.drawText((y.length - i - 1) * 50 + "", 2, y[i], textPaint);
        }

        //draw month value
        for (int i = 0; i < x.length; i++) {
            canvas.drawText(horizontalDimention[i], x[i] - 20, y[y.length - 1] + 40, textPaint_hor);
        }

        //draw lines between intersection points
        for (int i = 0; i < intersectionPoints.size() - 1; i++) {
            canvas.drawLine(intersectionPoints.get(i)[0], intersectionPoints.get(i)[1], intersectionPoints.get(i + 1)[0], intersectionPoints.get(i + 1)[1], paint);
        }

        //draw wallpath
        if (isWallPainted)
            canvas.drawPath(wallpath, wallpaint);

        //draw circles on month and price intersection points
        regionArrayList.clear();
        for (int i = 0; i < intersectionPoints.size(); i++) {

            //Add Circle touch reigon
            Region reigon = new Region();
            reigon.set(intersectionPoints.get(i)[0] - 5*nodeRingRadius, intersectionPoints.get(i)[1] - 5*nodeRingRadius, intersectionPoints.get(i)[0] + 5*nodeRingRadius, intersectionPoints.get(i)[1] + 5*nodeRingRadius);
            regionArrayList.add(reigon);

            /*Rect rect = reigon.getBounds();
            canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);*/

            //Draw Circles
            canvas.drawCircle(intersectionPoints.get(i)[0], intersectionPoints.get(i)[1], nodeRingRadius, circlePaint);
            canvas.drawCircle(intersectionPoints.get(i)[0], intersectionPoints.get(i)[1], nodeRingRadius - 2, circlePaint_inside);


        }

        if (shouldPopup) {
            Rect rect = regionArrayList.get(selectedRegionIndex).getBounds();

            float text_x =  (rect.right- rect.left)/2 - ringValueText_width/2;
            float text_y = (rect.bottom - rect.top) - ringValueText_size;

            //Draw bg Squar of value
            canvas.drawRect(rect.left-10, rect.top, rect.right+10, rect.bottom, ringValueTextPaint);
            canvas.drawRect(rect.left-9, rect.top+1, rect.right+9, rect.bottom-1, ringValueBgPaint);
            //Draw value
            canvas.drawText("200",rect.left+ text_x, rect.top+ text_y, ringValueTextPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float xPos = event.getX();
                float yPos = event.getY();


                shouldPopup = false;
                for (int i =0;i<regionArrayList.size();i++) {
                    Region r = regionArrayList.get(i);
                    if (r.contains((int) xPos, (int) yPos)) {
                        shouldPopup = true;
                         Rect rect =r.getBounds();
                        toolTipBox_x = rect.left;
                        toolTipBox_y =  rect.top;
                        selectedRegionIndex = i;
                        /*toolTipBox_x = r.getBounds().centerX();
                        toolTipBox_y = r.getBounds().centerY();*/
                    }
                }
                invalidate();
                break;
        }

        return true;

    }

    public void refreshView() {
        calculateCircleDrawPoints();
        invalidate();
    }

    //Set Text Style
    public void setTextStyle(int color, int ver_text_size, int hor_text_size) {
        textPaint.setColor(color);
        textPaint.setTextSize(ver_text_size);
        textPaint_hor.setTextSize(hor_text_size);
    }

    //Set graph Line color
    public void setGraphLineStyle(int color, int width) {
        paint.setColor(color);
        paint.setStrokeWidth(width);
        setRingColor(color);
        setWallAreaColor(color, 55);
    }


    //Set Node ring
    public void setRingRadius(int radius) {
        nodeRingRadius = radius;
    }

    public void setRingColor(int borderColor) {
        //Ring Border color
        circlePaint.setColor(borderColor);
        circlePaint.setStrokeWidth(3);
        circlePaint.setStyle(Paint.Style.STROKE);

        //Ring center color
        circlePaint_inside.setColor(viewbackgroundColor);
        circlePaint_inside.setStyle(Paint.Style.FILL);
        circlePaint_inside.setStrokeWidth(3);
    }


    //set View Color Schemes
    public void setViewBackgroundColor(int backgroundColor) {
        viewbackgroundColor = backgroundColor;
    }

    public void setWallAreaColor(int color, int alpha) {
        wallpaint.setColor(color);
        wallpaint.setAlpha(alpha);
        wallpaint.setStyle(Paint.Style.FILL);


        wallpaint.setShader(new LinearGradient(50, 50, 0, 700, color, Color.TRANSPARENT, Shader.TileMode.CLAMP));
    }

    //Set Vertical dimention Values
    public void setVerticalDimentionValues(int verticalDimention[]) {
        y = new int[verticalDimention.length];
        this.verticalDimention = verticalDimention;
    }

    //Set Horizontal dimention Values
    public void setHorizontalDimentionValues(String horizontalDimention[]) {
        x = new int[horizontalDimention.length];
        this.horizontalDimention = horizontalDimention;
        valuesOnRing = new String[horizontalDimention.length];
    }

    public void setWallpainted(boolean isWallPainted) {
        this.isWallPainted = isWallPainted;
    }

    public void setVerticleBackgorundLine(boolean isVerticleBackgroundGraphLineShown, int color, int strokwidth) {
        this.isVerticleBackgroundGraphLineShown = isVerticleBackgroundGraphLineShown;
        bgLine.setColor(color);
        bgLine.setStrokeWidth(strokwidth);
        bgLine.setAlpha(25);
    }

}
