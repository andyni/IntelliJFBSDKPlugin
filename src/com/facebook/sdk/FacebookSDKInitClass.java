package com.facebook.sdk;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;


public class FacebookSDKInitClass extends AnAction {
    public FacebookSDKInitClass() {
        super("Configure Facebook SDK...");
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        String txt= Messages.showInputDialog(project, "What is your Facebook app ID?", "Input your app ID", Messages.getQuestionIcon());
        Messages.showMessageDialog(project, "Your Facebook app ID is: " + txt, "Information", Messages.getInformationIcon());
    }
}
