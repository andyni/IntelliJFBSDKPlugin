package com.facebook.sdk;

import com.intellij.openapi.project.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;

/**
 * Created by andyni on 5/31/17.
 */
public class DownloadUtils {
    private static String SDK_URL = "https://origincache.facebook.com/developers/resources/?id=facebook-android-sdk-current.zip";


    public static void downloadSDK(Project project) {
        try {
            File tempFile = File.createTempFile("facebook-sdk" + new Date().getTime(), ".zip");

            URL url = new URL(SDK_URL);

            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.getChannel().transferFrom(rbc, 0L, Long.MAX_VALUE);
            fos.close();
            rbc.close();


            project.getBaseDir().refresh(false, true);
            tempFile.delete();

        } catch (IOException e) {
            PluginUtils.showError(project, "Could not download SDK.");
            e.printStackTrace();
        }
    }
}
