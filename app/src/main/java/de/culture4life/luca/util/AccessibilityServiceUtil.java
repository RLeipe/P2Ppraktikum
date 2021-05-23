package de.culture4life.luca.util;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Helper for AccessibilityServices
 */
public class AccessibilityServiceUtil {

    private static final String GOOGLE_TALKBACK_PACKAGE = "com.google.android.marvin.talkback";

    /**
     * This method checks if Google Talkback is enabled by using the {@link AccessibilityManager}.
     */
    public static boolean isGoogleTalkbackActive(@NonNull Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager == null) {
            return false;
        }
        List<AccessibilityServiceInfo> accessibilityServiceInfoList = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN);
        for (AccessibilityServiceInfo accessibilityServiceInfo : accessibilityServiceInfoList) {
            if (GOOGLE_TALKBACK_PACKAGE.equals(accessibilityServiceInfo.getResolveInfo().serviceInfo.processName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Speak the talkbackText as feedback to the user when Google Talkback is active
     *
     * @param context      Context for getting the AccessibilityManager
     * @param talkbackText text to speak
     */
    public static void speak(@NonNull Context context, @NonNull String talkbackText) {
        if (!isGoogleTalkbackActive(context)) {
            return;
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityEvent accessibilityEvent = AccessibilityEvent.obtain();
        accessibilityEvent.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
        accessibilityEvent.getText().add(talkbackText);
        accessibilityManager.sendAccessibilityEvent(accessibilityEvent);
    }

}
