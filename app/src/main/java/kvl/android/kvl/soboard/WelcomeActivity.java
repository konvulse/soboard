package kvl.android.kvl.soboard;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {

    private static final String LOG_TAG = "WelcomeActivity";

    private static final int PICK_IMAGE = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    ListView boardingPassListView;
    static final String BOARDING_PASS_EXTRA = "kvl.android.kvl.soboard.boarding_pass";
    static final String SAVED_IMAGE_LIST = "kvl.android.kvl.soboard.savedImages";

    ImageListAdapter imageAdapter;
    final Activity context = this;

    SQLiteDatabase ticketDb;

    InterstitialAd interstitialAd;
    AdView adBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ticketDb = new TicketInfoHelper(context).getReadableDatabase();
        setContentView(R.layout.activity_welcome);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        MobileAds.initialize(context, getResources().getString(R.string.ad_mob_app_id));

        adBanner = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("A11960FBF8D4DAB9AFC3DE56A7D7C0D8")
                .build();
        adBanner.loadAd(adRequest);

        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(getResources().getString(R.string.banner_ad_unit_id));
        requestNewInterstitial();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (interstitialAd.isLoaded()) {
                    Log.v(LOG_TAG, "Displaying ad.");
                    interstitialAd.show();
                } else {
                    addNewTicket();
                }

                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        requestNewInterstitial();
                        addNewTicket();
                    }
                });

            }
        });

        imageAdapter = new ImageListAdapter(context, R.layout.image_list_item);
        boardingPassListView = (ListView) findViewById(R.id.boardingPassListView);
        boardingPassListView.setAdapter(imageAdapter);

        initializeImageListClickListener();
        initializeImageListLongClickListener();

        if(!(savedInstanceState == null || savedInstanceState.isEmpty())) {
            rebuildFromBundle(savedInstanceState);
        } else {
            rebuildFromDatabase();
        }
    }

    private void addNewTicket() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            Log.d(LOG_TAG, "requesting permissions to write external storage");
        } else {
            Log.d(LOG_TAG, "permission already granted");
            getImage();
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("A11960FBF8D4DAB9AFC3DE56A7D7C0D8")
                .build();

        interstitialAd.loadAd(adRequest);
    }

    private void rebuildFromDatabase() {
        Cursor tickets = ticketDb.query(DatabaseSchema.TicketInfo.TABLE_NAME, null, null, null, null, null, null, null);
        tickets.moveToFirst();
        while(!tickets.isAfterLast()) {
            try {
                ImageListItem item = new ImageListItem(context, tickets);
                imageAdapter.add(item);

            } catch (FileNotFoundException e) {
                Log.w(LOG_TAG, "Image no longer exists at the saved URI. The ticket will not be displayed. Ticket will be removed from database.");
                //TODO: Remove deleted tickets from database
                ticketDb.delete(DatabaseSchema.TicketInfo.TABLE_NAME, DatabaseSchema.TicketInfo._ID + " = " + tickets.getLong(tickets.getColumnIndex(DatabaseSchema.TicketInfo._ID)), null);
            }
            tickets.moveToNext();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(SAVED_IMAGE_LIST, imageAdapter.getArrayList());
    }

    private void rebuildFromBundle(Bundle state) {
        ArrayList<ImageListItem> savedImages = state.getParcelableArrayList(SAVED_IMAGE_LIST);
        for(ImageListItem image: savedImages) {
            imageAdapter.add(image);
        }
    }

    private void initializeImageListClickListener() {
        boardingPassListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //try {
                if (imageAdapter.isEditing()) {
                    return;
                }
                imageAdapter.stopEditing(boardingPassListView);
                Intent displayImage = new Intent(context, BoardingPassActivity.class);
                displayImage.putExtra(BOARDING_PASS_EXTRA, imageAdapter.getItem(position).getImageUri());
                startActivity(displayImage);
               /*} catch (FileNotFoundException e) {
                    imageAdapter.remove(imageAdapter.getItem(position));
                    Toast.makeText(context, "The ticket image has been moved or deleted and cannot be displayed.", Toast.LENGTH_SHORT);
                }*/

            }
        });
    }

    private void initializeImageListLongClickListener() {
        boardingPassListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (imageAdapter.isEditing()) {
                    return false;
                }
                boolean consumed = imageAdapter.makeEditable(parent, view, position, id);

                return consumed;
            }
        });
    }

    /*@Override
    public void onBackPressed() {
        if(imageAdapter.isEditing()) {
            imageAdapter.stopEditing(boardingPassListView);
        }
        super.onBackPressed();
    }*/

    void getImage() {
        Log.d(LOG_TAG, "The user will now select an image to view");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(LOG_TAG, "WRITE_EXTERNAL_STORAGE permission granted");
                    getImage();
                } else {
                    Log.d(LOG_TAG, "WRITE_EXTERNAL_STORAGE permission denied");
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        switch (requestCode) {
            case PICK_IMAGE:
                if(data == null) {
                    Log.d(LOG_TAG, "activity finished with no image selected");
                    return;
                }
                try {
                    ImageListItem newItem = new ImageListItem(data.getData(), context, imageAdapter);
                    imageAdapter.add(newItem);
                    Intent displayImage = new Intent(this, BoardingPassActivity.class);
                    displayImage.putExtra(BOARDING_PASS_EXTRA, newItem.getImageUri());
                    startActivity(displayImage);
                } catch (FileNotFoundException e) {
                    Log.e(LOG_TAG, "Chosen image does not exist, this can't happen");
                }
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
