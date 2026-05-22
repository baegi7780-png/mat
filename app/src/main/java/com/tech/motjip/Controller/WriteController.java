package com.tech.motjip.Controller;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.exifinterface.media.ExifInterface;

import com.tech.motjip.API.ApiService;
import com.tech.motjip.API.RetrofitClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Callback;
import retrofit2.Response;

public class WriteController {

    private final Context context;

    private final ApiService apiService;

    public WriteController(Context context) {

        this.context = context;

        this.apiService =
                RetrofitClient.getApiService(context);
    }

    public void uploadCommunityPost(
            String tag,
            String region,
            String title,
            String location,
            String date,
            String content,
            Uri imageUri,
            Callback<Void> callback
    ) {

        RequestBody tagBody =
                createTextRequestBody(tag);

        RequestBody regionBody =
                createTextRequestBody(region);

        RequestBody titleBody =
                createTextRequestBody(title);

        RequestBody locationBody =
                createTextRequestBody(location);

        RequestBody dateBody =
                createTextRequestBody(date);

        RequestBody contentBody =
                createTextRequestBody(content);

        MultipartBody.Part imagePart = null;

        if (imageUri != null) {

            imagePart =
                    createCompressedImagePart(imageUri);
        }

        apiService.createCommunityPost(
                tagBody,
                regionBody,
                titleBody,
                locationBody,
                dateBody,
                contentBody,
                imagePart
        ).enqueue(callback);
    }

    public void updateCommunityPost(
            Long comId,
            String tag,
            String region,
            String title,
            String location,
            String date,
            String content,
            Uri imageUri,
            Callback<Void> callback
    ) {

        RequestBody tagBody =
                createTextRequestBody(tag);

        RequestBody regionBody =
                createTextRequestBody(region);

        RequestBody titleBody =
                createTextRequestBody(title);

        RequestBody locationBody =
                createTextRequestBody(location);

        RequestBody dateBody =
                createTextRequestBody(date);

        RequestBody contentBody =
                createTextRequestBody(content);

        MultipartBody.Part imagePart = null;

        if (imageUri != null) {

            imagePart =
                    createCompressedImagePart(imageUri);
        }

        apiService.updateCommunityPost(
                comId,
                tagBody,
                regionBody,
                titleBody,
                locationBody,
                dateBody,
                contentBody,
                imagePart
        ).enqueue(callback);
    }

    private RequestBody createTextRequestBody(
            String value
    ) {

        return RequestBody.create(
                value,
                MediaType.parse("text/plain")
        );
    }

    private MultipartBody.Part createCompressedImagePart(
            Uri imageUri
    ) {

        try {

            InputStream inputStream =
                    context.getContentResolver()
                            .openInputStream(imageUri);

            if (inputStream == null) {
                return null;
            }

            Bitmap originalBitmap =
                    BitmapFactory.decodeStream(inputStream);

            inputStream.close();

            if (originalBitmap == null) {
                return null;
            }

            originalBitmap =
                    rotateImageIfRequired(
                            originalBitmap,
                            imageUri
                    );

            int targetWidth = 1080;

            int width =
                    originalBitmap.getWidth();

            int height =
                    originalBitmap.getHeight();

            if (width > targetWidth) {

                int targetHeight =
                        (height * targetWidth) / width;

                originalBitmap =
                        Bitmap.createScaledBitmap(
                                originalBitmap,
                                targetWidth,
                                targetHeight,
                                true
                        );
            }

            String fileName =
                    getFileName(imageUri);

            if (fileName == null
                    || fileName.trim().isEmpty()) {

                fileName =
                        "community_image.jpg";
            }

            File compressedFile =
                    new File(
                            context.getCacheDir(),
                            "compressed_" + fileName
                    );

            FileOutputStream outputStream =
                    new FileOutputStream(compressedFile);

            originalBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    70,
                    outputStream
            );

            outputStream.flush();
            outputStream.close();

            RequestBody requestFile =
                    RequestBody.create(
                            compressedFile,
                            MediaType.parse("image/jpeg")
                    );

            return MultipartBody.Part.createFormData(
                    "image",
                    compressedFile.getName(),
                    requestFile
            );

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }

