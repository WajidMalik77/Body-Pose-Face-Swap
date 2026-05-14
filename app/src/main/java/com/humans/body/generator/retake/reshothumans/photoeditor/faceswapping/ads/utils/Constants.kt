package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object Constants {
    // Keep these aligned with the product IDs used by the purchase flows.
    const val PRODUCT_WEEKLY = "weekly_subscription1"
    const val PRODUCT_MONTHLY = "monthly_subscription1"
    const val PRODUCT_YEARLY = "yearly_subscription1"
    const val PRODUCT_LIFETIME = "lifetime_subscription"

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        val hasInternetCapability = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val hasValidatedConnection = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return hasInternetCapability && hasValidatedConnection
    }
}
