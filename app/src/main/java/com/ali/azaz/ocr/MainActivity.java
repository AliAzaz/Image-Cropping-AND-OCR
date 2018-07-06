package com.ali.azaz.ocr;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements ConvImageTask.AsyncResponse {

    final int CAMERA_CAPTURE = 1;
    CropImageView imageView;
    Bitmap bitmap;
    TextView txtData;
    private String TAG = MainActivity.class.getName();

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

                        // start cropping activity for pre-acquired image saved on the device
                        CropImage.activity(data.getData())
                                .setBackgroundColor(Color.parseColor("#80FFFFA6"))
                                .setActivityTitle("Cropping Activity")
                                .start(this);
                        break;

                    case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        Uri resultUri = result.getUri();
//                        imageView.setImageUriAsync(resultUri);

                        Bitmap cropped = null;
                        try {
                            cropped = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //execute the async task
                        new ConvImageTask(this).execute(cropped);

                        /*bitmap = decodeFromBase64ToBitmap(convertImageToBase64(cropped));
                        imageView.setImageBitmap(bitmap);
                        imageView.setShowCropOverlay(false);

                        OCRClass ocr = new OCRClass(MainActivity.this);
                        String s = ocr.processImage(bitmap);
                        txtData.setText(s);*/

                        break;

                }
                break;
        }
    }

    @Override
    public void processFinish(Bitmap output) {

        imageView.setImageBitmap(output);
        imageView.setShowCropOverlay(false);

        OCRClass ocr = new OCRClass(MainActivity.this);
        String s = ocr.processImage(output);
        txtData.setText(s);
    }


    /*Convert Image*/
    private String convertImageToBase64(Bitmap bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
        return imgString;
    }

    private Bitmap decodeFromBase64ToBitmap(String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);

        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;
    }
}
