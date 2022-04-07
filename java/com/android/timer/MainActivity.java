package com.android.timer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    /**
     * 0 not started
     * 1 in progress
     * 2 pause
     */
    private int status = 0;

    /**
     * how many seconds have been recorded
     */
    private long hasCountTime = 0;

    private Disposable countdownDisposable;

    private TextView timeTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sp = getSharedPreferences("test",MODE_MULTI_PROCESS);

        TextView tips = findViewById(R.id.tips);
        // get data from sp
        String dataStr = sp.getString("data","");
        if(!TextUtils.isEmpty(dataStr)) {
            tips.setText(dataStr);
        }

        timeTv = findViewById(R.id.time);

        EditText edit_text = findViewById(R.id.edit_text);


        findViewById(R.id.iv_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status == 1) {
                    showShortToast("already start");
                } else {
                    // If it is resumed
                    if(status == 2) {
                        // The status is assigned a value of 1
                        status = 1;
                        resetTimer(hasCountTime);
                        // If it is from stop to start
                    } else {
                        if(TextUtils.isEmpty(edit_text.getText().toString().trim())) {
                            showShortToast("please input task name");
                        } else {
                            // The status is assigned a value of 1
                            status = 1;
                            resetTimer(0);
                        }
                    }
                }
            }
        });

        findViewById(R.id.iv_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status == 2) {
                    showShortToast("already pause");
                } else {
                    // The state is assigned a value of 2
                    status = 2;
                    countdownDisposable.dispose();
                }
            }
        });

        findViewById(R.id.iv_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status == 0) {
                    showShortToast("already stop");
                } else {
                    if(TextUtils.isEmpty(edit_text.getText().toString().trim())) {
                        showShortToast("please input task name");
                    } else {
                        // The status is assigned a value of 0
                        status = 0;
                        // clear seconds
                        hasCountTime = 0;
                        // stop timing
                        countdownDisposable.dispose();
                        // store data in sp
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("data","You spent "+ timeTv.getText().toString() +" on " + edit_text.getText().toString() + " last time.");
                        editor.apply();
                        // get data from sp
                        String dataStr = sp.getString("data","");
                        if(!TextUtils.isEmpty(dataStr)) {
                            tips.setText(dataStr);
                        }
                    }
                }
            }
        });
    }

    public void showShortToast(String text){
        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT).show();
    }

    /**
     * reset the timer
     */
    private void resetTimer(long start){
        countdownDisposable = Flowable.intervalRange(start, 600000, 0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long time) throws Exception {
                        hasCountTime = time;
                        String hh = new DecimalFormat("00").format(time / 3600);
                        String mm = new DecimalFormat("00").format(time % 3600 / 60);
                        String ss = new DecimalFormat("00").format(time % 60);
                        timeTv.setText(hh + ":" + mm + ":" + ss);
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                    }
                })
                .subscribe();
    }
}