    /** Write a compound attribute excluding the '@' marker. */
    void writeCompoundAttribute(Attribute.Compound c) {
		DEBUG.P(this,"writeCompoundAttribute(1)");
		DEBUG.P("c="+c);
                //u2 注释类型全限定名在常量池中的索引
                //u2 注释类型字段个数
                //接着是注释类型字段表(表长＝注释类型字段个数)
                
                //注释类型字段表每个表项组成如下:
                //u2 注释类型字段名称在在常量池中的索引
                //u1 注释类型字段的种类（B代表boolean,s代表String,e代表Enum
                //c代表Class,@代表字段还是注释类型，［代表数组
                //具体看上面的visitXXX方法

        databuf.appendChar(pool.put(typeSig(c.type)));
        databuf.appendChar(c.values.length());
        DEBUG.P("c.values="+c.values);
        DEBUG.P("c.values.length()="+c.values.length());
        for (Pair<Symbol.MethodSymbol,Attribute> p : c.values) {
			DEBUG.P("p.snd.getClass().getName()="+p.snd.getClass().getName());
            databuf.appendChar(pool.put(p.fst.name));
            p.snd.accept(awriter);
        }

		DEBUG.P(0,this,"writeCompoundAttribute(1)");
    }