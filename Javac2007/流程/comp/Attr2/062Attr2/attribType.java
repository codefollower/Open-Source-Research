    /** Derived visitor method: attribute a type tree.
     */
    Type attribType(JCTree tree, Env<AttrContext> env) {
    	DEBUG.P(this,"attribType(2)");
        Type result = attribTree(tree, env, TYP, Type.noType);
        
        DEBUG.P("result="+result);
		DEBUG.P("result.tag="+TypeTags.toString(result.tag));
        DEBUG.P(0,this,"attribType(2)");
        return result;
    }