package cordova_plugin_gcprint;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.gcwebviewbmplib.webviewgetbmp;
import java.io.BufferedWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class HtmlPrint extends Activity {
    private WebView web;
    private float scale=1;
    private  int  prnMaxHeight=0;
    private  boolean bTimeOutPrint=false;
    private  boolean bSetCancel=false;
    private  ProgressDialog dialog   = null;
    private String TAG="GcHtmlPrintLog";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(cordova_plugin_gcprint.MResource.getIdByName(this, "layout", "gcprint_html"));

        web = (WebView) findViewById(cordova_plugin_gcprint.MResource.getIdByName(this, "id","webDsp"));
        setResult(RESULT_CANCELED);

        String htmlUrl=getIntent().getStringExtra("url");
        String htmlString = getIntent().getStringExtra("Content");
        int  setWidth = getIntent().getIntExtra("Width",0);
        int  timeOutTime = getIntent().getIntExtra("Time",0);
        bTimeOutPrint=getIntent().getBooleanExtra("bTimeOutPrint",false);
        bSetCancel=getIntent().getBooleanExtra("bSetCancel",false);
        if (setWidth >384 || (setWidth==LinearLayout.LayoutParams.MATCH_PARENT) || (setWidth==LinearLayout.LayoutParams.WRAP_CONTENT)){
            Log.d(TAG,"Set width:"+setWidth);
            LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) web.getLayoutParams();
            params.width=setWidth;
            web.setLayoutParams(params);
        }
        if (((htmlUrl == null) || (htmlUrl.length()<=4)) && ((htmlString == null) ||(htmlString.length()<=4))){
            Log.d(TAG,"HTML error:"+htmlUrl + " string:"+htmlString);
            finish();
        }
        else if ((htmlUrl == null) || (htmlUrl.length()<=4)){
            Log.d(TAG,"HTML String Len:"+htmlString.length());
            // Log.d(TAG,"HTML String:"+htmlString);
            htmlUrl=com.gcwebviewbmplib.webviewgetbmp.HtmlStringToUrl(this,htmlString);
            if (htmlUrl == null){
                Log.d(TAG,"HTML error:"+htmlUrl);
                finish();
            }
            else {
                web.loadUrl(htmlUrl);
            }
        }
        else{
            Log.d(TAG,"HTML URL:"+htmlUrl);
            web.loadUrl(htmlUrl);
        }
        if (timeOutTime>1){
            timeHandler.postDelayed(timeRunnable, (timeOutTime*1000));
            Log.i(TAG,"Page Start:"+(timeOutTime*1000));
        }

        WebSettings webSettings=web.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        web.setWebChromeClient(new WebChromeClient()
                               {
                                   @Override
                                   public boolean onJsAlert(WebView view, String url, String message,
                                                            JsResult result) {
                                       result.cancel();
                                       return true;
                                   }
                                   @Override
                                   public boolean onJsConfirm(WebView view, String url, String message,
                                                              JsResult result) {
                                       result.cancel();
                                       return true;
                                   }
                                   @Override
                                   public boolean onJsPrompt(WebView view, String url, String message,
                                                             String defaultValue, JsPromptResult result) {
                                       result.cancel();
                                       return true;
                                   }
                               }
        );
        web.setDrawingCacheEnabled(true);
        web.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                if(timeHandler != null) {
                    timeHandler.removeCallbacks(timeRunnable);
                    timeHandler=null;
                }
                prnHandler.postDelayed(runnable, 500);
                Log.i(TAG,"Page Finished");
            }
            @Override
            public void onScaleChanged(WebView view,float oldScale, float newScale){
                super.onScaleChanged(view, oldScale, newScale);
                scale = newScale;
                Log.i(TAG,"scale="+scale);
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setTitle(null);
        dialog.setMessage("Processing, please wait......");
        dialog.setCancelable(bSetCancel);
        dialog.show();
        Log.i(TAG,"bSetCancel="+bSetCancel);
    }

    Handler prnHandler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            startPrnWeb();
            setResult(RESULT_OK);
            finish();
        }
    };

    Handler timeHandler=new Handler();
    Runnable timeRunnable=new Runnable() {
        @Override
        public void run() {
            Log.i(TAG,"Page timeout: print="+bTimeOutPrint);
            if (bTimeOutPrint == true && web!=null) {
                web.stopLoading();
            }
            else{
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    };

    private void startPrnWeb(){
        web.setScrollY(0);
        float contentHeight2 = web.getContentHeight()*web.getScale();
        int contentHeight = (int)contentHeight2;
        int webHeight = web.getHeight();

        prnMaxHeight = webHeight * 6;
        Log.i(TAG,"ContentH="+contentHeight+"  WebH="+webHeight + " saH="+contentHeight2);
        if (contentHeight > prnMaxHeight){
            contentHeight=prnMaxHeight;
        }
        printWeb_Range(0,contentHeight);
    }

    private void printWeb_Range(int start,int contentHeight)
    {
        Bitmap bimap=com.gcwebviewbmplib.webviewgetbmp.getWebBmp(web,start,contentHeight);
        if (bimap == null){
            finish();
            return;
        }
        cordova_plugin_gcprint.GcPrintPlugin.sendBitmap(bimap);
    }

    @Override
    protected void onDestroy() {
        if (dialog != null){
            dialog.dismiss();
            dialog=null;
        }
        if (web != null) {
            ViewParent parent = web.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(web);
            }

            web.stopLoading();
            web.getSettings().setJavaScriptEnabled(false);
            web.clearHistory();
            web.clearCache(true);
            web.clearView();
            web.removeAllViews();
            web.destroy();

            web = null;
        }
        if (prnHandler != null) {
            prnHandler.removeCallbacks(runnable);
            prnHandler=null;
        }
        if (timeHandler != null) {
            timeHandler.removeCallbacks(timeRunnable);
            timeHandler=null;
        }
        super.onDestroy();
    }



}
