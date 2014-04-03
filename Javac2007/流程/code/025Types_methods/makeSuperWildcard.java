//makeCompoundType
    // <editor-fold defaultstate="collapsed" desc="makeCompoundType">
    /**
     * Make a compound type from non-empty list of types
     *
     * @param bounds            the types from which the compound type is formed
     * @param supertype         is objectType if all bounds are interfaces,
     *                          null otherwise.
     */
    public Type makeCompoundType(List<Type> bounds,
                                 Type supertype) {
        DEBUG.P(this,"makeCompoundType(2)");  
        DEBUG.P("bounds="+bounds);
        DEBUG.P("supertype="+supertype);  
		
		//如果javac命令行加了“-moreInfo”选项时，ClassSymbol的name
		//就是bounds的字符串，否则为空串
        ClassSymbol bc =
        //还有一个UNATTRIBUTED标志留在
        //com.sun.tools.javac.comp.Attr===>visitTypeParameter(1)设置
            new ClassSymbol(ABSTRACT|PUBLIC|SYNTHETIC|COMPOUND|ACYCLIC,
                            Type.moreInfo
                                ? names.fromString(bounds.toString())
                                : names.empty,
                            syms.noSymbol);
        //注意:在调用到makeCompoundType时对于这样的语法T extends V&InterfaceA
		//是允许的，只是到了后续编译阶段是才检查出类型变量V后不能跟其他限制范围
        if (bounds.head.tag == TYPEVAR)
            // error condition, recover
            bc.erasure_field = syms.objectType;
        else //CompoundType的erasure_field取第一个bound的erasure类型
            bc.erasure_field = erasure(bounds.head);
        DEBUG.P("ClassSymbol bc.name="+bc.name); 
        DEBUG.P("bc.erasure_field="+bc.erasure_field);  
        bc.members_field = new Scope(bc);
        ClassType bt = (ClassType)bc.type;
        bt.allparams_field = List.nil();
        if (supertype != null) {
            bt.supertype_field = supertype;
            bt.interfaces_field = bounds;
        } else {
            bt.supertype_field = bounds.head;
            bt.interfaces_field = bounds.tail;
        }
		DEBUG.P("bt.supertype_field.tsym.completer="+bt.supertype_field.tsym.completer);  
        assert bt.supertype_field.tsym.completer != null
            || !bt.supertype_field.isInterface()
            : bt.supertype_field;
        /*
		对于像<V extends InterfaceTest & InterfaceTest2>这样的泛型定义
		输出结果如下:
		------------------------------------
		ClassSymbol bc.name=my.test.InterfaceTest,my.test.InterfaceTest2
		bc.erasure_field=my.test.InterfaceTest
		bt.supertype_field=java.lang.Object
		bt.interfaces_field=my.test.InterfaceTest,my.test.InterfaceTest2
		------------------------------------
		也就是说当类型变量的bounds都是接口(两个或两个以上)时，
		那么这个类型变量的ClassType是Compound类型的，
		ClassType.supertype_field是java.lang.Object，
		ClassType.interfaces_field是bounds中的所有接口
		这个类型变量对应的ClassSymbol的erasure_field是bounds中的第一个接口。

		所以可以把泛型定义<V extends InterfaceTest&InterfaceTest2>看成
		这样<V extends Object & InterfaceTest & InterfaceTest2>
		*/
        DEBUG.P("bt.supertype_field="+bt.supertype_field);  
        DEBUG.P("bt.interfaces_field="+bt.interfaces_field);  
        DEBUG.P("return bt="+bt);
        DEBUG.P(0,this,"makeCompoundType(2)");  
        return bt;
    }

    /**
     * Same as {@link #makeCompoundType(List,Type)}, except that the
     * second parameter is computed directly. Note that this might
     * cause a symbol completion.  Hence, this version of
     * makeCompoundType may not be called during a classfile read.
     */
    public Type makeCompoundType(List<Type> bounds) {
        Type supertype = (bounds.head.tsym.flags() & INTERFACE) != 0 ?
            supertype(bounds.head) : null;
        return makeCompoundType(bounds, supertype);
    }

    /**
     * A convenience wrapper for {@link #makeCompoundType(List)}; the
     * arguments are converted to a list and passed to the other
     * method.  Note that this might cause a symbol completion.
     * Hence, this version of makeCompoundType may not be called
     * during a classfile read.
     */
    public Type makeCompoundType(Type bound1, Type bound2) {
        return makeCompoundType(List.of(bound1, bound2));
    }
    // </editor-fold>
//