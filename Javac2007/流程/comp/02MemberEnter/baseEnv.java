    private Env<AttrContext> baseEnv(JCClassDecl tree, Env<AttrContext> env) {
    	DEBUG.P(this,"baseEnv(2)");
    	DEBUG.P("env="+env);
    	DEBUG.P("tree.sym="+tree.sym);
    	DEBUG.P("env.enclClass.sym="+env.enclClass.sym);
        Scope typaramScope = new Scope(tree.sym);
        if (tree.typarams != null)
            for (List<JCTypeParameter> typarams = tree.typarams;
                 typarams.nonEmpty();
                 typarams = typarams.tail) {
                 	DEBUG.P("typarams.head.type.tsym=     "+typarams.head.type.tsym);
                typaramScope.enter(typarams.head.type.tsym);
            }
		/*
		//比如类型变量不能extends成员类
		class MemberEnterTest<T,V extends MemberClassB> { //找不到符号
			public class MemberClassB{}
		}
		*/
        Env<AttrContext> outer = env.outer; // the base clause can't see members of this class
        Env<AttrContext> localEnv = outer.dup(tree, outer.info.dup(typaramScope));
        localEnv.baseClause = true;
        localEnv.outer = outer;
        localEnv.info.isSelfCall = false;
        //localEnv与env是并列的，但是enclClass换了，
        //localEnv.enclClass=env.outer.enclClass
        DEBUG.P("localEnv="+localEnv);
        DEBUG.P("localEnv.enclClass.sym="+localEnv.enclClass.sym);
        DEBUG.P(0,this,"baseEnv(2)");
        return localEnv;
    }