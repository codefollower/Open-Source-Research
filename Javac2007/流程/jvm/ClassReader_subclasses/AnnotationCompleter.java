    class AnnotationCompleter extends AnnotationDeproxy implements Annotate.Annotator {
        final Symbol sym;
        final List<CompoundAnnotationProxy> l;
        final JavaFileObject classFile;
        public String toString() {
            return " ClassReader annotate " + sym.owner + "." + sym + " with " + l;
        }
        AnnotationCompleter(Symbol sym, List<CompoundAnnotationProxy> l) {
            this.sym = sym;
            this.l = l;
            this.classFile = currentClassFile;
        }
        // implement Annotate.Annotator.enterAnnotation()
        public void enterAnnotation() {
            JavaFileObject previousClassFile = currentClassFile;
            try {
                currentClassFile = classFile;
                List<Attribute.Compound> newList = deproxyCompoundList(l);
                sym.attributes_field = ((sym.attributes_field == null)
                                        ? newList
                                        : newList.prependList(sym.attributes_field));
            } finally {
                currentClassFile = previousClassFile;
            }
        }
    }