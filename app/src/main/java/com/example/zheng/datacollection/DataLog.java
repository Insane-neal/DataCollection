package com.example.zheng.datacollection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Zheng on 2016-04-24.
 */
public class DataLog implements SensorEventListener {

    public String writtenErrorMessage;
    public long startTime;

    public String[] sensorType;
    public String[] sensorLog;
    public int[] sensorID;
    public BufferedWriter[] bufferedWriters;


    public BufferedWriter positionBufferedWriter;
    public int positionID;
    public String positionLog;


    private Context context;
    private SensorManager sensorManager;
    private File dir;


    public DataLog() {
    }

    public DataLog(Context context, SensorManager sensorManager) {
        this.context = context;
        this.sensorManager = sensorManager;
    }

    public void setSensorType(String[] sensorTypeStringArray) {
        sensorType = sensorTypeStringArray;
    }

    public boolean start() {
        startTime = 0;
        if (!isExternalStorageWritable()) {
            return false;
        }
        dir = initDir();
        initBufferdWriters(dir);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor
                .TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor
                .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor
                .TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor
                .TYPE_GAME_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor
                .TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor
                .TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor
                .TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
        return true;
    }


    public void stop() {
        sensorManager.unregisterListener(this);
        closeBufferdWriters();
        String[] fileNames = new String[dir.listFiles().length];
        for (int i = 0; i < dir.listFiles().length; i++) {
            fileNames[i] = dir.listFiles()[i].getAbsolutePath();
        }
        MediaScannerConnection.scanFile(context, fileNames, null, null);
        dir = null;
        positionBufferedWriter = null;
    }


    /**
     * log sensor data based on different sensor types such as gyroscope, accelerometer, orientation, magnetometer, step
     * sensor, pressure sensor etc.
     */
    public void logData(SensorEvent event) {
        if (startTime == 0) {
            startTime = System.nanoTime();
        }
        float NS2S = 1.0f / 1000000000.0f;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GYROSCOPE:
                sensorLog[0] += sensorID[0]++ + "," + String.format("%.9f", (System.nanoTime() -
                        startTime) * NS2S) + "," + event.values[0] + "," + event.values[1] + "," +
                        event.values[2] + "," + System.getProperty("line.separator");
                writeLogToFileRealTime(0, sensorLog[0]);
                sensorLog[0] = "";
                break;
            case Sensor.TYPE_ACCELEROMETER:
                sensorLog[1] += sensorID[1]++ + "," + String.format("%.9f", (System.nanoTime() -
                        startTime) * NS2S) + "," + event.values[0] + "," + event.values[1] + "," +
                        event.values[2] + "," + System.getProperty("line.separator");
                writeLogToFileRealTime(1, sensorLog[1]);
                sensorLog[1] = "";
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                sensorLog[2] += sensorID[2]++ + "," + String.format("%.9f", (System.nanoTime() -
                        startTime) * NS2S) + "," + event.values[0] + "," + event.values[1] + "," +
                        event.values[2] + "," + System.getProperty("line.separator");
                writeLogToFileRealTime(2, sensorLog[2]);
                sensorLog[2] = "";
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                sensorLog[3] += sensorID[3]++ + "," + String.format("%.9f", (System.nanoTime() -
                        startTime) * NS2S) + "," + event.values[0] + "," + event.values[1] + "," +
                        event.values[2] + "," + System.getProperty("line.separator");
                writeLogToFileRealTime(3, sensorLog[3]);
                sensorLog[3] = "";
                break;
            case Sensor.TYPE_STEP_COUNTER:
                sensorLog[4] += sensorID[4]++ + "," + String.format("%.9f", (System.nanoTime() -
                        startTime) * NS2S) + ", 1," +
                        System.getProperty("line.separator");
                writeLogToFileRealTime(4, sensorLog[4]);
                sensorLog[4] = "";
                break;
            case Sensor.TYPE_PRESSURE:
                sensorLog[5] += sensorID[5]++ + "," + String.format("%.9f", (System.nanoTime() -
                        startTime) * NS2S) + "," + event.values[0] + "," + event.values[1] + "," +
                        event.values[2] + "," + System.getProperty("line.separator");
                writeLogToFileRealTime(5, sensorLog[5]);
                sensorLog[5] = "";
                break;
            default:
                break;
        }
    }

    public File initDir() {
        File dir = null;

        Calendar c = Calendar.getInstance();
        String dirName = "SensorData-" + c.get(Calendar.YEAR) + "-"
                + (c.get(Calendar.MONTH) + 1) + "-"
                + c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.HOUR)
                + "-" + c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND);
        try {
            //dir = new File(Environment.getExternalStorageDirectory()
            //+ File.separator + "SensorDataLog" + File.separator + dirName);
            dir = new File(this.context.getExternalFilesDir(null) +
                    File.separator + "SensorDataLog" + File.separator + dirName);
            if (!dir.exists()) {
                Boolean mkdir = dir.mkdirs();
                Log.d("Dir", "isDirExist:" + dir.exists());
                Log.d("Dir", "mkdir:" + mkdir);
            }
        } catch (Exception e) {
            Log.d("Dir", "Exception" + e.toString());
        }
        return dir;
    }

    public void initBufferdWriters(File dir) {
        sensorLog = new String[sensorType.length];
        sensorID = new int[sensorType.length];
        bufferedWriters = new BufferedWriter[sensorType.length];
        try {
            for (int i = 0; i < bufferedWriters.length; i++) {
                sensorLog[i] = "";
                sensorID[i] = 0;
                String filename = sensorType[i] + ".csv";
                File file = new File(dir, filename);
                bufferedWriters[i] = new BufferedWriter(new FileWriter(file));
            }

        } catch (Exception e) {
            Log.d("error", e.toString());
        }

    }


    /**
     * Write the logged data out to a persisted file real time.
     */
    public boolean writeLogToFileRealTime(int sensorIndex, String data) {
        boolean writtenFinished;
        if (isExternalStorageWritable()) {
            try {
                bufferedWriters[sensorIndex].append(data);
                bufferedWriters[sensorIndex].flush();
            } catch (Exception e) {
                writtenErrorMessage = e.toString();
                Log.d("writtenError", writtenErrorMessage);
            } finally {
                writtenFinished = true;
            }
        } else {
            writtenErrorMessage = "external storage is not writable";
            writtenFinished = false;
        }
        return writtenFinished;
    }


    public void closeBufferdWriters() {
        try {
            for (int i = 0; i < bufferedWriters.length; i++) {
                bufferedWriters[i].flush();
                //bufferedWriters[i].close();
                sensorID[i] = 0;
            }

            if (positionBufferedWriter != null) {
                positionBufferedWriter.flush();
                positionBufferedWriter.close();
            }
        } catch (Exception e) {
            Log.d("error", e.toString());
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {
        //logData(sensorEvent);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                logData(sensorEvent);
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
