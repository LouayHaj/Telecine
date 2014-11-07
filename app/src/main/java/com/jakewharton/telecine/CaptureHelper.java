package com.jakewharton.telecine;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import timber.log.Timber;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;

final class CaptureHelper {
  private static final int CREATE_SCREEN_CAPTURE = 4242;

  private CaptureHelper() {
    throw new AssertionError("No instances.");
  }

  static void fireScreenCaptureIntent(Activity activity, Tracker tracker) {
    MediaProjectionManager manager =
        (MediaProjectionManager) activity.getSystemService(MEDIA_PROJECTION_SERVICE);
    Intent intent = manager.createScreenCaptureIntent();
    activity.startActivityForResult(intent, CREATE_SCREEN_CAPTURE);

    tracker.send(new HitBuilders.EventBuilder() //
        .setCategory(Analytics.CATEGORY_SETTINGS)
        .setAction(Analytics.ACTION_CAPTURE_INTENT_LAUNCH)
        .build());
  }

  static boolean handleActivityResult(Activity activity, int requestCode, int resultCode,
      Intent data, Tracker tracker) {
    if (requestCode != CREATE_SCREEN_CAPTURE) {
      return false;
    }

    if (resultCode == 0) {
      Timber.d("Failed to acquire permission to screen capture.");
    } else {
      Timber.d("Acquired permission to screen capture. Starting service.");
      activity.startService(TelecineService.newIntent(activity, resultCode, data));
    }

    tracker.send(new HitBuilders.EventBuilder() //
        .setCategory(Analytics.CATEGORY_SETTINGS)
        .setAction(Analytics.ACTION_CAPTURE_INTENT_RESULT)
        .setValue(resultCode)
        .build());

    return true;
  }
}
