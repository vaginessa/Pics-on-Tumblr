package com.oleksiykovtun.picsontumblr.android.adapter.manager;

import android.util.Log;

import com.oleksiykovtun.picsontumblr.android.model.Picture;
import com.tumblr.jumblr.types.PhotoSize;

/**
 * Picks proper picture size
 */
public class PictureSizeManager {

    public static String getImageUrlForWidth(Picture picture, int desiredWidth) {
        if (picture != null && !picture.getPhotoSizes().isEmpty()) {
            return getOptimalPhotoSizeForWidth(picture, desiredWidth).getUrl();
        } else {
            return null;
        }
    }

    public static int getPlaceholderHeightForWidth(Picture picture, double desiredWidth) {
        if (picture != null && !picture.getPhotoSizes().isEmpty()) {
            PhotoSize photoSize = getOptimalPhotoSizeForWidth(picture, desiredWidth);
            if (photoSize.getWidth() >= desiredWidth) {
                double scale = photoSize.getWidth() / desiredWidth;
                return (int) (photoSize.getHeight() / scale);
            } else {
                return photoSize.getHeight();
            }
        } else {
            return 0;
        }
    }

    private static PhotoSize getOptimalPhotoSizeForWidth(Picture picture, double desiredWidth) {
        // photo sizes should be tried starting from the smallest one
        for (int i = picture.getPhotoSizes().size() - 1;  i >= 0; --i) {
            PhotoSize photoSize = picture.getPhotoSizes().get(i);
            if (photoSize.getWidth() >= desiredWidth) {
                return photoSize;
            }
        }
        // if the biggest picture is smaller than requested and was not found, get the biggest one
        return picture.getPhotoSizes().get(0);
    }

}
