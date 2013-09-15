package com.ibatis.common.xml;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Properties;

public class NodeletUtils {
    private static my.Debug DEBUG = new my.Debug(my.Debug.SqlMapConfigParser);//�Ҽ��ϵ�

    public static boolean getBooleanAttribute(Properties attribs, String name, boolean def) {
        String value = attribs.getProperty(name);
        if (value == null) {
            return def;
        } else {
            return "true".equals(value);
        }
    }

    public static int getIntAttribute(Properties attribs, String name, int def) {
        String value = attribs.getProperty(name);
        if (value == null) {
            return def;
        } else {
            return Integer.parseInt(value);
        }
    }

    public static Properties parseAttributes(Node n) {
        return parseAttributes(n, null);
    }

    public static Properties parseAttributes(Node n, Properties variables) {
        Properties attributes = new Properties();
        NamedNodeMap attributeNodes = n.getAttributes();
        for (int i = 0; i < attributeNodes.getLength(); i++) {
            Node attribute = attributeNodes.item(i);
            String value = parsePropertyTokens(attribute.getNodeValue(), variables);
            attributes.put(attribute.getNodeName(), value);
        }
        return attributes;
    }

    //����string="abc${var}def"�����variables����var=123��
    //��ô��"abc${var}def"�滻��"abc123def"
    public static String parsePropertyTokens(String string, Properties variables) {
        //����ֻ�ǲ���
        //string = "abc${var}def";
        //variables.setProperty("var","123");

        final String OPEN = "${";
        final String CLOSE = "}";

        //DEBUG.P("newString="+string);

        String newString = string;
        if (newString != null && variables != null) {
            int start = newString.indexOf(OPEN);
            int end = newString.indexOf(CLOSE);

            while (start > -1 && end > start) {
                String prepend = newString.substring(0, start);
                String append = newString.substring(end + CLOSE.length());
                String propName = newString.substring(start + OPEN.length(), end);
                String propValue = variables.getProperty(propName);
                if (propValue == null) {
                    newString = prepend + propName + append;
                } else {
                    newString = prepend + propValue + append;
                }
                start = newString.indexOf(OPEN);
                end = newString.indexOf(CLOSE);
            }
        }
        //DEBUG.P("newString="+newString);
        return newString;
    }

}
