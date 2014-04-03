    class AnnotationDefaultCompleter extends AnnotationDeproxy implements Annotate.Annotator {
        final MethodSymbol sym;
        final Attribute value;
        final JavaFileObject classFile = currentClassFile;
        public String toString() {
            return " ClassReader store default for " + sym.owner + "." + sym + " is " + value;
        }
        AnnotationDefaultCompleter(MethodSymbol sym, Attribute value) {
            this.sym = sym;
            this.value = value;
        }
        // implement Annotate.Annotator.enterAnnotation()
        public void enterAnnotation() {
            JavaFileObject previousClassFile = currentClassFile;
            try {
                currentClassFile = classFile;
                sym.defaultValue = deproxy(sym.type.getReturnType(), value);
            } finally {
                currentClassFile = previousClassFile;
            }
        }
    }