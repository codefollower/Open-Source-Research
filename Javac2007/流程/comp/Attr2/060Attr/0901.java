        /**
         * A total ordering between type symbols that refines the
         * class inheritance graph.
         *
         * Typevariables always precede other kinds of symbols.
         */
        public final boolean precedes(TypeSymbol that, Types types) {
			try {//我加上的
			DEBUG.P(this,"precedes(2)");
			DEBUG.P("that="+that);
			DEBUG.P("(this == that)="+(this == that));

            if (this == that)
                return false;

			DEBUG.P("this.type="+this.type+"  this.type.tag="+TypeTags.toString(this.type.tag));
			DEBUG.P("that.type="+that.type+"  that.type.tag="+TypeTags.toString(that.type.tag));

            if (this.type.tag == that.type.tag) {
                if (this.type.tag == CLASS) {
                    return
                        types.rank(that.type) < types.rank(this.type) ||
                        types.rank(that.type) == types.rank(this.type) &&
                        that.getQualifiedName().compareTo(this.getQualifiedName()) < 0;
                } else if (this.type.tag == TYPEVAR) {
                    return types.isSubtype(this.type, that.type);
                }
            }
            return this.type.tag == TYPEVAR;

			}finally{//我加上的
			DEBUG.P(0,this,"precedes(2)");
			}
        }