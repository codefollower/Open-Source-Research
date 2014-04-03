    /** Is the annotation applicable to the symbol? */
    boolean annotationApplicable(JCAnnotation a, Symbol s) {
		try {//我加上的
		DEBUG.P(this,"annotationApplicable(2)");
		DEBUG.P("a="+a);
		DEBUG.P("s="+s+"  s.kind="+Kinds.toString(s.kind)+" s.isStatic()="+s.isStatic());
		
		Attribute.Compound atTarget =
			a.annotationType.type.tsym.attribute(syms.annotationTargetType.tsym);
		
		DEBUG.P("atTarget="+atTarget);
		if (atTarget == null) return true;
		Attribute atValue = atTarget.member(names.value);
		DEBUG.P("atValue="+atValue);
		DEBUG.P("(!(atValue instanceof Attribute.Array))="+(!(atValue instanceof Attribute.Array)));
		if (!(atValue instanceof Attribute.Array)) return true; // error recovery
		Attribute.Array arr = (Attribute.Array) atValue;
		for (Attribute app : arr.values) {
			DEBUG.P("(!(app instanceof Attribute.Enum))="+(!(app instanceof Attribute.Enum)));
			if (!(app instanceof Attribute.Enum)) return true; // recovery
			Attribute.Enum e = (Attribute.Enum) app;
			
			DEBUG.P("s.kind="+Kinds.toString(s.kind));
			DEBUG.P("s.owner.kind="+Kinds.toString(s.owner.kind));
			DEBUG.P("s.flags()="+Flags.toString(s.flags()));
			DEBUG.P("e.value.name="+e.value.name);
			if (e.value.name == names.TYPE)
			{ if (s.kind == TYP) return true; }
			else if (e.value.name == names.FIELD)
			{ if (s.kind == VAR && s.owner.kind != MTH) return true; }
			else if (e.value.name == names.METHOD)
			{ if (s.kind == MTH && !s.isConstructor()) return true; }
			else if (e.value.name == names.PARAMETER)
			{	
				if (s.kind == VAR &&
				  s.owner.kind == MTH &&
				  (s.flags() & PARAMETER) != 0)
				return true;
			}
			else if (e.value.name == names.CONSTRUCTOR)
			{ if (s.kind == MTH && s.isConstructor()) return true; }
			else if (e.value.name == names.LOCAL_VARIABLE)
			{ if (s.kind == VAR && s.owner.kind == MTH &&
				  (s.flags() & PARAMETER) == 0)
				return true;
			}
			else if (e.value.name == names.ANNOTATION_TYPE)
			{ if (s.kind == TYP && (s.flags() & ANNOTATION) != 0)
				return true;
			}
			else if (e.value.name == names.PACKAGE)
			{ if (s.kind == PCK) return true; }
			else
			return true; // recovery
		}
		return false;
		
		}finally{//我加上的
		DEBUG.P(0,this,"annotationApplicable(2)");
		}
    }