    /**
     * A class definition.
     * @param modifiers the modifiers
     * @param name the name of the class
     * @param typarams formal class parameters
     * @param extending the classes this class extends
     * @param implementing the interfaces implemented by this class
     * @param defs all variables and methods defined in this class
     * @param sym the symbol
     */
    public static class JCClassDecl extends JCStatement implements ClassTree {
    	//举例:public class Test<S extends TestBound & MyInterfaceA, T> extends TestOhter<Integer,String> implements MyInterfaceA,MyInterfaceB
    	
        public JCModifiers mods; //对应 public
        public Name name; //对应 Test 只是一个简单的类名(不含包名)

		//typarams一定不为null(见Parser.classDeclaration(2))
        public List<JCTypeParameter> typarams; //对应 <S extends TestBound & MyInterfaceA, T>
        public JCTree extending; //对应 TestOhter<Integer,String>
        public List<JCExpression> implementing; //对应 MyInterfaceA,MyInterfaceB
        public List<JCTree> defs;
        
        //sym.members_field是一个Scope,这个Scope里的每一个Entry
        //代表一个成员类(或成员接口)，但是不包括type parameter
        //每个Entry是在Enter阶段加入的
        public ClassSymbol sym;
        protected JCClassDecl(JCModifiers mods,
			   Name name,
			   List<JCTypeParameter> typarams,
			   JCTree extending,
			   List<JCExpression> implementing,
			   List<JCTree> defs,
			   ClassSymbol sym)
	{
            super(CLASSDEF);
            this.mods = mods;
            this.name = name;
            this.typarams = typarams;
            this.extending = extending;
            this.implementing = implementing;
            this.defs = defs;
            this.sym = sym;
        }
        @Override
        public void accept(Visitor v) { v.visitClassDef(this); }

        public Kind getKind() { return Kind.CLASS; }
        public JCModifiers getModifiers() { return mods; }
        public Name getSimpleName() { return name; }
        public List<JCTypeParameter> getTypeParameters() {
            return typarams;
        }
        public JCTree getExtendsClause() { return extending; }
        public List<JCExpression> getImplementsClause() {
            return implementing;
        }
        public List<JCTree> getMembers() {
            return defs;
        }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitClass(this, d);
        }
    }