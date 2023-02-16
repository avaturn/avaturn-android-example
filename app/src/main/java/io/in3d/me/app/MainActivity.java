package io.in3d.me.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebView myWebView = findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String PROJECT_DOMAIN = "https://demo.avaturn.dev"; // Replace with your project's domain

        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(myWebView, "native_app",
                    Set.of(PROJECT_DOMAIN),
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

        myWebView.loadUrl(PROJECT_DOMAIN + "/iframe/editor");
    }
}