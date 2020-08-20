package app.wimt.cheese.permissions;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class PermissionsHandler implements EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {

    private final Activity activity;
    private final Fragment fragment;

    @Override
    public void onRationaleAccepted(int requestCode) {}

    @Override
    public void onRationaleDenied(int requestCode) {
callBack.onDenied();
    }

    public interface PermissionsCallBack {
        void onGranted();

        void onDenied();
    }

    private final PermissionsCallBack callBack;

    public PermissionsHandler(Activity activity, Fragment fragment, PermissionsCallBack callBack) {
        this.activity = activity;
        this.fragment = fragment;
        this.callBack = callBack;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        callBack.onGranted();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (activity != null && (EasyPermissions.somePermissionPermanentlyDenied(activity, perms))) {
            new AppSettingsDialog.Builder(activity).build().show();
        } else if (fragment != null && EasyPermissions.somePermissionPermanentlyDenied(fragment, perms)) {
            new AppSettingsDialog.Builder(fragment).build().show();
        } else {
            callBack.onDenied();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
