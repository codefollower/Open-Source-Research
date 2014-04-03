    /** Return code byte at position pc as an unsigned int.
     */
    private int get1(int pc) {
        return code[pc] & 0xFF;
    }

    /** Return two code bytes at position pc as an unsigned int.
     */
    private int get2(int pc) {
        return (get1(pc) << 8) | get1(pc+1);
    }

    /** Return four code bytes at position pc as an int.
     */
    public int get4(int pc) {
        // pre: pc + 4 <= cp
        return
            (get1(pc) << 24) |
            (get1(pc+1) << 16) |
            (get1(pc+2) << 8) |
            (get1(pc+3));
    }