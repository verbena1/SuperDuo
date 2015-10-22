package it.jaschke.alexandria;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by dwtorres on 10/15/2015.
 * For alexandria
 */
public class BarcodeScannerActivity extends ActionBarActivity
        implements ZXingScannerView.ResultHandler {

    public static final int BARCODE_READER_REQUEST_CODE = 1;

    public static final String INTENT_EXTRA_PARAM_BARCODE_RESULT
            = "it.jaschke.alexandria.INTENT_PARAM_BARCODE_RESULT";

    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);
        setupFormats();
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        int cameraId = -1;
        mScannerView.startCamera(cameraId);
        mScannerView.setFlash(false);
        mScannerView.setAutoFocus(true);
    }

    @Override
    public void handleResult(Result rawResult) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent returnIntent = new Intent();
        if (rawResult != null) {
            returnIntent.putExtra(INTENT_EXTRA_PARAM_BARCODE_RESULT, rawResult.getText());
            setResult(RESULT_OK, returnIntent);
        } else {
            setResult(RESULT_CANCELED, returnIntent);
        }
        finish();
    }

    public void setupFormats() {
        if (mScannerView != null) {
            mScannerView.setFormats(ZXingScannerView.ALL_FORMATS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public static Intent getIntent(final Context context) {
        return new Intent(context, BarcodeScannerActivity.class);
    }
}
