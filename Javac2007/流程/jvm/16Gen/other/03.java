    /** Distribute member initializer code into constructors and <clinit>
     *  method.
     *  @param defs         The list of class member declarations.
     *  @param c            The enclosing class.
     */
    List<JCTree> normalizeDefs(List<JCTree> defs, ClassSymbol c) {
		DEBUG.P(this,"normalizeDefs(2)");
		DEBUG.P("c="+c);
		
		ListBuffer<JCStatement> initCode = new ListBuffer<JCStatement>();
		ListBuffer<JCStatement> clinitCode = new ListBuffer<JCStatement>();
		ListBuffer<JCTree> methodDefs = new ListBuffer<JCTree>();
		// Sort definitions into three listbuffers:
		//  - initCode for instance initializers
		//  - clinitCode for class initializers
		//  - methodDefs for method definitions
		for (List<JCTree> l = defs; l.nonEmpty(); l = l.tail) {
			JCTree def = l.head;
			DEBUG.P("");
			DEBUG.P("def.tag="+def.myTreeTag());
			switch (def.tag) {
				case JCTree.BLOCK:
					JCBlock block = (JCBlock)def;
					DEBUG.P("block.flags="+Flags.toString(block.flags));
					if ((block.flags & STATIC) != 0)
						clinitCode.append(block);
					else
						initCode.append(block);
						break;
				case JCTree.METHODDEF:
					methodDefs.append(def);
					break;
				case JCTree.VARDEF:
					JCVariableDecl vdef = (JCVariableDecl) def;
					VarSymbol sym = vdef.sym;
					DEBUG.P("sym="+sym);
					DEBUG.P("vdef.init="+vdef.init);
					checkDimension(vdef.pos(), sym.type);//检查变量的类型是否是多维数组，如果是，则维数不能大于255
					if (vdef.init != null) {
						DEBUG.P("");
						DEBUG.P("sym.getConstValue()="+sym.getConstValue());
						DEBUG.P("sym.flags()="+Flags.toString(sym.flags()));
						if ((sym.flags() & STATIC) == 0) {
							// Always initialize instance variables.
							JCStatement init = make.at(vdef.pos()).
								Assignment(sym, vdef.init);
							initCode.append(init);
							if (endPositions != null) {
								Integer endPos = endPositions.remove(vdef);
								if (endPos != null) endPositions.put(init, endPos);
							}
						} else if (sym.getConstValue() == null) {
						// Initialize class (static) variables only if
							// they are not compile-time constants.
							JCStatement init = make.at(vdef.pos).
								Assignment(sym, vdef.init);

							DEBUG.P("");
							DEBUG.P("init="+init);
							clinitCode.append(init);
							if (endPositions != null) {
								Integer endPos = endPositions.remove(vdef);
								if (endPos != null) endPositions.put(init, endPos);
							}
						} else {//只有已初始化的static final类型变量才是compile-time constants
							checkStringConstant(vdef.init.pos(), sym.getConstValue());
						}
					}
					break;
				default:
					assert false;
			}
		}
		
		DEBUG.P(2);
		DEBUG.P("initCode="+initCode.toList());
		DEBUG.P("clinitCode="+clinitCode.toList());
		// Insert any instance initializers into all constructors.
		if (initCode.length() != 0) {
			List<JCStatement> inits = initCode.toList();
			for (JCTree t : methodDefs) {
				normalizeMethod((JCMethodDecl)t, inits);
			}
		}
		// If there are class initializers, create a <clinit> method
		// that contains them as its body.
		if (clinitCode.length() != 0) {
			MethodSymbol clinit = new MethodSymbol(
			STATIC, names.clinit,
			new MethodType(
				List.<Type>nil(), syms.voidType,
				List.<Type>nil(), syms.methodClass),
			c);
			c.members().enter(clinit);
			List<JCStatement> clinitStats = clinitCode.toList();
			JCBlock block = make.at(clinitStats.head.pos()).Block(0, clinitStats);
			block.endpos = TreeInfo.endPos(clinitStats.last());
			methodDefs.append(make.MethodDef(clinit, block));
			DEBUG.P("c.members()="+c.members());
		}
		DEBUG.P(0,this,"normalizeDefs(2)");
		// Return all method definitions.
		return methodDefs.toList();
    }