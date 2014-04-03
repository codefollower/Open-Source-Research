	/** Finish the attribution of a class. */
    private void attribClassBody(Env<AttrContext> env, ClassSymbol c) {
    	DEBUG.P(this,"attribClassBody(2)");
    	DEBUG.P("ClassSymbol c="+c);
        DEBUG.P("env="+env);
    	
        JCClassDecl tree = (JCClassDecl)env.tree;
        assert c == tree.sym;

        // Validate annotations
        chk.validateAnnotations(tree.mods.annotations, c);

        // Validate type parameters, supertype and interfaces.
        attribBounds(tree.typarams);//对COMPOUND型的上限绑定进行attribClass
        /*
        主要是检查同一泛型类的参数化类型之间的差别
        如泛型类定义  :interface Test<T extends Number>
        参数化类型t :Test<Number>
        参数化类型s :Test<? super Float>
        
        当定义新的泛型类：Test2<S extends Test<Number>&Test<? super Float>>
        时，在validateTypeParams中能检查出“无法使用以下不同的参数继承”错误
        */
        chk.validateTypeParams(tree.typarams);
        chk.validate(tree.extending);
        chk.validate(tree.implementing);
        
        DEBUG.P(2);DEBUG.P("结束:Validate annotations, type parameters, supertype and interfaces : "+c);DEBUG.P(2);
        
        DEBUG.P("relax="+relax);
        DEBUG.P("c.flags()="+Flags.toString(c.flags()));
        // If this is a non-abstract class, check that it has no abstract
        // methods or unimplemented methods of an implemented interface.
        if ((c.flags() & (ABSTRACT | INTERFACE)) == 0) {
            if (!relax)
                chk.checkAllDefined(tree.pos(), c);
        }

        if ((c.flags() & ANNOTATION) != 0) {
            if (tree.implementing.nonEmpty())
                log.error(tree.implementing.head.pos(),
                          "cant.extend.intf.annotation");
            if (tree.typarams.nonEmpty())
                log.error(tree.typarams.head.pos(),
                          "intf.annotation.cant.have.type.params");
        } else {
            // Check that all extended classes and interfaces
            // are compatible (i.e. no two define methods with same arguments
            // yet different return types).  (JLS 8.4.6.3)
            chk.checkCompatibleSupertypes(tree.pos(), c.type);
        }

        // Check that class does not import the same parameterized interface
        // with two different argument lists.
        chk.checkClassBounds(tree.pos(), c.type);

        tree.type = c.type;

        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        
        DEBUG.P("env.info.scope="+env.info.scope);
        if (assertsEnabled) {
            for (List<JCTypeParameter> l = tree.typarams;
                 l.nonEmpty(); l = l.tail)
                assert env.info.scope.lookup(l.head.name).scope != null;
        }
        
        DEBUG.P("c.type="+c.type);
        DEBUG.P("c.type.allparams()="+c.type.allparams());
        /*错误例子:
        bin\mysrc\my\test\Test.java:7: 泛型类无法继承 java.lang.Throwable
		public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA>
		extends Exception {
		
		        ^
		1 错误
		*/
        // Check that a generic class doesn't extend Throwable
        if (!c.type.allparams().isEmpty() && types.isSubtype(c.type, syms.throwableType))
            log.error(tree.extending.pos(), "generic.throwable");

        // Check that all methods which implement some
        // method conform to the method they implement.
        chk.checkImplementations(tree);

        for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
            // Attribute declaration
            attribStat(l.head, env);
            // Check that declarations in inner classes are not static (JLS 8.1.2)
            // Make an exception for static constants.
            if (c.owner.kind != PCK &&
                ((c.flags() & STATIC) == 0 || c.name == names.empty) &&
                (TreeInfo.flags(l.head) & (STATIC | INTERFACE)) != 0) {
                Symbol sym = null;
                if (l.head.tag == JCTree.VARDEF) sym = ((JCVariableDecl) l.head).sym;
                if (sym == null ||
                    sym.kind != VAR ||
                    ((VarSymbol) sym).getConstValue() == null)
                    log.error(l.head.pos(), "icls.cant.have.static.decl");
            }
        }

        // Check for cycles among non-initial constructors.
        chk.checkCyclicConstructors(tree);

        // Check for cycles among annotation elements.
        chk.checkNonCyclicElements(tree);

        // Check for proper use of serialVersionUID
        if (env.info.lint.isEnabled(Lint.LintCategory.SERIAL) &&
            isSerializable(c) &&
            (c.flags() & Flags.ENUM) == 0 &&
            (c.flags() & ABSTRACT) == 0) {
            checkSerialVersionUID(tree, c);
        }
        
        DEBUG.P(0,this,"attribClassBody(2)");
    }