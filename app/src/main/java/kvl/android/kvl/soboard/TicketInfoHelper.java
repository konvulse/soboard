package kvl.android.kvl.soboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kvl on 10/20/16.
 */
public class TicketInfoHelper extends SQLiteOpenHelper {

    TicketInfoHelper(Context context) {
        super(context, DatabaseSchema.DATABASE_NAME, null, DatabaseSchema.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseSchema.TicketInfo.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
