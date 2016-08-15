package com.android.kvl.soboard;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
import android.widget.ListView;

public class WelcomeActivity extends AppCompatActivity {

    private static final String LOG_TAG = "WelcomeActivity";

    private static final int PICK_IMAGE = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_WRITE_SETTINGS = 3;

    ListView boardingPassListView;
    static final String BOARDING_PASS_EXTRA = "com.android.kvl.soboard.boarding_pass";

    //ArrayAdapter<> images;
    final Activity context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                    Log.d(LOG_TAG, "requesting permissions to write external storage");
                } else {
                    Log.d(LOG_TAG, "permission already granted");
                    getImage();
                }
            }
        });

        boardingPassListView = (ListView) findViewById(R.id.boardingPassListView);

        requestPermissionWriteSettings();
    }

    void requestPermissionWriteSettings() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!Settings.System.canWrite(context)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Log.d(LOG_TAG, "permission to write settings already granted");
            }
        } else {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SETTINGS)) {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_SETTINGS}, REQUEST_WRITE_SETTINGS);
                Log.d(LOG_TAG, "requesting permissions to write settings");
            } else {
                Log.d(LOG_TAG, "write settings permission already granted");
            }
        }
    }

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
            case REQUEST_WRITE_SETTINGS:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(LOG_TAG, "WRITE_SETTINGS permission granted");
                } else {
                    Log.d(LOG_TAG, "WRITE_SETTINGS permission denied");
                }
                //startBoardingPassActivity();
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

                //startBoardingPassActivity();
                Intent displayImage = new Intent(this, BoardingPassActivity.class);
                displayImage.putExtra(BOARDING_PASS_EXTRA, data);
                startActivity(displayImage);

            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
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
