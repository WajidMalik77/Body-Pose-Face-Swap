package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.core

import android.annotation.SuppressLint
import android.app.Application
import com.android.billingclient.api.ProductDetails
import com.google.firebase.FirebaseApp
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.BillingManager

class MyApp : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var billingManager: BillingManager
    }

    var cachedProductDetails: List<ProductDetails> = emptyList()
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        billingManager = BillingManager(this, object : BillingManager.PurchaseListener {
            override fun onPurchaseSuccess() {}
            override fun onPurchaseFailure() {}
        })

        billingManager.startConnection {
            billingManager.querySubscriptions(
                listOf("weeklysubscription", "monthlysubscription", "yearlysubscription")
            ) { products ->
                cachedProductDetails = products
            }
        }
    }
}