package com.teramatrix.fleetiq.Utils;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.teramatrix.fleetiq.Login;
import com.teramatrix.fleetiq.MainActivity;
import com.teramatrix.fleetiq.R;

import java.lang.reflect.Field;

/**
 * Created by arun.singh on 7/28/2016.
 */
public class MyAnnotation {

    public void bind(Activity activity)
    {
        Field[] fields = activity.getClass().getFields();

        for(Field field:fields)
        {
            TextViewAnnotation textViewAnnotation = field.getAnnotation(TextViewAnnotation.class);
            if(textViewAnnotation!=null)
            {

                View view = activity.findViewById(textViewAnnotation.value());
                try {

                    field.set(activity,view);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
