package kvl.android.kvl.soboard;

import android.provider.BaseColumns;

/**
 * Created by kvl on 10/20/16.
 */
public class DatabaseSchema {

    public static final String DATABASE_NAME = "kvl.android.kvl.soboard.db";
    public static final int DATABASE_VERSION = 1;
    private DatabaseSchema() {};

    public static class TicketInfo implements BaseColumns {
        public static final String TABLE_NAME = "ticket_info";

        public static final String COLUMN_NAME_RAW_DATA = "raw_data";
        public static final String COLUMN_NAME_AIRLINE = "airline";
        public static final String COLUMN_NAME_IMAGE_URI = "image_uri";
        public static final String COLUMN_NAME_FLIGHT_NUMBER = "flight_number";
        public static final String COLUMN_NAME_DEPARTURE_TIME = "flight_number";
        public static final String COLUMN_NAME_USER_DEFINED_NAME = "user_defined_name";

        public static final String TABLE_CREATE =
                "CREATE TABLE" + DATABASE_NAME + "." + TABLE_NAME +
                        " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_IMAGE_URI + " TEXT," +
                        COLUMN_NAME_AIRLINE + " TEXT," +
                        COLUMN_NAME_FLIGHT_NUMBER + " TEXT," +
                        COLUMN_NAME_DEPARTURE_TIME + " TEXT," +
                        COLUMN_NAME_USER_DEFINED_NAME + " TEXT," +
                        COLUMN_NAME_RAW_DATA + " TEXT," +
                        ");";
    }
}
