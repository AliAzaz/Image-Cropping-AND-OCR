package com.ali.azaz.ocr;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import static com.ali.azaz.ocr.ImplementConversion.convertImageToBase64;
import static com.ali.azaz.ocr.ImplementConversion.decodeFromBase64ToBitmap;

public class ConvImageTask extends AsyncTask<Bitmap, Void, Boolean> {
    public ImplementConversion.AsyncResponse delegate = null;
    private ProgressDialog dialog;
    private Context context;
    private Bitmap bitmap;

    public ConvImageTask(Context mContext) {
        context = mContext;
        dialog = new ProgressDialog(context, R.style.AppTheme_Dark_Dialog);
    }

    protected void onPreExecute() {
        this.dialog.setMessage("Generating List.");
        this.dialog.show();
    }

    @Override
    protected void onPostExecute(final Boolean success) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                delegate.processFinish(bitmap);

                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

                if (!success) {
                    Toast.makeText(context, "Error in getting Data!!", Toast.LENGTH_LONG).show();
                }

            }
        }, 800);

    }

    protected Boolean doInBackground(final Bitmap... args) {
        try {

            String img64Bit = convertImageToBase64(args[0]);
            bitmap = decodeFromBase64ToBitmap(img64Bit);

            return true;
        } catch (Exception e) {
            Log.e("tag", "error", e);
            return false;
        }
    }
}
