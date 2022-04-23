package com.hajigames.quizmoiz


import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.adivery.sdk.Adivery
import com.adivery.sdk.AdiveryListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_webview.*


class MainActivity : AppCompatActivity() {

    var requiredPermissions = arrayOf<String>(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    val REQUEST_SELECT_FILE = 100
    private val FILECHOOSER_RESULTCODE = 1
    var uploadMessage: ValueCallback<Array<Uri>>? = null

    var link: String? = null
    private var mUploadMessage: ValueCallback<*>? = null


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_webview)

        val mWebView = findViewById<WebView>(R.id.webview)
        val webSettings = mWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.databaseEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webSettings.loadsImagesAutomatically = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.setAppCacheEnabled(true)

        mWebView.loadUrl(getString(R.string.website_url))
        mWebView.webViewClient = HelloWebViewClient()

        WebView.setWebContentsDebuggingEnabled(false)

        mWebView.addJavascriptInterface(WebViewJavaScriptInterface(this, mWebView), "Android");

        mWebView.webChromeClient = object : WebChromeClient() {

            // For 3.0+ Devices (Start)
            // onActivityResult attached before constructor
            protected fun openFileChooser(uploadMsg: ValueCallback<*>, acceptType: String) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*"
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE)
            }

            // For Lollipop 5.0+ Devices
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onShowFileChooser(mWebView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
                if (uploadMessage != null) {
                    uploadMessage!!.onReceiveValue(null)
                    uploadMessage = null
                }

                uploadMessage = filePathCallback

                val intent = fileChooserParams.createIntent()
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE)
                } catch (e: Exception) {
                    uploadMessage = null
                    return false
                }

                return true
            }

            //For Android 4.1 only
            protected fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
                mUploadMessage = uploadMsg
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILECHOOSER_RESULTCODE)
            }

            protected fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*"
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE)
            }


            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress_bar.progress = newProgress

                //display web page
                if (newProgress > 80) {
                    hlnt_iv_load.visibility = View.GONE
                    mWebView.visibility = View.VISIBLE
                }
                Log.d("Progress", newProgress.toString())
            }
        }

        //
        Adivery.configure(application, "09220273-60aa-4ea8-a43c-535dc9888f5b")
        Adivery.prepareRewardedAd(baseContext, "5f5ce47c-697e-4a5d-9873-edecb7e1ff8e")

        Adivery.addGlobalListener(object : AdiveryListener() {
            override fun onAppOpenAdLoaded(placementId: String) {
                // تبلیغ اجرای اپلیکیشن بارگذاری شده است.
            }

            override fun onInterstitialAdLoaded(placementId: String) {
                // تبلیغ میان‌صفحه‌ای بارگذاری شده
            }

            override fun onRewardedAdLoaded(placementId: String) {
                // تبلیغ جایزه‌ای بارگذاری شده
                //Toast.makeText(baseContext, "onRewardedAdLoaded", Toast.LENGTH_SHORT).show()
            }

            override fun onRewardedAdClosed(placementId: String, isRewarded: Boolean) {
                // بررسی کنید که آیا کاربر جایزه دریافت می‌کند یا خیر
                //Toast.makeText(baseContext, "onRewardedAdClosed", Toast.LENGTH_SHORT).show()

                val mWebView = findViewById<WebView>(R.id.webview)
                mWebView.loadUrl("javascript:appendRewrad();");
            }

            override fun log(placementId: String, log: String) {
                // پیغام را چاپ کنید
                //Log.e("Adivery", log)
            }
        })

        FirebaseApp.initializeApp(this)

        val mp: MediaPlayer
        mp = MediaPlayer.create(applicationContext, R.raw.music)
        mp.isLooping = true
        mp.start()

    }


    class WebViewJavaScriptInterface(private val context: Context, private val mWebView: WebView) {


        @JavascriptInterface
        fun playRewardVideo(userid: String?) {
            // Toast.makeText(context, userid, Toast.LENGTH_SHORT).show()
            Adivery.showAd("5f5ce47c-697e-4a5d-9873-edecb7e1ff8e")
        }

        @JavascriptInterface
        fun getfirebaseToken() {

            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                // Log and toast
                val msg = token

                showClickMessage(msg)
            })


        }

        private fun showClickMessage(msg: String?) {
            mWebView.loadUrl("javascript:SaveFireBaseToken('" + msg + "');");
        }

        /*
         * Need a reference to the context in order to sent a post message
         */
        init {
            Log.e("Inside Interface", "Hello Vinod Dirishala")
        }

    }


    private inner class HelloWebViewClient : WebViewClient() {

        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            super.onReceivedError(view, errorCode, description, failingUrl)
        }


        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (Uri.parse(url).host == getString(R.string.website_domain)) {
                return false
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {

            super.onPageFinished(view, url)
        }


    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
            webview.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }





}
