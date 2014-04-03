//isReifiable
    // <editor-fold defaultstate="collapsed" desc="isReifiable">
    public boolean isReifiable(Type t) {
        //return isReifiable.visit(t);

		DEBUG.P(this,"isReifiable(1)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		boolean returnResult=isReifiable.visit(t);
		DEBUG.P("returnResult="+returnResult);
		DEBUG.P(1,this,"isReifiable(1)");
		return returnResult;
    }
    // where
        private UnaryVisitor<Boolean> isReifiable = new UnaryVisitor<Boolean>() {

            public Boolean visitType(Type t, Void ignored) {
                return true;
            }

            @Override
            public Boolean visitClassType(ClassType t, Void ignored) {
				//不带泛型参数时<...>时返回true
                if (!t.isParameterized())
                    return true;

				//全部是<?>时才返回true
                for (Type param : t.allparams()) {
                    if (!param.isUnbound())
                        return false;
                }
                return true;
            }

            @Override
            public Boolean visitArrayType(ArrayType t, Void ignored) {
                return visit(t.elemtype);
            }

            @Override
            public Boolean visitTypeVar(TypeVar t, Void ignored) {
                return false;
            }
        };
    // </editor-fold>
//