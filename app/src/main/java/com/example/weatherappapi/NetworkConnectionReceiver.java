package com.example.weatherappapi;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.core.app.NotificationCompat;

public class NetworkConnectionReceiver extends BroadcastReceiver {
    private int messageId = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "3")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Attention")
                    .setContentText("No network connection");
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(messageId++, builder.build());
        }
    }
}
