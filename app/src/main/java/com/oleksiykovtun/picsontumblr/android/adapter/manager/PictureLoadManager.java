package com.oleksiykovtun.picsontumblr.android.adapter.manager;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.oleksiykovtun.picsontumblr.android.App;
import com.oleksiykovtun.picsontumblr.android.model.Picture;
import com.squareup.picasso.Picasso;
import com.tumblr.jumblr.types.PhotoSize;

/**
 * Picks proper picture size
 */
public class PictureLoadManager {

    private static final String EXTENSION_GIF = ".gif";

    public static void loadFromUrl(String urlString, ImageView targetImageView,
                                   Drawable placeholderDrawable) {
        if (isAnimated(urlString)) {
            Ion.with(targetImageView)
                    .placeholder(placeholderDrawable)
                    .load(urlString);
        } else {
            Picasso.with(App.getContext()).load(urlString)
                    .placeholder(placeholderDrawable)
                    .into(targetImageView);
        }
    }

    public static void loadFromUrl(String urlString, ImageView targetImageView) {
        Drawable placeholderDrawable = null;
        loadFromUrl(urlString, targetImageView, placeholderDrawable);
    }

    private static boolean isAnimated(String urlString) {
        return urlString.toLowerCase().endsWith(EXTENSION_GIF);
    }

}
