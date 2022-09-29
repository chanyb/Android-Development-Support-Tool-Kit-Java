package chanyb.android.java;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;


public class GoogleLocationManger {

    private static FusedLocationProviderClient fusedLocationClient;
    private static GoogleLocationManger instance = new GoogleLocationManger();

    private GoogleLocationManger() {
    }

    public static GoogleLocationManger getInstance() {
        if(fusedLocationClient == null) fusedLocationClient = LocationServices.getFusedLocationProviderClient(GlobalApplcation.getContext());
        if(instance == null) instance = new GoogleLocationManger();
        return instance;
    }

    public void getUserLocation(FutureTaskRunner.Callback callback) {
        String [] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };
        FutureTaskRunner<Object> futureTaskRunner = new FutureTaskRunner();

        futureTaskRunner.nextTask(() -> {
            int ACCESS_FINE_LOCATION = ContextCompat.checkSelfPermission(GlobalApplcation.getContext(), permissions[0]);
            if(ACCESS_FINE_LOCATION == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(GlobalApplcation.currentActivity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                while (true) {
                    ACCESS_FINE_LOCATION = ContextCompat.checkSelfPermission(GlobalApplcation.getContext(), permissions[0]);
                    if(ACCESS_FINE_LOCATION != PackageManager.PERMISSION_DENIED) break;
                }
            }
            return true;
        });

        futureTaskRunner.nextTask(() -> {
            Thread.sleep(100);
            int ACCESS_COARSE_LOCATION  = ContextCompat.checkSelfPermission(GlobalApplcation.getContext(),  permissions[1]);
            if(ACCESS_COARSE_LOCATION == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(GlobalApplcation.currentActivity, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                do {
                    ACCESS_COARSE_LOCATION = ContextCompat.checkSelfPermission(GlobalApplcation.getContext(), permissions[1]);
                } while (ACCESS_COARSE_LOCATION == PackageManager.PERMISSION_DENIED);
            }
            return true;
        });

        futureTaskRunner.nextTask(() -> {
            Thread.sleep(100);
            int ACCESS_BACKGROUND_LOCATION  = ContextCompat.checkSelfPermission(GlobalApplcation.getContext(), permissions[2]);
            if(ACCESS_BACKGROUND_LOCATION == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(GlobalApplcation.currentActivity, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 3);
                do {
                    ACCESS_BACKGROUND_LOCATION = ContextCompat.checkSelfPermission(GlobalApplcation.getContext(), permissions[2]);
                } while (ACCESS_BACKGROUND_LOCATION == PackageManager.PERMISSION_DENIED);
            }
            return true;
        });

        futureTaskRunner.nextTask(() -> {
            Task<Location> task = fusedLocationClient.getLastLocation();
            while(true) {
                if(task.isComplete()) break;
            }
            return task.getResult();
        });

        futureTaskRunner.setCallback(callback);

        futureTaskRunner.start();

    }
}
