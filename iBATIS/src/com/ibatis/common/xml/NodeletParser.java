package com.ibatis.common.xml;

import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * The NodeletParser is a callback based parser similar to SAX.  The big
 * difference is that rather than having a single callback for all nodes,
 * the NodeletParser has a number of callbacks mapped to
 * various nodes.   The callback is called a Nodelet and it is registered
 * with the NodeletParser against a specific XPath.
 */
public class NodeletParser {
    private static my.Debug DEBUG = new my.Debug(my.Debug.NodeletParser);//�Ҽ��ϵ�

    private Map letMap = new HashMap();

    private boolean validation;
    private EntityResolver entityResolver;

    /**
     * Registers a nodelet for the specified XPath.  Current XPaths supported
     * are:
     * <ul>
     * <li> Text Path - /rootElement/childElement/text()
     * <li> Attribute Path  - /rootElement/childElement/@theAttribute
     * <li> Element Path - /rootElement/childElement/theElement
     * <li> All Elements Named - //theElement
     * </ul>
     */
    public void addNodelet(String xpath, Nodelet nodelet) {
        DEBUG.P(this, "addNodelet(2)");
        DEBUG.P("xpath=" + xpath);

        letMap.put(xpath, nodelet);

        DEBUG.P("letMap.size()=" + letMap.size());
        DEBUG.P(0, this, "addNodelet(2)");
    }

    /**
     * Begins parsing from the provided Reader.
     */
    public void parse(Reader reader) throws NodeletException {
        try {//�Ҽ��ϵ�
            DEBUG.P(this, "parse(1)");
            DEBUG.P("validation=" + validation);
            try {
                //����org.w3c.dom.Document
                Document doc = createDocument(reader);

                //Node node = doc.getLastChild();
                //DEBUG.P("node="+node);
                //parse(node);

                parse(doc.getLastChild());
            } catch (Exception e) {
                throw new NodeletException("Error parsing XML.  Cause: " + e, e);
            }

        } finally {//�Ҽ��ϵ�
            DEBUG.P(0, this, "parse(1)");
        }
    }

    //�����淽������
    public void parse(InputStream inputStream) throws NodeletException {
        try {
            //����org.w3c.dom.Document
            Document doc = createDocument(inputStream);
            parse(doc.getLastChild());
        } catch (Exception e) {
            throw new NodeletException("Error parsing XML.  Cause: " + e, e);
        }
    }

    /**
     * Begins parsing from the provided Node.
     */
    public void parse(Node node) {
        DEBUG.P(this, "parse(Node node)");
        DEBUG.P("node=" + node);

        Path path = new Path();
        processNodelet(node, "/");
        process(node, path);

        DEBUG.P(0, this, "parse(Node node)");
    }

    /**
     * A recursive method that walkes the DOM tree, registers XPaths and
     * calls Nodelets registered under those XPaths.
     */
    private void process(Node node, Path path) {
        DEBUG.P(this, "process(2)");
        DEBUG.P("path=" + path);
        DEBUG.P("(node instanceof Element)=" + (node instanceof Element));
        DEBUG.P("(node instanceof Text)=" + (node instanceof Text));

        //��@��text()�����·��ʵ�����ã�
        //SqlMapConfigParser��SqlMapParser��û����addNodelet�м��������·��
        if (node instanceof Element) {
            // Element
            String elementName = node.getNodeName();

            DEBUG.P("elementName=" + elementName);

            path.add(elementName);
            processNodelet(node, path.toString());
            processNodelet(node, new StringBuffer("//").append(elementName).toString());

            // Attribute
            NamedNodeMap attributes = node.getAttributes();
            int n = attributes.getLength();

            DEBUG.P("n=" + n);

            //����Ԫ������
            for (int i = 0; i < n; i++) {
                Node att = attributes.item(i);
                String attrName = att.getNodeName();
                path.add("@" + attrName);
                processNodelet(att, path.toString());
                processNodelet(node, new StringBuffer("//@").append(attrName).toString());
                path.remove();
            }

            // Children
            NodeList children = node.getChildNodes();
            DEBUG.P("children.getLength()=" + children.getLength());
            for (int i = 0; i < children.getLength(); i++) {
                process(children.item(i), path);
            }
            path.add("end()");
            processNodelet(node, path.toString());
            path.remove();
            path.remove();
        } else if (node instanceof Text) {
            // Text
            path.add("text()");
            processNodelet(node, path.toString());
            processNodelet(node, "//text()");
            path.remove();
        }

        DEBUG.P(0, this, "process(2)");
    }

    private void processNodelet(Node node, String pathString) {
        DEBUG.P(this, "processNodelet(2)");
        DEBUG.P("node=" + node);
        DEBUG.P("pathString=" + pathString);

        Nodelet nodelet = (Nodelet) letMap.get(pathString);

        DEBUG.P("nodelet=" + nodelet);
        if (nodelet != null) {
            try {
                nodelet.process(node);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing XPath '" + pathString + "'.  Cause: " + e, e);
            }
        }

        DEBUG.P(0, this, "processNodelet(2)");
    }

    /**
     * Creates a JAXP Document from a reader.
     */
    private Document createDocument(Reader reader) throws ParserConfigurationException, FactoryConfigurationError, SAXException,
            IOException {
        try {//�Ҽ��ϵ�
            DEBUG.P(this, "createDocument(1)");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validation);

            factory.setNamespaceAware(false);
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(false);
            factory.setCoalescing(false);
            factory.setExpandEntityReferences(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(entityResolver);
            builder.setErrorHandler(new ErrorHandler() {
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                public void warning(SAXParseException exception) throws SAXException {
                }
            });

            return builder.parse(new InputSource(reader));

        } finally {//�Ҽ��ϵ�
            DEBUG.P(0, this, "createDocument(1)");
        }
    }

    /**
     * Creates a JAXP Document from an InoutStream.
     */
    //�����淽�����ƣ�ֻ��new InputSource(xxx)�Ĳ���ͬ
    private Document createDocument(InputStream inputStream) throws ParserConfigurationException, FactoryConfigurationError,
            SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(validation);

        factory.setNamespaceAware(false);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(false);
        factory.setCoalescing(false);
        factory.setExpandEntityReferences(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(entityResolver);
        builder.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void warning(SAXParseException exception) throws SAXException {
            }
        });

        return builder.parse(new InputSource(inputStream));
    }

    public void setValidation(boolean validation) {
        this.validation = validation;
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }

    /**
     * Inner helper class that assists with building XPath paths.
     * <p/>
     * Note:  Currently this is a bit slow and could be optimized.
     */
    private static class Path {

        private List nodeList = new ArrayList();

        public Path() {
        }

        //δʹ��
        public Path(String path) {
            DEBUG.P(this, "Path(1)");
            DEBUG.P("path=" + path);
            StringTokenizer parser = new StringTokenizer(path, "/", false);
            while (parser.hasMoreTokens()) {
                nodeList.add(parser.nextToken());
            }
            DEBUG.P(0, this, "Path(1)");
        }

        public void add(String node) {
            nodeList.add(node);
        }

        public void remove() {
            nodeList.remove(nodeList.size() - 1);
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer("/");
            for (int i = 0; i < nodeList.size(); i++) {
                buffer.append(nodeList.get(i));
                if (i < nodeList.size() - 1) {
                    buffer.append("/");
                }
            }
            return buffer.toString();
        }
    }

}