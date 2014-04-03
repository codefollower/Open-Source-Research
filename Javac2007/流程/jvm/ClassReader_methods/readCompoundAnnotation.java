    CompoundAnnotationProxy readCompoundAnnotation() {
        Type t = readTypeOrClassSymbol(nextChar());
        int numFields = nextChar();
        ListBuffer<Pair<Name,Attribute>> pairs =
            new ListBuffer<Pair<Name,Attribute>>();
        for (int i=0; i<numFields; i++) {
            Name name = readName(nextChar());
            Attribute value = readAttributeValue();
            pairs.append(new Pair<Name,Attribute>(name, value));
        }
        return new CompoundAnnotationProxy(t, pairs.toList());
    }