package com.example.lenovo.correctly;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.example.lenovo.PronunceActivity;

public class BoardingActivity extends AppCompatActivity {
    public Button learn,check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boarding);
        learn= (Button) findViewById(R.id.learn);
        check= (Button) findViewById(R.id.Check);
        learn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Animation pulse = AnimationUtils.loadAnimation
                        (getApplicationContext(), R.anim.pulse);
                learn.startAnimation(pulse);

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        learn.clearAnimation();

                        startActivity(new Intent(BoardingActivity.this,fluencyActivity.class));

                    }

                };

                Handler h = new Handler();
                h.postDelayed(r, 2000);


            }
        });
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation pulse = AnimationUtils.loadAnimation
                        (getApplicationContext(), R.anim.pulse);
                check.startAnimation(pulse);

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        check.clearAnimation();
                        startActivity(new Intent(BoardingActivity.this,PronunceActivity.class));

                    }

                };

                Handler h = new Handler();
                h.postDelayed(r, 2000);


            }
        });

    }

}
