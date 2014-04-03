    /** Extract an integer at position bp from buf.
     */
    int getInt(int bp) {
        return
            ((buf[bp] & 0xFF) << 24) +
            ((buf[bp+1] & 0xFF) << 16) +
            ((buf[bp+2] & 0xFF) << 8) +
            (buf[bp+3] & 0xFF);
    }