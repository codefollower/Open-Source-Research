    public void visitClassDef(JCClassDecl tree) {
        DEBUG.P(this,"visitClassDef(1)");
        //在没有进行到Enter阶段的时候JCClassDecl的ClassSymbol sym
		//是null，这也说明了:Parser的后续阶段的任务就是往各类JCTree中“塞入数据”
		DEBUG.P("JCClassDecl tree.sym="+tree.sym);
		DEBUG.P("JCClassDecl tree.name="+tree.name);
		
		Symbol owner = env.info.scope.owner;
		Scope enclScope = enterScope(env);
		ClassSymbol c;
		
		DEBUG.P("Symbol owner.kind="+Kinds.toString(owner.kind));
		DEBUG.P("Symbol owner="+owner);
		/*
		注意Scope enclScope与
		JCCompilationUnit.PackageSymbol packge.members_field的差别
		Scope enclScope有可能是指向JCCompilationUnit.namedImportScope(参看topLevelEnv())
		所以下面的输出可能是:Scope enclScope=Scope[]
		*/
		DEBUG.P("Scope enclScope="+enclScope);
		if (owner.kind == PCK) {
				// <editor-fold defaultstate="collapsed">
			// We are seeing a toplevel class.
			PackageSymbol packge = (PackageSymbol)owner;
			//一般在ClassReader.includeClassFile()中已设过
			DEBUG.P("PackageSymbol packge.flags_field(1)="+packge.flags_field+"("+Flags.toString(packge.flags_field)+")");
			for (Symbol q = packge; q != null && q.kind == PCK; q = q.owner)
			q.flags_field |= EXISTS;//EXISTS在com.sun.tools.javac.code.Flags
			
				DEBUG.P("PackageSymbol packge.name="+packge);
				DEBUG.P("PackageSymbol packge.flags_field(2)="+packge.flags_field+"("+Flags.toString(packge.flags_field)+")");
			
				//JCClassDecl.name只是一个简单的类名(不含包名)
			c = reader.enterClass(tree.name, packge);
			
			DEBUG.P("packge.members()前="+packge.members());
			packge.members().enterIfAbsent(c);
			DEBUG.P("packge.members()后="+packge.members());
			
			//如果一个类是public的，则源文件名需和类名一样
			//否则报错:如:
			//Test4.java:25: class Test4s is public, should be declared in a file named Test4s.java
			//public class Test4s {
			//       ^
			if ((tree.mods.flags & PUBLIC) != 0 && !classNameMatchesFileName(c, env)) {
			log.error(tree.pos(),
				  "class.public.should.be.in.file", tree.name);
			}
				// </editor-fold>
		} else {
				// <editor-fold defaultstate="collapsed">
			if (tree.name.len != 0 &&
			!chk.checkUniqueClassName(tree.pos(), tree.name, enclScope)) {
				/*
				有两个或两个以上的成员类(或接口)同名时并不是在Parser阶段发现错误的
				而是在这里通过checkUniqueClassName()检查
				例如下面的代码:
				package my.test;
				public class Test {
					public class MyInnerClass {
					}
					
					public interface MyInnerClass {
					}
				}
				通过compiler.properties文件中的"compiler.err.already.defined"报错:
				bin\mysrc\my\test\Test.java:12: 已在 my.test.Test 中定义 my.test.Test.MyInnerClass
						public interface MyInnerClass {
							   ^
				1 错误
				*/
				
				DEBUG.P(2,this,"visitClassDef(1)");
				result = null;
				return;
			}
				// </editor-fold>
				// <editor-fold defaultstate="collapsed">
			if (owner.kind == TYP) {
				// We are seeing a member class.
				c = reader.enterClass(tree.name, (TypeSymbol)owner);

				DEBUG.P("owner.flags_field="+Flags.toString(owner.flags_field));
				DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));
				
				//接口中的成员的修饰符都包括PUBLIC和STATIC
				//注意在接口内部也可定义接口、类、枚举类型
				if ((owner.flags_field & INTERFACE) != 0) {
					tree.mods.flags |= PUBLIC | STATIC;
				}

				DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));

			} else {
				DEBUG.P("owner.kind!=TYP(注意)");
				// We are seeing a local class.
				c = reader.defineClass(tree.name, owner);
				c.flatname = chk.localClassName(c);
				DEBUG.P("c.flatname="+c.flatname);
				if (c.name.len != 0)
					chk.checkTransparentClass(tree.pos(), c, env.info.scope);
			}
				// </editor-fold>
		}
		tree.sym = c;
		
		DEBUG.P(2);
		DEBUG.P("JCClassDecl tree.sym="+tree.sym);
		DEBUG.P("JCClassDecl tree.sym.members_field="+tree.sym.members_field);
		DEBUG.P("ClassSymbol c.sourcefile="+c.sourcefile);
		DEBUG.P("ClassSymbol c.classfile="+c.classfile);
		DEBUG.P("if (chk.compiled.get(c.flatname) != null)="+(chk.compiled.get(c.flatname) != null));
		
		//在com.sun.tools.javac.comp.Check定义为:public Map<Name,ClassSymbol> compiled = new HashMap<Name, ClassSymbol>();
		
		// Enter class into `compiled' table and enclosing scope.
		if (chk.compiled.get(c.flatname) != null) {
			//在同一源文件中定义了两个同名的类
			duplicateClass(tree.pos(), c);
			result = new ErrorType(tree.name, (TypeSymbol)owner);
			tree.sym = (ClassSymbol)result.tsym;
			
			DEBUG.P(2,this,"visitClassDef(1)");
			return;
		}
		chk.compiled.put(c.flatname, c);
		enclScope.enter(c);
		DEBUG.P("Scope enclScope="+enclScope);
		//DEBUG.P("env="+env);
		

		// Set up an environment for class block and store in `typeEnvs'
		// table, to be retrieved later in memberEnter and attribution.
		Env<AttrContext> localEnv = classEnv(tree, env);
		typeEnvs.put(c, localEnv);
		
		DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
		DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));

		// Fill out class fields.
		c.completer = memberEnter;//这里要注意,往后complete()的调用转到MemberEnter了
		c.flags_field = chk.checkFlags(tree.pos(), tree.mods.flags, c, tree);
		c.sourcefile = env.toplevel.sourcefile;
		c.members_field = new Scope(c);
		
		DEBUG.P("ClassSymbol c.sourcefile="+c.sourcefile);
		DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
		
		ClassType ct = (ClassType)c.type;
		DEBUG.P("owner.kind="+Kinds.toString(owner.kind));
		DEBUG.P("ct.getEnclosingType()="+ct.getEnclosingType());
		
		/*如果是非静态成员类(不包括成员接口，成员枚举类)，将owner的type设成它的outer_field
		如下代码:只有MyInnerClass符合条件，将outer_field指向Test
		public class Test {
			public class MyInnerClass {}
			public static class MyInnerClassStatic {}
			public interface MyInnerInterface {}
			public static interface MyInnerInterfaceStatic {}
			public enum MyInnerEnum {}
			public static enum MyInnerEnumStatic {}
		}
		*/
		if (owner.kind != PCK && (c.flags_field & STATIC) == 0) {
			// We are seeing a local or inner class.
			// Set outer_field of this class to closest enclosing class
			// which contains this class in a non-static context
			// (its "enclosing instance class"), provided such a class exists.
			Symbol owner1 = owner;
			//注:在静态上下文(如：静态方法体)中是不能引用非静态类的，
			//按照上面这一点来理解while的条件组合就不会莫明其妙了
				
			//是一个本地类
			/*例:
			class EnterTest {
				static void methodA() {
					class LocalClass{} //ct.getEnclosingType()=<none>
				}
				void methodB() {
					class LocalClass{} //ct.getEnclosingType()=my.test.EnterTest
				}
			}
			*/
			while ((owner1.kind & (VAR | MTH)) != 0 &&
			   (owner1.flags_field & STATIC) == 0) { //静态方法中的本地类没有outer
				owner1 = owner1.owner;
			}
			if (owner1.kind == TYP) {
				ct.setEnclosingType(owner1.type);

				DEBUG.P("ct      ="+ct.tsym);
				DEBUG.P("ct.outer="+ct.getEnclosingType());
			}
		}
		DEBUG.P("ct.getEnclosingType()="+ct.getEnclosingType());
		DEBUG.P("ct.typarams_field="+ct.typarams_field);

		// Enter type parameters.
		ct.typarams_field = classEnter(tree.typarams, localEnv);
		
		DEBUG.P("ct.typarams_field="+ct.typarams_field);

        DEBUG.P(2);
        DEBUG.P("***Enter完Type Parameter***");
        DEBUG.P("-----------------------------------------------");
        DEBUG.P("类名: "+c);
        //注意Type Parameter并不是c.members_field的成员
        DEBUG.P("成员: "+c.members_field);
        DEBUG.P("Type Parameter: "+localEnv.info.scope);
       	DEBUG.P(2);

		DEBUG.P("if (!c.isLocal() && uncompleted != null)="+(!c.isLocal() && uncompleted != null));
		
		// Add non-local class to uncompleted, to make sure it will be
		// completed later.
		if (!c.isLocal() && uncompleted != null) uncompleted.append(c);
		//	System.err.println("entering " + c.fullname + " in " + c.owner);//DEBUG

		// Recursively enter all member classes.
		

		DEBUG.P("tree.type="+tree.type);
		classEnter(tree.defs, localEnv);
		//DEBUG.P("Enter.visitClassDef(JCClassDecl tree) stop",true);

		result = c.type;
		
			DEBUG.P(2);
			DEBUG.P("***类的所有成员Enter完成***");
			DEBUG.P("-----------------------------------------------");
			DEBUG.P("类名: "+c);
			DEBUG.P("成员: "+c.members_field);
			DEBUG.P("Type Parameter: "+localEnv.info.scope);
		
		//注意:方法中定义的类(本地类)并不Enter
		DEBUG.P(2,this,"visitClassDef(1)");
    }