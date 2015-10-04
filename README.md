# Pics on Tumblr

An Android app for viewing images on tumblr.com


## Build instructions

The following instructions require Java 1.7+, Gradle 2.2+ and Android SDK installed.

Get (clone or download ZIP) this repo and navigate to its root folder.

    cd Pics-on-Tumblr

Provide the valid OAuth Consumer Key `API_KEY` and Secret Key `API_SECRET` values
in `com.oleksiykovtun.picsontumblr.android.manager.AccountManager`.

Connect your Android device to ADB and run the command:

    gradlew installDebug

The app will be installed on your device.
