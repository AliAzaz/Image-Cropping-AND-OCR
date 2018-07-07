package com.ali.azaz.ocr;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


public class MainActivity extends AppCompatActivity implements ConvImageTask.AsyncResponse {

    final int CAMERA_CAPTURE = 1;
    CropImageView imageView;
    Bitmap bitmap;
    TextView txtData;
    private String TAG = MainActivity.class.getName();

    String ImageUri = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnScan = findViewById(R.id.btnScan);
        txtData = findViewById(R.id.txtData);
        imageView = findViewById(R.id.imageView);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //use standard intent to capture an image
                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //we will handle the returned data in onActivityResult
                    startActivityForResult(captureIntent, CAMERA_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Whoops - your device doesn't support the crop action!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (resultCode) {
            case RESULT_CANCELED:
                Log.i(TAG, "User cancelled");
                break;

            case RESULT_OK:
                switch (requestCode) {
                    case CAMERA_CAPTURE:

//                      Copying image path
                        ImageUri = getRealPathFromURI(Uri.parse(data.getDataString()));

                        // start cropping activity for pre-acquired image saved on the device
                        CropImage.activity(data.getData())
                                .setBackgroundColor(Color.parseColor("#80FFFFA6"))
                                .setActivityTitle("Cropping Activity")
                                .start(this);
                        break;

                    case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        Uri resultImageUri = result.getUri();

                        Bitmap cropped = null;

                        // Also use in this manner
                        // cropped = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultImageUri);

                        // or In this manner
                        cropped = BitmapUtils.resamplePic(this, resultImageUri.getPath());

                        // execute the async task
                        new ConvImageTask(this).execute(cropped);

                        break;

                }
                break;
        }
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }


    // return the bitmap result after picture taking and cropping picture
    @Override
    public void processFinish(Bitmap mBitmap) {

        imageView.setImageBitmap(mBitmap);
        imageView.setShowCropOverlay(false);

        OCRClass ocr = new OCRClass(MainActivity.this);
        String s = ocr.processImage(mBitmap);
        txtData.setText(s);
    }

    @Override
    protected void onDestroy() {

        if (!ImageUri.equals("")) {
            BitmapUtils.deleteImageFile(this, ImageUri);
        }

        super.onDestroy();
    }
}
