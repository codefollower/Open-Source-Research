    public void visitClassDef(JCClassDecl tree) {
    	DEBUG.P(this,"visitClassDef(1)");
    	DEBUG.P("tree.sym="+tree.sym);
    	DEBUG.P("env.info.scope.owner.kind="+Kinds.toString(env.info.scope.owner.kind));
    	
        // Local classes have not been entered yet, so we need to do it now:
        if ((env.info.scope.owner.kind & (VAR | MTH)) != 0)
            enter.classEnter(tree, env);

        ClassSymbol c = tree.sym;
        DEBUG.P("enter.classEnter 结束  c="+c);
        if (c == null) {
            // exit in case something drastic went wrong during enter.
            result = null;
        } else {
            // make sure class has been completed:
            c.complete();

            // If this class appears as an anonymous class
            // in a superclass constructor call where
            // no explicit outer instance is given,
            // disable implicit outer instance from being passed.
            // (This would be an illegal access to "this before super").
            if (env.info.isSelfCall &&
                env.tree.tag == JCTree.NEWCLASS &&
                ((JCNewClass) env.tree).encl == null)
            {
                c.flags_field |= NOOUTERTHIS;
            }
            attribClass(tree.pos(), c);
            result = tree.type = c.type;
        }
        
        DEBUG.P(0,this,"visitClassDef(1)");
    }