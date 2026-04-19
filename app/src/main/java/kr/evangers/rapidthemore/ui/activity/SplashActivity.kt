package kr.evangers.rapidthemore.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.atomic.AtomicBoolean
import kr.evangers.rapidthemore.R
import kr.evangers.rapidthemore.ui.theme.RapidTheMoreTheme

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    private var webView: WebView? = null
    private var lastIntentFiredAt = 0L
    private val clickGuard = AtomicBoolean(false)
    private val retryHandler = Handler(Looper.getMainLooper())
    private var pendingRetry: Runnable? = null
    private var retryCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val coupangScript = getString(R.string.coupang_ad_script)

        setContent {
            RapidTheMoreTheme {
                CoupangLinkScreen(
                    coupangScript = coupangScript,
                    clickGuard = clickGuard,
                    onWebViewCreated = { webView = it },
                    onExternalUrl = { url -> fireExternal(url) },
                    onClickDispatched = { scheduleRetryCheck() },
                    onBackPressed = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        clickGuard.set(false)
        retryCount = 0
        lastIntentFiredAt = 0L
        pendingRetry?.let { retryHandler.removeCallbacks(it) }
        pendingRetry = null
        webView?.let { reloadBanner(it) }
    }

    private fun reloadBanner(web: WebView) {
        val coupangScript = getString(R.string.coupang_ad_script)
        web.loadData(buildBannerHtml(coupangScript), "text/html", "UTF-8")
    }

    private fun scheduleRetryCheck() {
        pendingRetry?.let { retryHandler.removeCallbacks(it) }
        val dispatchedAt = SystemClock.elapsedRealtime()
        val runnable = Runnable {
            if (lastIntentFiredAt >= dispatchedAt) return@Runnable
            if (retryCount >= MAX_CLICK_RETRIES) return@Runnable
            retryCount++
            clickGuard.set(false)
            webView?.evaluateJavascript(
                "window.__retryCoupangClick && window.__retryCoupangClick();",
                null
            )
        }
        pendingRetry = runnable
        retryHandler.postDelayed(runnable, CLICK_RETRY_DELAY_MS)
    }

    private fun fireExternal(url: String) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastIntentFiredAt < 2000L) return
        lastIntentFiredAt = now
        pendingRetry?.let { retryHandler.removeCallbacks(it) }
        pendingRetry = null
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        pendingRetry?.let { retryHandler.removeCallbacks(it) }
        pendingRetry = null
        webView?.destroy()
        webView = null
        super.onDestroy()
    }

    companion object {
        private const val MAX_CLICK_RETRIES = 20
        private const val CLICK_RETRY_DELAY_MS = 200L
    }
}

private fun buildBannerHtml(coupangScript: String): String {
    val watcherScript = """
        (function() {
            var fired = false;

            function findCoupangAnchor() {
                var anchors = document.querySelectorAll('a[href]');
                for (var i = 0; i < anchors.length; i++) {
                    var href = anchors[i].href || '';
                    if (href.indexOf('coupang.com') !== -1 || href.indexOf('link.coupang') !== -1) {
                        return href;
                    }
                }
                return null;
            }

            function dispatchClick(iframe) {
                var r = iframe.getBoundingClientRect();
                if (r.width < 50 || r.height < 20) return;
                var dpr = window.devicePixelRatio || 1;
                var cx = (r.left + r.width / 2) * dpr;
                var cy = (r.top + r.height / 2) * dpr;
                try { AndroidBannerBridge.onIframeReady(cx, cy); } catch (e) {}
            }

            window.__retryCoupangClick = function() {
                var iframe = document.querySelector('iframe');
                if (!iframe) return;
                dispatchClick(iframe);
            };

            function tryClickIframe() {
                var iframe = document.querySelector('iframe');
                if (!iframe) return false;
                var rect = iframe.getBoundingClientRect();
                if (rect.width < 50 || rect.height < 20) return false;
                fired = true;
                dispatchClick(iframe);
                return true;
            }

            function tryNavigate() {
                if (fired) return;
                var anchor = findCoupangAnchor();
                if (anchor) {
                    fired = true;
                    window.location.href = anchor;
                    return;
                }
                tryClickIframe();
            }

            var obs = new MutationObserver(tryNavigate);
            obs.observe(document.documentElement, { childList: true, subtree: true });

            var tries = 0;
            var iv = setInterval(function() {
                tryNavigate();
                if (fired || ++tries > 200) { clearInterval(iv); obs.disconnect(); }
            }, 50);
        })();
    """.trimIndent()

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body, html { margin: 0; padding: 0; width: 100%; height: 100%; }
                .banner-container { width: 100%; display: flex; justify-content: center; align-items: center; }
            </style>
        </head>
        <body>
            <div class="banner-container">
            <script src="https://ads-partners.coupang.com/g.js"></script>
<script>
    $coupangScript
</script>
            </div>
            <script>$watcherScript</script>
        </body>
        </html>
    """.trimIndent()
}

private class BannerClickBridge(
    private val guard: AtomicBoolean,
    private val webViewProvider: () -> WebView?,
    private val onClickDispatched: () -> Unit
) {
    @JavascriptInterface
    fun onIframeReady(deviceX: Float, deviceY: Float) {
        if (!guard.compareAndSet(false, true)) return
        val view = webViewProvider() ?: return
        Handler(Looper.getMainLooper()).post {
            if (view.width <= 0 || view.height <= 0) return@post
            val downTime = SystemClock.uptimeMillis()
            val down = MotionEvent.obtain(
                downTime, downTime,
                MotionEvent.ACTION_DOWN, deviceX, deviceY, 0
            ).apply { source = InputDevice.SOURCE_TOUCHSCREEN }
            view.dispatchTouchEvent(down)
            down.recycle()

            view.postDelayed({
                val upTime = SystemClock.uptimeMillis()
                val up = MotionEvent.obtain(
                    downTime, upTime,
                    MotionEvent.ACTION_UP, deviceX, deviceY, 0
                ).apply { source = InputDevice.SOURCE_TOUCHSCREEN }
                view.dispatchTouchEvent(up)
                up.recycle()
                onClickDispatched()
            }, 80L)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
@Composable
fun CoupangLinkScreen(
    coupangScript: String,
    clickGuard: AtomicBoolean = AtomicBoolean(false),
    onWebViewCreated: (WebView) -> Unit = {},
    onExternalUrl: (String) -> Unit = {},
    onClickDispatched: () -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    BackHandler { onBackPressed() }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                factory = { context ->
                    lateinit var web: WebView
                    web = WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            cacheMode = WebSettings.LOAD_NO_CACHE
                        }

                        addJavascriptInterface(
                            BannerClickBridge(
                                guard = clickGuard,
                                webViewProvider = { web },
                                onClickDispatched = onClickDispatched
                            ),
                            "AndroidBannerBridge"
                        )

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val target = request?.url?.toString() ?: return false
                                onExternalUrl(target)
                                return true
                            }
                        }

                        onWebViewCreated(this)
                        loadData(buildBannerHtml(coupangScript), "text/html", "UTF-8")
                    }
                    web
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoupangLinkScreenPreview() {
    RapidTheMoreTheme {
        CoupangLinkScreen(
            coupangScript = ""
        )
    }
}
