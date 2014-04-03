    /**
     * Attribute the type references in a list of annotations.
     */
    void attribAnnotationTypes(List<JCAnnotation> annotations,
                               Env<AttrContext> env) {
        DEBUG.P(this,"attribAnnotationTypes(2)");  
        DEBUG.P("env="+env);
        DEBUG.P("annotations="+annotations);                     	
        for (List<JCAnnotation> al = annotations; al.nonEmpty(); al = al.tail) {
            JCAnnotation a = al.head;
            attribType(a.annotationType, env);
        }
        DEBUG.P(0,this,"attribAnnotationTypes(2)");  
    }