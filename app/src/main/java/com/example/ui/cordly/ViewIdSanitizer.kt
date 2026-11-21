package com.example.ui.cordly

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.R

/**
 * Ensures every View in the Activity and Dialog window hierarchies has a registered app R.id
 * instead of View.NO_ID (0xffffffff), eliminating system/emulator hierarchy-inspection log errors
 * ("No package ID ff found for resource ID 0xffffffff"), and keeps system bottom navigation bars
 * hidden so they never obstruct bottom controls.
 */
object ViewIdSanitizer {
    private val autoIdPool = intArrayOf(
        R.id.cordly_auto_view_1,
        R.id.cordly_auto_view_2,
        R.id.cordly_auto_view_3,
        R.id.cordly_auto_view_4,
        R.id.cordly_auto_view_5,
        R.id.cordly_auto_view_6,
        R.id.cordly_auto_view_7,
        R.id.cordly_auto_view_8,
        R.id.cordly_auto_view_9,
        R.id.cordly_auto_view_10,
        R.id.cordly_auto_view_11,
        R.id.cordly_auto_view_12,
        R.id.cordly_auto_view_13,
        R.id.cordly_auto_view_14,
        R.id.cordly_auto_view_15,
        R.id.cordly_auto_view_16,
        R.id.cordly_auto_view_17,
        R.id.cordly_auto_view_18,
        R.id.cordly_auto_view_19,
        R.id.cordly_auto_view_20,
        R.id.cordly_auto_view_21,
        R.id.cordly_auto_view_22,
        R.id.cordly_auto_view_23,
        R.id.cordly_auto_view_24
    )

    private var cursor = 0

    private fun nextId(): Int {
        val id = autoIdPool[cursor]
        cursor = (cursor + 1) % autoIdPool.size
        return id
    }

    fun assignMissingViewIds(root: View?) {
        if (root == null) return
        if (root.id == View.NO_ID) {
            root.id = nextId()
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                assignMissingViewIds(root.getChildAt(i))
            }
        }
    }

    fun hideSystemNavigationBars(window: Window?, targetView: View? = window?.decorView) {
        if (window == null || targetView == null) return
        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, targetView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        } catch (_: Exception) {
        }
    }

    fun attachToWindow(window: Window?) {
        val decor = window?.decorView ?: return
        assignMissingViewIds(decor)
        hideSystemNavigationBars(window, decor)
        decor.post {
            assignMissingViewIds(decor)
            hideSystemNavigationBars(window, decor)
        }
        val vto = decor.viewTreeObserver
        if (vto.isAlive) {
            vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    assignMissingViewIds(decor)
                }
            })
            vto.addOnWindowFocusChangeListener { hasFocus ->
                if (hasFocus) {
                    hideSystemNavigationBars(window, decor)
                }
            }
        }
    }
}

@Composable
fun SanitizeDialogWindowIds() {
    val view = LocalView.current
    val dialogWindow = (view.parent as? DialogWindowProvider)?.window
        ?: (view.context as? Activity)?.window
    SideEffect {
        ViewIdSanitizer.assignMissingViewIds(view.rootView)
        ViewIdSanitizer.hideSystemNavigationBars(dialogWindow, view)
    }
    DisposableEffect(view, dialogWindow) {
        val root = view.rootView
        ViewIdSanitizer.assignMissingViewIds(root)
        ViewIdSanitizer.hideSystemNavigationBars(dialogWindow, root)
        val vto = root.viewTreeObserver
        val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            ViewIdSanitizer.assignMissingViewIds(root)
        }
        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) {
                ViewIdSanitizer.hideSystemNavigationBars(dialogWindow, root)
            }
        }
        if (vto.isAlive) {
            vto.addOnGlobalLayoutListener(layoutListener)
            vto.addOnWindowFocusChangeListener(focusListener)
        }
        onDispose {
            if (vto.isAlive) {
                vto.removeOnGlobalLayoutListener(layoutListener)
                vto.removeOnWindowFocusChangeListener(focusListener)
            }
        }
    }
}
