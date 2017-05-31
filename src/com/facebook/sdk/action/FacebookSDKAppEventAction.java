package com.facebook.sdk.action;

import com.intellij.openapi.actionSystem.*;

public class FacebookSDKAppEventAction extends AnAction {

    public FacebookSDKAppEventAction() {
        super("Generate App Event Configuration...");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
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
