        /** Does this symbol override `other' symbol, when both are seen as
         *  members of class `origin'?  It is assumed that _other is a member
         *  of origin.
         *
         *  It is assumed that both symbols have the same name.  The static
         *  modifier is ignored for this test.
         *
         *  See JLS 8.4.6.1 (without transitivity) and 8.4.6.4
         */
        //检查当前Symbol是否覆盖了Symbol _other
		//当前Symbol有可能是原始实现类(origin)或者超类中的方法
        public boolean overrides(Symbol _other, TypeSymbol origin, Types types, boolean checkResult) {
        	try {//我加上的
			DEBUG.P(this,"overrides(4)");
			DEBUG.P("this  ="+toString());
			DEBUG.P("_other="+_other);
			DEBUG.P("this.owner  ="+this.owner);
			DEBUG.P("_other.owner="+_other.owner);
			DEBUG.P("_other.kind ="+Kinds.toString(_other.kind));
			DEBUG.P("isConstructor()="+isConstructor());
            if (isConstructor() || _other.kind != MTH) return false;
            
            DEBUG.P("");
			DEBUG.P("TypeSymbol origin="+origin);
			DEBUG.P("boolean checkResult="+checkResult);
            DEBUG.P("(this == _other)="+(this == _other));
            if (this == _other) return true;
            MethodSymbol other = (MethodSymbol)_other;

            // check for a direct implementation

			/*在判断当前方法能否覆盖other方法前，先调用isOverridableIn
			判别other方法的修饰符(PRIVATE,PUBLIC,PROTECTED或没有)
			是否能在当前方法的owner中覆盖other，比如说，如果other
			方法的修饰符是PRIVATE，那么在owner中不能覆盖他。

			如果isOverridableIn返回true了，还必须确认other方法的owner
			是当前当前方法的owner的超类
			*/
            if (other.isOverridableIn((TypeSymbol)owner) &&
                types.asSuper(owner.type, other.owner) != null) {
                Type mt = types.memberType(owner.type, this);
                Type ot = types.memberType(owner.type, other);
                if (types.isSubSignature(mt, ot)) {
                    if (!checkResult) //检查方法返回类型
                        return true;
                    if (types.returnTypeSubstitutable(mt, ot))
                        return true;
                }
            }
			DEBUG.P("");
			DEBUG.P("this  ="+toString());
			DEBUG.P("_other="+_other);
			DEBUG.P("this.owner  ="+this.owner);
			DEBUG.P("_other.owner="+_other.owner);
			DEBUG.P("");
			DEBUG.P("this.flags() ="+Flags.toString(this.flags()));
			DEBUG.P("other.flags()="+Flags.toString(other.flags()));

            // check for an inherited implementation
            if ((flags() & ABSTRACT) != 0 ||
                (other.flags() & ABSTRACT) == 0 ||
                !other.isOverridableIn(origin) ||
                !this.isMemberOf(origin, types))
                return false;

            // assert types.asSuper(origin.type, other.owner) != null;
            Type mt = types.memberType(origin.type, this);
            Type ot = types.memberType(origin.type, other);
            return
                types.isSubSignature(mt, ot) &&
                (!checkResult || types.resultSubtype(mt, ot, Warner.noWarnings));
            }finally{//我加上的
			DEBUG.P(0,this,"overrides(4)");
			}
        }