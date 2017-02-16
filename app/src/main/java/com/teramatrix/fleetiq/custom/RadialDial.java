package com.teramatrix.fleetiq.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.teramatrix.fleetiq.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by arun.singh on 7/26/2016.
 */
public class RadialDial extends View {

    private Context context;
    private int view_height;
    private int view_width;
    private Paint paint = new Paint();
    ;
    private Paint paint_line = new Paint();
    ;
    private Paint paint_glow = new Paint();
    ;

    private Bitmap gasStation;
    int gasStation_x;
    int gasStation_y;

    PointF[] pointFs = new PointF[4];

    PointF[] pointFs_inner_line_meter = new PointF[36];

    boolean bigEffect = false;


    int inner_flap_angle_offset_for_aimation = 0;

    Timer timer;

    public RadialDial(Context context) {
        super(context);
        intViews(context);
    }

    public RadialDial(Context context, AttributeSet attrs) {
        super(context, attrs);
        intViews(context);
    }

    public RadialDial(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        intViews(context);
    }

    private void intViews(Context context) {
        this.context = context;

        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setAlpha(50);
        paint.setStrokeWidth(4);
        setLayerType(LAYER_TYPE_SOFTWARE, paint);


        paint_glow.setColor(Color.parseColor("#00BCD4"));
        paint_glow.setShadowLayer(60, 0, 0, Color.parseColor("#00BCD4"));

        paint_line.setColor(Color.parseColor("#FFFFFF"));
        paint_line.setAlpha(60);
        paint_line.setStrokeWidth(4);


        gasStation = BitmapFactory.decodeResource(getResources(), R.drawable.gas_station_img);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        view_width = View.MeasureSpec.getSize(widthMeasureSpec);
        view_height = View.MeasureSpec.getSize(heightMeasureSpec);


        PointF pointF = new PointF();
        pointF.set(view_width / 2, view_height / 2);

        pointFs[0] = getPosition(pointF, view_width / 2, 45);
        pointFs[1] = getPosition(pointF, view_width / 2, 45 * 3);
        pointFs[2] = getPosition(pointF, view_width / 2, 45 * 5);
        pointFs[3] = getPosition(pointF, view_width / 2, 45 * 7);


        PointF pointF_gass_station_image_cordinate = getPosition(pointF, view_width / 2 - 100, 90);
        gasStation_x = (int) pointF_gass_station_image_cordinate.x - 15;
        gasStation_y = (int) pointF_gass_station_image_cordinate.y;



        /*Experiment 2---------------*/
        for (int i = 0; i < pointFs_inner_line_meter.length; i++) {
            pointFs_inner_line_meter[i] = getPosition(pointF, view_width / 2 - 155 - 80 - 10, i * 10);
        }


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Draw oter Circle
        /*paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setAlpha(50);
        canvas.drawCircle(view_width / 2, view_height / 2, view_width / 2, paint);*/

        //Draw Lines at 45 angles
        /*for (int i = 0; i < pointFs.length; i++) {
            canvas.drawLine(view_width / 2, view_height / 2, pointFs[i].x, pointFs[i].y, paint_line);
        }*/

        //Draw inner Circle with glow effect
//        canvas.drawCircle(view_width / 2, view_height / 2, view_width / 2 - 120, paint_glow);

        //draw inner dark circle
/*        paint = new Paint();
        paint.setColor(Color.parseColor("#263238"));
        canvas.drawCircle(view_width / 2, view_height / 2, view_width / 2 - 125, paint);*/


        //draw image Bitmap - Gas Station
//        canvas.drawBitmap(gasStation, gasStation_x,gasStation_y,paint);



        /*---------------------------------Experiment---------------------------------------------*/
        //draw arc
        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setAlpha(110);
        final RectF oval = new RectF();


        int angle_gap = 1;
        int circle_pop_up_offset = 25;
        if (!bigEffect) {
            oval.set(0 + circle_pop_up_offset, 0 + circle_pop_up_offset, view_width - circle_pop_up_offset, view_height - circle_pop_up_offset);
            canvas.drawArc(oval, 45 * 1 + angle_gap, 90 - angle_gap, true, paint);
            canvas.drawArc(oval, 45 * 3 + angle_gap, 90 - angle_gap, true, paint);
            canvas.drawArc(oval, 45 * 5 + angle_gap, 90 - angle_gap, true, paint);
            canvas.drawArc(oval, 45 * 7 + angle_gap, 90 - angle_gap, true, paint);
        } else {
            oval.set(0, 0, view_width, view_height);
            canvas.drawArc(oval, 45 * 5 + angle_gap, 90 - angle_gap, true, paint);
            oval.set(0 + circle_pop_up_offset, 0 + circle_pop_up_offset, view_width - circle_pop_up_offset, view_height - circle_pop_up_offset);
            canvas.drawArc(oval, 45 * 1 + angle_gap, 90 - angle_gap, true, paint);
            canvas.drawArc(oval, 45 * 3 + angle_gap, 90 - angle_gap, true, paint);
            canvas.drawArc(oval, 45 * 7 + angle_gap, 90 - angle_gap, true, paint);
        }

        //Draw Lines at 45 angles
       /* for (int i = 0; i < pointFs.length; i++) {
            canvas.drawLine(view_width / 2, view_height / 2, pointFs[i].x, pointFs[i].y, paint_line);
        }*/

        //Draw inner Circle with glow effect
        canvas.drawCircle(view_width / 2, view_height / 2, view_width / 2 - 120, paint_glow);

//        draw inner dark circle
        paint = new Paint();
        paint.setColor(Color.parseColor("#263238"));
        canvas.drawCircle(view_width / 2, view_height / 2, view_width / 2 - 125, paint);


        //Experiment 2------------------------------------------------------------------------------
        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setAlpha(50);
        circle_pop_up_offset = 140;
        angle_gap = 0;
        oval.set(0 + circle_pop_up_offset, 0 + circle_pop_up_offset, view_width - circle_pop_up_offset, view_height - circle_pop_up_offset);
        canvas.drawArc(oval, 45 * 1 + angle_gap, 90 - angle_gap, true, paint);
        canvas.drawArc(oval, 45 * 3 + angle_gap, 90 - angle_gap, true, paint);
        canvas.drawArc(oval, 45 * 5 + angle_gap, 90 - angle_gap, true, paint);
        canvas.drawArc(oval, 45 * 7 + angle_gap, 90 - angle_gap, true, paint);

        paint = new Paint();
        paint.setColor(Color.parseColor("#263238"));
        canvas.drawCircle(view_width / 2, view_height / 2, view_width / 2 - circle_pop_up_offset - 8, paint);

        //draw 8 inner Flaps
        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setAlpha(50);
        circle_pop_up_offset = 140 + 15;
        angle_gap = 2;
        oval.set(0 + circle_pop_up_offset, 0 + circle_pop_up_offset, view_width - circle_pop_up_offset, view_height - circle_pop_up_offset);

        if (360 - inner_flap_angle_offset_for_aimation == 45)
            inner_flap_angle_offset_for_aimation = 0;

        int start_angle = (45 + inner_flap_angle_offset_for_aimation);
        canvas.drawArc(oval, start_angle + angle_gap, 45 - angle_gap, true, paint);
        canvas.drawArc(oval, start_angle + (45 * 1) + angle_gap, 45 - angle_gap, true, paint);
        canvas.drawArc(oval, start_angle + (45 * 2) + angle_gap, 45 - angle_gap, true, paint);
        canvas.drawArc(oval, start_angle + (45 * 3) + angle_gap, 45 - angle_gap, true, paint);
        canvas.drawArc(oval, start_angle + (45 * 4) + angle_gap, 45 - angle_gap, true, paint);
        canvas.drawArc(oval, start_angle + (45 * 5) + angle_gap, 45 - angle_gap, true, paint);
        canvas.drawArc(oval, start_angle + (45 * 6) + angle_gap, 45 - angle_gap, true, paint);
        canvas.drawArc(oval, start_angle + (45 * 7) + angle_gap, 45 - angle_gap, true, paint);

        paint = new Paint();
        paint.setColor(Color.parseColor("#263238"));
        canvas.drawCircle(view_width / 2, view_height / 2, view_width / 2 - circle_pop_up_offset - 80, paint);

        //Draw inner line meter
        paint = new Paint();
        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setAlpha(160);
        for (int i = 0; i < pointFs_inner_line_meter.length; i++) {
            canvas.drawLine(view_width / 2, view_height / 2, pointFs_inner_line_meter[i].x, pointFs_inner_line_meter[i].y, paint);
        }

        paint = new Paint();
        paint.setColor(Color.parseColor("#263238"));
        canvas.drawCircle(view_width / 2, view_height / 2, view_width / 2 - circle_pop_up_offset - 80 - 20, paint);


        //Draw Left Spoon----------------------------

        //1.background of left spoon- dark background

        int left_spoon_start_angle = 45*4;
        paint = new Paint();
        paint.setColor(Color.parseColor("#263238"));
        circle_pop_up_offset = 140 + 15 + 15;
        oval.set(0 + circle_pop_up_offset, 0 + circle_pop_up_offset, view_width - circle_pop_up_offset, view_height - circle_pop_up_offset);
        canvas.drawArc(oval, left_spoon_start_angle - 20, 40, true, paint);

        //2. highlighted part of spoon
        paint.setColor(Color.parseColor("#00BCD4"));
        circle_pop_up_offset = circle_pop_up_offset + 15 + 1;
        oval.set(0 + circle_pop_up_offset, 0 + circle_pop_up_offset, view_width - circle_pop_up_offset, view_height - circle_pop_up_offset);
        canvas.drawArc(oval, left_spoon_start_angle - 15, 30, true, paint);

        paint = new Paint();
        paint.setColor(Color.parseColor("#263238"));
        circle_pop_up_offset = circle_pop_up_offset + 20;
        oval.set(0 + circle_pop_up_offset, 0 + circle_pop_up_offset, view_width - circle_pop_up_offset, view_height - circle_pop_up_offset);
        canvas.drawArc(oval, left_spoon_start_angle - 14, 28, true, paint);

        paint = new Paint();
        paint.setColor(Color.parseColor("#00BCD4"));
        circle_pop_up_offset = circle_pop_up_offset + 20 + 40;
        oval.set(0 + circle_pop_up_offset, 0 + circle_pop_up_offset, view_width - circle_pop_up_offset, view_height - circle_pop_up_offset);
        canvas.drawArc(oval, left_spoon_start_angle - 14, 28, true, paint);

        //Draw Right Spoon----------------------------

        int right_spoon_start_angle = 45*8;
        //1.background of Right spoon- dark background
        paint = new Paint();
        paint.setColor(Color.parseColor("#263238"));
        circle_pop_up_offset = 140 + 15 + 15;
        oval.set(0 + circle_pop_up_offset, 0 + circle_pop_up_offset, view_width - circle_pop_up_offset, view_height - circle_pop_up_offset);
        canvas.drawArc(oval, right_spoon_start_angle - 20, 40, true, paint);

        //2. highlighted part of spoon
        paint.setColor(Color.parseColor("#00BCD4"));
        circle_pop_up_offset = circle_pop_up_offset + 15 + 1;
        oval.set(0 + circle_pop_up_offset, 0 + circle_pop_up_offset, view_width - circle_pop_up_offset, view_height - circle_pop_up_offset);
        canvas.drawArc(oval, right_spoon_start_angle - 15, 30, true, paint);

        paint = new Paint();
        paint.setColor(Color.parseColor("#263238"));
        circle_pop_up_offset = circle_pop_up_offset + 20;
        oval.set(0 + circle_pop_up_offset, 0 + circle_pop_up_offset, view_width - circle_pop_up_offset, view_height - circle_pop_up_offset);
        canvas.drawArc(oval, right_spoon_start_angle - 14, 28, true, paint);

        paint = new Paint();
        paint.setColor(Color.parseColor("#00BCD4"));
        circle_pop_up_offset = circle_pop_up_offset + 20 + 40;
        oval.set(0 + circle_pop_up_offset, 0 + circle_pop_up_offset, view_width - circle_pop_up_offset, view_height - circle_pop_up_offset);
        canvas.drawArc(oval, right_spoon_start_angle - 14, 28, true, paint);

        paint = new Paint();
        paint.setColor(Color.parseColor("#263238"));
        canvas.drawCircle(view_width / 2, view_height / 2, view_width / 2 - 155 - 80 - 20 - 18, paint);


        if(timer==null)
            startAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            paint_glow.setShadowLayer(90, 0, 0, Color.parseColor("#00BCD4"));
            bigEffect = true;
            invalidate();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            paint_glow.setShadowLayer(60, 0, 0, Color.parseColor("#00BCD4"));
            bigEffect = false;
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private PointF getPosition(PointF center, float radius, float angle) {

        PointF p = new PointF((float) (center.x + radius * Math.cos(Math.toRadians(angle))),
                (float) (center.y + radius * Math.sin(Math.toRadians(angle))));
        return p;
    }

    private void startAnimation() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                if (inner_flap_angle_offset_for_aimation >= 360)
                    inner_flap_angle_offset_for_aimation = 0;


                    inner_flap_angle_offset_for_aimation = inner_flap_angle_offset_for_aimation + 2;
                    ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    });
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask,500,50);

    }
}
