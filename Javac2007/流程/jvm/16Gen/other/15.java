    public void visitIdent(JCIdent tree) {
    	DEBUG.P(this,"visitIdent(JCIdent tree)");
    	DEBUG.P("tree="+tree);
		DEBUG.P("tree.sym="+tree.sym);
        DEBUG.P("tree.sym.kind="+Kinds.toString(tree.sym.kind));
        DEBUG.P("tree.sym.type.tag="+TypeTags.toString(tree.sym.type.tag));
        DEBUG.P("tree.type.constValue()="+tree.type.constValue());

        Type et = tree.sym.erasure(types);
        
        DEBUG.P("et="+et);
        // Map type variables to their bounds.
        if (tree.sym.kind == TYP && tree.sym.type.tag == TYPEVAR) {
			//如<S>,则 result=Object
			//如<S extends String>,则 result=String
            result = make.at(tree.pos).Type(et);
        } else
        // Map constants expressions to themselves.
        if (tree.type.constValue() != null) {
            result = tree;
        }
        // Insert casts of variable uses as needed.
        else if (tree.sym.kind == VAR) {
            result = retype(tree, et, pt);
        }
        else {
            tree.type = erasure(tree.type);
            result = tree;
        }
        DEBUG.P("tree.type="+tree.type);
        DEBUG.P("result="+result);
        DEBUG.P(0,this,"visitIdent(JCIdent tree)");
    }