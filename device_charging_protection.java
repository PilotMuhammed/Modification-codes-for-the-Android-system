//  MainActivity.java:
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.biometrics.BiometricPrompt;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Vibrator;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private CancellationSignal cancellationSignal;
    private boolean isCharging = false;  // To track charging status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register a broadcast receiver to detect when the device is charging
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, intentFilter);
    }

    // BroadcastReceiver to listen for charging state changes
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

            if (isCharging) {
                // If the device is charging, request fingerprint authentication
                requestFingerprint();
            }
        }
    };

    // Method to handle fingerprint authentication
    private void requestFingerprint() {
        BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(this)
                .setTitle("Authenticate to confirm charging")
                .setDescription("Please authenticate using your fingerprint to confirm the device is charging")
                .setNegativeButton("Cancel", ContextCompat.getMainExecutor(this), (dialogInterface, i) -> {
                    // If authentication is canceled, trigger the alarm
                    triggerAlarm();
                }).build();

        biometricPrompt.authenticate(getCancellationSignal(), getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(MainActivity.this, "Fingerprint authenticated, charging confirmed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(MainActivity.this, "Authentication failed! Triggering alarm.", Toast.LENGTH_SHORT).show();
                // If fingerprint authentication fails, trigger the alarm
                triggerAlarm();
            }
        });
    }

    // Creates a cancellation signal to allow fingerprint authentication to be canceled
    private CancellationSignal getCancellationSignal() {
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(() -> Toast.makeText(MainActivity.this, "Authentication canceled", Toast.LENGTH_SHORT).show());
        return cancellationSignal;
    }

    // Method to trigger an alarm that won't stop without fingerprint authentication
    private void triggerAlarm() {
        // Use the device's vibrator as an alarm substitute
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            // For older Android versions
            vibrator.vibrate(1000);
        }

        // Continue to request fingerprint until authenticated
        requestFingerprint();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the battery receiver when the activity is destroyed
        unregisterReceiver(batteryReceiver);
    }
}


// Required Permissions in AndroidManifest.xml:
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<uses-permission android:name="android.permission.VIBRATE" />

