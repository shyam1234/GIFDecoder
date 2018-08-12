package com.malviya.gifdecoder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_WRITE = 112;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        init();
    }

    private void init() {
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
        mLinLayout = (LinearLayout) findViewById(R.id.linlayout);
        mGIFPreview = (GifImageView) findViewById(R.id.gif_preview);
        mSpriteDetails = (TextView) findViewById(R.id.textview_sprite_details);
        mSave = (TextView) findViewById(R.id.textview_save);
        mShare = (TextView) findViewById(R.id.textview_share);
        mGifDecoderImageView = (GifImageView) findViewById(R.id.gifimagebtn);
        mGifDecoderImageView.setImageResource(R.drawable.welcome);
        mChooseGif = (TextView) findViewById(R.id.textview_choose_gif);
        mLoader = (GifImageView) findViewById(R.id.loader);
        //mGifDecoderImageView.setOnClickListener(this);
        mSave.setAlpha(0.5f);
        mSave.setEnabled(false);
        mShare.setAlpha(0.5f);
        mShare.setEnabled(false);
        mSave.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mChooseGif.setOnClickListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        final GifDecoder gifDecoder = new GifDecoder(MainActivity.this);
        switch (v.getId()) {
            case R.id.textview_save:
                try {
                    mLoader.setVisibility(View.VISIBLE);
                    gifDecoder.saveSprite(mBitmap, "" + mFileName, new GifDecoder.ISaveCallback() {
                        @Override
                        public void onSaved(File file) {
                            Toast.makeText(MainActivity.this, "Saved Sprite", Toast.LENGTH_LONG).show();
                            mSave.setAlpha(0.5f);
                            mSave.setEnabled(false);
                            mLoader.setVisibility(View.GONE);
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "textview_save onClick" + e.getMessage());
                }
                break;
            case R.id.textview_share:
                try {
                    mLoader.setVisibility(View.VISIBLE);
                    String description = mSpriteDetails.getText().toString();
                    gifDecoder.shareSprite(mBitmap, (description != null ? description : getString(R.string.msg_like_this_app)), new GifDecoder.ISaveCallback() {
                        @Override
                        public void onSaved(File file) {
                            mLoader.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "textview_share onClick" + e.getMessage());
                }
                break;
            case R.id.textview_choose_gif:

                mFileName = System.currentTimeMillis();
                FileChooser fileChooser = new FileChooser(MainActivity.this);
                fileChooser.showFileChooser();
                break;
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
                gifDecoder.getSavedExtractFramesImagePath(path, 8, 6, "" + mFileName, mGifDecoderImageView, new GifDecoder.IFramesSaved() {
                    @Override
                    public void onSaved(GifDecoder.FrameHolder holder) {
                        try {
                            if (holder != null) {
                                mGifDecoderImageView.setImageBitmap(holder.bitmap);
                                mLoader.setVisibility(View.GONE);
                                mBitmap = holder.bitmap;
                                mSave.setAlpha(1f);
                                mSave.setEnabled(true);
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
}
