package com.malviya.gifdecoder;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import pl.droidsonroids.gif.GifImageView;

import static com.malviya.gifdecoder.GifDecoder.FOLDER_NAME;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_WRITE = 112;
    private static final int COL_SIZE = 8;
    private static final int SCALE_SIZE = 6;
    private TextView mSave;
    private TextView mShare;
    private GifImageView mGifDecoderImageView;
    private TextView mChooseGif;
    private GifImageView mLoader;
    private Bitmap mBitmap;
    private long mFileName;
    private TextView mSpriteDetails;
    private LinearLayout mLinLayout;
    private GifImageView mGIFPreview;
    private String mPath;
    private TextView mSaveFrames;
    private ArrayList<GifDecoder.FrameHolder> mFrameList;
    private TextView mSupport;
    private InterstitialAd mInterstitialAd;
    private String mState;
    private TextView mTnC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        init();
        initInterstitialAd();
    }

    private void init() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        mFrameList = new ArrayList<GifDecoder.FrameHolder>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE);

        } else {
            //already granted
            initView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initView();
                } else {
                    finish();
                }
                return;
            }
        }
    }

    private void initView() {
        mSupport = (TextView) findViewById(R.id.view_support);
        mLinLayout = (LinearLayout) findViewById(R.id.linlayout);
        mGIFPreview = (GifImageView) findViewById(R.id.gif_preview);
        mSpriteDetails = (TextView) findViewById(R.id.textview_sprite_details);
        mSave = (TextView) findViewById(R.id.textview_save);
        mShare = (TextView) findViewById(R.id.textview_share);
        mGifDecoderImageView = (GifImageView) findViewById(R.id.gifimagebtn);
        mGifDecoderImageView.setImageResource(R.drawable.welcome);
        mChooseGif = (TextView) findViewById(R.id.textview_choose_gif);
        mLoader = (GifImageView) findViewById(R.id.loader);
        mSaveFrames = (TextView) findViewById(R.id.textview_save_frames);
        mTnC = (TextView) findViewById(R.id.textview_term_condition);
        //mGifDecoderImageView.setOnClickListener(this);
        mSave.setAlpha(0.5f);
        mSave.setEnabled(false);
        mSaveFrames.setAlpha(0.5f);
        mSaveFrames.setEnabled(false);
        mShare.setAlpha(0.5f);
        mShare.setEnabled(false);
        mSave.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mChooseGif.setOnClickListener(this);
        mSaveFrames.setOnClickListener(this);
        mSupport.setOnClickListener(this);
        mTnC.setOnClickListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        showAdvertice();
        final GifDecoder gifDecoder = new GifDecoder(MainActivity.this);
        switch (v.getId()) {
            case R.id.textview_save:
                try {
                    mLoader.setVisibility(View.VISIBLE);
                    gifDecoder.saveSprite(mBitmap, "" + mFileName, new GifDecoder.ISaveCallback() {
                        @Override
                        public void onSaved(File file) {
                            Toast.makeText(MainActivity.this, "Saved Sprite in " + FOLDER_NAME, Toast.LENGTH_LONG).show();
                            mSave.setAlpha(0.5f);
                            mSave.setEnabled(false);
                            mLoader.setVisibility(View.GONE);
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "textview_save onClick" + e.getMessage());
                }
                break;
            case R.id.textview_save_frames:
                try {
                    final int[] tempIndex = {};
                    mLoader.setVisibility(View.VISIBLE);
                    saveFrames(0, gifDecoder);
                    for (int index = 0; index < mFrameList.size(); index++) {
                        tempIndex[0] = index;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "textview_save_frames onClick" + e.getMessage());
                }
                break;
            case R.id.textview_term_condition:
                openPopupWebview(MainActivity.this,"file:///android_asset/termncondition.html");
                break;
            case R.id.textview_share:
                try {
                    openFolder();
//                    mLoader.setVisibility(View.VISIBLE);
//                    String description = mSpriteDetails.getText().toString();
//                    gifDecoder.shareSprite(mBitmap, (description != null ? description : getString(R.string.msg_like_this_app)), new GifDecoder.ISaveCallback() {
//                        @Override
//                        public void onSaved(File file) {
//                            mLoader.setVisibility(View.GONE);
//                        }
//                    });
                } catch (Exception e) {
                    Log.e(TAG, "textview_share onClick" + e.getMessage());
                }
                break;
            case R.id.view_support:
                openWhatsAppGroupChat();
                break;
            case R.id.textview_choose_gif:
                mFileName = System.currentTimeMillis();
                FileChooser fileChooser = new FileChooser(MainActivity.this);
                fileChooser.showFileChooser();
                break;
        }
    }


    private void openWhatsAppGroupChat() {
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://api.whatsapp.com/send?phone=918904188389"));
        startActivity(Intent.createChooser(intent, "Open WhatsApp"));
    }

    private void openFolder() {
        File file = new File(Environment.getExternalStorageDirectory(), FOLDER_NAME);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(Uri.fromFile(file), "*/*");
        startActivity(Intent.createChooser(intent, "Open folder"));
    }

    private void saveFrames(final int index, final GifDecoder gifDecoder) {
        try {
            if (index < mFrameList.size()) {
                gifDecoder.saveFrames(mFrameList.get(index).bitmap, "" + mFileName + "_" + index, new GifDecoder.ISaveCallback() {
                    @Override
                    public void onSaved(File file) {
                        mSaveFrames.setAlpha(0.5f);
                        mSaveFrames.setEnabled(false);
                        saveFrames(index + 1, gifDecoder);
                    }
                });
            } else {
                mLoader.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Saved All Frames in " + FOLDER_NAME, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "saveFrames" + e.getMessage());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FileChooser.FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    String path = null;
                    try {
                        path = FileChooser.getPathAdvance(this, uri);
                        Log.d(TAG, "getPathAdvance File Path: " + path);
                        mPath = path;
                        doExtractGIF(path);
                    } catch (Exception e) {
                        Log.e(TAG, "onActivityResult" + e.getMessage());
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doExtractGIF(String path) {
        if (path != null && !path.isEmpty()) {
            if (path.contains(".gif")) {
                final GifDecoder gifDecoder = new GifDecoder(MainActivity.this);
                mLoader.setVisibility(View.VISIBLE);
                gifDecoder.getSavedExtractFramesImagePath(path, COL_SIZE, SCALE_SIZE, "" + mFileName, mGifDecoderImageView, new GifDecoder.IFramesSaved() {
                    @Override
                    public void onSaved(GifDecoder.FrameHolder holder) {
                        try {
                            if (holder != null) {
                                mGifDecoderImageView.setImageBitmap(holder.bitmap);
                                mLoader.setVisibility(View.GONE);
                                mBitmap = holder.bitmap;
                                mFrameList = holder.frameList;
                                mSave.setAlpha(1f);
                                mSave.setEnabled(true);
                                mSaveFrames.setAlpha(1f);
                                mSaveFrames.setEnabled(true);
                                mShare.setAlpha(1f);
                                mShare.setEnabled(true);
                                showSpriteDetails(holder);
                            } else {
                                Toast.makeText(MainActivity.this, R.string.msg_smthing_went_wrong, Toast.LENGTH_LONG).show();
                            }
                            //gifDecoder.saveSprite(bitmap, "" + System.currentTimeMillis());
                        } catch (Exception e) {
                            Log.e(TAG, "doExtractGIF " + e.getMessage());
                        }
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, R.string.msg_validate_choose_only_gif, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, R.string.msg_validate_file_not_selected, Toast.LENGTH_LONG).show();
        }
    }

    private void showSpriteDetails(GifDecoder.FrameHolder pHolder) {
        mGIFPreview.setImageURI(Uri.fromFile(new File(mPath)));
        mSpriteDetails.setText(" GIF Width: " + pHolder.bitmap.getWidth()
                + "\n GIF Height: " + pHolder.bitmap.getHeight()
                + "\n Total Frames: " + pHolder.total_frames
                + "\n Frame Width: " + pHolder.frameW
                + "\n Frame Height: " + pHolder.frameH
                + "\n Total Row: " + pHolder.row
                + "\n Total Column: " + pHolder.col);
        mLinLayout.setVisibility(View.VISIBLE);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(MainActivity.this, R.string.exit_msg, Toast.LENGTH_LONG).show();
    }

    private void initInterstitialAd() {
        MobileAds.initialize(this, "ca-app-pub-3857992721987002~9411771658");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.string_ads));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mState = "onAdLoaded";
                // Code to be executed when an ad finishes loading.
                Log.d("test1234", "onAdLoaded");
                showAdvertice();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                mState = "onAdFailedToLoad";
                // Code to be executed when an ad request fails.
                // Code to be executed when when the interstitial ad is closed.
                Log.d("test1234", "onAdFailedToLoad");
                // handleMusicplayer(view);
            }

            @Override
            public void onAdOpened() {
                mState = "onAdOpened";
                // Code to be executed when the ad is displayed.
                Log.d("test1234", "onAdOpened");
            }

            @Override
            public void onAdLeftApplication() {
                mState = "onAdLeftApplication";
                // Code to be executed when the user has left the app.
                Log.d("test1234", "onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                mState = "onAdClosed";
                Log.d("test1234", "onAdClosed");
            }
        });

    }


    private void showAdvertice() {
        if (mState == null || (mState != null && mState.equalsIgnoreCase("onAdClosed") || mState.equalsIgnoreCase("onAdFailedToLoad"))) {
            initInterstitialAd();
        }

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        showAdvertice();
    }

    private void openPopupWebview(Context pContext, String pURL) {
        final Dialog dialog = new Dialog(pContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.webview_layout);
        dialog.show();
        WebView wv = (WebView) dialog
                .findViewById(R.id.webView);

        wv.loadUrl(pURL);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    dialog.dismiss();
                }

                return false;
            }
        });

    }

}
