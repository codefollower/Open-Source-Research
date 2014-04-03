    Type readEnumType(int i) {
        // support preliminary jsr175-format class files
        int index = poolIdx[i];
        int length = getChar(index + 1);
        if (buf[index + length + 2] != ';')
            return enterClass(readName(i)).type;
        return readType(i);
    }