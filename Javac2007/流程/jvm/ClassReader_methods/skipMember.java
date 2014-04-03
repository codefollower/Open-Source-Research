    /** Skip a field or method
     */
    void skipMember() {
        bp = bp + 6;
        char ac = nextChar();
        for (int i = 0; i < ac; i++) {
            bp = bp + 2;
            int attrLen = nextInt();
            bp = bp + attrLen;
        }
    }