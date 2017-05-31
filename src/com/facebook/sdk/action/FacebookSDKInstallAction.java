package com.facebook.sdk.action;

import com.facebook.sdk.DownloadUtils;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;

public class FacebookSDKInstallAction extends AnAction {

    public FacebookSDKInstallAction() {
        super("Install Facebook SDK...");
        // super("Install Facebook SDK...","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        DownloadUtils.downloadSDK(project);
    }
}
