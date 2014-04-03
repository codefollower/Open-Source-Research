        public Type getSuperclass() {
			try {//我加上的
			DEBUG.P(this,"getSuperclass()");

            complete();
            if (type instanceof ClassType) {
                ClassType t = (ClassType)type;
                if (t.supertype_field == null) // FIXME: shouldn't be null
                    t.supertype_field = Type.noType;
				// An interface has no superclass; its supertype is Object.
				return t.isInterface()
					? Type.noType
					: t.supertype_field;
            } else {
                return Type.noType;
            }

			}finally{//我加上的
			DEBUG.P(0,this,"getSuperclass()");
			}
        }