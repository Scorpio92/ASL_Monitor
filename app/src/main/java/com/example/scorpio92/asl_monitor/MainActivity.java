package com.example.scorpio92.asl_monitor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import android.os.Handler;
import android.os.Message;

import static android.view.Gravity.CENTER;
import static android.view.Gravity.LEFT;


public class MainActivity extends ActionBarActivity {

    private TextView mTextView;
    private TextView protoLoading;
    private TextView TOP;
    private TextView ASL_STATUS;
    private TextView CHECK_STATUS;
    private TextView ASL_IMG;
    private String version;
    private String enabled;
    private String check_status;
    private String temp;
    private Button but;
    private Button but2;
    private Button but3;
    private ProgressBar myProgressBar;
    private int myProgress = 0;

    private String RECOVERY_IMG = "asl.img";
    private String DATA_IMG_PATH = "/data/asl/asl.img";
    private boolean DataExist=true;
    private boolean SdExist=true;
    private String Protocol="";
    private Integer filesCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TOP=(TextView)findViewById(R.id.textView2);

        try {
            File v = new File("/proc/asl/version");
            BufferedReader br = new BufferedReader(new FileReader(v));
            version = br.readLine();

            ASL_STATUS=(TextView)findViewById(R.id.textView3);

            try {
                File e = new File("/proc/asl/enabled");
                BufferedReader br2 = new BufferedReader(new FileReader(e));
                enabled = br2.readLine();

                //кнопка просмотра протокола ASL и разрешений
                but = (Button)findViewById(R.id.button);
                but2 = (Button)findViewById(R.id.button3);
                //кнопка лога
                but3 = (Button)findViewById(R.id.button2);

                if(enabled.equals("0"))
                {
                    ASL_STATUS.setText("ASL STATUS: DISABLED");
                    ASL_STATUS.setTextColor(Color.RED);
                    //скрываем кнопки
                    //butt.setVisibility(View.INVISIBLE);
                    //but2.setVisibility(View.INVISIBLE);
                    //but3.setVisibility(View.INVISIBLE);
                    but.setEnabled(false);
                    but2.setEnabled(false);
                    but3.setEnabled(false);

                }
                if(enabled.equals("1")) {
                    ASL_STATUS.setText("ASL STATUS: ENABLED");

                    //если включен - инициализируем поля ввывода данных
                    mTextView = (TextView) findViewById(R.id.textView);
                    CHECK_STATUS = (TextView) findViewById(R.id.textView4);
                    ASL_IMG = (TextView) findViewById(R.id.textView5);

                    try {
                        File n = new File("/dev/asl/need_recovery");
                        BufferedReader br3 = new BufferedReader(new FileReader(n));
                        check_status = br3.readLine();

                        if (check_status.equals("0")) {
                            CHECK_STATUS.setText("CHECK STATUS: SUCCESSFUL");
                            //скрываем кнопку лога
                            //but3.setEnabled(false);
                            but3.setVisibility(View.INVISIBLE);
                        }
                        if (check_status.equals("1")) {
                            CHECK_STATUS.setText("CHECK STATUS: FOUND TEMPERING");
                            CHECK_STATUS.setTextColor(Color.RED);
                        }
                    } catch (FileNotFoundException e2) {
                        e2.printStackTrace();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }

                    myProgressBar = (ProgressBar) findViewById(R.id.progressBar);
                    protoLoading = (TextView) findViewById(R.id.textView13);

                    protocolLoad();
                    //new Thread(myThread).start();
                    //myProgressBar.setVisibility(View.INVISIBLE);

                    //ASL.img
                    File asl_img = new File(DATA_IMG_PATH);
                    if(!asl_img.exists()) {
                        DataExist=false;
                    }
                    // проверяем доступность SD
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        ASL_IMG.setText("Not access to SD Card !"+ "\n");
                        return;
                    }
                    // получаем путь к SD
                    File sdPath = Environment.getExternalStorageDirectory();
                    // добавляем свой каталог к пути
                    sdPath = new File(sdPath.getAbsolutePath());
                    // формируем объект File, который содержит путь к файлу
                    asl_img = new File(sdPath, RECOVERY_IMG);
                    if(!asl_img.exists()) {
                        SdExist=false;
                    }

                    if((DataExist==true)&&(SdExist==true))
                    {
                        ASL_IMG.setEnabled(false);
                    }
                    if((DataExist==false)&&(SdExist==true))
                    {
                        ASL_IMG.append("Recovery image not found in DATA partition !");
                    }
                    if((DataExist==true)&&(SdExist==false))
                    {
                        ASL_IMG.append("Recovery image not found in SD Card !");
                    }
                    if((DataExist==false)&&(SdExist==false))
                    {
                        ASL_IMG.append("Recovery image not found in DATA partition and SD Card !");
                    }

                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

         catch (FileNotFoundException e) {
            e.printStackTrace();
            TOP.setText("ASL NOT ENABLE IN YOUR DEVICE !");
             TOP.setTextColor(Color.RED);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TOP.setText("Android Security List v" + version);
        //mTextView=(TextView)findViewById(R.id.textView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.asl_fops) {
            Intent intent = new Intent(MainActivity.this, ASLImageFOPS.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.options_item) {
            Intent intent = new Intent(MainActivity.this, Options.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.help_item) {
            Intent intent = new Intent(MainActivity.this, Help.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.exit_item) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getFilesCount()
    {
        try {
            File f = new File("/proc/asl/files_count");
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new FileReader(f));
            // читаем содержимое
            filesCount = Integer.parseInt(br.readLine());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void protocolLoad() {
        but.setEnabled(false);
        mTextView.setText("");
        mTextView.setGravity(LEFT);
        mTextView.setTextColor(Color.GREEN);

        if (Protocol.equals("")) {
            getFilesCount();
            myProgressBar.setVisibility(View.VISIBLE);
            myProgressBar.setMax(filesCount);
            //myProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.abc_dialog_material_background_light));

            new Thread(new Runnable() {
                public void run() {
                    try {
                        File f = new File("/dev/asl/asl_protocol");
                        // открываем поток для чтения
                        BufferedReader br4 = new BufferedReader(new FileReader(f));
                        String str = "";
                        //String str2 = "";
                        // читаем содержимое
                        while ((str = br4.readLine()) != null) {

                            Protocol = Protocol + str + "\n";
                            myHandle.sendMessage(myHandle.obtainMessage());
                        }
                        mTextView.post(new Runnable() {
                            public void run() {
                                mTextView.append(Protocol);
                                myProgressBar.setVisibility(View.INVISIBLE);
                                protoLoading.setVisibility(View.INVISIBLE);

                            }
                        });

                    } catch (FileNotFoundException e3) {
                        e3.printStackTrace();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }

                Handler myHandle = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        // TODO Auto-generated method stub
                        if (myProgress < filesCount) {
                            myProgress++;
                            myProgressBar.setProgress(myProgress);
                        } else {
                            myProgressBar.setProgress(0);
                            myProgress = 0;
                        }
                    }
                };
            }).start();
        }
        else {
            mTextView.append(Protocol);
        }
    }

    public void protocolClick(View view) {
            but2.setEnabled(true);
            but3.setEnabled(true);
            protocolLoad();
    }

    public void permissionClick(View view) {
        try {
            but.setEnabled(true);
            but2.setEnabled(false);
            but3.setEnabled(true);
            mTextView.setText("");
            mTextView.setGravity(LEFT);
            mTextView.setTextColor(Color.GREEN);

            File f = new File("/dev/asl/permissions");
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new FileReader(f));
            String str = "";
            // читаем содержимое
            while ((str = br.readLine()) != null) {
                mTextView.append(str + "\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void detectedClick(View view) {
        try {
            but.setEnabled(true);
            but2.setEnabled(true);
            but3.setEnabled(false);
            mTextView.setText("");
            mTextView.setGravity(CENTER);
            mTextView.setTextColor(Color.RED);

            File mod = new File("/dev/asl/mod_detected");
            BufferedReader br4 = new BufferedReader(new FileReader(mod));
            if(mod.length() > 2) {
                mTextView.append("Modified files: " + "\n\n");

                while ((temp = br4.readLine()) != null) {

                    mTextView.append(temp + "\n");
                }
            }

        } catch (FileNotFoundException e3) {
            e3.printStackTrace();
        } catch (IOException e3) {
            e3.printStackTrace();
        }

        try {
            File doa = new File("/dev/asl/doa_detected");
            BufferedReader br5 = new BufferedReader(new FileReader(doa));
            if(doa.length() > 2) {
                mTextView.append("\n" + "Deleted or added files: " + "\n\n");

                while ((temp = br5.readLine()) != null) {

                    mTextView.append(temp + "\n");
                }
            }

        } catch (FileNotFoundException e4) {
            e4.printStackTrace();
        } catch (IOException e4) {
            e4.printStackTrace();
        }
    }

    public void aslimg_click(View view) {
        Intent intent = new Intent(MainActivity.this, ASL_Image.class);
        startActivity(intent);
    }
}
