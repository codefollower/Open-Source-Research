    /**
     * Make a byte buffer from an input stream.
     */
    private ByteBuffer makeByteBuffer(InputStream in)
        throws IOException {
        int limit = in.available();
        if (mmappedIO && in instanceof FileInputStream) {
            // Experimental memory mapped I/O
            FileInputStream fin = (FileInputStream)in;
            return fin.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, limit);
        }
        if (limit < 1024) limit = 1024;
        ByteBuffer result = byteBufferCache.get(limit);
        int position = 0;
        while (in.available() != 0) {
            if (position >= limit)
                // expand buffer
                result = ByteBuffer.
                    allocate(limit <<= 1).
                    put((ByteBuffer)result.flip());
            int count = in.read(result.array(),
                position,
                limit - position);
            if (count < 0) break;
            result.position(position += count);
        }
        return (ByteBuffer)result.flip();
    }