package com.facebook.sdk;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by andyni on 5/31/17.
 */
public class DownloadUtils {
    private static final String SDK_URL = "https://origincache.facebook.com/developers/resources/?id=facebook-android-sdk-current.zip";
    private static final int BUFFER_SIZE = 4096;

    public static boolean installSdk(Project project) {
        VirtualFile baseDir = project.getBaseDir();
        VirtualFile gradleFile = findFile(baseDir, "build.gradle");
        if (gradleFile != null) {
            final Document document = FileDocumentManager.getInstance().getDocument(gradleFile);
            new WriteCommandAction.Simple(project) {
                @Override protected void run() throws Throwable {
                    document.setText(formatGradle(document.getText()));
                }
            }.execute();

            return true;
        }

        // Fallback to download
        try {
            File tempFile = File.createTempFile("facebook-sdk" + new Date().getTime(), ".zip");

            downloadZip(project, tempFile);
            unzipSdkToRoot(project, tempFile);

            tempFile.delete();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String formatGradle(String text) {
        Pattern repository_pattern = Pattern.compile("repositories(\\s)*\\{([^\\}]+)\\}");
        Pattern dependencies_pattern = Pattern.compile("dependencies(\\s)*\\{([^\\}]+)\\}");
        Matcher repository_matcher = repository_pattern.matcher(text);
        Matcher dependencies_matcher = dependencies_pattern.matcher(text);

        if (repository_matcher.find()) {
            String repositories = repository_matcher.group(2);
            if (repositories.indexOf("mavenCentral()") == -1) {
                text = text.replace(repositories, repositories + "    mavenCentral()\n");
            }
        }

        if (dependencies_matcher.find()) {
            String dependencies = dependencies_matcher.group(2);
            if (dependencies.indexOf("com.facebook.android:facebook-android-sdk") == -1) {
                text = text.replace(dependencies, dependencies + "    compile 'com.facebook.android:facebook-android-sdk:[4,5)'\n");
            }
        }

        return text;
    }

    private static VirtualFile findFile(VirtualFile dir, String fileName) {
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

    private static void unzipSdkToRoot(Project project, File zipFile) {
        String rootPath = project.getBasePath();
        File destDir = new File(rootPath);
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        try {
            String zipPath = zipFile.getAbsolutePath();
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipPath));
            ZipEntry entry = zipInputStream.getNextEntry();

            while (entry != null) {
                String filePath = rootPath + File.separator + entry.getName();
                if (entry.isDirectory()) {
                    File dir = new File(filePath);
                    if (!dir.mkdir()) {
                        throw new IOException("Could not create directory.");
                    }
                } else {
                    FileOutputStream fOutput = new FileOutputStream(filePath);
                    byte[] bytesIn = new byte[BUFFER_SIZE];
                    int read = 0;
                    while ((read = zipInputStream.read(bytesIn)) != -1) {
                        fOutput.write(bytesIn, 0, read);
                    }
                    fOutput.close();
                }
                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }

            zipInputStream.close();

            project.getBaseDir().refresh(false, true);
            System.out.println("Unzipped SDK to: " + rootPath);
        } catch (IOException e) {
            System.out.println("SDK unzip failed.");
            e.printStackTrace();
        }
    }

    private static void downloadZip(Project project, File file) {
        try {
            URL url = new URL(SDK_URL);

            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0L, Long.MAX_VALUE);
            fos.close();
            rbc.close();
            System.out.println("Downloaded zipped SDK to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("SDK download failed");
            e.printStackTrace();
        }
    }
}
