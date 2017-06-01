package com.facebook.sdk;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * Created by andyni on 5/31/17.
 */
public class PluginUtils {

    public static void showInfo(Project project, String text) {
        Messages.showMessageDialog(String.valueOf(project), text, Messages.getInformationIcon());
    }

    public static void showError(Project project, String text) {
        Messages.showMessageDialog(String.valueOf(project), text, Messages.getErrorIcon());
    }
}

