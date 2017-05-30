package com.facebook.sdk;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

public class FacebookSDKRegistration implements ApplicationComponent {

    @NotNull
    public String getComponentName() {
        return "FacebookSDKRegistration";
    }

    public void initComponent() {
        ActionManager am = ActionManager.getInstance();
        FacebookSDKInitClass facebookSDKAction = new FacebookSDKInitClass();

        am.registerAction("FacebookSDKAction", facebookSDKAction);

        DefaultActionGroup windowM = (DefaultActionGroup) am.getAction("ToolsMenu");
        windowM.addSeparator();
        windowM.add(facebookSDKAction);
    }

    // Disposes system resources.
    public void disposeComponent() {
    }
}