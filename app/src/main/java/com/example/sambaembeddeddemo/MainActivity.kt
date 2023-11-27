package com.example.sambaembeddeddemo

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import org.json.JSONObject
import android.Manifest;
import android.widget.LinearLayout

val PERMISSION_REQUEST_CODE = 1;
val ROOM_URL = "https://localhost:3000/Public";

private class LocalContentWebViewClient(private val assetLoader: WebViewAssetLoader) : WebViewClientCompat() {
    @RequiresApi(21)
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(request.url)
    }

    // To support API < 21.
    override fun shouldInterceptRequest(
        view: WebView,
        url: String
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(Uri.parse(url))
    }
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if(checkPermission()) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            val WebView: WebView = findViewById(R.id.webview)
            val buttonParent: LinearLayout = findViewById(R.id.button_parent);
            val toggleToolbarButton: Button = findViewById(R.id.toggle_toolbar);
            val endSessionButton: Button = findViewById(R.id.end_session);

            WebView.settings.javaScriptEnabled = true;
            WebView.settings.domStorageEnabled = true;

            WebView.settings.setMediaPlaybackRequiresUserGesture(false);

            val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
                .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(this))
                .build();


            WebView.setWebChromeClient(object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    runOnUiThread { request.grant(request.resources) }
                }
            });

            WebView.webViewClient = LocalContentWebViewClient(assetLoader);

            class SDKMessage {
                @JavascriptInterface
                fun receiveMessage(data: String) {
                    val responseJSON: JSONObject = JSONObject(data);
                    val type = responseJSON.getString("type");
                    Log.i("SDKMessage", "postMessage data=$responseJSON type=$type");

                    if(type == "roomJoined") {
                        Log.i("SDKMessage", "room joined");
                    }
                }
            }

            WebView.addJavascriptInterface(SDKMessage(), "Android")

            WebView.loadUrl("https://appassets.androidplatform.net/assets/frame.html?roomUrl=$ROOM_URL");

            toggleToolbarButton.setOnClickListener {
                WebView.evaluateJavascript("sambaEmbedded.toggleToolbar()", {});
            };

            endSessionButton.setOnClickListener {
                WebView.evaluateJavascript("sambaEmbedded.endSession()", {});
            };
        } else {
            requestPermission();
        }
    }

    private fun checkPermission(): Boolean {
        val cameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val micGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        return cameraGranted && micGranted
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            PERMISSION_REQUEST_CODE
        )
    }
}