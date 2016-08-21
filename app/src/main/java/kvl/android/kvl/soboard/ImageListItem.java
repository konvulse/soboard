package kvl.android.kvl.soboard;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by kvl on 8/19/16.
 */
public final class ImageListItem implements Parcelable {
    private Uri imageUri;
    private String imageName;
    private Bitmap imageBitmap;
    private Context context;
    private String flightNumber;
    private String airline;
    private String departureTime;

    private static final String LOG_TAG = "ImageListItem";

    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/soboard/";
    public static final String lang = "eng";

    public ImageListItem(Uri image, Context context) {
        this.context = context;
        imageUri = image;
        Cursor imageCursor = context.getContentResolver().query(imageUri, null, null, null, null);
        int nameIndex = imageCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        imageCursor.moveToFirst();
        imageName = imageCursor.getString(nameIndex);

        try {
            imageBitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        initializeOcr();
        performOcr();
    }

    public ImageListItem(Parcel in) {
        readFromParcel(in);
    }

    public String getName() {
        String name = imageName;
        if (flightNumber != "unknown") {
            name = "Flight " + flightNumber;
        }
        return name;
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

    private void initializeOcr() {
        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/"};

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(LOG_TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(LOG_TAG, "Created directory " + path + " on sdcard");
                }
            }
        }

        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {
                AssetManager assetManager = context.getAssets();
                String[] files = assetManager.list("");
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(LOG_TAG, "Copied " + lang + ".traineddata");
            } catch (IOException e) {
                Log.e(LOG_TAG, "Was unable to copy " + lang + ".traineddata " + e.toString());
            }
        }
    }

    private String findFlightNumber(String line) {
        String flightNumber = "unknown";
        String[] words = line.split(" ");
        if (words.length == 2) {
            flightNumber = words[1];
        }

        return flightNumber;
    }

    private String findDepartureTime(String line) {
        String departureTime = "unknown";
        String[] words = line.split(" ");
        if(words.length > 1) {
            departureTime = words[1] + " " + words[2];
        }

        return departureTime;
    }

    private void findInfo(String recognizedText) {
        String[] lines = recognizedText.split("\\r?\\n");
        for(String line : lines) {
            if(line.contains("Flight")) {
                flightNumber = findFlightNumber(line);
            } else if (line.contains("Depart")) {
                departureTime = findDepartureTime(line);
            }
        }

    }
    private void performOcr() {
        /*
        FOR NOW ASSUME THE IMAGE IS NOT ROTATED
        if(imageBitmap.getWidth() > imageBitmap.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), true);
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
        }
        */

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(imageBitmap);

        String recognizedText = baseApi.getUTF8Text();
        Log.v(LOG_TAG, recognizedText);

        findInfo(recognizedText);
    }
}
