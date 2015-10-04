package com.oleksiykovtun.picsontumblr.android.manager;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.oleksiykovtun.picsontumblr.android.util.SettingsUtil;
import com.oleksiykovtun.picsontumblr.android.view.AuthorizationActivity;
import com.oleksiykovtun.picsontumblr.android.view.MainActivity;
import com.tumblr.jumblr.JumblrClient;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TumblrApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

/**
 * tumblr.com API and user account manager
 */
public class AccountManager {

    public static final String TAG_VERIFIER = "VERIFIER";

    private static final String API_KEY = "(insert OAuth Consumer Key for your app here)";

    private static final String API_SECRET = "(insert Secret Key for your app here)";

    private static final String TAG_USER_KEY = "USER_KEY";
    private static final String TAG_USER_SECRET = "USER_SECRET";

    private static Token authenticationRequestToken = null;

    private static OAuthService getAuthenticationService() {
        return new ServiceBuilder().provider(TumblrApi.class).apiKey(API_KEY).apiSecret(API_SECRET)
                .callback("http://www.tumblr.com/connect/login_success.html").build();
    }

    public static void startAuthorization() {
        new Thread() {
            public void run() {
                authenticationRequestToken = getAuthenticationService().getRequestToken();
                String requestUrl =
                        getAuthenticationService().getAuthorizationUrl(authenticationRequestToken);
                Log.d("", "Auth token: " + authenticationRequestToken.getToken());
                AuthorizationActivity.setAuthorizationUrl(requestUrl);
                Intent intent = new Intent(MainActivity.get(), AuthorizationActivity.class);
                MainActivity.get().startActivityForResult(intent, AuthorizationActivity.CODE);
            }
        }.start();
    }

    public static void finishAuthorization(final String verifierString) {
        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                Token accessToken = getAuthenticationService().
                        getAccessToken(authenticationRequestToken, new Verifier(verifierString));
                SettingsUtil.writePreferences(TAG_USER_KEY, accessToken.getToken());
                SettingsUtil.writePreferences(TAG_USER_SECRET, accessToken.getSecret());
                return "";
            }

            @Override
            protected void onPostExecute(String result) {
                MainActivity.get().loadPictureAlbumInNewPage("dashboard", false);
            }
        }.execute();
    }

    public static void revokeAuthorization() {
        SettingsUtil.writePreferences(TAG_USER_KEY, "");
        SettingsUtil.writePreferences(TAG_USER_SECRET, "");
        authenticationRequestToken = null;
    }

    public static boolean isClientAuthorized() {
        return !SettingsUtil.readPreferences(TAG_USER_KEY, "").isEmpty();
    }

    public static JumblrClient getAccountClient() {
        JumblrClient client = new JumblrClient(API_KEY, API_SECRET);
        client.setToken(SettingsUtil.readPreferences(TAG_USER_KEY, ""),
                SettingsUtil.readPreferences(TAG_USER_SECRET, ""));
        return client;
    }

}
