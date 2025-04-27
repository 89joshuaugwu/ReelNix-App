package com.example.reelnixapp

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.text
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.glance.visibility
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

class MainActivity : ComponentActivity() {
    private lateinit var myWebView: WebView
    private var nativeAd: NativeAd? = null // Add this line to manage the NativeAd lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the Mobile Ads SDK
        MobileAds.initialize(this) {}
        setContent {
            Column(modifier = Modifier.fillMaxSize()) {
                // Display the Banner Ad
                BannerAdView(modifier = Modifier)
                // Display the Native Advanced Ad
                NativeAdView(modifier = Modifier)
                // Display the WebView
                WebViewScreen(url = "https://reelnix.vercel.app/") { webView ->
                    myWebView = webView
                }
            }
        }
    }

    override fun onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    override fun onDestroy() {
        nativeAd?.destroy() // Destroy the native ad when the activity is destroyed
        super.onDestroy()
    }
}

@Composable
fun NativeAdView(modifier: Modifier = Modifier) {
    var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }

    AndroidView(
        modifier = modifier.padding(10.dp),
        factory = { context ->
            val adLoader = AdLoader.Builder(context, "ca-app-pub-2745430575041686/1344161781")
                .forNativeAd { nativeAd ->
                    nativeAdState?.destroy()
                    nativeAdState = nativeAd
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e("NativeAd", "Failed to load native ad: ${adError.message}")
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
            val adView = LayoutInflater.from(context).inflate(R.layout.native_ad_layout, null) as NativeAdView
            nativeAdState?.let { populateNativeAdView(it, adView) }
            adView
        },
        update = { adView ->
            nativeAdState?.let { populateNativeAdView(it, adView) }
        }
    )
}

fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    adView.headlineView = adView.findViewById<TextView>(R.id.native_ad_headline)
    adView.mediaView = adView.findViewById<MediaView>(R.id.native_ad_media)
    adView.bodyView = adView.findViewById<TextView>(R.id.native_ad_body)
    adView.callToActionView = adView.findViewById<Button>(R.id.native_ad_call_to_action)

    (adView.headlineView as TextView).text = nativeAd.headline
    adView.mediaView?.setMediaContent(nativeAd.mediaContent!!)
    if (nativeAd.body == null) {
        adView.bodyView?.visibility = android.view.View.INVISIBLE
    } else {
        adView.bodyView?.visibility = android.view.View.VISIBLE
        (adView.bodyView as TextView).text = nativeAd.body
    }

    if (nativeAd.callToAction == null) {
        adView.callToActionView?.visibility = android.view.View.INVISIBLE
    } else {
        adView.callToActionView?.visibility = android.view.View.VISIBLE
        (adView.callToActionView as Button).text = nativeAd.callToAction
    }

    adView.setNativeAd(nativeAd)
}

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-2745430575041686/1201509797" // Your Banner Ad Unit ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(url: String, onWebViewCreated: (WebView) -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    val problematicDomains = listOf("downloadwella.com", "kimoitv.com", "meetdownload.com")

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            url: String?
                        ): Boolean {
                            Log.d("WebView", "URL Loading: $url")
                            if (url != null) {
                                if (problematicDomains.any { url.contains(it) } || url.startsWith("intent://")) {
                                    Log.d("WebView", "Opening in external browser: $url")
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                    return true
                                } else if (url.startsWith("http")) {
                                    Log.d("WebView", "Loading URL inside WebView: $url")
                                    view?.let {
                                        it.loadUrl(url)
                                    }
                                    return true
                                }
                            }
                            Log.d("WebView", "Not handling URL: $url")
                            return false
                        }
                    }
                    webChromeClient = WebChromeClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                        val request = DownloadManager.Request(
                            Uri.parse(url)
                        )
                        request.setMimeType(mimetype)
                        val cookies = android.webkit.CookieManager.getInstance().getCookie(url)
                        request.addRequestHeader("cookie", cookies)
                        request.addRequestHeader("User-Agent", userAgent)
                        request.setDescription("Downloading file...")
                        request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                        request.allowScanningByMediaScanner()
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            URLUtil.guessFileName(url, contentDisposition, mimetype)
                        )
                        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        dm.enqueue(request)
                        Toast.makeText(context, "Downloading File", Toast.LENGTH_LONG).show()
                    }
                    loadUrl(url)
                    onWebViewCreated(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}