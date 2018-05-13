package com.ctt.format;

import com.ctt.format.util.XmlFormatUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 可支持多组选择
 * Created by Administrator on 2018/5/12 0012.
 */
public class SayHellAction extends AnAction {

    private static final String PROPERTIES = "properties";
    private static final String DEPENDENCIES = "dependencies";
    private static final String DEPENDENCY = "dependency";
    private static final String ARTIFACTID = "artifactId";
    private static final String GROUPID = "groupId";
    private static final String VERSION = "version";
    private static final String[] REG_FIX  = {ARTIFACTID,GROUPID,VERSION};
    private static final String XML_FORMAT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";


    /**
     * 插件调用入口
     * @param e
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        //Access document, caret, and selection
        final Document document = editor.getDocument();
        final  int lastLength = editor.getDocument().getTextLength();
//        final SelectionModel selectionModel = editor.getSelectionModel();
//        final int start = selectionModel.getSelectionStart();
//        final int end = selectionModel.getSelectionEnd();
//        getMultiReplaceStr(editor);
//        final String replaceText = this.getOneReplaceStr(selectionModel.getSelectedText());

        final String replaceText = this.getOneReplaceStr(editor);
        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                document.replaceString(0, lastLength, replaceText);
            }
        });
        editor.getCaretModel().removeSecondaryCarets();
    }

    private String getOneReplaceStr(final Editor editor) {
        org.w3c.dom.Document document = null;
        try {
             org.w3c.dom.Element root = null;
             String text = editor.getDocument().getText();

             document = createStandardDocument(text);
             root = document.getDocumentElement();
            NodeList nodePros = root.getElementsByTagName(PROPERTIES);
            if(null != nodePros && nodePros.getLength()>1){
                // TODO: 2018/5/13 0013 throw exception
            }

            Node nodePro = null;
            // 添加 properties 节点
            if(null == nodePros || nodePros.getLength()<=0){
                nodePro = document.createElement(PROPERTIES);
                root.appendChild(nodePro);
            }else {
                nodePro = nodePros.item(0);
            }

            // 验证 dependencies 节点
            NodeList nodeDepens = root.getElementsByTagName(DEPENDENCIES);
            if(null == nodeDepens){
                // TODO: 2018/5/13 0013 不做任何处理 ，直接返回原数据
                return text;
            }

            final SelectionModel selectionModel = editor.getSelectionModel();
            boolean flag = selectionModel.hasSelection();

            int length = nodeDepens.getLength();
            for(int i = 0; i<length; i++){
                Node node = nodeDepens.item(i);
                NodeList nodeListDep = node.getChildNodes();
                int len = nodeListDep.getLength();
                for(int j = 0; j < len; j++) {
                    Node node2 = nodeListDep.item(j);
                    if (!DEPENDENCY.equals(node2.getNodeName())) continue;
                    if(!flag) {
                        getReplaceStr(node2,nodePro,document);
                    }
                    else {
                       boolean mark = getRealNode(node2, selectionModel.getSelectedText());
                       if(mark) getReplaceStr(node2,nodePro,document);
                    }
                }
            }
            if(null != root){
                return w3cToDom4j(getXmlString(document));
            }

        } catch (Exception e) {
                e.printStackTrace();
            }


        return null;
    }

    // 验证存在性
    private boolean getRealNode(Node node,String selectText){
        String srcStr = getStr(node);
        // 替换指定段
        org.w3c.dom.Document document = createStandardDocument(createXml(selectText));
        org.w3c.dom.Element root = document.getDocumentElement();
        NodeList nodeDepens = root.getElementsByTagName(DEPENDENCIES);
        int length = nodeDepens.getLength();
        for(int i = 0; i<length; i++){
            Node node1 = nodeDepens.item(i);
            NodeList nodeListDep = node1.getChildNodes();
            int len = nodeListDep.getLength();
            for(int j = 0; j < len; j++) {
                Node node2 = nodeListDep.item(j);
                if (!DEPENDENCY.equals(node2.getNodeName())) continue;
                String tagStr = getStr(node2);
                if(null != srcStr && null != tagStr && srcStr.trim().equals(tagStr.trim())){
                    return true;
                }
            }
        }
        return false;
    }

    private String getStr(Node node){
        NodeList nodeDepens = node.getChildNodes();
        int length = nodeDepens.getLength();
        String groupId = null, artifactId = null;
        for(int i = 0; i<length; i++){
            Node node1 = nodeDepens.item(i);
            String nodeName = node1.getNodeName();
            if("groupId".equals(nodeName)){
                groupId = node1.getTextContent();
            }
            if("artifactId".equals(nodeName)){
                artifactId = node1.getTextContent();
            }
        }

        if(null != groupId && null != artifactId && !"".equals(groupId.trim()) && !"".equals(artifactId.trim()) ){
            return groupId+"."+artifactId;
        }
        return null;
    }

    private org.w3c.dom.Document createStandardDocument(String srcXml){
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        try {
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder db= factory.newDocumentBuilder();
            try {
                return db.parse(new InputSource(new StringReader(srcXml)));
            } catch (SAXException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
                e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String createXml(String commonXml){
        return new StringBuilder().append("<ROOT><dependencies>").append(commonXml).append("</dependencies></ROOT>").toString();
    }


    private String w3cToDom4j(String rtnStr){
        if(null == rtnStr || "".equals(rtnStr)){
            return null;
        }
        String rtnCode = getDocumnet(rtnStr).asXML().replace(XML_FORMAT,"").replaceAll("\n","").replaceAll("\r","").replaceAll("\t","");
        return  XmlFormatUtil.format(rtnCode).replace(XML_FORMAT,"").substring(1);
    }

    public String getXmlString(org.w3c.dom.Document document){
        String result = null;
        StringWriter strWtr = new StringWriter();
        StreamResult strResult = new StreamResult(strWtr);
        TransformerFactory tfac = TransformerFactory.newInstance();
        try {
            javax.xml.transform.Transformer t = tfac.newTransformer();
//            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//            t.setOutputProperty(OutputKeys.INDENT, "yes");
//            t.setOutputProperty(OutputKeys.METHOD, "xml"); // xml, html,
            // text
            t.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "4");
            t.transform(new DOMSource(document.getDocumentElement()),
                    strResult);
        } catch (Exception e) {
            System.err.println("XML.toString(Document): " + e);
        }
        result = strResult.getWriter().toString();
        try {
            strWtr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    private  void getReplaceStr(Node node, Node nodePro, org.w3c.dom.Document document )  {
        if(null == node){
            return;
        }

        NodeList sonElementList = node.getChildNodes();
        // 保证顺序性
        List<KeyValue> keyValueList = new ArrayList<>();
        Node nodeVersion = null;
        int length = sonElementList.getLength();
        for (int i = 0; i<length; i++) {
            Node node2 = sonElementList.item(i);
            String nodeName = node2.getNodeName();
            if(null == nodeName || !validRight(nodeName)){
                continue;
            }
            String nodeText = node2.getTextContent();
            if("groupId".equals(nodeName)){
                keyValueList.add(0,new KeyValue(nodeName,nodeText));
            }
            if("artifactId".equals(nodeName)){
                keyValueList.add(1,new KeyValue(nodeName,nodeText.replaceAll("-",".").replaceAll("-",".")));
            }
            if("version".equals(nodeName)){
                nodeVersion = node2;
                keyValueList.add(2,new KeyValue(nodeName,nodeText));
            }
        }
        String versionStr = keyValueList.get(0).value+"."+keyValueList.get(1).value+".version";
        String replace$Str = "${"+versionStr+"}";
        if(null != nodeVersion) {
            // 在版本更改之前赋值
            getNodePro(nodePro,versionStr,nodeVersion.getTextContent(),document);
            nodeVersion.setTextContent(replace$Str);
        }
    }

    private void getNodePro(Node nodePro,String key,String value, org.w3c.dom.Document document){
        NodeList nodeList = nodePro.getChildNodes();
        int length = nodeList.getLength();
        boolean flag = false;
        for(int i = 0; i < length; i++){
            Node node = nodeList.item(i);
            if(key.equals(node.getNodeName())){
                node.setTextContent(value);
                flag = true;
                break;
            }
        }

        if(!flag){
            Node subNode = document.createElement(key);
            subNode.setTextContent(value);
            nodePro.appendChild(subNode);
        }
    }

    private org.dom4j.Document getDocumnet(String selectText){
        SAXReader saxReader = new SAXReader();
        org.dom4j.Document document = null;
        try {
           return saxReader.read(new ByteArrayInputStream(selectText.getBytes()));
        } catch (DocumentException e) {
            e.getMessage();
        }

        return null;
    }

    private boolean validRight(String nodeName) {
        if(Arrays.asList(REG_FIX).contains(nodeName)){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    class KeyValue{
        public String column;
        public String value;

        public KeyValue(String column,String value){
            this.column = column;
            this.value = value;
        }

    }

}
