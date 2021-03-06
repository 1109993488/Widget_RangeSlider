package com.blingbling.rangeslider.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.blingbling.widget.rangeslider.BGMRangeSlider;

public class MainActivity extends AppCompatActivity {

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tv);
        BGMRangeSlider slider = findViewById(R.id.slider);
        slider.setOnRangeChangeListener(new BGMRangeSlider.OnRangeChangeListener() {
            @Override
            public void onChanged(int startTime, int endTime, boolean touch) {
                tv.setText("progress-->" + startTime + "  " + endTime + "  " + touch);
                Log.e("TAG", "progress-->" + startTime + "  " + endTime + "  " + touch);
            }
        });
    }
}
