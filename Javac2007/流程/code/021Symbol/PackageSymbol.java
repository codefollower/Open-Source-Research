    /** A class for package symbols
     */
    public static class PackageSymbol extends TypeSymbol
        implements PackageElement {

        public Scope members_field;
        public Name fullname;
        
        //对应package-info.java的情况
        public ClassSymbol package_info; // see bug 6443073

        public PackageSymbol(Name name, Type type, Symbol owner) {
        	//这里的0代表flags_field,因为是一个PackageSymbol,包是没有修饰符(modifier)的,
        	//所以用0表示(注:在类Flags中没有定义值为0的flag)
        	//DEBUG.P("flag=0 modifier=("+Flags.toString(0)+")");
        	
            super(0, name, type, owner);
            //当调用TypeSymbol的构造方法时,kind默认取值为TYP,所以得在这里修正为PCK
            this.kind = PCK;
            this.members_field = null;
            this.fullname = formFullName(name, owner);//在TypeSymbol中定义
        }

        public PackageSymbol(Name name, Symbol owner) {
            this(name, null, owner);
            this.type = new PackageType(this);
        }

        public String toString() {
            return fullname.toString();
        }

        public Name getQualifiedName() {
            return fullname;
        }

		public boolean isUnnamed() {
		    return name.isEmpty() && owner != null;
		}

        public Scope members() {
            if (completer != null) complete();
            return members_field;
        }

        public long flags() {
            if (completer != null) complete();
            return flags_field;
        }

        public List<Attribute.Compound> getAnnotationMirrors() {
            if (completer != null) complete();
            assert attributes_field != null;
            return attributes_field;
        }

        /** A package "exists" if a type or package that exists has
         *  been seen within it.
         */
        public boolean exists() {
            return (flags_field & EXISTS) != 0;
        }

        public ElementKind getKind() {
            return ElementKind.PACKAGE;
        }

        public Symbol getEnclosingElement() {
            return null;
        }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return v.visitPackage(this, p);
        }
    }