    private Bitmap rotateImageIfRequired(
            Bitmap bitmap,
            Uri imageUri
    ) {

        try {

            InputStream inputStream =
                    context.getContentResolver()
                            .openInputStream(imageUri);

            if (inputStream == null) {
                return bitmap;
            }

            ExifInterface exifInterface =
                    new ExifInterface(inputStream);

            int orientation =
                    exifInterface.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                    );

            inputStream.close();

            Matrix matrix =
                    new Matrix();

            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;

                default:
                    return bitmap;
            }

            return Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true
            );

        } catch (Exception e) {

            e.printStackTrace();

            return bitmap;
        }
    }

    public String getFileName(
            Uri uri
    ) {

        String result =
                "선택된 이미지";

        Cursor cursor =
                context.getContentResolver().query(
                        uri,
                        null,
                        null,
                        null,
                        null
                );

        if (cursor != null) {

            try {

                int nameIndex =
                        cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME
                        );

                if (nameIndex >= 0
                        && cursor.moveToFirst()) {

                    result =
                            cursor.getString(nameIndex);
                }

            } finally {

                cursor.close();
            }
        }

        return result;
    }

    public boolean isImageSizeValid(
            Uri imageUri,
            int maxSizeMb
    ) {

        try {

            AssetFileDescriptor assetFileDescriptor =
                    context.getContentResolver()
                            .openAssetFileDescriptor(
                                    imageUri,
                                    "r"
                            );

            if (assetFileDescriptor == null) {
                return false;
            }

            long fileSize =
                    assetFileDescriptor.getLength();

            assetFileDescriptor.close();

            long maxBytes =
                    (long) maxSizeMb * 1024 * 1024;

            return fileSize > 0 && fileSize <= maxBytes;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    public String validateCommunityPost(
            String title,
            String location,
            String date,
            String content
    ) {

        if (title == null || title.trim().isEmpty()) {
            return "제목을 입력해 주세요.";
        }

        if (title.trim().length() > 25) {
            return "제목은 최대 25자까지 입력 가능합니다.";
        }

        if (location == null || location.trim().isEmpty()) {
            return "위치를 입력해 주세요.";
        }

        if (date == null || date.trim().isEmpty()) {
            return "날짜를 선택해 주세요.";
        }

        if (content == null || content.trim().isEmpty()) {
            return "내용을 입력해 주세요.";
        }

        if (content.trim().length() > 500) {
            return "내용은 최대 500자까지 입력 가능합니다.";
        }

        return null;
    }

    public String convertDateForServer(String dateText) {

        if (dateText == null || dateText.trim().isEmpty()) {
            return "";
        }

        dateText = dateText.trim();

        if (dateText.contains("-")) {
            return dateText;
        }

        try {

            String[] parts =
                    dateText.split(" ");

            String datePart =
                    parts[0];

            String amPm =
                    parts[1];

            String timePart =
                    parts[2];

            String[] dateParts =
                    datePart.split("\\.");

            String year =
                    dateParts[0];

            String month =
                    String.format(
                            "%02d",
                            Integer.parseInt(dateParts[1])
                    );

            String day =
                    String.format(
                            "%02d",
                            Integer.parseInt(dateParts[2])
                    );

            String[] timeParts =
                    timePart.split(":");

            int hour =
                    Integer.parseInt(timeParts[0]);

            int minute =
                    Integer.parseInt(timeParts[1]);

            if (amPm.equals("오후") && hour < 12) {
                hour += 12;
            }

            if (amPm.equals("오전") && hour == 12) {
                hour = 0;
            }

            return year
                    + "-"
                    + month
                    + "-"
                    + day
                    + " "
                    + String.format("%02d", hour)
                    + ":"
                    + String.format("%02d", minute);

        } catch (Exception e) {

            e.printStackTrace();

            return dateText;
        }
    }

    public boolean hasInputData(
            String title,
            String location,
            String date,
            String content,
            Uri selectedImageUri
    ) {

        return title != null && !title.trim().isEmpty()
                || location != null && !location.trim().isEmpty()
                || date != null && !date.trim().isEmpty()
                || content != null && !content.trim().isEmpty()
                || selectedImageUri != null;
    }

    public String getErrorMessage(
            Response<Void> response,
            String defaultMessage
    ) {

        try {

            if (response.errorBody() != null) {

                return response.errorBody().string();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return defaultMessage;
    }
}