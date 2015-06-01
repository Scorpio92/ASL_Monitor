package com.example.scorpio92.asl_monitor;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import static android.view.Gravity.LEFT;

/**
 * Created by scorpio92 on 16.05.15.
 */
public class ASL_Image extends ActionBarActivity {

    private TextView mTextView;
    private String RECOVERY_IMG = "asl.img";
    private String DATA_IMG_PATH = "/data/asl/asl.img";
    private boolean block=false;
    private ProgressBar myProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asl_img);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mTextView=(TextView)findViewById(R.id.textView6);
        myProgressBar = (ProgressBar) findViewById(R.id.progressBar3);
        myProgressBar.setVisibility(View.INVISIBLE);

    }

    public void fixClick(View view) {
        File data_img = new File(DATA_IMG_PATH);
        if (!data_img.exists()) {
            // проверяем доступность SD
            if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                mTextView.setTextColor(Color.RED);
                mTextView.setGravity(LEFT);
                mTextView.append("\n"+"Not access to SD Card !");
                return;
            }
            // получаем путь к SD
            File sdPath = Environment.getExternalStorageDirectory();
            // добавляем свой каталог к пути
            sdPath = new File(sdPath.getAbsolutePath());
            // формируем объект File, который содержит путь к файлу
            final File asl_img = new File(sdPath, RECOVERY_IMG);
            if (asl_img.exists()) {
                if(block==false) {
                    block=true;
                    myProgressBar.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        public void run() {
                            FileInputStream src = null;
                            try {
                                src = new FileInputStream(asl_img);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            FileOutputStream dist = null;
                            try {
                                dist = new FileOutputStream(new File(DATA_IMG_PATH));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            FileChannel fcin = src.getChannel();
                            FileChannel fcout = dist.getChannel();

                            // выполнить копирование файла
                            try {
                                fcin.transferTo(0, fcin.size(), fcout);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // закрываем
                            try {
                                fcin.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                fcout.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                src.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                dist.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            mTextView.post(new Runnable() {
                                public void run() {
                                    mTextView.setTextColor(Color.GREEN);
                                    mTextView.setGravity(LEFT);
                                    mTextView.append("\n" + "Recovery image was copied to DATA partition !");
                                    myProgressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }).start();
                    block=false;
                }
            } else {
                mTextView.setTextColor(Color.RED);
                mTextView.setGravity(LEFT);
                mTextView.append("\n"+"Not found asl.img on SD card!");
                return;
            }
        }
        else
        {
            mTextView.setTextColor(Color.GREEN);
            mTextView.setGravity(LEFT);
            mTextView.append("\n"+"Recovery image was copied to DATA partition !");
        }
    }
}
