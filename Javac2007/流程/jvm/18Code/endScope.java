    /** End the scope of a variable. */
    private void endScope(int adr) {
        // <editor-fold defaultstate="collapsed">
        /*
        class GenInnerclassTest {
			int fieldA=10;
			{
				
				int b,c=0,d;
				fieldA=20;
				d=1;
			}
			
			GenInnerclassTest() {
				fieldA=30;
			}
        }
        
        env.tree.tag=BLOCK visitBlock
        com.sun.tools.javac.jvm.Code===>endScopes(int first)
        -------------------------------------------------------------------------
        first=2 nextreg=5
        com.sun.tools.javac.jvm.Code===>endScope(int adr)
        -------------------------------------------------------------------------
        adr=2
        v=b in register 2 starts at pc=65535 length=65535
        v.start_pc=65535
        cp=26
        state.defined.excl前=(长度=32)00000000000000000000000000011011
        state.defined.excl后=(长度=32)00000000000000000000000000011011
        com.sun.tools.javac.jvm.Code===>endScope(int adr)  END
        -------------------------------------------------------------------------

        com.sun.tools.javac.jvm.Code===>endScope(int adr)
        -------------------------------------------------------------------------
        adr=3
        v=c in register 3 starts at pc=17 length=65535
        v.start_pc=17
        cp=26

        length=9
        v.length=9
        com.sun.tools.javac.jvm.Code===>putVar(LocalVar var)
        -------------------------------------------------------------------------
        var=c in register 3 starts at pc=17 length=9
        state.defined.excl前=(长度=32)00000000000000000000000000011011
        state.defined.excl后=(长度=32)00000000000000000000000000010011
        com.sun.tools.javac.jvm.Code===>endScope(int adr)  END
        -------------------------------------------------------------------------

        com.sun.tools.javac.jvm.Code===>endScope(int adr)
        -------------------------------------------------------------------------
        adr=4
        v=d in register 4 starts at pc=26 length=65535
        v.start_pc=26
        cp=26

        length=0
        v.length=0
        com.sun.tools.javac.jvm.Code===>putVar(LocalVar var)
        -------------------------------------------------------------------------
        var=d in register 4 starts at pc=26 length=0
        state.defined.excl前=(长度=32)00000000000000000000000000010011
        state.defined.excl后=(长度=32)00000000000000000000000000000011
        com.sun.tools.javac.jvm.Code===>endScope(int adr)  END
        -------------------------------------------------------------------------


        重新赋值nextreg=2
        com.sun.tools.javac.jvm.Code===>endScopes(int first)  END
        -------------------------------------------------------------------------
        */
        // </editor-fold>
		DEBUG.P(this,"endScope(int adr)");
		DEBUG.P("adr="+adr);

		LocalVar v = lvar[adr];

		DEBUG.P("v="+v);
		if (v != null) {
			lvar[adr] = null;
			DEBUG.P("v.start_pc="+(int)v.start_pc);
			DEBUG.P("cp="+cp);
			if (v.start_pc != Character.MAX_VALUE) {
				char length = (char)(curPc() - v.start_pc);

				DEBUG.P("");
				DEBUG.P("length="+(int)length);
				if (length < Character.MAX_VALUE) {
					v.length = length;
					DEBUG.P("v.length="+(int)v.length);
					putVar(v);
				}
			}
		}
		DEBUG.P("state.defined.excl前="+state.defined);
		state.defined.excl(adr);
		DEBUG.P("state.defined.excl后="+state.defined);
		DEBUG.P(0,this,"endScope(int adr)");
    }