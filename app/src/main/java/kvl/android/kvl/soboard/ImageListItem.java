package kvl.android.kvl.soboard;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kvl on 8/19/16.
 */
public final class ImageListItem implements Parcelable {
    private Uri imageUri;
    public ImageListItem(Uri image) {
        imageUri = image;
    }

    public ImageListItem(Parcel in) {
        readFromParcel(in);
    }

    public String getName() {
        return imageUri.getPath();
    }

    public Uri getUri() {
        return imageUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.imageUri, flags);
    }

    private void readFromParcel(Parcel in) {
        imageUri = in.readParcelable(null);
    }

    public static final Parcelable.Creator<ImageListItem> CREATOR = new Parcelable.Creator<ImageListItem>() {
        public ImageListItem createFromParcel(Parcel in) {
            return new ImageListItem(in);
        }

        public ImageListItem[] newArray(int size) {
            return new ImageListItem[size];
        }
    };
}
