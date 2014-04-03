    /** Extract a double at position bp from buf.
     */
    double getDouble(int bp) {
        DataInputStream bufin =
            new DataInputStream(new ByteArrayInputStream(buf, bp, 8));
        try {
            return bufin.readDouble();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }