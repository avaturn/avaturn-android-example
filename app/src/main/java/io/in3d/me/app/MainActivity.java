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
        WebView myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(myWebView, "native_app", Set.of("*"),
                    (view, message, sourceOrigin, isMainFrame, replyProxy) -> {
                        byte[] glb_bytes = new byte[0];
                        try {
                            JSONObject obj = new JSONObject(Objects.requireNonNull(message.getData()));
                            JSONObject data = obj.getJSONObject("data");
                            String dataURI = data.getString("blobURI");
                            glb_bytes = Base64.decode(dataURI.substring(dataURI.lastIndexOf(",") + 1), Base64.DEFAULT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            new AlertDialog.Builder(this)
                                    .setTitle("Exported avatar")
                                    .setMessage(String.format("The received glb file has %.2f Mb size", glb_bytes.length / 1024. / 1024))
                                    .show();
                        }
                    }
            );
        }

        myWebView.loadUrl("https://avaturn.me/iframe/editor");
    }
}