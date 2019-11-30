package app.alessandrotedesco.asana.github.api

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import app.alessandrotedesco.asana.github.R

interface LoadingView {
    /**
     * disable user interaction with the UI
     */
    fun AppCompatActivity.blockInput() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    /**
     * enable user interaction with the UI
     */
    fun AppCompatActivity.unblockInput() {
        runOnUiThread {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    /**
     * show/hide the loading progressBar and block/allow user interaction
     * with the UI, depending on the loading variable value
     *
     * @param loading true if it should start the loading process, false if it should stop it
     * @see blockInput
     * @see unblockInput
     */
    fun setLoading(loading: Boolean)

    var loadingScreen: Dialog

    /**
     * initialises the loading screen, setting it to fullscreen,
     * without title,loading the view and making it not cancelable
     */
    fun initLoadingScreen(context: Context) {
        loadingScreen = Dialog(context, R.style.fullscreen)
        // set loadingScreen as a fullscreen Dialog
        loadingScreen.requestWindowFeature(Window.FEATURE_NO_TITLE)
        // set loading layout
        loadingScreen.setContentView(R.layout.loading_layout)
        // avoid user interaction
        loadingScreen.setCancelable(false)
        // set background color
        loadingScreen.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
