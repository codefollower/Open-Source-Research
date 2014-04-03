    /** Given a field, return its name.
     */
    Name fieldName(Symbol sym) {
		//对于私有(PRIVATE)或非保护(PROTECTED)或非公有(PUBLIC)成员，
		//将其名称搅乱，采用sym.name.index命名
        if (scramble && (sym.flags() & PRIVATE) != 0 ||
            scrambleAll && (sym.flags() & (PROTECTED | PUBLIC)) == 0) //这个条件还是不正确，包成员会有错误
            return names.fromString("_$" + sym.name.index);
        else
            return sym.name;
    }