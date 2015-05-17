package com.example.scorpio92.asl_monitor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import static android.view.Gravity.LEFT;


public class ASLImageFOPS extends ActionBarActivity {

    private TextView mTextView;
    private String DATA_IMG_PATH = "/data/asl/asl.img";
    private String RECOVERY_IMG = "asl.img";
    private boolean block=false;
    private String CalcSum;
    private String OrigSum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aslimage_fops);

        mTextView=(TextView)findViewById(R.id.textView10);
    }

    public void SHA1_Check(String location) throws Exception {

            MessageDigest md = MessageDigest.getInstance("SHA1");
            FileInputStream fis = new FileInputStream(location);
            byte[] dataBytes = new byte[1024];

            int nread = 0;

            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }

            byte[] mdbytes = md.digest();

            //convert the byte to hex format
            StringBuffer sb = new StringBuffer("");
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            CalcSum = sb.toString();
            mTextView.append("\n" + "Calculated SHA-1 hash for " + location +" is: " + CalcSum);

            fis.close();

            try {
                File f = new File("/proc/asl/asl_img_hash");
                BufferedReader br = new BufferedReader(new FileReader(f));
                OrigSum = br.readLine();
                //while ((OrigSum = br.readLine()) != null) {
                //    mTextView.append("\n" + "Original SHA-1 hash is: " + OrigSum);
                //}

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

    }

    public void checkClick(View view) {
        if (block == false) {
            block = true;
            //check asl.img on DATA
            File data_img = new File(DATA_IMG_PATH);
            if (data_img.exists()) {
                try {
                    SHA1_Check(DATA_IMG_PATH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mTextView.append("\n" + "File asl.img not found in DATA partition !");
            }

            if(Options.OnlyDATA==false) {
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
                if (asl_img.exists()) {
                    try {
                        SHA1_Check(sdPath + "/" + RECOVERY_IMG);
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
                mTextView.append("\n" + "File does not exists");

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
