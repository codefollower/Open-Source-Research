    //下面这行文法表示得不够准确
    /** CompilationUnit = [ { "@" Annotation } PACKAGE Qualident ";"] {ImportDeclaration} {TypeDeclaration}
     */
    //上面的注释是LL(1)文法的总的全貌, 说明CompilationUnit =空也可以,
    //这如同编译一个没有任何内容的源文件也不会报错一样
    public JCTree.JCCompilationUnit compilationUnit() {
    	DEBUG.P(this,"compilationUnit() 正式开始语法分析......");
    	DEBUG.P("startPos="+S.pos());
    	DEBUG.P("errorPos="+errorPos);
    	DEBUG.P("errorEndPos="+errorEndPos);
        DEBUG.P("startToken="+S.token());
        
        int pos = S.pos();
        JCExpression pid = null;//对应文法中的Qualident
        //当前token对应的javadoc(见DocCommentScanner.processComment(1))
        String dc = S.docComment();
        DEBUG.P("dc="+dc);

		//对应文法中的{ "@" Annotation }，可能是包注释，
		//也可能是第一个声明的类的修饰符
        JCModifiers mods = null;
        
        List<JCAnnotation> packageAnnotations = List.nil();
        
        if (S.token() == MONKEYS_AT)
            mods = modifiersOpt();
        /*
        只有在package-info.java文件中才能有包注释(在没有特别指明的情况下，“注释”指的是Annotation)
        否则会有错误提示：“软件包注释应在文件 package-info.java 中”
        对应compiler.properties中的"pkg.annotations.sb.in.package-info.java"
        错误不在语法分析阶段检查，而是在com.sun.tools.javac.comp.Enter中检查
        */
        if (S.token() == PACKAGE) {
            //如果在“package”前有JavaDoc,且里面有@deprecated，
            //但是后面没有@Annotation或其他modifiers就是合法的。
            if (mods != null) {
            	/*
            	检查是否允许使用修饰符
            	如果package-info.java文件的源码像下面那样:
            	@Deprecated public
                package my.test;

                就会报错:
                bin\mysrc\my\test\package-info.java:2: 此处不允许使用修饰符 public
                package my.test;
                ^
                1 错误				
                */
                checkNoMods(mods.flags);
                packageAnnotations = mods.annotations;
                mods = null;
            }
            S.nextToken();
            pid = qualident();
            accept(SEMI);
        }
        //defs中存放跟import语句与类型(class,interface等)定义相关的JTree
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
       	boolean checkForImports = true;
        while (S.token() != EOF) {
            DEBUG.P("S.pos()="+S.pos()+"  errorEndPos="+errorEndPos);
            if (S.pos() <= errorEndPos) {
                // error recovery
                skip(checkForImports, false, false, false);
                if (S.token() == EOF)
                    break;
            }
            
            //软件包注释应在文件 package-info.java 中,而package-info.java是没有import的，
            //非package-info.java文件不能有包注释，所以mods==null(???)
            //(有三个问号的注释表明目前还未完全搞明白)
			//因为第一个类声明之前可能没有import，此时因为是第一次进入while循环
			//checkForImports为true，但是mods可能不为null(如含有public等)
            if (checkForImports && mods == null && S.token() == IMPORT) {
                defs.append(importDeclaration());
            } else {
				//当没有指定package与import语句时，并且在类声明之前加有@，
				//如：@MyAnnotation public ClassA {}，则mods!=null
                JCTree def = typeDeclaration(mods);
                
                //用JCExpressionStatement将JCErroneous“包装”起来
                if (def instanceof JCExpressionStatement)
                    def = ((JCExpressionStatement)def).expr;
                defs.append(def);

				//这里保证了在类声明之后不能有import语句
                if (def instanceof JCClassDecl)
                    checkForImports = false;
				//这个是首先声明的类的修饰符，
				//对于在同一文件中声明的其他类必须设为null，
				//因为typeDeclaration(mods)时会重新modifiersOpt(mods)
                mods = null;
            }
        }
        //F.at(pos)里的pos还是int pos = S.pos();时的pos,一直没变
        JCTree.JCCompilationUnit toplevel = F.at(pos).TopLevel(packageAnnotations, pid, defs.toList());
        attach(toplevel, dc);

		DEBUG.P("defs.elems.isEmpty()="+defs.elems.isEmpty());
        if (defs.elems.isEmpty())
            storeEnd(toplevel, S.prevEndPos());
        if (keepDocComments) toplevel.docComments = docComments;
        
        //运行到这里，语法分析完成，生成了一棵抽象语法树
		//DEBUG.P("toplevel="+toplevel);
		DEBUG.P("toplevel.startPos="+getStartPos(toplevel));
		DEBUG.P("toplevel.endPos  ="+getEndPos(toplevel));
        DEBUG.P(3,this,"compilationUnit()");
        //DEBUG.P("Parser stop",true);
        return toplevel;
    }