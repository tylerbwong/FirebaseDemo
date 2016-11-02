package services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import me.tylerbwong.firebasedemo.activities.MainActivity;

import static android.content.ContentValues.TAG;

/**
 * @author Tyler Wong
 */

public class CustomFirebaseMessagingService extends FirebaseMessagingService {
   @Override
   public void onMessageReceived(RemoteMessage remoteMessage) {
      // ...

      // TODO(developer): Handle FCM messages here.
      // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
      Log.d(TAG, "From: " + remoteMessage.getFrom());

      // Check if message contains a data payload.
      if (remoteMessage.getData().size() > 0) {
         Log.d(TAG, "Message data payload: " + remoteMessage.getData());
      }

      // Check if message contains a notification payload.
      if (remoteMessage.getNotification() != null) {
         Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
      }

      // Also if you intend on generating your own notifications as a result of a received FCM
      // message, here is where that should be initiated. See sendNotification method below.
      NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
            .setSmallIcon(android.R.mipmap.sym_def_app_icon)
            .setContentTitle(remoteMessage.getNotification().getTitle())
            .setContentText(remoteMessage.getNotification().getBody());
      Intent resultIntent = new Intent(this, MainActivity.class);
      TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
      stackBuilder.addParentStack(MainActivity.class);
      stackBuilder.addNextIntent(resultIntent);
      PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
      builder.setContentIntent(resultPendingIntent);
      NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.notify(0, builder.build());
   }
}
