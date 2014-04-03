    /** Fill in number of attributes.
     */
    void endAttrs(int index, int count) {
        putChar(databuf, index - 2, count);
    }