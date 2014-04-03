/************************************************************************
 * Reading Java-language annotations
 ***********************************************************************/

    /** Attach annotations.
     */
    void attachAnnotations(final Symbol sym) {
        int numAttributes = nextChar();
        if (numAttributes != 0) {
            ListBuffer<CompoundAnnotationProxy> proxies =
                new ListBuffer<CompoundAnnotationProxy>();
            for (int i = 0; i<numAttributes; i++) {
                CompoundAnnotationProxy proxy = readCompoundAnnotation();
                if (proxy.type.tsym == syms.proprietaryType.tsym)
                    sym.flags_field |= PROPRIETARY;
                else
                    proxies.append(proxy);
            }
            annotate.later(new AnnotationCompleter(sym, proxies.toList()));
        }
    }