    /** Extract a float at position bp from buf.
     */
    float getFloat(int bp) {
        DataInputStream bufin =
            new DataInputStream(new ByteArrayInputStream(buf, bp, 4));
        try {
            return bufin.readFloat();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }