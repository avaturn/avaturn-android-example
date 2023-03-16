package io.in3d.me.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(String.format("Camera permission required for scanning"))
                        .show();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebView myWebView = findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, CAMERA_PERMISSION_REQUEST_CODE);
        }

        myWebView.setWebChromeClient(new WebChromeClient() {
             @Override
             public void onPermissionRequest(PermissionRequest request) {
                 request.grant(request.getResources());
             }
        }
        );

        String URL = "https://demo.avaturn.dev/iframe"; // Replace with your project's domain
        Uri uri = Uri.parse(URL);
        String projectDomain = uri.getScheme() + "://" + uri.getHost();


        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(myWebView, "native_app",
                    Set.of(projectDomain),
                    (view, message, sourceOrigin, isMainFrame, replyProxy) -> {
                        try {
                            JSONObject obj = new JSONObject(Objects.requireNonNull(message.getData()));
                            JSONObject data = obj.getJSONObject("data");

                            if (!obj.getString("eventName").equals("v2.avatar.exported")) {
                                return;
                            }

                            String url_type = data.getString("urlType");
                            String url = data.getString("url");
                            if (url_type.equals("httpURL")) {
                                new AlertDialog.Builder(this)
                                        .setTitle("Received http URL for glb file")
                                        .setMessage(url)
                                        .show();

                            } else {
                                byte[] glb_bytes = Base64.decode(url.substring(url.lastIndexOf(",") + 1), Base64.DEFAULT);
                                new AlertDialog.Builder(this)
                                        .setTitle("Received data URL for glb file")
                                        .setMessage(String.format("Glb file has %.2f Mb size", glb_bytes.length / 1024. / 1024))
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
            );
        }

        myWebView.loadUrl(URL);
    }
}
