package app.wimt.cheese;

import android.Manifest;
import android.annotation.SuppressLint;

import com.example.tes.BuildConfig;

/**
 * Cheez is always the best when melted.. Here we just have some constants, to keep the code more cheezy
 */
public class Constants {


    public static int REQUEST_CHECK_SETTIGS = 16157; //used when we redirect user to the settings screen after they deny app permissions

    public static final String SERVICE_ACTION_GEOFENCE = BuildConfig.APPLICATION_ID + ".action.geofence"; //tells service about a geofence event
    public static final String SERVICE_ACTION_MODE = BuildConfig.APPLICATION_ID + ".action.mocked"; //tells service wether to run in mockmode or not
    public static final String MOCK_MODE = "location_mode"; //tells service wether to run in mockmode or not
    public static final String SERVICE_ACTION_MOCK_LOCATION = "action_mock_location"; //mock location
    public static final String SERVICE_MOCK_LOCATION = "mocked_location"; //mock location

    //passed by the CheesyService in pendingIntent so we know if we should show a found marker when launching the HomeScreen
    public static final String ARGS_FOUND_MARKERS = "cheese_markers_found";
    public static final String ARGS_MARKER = "cheese_marker";
    public static final String ARGS_MARKER_ITEMS = "cheese_marker_items";

    public static final float GEOFENCE_RADIUS_IN_METERS = 50.0f;


    public interface PERMISSIONS {
        int ACCESS_FINE_LOCATION = 16158;

        @SuppressLint("InlinedApi")
        String[] PERMISSION_ENTRIES = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) ?
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION} :
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    }
}
