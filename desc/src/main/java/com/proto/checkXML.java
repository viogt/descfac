package com.proto;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.StringReader;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class checkXML {

    private static Document parseXmlString(byte[] cnt) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(new String(cnt, StandardCharsets.UTF_8))));
    }

    private static void removeNode(Node n) {
        Node parent = n.getParentNode();
        if (parent != null) parent.removeChild(n);
    }

    private static String serializeXmlDocument(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    private static void inReport(String txt, String vl) {
        int ix = Integer.parseInt(txt.substring(
            txt.indexOf("/sheet") + 6, txt.indexOf(".xml"))) - 1;
        checkZip.report[ix][2] = vl;
    }

    public static byte[] checkSheet(String xmlName, byte[] cnt) throws Exception {
        Document doc = parseXmlString(cnt);
        NodeList itemList = doc.getElementsByTagName("sheetProtection");
        if (itemList.getLength() > 0) {
            inReport(xmlName, "<b>Protected</b>");
            System.out.println("> "+ xmlName +" SheetProtection found and removed.");
            removeNode(itemList.item(0));
            checkZip.hasProtection = true;
        }
        String modifiedXml = serializeXmlDocument(doc);
        return modifiedXml.getBytes("UTF-8");
    }

    public static byte[] checkBook(byte[] cnt) throws Exception {
        Document doc = parseXmlString(cnt);
        NodeList itemList = doc.getElementsByTagName("workbookProtection");
        if (itemList.getLength() > 0) {
            System.out.println("> WorkbookProtection found and removed.");
            checkZip.hasProtection = true;
            removeNode(itemList.item(0));
        }
        itemList = doc.getElementsByTagName("sheet");
        int Len = itemList.getLength();
        checkZip.report = new String[Len][3];
        for (int i = 0; i < Len; i++) {
            Element el = (Element) itemList.item(i);
            checkZip.report[i][0] = el.getAttribute("name");
            checkZip.report[i][1] = el.hasAttribute("state") ? "<b lilac>Hidden</b>" : "—";
            checkZip.report[i][2] = "—";
            if (el.hasAttribute("state")) {
                el.removeAttribute("state");
                checkZip.hasProtection = true;
                System.out.println("> \"" + el.getAttribute("name") + "\" Hiddden Attribute removed.");
            }
        }
        String modifiedXml = serializeXmlDocument(doc);
        return modifiedXml.getBytes("UTF-8");
    }
}