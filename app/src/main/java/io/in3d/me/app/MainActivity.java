package io.in3d.me.app;

import android.app.AlertDialog;
import android.os.Bundle;
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
        WebView myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(myWebView, "native_app", Set.of("*"),
                    (view, message, sourceOrigin, isMainFrame, replyProxy) -> {
                        String cust = null;
                        try {
                            JSONObject obj = new JSONObject(Objects.requireNonNull(message.getData()));
                            cust = obj.getJSONObject("customizations").toString(2);
                        } catch (JSONException e) {
                            cust = "error";
                        }

                        new AlertDialog.Builder(this)
                                .setTitle("Exported avatar")
                                .setMessage(cust)
                                .show();
                    }
            );
        }

        myWebView.loadUrl("https://vto.in3d.io/");
    }
}