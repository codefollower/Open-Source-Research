    /**
     * Decode a ByteBuffer into a CharBuffer.
     */
    private CharBuffer decode(ByteBuffer inbuf, boolean ignoreEncodingErrors) {
        String encodingName = getEncodingName();
        CharsetDecoder decoder;
        try {
            decoder = getDecoder(encodingName, ignoreEncodingErrors);
        } catch (IllegalCharsetNameException e) {
            log.error("unsupported.encoding", encodingName);
            return (CharBuffer)CharBuffer.allocate(1).flip();
        } catch (UnsupportedCharsetException e) {
            log.error("unsupported.encoding", encodingName);
            return (CharBuffer)CharBuffer.allocate(1).flip();
        }

        // slightly overestimate the buffer size to avoid reallocation.
        float factor =
            decoder.averageCharsPerByte() * 0.8f +
            decoder.maxCharsPerByte() * 0.2f;
        CharBuffer dest = CharBuffer.
            allocate(10 + (int)(inbuf.remaining()*factor));

        while (true) {
            CoderResult result = decoder.decode(inbuf, dest, true);
            dest.flip();

            if (result.isUnderflow()) { // done reading
                // make sure there is at least one extra character
                if (dest.limit() == dest.capacity()) {
                    dest = CharBuffer.allocate(dest.capacity()+1).put(dest);
                    dest.flip();
                }
                return dest;
            } else if (result.isOverflow()) { // buffer too small; expand
                int newCapacity =
                    10 + dest.capacity() +
                    (int)(inbuf.remaining()*decoder.maxCharsPerByte());
                dest = CharBuffer.allocate(newCapacity).put(dest);
            } else if (result.isMalformed() || result.isUnmappable()) {
                // bad character in input

                // report coding error (warn only pre 1.5)
                if (!getSource().allowEncodingErrors()) {
                    log.error(new SimpleDiagnosticPosition(dest.limit()),
                              "illegal.char.for.encoding",
                              charset == null ? encodingName : charset.name());
                } else {
                    log.warning(new SimpleDiagnosticPosition(dest.limit()),
                                "illegal.char.for.encoding",
                                charset == null ? encodingName : charset.name());
                }

                // skip past the coding error
                inbuf.position(inbuf.position() + result.length());

                // undo the flip() to prepare the output buffer
                // for more translation
                dest.position(dest.limit());
                dest.limit(dest.capacity());
                dest.put((char)0xfffd); // backward compatible
            } else {
                throw new AssertionError(result);
            }
        }
        // unreached
    }