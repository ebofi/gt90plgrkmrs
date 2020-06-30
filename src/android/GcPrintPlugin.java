package cordova_plugin_gcprint;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

/**
 * This class echoes a string called from JavaScript.
 */
public class GcPrintPlugin extends CordovaPlugin {

    private static final String TAG="GcPrintPlugin";
    private static final String URL = "http://127.0.0.1:62769";
    public static CallbackContext m_callbackContext = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        m_callbackContext= callbackContext;
        if (action.equals("printString")) {

            String message = args.getString(0);
            this.printString(message);
            return true;
        }
        else if (action.equals("printBmpIntent")){
            if(!PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                PermissionHelper.requestPermission(this, 0, Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            else {
                openGetBitmap(null);
            }
            return true;
        }
        else if (action.equals("printBmpUri")){
            sendBitmap(args.getString(0));
        }
        else if (action.equals("printHtml")){
            Intent intent = new Intent(this.cordova.getActivity(), cordova_plugin_gcprint.HtmlPrint.class);
            intent.putExtra("url", args.getString(0));
            intent.putExtra("Content", args.getString(1));
            intent.putExtra("Width", args.getInt(2));
            intent.putExtra("Time", args.getInt(3));
            intent.putExtra("bTimeOutPrint", args.getBoolean(4));
            intent.putExtra("bSetCancel", args.getBoolean(5));
            this.cordova.startActivityForResult(this, intent, 1);
        }
        return false;
    }

    private void openGetBitmap(String bmpUri){
        Intent intent = new Intent(this.cordova.getActivity(), cordova_plugin_gcprint.OpenBmp.class);
        if (bmpUri != null) {
            intent.putExtra("bmp_uri", bmpUri);
        }
        this.cordova.startActivityForResult(this, intent, 0);
    }

    private void printString(String message) {
        if (message != null && message.length() > 0) {
            sendMessage(message);
        } else {
            Toast.makeText(cordova.getActivity(),"Print content is empty",Toast.LENGTH_LONG).show();
            if (m_callbackContext != null) {
                m_callbackContext.error("Print Error");
            }
        }
    }

    private void sendBitmap(String bmpUri){
        openGetBitmap(bmpUri);
    }

    private  void sendMessage(final String content){

        AsyncHttpClient.getDefaultInstance().websocket(URL,
                "printer",
                new AsyncHttpClient.WebSocketConnectCallback() {
                    @Override
                    public void onCompleted(Exception e, WebSocket webSocket) {
                        if (e != null){
                            e.printStackTrace();
                            return;
                        }
                        if (webSocket != null){
                            webSocket.send(content);

                            webSocket.setStringCallback(new WebSocket.StringCallback() {
                                @Override
                                public void onStringAvailable(String s) {
                                    if (s.equals("0")){
                                        Log.i(TAG, "Print Error");
                                        m_callbackContext.error("Print Error");
                                    }else{
                                        Log.i(TAG, "Print OK");
                                        m_callbackContext.success("Print Ok");
                                    }
                                }
                            });
                            webSocket.setClosedCallback(new CompletedCallback() {
                                @Override
                                public void onCompleted(Exception e) {
                                    webSocket.close();
                                }
                            });
                        }

                    }
                });
    }

    public static void sendBitmap(final Bitmap bitmap){
        AsyncHttpClient.getDefaultInstance().websocket(URL,
                "printer",
                new AsyncHttpClient.WebSocketConnectCallback() {
                    @Override
                    public void onCompleted(Exception e, final WebSocket webSocket) {
                        if (e != null){
                            e.printStackTrace();
                            return;
                        }
                        if (webSocket!= null){
                            webSocket.send(Bitmap2Bytes(bitmap));
                            webSocket.setStringCallback(new WebSocket.StringCallback() {
                                @Override
                                public void onStringAvailable(String s) {
                                    if (m_callbackContext != null) {
                                        if (s.equals("0")){
                                            Log.i(TAG, "Print Error");
                                            m_callbackContext.error("Print Error");
                                        }else{
                                            Log.i(TAG, "Print OK");
                                            m_callbackContext.success("Print Ok");
                                        }
                                    }
                                }
                            });
                            webSocket.setClosedCallback(new CompletedCallback() {
                                @Override
                                public void onCompleted(Exception e) {
                                    webSocket.close();
                                }
                            });
                        }
                    }
                });
    }



    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
            openGetBitmap(null);
        }
    }

    private static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == cordova.getActivity().RESULT_CANCELED){
                if (m_callbackContext != null) {
                    m_callbackContext.error("Print Error");
                }
            }
        }
        else if (requestCode == 1) {
            if (resultCode == cordova.getActivity().RESULT_CANCELED){
                if (m_callbackContext != null) {
                    m_callbackContext.error("Print Error");
                }
            }
        }
    }
}
