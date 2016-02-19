package com.example.kanishk.direction;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;

public class MainActivity extends Activity implements SensorEventListener {

    private float mLastX, mLastY, mLastZ=0;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final float NOISE = (float) 1.0;
    private float acc_x,acc_y,acc_z,gyro_x,gyro_y,gyro_z,mag_x,mag_y,mag_z, light = 0.0f;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        activate();
    }

    public void activate(){
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mSensorManager.registerListener(MainActivity.this, mAccelerometer, SENSOR_DELAY_NORMAL);
                mSensorManager.registerListener(MainActivity.this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
                mSensorManager.registerListener(MainActivity.this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
                mSensorManager.registerListener(MainActivity.this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
            }
        });

        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onPause();
            }
        });
    }

    protected void onResume() {
        super.onResume();
        activate();
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // can be safely ignored
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView tvX = (TextView) findViewById(R.id.x_axis);
        TextView tvY = (TextView) findViewById(R.id.y_axis);
        TextView tvZ = (TextView) findViewById(R.id.z_axis);
        ImageView iv = (ImageView) findViewById(R.id.image);
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                float deltaX = Math.abs(mLastX - x);
                float deltaY = Math.abs(mLastY - y);
                float deltaZ = Math.abs(mLastZ - z);
                if (deltaX < NOISE) x = (float) mLastX;
                if (deltaY < NOISE) y = (float) mLastY;
                if (deltaZ < NOISE) z = (float) mLastZ;
                mLastX = x;
                mLastY = y;
                mLastZ = z;
                tvX.setText(Float.toString(x));
                tvY.setText(Float.toString(y));
                tvZ.setText(Float.toString(z));
                Log.d("x-axis", String.valueOf(x));
                Log.d("y-axis", String.valueOf(y));
                Log.d("z-axis", String.valueOf(z));
                try {
                        createCsv(String.valueOf(x), String.valueOf(y), String.valueOf(z), event.timestamp / 1000000);
                        acc_x = x;
                        acc_y = y;
                        acc_z = z;
                    }
                catch (IOException e) {
                        e.printStackTrace();
                    }
                iv.setVisibility(View.VISIBLE);
                if (x > y) {
                    iv.setImageResource(R.drawable.horizontal);
                } else if (y > x) {
                    iv.setImageResource(R.drawable.vertical);
                } else {
                    iv.setVisibility(View.INVISIBLE);
                }
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Log.d("x-axis-gyro", String.valueOf(x));
            Log.d("y-axis-gyro", String.valueOf(y));
            Log.d("z-axis-gyro", String.valueOf(z));
            try {
                    createCsvGy(String.valueOf(x), String.valueOf(y), String.valueOf(z), event.timestamp / 1000000);
                    gyro_x = x;
                    gyro_y = y;
                    gyro_z = z;
                }
            catch (IOException e) {
                    e.printStackTrace();
                }
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            Log.d("x-axis-mag", String.valueOf(x));
            Log.d("y-axis-mag", String.valueOf(y));
            Log.d("z-axis-mag", String.valueOf(z));
            mag_x = x;
            mag_y = y;
            mag_z = z;
        };
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            Log.d("light", String.valueOf(x));
            light = x;
        }

        try {
            createDataCsv(event.timestamp / 1000000, String.valueOf(acc_x), String.valueOf(acc_y), String.valueOf(acc_z), String.valueOf(gyro_x), String.valueOf(gyro_y), String.valueOf(gyro_z), String.valueOf(mag_x), String.valueOf(mag_y), String.valueOf(mag_z), String.valueOf(light));
            doStuff(acc_x,acc_y,acc_z);
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.d("here","Eroor");
        }
    }

    public void doStuff(final float acc_x,final float acc_y,final float acc_z)throws IOException
    {
//        ParseObject testObject = new ParseObject("Data");
//        testObject.put("acc_x", acc_x);
//        testObject.saveInBackground();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Data");
// Retrieve the object by id
        query.getInBackground("b0jFOwNg8p", new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, com.parse.ParseException e) {
                if (e == null) {
                    // Now let's update it with some new data. In this case, only cheatMode and score
                    // will get sent to the Parse Cloud. playerName hasn't changed.
                    parseObject.put("acc_x", acc_x);
                    parseObject.put("acc_y", acc_y);
                    parseObject.put("acc_z",acc_z);
                    parseObject.saveInBackground();
                }
            }
        });
    }

    public void createCsv(String x, String y, String z, float timestamp) throws IOException {
        File folder = new File(Environment.getExternalStorageDirectory() + "/project");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            // Do something on success
            String csv = "/storage/emulated/0/project/AccelerometerValue.csv";
            FileWriter file_writer = new FileWriter(csv, true);

            String s = timestamp + "," + x + "," + y + "," + z + "\n";

            file_writer.append(s);
            file_writer.close();

        }

    }

    public void createCsvGy(String x, String y, String z, float timestamp) throws IOException {
        File folder = new File(Environment.getExternalStorageDirectory() + "/project");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            // Do something on success
            String csv = "/storage/emulated/0/project/GyroscopeValue.csv";
            FileWriter file_writer = new FileWriter(csv, true);
            String s = timestamp + "," + x + "," + y + "," + z + "\n";

            file_writer.append(s);
            file_writer.close();

        }
    }

    public void createDataCsv(float timestamp, String acc_x, String acc_y, String acc_z, String gyro_x, String gyro_y, String gyro_z, String mag_x, String mag_y, String mag_z, String light ) throws IOException {
        File folder = new File(Environment.getExternalStorageDirectory() + "/project");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            // Do something on success
            String csv = "/storage/emulated/0/project/AllValues.csv";
            FileWriter file_writer = new FileWriter(csv, true);

            String s = timestamp + "," + acc_x + "," + acc_y + "," + acc_z + ","+gyro_x+"," +gyro_y+","+gyro_z+","+mag_x+","+mag_y+","+mag_z+","+light + "\n";

            file_writer.append(s);
            file_writer.close();

        }
    }
}