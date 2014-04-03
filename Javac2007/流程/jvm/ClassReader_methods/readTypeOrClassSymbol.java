    Type readTypeOrClassSymbol(int i) {
        // support preliminary jsr175-format class files
        if (buf[poolIdx[i]] == CONSTANT_Class)
            return readClassSymbol(i).type;
        return readType(i);
    }