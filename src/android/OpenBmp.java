package cordova_plugin_gcprint;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cordova_plugin_gcprint.GcPrintPlugin;

public class OpenBmp extends Activity {
    private static final String TAG="GcPrintBMP";
    private ProgressDialog dialog   = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onResume() {
        setResult(RESULT_CANCELED);
        String bmpUriStr=getIntent().getStringExtra("bmp_uri");
        if (bmpUriStr != null && (bmpUriStr.regionMatches(true,0,"http",0,4))){
            Log.d(TAG, "get url: "+bmpUriStr);
            getBitMap(bmpUriStr);
        }
        else if (bmpUriStr != null && bmpUriStr.length()>5){
            sendBmpData(Uri.parse(bmpUriStr));
            finish();
        } else {
            Intent getBmpintent = new Intent(Intent.ACTION_GET_CONTENT);
            getBmpintent.setType("image/*");
            getBmpintent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(getBmpintent, 0);
        }
        super.onResume();
    }

    private  void sendBmpData(Uri uri){
        ContentResolver cr = this.getContentResolver();
        try {
            Log.d(TAG, "get uri: "+uri.toString());
            Bitmap bitmap1 = BitmapFactory.decodeStream(cr.openInputStream(uri));
            Log.d(TAG, "bitmap:W="+bitmap1.getWidth()+" H="+bitmap1.getHeight());
            GcPrintPlugin.sendBitmap(bitmap1);
            setResult(RESULT_OK);
        } catch (FileNotFoundException e) {
            Log.e("Exception", e.getMessage(), e);
        }
    }
    @Override
    protected void onDestroy(){
        if (dialog != null){
            dialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                sendBmpData(uri);
            }
            finish();
        }
    }


    public void getBitMap(final String url){

        dialog = new ProgressDialog(this);
        dialog.setTitle(null);
        dialog.setMessage("Connecting, please wait......");
        dialog.setCancelable(false);
        dialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                URL imageurl = null;
                Bitmap bitmap;
                try {
                    imageurl = new URL(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    HttpURLConnection conn = (HttpURLConnection)imageurl.openConnection();
                    conn.setDoInput(true);
                    Log.d(TAG, "start");
                    conn.setConnectTimeout(10*1000);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    Log.d(TAG, "bitmap0:W="+bitmap.getWidth()+" H="+bitmap.getHeight());
                    if (bitmap != null && (bitmap.getWidth()>10) && (bitmap.getHeight()>10)) {
                        GcPrintPlugin.sendBitmap(bitmap);
                        setResult(RESULT_OK);
                    }
                    else{
                        setResult(RESULT_CANCELED);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "end");
                finish();
            }
        }).start();
    }


}
