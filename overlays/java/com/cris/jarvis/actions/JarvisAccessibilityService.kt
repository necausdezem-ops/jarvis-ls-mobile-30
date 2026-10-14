package com.cris.jarvis.actions

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Optional bridge for Android global navigation actions only. It does not read on-screen content
 * or click UI controls in other applications.
 */
class JarvisAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        active = this
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
    override fun onDestroy() {
        if (active === this) active = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_BACK = "back"
        const val ACTION_HOME = "home"
        const val ACTION_RECENTS = "recents"
        const val ACTION_NOTIFICATIONS = "notifications"
        const val ACTION_QUICK_SETTINGS = "quick_settings"
        @Volatile private var active: JarvisAccessibilityService? = null

        fun globalAction(action: String): Boolean {
            val service = active ?: return false
            val id = when (action) {
                ACTION_BACK -> AccessibilityService.GLOBAL_ACTION_BACK
                ACTION_HOME -> AccessibilityService.GLOBAL_ACTION_HOME
                ACTION_RECENTS -> AccessibilityService.GLOBAL_ACTION_RECENTS
                ACTION_NOTIFICATIONS -> AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
                ACTION_QUICK_SETTINGS -> AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS
                else -> return false
            }
            return service.performGlobalAction(id)
        }
    }
}