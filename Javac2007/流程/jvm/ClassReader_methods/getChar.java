    /** Extract a character at position bp from buf.
     */
    char getChar(int bp) {
        return
            (char)(((buf[bp] & 0xFF) << 8) + (buf[bp+1] & 0xFF));
    }