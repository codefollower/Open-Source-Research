    /** Convert (implicit) signature to type parameter.
     */
    Type sigToTypeParam() {
    	DEBUG.P(this,"sigToTypeParam()");
    	DEBUG.P("sigp="+sigp);
    	DEBUG.P("signature[sigp]="+(char)signature[sigp]);
    	
        int start = sigp;
        while (signature[sigp] != ':') sigp++;
        Name name = names.fromUtf(signature, start, sigp - start);
        TypeVar tvar;
		DEBUG.P("name="+name);
		DEBUG.P("sigEnterPhase="+sigEnterPhase);
		DEBUG.P("currentOwner="+currentOwner);
        if (sigEnterPhase) {
            tvar = new TypeVar(name, currentOwner);
            typevars.enter(tvar.tsym);
        } else {
            tvar = (TypeVar)findTypeVar(name);
        }
		DEBUG.P("tvar="+tvar);
        List<Type> bounds = List.nil();
        Type st = null;

		DEBUG.P("signature[sigp]="+(char)signature[sigp]);
		if(signature[sigp] == ':')
			DEBUG.P("signature[sigp+1]="+(char)signature[sigp+1]);

        if (signature[sigp] == ':' && signature[sigp+1] == ':') {
            sigp++;
            st = syms.objectType;
        }

		DEBUG.P("st="+st);
		DEBUG.P("signature[sigp]="+(char)signature[sigp]);
        while (signature[sigp] == ':') {
            sigp++;
            bounds = bounds.prepend(sigToType());
        }

		DEBUG.P("sigEnterPhase="+sigEnterPhase);
        if (!sigEnterPhase) {
            types.setBounds(tvar, bounds.reverse(), st);
        }
        
        DEBUG.P("tvar="+tvar);
        DEBUG.P(0,this,"sigToTypeParam()");
        return tvar;
    }