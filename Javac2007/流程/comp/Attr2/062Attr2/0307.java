        //根据方法前的修饰符(PRIVATE,PUBLIC,PROTECTED或没有)
        //来决定实现类是否能覆盖此方法
        private boolean isOverridableIn(TypeSymbol origin) {
			/*
            // JLS3 8.4.6.1
            switch ((int)(flags_field & Flags.AccessFlags)) {
            case Flags.PRIVATE:
                return false;
            case Flags.PUBLIC:
                return true;
            case Flags.PROTECTED:
                return (origin.flags() & INTERFACE) == 0;
            case 0:
                // for package private: can only override in the same
                // package
                return
                    this.packge() == origin.packge() &&
                    (origin.flags() & INTERFACE) == 0;
            default:
                return false;
            }
			*/

			boolean isOverridableIn=false;
			DEBUG.P(this,"isOverridableIn(TypeSymbol origin)");
			DEBUG.P("flags_field="+Flags.toString(flags_field));
			DEBUG.P("flags_field & AccessFlags="+Flags.toString(flags_field & AccessFlags));
		
			DEBUG.P("  this="+toString()+"    this.owner="+this.owner);
			DEBUG.P("this.packge()="+this.packge());
			DEBUG.P("origin.packge()="+origin.packge());
			DEBUG.P("origin="+origin);
			DEBUG.P("origin.flags_field="+Flags.toString(origin.flags_field));

			switch ((int)(flags_field & Flags.AccessFlags)) {
            case Flags.PRIVATE:
                isOverridableIn= false;break;
            case Flags.PUBLIC:
                isOverridableIn= true;break;
            case Flags.PROTECTED:
                isOverridableIn= (origin.flags() & INTERFACE) == 0;break;
            case 0:
                // for package private: can only override in the same
                // package
                isOverridableIn=
                    this.packge() == origin.packge() &&
                    (origin.flags() & INTERFACE) == 0;break;
            default:
                isOverridableIn= false;
            }

			DEBUG.P("");
			DEBUG.P("isOverridableIn="+isOverridableIn);
			DEBUG.P(0,this,"isOverridableIn(TypeSymbol origin)");
			return isOverridableIn;
        }