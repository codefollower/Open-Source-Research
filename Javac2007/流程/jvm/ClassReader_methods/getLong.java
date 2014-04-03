    /** Extract a long integer at position bp from buf.
     */
    long getLong(int bp) {
        DataInputStream bufin =
            new DataInputStream(new ByteArrayInputStream(buf, bp, 8));
        try {
            return bufin.readLong();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
