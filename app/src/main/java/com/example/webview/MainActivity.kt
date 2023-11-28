package com.example.webview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.CaseMap.Title
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.webview.ui.theme.WebviewTheme
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class MainActivity : ComponentActivity() {
    private lateinit var  mAdView : AdView
    private var number: Int = 0
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivity"
    companion object {
        var globalUrl = "http://www.sportainmentdesign.com/"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        MobileAds.initialize(this) {}
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myWebView: WebView = findViewById(R.id.myWebView)
        val button1:Button=findViewById(R.id.button1)
        val alertMsg:TextView=findViewById(R.id.internetalert)
        if (checkForInternet(this)) {
            val progressBar: ProgressBar = findViewById(R.id.progress_bar)
            myWebView.settings.javaScriptEnabled = true
            myWebView.loadUrl(globalUrl)
            myWebView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    globalUrl= myWebView.url.toString()
                    return true
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view,url,favicon)
                    //progress_bar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    //progress_bar.visibility = View.GONE
                }
                override fun onReceivedError(view: WebView, int: Int, description:String, failingUrl:String){
                    Toast.makeText(applicationContext, "No internet connection", Toast.LENGTH_LONG).show()
                    myWebView.loadUrl("file:///android_asset/lost.html")
                }
            }
            myWebView.webChromeClient = object : WebChromeClient(){
                override fun onProgressChanged(view: WebView, newProgress:Int){
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                    if(newProgress==100){
                        progressBar.visibility = View.GONE
                    }
                    super.onProgressChanged(view, newProgress)
                }
            }

            //Swipe to refresh
            val swipeRefreshLayout:SwipeRefreshLayout = findViewById(R.id.swipe)
            swipeRefreshLayout.setOnRefreshListener {
                alertMsg.text = number++.toString()
                myWebView.loadUrl(globalUrl)
                swipeRefreshLayout.isRefreshing = false
            }

            //Show mobile ad
            MobileAds.initialize(this) {}
            mAdView = findViewById(R.id.adView)
            val adRequest = AdRequest.Builder().build()
            mAdView.loadAd(adRequest)
            button1.visibility = View.INVISIBLE
            mAdView.adListener = object: AdListener() {
                override fun  onAdClicked() {
                    alertMsg.text ="Ads Clicked"
                    // Code to be executed when the user clicks on an ad.
                }
                override fun onAdClosed(){
                    alertMsg.text="Ads Closed"
                }
            }

            val buttonLogin: Button = findViewById(R.id.submit1)
            buttonLogin.setOnClickListener {

                var adRequest1 = AdRequest.Builder().build()
                InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712",
                    adRequest1, object : InterstitialAdLoadCallback(){
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        //Log.d(TAG, adError?.toString())
                        mInterstitialAd = null
                    }
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        //Log.d(TAG, 'Ad was loaded.')
                        mInterstitialAd = interstitialAd
                    }
                })
                val intentLogin = Intent(this, StateActivity::class.java)
                startActivity(intentLogin)
            }
            //alertMsg.setVisibility(View.INVISIBLE)
        }else{
            //val buttonClick = findViewById<Button>(R.id.button1)
            button1.setOnClickListener {
                val mIntent = intent
                finish()
                startActivity(mIntent)
                //myWebView.reload();
                /*val i = Intent(this@MainActivity, MainActivity::class.java)
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                finish()
                startActivity(getIntent());*/
                //startActivity(i)

            }
            button1.visibility=View.VISIBLE
            button1.text= "Please check your Internet4 Connection."
            alertMsg.visibility=View.VISIBLE
            alertMsg.text = "Please check your Internet4 Connection."
        }


        onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                /*if (backPressedTime + 3000 > System.currentTimeMillis()) {
                    super.onBackPressed()
                    finish()
                } else {
                    Toast.makeText(this, "Press back again to leave the app.", Toast.LENGTH_LONG).show()
                }*/
                // Back is pressed... Finishing the activity
                if (myWebView.canGoBack()) {
                    myWebView.goBack()

                }else {
                    finish()
                }
            }
        })



    }
    /*Working Code @Override
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val myWebView: WebView = findViewById(R.id.myWebView)
        // Check whether the key event is the Back button and if there's history.
        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack()
            return true
        }
        // If it isn't the Back button or there isn't web page history, bubble up to
        // the default system behavior. Probably exit the activity.
        return super.onKeyDown(keyCode, event)
    }*/

    private fun checkForInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }



}












@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WebviewTheme {
        Greeting("Android")
    }
}