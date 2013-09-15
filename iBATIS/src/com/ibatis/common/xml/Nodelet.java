package com.ibatis.common.xml;

import org.w3c.dom.Node;

/**
 * A nodelet is a sort of callback or event handler that can be registered 
 * to handle an XPath event registered with the NodeParser.   
 */
public interface Nodelet {

    /**
     * For a registered XPath, the NodeletParser will call the Nodelet's 
     * process method for processing. 
     * 
     * @param node The node represents any XML node that can be registered under
     * an XPath supported by the NodeletParser.  Possible nodes are:
     * <ul>
     *   <li>Text - Use node.getNodeValue() for the text value.
     *   <li>Attribute - Use node.getNodeValue() for the attribute value.
     *   <li>Element - This is the most flexible type.  You can get the node
     * content and iterate over the node's child nodes if neccessary.  This is 
     * useful where a single XPath registration cannot describe the complex
     * structure for a given XML stanza.
     * </ul>
     *
     */
    void process(Node node) throws Exception;

}
