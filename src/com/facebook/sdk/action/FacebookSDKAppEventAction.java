package com.facebook.sdk.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

public class FacebookSDKAppEventAction extends AnAction {

    private static String ANDROID_NS_URI = "http://schemas.android.com/apk/res/android";

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
        String appId = Messages.showInputDialog(
                project,
                "What is your Facebook application ID?",
                "Input your application ID",
                Messages.getQuestionIcon());
        addAppId(project, manifestFile, appId);
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

    private static boolean addAppId(Project project, VirtualFile file, String appId) {
        final com.intellij.openapi.editor.Document document = FileDocumentManager.getInstance().getDocument(file);
        new WriteCommandAction.Simple(project) {
            @Override protected void run() throws Throwable {
                document.setText(formatManifest(document.getText(), appId));
            }
        }.execute();
        return false;
    }

    private static String formatManifest(String manifestText, String appId) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            Document doc = docFactory.newDocumentBuilder().parse(new InputSource(new StringReader(manifestText)));
            Element manifestRoot = doc.getDocumentElement();

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            xpath.setNamespaceContext(new AndroidNamespaceContext());

            boolean isInternetPermissionDeclared = (boolean) xpath.evaluate(
                    "/manifest/uses-permission[@name='android.permission.INTERNET']",
                    doc,
                    XPathConstants.BOOLEAN);
            if (!isInternetPermissionDeclared) {
                Element internetPermissionElement = doc.createElement("uses-permission");
                internetPermissionElement.setAttributeNS(ANDROID_NS_URI, "android:name", "android.permission.INTERNET");
                manifestRoot.appendChild(internetPermissionElement);
            }

            boolean isAppIdDeclared = (boolean) xpath.evaluate(
                    "/manifest/application/meta-data[@name='com.facebook.sdk.ApplicationId']",
                    doc,
                    XPathConstants.BOOLEAN);
            if (!isAppIdDeclared) {
                Element applicationElement = (Element) xpath.evaluate("/manifest/application", doc, XPathConstants.NODE);
                Element metadataElement = doc.createElement("meta-data");
                metadataElement.setAttributeNS(ANDROID_NS_URI, "android:name", "com.facebook.sdk.ApplicationId");
                metadataElement.setAttributeNS(ANDROID_NS_URI, "android:value", appId);
                applicationElement.appendChild(metadataElement);
            }

            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return manifestText;
    }

    private static class AndroidNamespaceContext implements NamespaceContext {

        @Override
        public String getNamespaceURI(String prefix) {
            return prefix.equals("android") ? ANDROID_NS_URI : null;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return null;
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }
    }
}
