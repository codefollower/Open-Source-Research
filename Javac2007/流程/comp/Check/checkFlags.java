    /** Check that given modifiers are legal for given symbol and
     *  return modifiers together with any implicit modififiers for that symbol.
     *  Warning: we can't use flags() here since this method
     *  is called during class enter, when flags() would cause a premature
     *  completion.
     *  @param pos           Position to be used for error reporting.
     *  @param flags         The set of modifiers given in a definition.
     *  @param sym           The defined symbol.
     */
    long checkFlags(DiagnosticPosition pos, long flags, Symbol sym, JCTree tree) {
		DEBUG.P(this,"checkFlags(4)");
		DEBUG.P("flags="+Flags.toString(flags));
		DEBUG.P("sym.kind="+Kinds.toString(sym.kind));

		long mask;
		long implicit = 0;
		switch (sym.kind) {
			case VAR:
				if (sym.owner.kind != TYP)
					mask = LocalVarFlags; //本地变量
				else if ((sym.owner.flags_field & INTERFACE) != 0)
					mask = implicit = InterfaceVarFlags; //接口中定义的字段
				else
					mask = VarFlags; //类中定义的字段
				break;
			case MTH:
				DEBUG.P("sym.name="+sym.name);
				DEBUG.P("if (sym.name == names.init)="+(sym.name == names.init));
				DEBUG.P("sym.owner.flags_field="+Flags.toString(sym.owner.flags_field));
				if (sym.name == names.init) {
					if ((sym.owner.flags_field & ENUM) != 0) { 
						/*错误例子:
						bin\mysrc\my\test\Test.java:16: 此处不允许使用修饰符 public
						   public MyInnerEnum() {}
								  ^
						bin\mysrc\my\test\Test.java:16: 此处不允许使用修饰符 protected
							protected MyInnerEnum() {}
									  ^         
						*/
						// enum constructors cannot be declared public or
						// protected and must be implicitly or explicitly
						// private
						implicit = PRIVATE;
						mask = PRIVATE;
					} else
						mask = ConstructorFlags;
				} else if ((sym.owner.flags_field & INTERFACE) != 0)
					mask = implicit = InterfaceMethodFlags;
				else {
					mask = MethodFlags;
				}

				//如果方法不是抽象的(abstract)，
				//并且定义方法的类型含有strictfp修饰符，
				//则此方法也默认含有strictfp修饰符
				//接口方法默认是public abstract，不会有strictfp修饰符
				DEBUG.P("((flags|implicit) & Flags.ABSTRACT)="+Flags.toString(((flags|implicit) & Flags.ABSTRACT)));
				// Imply STRICTFP if owner has STRICTFP set.
				if (((flags|implicit) & Flags.ABSTRACT) == 0) //参考<<深入java虚拟机>>P290
					implicit |= sym.owner.flags_field & STRICTFP;
				DEBUG.P("implicit="+Flags.toString(implicit));
				break;
			case TYP:
				DEBUG.P("sym.isLocal()="+sym.isLocal());
				DEBUG.P("sym.owner.kind="+Kinds.toString(sym.owner.kind));
				if (sym.isLocal()) {
					mask = LocalClassFlags;
					if (sym.name.len == 0) { // Anonymous class
						// Anonymous classes in static methods are themselves static;
						// that's why we admit STATIC here.
						mask |= STATIC;
						// JLS: Anonymous classes are final.
						implicit |= FINAL;
					}
						
					if ((sym.owner.flags_field & STATIC) == 0 &&
						(flags & ENUM) != 0)
						log.error(pos, "enums.must.be.static");
				} else if (sym.owner.kind == TYP) {
					mask = MemberClassFlags;
					if (sym.owner.owner.kind == PCK ||
						(sym.owner.flags_field & STATIC) != 0)
						mask |= STATIC;
					/*源码例子:
					public class Test {
						public class MyInnerClass {
							public enum MyInnerEnum2{}
						}
					}
					
					错误提示:
					bin\mysrc\my\test\Test.java:11: 只有在静态上下文中才允许使用枚举声明
							public enum MyInnerEnum2{}
								   ^
					*/
					else if ((flags & ENUM) != 0)
						log.error(pos, "enums.must.be.static");
					// Nested interfaces and enums are always STATIC (Spec ???)
					if ((flags & (INTERFACE | ENUM)) != 0 ) implicit = STATIC;
				} else {
					mask = ClassFlags;
				}
				// Interfaces are always ABSTRACT
				if ((flags & INTERFACE) != 0) implicit |= ABSTRACT;

				if ((flags & ENUM) != 0) {
					// enums can't be declared abstract or final
					mask &= ~(ABSTRACT | FINAL);
					implicit |= implicitEnumFinalFlag(tree);
				}
				// Imply STRICTFP if owner has STRICTFP set.
				implicit |= sym.owner.flags_field & STRICTFP;
				break;
			default:
				throw new AssertionError();
		}

		//mask的值表示能用在VAR、MTH、TYP前的所有修饰符的集合(见Flags类中的Modifier masks)
		//如果在一个接口中这样定义一个方法:strictfp void methodA();
		//因为mask = InterfaceMethodFlags = ABSTRACT | PUBLIC
		//所以就会报错:此处不允许使用修饰符 strictfp
		long illegal = flags & StandardFlags & ~mask;
        if (illegal != 0) {
			if ((illegal & INTERFACE) != 0) {
				log.error(pos, "intf.not.allowed.here");
				mask |= INTERFACE;
			}
			else {
				log.error(pos, "mod.not.allowed.here", TreeInfo.flagNames(illegal));
			}
		}
        else if ((sym.kind == TYP ||
		  // ISSUE: Disallowing abstract&private is no longer appropriate
		  // in the presence of inner classes. Should it be deleted here?
		  checkDisjoint(pos, flags,
				ABSTRACT,  //ABSTRACT与"PRIVATE,STATIC"其中之一不能同时出现
				PRIVATE | STATIC))//如static abstract void methodC();非法的修饰符组合 abstract 和  static
		 && //下面的checkDisjoint同上，都是第三个参数与第四个参数中所含的修饰符不能同时出现在flags中
		 checkDisjoint(pos, flags,
			       ABSTRACT | INTERFACE,
			       FINAL | NATIVE | SYNCHRONIZED)
		 &&
                 checkDisjoint(pos, flags,
                               PUBLIC,
                               PRIVATE | PROTECTED)
		 &&
                 checkDisjoint(pos, flags,
                               PRIVATE,
                               PUBLIC | PROTECTED)
		 &&
		 checkDisjoint(pos, flags,
			       FINAL,
			       VOLATILE)
		 &&
		 (sym.kind == TYP ||
		  checkDisjoint(pos, flags,
				ABSTRACT | NATIVE,
				STRICTFP))) {
	    // skip
        }
        DEBUG.P("flags="+Flags.toString(flags));
        DEBUG.P("mask="+Flags.toString(mask));
        DEBUG.P("implicit="+Flags.toString(implicit));
        DEBUG.P("returnFlags="+(flags & (mask | ~StandardFlags) | implicit)+"("+Flags.toString((flags & (mask | ~StandardFlags) | implicit))+")");
        DEBUG.P(0,this,"checkFlags(4)");
        return flags & (mask | ~StandardFlags) | implicit;
    }