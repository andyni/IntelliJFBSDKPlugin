package com.facebook.sdk.action;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
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
        VirtualFile stringsFile = findFile(baseDir, "strings.xml");
        if (manifestFile == null) {
            return;
        }

        String appId = Messages.showInputDialog(
                project,
                "What is your Facebook application ID?",
                "Input your application ID",
                Messages.getQuestionIcon());
        addAppId(project, manifestFile, appId);
        addAppIdToStrings(project, stringsFile, appId);

        addActivateApp(project, manifestFile);

        Messages.showMessageDialog("The Facebook SDK has been added.", "Success", Messages.getInformationIcon());
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
        if (document == null) {
            return false;
        }
        new WriteCommandAction.Simple(project) {
            @Override protected void run() throws Throwable {
                document.setText(formatManifest(document.getText(), appId));
            }
        }.execute();
        return true;
    }

    private static boolean addAppIdToStrings(Project project, VirtualFile file, String appId) {
        final com.intellij.openapi.editor.Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return false;
        }
        new WriteCommandAction.Simple(project) {
            @Override protected void run() throws Throwable {
                document.setText(formatStrings(document.getText(), appId));
            }
        }.execute();
        return true;
    }

    private static String formatStrings(String stringsText, String appId) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            Document doc = docFactory.newDocumentBuilder().parse(new InputSource(new StringReader(stringsText)));
            Element stringsRoot = doc.getDocumentElement();

            Element appIdElement = doc.createElement("string");
            appIdElement.setAttribute("name", "facebook_app_id");
            appIdElement.setTextContent(appId);
            stringsRoot.appendChild(appIdElement);

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
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return stringsText;
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
                metadataElement.setAttributeNS(ANDROID_NS_URI, "android:value", "@string/facebook_app_id");
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

    private static boolean addActivateApp(Project project, VirtualFile manifestFile) {
        VirtualFile mainActivityVirtualFile = findMainActivity(project, manifestFile);
        if (mainActivityVirtualFile == null) {
            return false;
        }
        PsiJavaFile mainActivityPsi = (PsiJavaFile) PsiManager.getInstance(project).findFile(mainActivityVirtualFile);
        ASTNode[] children =
                mainActivityPsi.getNode().findChildByType(JavaStubElementTypes.IMPORT_LIST).getChildren(null);
        PsiJavaCodeReferenceElement prevImport = null;
        for (ASTNode child : children) {
            if (child.getElementType() != JavaStubElementTypes.IMPORT_STATEMENT) {
                continue;
            }
            PsiJavaCodeReferenceElement importedReference = null;
            for (ASTNode grandchild : child.getChildren(null)) {
                if (grandchild instanceof PsiJavaCodeReferenceElement) {
                    importedReference = (PsiJavaCodeReferenceElement) grandchild;
                    break;
                }
            }
            int compared = importedReference.getText().compareTo("com.facebook.FacebookSdk");
            if (compared < 0) {
                prevImport = importedReference;
                continue;
            } else if (compared == 0) {
                prevImport = importedReference;
                break;
            } else {
                break;
            }
        }
        if (prevImport == null) {
            // TODO: insert import wherever
        }
        if (!prevImport.getText().equals("com.facebook.FacebookSdk")) {
            // TODO: insert import
        }

        PsiClass[] classes = mainActivityPsi.getClasses();
        PsiClass mainActivityClass = null;
        for (PsiClass _class : classes) {
            if (mainActivityPsi.getName().contains(_class.getName())) {
                mainActivityClass = _class;
            }
        }
        if (mainActivityClass == null) {
            return false;
        }
        PsiMethod[] onCreateMethods = mainActivityClass.findMethodsByName("onCreate", false);
        if (onCreateMethods.length == 0) {
            return false;
        }

        PsiMethod onCreateMethod = onCreateMethods[0];
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        PsiStatement sdkInitializeStatement = elementFactory.createStatementFromText(
                "com.facebook.FacebookSdk.sdkInitialize(getApplicationContext());",
                onCreateMethod);
        PsiStatement activateAppStatement = elementFactory.createStatementFromText(
                "com.facebook.appevents.AppEventsLogger.activateApp(this);",
                onCreateMethod);
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
        new WriteCommandAction.Simple(project) {
            @Override protected void run() throws Throwable {
                styleManager.optimizeImports(mainActivityPsi);
                styleManager.shortenClassReferences(sdkInitializeStatement);
                styleManager.shortenClassReferences(activateAppStatement);
                onCreateMethod.addAfter(sdkInitializeStatement, onCreateMethod.getBody().getLastBodyElement());
                onCreateMethod.addAfter(activateAppStatement, onCreateMethod.getBody().getLastBodyElement());
                mainActivityVirtualFile.setBinaryContent(mainActivityPsi.getText().getBytes());
            }
        }.execute();
        return true;
    }

    private static VirtualFile findMainActivity(Project project, VirtualFile manifestFile) {
        String activityName = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            String manifestText = FileDocumentManager.getInstance().getDocument(manifestFile).getText();
            Document manifestDoc = docFactory.newDocumentBuilder().parse(new InputSource(new StringReader(manifestText)));

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            xpath.setNamespaceContext(new AndroidNamespaceContext());
            activityName = (String) xpath.evaluate(
                    "manifest/application/activity/intent-filter/category[@name='android.intent.category.LAUNCHER']/ancestor::activity/@name",
                    manifestDoc,
                    XPathConstants.STRING);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        if (activityName == null || activityName.isEmpty()) {
            return null;
        }
        activityName = activityName.substring(activityName.lastIndexOf('.') + 1);
        VirtualFile virtualFile = findFile(project.getBaseDir(), activityName + ".java");
        return virtualFile;
    }

    private static String formatActivity(String activityText) {
        return activityText;
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
