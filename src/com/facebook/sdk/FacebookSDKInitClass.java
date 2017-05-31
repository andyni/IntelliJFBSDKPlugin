package com.facebook.sdk;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import javax.annotation.Nullable;

public class FacebookSDKInitClass extends AnAction {
    public FacebookSDKInitClass() {
        super("Configure Facebook SDK...");
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        /*
        String txt = Messages.showInputDialog(project, "What is your Facebook app ID?", "Input your app ID", Messages.getQuestionIcon());
        Messages.showMessageDialog(project, "Your Facebook app ID is: " + txt, "Information", Messages.getInformationIcon());
        */

        VirtualFile baseDir = project.getBaseDir();
        VirtualFile manifestFile = findFile(baseDir, "AndroidManifest.xml");
        if (manifestFile == null) {
            return;
        }
    }

    private static @Nullable VirtualFile findFile(VirtualFile dir, String fileName) {
        VirtualFile virtualFile = dir.findChild(fileName);
        if (virtualFile != null) {
            return virtualFile;
        }
        for (VirtualFile child : dir.getChildren()) {
            if (child.isDirectory()) {
                VirtualFile childVirtualFile = findFile(child, fileName);
                if (childVirtualFile != null) {
                    return childVirtualFile;
                }
            }
        }
        return null;
    }

    private static boolean addAppId(Project project, VirtualFile file, String appId) {
        final Document document = FileDocumentManager.getInstance().getDocument(file);
        new WriteCommandAction.Simple(project) {
            @Override protected void run() throws Throwable {
                String text = document.getText();
                document.setText(formatManifestWithAppId(text));
            }
        }.execute();
        return false;
    }

    private static String formatManifestWithAppId(String manifestText) {
        
    }
}
