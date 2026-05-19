package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.zeugmasolutions.localehelper.LocaleHelper
import com.zeugmasolutions.localehelper.LocaleHelperActivityDelegate
import com.zeugmasolutions.localehelper.LocaleHelperActivityDelegateImpl
import dagger.hilt.android.AndroidEntryPoint

import java.util.Locale
@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {
    private val localeDelegate: LocaleHelperActivityDelegate = LocaleHelperActivityDelegateImpl()

    override fun getDelegate() = localeDelegate.getAppCompatDelegate(super.getDelegate())

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(localeDelegate.attachBaseContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            localeDelegate.onCreate(this)

        }catch (_:ClassCastException){

        }




    }

    override fun onResume() {
        try {
            super.onResume()
            localeDelegate.onResumed(this)
        }catch (_:ClassCastException){}

    }

    override fun onPause() {
        try {
            super.onPause()
            localeDelegate.onPaused()
        }catch (_:ClassCastException){}

    }

    override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
        val context = super.createConfigurationContext(overrideConfiguration)
        return LocaleHelper.onAttach(context)
    }

    override fun getApplicationContext(): Context =
        localeDelegate.getApplicationContext(super.getApplicationContext())

    open fun updateLocale(locale: Locale) {
        localeDelegate.setLocale(this, locale)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return try {
            super.dispatchKeyEvent(event)
        } catch (securityException: SecurityException) {
            // Some OEM/framework builds can throw when fallback key handling attempts to
            // close system dialogs without privileged permission.
            Log.w(
                "BaseActivity",
                "Blocked framework key fallback SecurityException (keyCode=${event.keyCode})",
                securityException
            )
            true
        }
    }
}
