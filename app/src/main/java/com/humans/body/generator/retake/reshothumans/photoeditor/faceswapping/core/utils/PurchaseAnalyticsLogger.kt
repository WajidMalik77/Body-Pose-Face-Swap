package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.core.utils

import android.content.Context
import android.os.Bundle
import com.android.billingclient.api.BillingResult
import java.security.MessageDigest
import java.util.Locale

object PurchaseAnalyticsLogger {
    const val EVENT_PAYWALL_VIEWED = "iap_step_01_paywall_viewed"
    const val EVENT_PURCHASE_TAP = "iap_step_02_purchase_tap"
    const val EVENT_PURCHASE_FLOW_LAUNCHED = "iap_step_03_flow_launched"
    const val EVENT_PURCHASE_RESULT = "iap_step_04_purchase_result"
    const val EVENT_PURCHASE_ACK_STARTED = "iap_step_05_ack_started"
    const val EVENT_PURCHASE_ACK_SUCCESS = "iap_step_06_ack_success"
    const val EVENT_PURCHASE_ACK_FAILED = "iap_step_06_ack_failed"
    const val EVENT_ENTITLEMENT_ACTIVATED = "iap_step_07_entitlement_activated"
    const val EVENT_RESTORE_STARTED = "iap_step_08_restore_started"
    const val EVENT_RESTORE_RESULT = "iap_step_09_restore_result"

    private const val PARAM_PRODUCT_ID = "product_id"
    private const val PARAM_PRODUCT_TYPE = "product_type"
    private const val PARAM_SCREEN = "screen"
    private const val PARAM_SOURCE = "source"
    private const val PARAM_RESULT = "result"
    private const val PARAM_RESPONSE_CODE = "response_code"
    private const val PARAM_DEBUG_MESSAGE = "debug_message"
    private const val PARAM_TOKEN_HASH = "token_hash"
    private const val PARAM_IS_ACKNOWLEDGED = "is_acknowledged"
    private const val PARAM_ATTEMPTS_LEFT = "attempts_left"
    private const val PARAM_PURCHASE_COUNT = "purchase_count"

    @JvmStatic
    fun logPaywallViewed(screen: String, source: String? = null) {
        log(EVENT_PAYWALL_VIEWED) {
            putString(PARAM_SCREEN, screen)
            source?.let { putString(PARAM_SOURCE, it) }
        }
    }

    @JvmStatic
    fun logPurchaseTap(productId: String, productType: String, screen: String, source: String? = null) {
        log(EVENT_PURCHASE_TAP) {
            putString(PARAM_PRODUCT_ID, productId)
            putString(PARAM_PRODUCT_TYPE, productType)
            putString(PARAM_SCREEN, screen)
            source?.let { putString(PARAM_SOURCE, it) }
        }
    }

    @JvmStatic
    fun logPurchaseFlowLaunched(
        productId: String,
        productType: String,
        screen: String,
        billingResult: BillingResult,
        source: String? = null
    ) {
        log(EVENT_PURCHASE_FLOW_LAUNCHED) {
            putString(PARAM_PRODUCT_ID, productId)
            putString(PARAM_PRODUCT_TYPE, productType)
            putString(PARAM_SCREEN, screen)
            putInt(PARAM_RESPONSE_CODE, billingResult.responseCode)
            putString(PARAM_DEBUG_MESSAGE, billingResult.debugMessage.safeTrim())
            source?.let { putString(PARAM_SOURCE, it) }
        }
    }

    @JvmStatic
    fun logPurchaseResult(
        productId: String?,
        productType: String,
        result: String,
        billingResult: BillingResult,
        token: String? = null,
        isAcknowledged: Boolean? = null,
        source: String? = null
    ) {
        log(EVENT_PURCHASE_RESULT) {
            productId?.let { putString(PARAM_PRODUCT_ID, it) }
            putString(PARAM_PRODUCT_TYPE, productType)
            putString(PARAM_RESULT, result)
            putInt(PARAM_RESPONSE_CODE, billingResult.responseCode)
            putString(PARAM_DEBUG_MESSAGE, billingResult.debugMessage.safeTrim())
            token?.let { putString(PARAM_TOKEN_HASH, hashToken(it)) }
            isAcknowledged?.let { putLong(PARAM_IS_ACKNOWLEDGED, if (it) 1 else 0) }
            source?.let { putString(PARAM_SOURCE, it) }
        }
    }

