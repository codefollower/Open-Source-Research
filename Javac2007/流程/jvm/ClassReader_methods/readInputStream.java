    // where
        private static byte[] readInputStream(byte[] buf, InputStream s) throws IOException {
            try {
                buf = ensureCapacity(buf, s.available());
                int r = s.read(buf);
                int bp = 0;
                while (r != -1) {
                    bp += r;
                    buf = ensureCapacity(buf, bp);
                    r = s.read(buf, bp, buf.length - bp);
                }
                return buf;
            } finally {
                try {
                    s.close();
                } catch (IOException e) {
                    /* Ignore any errors, as this stream may have already
                     * thrown a related exception which is the one that
                     * should be reported.
                     */
                }
            }
        }
        private static byte[] ensureCapacity(byte[] buf, int needed) {
            if (buf.length < needed) {
                byte[] old = buf;
                buf = new byte[Integer.highestOneBit(needed) << 1];
                System.arraycopy(old, 0, buf, 0, old.length);
            }
            return buf;
        }
        /** Static factory for CompletionFailure objects.
         *  In practice, only one can be used at a time, so we share one
         *  to reduce the expense of allocating new exception objects.
         */
        private CompletionFailure newCompletionFailure(ClassSymbol c,
                                                       String localized) {
            if (!cacheCompletionFailure) {
                // log.warning("proc.messager",
                //             Log.getLocalizedString("class.file.not.found", c.flatname));
                // c.debug.printStackTrace();
                return new CompletionFailure(c, localized);
            } else {
                CompletionFailure result = cachedCompletionFailure;
                result.sym = c;
                result.errmsg = localized;
                return result;
            }
        }
        private CompletionFailure cachedCompletionFailure =
            new CompletionFailure(null, null);
        {
            cachedCompletionFailure.setStackTrace(new StackTraceElement[0]);
        }
