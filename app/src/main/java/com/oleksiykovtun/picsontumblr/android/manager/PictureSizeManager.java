package com.oleksiykovtun.picsontumblr.android.manager;

import com.oleksiykovtun.picsontumblr.android.model.Picture;
import com.oleksiykovtun.picsontumblr.android.model.SizedPicture;

/**
 * Picks proper picture size
 */
public class PictureSizeManager {

    public static String getImageUrlForWidth(Picture picture, int desiredWidth) {
        if (picture != null && !picture.getSizedPictures().isEmpty()) {
            return getOptimalPhotoSizeForWidth(picture, desiredWidth).getUrl();
        } else {
            return null;
        }
    }

    public static int getPlaceholderHeightForWidth(Picture picture, double desiredWidth) {
        if (picture != null && !picture.getSizedPictures().isEmpty()) {
            SizedPicture photoSize = getOptimalPhotoSizeForWidth(picture, desiredWidth);
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

    private static SizedPicture getOptimalPhotoSizeForWidth(Picture picture, double desiredWidth) {
        // photo sizes should be tried starting from the smallest one
        for (int i = picture.getSizedPictures().size() - 1;  i >= 0; --i) {
            SizedPicture photoSize = picture.getSizedPictures().get(i);
            if (photoSize.getWidth() >= desiredWidth) {
                return photoSize;
            }
        }
        // if the biggest picture is smaller than requested and was not found, get the biggest one
        return picture.getSizedPictures().get(0);
    }

}
