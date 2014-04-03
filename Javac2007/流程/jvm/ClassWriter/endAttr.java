    /** Fill in attribute length.
     */
    void endAttr(int index) {
		DEBUG.P(this,"endAttr(int index)");
		DEBUG.P("attribute.index ="+(index - 4));
		DEBUG.P("attribute.length="+(databuf.length - index));

        putInt(databuf, index - 4, databuf.length - index);

		DEBUG.P(0,this,"endAttr(int index)");
    }