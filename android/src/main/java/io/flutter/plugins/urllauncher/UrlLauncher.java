package io.flutter.plugins.urllauncher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import androidx.annotation.Nullable;

/** Launches components for URLs. */
class UrlLauncher {
  private final Context applicationContext;
  @Nullable private Activity activity;

  /**
   * Uses the given {@code applicationContext} for launching intents.
   *
   * <p>It may be null initially, but should be set before calling {@link #launch}.
   */
  UrlLauncher(Context applicationContext, @Nullable Activity activity) {
    this.applicationContext = applicationContext;
    this.activity = activity;
  }

  void setActivity(@Nullable Activity activity) {
    this.activity = activity;
  }

  /** Returns whether the given {@code url} resolves into an existing component. */
  boolean canLaunch(String url) {
    Intent launchIntent = new Intent(Intent.ACTION_VIEW);
    launchIntent.setData(Uri.parse(url));
    ComponentName componentName =
        launchIntent.resolveActivity(applicationContext.getPackageManager());

    return componentName != null
        && !"{com.android.fallback/com.android.fallback.Fallback}"
            .equals(componentName.toShortString());
  }

  /**
   * Attempts to launch the given {@code url}.
   *
   * @param headersBundle forwarded to the intent as {@code Browser.EXTRA_HEADERS}.
   * @param useWebView when true, the URL is launched inside of {@link WebViewActivity}.
   * @param enableJavaScript Only used if {@param useWebView} is true. Enables JS in the WebView.
   * @param enableDomStorage Only used if {@param useWebView} is true. Enables DOM storage in the
   * @return {@link LaunchStatus#NO_ACTIVITY} if there's no available {@code applicationContext}.
   *     {@link LaunchStatus#OK} otherwise.
   */
  LaunchStatus launch(
      String url,
      Bundle headersBundle,
      boolean useWebView,
      boolean enableJavaScript,
      boolean enableDomStorage) {
    if (activity == null) {
      return LaunchStatus.NO_ACTIVITY;
    }

    Intent launchIntent;
    if (useWebView) {
      launchIntent =
          WebViewActivity.createIntent(
              activity, url, enableJavaScript, enableDomStorage, headersBundle);
    } else {
      launchIntent =
          new Intent(Intent.ACTION_VIEW)
              .setData(Uri.parse(url))
              .putExtra(Browser.EXTRA_HEADERS, headersBundle);
    }

    activity.startActivity(launchIntent);
    return LaunchStatus.OK;
  }

  /** Closes any activities started with {@link #launch} {@code useWebView=true}. */
  void closeWebView() {
    applicationContext.sendBroadcast(new Intent(WebViewActivity.ACTION_CLOSE));
  }

  /** Result of a {@link #launch} call. */
  enum LaunchStatus {
    /** The intent was well formed. */
    OK,
    /** No activity was found to launch. */
    NO_ACTIVITY,
  }
}
