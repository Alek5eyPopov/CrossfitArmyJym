package com.crossfitarmyjym.app.ui.news;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageLoader {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ImageLoader() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void load(ImageView imageView, String imageUrl, @DrawableRes int placeholder) {
        imageView.setImageResource(placeholder);
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            return;
        }
        imageView.setTag(imageUrl);
        EXECUTOR.execute(() -> {
            Bitmap bitmap = download(imageUrl);
            MAIN.post(() -> {
                Object currentTag = imageView.getTag();
                if (imageUrl.equals(currentTag) && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            });
        });
    }

    private static Bitmap download(String imageUrl) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(12000);
            connection.connect();
            try (InputStream input = connection.getInputStream()) {
                return BitmapFactory.decodeStream(input);
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
