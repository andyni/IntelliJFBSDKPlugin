package com.facebook.sdk.action;

import com.facebook.sdk.DownloadUtils;

import com.facebook.sdk.PluginUtils;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

public class FacebookSDKInstallAction extends AnAction {

    public FacebookSDKInstallAction() {
        super("Install Facebook SDK...");
        // super("Install Facebook SDK...","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Application application = ApplicationManager.getApplication();
        Project project = e.getProject();

        Runnable downloadSDK = new Runnable() {
            @Override
            public void run() {
                if (DownloadUtils.installSdk(project)) {
                    application.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            PluginUtils.showInfo(project, "SDK Integration completed.");
                        }
                    });
                } else {
                    application.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            PluginUtils.showError(project, "SDK Integration failed.");
                        }
                    });
                }
            }
        };

        new Thread(downloadSDK).start();
    }
}
