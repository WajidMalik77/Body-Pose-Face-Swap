package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.core.utils.PurchaseAnalyticsLogger;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class BillingUtilsIAP
{


    public static final String LIFETIME = "lifetime_subscription";
    //   public static final String LIFETIME = "android.test.purchased";
    private static BillingClient billingClient;
    private static boolean isBillingReady;
    private static boolean isPremium;
    private static PurchasesUpdatedListener purchaseUpdateListener;

    public BillingUtilsIAP(final Context context) {
        InAppPrefs instance = InAppPrefs.getInstance(context);
        isPremium = instance.getPremium();

        if (billingClient == null) {
            purchaseUpdateListener = new PurchasesUpdatedListener() {
                @Override
                public void onPurchasesUpdated(@NotNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
                    // Timber.i("getOldPurchases: in Listener");
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        for (Purchase purchase : purchases) {
                            PurchaseAnalyticsLogger.logPurchaseResult(
                                    purchase.getProducts().isEmpty() ? null : purchase.getProducts().get(0),
                                    BillingClient.ProductType.INAPP,
                                    "success",
                                    billingResult,
                                    purchase.getPurchaseToken(),
                                    purchase.isAcknowledged(),
                                    "lifetime_flow"
                            );
                            handlePurchase(context, purchase);
                        }
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                        PurchaseAnalyticsLogger.logPurchaseResult(
                                LIFETIME,
                                BillingClient.ProductType.INAPP,
                                "cancel",
                                billingResult,
                                null,
                                null,
                                "lifetime_flow"
                        );
                        //    Timber.i("getOldPurchases: User Cancelled");
                    } else {
                        PurchaseAnalyticsLogger.logPurchaseResult(
                                LIFETIME,
                                BillingClient.ProductType.INAPP,
                                "error",
                                billingResult,
                                null,
                                null,
                                "lifetime_flow"
                        );
                        //Timber.i("getOldPurchases: Other Error");
                    }
                }


            };
            BillingClient.Builder newBuilder = BillingClient.newBuilder(context);
            billingClient = newBuilder.setListener(purchaseUpdateListener)
                    .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                    .build();
            setupConnection(context);
        }
    }

    private void setupConnection(Context context) {

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NotNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    setBillingReady(true);
                    //Timber.i("onBillingServiceDisconnected: Setup Connection");
                    getOldPurchases(context);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                //    Timber.i("onBillingServiceDisconnected: Setup Connection Failed");
                setBillingReady(false);
            }
        });
    }

    public void purchase(Activity activity, String str) {
        PurchaseAnalyticsLogger.logPurchaseTap(str, BillingClient.ProductType.INAPP, "activity_purchase", "lifetime_flow");

        if (isBillingReady) {
            List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
            productList.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(str)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());
            QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build();
            BillingClient billingClient2 = billingClient;
            billingClient2.queryProductDetailsAsync(params, (billingResult, productDetailsResult) -> {
                List<ProductDetails> productDetailsList = productDetailsResult.getProductDetailsList();
                if (productDetailsList != null && !productDetailsList.isEmpty()) {
                    ProductDetails productDetails = productDetailsList.get(0);
                    BillingFlowParams.ProductDetailsParams detailsParams =
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .build();
                    BillingFlowParams build = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(java.util.Collections.singletonList(detailsParams))
                            .build();
                    BillingResult launchBillingFlow = getBillingClient().launchBillingFlow(activity, build);
                    PurchaseAnalyticsLogger.logPurchaseFlowLaunched(
                            str,
                            BillingClient.ProductType.INAPP,
                            "activity_purchase",
                            launchBillingFlow,
                            "lifetime_flow"
                    );
                    if (launchBillingFlow.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        //    Timber.i("getOldPurchases: Please try Again Later1");
                    }
                }
            });
            return;
        }
        //  Timber.i("getOldPurchases: Please try Again Later2");
        setupConnection(activity);
    }

    private void handlePurchase(Context context, Purchase purchase) {
        if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) return;

        boolean isLifetime = false;
        for (String skus : purchase.getProducts()) {
            if (skus.equals(LIFETIME)) {
                isLifetime = true;
                break;
            }
        }
        if (!isLifetime) return;

        if (purchase.isAcknowledged()) {
            grantLifetime(context);
            return;
        }

        acknowledgeWithRetry(context, purchase, 3);
    }

    private void acknowledgeWithRetry(final Context context, final Purchase purchase, final int attemptsLeft) {
        PurchaseAnalyticsLogger.logPurchaseAckStarted(
                purchase.getProducts().isEmpty() ? null : purchase.getProducts().get(0),
                purchase.getPurchaseToken(),
                attemptsLeft,
                "lifetime_flow"
        );
        AcknowledgePurchaseParams build = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken()).build();
        billingClient.acknowledgePurchase(build, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NotNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    PurchaseAnalyticsLogger.logPurchaseAckSuccess(
                            purchase.getProducts().isEmpty() ? null : purchase.getProducts().get(0),
                            purchase.getPurchaseToken(),
                            billingResult,
                            "lifetime_flow"
                    );
                    grantLifetime(context);
                } else if (attemptsLeft > 1) {
                    PurchaseAnalyticsLogger.logPurchaseAckFailed(
                            purchase.getProducts().isEmpty() ? null : purchase.getProducts().get(0),
                            purchase.getPurchaseToken(),
                            attemptsLeft,
                            billingResult,
                            "lifetime_flow"
                    );
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                            new Runnable() {
                                @Override public void run() {
                                    acknowledgeWithRetry(context, purchase, attemptsLeft - 1);
                                }
                            }, 2000);
                } else {
                    PurchaseAnalyticsLogger.logPurchaseAckFailed(
                            purchase.getProducts().isEmpty() ? null : purchase.getProducts().get(0),
                            purchase.getPurchaseToken(),
                            attemptsLeft,
                            billingResult,
                            "lifetime_flow"
                    );
                    Log.w("TAG", "Failed to acknowledge lifetime purchase: " + billingResult.getDebugMessage());
                }
            }
        });
    }

    private void grantLifetime(Context context) {
        isPremium = true;
        new InAppPrefs(context).setPremium(true);
        InAppPrefs.getInstance(context).setPremium(true);
        PrefUtil.Companion.setPremium(context, true);
        PurchaseAnalyticsLogger.logEntitlementActivated(
                LIFETIME,
                BillingClient.ProductType.INAPP,
                null,
                "lifetime_flow"
        );
        Log.i("TAG", "handlePurchase: premium granted after ack");
    }

    public void getOldPurchases(Context context) {

        QueryPurchasesParams queryPurchasesParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();
        billingClient.queryPurchasesAsync(queryPurchasesParams, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
                for (Purchase purchase : list) {
                    // Routes through ack-with-retry so unacknowledged purchases don't get auto-refunded.
                    handlePurchase(context, purchase);
                }
            }
        });

    }


    public BillingClient getBillingClient() {
        return billingClient;
    }

    public void setBillingClient(BillingClient client) {
        client = client;
    }

    public PurchasesUpdatedListener getPurchaseUpdateListener() {
        return purchaseUpdateListener;
    }

    public void setPurchaseUpdateListener(PurchasesUpdatedListener purchasesUpdatedListener) {
        purchaseUpdateListener = purchasesUpdatedListener;
    }

    public boolean isBillingReady() {
        return isBillingReady;
    }

    public static void setBillingReady(boolean z) {
        isBillingReady = z;
    }

    public static boolean isPremium() {
        return isPremium;
    }

    public final void setPremium(boolean z) {
        isPremium = z;
    }


}
