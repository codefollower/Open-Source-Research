/************************************************************************
 * Reading Attributes
 ***********************************************************************/

    /** Report unrecognized attribute.
     */
    void unrecognized(Name attrName) {
        if (checkClassFile)
            printCCF("ccf.unrecognized.attribute", attrName);
    }