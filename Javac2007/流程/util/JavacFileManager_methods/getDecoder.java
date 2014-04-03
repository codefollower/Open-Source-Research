    private CharsetDecoder getDecoder(String encodingName, boolean ignoreEncodingErrors) {
        Charset charset = (this.charset == null)
            ? Charset.forName(encodingName)
            : this.charset;
        CharsetDecoder decoder = charset.newDecoder();

        CodingErrorAction action;
        if (ignoreEncodingErrors)
            action = CodingErrorAction.REPLACE;
        else
            action = CodingErrorAction.REPORT;

        return decoder
            .onMalformedInput(action)
            .onUnmappableCharacter(action);
    }