/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.common.beans;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ibatis.common.resources.Resources;

/**
 * A Probe implementation for working with DOM objects
 */
public class DomProbe extends BaseProbe {

    public String[] getReadablePropertyNames(Object object) {
        List props = new ArrayList();
        Element e = resolveElement(object);
        NodeList nodes = e.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            props.add(nodes.item(i).getNodeName());
        }
        return (String[]) props.toArray(new String[props.size()]);
    }

    public String[] getWriteablePropertyNames(Object object) {
        List props = new ArrayList();
        Element e = resolveElement(object);
        NodeList nodes = e.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            props.add(nodes.item(i).getNodeName());
        }
        return (String[]) props.toArray(new String[props.size()]);
    }

    public Class getPropertyTypeForSetter(Object object, String name) {
        Element e = findNestedNodeByName(resolveElement(object), name, false);
        //todo alias types, don't use exceptions like this
        try {
            return Resources.classForName(e.getAttribute("type"));
        } catch (ClassNotFoundException e1) {
            return Object.class;
        }
    }

    public Class getPropertyTypeForGetter(Object object, String name) {
        Element e = findNestedNodeByName(resolveElement(object), name, false);
        //todo alias types, don't use exceptions like this
        try {
            return Resources.classForName(e.getAttribute("type"));
        } catch (ClassNotFoundException e1) {
            return Object.class;
        }
    }

    public boolean hasWritableProperty(Object object, String propertyName) {
        return findNestedNodeByName(resolveElement(object), propertyName, false) != null;
    }

    public boolean hasReadableProperty(Object object, String propertyName) {
        return findNestedNodeByName(resolveElement(object), propertyName, false) != null;
    }

    public Object getObject(Object object, String name) {
        Object value = null;
        Element element = findNestedNodeByName(resolveElement(object), name, false);
        if (element != null) {
            value = getElementValue(element);
        }
        return value;
    }

    public void setObject(Object object, String name, Object value) {
        Element element = findNestedNodeByName(resolveElement(object), name, true);
        if (element != null) {
            setElementValue(element, value);
        }
    }

    protected void setProperty(Object object, String property, Object value) {
        Element element = findNodeByName(resolveElement(object), property, 0, true);
        if (element != null) {
            setElementValue(element, value);
        }
    }

    protected Object getProperty(Object object, String property) {
        Object value = null;
        Element element = findNodeByName(resolveElement(object), property, 0, false);
        if (element != null) {
            value = getElementValue(element);
        }
        return value;
    }

    private Element resolveElement(Object object) {
        Element element = null;
        if (object instanceof Document) {
            element = (Element) ((Document) object).getLastChild();
        } else if (object instanceof Element) {
            element = (Element) object;
        } else {
            throw new ProbeException("An unknown object type was passed to DomProbe.  Must be a Document.");
        }
        return element;
    }

    private void setElementValue(Element element, Object value) {
        CharacterData data = null;

        Element prop = element;

        if (value instanceof Collection) {
            Iterator items = ((Collection) value).iterator();
            while (items.hasNext()) {
                Document valdoc = (Document) items.next();
                NodeList list = valdoc.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    Node newNode = element.getOwnerDocument().importNode(list.item(i), true);
                    element.appendChild(newNode);
                }
            }
        } else if (value instanceof Document) {
            Document valdoc = (Document) value;
            Node lastChild = valdoc.getLastChild();
            NodeList list = lastChild.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node newNode = element.getOwnerDocument().importNode(list.item(i), true);
                element.appendChild(newNode);
            }
        } else if (value instanceof Element) {
            Node newNode = element.getOwnerDocument().importNode((Element) value, true);
            element.appendChild(newNode);
        } else {
            // Find text child element
            NodeList texts = prop.getChildNodes();
            if (texts.getLength() == 1) {
                Node child = texts.item(0);
                if (child instanceof CharacterData) {
                    // Use existing text.
                    data = (CharacterData) child;
                } else {
                    // Remove non-text, add text.
                    prop.removeChild(child);
                    Text text = prop.getOwnerDocument().createTextNode(String.valueOf(value));
                    prop.appendChild(text);
                    data = text;
                }
            } else if (texts.getLength() > 1) {
                // Remove all, add text.
                for (int i = texts.getLength() - 1; i >= 0; i--) {
                    prop.removeChild(texts.item(i));
                }
                Text text = prop.getOwnerDocument().createTextNode(String.valueOf(value));
                prop.appendChild(text);
                data = text;
            } else {
                // Add text.
                Text text = prop.getOwnerDocument().createTextNode(String.valueOf(value));
                prop.appendChild(text);
                data = text;
            }
            data.setData(String.valueOf(value));
        }

        // Set type attribute
        //prop.setAttribute("type", value == null ? "null" : value.getClass().getName());

    }

    private Object getElementValue(Element element) {
        StringBuffer value = null;

        Element prop = element;

        if (prop != null) {
            // Find text child elements
            NodeList texts = prop.getChildNodes();
            if (texts.getLength() > 0) {
                value = new StringBuffer();
                for (int i = 0; i < texts.getLength(); i++) {
                    Node text = texts.item(i);
                    if (text instanceof CharacterData) {
                        value.append(((CharacterData) text).getData());
                    }
                }
            }
        }

        //convert to proper type
        //value = convert(value.toString());

        if (value == null) {
            return null;
        } else {
            return String.valueOf(value);
        }
    }

    private Element findNestedNodeByName(Element element, String name, boolean create) {
        Element child = element;

        StringTokenizer parser = new StringTokenizer(name, ".", false);
        while (parser.hasMoreTokens()) {
            String childName = parser.nextToken();
            if (childName.indexOf('[') > -1) {
                String propName = childName.substring(0, childName.indexOf('['));
                int i = Integer.parseInt(childName.substring(childName.indexOf('[') + 1, childName.indexOf(']')));
                child = findNodeByName(child, propName, i, create);
            } else {
                child = findNodeByName(child, childName, 0, create);
            }
            if (child == null) {
                break;
            }
        }

        return child;
    }

    private Element findNodeByName(Element element, String name, int index, boolean create) {
        Element prop = null;

        // Find named property element
        NodeList propNodes = element.getElementsByTagName(name);
        if (propNodes.getLength() > index) {
            prop = (Element) propNodes.item(index);
        } else {
            if (create) {
                for (int i = 0; i < index + 1; i++) {
                    prop = element.getOwnerDocument().createElement(name);
                    element.appendChild(prop);
                }
            }
        }
        return prop;
    }

    /**
     * Converts a DOM node to a complete xml string
     * @param node - the node to process
     * @param indent - how to indent the children of the node
     * @return The node as a String
     */
    public static String nodeToString(Node node, String indent) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        switch (node.getNodeType()) {

        case Node.DOCUMENT_NODE:
            printWriter.println("<xml version=\"1.0\">\n");
            // recurse on each child
            NodeList nodes = node.getChildNodes();
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    printWriter.print(nodeToString(nodes.item(i), ""));
                }
            }
            break;

        case Node.ELEMENT_NODE:
            String name = node.getNodeName();
            printWriter.print(indent + "<" + name);
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node current = attributes.item(i);
                printWriter.print(" " + current.getNodeName() + "=\"" + current.getNodeValue() + "\"");
            }
            printWriter.print(">");

            // recurse on each child
            NodeList children = node.getChildNodes();
            if (children != null) {
                for (int i = 0; i < children.getLength(); i++) {
                    printWriter.print(nodeToString(children.item(i), indent + indent));
                }
            }

            printWriter.print("</" + name + ">");
            break;

        case Node.TEXT_NODE:
            printWriter.print(node.getNodeValue());
            break;
        }

        printWriter.flush();
        String result = stringWriter.getBuffer().toString();
        printWriter.close();

        return result;
    }

}
