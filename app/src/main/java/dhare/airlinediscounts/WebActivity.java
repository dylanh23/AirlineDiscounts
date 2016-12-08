package dhare.airlinediscounts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebActivity extends Activity {
    private WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        webView = (WebView) this.findViewById(R.id.webView);
        String url = intent.getStringExtra("AIRPORT NAME") == null ?
                "https://www.google.com/search?q=" + "discounted+" + intent.getStringExtra("AIRLINE NAME").replaceAll("\\s", "+") + "+flights" :
                "https://www.google.com/search?q=" + "discounted+" + intent.getStringExtra("AIRLINE NAME").replaceAll("\\s", "+") + "+flights+from+"
                        + intent.getStringExtra("AIRPORT NAME").replaceAll("\\s", "+");
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient());
    }
}
