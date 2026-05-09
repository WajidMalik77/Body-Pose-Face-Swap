package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.PurchasesUpdatedListener
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ActivityPurchaseBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.BillingUtilsIAP
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharedPreferencesClass
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.openGooglePrivacy
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.privacyPolicy
import java.util.Currency

class ActivityPurchase : BaseActivity() {

    private val binding by lazy {
        ActivityPurchaseBinding.inflate(layoutInflater)
    }
    private var isSplash = false
    private var billingClient: BillingClient? = null
    private var mSharePrefHelper: SharedPreferencesClass? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.light_color) // or your dark color
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        isSplash = intent.getBooleanExtra("isSplash", false)
        Handler(mainLooper).postDelayed({
            binding.close.visibility = View.VISIBLE
        }, 1400)
        binding.continueWithAds.setOnClickListener {
            goNext()
        }
        binding.termsAmpConditions.setOnClickListener {
            openGooglePrivacy()
        }
        binding.privacy.setOnClickListener {
            privacyPolicy()

        }
        binding.close.setOnClickListener {
            goNext()
        }
        mSharePrefHelper = SharedPreferencesClass(applicationContext)


        mSharePrefHelper!!.setBooleanPreferences(
            mSharePrefHelper!!.REMOVE_AD_ACTIVITY_OPEN, true
        )

        binding.purchase.setOnClickListener {
            MyApplication.isResume = true
            BillingUtilsIAP(this)
                .purchase(
                    this,
                    BillingUtilsIAP.LIFETIME
                )

        }
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    querySkuDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to Google Play by calling the startConnection() method.
            }
        })


    }


    val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                Log.d("TAG", "skuDetails: list = ${purchase.orderId}")
                // Handle the purchase, acknowledge, consume, or process it as needed
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle user cancellation
            Log.d("TAG", "skuDetails: cancel")
        } else {
            // Handle other errors
            Log.d("TAG", "skuDetails: error")
        }
    }

    fun querySkuDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BillingUtilsIAP.LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        Log.e("TESTTAG", "querySkuDetails ")
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            Log.e("TESTTAG", "billingResult ")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetailsList = productDetailsResult.productDetailsList
                Log.e("TESTTAG", "productDetailsList = ${productDetailsList.size} ")
                for (productDetails in productDetailsList) {
                    Log.e("TESTTAG", "productDetails ")
                    val oneTime = productDetails.oneTimePurchaseOfferDetails
                    val price = oneTime?.formattedPrice ?: continue
                    val currencyCode = oneTime.priceCurrencyCode
                    Currency.getInstance(currencyCode).symbol

                    runOnUiThread {
                        binding.price.text = price
                    }
                }
            } else {
                Log.d("TAG", "Failed to query product details: ${billingResult.debugMessage}")
            }
        }
    }

    private fun goNext() {
        if (isSplash) {
//            if (!isFinishing && !isDestroyed)
//                preLoadShowInterstitial(Ispurchase_inter_ad_key, purchase_inter_ad_key) {
                    startActivity(
                        Intent(
                            this,
                            HomeActivity::class.java
                        )
                    )
                    finish()
//                }
        } else
            finish()
    }
}
