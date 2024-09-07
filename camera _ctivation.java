import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyDeviceAdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        // This is called when the user fails to enter the password
        Toast.makeText(context, "Password failed!", Toast.LENGTH_SHORT).show();

        // Turn on the camera to take a picture
        Intent cameraIntent = new Intent(context, CameraService.class);
        context.startService(cameraIntent);
    }
}


<receiver android:name=".MyDeviceAdminReceiver"
    android:label="Device Admin">
    <meta-data android:name="android.app.device_admin"
        android:resource="@xml/device_admin" />
    <intent-filter>
        <action android:name="android.app.action.DEVICE_ADMIN_ENABLED" />
    </intent-filter>
</receiver>


<device-admin xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-policies>
        <limit-password />
        <watch-login />
    </uses-policies>
</device-admin>


