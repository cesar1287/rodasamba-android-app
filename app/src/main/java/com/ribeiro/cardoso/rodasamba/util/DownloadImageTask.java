package com.ribeiro.cardoso.rodasamba.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by vinicius on 10/09/14.
 */
public class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {
    private final ImageView mImageView;
    private final String mImageUrl;

    public DownloadImageTask(ImageView imageView, String imageUrl) {
        this.mImageView = imageView;
        this.mImageUrl = imageUrl;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(this.mImageUrl).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mIcon11;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        mImageView.setImageBitmap(bitmap);
    }
}
