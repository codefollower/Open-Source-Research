//Capture conversion
    // <editor-fold defaultstate="collapsed" desc="Capture conversion">
    /*
     * JLS 3rd Ed. 5.1.10 Capture Conversion:
     *
     * Let G name a generic type declaration with n formal type
     * parameters A1 ... An with corresponding bounds U1 ... Un. There
     * exists a capture conversion from G<T1 ... Tn> to G<S1 ... Sn>,
     * where, for 1 <= i <= n:
     *
     * + If Ti is a wildcard type argument (4.5.1) of the form ? then
     *   Si is a fresh type variable whose upper bound is
     *   Ui[A1 := S1, ..., An := Sn] and whose lower bound is the null
     *   type.
     *
     * + If Ti is a wildcard type argument of the form ? extends Bi,
     *   then Si is a fresh type variable whose upper bound is
     *   glb(Bi, Ui[A1 := S1, ..., An := Sn]) and whose lower bound is
     *   the null type, where glb(V1,... ,Vm) is V1 & ... & Vm. It is
     *   a compile-time error if for any two classes (not interfaces)
     *   Vi and Vj,Vi is not a subclass of Vj or vice versa.
     *
     * + If Ti is a wildcard type argument of the form ? super Bi,
     *   then Si is a fresh type variable whose upper bound is
     *   Ui[A1 := S1, ..., An := Sn] and whose lower bound is Bi.
     *
     * + Otherwise, Si = Ti.
     *
     * Capture conversion on any type other than a parameterized type
     * (4.5) acts as an identity conversion (5.1.1). Capture
     * conversions never require a special action at run time and
     * therefore never throw an exception at run time.
     *
     * Capture conversion is not applied recursively.
     */
    /**
     * Capture conversion as specified by JLS 3rd Ed.
     */
	public Type capture(Type t) {
		Type capture=null;//我加上的
		try {//我加上的
		DEBUG.P(this,"capture(Type t)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));

        if (t.tag != CLASS)
            return capture=t;
        ClassType cls = (ClassType)t;
        
        DEBUG.P("cls.isRaw()="+cls.isRaw());
		DEBUG.P("cls.isParameterized()="+cls.isParameterized());
		
        if (cls.isRaw() || !cls.isParameterized())
            return capture=cls;

        ClassType G = (ClassType)cls.asElement().asType();
        List<Type> A = G.getTypeArguments();
        List<Type> T = cls.getTypeArguments();
        List<Type> S = freshTypeVariables(T);

        List<Type> currentA = A;
        List<Type> currentT = T;
        List<Type> currentS = S;

		DEBUG.P("G="+G);
		DEBUG.P("A="+A);
		DEBUG.P("T="+T);
		DEBUG.P("S="+S);

		//G=test.attr.Test<T1 {bound=C1},T2 {bound=T1},T3 {bound=C3},T4 {bound=C4}>
		//A=T1 {bound=C1},T2 {bound=T1},T3 {bound=C3},T4 {bound=C4}
		//T=? extends test.attr.D1,? super test.attr.D2,test.attr.D3,?
		//S=capture#444 of ? extends test.attr.D1,capture#288 of ? super test.attr.D2,test.attr.D3,capture#802 of ?

        boolean captured = false;
        while (!currentA.isEmpty() &&
               !currentT.isEmpty() &&
               !currentS.isEmpty()) {
            if (currentS.head != currentT.head) {
                captured = true;
                WildcardType Ti = (WildcardType)currentT.head;
                Type Ui = currentA.head.getUpperBound();
                CapturedType Si = (CapturedType)currentS.head;

				DEBUG.P("Ti="+Ti);
				DEBUG.P("Ui="+Ui);
				DEBUG.P("Si="+Si);
                if (Ui == null)
                    Ui = syms.objectType;

				DEBUG.P("Ti.kind="+Ti.kind);
                switch (Ti.kind) {
                case UNBOUND:
                    Si.bound = subst(Ui, A, S);
                    Si.lower = syms.botType;
                    break;
                case EXTENDS:
                    Si.bound = glb(Ti.getExtendsBound(), subst(Ui, A, S));
                    Si.lower = syms.botType;
                    break;
                case SUPER:
                    Si.bound = subst(Ui, A, S);
                    Si.lower = Ti.getSuperBound();
                    break;
                }

				DEBUG.P("Si.bound="+Si.bound);
				DEBUG.P("Si.lower="+Si.lower);
				DEBUG.P("if (Si.bound == Si.lower)="+(Si.bound == Si.lower));
                if (Si.bound == Si.lower)
                    currentS.head = Si.bound;
            }
            currentA = currentA.tail;
            currentT = currentT.tail;
            currentS = currentS.tail;
        }
        if (!currentA.isEmpty() || !currentT.isEmpty() || !currentS.isEmpty())
            return capture=erasure(t); // some "rare" type involved

		DEBUG.P("captured="+captured);
        if (captured)
            return capture=new ClassType(cls.getEnclosingType(), S, cls.tsym);
        else
            return capture=t;

		}finally{//我加上的
		DEBUG.P("t      ="+t+"  t.tag      ="+TypeTags.toString(t.tag));
		DEBUG.P("capture="+capture+"  capture.tag="+TypeTags.toString(capture.tag));
		DEBUG.P(0,this,"capture(Type t)");
		}
    }

    // where
        private List<Type> freshTypeVariables(List<Type> types) {
			try {//我加上的
			DEBUG.P(this,"freshTypeVariables(1)");
			DEBUG.P("types="+types);

            ListBuffer<Type> result = lb();
            for (Type t : types) {
				DEBUG.P("t="+t+"  t.tag ="+TypeTags.toString(t.tag));
                if (t.tag == WILDCARD) {
                    Type bound = ((WildcardType)t).getExtendsBound();
					DEBUG.P("bound="+bound);
                    if (bound == null)
                        bound = syms.objectType;
                    result.append(new CapturedType(capturedName,
                                                   syms.noSymbol,
                                                   bound,
                                                   syms.botType,
                                                   (WildcardType)t));
                } else {
                    result.append(t);
                }
            }
			DEBUG.P("result.toList()="+result.toList());

            return result.toList();

			}finally{//我加上的
			DEBUG.P(0,this,"freshTypeVariables(1)");
			}
        }
    // </editor-fold>
//