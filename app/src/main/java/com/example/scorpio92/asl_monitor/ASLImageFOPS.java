package com.example.scorpio92.asl_monitor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.view.Gravity.LEFT;


public class ASLImageFOPS extends ActionBarActivity {

    private TextView mTextView;
    private String DATA_IMG_PATH = "/data/asl/asl.img";
    private String RECOVERY_IMG = "asl.img";
    private boolean block=false;
    private String CalcSum;
    private String OrigSum;
    private ProgressBar myProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aslimage_fops);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mTextView=(TextView)findViewById(R.id.textView10);
        myProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        myProgressBar.setVisibility(View.INVISIBLE);
    }

    public void SHA1_Check(final String location, final boolean last) throws Exception {
        //myProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            public void run() {
                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance("SHA1");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(location);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                byte[] dataBytes = new byte[1024];

                int nread = 0;

                try {
                    while ((nread = fis.read(dataBytes)) != -1) {
                        md.update(dataBytes, 0, nread);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                byte[] mdbytes = md.digest();

                //convert the byte to hex format
                StringBuffer sb = new StringBuffer("");
                for (int i = 0; i < mdbytes.length; i++) {
                    sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
                }

                CalcSum = sb.toString();
                //mTextView.append("\n" + "Calculated SHA-1 hash for " + location + " is: " + CalcSum);

                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mTextView.post(new Runnable() {
                    public void run() {
                        mTextView.append("\n" + "Calculated SHA-1 hash for " + location + " is: " + CalcSum);

                        try {
                            File f = new File("/proc/asl/asl_img_hash");
                            BufferedReader br = new BufferedReader(new FileReader(f));
                            OrigSum = br.readLine();

                            if (CalcSum.equals(OrigSum)) {
                                mTextView.append("\n" + location + " file is original !");
                            } else {
                                mTextView.append("\n" + location + " file is not original !");
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(last) {
                            myProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        }).start();

    }

    public void checkClick(View view) {
        if (block == false) {
            block = true;
            //check asl.img on DATA
            if(Options.OnlyDATA==true) {
                File data_img = new File(DATA_IMG_PATH);
                if (data_img.exists()) {
                    try {
                        myProgressBar.setVisibility(View.VISIBLE);
                        SHA1_Check(DATA_IMG_PATH, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mTextView.append("\n" + "File asl.img not found in DATA partition !");
                }
            }

            else {
                //check asl.img on SD Card
                if (!Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    mTextView.setTextColor(Color.RED);
                    mTextView.setGravity(LEFT);
                    mTextView.append("\n" + "Not access to SD Card !");
                    return;
                }
                // получаем путь к SD
                File sdPath = Environment.getExternalStorageDirectory();
                // добавляем свой каталог к пути
                sdPath = new File(sdPath.getAbsolutePath());
                // формируем объект File, который содержит путь к файлу
                File asl_img = new File(sdPath, RECOVERY_IMG);

                //check asl.img on DATA
                File data_img = new File(DATA_IMG_PATH);
                if (data_img.exists()) {
                    try {
                        myProgressBar.setVisibility(View.VISIBLE);
                        if (asl_img.exists()) {
                            SHA1_Check(DATA_IMG_PATH, false);
                        }
                        else {
                            SHA1_Check(DATA_IMG_PATH, true);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mTextView.append("\n" + "File asl.img not found in DATA partition !");
                }

                if (asl_img.exists()) {
                    try {
                        if(myProgressBar.getVisibility()==View.INVISIBLE) {
                            myProgressBar.setVisibility(View.VISIBLE);
                        }
                        SHA1_Check(sdPath + "/" + RECOVERY_IMG, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mTextView.setTextColor(Color.RED);
                    mTextView.setGravity(LEFT);
                    mTextView.append("\n" + "Not found asl.img on SD card!");
                    return;
                }
            }

            //myProgressBar.setVisibility(View.INVISIBLE);
            block = false;
        }

    }

    public void remove()
    {
        try{
            File file = new File(DATA_IMG_PATH);
            boolean status = file.delete();
            if (status)
                mTextView.append("\n"+"File deleted successfully !");
            else
                mTextView.append("\n" + "File does not exists !");

        }catch(Exception e){

            e.printStackTrace();

        }

    }

    public void removeClick(View view) {
        remove();
    }

    public void copyClick(View view) {
        Intent intent = new Intent(ASLImageFOPS.this, ASL_Image.class);
        startActivity(intent);
    }
}
