package com.crossfitarmyjym.app.data.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.crossfitarmyjym.app.data.SupabaseConfig;
import com.crossfitarmyjym.app.data.api.AuthInterceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StorageRepository {

    private static final String NEWS_BUCKET = "news-images";
    private static final long MAX_IMAGE_BYTES = 5L * 1024L * 1024L;

    private final Context context;
    private final OkHttpClient client;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface UploadCallback {
        void onSuccess(String publicUrl);
        void onError(@NonNull String error);
    }

    public StorageRepository(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();
    }

    public void uploadNewsImage(@NonNull Uri imageUri, @NonNull UploadCallback callback) {
        executor.execute(() -> {
            try {
                String mimeType = mimeType(imageUri);
                byte[] bytes = readBytes(imageUri);
                if (bytes.length > MAX_IMAGE_BYTES) {
                    callback.onError("Картинка больше 5 МБ");
                    return;
                }
                String objectPath = "posts/" + UUID.randomUUID() + extension(mimeType);
                String uploadUrl = storageBaseUrl() + "/object/" + NEWS_BUCKET + "/" + objectPath;
                RequestBody body = RequestBody.create(bytes, MediaType.parse(mimeType));
                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .put(body)
                        .header("Content-Type", mimeType)
                        .header("x-upsert", "true")
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        callback.onSuccess(publicUrl(objectPath));
                    } else {
                        callback.onError("Не удалось загрузить картинку: HTTP " + response.code());
                    }
                }
            } catch (IOException | RuntimeException error) {
                callback.onError("Ошибка загрузки картинки: " + error.getMessage());
            }
        });
    }

    private byte[] readBytes(Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        try (InputStream input = resolver.openInputStream(uri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) {
                throw new IOException("Файл не найден");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
                if (output.size() > MAX_IMAGE_BYTES) {
                    break;
                }
            }
            return output.toByteArray();
        }
    }

    private String mimeType(Uri uri) {
        String type = context.getContentResolver().getType(uri);
        if (type == null || type.trim().isEmpty()) {
            type = "image/jpeg";
        }
        return type;
    }

    private String extension(@Nullable String mimeType) {
        String extension = mimeType == null ? null
                : MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if (extension == null || extension.trim().isEmpty()) {
            extension = "jpg";
        }
        return "." + extension.toLowerCase(Locale.US);
    }

    private String publicUrl(String objectPath) {
        return storageBaseUrl() + "/object/public/" + NEWS_BUCKET + "/" + objectPath;
    }

    private String storageBaseUrl() {
        return SupabaseConfig.getSupabaseUrl()
                .replace("/rest/v1/", "/storage/v1/")
                .replace("/rest/v1", "/storage/v1");
    }
}
