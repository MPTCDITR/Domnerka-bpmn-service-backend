package com.domnerka.camundautillity;

import lombok.experimental.UtilityClass;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

@UtilityClass
public class BPMNModificationUtility {

    public static String insertIntoEndEventSendMsgNotification(String bpmn)
            throws ParserConfigurationException, IOException, SAXException, TransformerException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(bpmn)));

        Element rootElement = doc.getDocumentElement();

        // Add the camunda namespace if it doesn't exist
        if (!rootElement.hasAttribute("xmlns:camunda")) {
            rootElement.setAttribute("xmlns:camunda", "http://camunda.org/schema/1.0/bpmn");
        }

        doc.getDocumentElement().normalize();
        NodeList endEvents = doc.getElementsByTagName("bpmn:endEvent");
        for (int i = 0; i < endEvents.getLength(); i++) {
            Node endEvent = endEvents.item(i);
            // if not already contain
            if (!containSendMsg(endEvent)) {
                // Create bpmn:messageEventDefinition
                Element messageEventDefinition = doc.createElement("bpmn:messageEventDefinition");
                messageEventDefinition.setAttribute("id", "MessageEventDefinition_" + i);
                messageEventDefinition.setAttribute("camunda:class",
                        "com.domerka.workflow.camundautillity.EndProcessMessageHandler");
                // Add to bpmn:endEvent
                endEvent.appendChild(messageEventDefinition);
            }
        }

        return convertDocumentToString(doc);
    }

    public static String convertDocumentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();

        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();
        } catch (TransformerException e) {
            throw new TransformerException(e);
        }
    }

    private static boolean containSendMsg(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeName().equals("bpmn:messageEventDefinition")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a description to the given BPMN XML.
     * <p>
     * If the description is null or empty, the original BPMN XML is returned.
     * Otherwise, the description is added as a bpmn:documentation element inside
     * the process tag.
     * If a bpmn:documentation element already exists, its content is replaced with
     * the new description.
     *
     * @param bpmnXml     the original BPMN XML
     * @param description the description to be added
     * @return the modified BPMN XML with the added description
     */
    public static String addDescriptionToBpmnXml(String bpmnXml, String description) {
        if (description == null || description.isEmpty()) {
            return bpmnXml;
        }

        String processTag = bpmnXml.contains("<bpmn:process") ? "<bpmn:process" : "<process";
        int processTagPosition = bpmnXml.indexOf(processTag);

        if (processTagPosition == -1) {
            return bpmnXml;
        }

        String documentationTag = "<bpmn:documentation>";
        String escapedDescription = StringEscapeUtils.escapeXml10(description);
        String newDocumentationXml = String.format("\n  <bpmn:documentation>%s</bpmn:documentation>",
                escapedDescription);

        return Optional.of(bpmnXml.indexOf(documentationTag, processTagPosition))
                .filter(start -> start != -1)
                .map(start -> {
                    int end = bpmnXml.indexOf("</bpmn:documentation>", start) + "</bpmn:documentation>".length();
                    return new StringBuilder(bpmnXml).replace(start, end, newDocumentationXml).toString();
                })
                .orElseGet(() -> {
                    int endOfOpeningTag = bpmnXml.indexOf(">", processTagPosition);
                    return endOfOpeningTag == -1 ? bpmnXml
                            : new StringBuilder(bpmnXml).insert(endOfOpeningTag + 1, newDocumentationXml).toString();
                });
    }
}
