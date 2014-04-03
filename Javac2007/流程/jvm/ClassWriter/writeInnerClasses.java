    /** Write "inner classes" attribute.
     */
    void writeInnerClasses() {
		DEBUG.P(this,"writeInnerClasses()");

        int alenIdx = writeAttr(names.InnerClasses);
        databuf.appendChar(innerClassesQueue.length());
        for (List<ClassSymbol> l = innerClassesQueue.toList();
             l.nonEmpty();
             l = l.tail) {
            ClassSymbol inner = l.head;
            char flags = (char) adjustFlags(inner.flags_field);
            if (dumpInnerClassModifiers) {
                log.errWriter.println("INNERCLASS  " + inner.name);
                log.errWriter.println("---" + flagNames(flags));
            }
            databuf.appendChar(pool.get(inner));
            databuf.appendChar(
                inner.owner.kind == TYP ? pool.get(inner.owner) : 0);
            databuf.appendChar(
                inner.name.len != 0 ? pool.get(inner.name) : 0);
            databuf.appendChar(flags);
        }
        endAttr(alenIdx);

		DEBUG.P(0,this,"writeInnerClasses()");
    }