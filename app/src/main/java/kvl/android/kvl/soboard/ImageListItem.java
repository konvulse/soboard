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
    private String flightNumber = "unknown";
    private String airline = "unknown";
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
        String name = "";
        if (!airline.equals("unknown")) {
            name += airline + " ";
        }
        if (!flightNumber.equals("unknown")) {
            name += "Flight " + flightNumber + " ";
        }
        if(name.isEmpty()) {
            name = imageName;
        }
        name = name.trim();
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

    private String findAirline(String[] lines) {
        for(String line : lines) {
            if (line.contains("Southwest")) {
                return "Southwest";
            } else if (line.contains("American")) {
                return "American";
            }
        }
        return "unknown";
    }

    private String findSouthwestFlightNumber(String line) {
        String flightNumber = "unknown";
        String[] words = line.split(" ");
        if (words.length == 2) {
            flightNumber = words[1];
        }

        return flightNumber;
    }

    private String findSouthwestDepartureTime(String line) {
        String departureTime = "unknown";
        String[] words = line.split(" ");
        if(words.length > 1) {
            departureTime = words[1] + " " + words[2];
        }

        return departureTime;
    }

    private void findSouthwestInfo(String[] lines) {
        for(String line : lines) {
            if(line.contains("Flight")) {
                flightNumber = findSouthwestFlightNumber(line);
            } else if (line.contains("Depart")) {
                departureTime = findSouthwestDepartureTime(line);
            }
        }
    }

    private String findAmericanFlightNumber(String keyLine, String valueLine) {
        String flightNumber = "unknown";
        String[] words = valueLine.split(" ");

        if(words.length >= 2) {
            flightNumber = words[1];
            if(flightNumber.contains("AA")) {
                flightNumber = flightNumber.substring(2);
            }
        }

        return flightNumber;
    }

    private String findAmericanDepartureTime(String line) {
        String departureTime = "unknown";
        String[] words = line.split(" ");
        if(words.length > 1) {
            departureTime = words[1];
        }

        return departureTime;
    }
    private void findAmericanInfo(String[] lines) {
        int lineIndex = 0;
        for(String line : lines) {
            if(line.contains("Flight")) {
                flightNumber = findAmericanFlightNumber(line, lines[lineIndex + 1]);
            } else if (line.contains("Depart")) {
                departureTime = findAmericanDepartureTime(line);
            }
            ++lineIndex;
        }
    }

    private void findFlightInfo(String recognizedText) {
        String[] lines = recognizedText.split("\\r?\\n");
        airline = findAirline(lines);
        if(airline.equals("Southwest")) {
            findSouthwestInfo(lines);
        } else if (airline.equals("American")) {
            findAmericanInfo(lines);
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

        findFlightInfo(recognizedText);
    }
}
