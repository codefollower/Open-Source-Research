    // process the non-static imports and the static imports of types.
    public void visitImport(JCImport tree) {
        // <editor-fold defaultstate="collapsed">
        
    	DEBUG.P(this,"visitImport(1)");
    	DEBUG.P("tree.qualid="+tree.qualid);
        DEBUG.P("tree.staticImport="+tree.staticImport);
    	
        JCTree imp = tree.qualid;
        Name name = TreeInfo.name(imp);//取最后一个Ident(如java.util.* 则返回*; 如java.util.Map 则返回Map)
        TypeSymbol p;
        
        // Create a local environment pointing to this tree to disable
        // effects of other imports in Resolve.findGlobalType
        Env<AttrContext> localEnv = env.dup(tree);//outer为null
        //localEnv = env.dup(tree)相当于先把env复制一分，再用当前tree替换原来的tree,
        //新的env(localEnv)的next指向原来的env

        // Attribute qualifying package or class.
        JCFieldAccess s = (JCFieldAccess) imp;
        
        
        /*
        因为所有的导入(import)语句都是用一棵JCFieldAccess树
        表示的(参见Parser.importDeclaration())，
        JCFieldAccess树也含有JCIdent(最后一个selector)，
        在MemberEnter阶段的visitImport(1)方法中会设
        置JCFieldAccess与JCIdent的Symbol sym字段
        */
        //在没有attribTree()前sym都是null
        DEBUG.P(2);DEBUG.P("************attribTree()前************");
        for(JCTree myJCTree=s;;) {
            DEBUG.P("");
            if(myJCTree.tag==JCTree.SELECT) {
                JCFieldAccess myJCFieldAccess=(JCFieldAccess)myJCTree;
                DEBUG.P("JCFieldAccess.name="+myJCFieldAccess.name);
                DEBUG.P("JCFieldAccess.sym="+myJCFieldAccess.sym);
                myJCTree=myJCFieldAccess.selected;
            } else if(myJCTree.tag==JCTree.IDENT) {
                JCIdent myJCIdent=(JCIdent)myJCTree;
                DEBUG.P("JCIdent.name="+myJCIdent.name);
                DEBUG.P("JCIdent.sym="+myJCIdent.sym);
                break;
            } else break;
        }
        DEBUG.P("************attribTree()前************");DEBUG.P(2);

        
        //attribTree()调用有点繁琐，得耐心看
        p = attr.
            attribTree(s.selected,
                       localEnv,
                       tree.staticImport ? TYP : (TYP | PCK),
                       Type.noType).tsym;
        
        
        
        //在attribTree()后只有第一个JCFieldAccess的sym是null
        DEBUG.P(2);DEBUG.P("************attribTree()后************");
        for(JCTree myJCTree=s;;) {
            DEBUG.P("");
            if(myJCTree.tag==JCTree.SELECT) {
                JCFieldAccess myJCFieldAccess=(JCFieldAccess)myJCTree;
                DEBUG.P("JCFieldAccess.name="+myJCFieldAccess.name);
                DEBUG.P("JCFieldAccess.sym="+myJCFieldAccess.sym);
                myJCTree=myJCFieldAccess.selected;
            } else if(myJCTree.tag==JCTree.IDENT) {
                JCIdent myJCIdent=(JCIdent)myJCTree;
                DEBUG.P("JCIdent.name="+myJCIdent.name);
                DEBUG.P("JCIdent.sym="+myJCIdent.sym);
                break;
            } else break;
        }
        DEBUG.P("************attribTree()后************");DEBUG.P(2);  
        
	// </editor-fold>
        
        DEBUG.P("p="+p);
        DEBUG.P("name="+name);    
        //DEBUG.P("visitImport stop",true);          
        if (name == names.asterisk) {
            // Import on demand.
            chk.checkCanonical(s.selected);
            if (tree.staticImport)
                importStaticAll(tree.pos, p, env);
            else
                importAll(tree.pos, p, env);
        } else {
            // Named type import.
            if (tree.staticImport) {
                importNamedStatic(tree.pos(), p, name, localEnv);
                chk.checkCanonical(s.selected);
            } else {
                TypeSymbol c = attribImportType(imp, localEnv).tsym;
                DEBUG.P("TypeSymbol c="+c); 
                chk.checkCanonical(imp);
                importNamed(tree.pos(), c, env);
            }
        }
        
        DEBUG.P(0,this,"visitImport(1)");
    }