    /** Are surrogates supported?
     */
    final static boolean surrogatesSupported = surrogatesSupported();
    private static boolean surrogatesSupported() {
        try {
            Character.isHighSurrogate('a');
            return true;
        } catch (NoSuchMethodError ex) {
            return false;
        }
    }

    /** Scan surrogate pairs.  If 'ch' is a high surrogate and
     *  the next character is a low surrogate, then put the low
     *  surrogate in 'ch', and return the high surrogate.
     *  otherwise, just return 0.
     */
    private char scanSurrogates() {
        if (surrogatesSupported && Character.isHighSurrogate(ch)) {
            char high = ch;

            scanChar();

            if (Character.isLowSurrogate(ch)) {
                return high;
            }

            ch = high;
        }

        return 0;
    }