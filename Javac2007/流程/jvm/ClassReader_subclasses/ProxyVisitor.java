    interface ProxyVisitor extends Attribute.Visitor {
        void visitEnumAttributeProxy(EnumAttributeProxy proxy);
        void visitArrayAttributeProxy(ArrayAttributeProxy proxy);
        void visitCompoundAnnotationProxy(CompoundAnnotationProxy proxy);
    }