package kvl.android.kvl.soboard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.FileNotFoundException;

public class BoardingPassActivity extends AppCompatActivity {
    private static final String LOG_TAG = "BoardingPassActivity";

    @Override
    public void onBackPressed() {
        if (interstitialAd.isLoaded()) {
            Log.v(LOG_TAG, "Displaying ad.");
            if(!BuildConfig.DEBUG) {
                interstitialAd.show();
            }
        }

        super.onBackPressed();
    }

    Bitmap boardingPass;
    ImageView imageView;
    Activity context = this;

    InterstitialAd interstitialAd;


    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("A11960FBF8D4DAB9AFC3DE56A7D7C0D8")
                .build();

        interstitialAd.loadAd(adRequest);
    }

    void adjustBrightness() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        getWindow().setAttributes(lp);
    }

    void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideStatusBar();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId("ca-app-pub-1470341388733034/9951540908");
        requestNewInterstitial();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        adjustBrightness();

        setContentView(R.layout.activity_boarding_pass);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent input = getIntent();
        Uri imageUri = input.getParcelableExtra(WelcomeActivity.BOARDING_PASS_EXTRA);

        imageView = (ImageView) findViewById(R.id.boardingPassImageView);

        try {
            boardingPass = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(imageUri));
            if(boardingPass.getWidth() > boardingPass.getHeight()) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                boardingPass = Bitmap.createScaledBitmap(boardingPass,boardingPass.getWidth(),boardingPass.getHeight(),true);
                boardingPass = Bitmap.createBitmap(boardingPass, 0, 0, boardingPass.getWidth(), boardingPass.getHeight(), matrix, true);
            }
            imageView.setImageBitmap(boardingPass);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Image not found.");
            //setResult(Activity.RESULT_CANCELED);
            //finish();
        }

        imageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hide();
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }
}
