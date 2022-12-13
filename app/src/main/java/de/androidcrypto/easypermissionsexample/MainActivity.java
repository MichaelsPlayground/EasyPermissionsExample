package de.androidcrypto.easypermissionsexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {

    private static final String TAG = "EasyPermissionExample";

    Button openGallery;
    TextView uncroppedFileUri, fileUri;
    de.hdodenhof.circleimageview.CircleImageView profileImage;

    private static int START_GALLERY_REQUEST = 1;

    // used for EasyPermissions
    private static final String[] READ_EXTERNAL_STORAGE_PERMISSION = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final String[] READ_MEDIA_IMAGES_PERMISSION = {Manifest.permission.READ_MEDIA_IMAGES};
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERM = 123;
    private static final int REQUEST_CODE_READ_MEDIA_IMAGES_PERM = 124;

    /*
    Starting from Target 13, you must request one or more of the following granular media permissions
    instead of the READ_EXTERNAL_STORAGE permission: READ_MEDIA_IMAGES for accessing images.
    READ_MEDIA_VIDEO for accessing videos. READ_MEDIA_AUDIO for accessing audio files.
    https://levelup.gitconnected.com/read-external-storage-permission-is-deprecated-heres-the-new-way-to-access-android-storage-8ce0644e9955
     */

    /**
     * code taken from https://github.com/googlesamples/easypermissions
     * description: 
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openGallery = findViewById(R.id.btnOpenGallery);
        uncroppedFileUri = findViewById(R.id.tvUncroppedFileUri);
        fileUri = findViewById(R.id.tvFileUri);
        profileImage = findViewById(R.id.ivProfileImage);

        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    readMediaImagesTask();
                } else {
                    readExternalStorageTask();
                }
            }
        });


    }

    /**
     * permission check with EasyPermissions
     */

    private boolean hasReadExternalStoragePermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private boolean hasReadMediaImagesPermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.READ_MEDIA_IMAGES);
    }

    @AfterPermissionGranted(REQUEST_CODE_READ_MEDIA_IMAGES_PERM)
    public void readMediaImagesTask() {
        if (hasReadMediaImagesPermission()) {
            // Have permission, do the thing!
            Toast.makeText(this, "TODO: read media images things", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "checkPermissions -> granted");
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, START_GALLERY_REQUEST);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs read access to your storage so you can select a profile image",
                    REQUEST_CODE_READ_MEDIA_IMAGES_PERM,
                    Manifest.permission.READ_MEDIA_IMAGES);
        }
    }

    @AfterPermissionGranted(REQUEST_CODE_READ_EXTERNAL_STORAGE_PERM)
    public void readExternalStorageTask() {
        if (hasReadExternalStoragePermission()) {
            // Have permission, do the thing!
            Toast.makeText(this, "TODO: read external storage things", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "checkPermissions -> granted");
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, START_GALLERY_REQUEST);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs read access to your storage so you can select a profile image",
                    REQUEST_CODE_READ_EXTERNAL_STORAGE_PERM,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // permission activity results
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            String yes = "Yes";
            String no = "No";

            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(
                            this,
                            getString(R.string.returned_from_app_settings_to_activity,
                                    hasReadExternalStoragePermission() ? yes : no),
                            Toast.LENGTH_LONG)
                    .show();
        }
        // now the requests for cropping
        if (requestCode == START_GALLERY_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                uncroppedFileUri.setText("uncropped file uri: " + data.getData());
                resizeImage(data.getData());
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //profileImage.setImageURI(result.getUri());
            showImage(result.getUri());
        }
    }

    @Override
    public void onRationaleAccepted(int requestCode) {
        Log.i(TAG, "onRationaleAccepted:" + requestCode);
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        Log.i(TAG, "onRationaleDenied:" + requestCode);
    }

    /**
     * cropping service
     */

    private void resizeImage(Uri data) {
        CropImage.activity(data)
                .setMultiTouchEnabled(true)
                .setAspectRatio(1 , 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                //.setMaxCropResultSize(512, 512)
                //.setOutputCompressQuality(50)
                .start(this);
    }

    private void showImage(Uri uri) {
        fileUri.setText("fileUri: " + uri);
        Glide.with(this).load(uri).into(profileImage);
    }
}