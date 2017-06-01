package com.facebook.sdk;

import com.intellij.openapi.project.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by andyni on 5/31/17.
 */
public class DownloadUtils {
    private static final String SDK_URL = "https://origincache.facebook.com/developers/resources/?id=facebook-android-sdk-current.zip";
    private static final int BUFFER_SIZE = 4096;

    public static boolean installSdk(Project project) {
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