    @JvmStatic
    fun logPurchaseAckStarted(productId: String?, token: String, attemptsLeft: Int, source: String? = null) {
        log(EVENT_PURCHASE_ACK_STARTED) {
            productId?.let { putString(PARAM_PRODUCT_ID, it) }
            putString(PARAM_TOKEN_HASH, hashToken(token))
            putInt(PARAM_ATTEMPTS_LEFT, attemptsLeft)
            source?.let { putString(PARAM_SOURCE, it) }
        }
    }

    @JvmStatic
    fun logPurchaseAckSuccess(productId: String?, token: String, billingResult: BillingResult, source: String? = null) {
        log(EVENT_PURCHASE_ACK_SUCCESS) {
            productId?.let { putString(PARAM_PRODUCT_ID, it) }
            putString(PARAM_TOKEN_HASH, hashToken(token))
            putInt(PARAM_RESPONSE_CODE, billingResult.responseCode)
            putString(PARAM_DEBUG_MESSAGE, billingResult.debugMessage.safeTrim())
            source?.let { putString(PARAM_SOURCE, it) }
        }
    }

    @JvmStatic
    fun logPurchaseAckFailed(
        productId: String?,
        token: String,
        attemptsLeft: Int,
        billingResult: BillingResult,
        source: String? = null
    ) {
        log(EVENT_PURCHASE_ACK_FAILED) {
            productId?.let { putString(PARAM_PRODUCT_ID, it) }
            putString(PARAM_TOKEN_HASH, hashToken(token))
            putInt(PARAM_ATTEMPTS_LEFT, attemptsLeft)
            putInt(PARAM_RESPONSE_CODE, billingResult.responseCode)
            putString(PARAM_DEBUG_MESSAGE, billingResult.debugMessage.safeTrim())
            source?.let { putString(PARAM_SOURCE, it) }
        }
    }

    @JvmStatic
    fun logEntitlementActivated(productId: String?, productType: String, token: String?, source: String? = null) {
        log(EVENT_ENTITLEMENT_ACTIVATED) {
            productId?.let { putString(PARAM_PRODUCT_ID, it) }
            putString(PARAM_PRODUCT_TYPE, productType)
            token?.let { putString(PARAM_TOKEN_HASH, hashToken(it)) }
            source?.let { putString(PARAM_SOURCE, it) }
        }
    }

    @JvmStatic
    fun logRestoreStarted(productType: String, source: String? = null) {
        log(EVENT_RESTORE_STARTED) {
            putString(PARAM_PRODUCT_TYPE, productType)
            source?.let { putString(PARAM_SOURCE, it) }
        }
    }

    @JvmStatic
    fun logRestoreResult(productType: String, billingResult: BillingResult, purchaseCount: Int, source: String? = null) {
        log(EVENT_RESTORE_RESULT) {
            putString(PARAM_PRODUCT_TYPE, productType)
            putInt(PARAM_RESPONSE_CODE, billingResult.responseCode)
            putString(PARAM_DEBUG_MESSAGE, billingResult.debugMessage.safeTrim())
            putInt(PARAM_PURCHASE_COUNT, purchaseCount)
            source?.let { putString(PARAM_SOURCE, it) }
        }
    }

    @JvmStatic
    fun hashToken(rawToken: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(rawToken.toByteArray())
        return bytes.joinToString("") { "%02x".format(Locale.US, it) }.take(16)
    }

    private inline fun log(eventName: String, fill: Bundle.() -> Unit) {
        // Disabled: only funnel analytics events are allowed.
    }

    private fun String?.safeTrim(maxLen: Int = 100): String {
        if (this.isNullOrBlank()) return ""
        return this.take(maxLen)
    }
}
