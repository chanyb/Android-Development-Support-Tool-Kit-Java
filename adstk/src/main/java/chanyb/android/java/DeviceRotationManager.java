package chanyb.android.java;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.NonNull;

public class DeviceRotationManager {
    public enum ROTATE_STATE {
        PORTRAIT, PORTRAIT_REVERSE, LANDSCAPE, LANDSCAPE_REVERSE, UNKNOWN
    }
    private static DeviceRotationManager instance = new DeviceRotationManager();
    private DeviceRotationManager() {
    }

    public static DeviceRotationManager getInstance() {
        if (instance == null) instance = new DeviceRotationManager();
        return instance;
    }

    private static final int RADIAN_TO_DEGREE = -57;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;
    private DeviceRotateListener listener;

    public interface DeviceRotateListener {
        void onPortrait();
        void onPortraitReverse();
        void onLandscape();
        void onLandscapeReverse();
    }

    private void addListener(@NonNull DeviceRotateListener listener) {
        this.listener = listener;
    }


    public void unregisterListener() {
        if(sensorEventListener != null && sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
            this.listener = null;
        }
    }

    public void registerListener(Context mContext, DeviceRotateListener listener) {
        if (this.listener != null) return; // 한번에 한 곳에서만 register 할 수 있다.

        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if(sensor == null) {
            Log.i("this", "센서가 존재하지 않습니다.");
        }

        addListener(listener);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                ROTATE_STATE STATE = checkOrientation(event.values);

                if (STATE == ROTATE_STATE.PORTRAIT) {
                    listener.onPortrait();
                } else if (STATE == ROTATE_STATE.PORTRAIT_REVERSE) {
                    listener.onPortraitReverse();
                } else if (STATE == ROTATE_STATE.LANDSCAPE) {
                    listener.onLandscape();
                } else if (STATE == ROTATE_STATE.LANDSCAPE_REVERSE) {
                    listener.onLandscapeReverse();
                } else {
                    // UNKNOWN
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(sensorEventListener, sensor, 10000);
    }


    private ROTATE_STATE checkOrientation(float[] vectors) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);

        int axisX = SensorManager.AXIS_X;
        int axisZ = SensorManager.AXIS_Z;

        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisZ, adjustedRotationMatrix);

        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        float azimuch = orientation[0] * RADIAN_TO_DEGREE;
        float pitch = orientation[1] * RADIAN_TO_DEGREE;
        float roll = orientation[2] * RADIAN_TO_DEGREE;

        return calculateOrientation(roll, pitch);
    }

    private ROTATE_STATE calculateOrientation(float roll, float pitch) {
        if (-120 < roll && roll < -60) {
            return ROTATE_STATE.LANDSCAPE_REVERSE;
        } else if (60 < roll && roll < 120) {
            return ROTATE_STATE.LANDSCAPE;
        } else if ((-30 < roll && roll < 30)) {
            return ROTATE_STATE.PORTRAIT;
        } else if ((150 < roll && roll < 180) || (-180 < roll && roll < -150)) {
            return ROTATE_STATE.PORTRAIT_REVERSE;
        } else {
            return ROTATE_STATE.UNKNOWN;
        }
    }

}
