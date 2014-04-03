    public void visitSelect(JCFieldAccess tree) {
       	DEBUG.P(this,"visitSelect(1)");
       	DEBUG.P("tree="+tree);
       	DEBUG.P("tree.type.tag="+TypeTags.toString(tree.type.tag));
       	
	    if (tree.type.tag == CLASS) {
                visitSelectInternal(tree);

                // Check that this type is either fully parameterized, or
                // not parameterized at all.
				DEBUG.P("tree.selected.type.isParameterized()="+tree.selected.type.isParameterized());
				DEBUG.P("tree.type.tsym.type.getTypeArguments().nonEmpty()="+tree.type.tsym.type.getTypeArguments().nonEmpty());
                if (tree.selected.type.isParameterized() && tree.type.tsym.type.getTypeArguments().nonEmpty())
                    log.error(tree.pos(), "improperly.formed.type.param.missing");
            }
        
        DEBUG.P(0,this,"visitSelect(1)");
	}
        public void visitSelectInternal(JCFieldAccess tree) {
        	DEBUG.P(this,"visitSelectInternal(1)");
        	DEBUG.P("tree.type.getEnclosingType().tag="+TypeTags.toString(tree.type.getEnclosingType().tag));
        	DEBUG.P("tree.selected.type.isParameterized()="+tree.selected.type.isParameterized());
            DEBUG.P("tree.selected.type.allparams()="+tree.selected.type.allparams());
            if (tree.type.getEnclosingType().tag != CLASS &&
                tree.selected.type.isParameterized()) {
                /*错误例子:
                bin\mysrc\my\test\Test.java:7: 无法从参数化的类型中选择静态类
				public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA>
				extends my.ExtendsTest<String>.MyInnerClassStatic {
				
				                              ^
				1 错误
				
				打印结果:
				com.sun.tools.javac.comp.Check$Validator===>visitSelect(1)
				-------------------------------------------------------------------------
				tree=my.ExtendsTest<String>.MyInnerClassStatic
				tree.type.tag=CLASS
				com.sun.tools.javac.comp.Check$Validator===>visitSelectInternal(1)
				-------------------------------------------------------------------------
				tree.type.getEnclosingType().tag=NONE
				tree.selected.type.isParameterized()=true
				tree.selected.type.allparams()=java.lang.String
				com.sun.tools.javac.comp.Check$Validator===>visitSelectInternal(1)  END
				-------------------------------------------------------------------------
				com.sun.tools.javac.comp.Check$Validator===>visitSelect(1)  END
				-------------------------------------------------------------------------
                */
                
                // The enclosing type is not a class, so we are
                // looking at a static member type.  However, the
                // qualifying expression is parameterized.
                log.error(tree.pos(), "cant.select.static.class.from.param.type");
            } else {
                // otherwise validate the rest of the expression
                validate(tree.selected);
            }
            
            DEBUG.P(0,this,"visitSelectInternal(1)");
        }