import android.hardware.biometrics.BiometricPrompt;
import android.os.CancellationSignal;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private CancellationSignal cancellationSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting up the button to turn on fingerprint authentication
        findViewById(R.id.authenticateButton).setOnClickListener(view -> authenticateUser());
    }

    private void authenticateUser() {
        BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(this)
                .setTitle("Authentication required")
                .setDescription("Please authenticate to continue")
                .setNegativeButton("Cancel", ContextCompat.getMainExecutor(this), (dialogInterface, i) -> {
                    Toast.makeText(MainActivity.this, "Authentication canceled", Toast.LENGTH_SHORT).show();
                }).build();

        biometricPrompt.authenticate(getCancellationSignal(), getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(MainActivity.this, "Authentication successful", Toast.LENGTH_SHORT).show();
                // Authentication success, allow shutdown
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                // Authentication failed, shutdown refused
            }
        });
    }

    // Revoke authentication when necessary
    private CancellationSignal getCancellationSignal() {
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(() -> Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show());
        return cancellationSignal;
    }
}

// Running a service in the background to prevent shutdown

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Build;

public class ForegroundService extends Service {

    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Starting the service in the foreground
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Service is running")
                .setContentText("Monitoring for shutdown")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        startForeground(1, notification);

        // Do your job here (for example: monitor the shutdown button)
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}

// Add permissions in the AndroidManifest file.xml
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<application
    ...>
    <service
        android:name=".ForegroundService"
        android:foregroundServiceType="camera" />
</application>



Intent serviceIntent = new Intent(this, ForegroundService.class);
ContextCompat.startForegroundService(this, serviceIntent);
