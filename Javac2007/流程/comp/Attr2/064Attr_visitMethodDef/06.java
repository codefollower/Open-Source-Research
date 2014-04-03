    //b10
    public void visitTypeParameter(JCTypeParameter tree) {
    	try {//我加上的
		DEBUG.P(this,"visitTypeParameter(1)");
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.type.tag="+TypeTags.toString(tree.type.tag));
		DEBUG.P("tree.bounds="+tree.bounds);
		
        TypeVar a = (TypeVar)tree.type;
        Set<Type> boundSet = new HashSet<Type>();
        if (a.bound.isErroneous())
            return;
        List<Type> bs = types.getBounds(a);
        if (tree.bounds.nonEmpty()) {
            // accept class or interface or typevar as first bound.
            Type b = checkBase(bs.head, tree.bounds.head, env, false, false, false);
            boundSet.add(types.erasure(b));
            DEBUG.P("b.tag="+TypeTags.toString(b.tag));
            if (b.tag == TYPEVAR) {
                // if first bound was a typevar, do not accept further bounds.
                if (tree.bounds.tail.nonEmpty()) {
					/*错误例子:
					bin\mysrc\my\test\Test.java:8: 类型变量后面不能带有其他限制范围
					public class Test<S,T extends ExtendsTest,E extends S & MyInterfaceA> extends my
					.ExtendsTest.MyInnerClassStatic {
																			^
					*/
                    log.error(tree.bounds.tail.head.pos(),
                              "type.var.may.not.be.followed.by.other.bounds");
                    tree.bounds = List.of(tree.bounds.head);
                }
            } else {
                // if first bound was a class or interface, accept only interfaces
                // as further bounds.
                for (JCExpression bound : tree.bounds.tail) {
                    bs = bs.tail;
                    Type i = checkBase(bs.head, bound, env, false, true, false);
                    if (i.tag == CLASS)
                        chk.checkNotRepeated(bound.pos(), types.erasure(i), boundSet);
                }
            }
        }
        bs = types.getBounds(a);
        
        DEBUG.P("bs="+bs);
        DEBUG.P("bs.length()="+bs.length());
        // in case of multiple bounds ...
        if (bs.length() > 1) {
            // ... the variable's bound is a class type flagged COMPOUND
            // (see comment for TypeVar.bound).
            // In this case, generate a class tree that represents the
            // bound class, ...
            JCTree extending;
            List<JCExpression> implementing;
            if ((bs.head.tsym.flags() & INTERFACE) == 0) {
                extending = tree.bounds.head;
                implementing = tree.bounds.tail;
            } else {
                extending = null;
                implementing = tree.bounds;
            }
            JCClassDecl cd = make.at(tree.pos).ClassDef(
                make.Modifiers(PUBLIC | ABSTRACT),
                tree.name, List.<JCTypeParameter>nil(),
                extending, implementing, List.<JCTree>nil());

            ClassSymbol c = (ClassSymbol)a.getUpperBound().tsym;
            assert (c.flags() & COMPOUND) != 0;
            cd.sym = c;
            c.sourcefile = env.toplevel.sourcefile;

            // ... and attribute the bound class
            c.flags_field |= UNATTRIBUTED;
            Env<AttrContext> cenv = enter.classEnv(cd, env);
            enter.typeEnvs.put(c, cenv);
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"visitTypeParameter(1)");
		}
    }