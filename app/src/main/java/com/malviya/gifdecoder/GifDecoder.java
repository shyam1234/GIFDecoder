package com.malviya.gifdecoder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Decode gif and extract the frames by using GifDrawable lib
 */
public class GifDecoder {
    private static final String TAG = GifDecoder.class.getSimpleName();
    public static final String FOLDER_NAME = "GIF_Decoder";
    private int TOTAL_FRAME = 0;
    private Context mContext;

    public GifDecoder(Context pContext) {
        mContext = pContext;
    }

    public void showExtractFrames(int pGifDrawable, ImageView pImageHolder, int pCol, int pScale) {
        try {
            GifDrawable gif = new GifDrawable(mContext.getResources(), pGifDrawable);
            int total_frame = gif.getNumberOfFrames();
            ArrayList<FrameHolder> list = new ArrayList<>();
            for (int index = 0; index < total_frame; index++) {
                list.add(new FrameHolder(gif.seekToFrameAndGet(index)));
            }
            FrameHolder b = createSprite(list, pCol, pScale);
            if (b != null) {
                pImageHolder.setImageBitmap(b.bitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "showExtractFrames " + e.getMessage());
        }
    }

    public void showExtractFrames(String pPath, ImageView pImageHolder, int pCol, int pScale) {
        try {
            GifDrawable gif = new GifDrawable(pPath);
            int total_frame = gif.getNumberOfFrames();
            ArrayList<FrameHolder> list = new ArrayList<>();
            for (int index = 0; index < total_frame; index++) {
                list.add(new FrameHolder(gif.seekToFrameAndGet(index)));
            }
            FrameHolder b = createSprite(list, pCol, pScale);
            if (b != null) {
                pImageHolder.setImageBitmap(b.bitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "showExtractFrames " + e.getMessage());
        }
    }


    public File getSavedExtractFramesImagePath(String pGifPath, int pCol, int pScale, String pSpriteName, ImageView pImageHolder) {
        File file = null;
        try {
            Log.d(TAG, "called getSavedExtractFramesImagePath " + pGifPath);
            GifDrawable gif = new GifDrawable(pGifPath);
            int total_frame = gif.getNumberOfFrames();
            Log.d(TAG, "called getSavedExtractFramesImagePath111  ");
            ArrayList<FrameHolder> list = new ArrayList<>();
            for (int index = 0; index < total_frame; index++) {
                list.add(new FrameHolder(gif.seekToFrameAndGet(index)));
            }
            FrameHolder b = createSprite(list, pCol, pScale);
            if (b != null) {
                File rootFile = new File(Environment.getExternalStorageDirectory(), "/" + FOLDER_NAME + "/");
                rootFile.mkdirs();
                file = new File(rootFile, pSpriteName + ".png");
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                b.bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                //framepath = file.getAbsolutePath();
                if (out != null) {
                    out.close();
                }
                pImageHolder.setImageBitmap(b.bitmap);
                Log.d(TAG, "frame path " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e(TAG, "showExtractFrames " + e.getMessage());
        }
        return file;
    }

    public void getSavedExtractFramesImagePath(String pGifPath, int pCol, int pScale, String pSpriteName, ImageView pImageHolder, IFramesSaved iCallBack) {
        new SaveFramesAsync(iCallBack, pImageHolder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pGifPath, "" + pCol, "" + pScale, "" + pSpriteName);
    }

    private FrameHolder createSprite(ArrayList<FrameHolder> pFrameHolder, int pCol, int pScale) {
        Log.d(TAG, "called createSprite: total frames: " + pFrameHolder.size());
        if (!pFrameHolder.isEmpty()) {
            TOTAL_FRAME = pFrameHolder.size();
            int frameW = (pFrameHolder.get(0).bitmap.getWidth()) / pScale;
            int frameH = (pFrameHolder.get(0).bitmap.getHeight()) / pScale;
            int row = ((TOTAL_FRAME / pCol) + ((TOTAL_FRAME % pCol) > 0 ? 1 : 0));
            int colW = pCol * frameW;
            int rowH = row * frameH;
            Bitmap bitmap = Bitmap.createBitmap(colW, rowH, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            for (int index = 0; index < TOTAL_FRAME; index++) {
                Bitmap bmp = Bitmap.createScaledBitmap(pFrameHolder.get(index).bitmap, frameW, frameH, false);
                canvas.drawBitmap(bmp, ((index % pCol) * bmp.getWidth()), ((index / pCol) * bmp.getHeight()), null);
            }
            return new FrameHolder(bitmap, frameW, frameH, TOTAL_FRAME, row, pCol);
        }
        return null;
    }

    public void saveSprite(final Bitmap b, final String pSpriteName, final ISaveCallback pCallback) throws IOException {
        new AsyncTask<String, Void, File>() {
            @Override
            protected File doInBackground(String... param) {
                File rootFile = new File(Environment.getExternalStorageDirectory(), "/" + FOLDER_NAME + "/");
                rootFile.mkdirs();
                File file = new File(rootFile, param[0] + ".png");
                try {
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    b.compress(Bitmap.CompressFormat.PNG, 100, out);
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "frame path " + file.getAbsolutePath());
                return file;
            }

            @Override
            protected void onPostExecute(File file) {
                //super.onPostExecute(file);
                pCallback.onSaved(file);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pSpriteName);
    }

    public void saveFrames(final Bitmap b, final String pSpriteName, final ISaveCallback pCallback) throws IOException {
        File rootFile = new File(Environment.getExternalStorageDirectory(), "/" + FOLDER_NAME + "/");
        rootFile.mkdirs();
        new AsyncTask<String, Void, File>() {
            @Override
            protected File doInBackground(String... param) {
                File rootFile = new File(Environment.getExternalStorageDirectory()+"/"+FOLDER_NAME, "/" + pSpriteName.split("_")[0] + "/");
                rootFile.mkdirs();
                File file = new File(rootFile, param[0] + ".png");
                try {
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    b.compress(Bitmap.CompressFormat.PNG, 100, out);
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "frame path " + file.getAbsolutePath());
                return file;
            }

            @Override
            protected void onPostExecute(File file) {
                //super.onPostExecute(file);
                pCallback.onSaved(file);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pSpriteName);
    }

    public void shareSprite(final Bitmap b, final String pDescription, final ISaveCallback pCallback) {
        new AsyncTask<Void, Void, Intent>() {

            @Override
            protected Intent doInBackground(Void... param) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/png");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), b, "Sprite", pDescription);
                Uri imageUri = Uri.parse(path);
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                return intent;

            }

            @Override
            protected void onPostExecute(Intent intent) {
                mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share_sprite)));
                pCallback.onSaved(null);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);

    }


    public interface IFramesSaved {
        void onSaved(FrameHolder holder);
    }


    protected interface ISaveCallback {
        void onSaved(File file);
    }

    protected class FrameHolder {
        public final Bitmap bitmap;
        public int frameW;
        public int frameH;
        public int total_frames;
        public int row;
        public int col;
        public ArrayList<FrameHolder> frameList;

        public FrameHolder(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public FrameHolder(Bitmap bitmap, int frameW, int frameH, int total_frames, int row, int col) {
            this.bitmap = bitmap;
            this.frameW = frameW;
            this.frameH = frameH;
            this.total_frames = total_frames;
            this.row = row;
            this.col = col;
        }

        public void setFrameList(ArrayList<FrameHolder> pList) {
            frameList = pList;
        }
    }

    private class SaveFramesAsync extends AsyncTask<String, Void, FrameHolder> {
        private final ImageView mImageHolder;
        private IFramesSaved callBack;

        public SaveFramesAsync(IFramesSaved iCallBack, ImageView pImageHolder) {
            callBack = iCallBack;
            mImageHolder = pImageHolder;
        }

        @Override
        protected FrameHolder doInBackground(String... param) {
            FrameHolder b = null;
            try {
                Log.d(TAG, "called SaveFramesAsync ");
                GifDrawable gif = new GifDrawable(param[0]);
                int total_frame = gif.getNumberOfFrames();
                ArrayList<FrameHolder> list = new ArrayList<>();
                for (int index = 0; index < total_frame; index++) {
                    list.add(new FrameHolder(gif.seekToFrameAndGet(index)));
                }
                b = createSprite(list, Integer.parseInt(param[1]), Integer.parseInt(param[2]));
                b.setFrameList(list);
            } catch (Exception e) {
                Log.e(TAG, "showExtractFrames doInBackground " + e.getMessage());
            }
            return b;
        }

        @Override
        protected void onPostExecute(FrameHolder b) {
            callBack.onSaved(b);
        }
    }
}