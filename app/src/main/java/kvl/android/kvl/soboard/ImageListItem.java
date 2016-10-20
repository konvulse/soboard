package kvl.android.kvl.soboard;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kvl on 8/19/16.
 */
public final class ImageListItem implements Parcelable {
    private Uri imageUri;

    private Context context;
    private String userDefinedName = null;

    private static final String LOG_TAG = "ImageListItem";

    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/soboard/";
    public static final String lang = "eng";

    private TicketParser ticketInfo;

    public ImageListItem(Uri image, Context context, ImageListAdapter listAdapter) {
        this.context = context;
        this.imageUri = image;

        ticketInfo = new TicketParser(this.context, this.imageUri, listAdapter);
        ticketInfo.execute();
    }

    public ImageListItem(Parcel in) {
        readFromParcel(in);
    }

    public void setName(String name) {
        if(name != null && !name.isEmpty()) {
            userDefinedName = name.trim();
        } else {
            userDefinedName = null;
        }
    }

    public String getName() {
        String name = "";
        if (userDefinedName != null) {
            name = userDefinedName;
        }
        else {
            if (!ticketInfo.getFlightNumber().equals("unknown")) {
                name += ticketInfo.getAirline() + " ";
            }
            if (!ticketInfo.getFlightNumber().equals("unknown")) {
                name += "Flight " + ticketInfo.getFlightNumber() + " ";
            }
            if (name.isEmpty()) {
                name = ticketInfo.getImageName();
            }
            name = name.trim();
        }
        return name;
    }

    @Override
    public String toString() {
        return this.getName();
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
