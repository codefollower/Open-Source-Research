    /** Generate a base clause for an enum type.
     *  @param pos              The position for trees and diagnostics, if any
     *  @param c                The class symbol of the enum
     */
    private JCExpression enumBase(int pos, ClassSymbol c) {
    	DEBUG.P(this,"enumBase(2)");
        JCExpression result = make.at(pos).
            TypeApply(make.QualIdent(syms.enumSym),
                      List.<JCExpression>of(make.Type(c.type)));
        DEBUG.P("result="+result);
		//result=.java.lang.Enum<.test.memberEnter.EnumTest>
		//为什么最前面是"."号呢？因为在enterPackage时，java包、test包的owner
		//都是rootPackage，调用make.QualIdent时递归到rootPackage时才结束
        DEBUG.P(0,this,"enumBase(2)");
        return result;
    }