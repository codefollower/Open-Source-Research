    /** Read signature and convert to type.
     */
    Type readType(int i) {
        int index = poolIdx[i];//CONSTANT_Utf8类型
        return sigToType(buf, index + 3, getChar(index + 1));
    }