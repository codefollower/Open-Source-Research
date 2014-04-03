    long getLastModified(FileObject filename) {
        long mod = 0;
        try {
            mod = filename.getLastModified();
        } catch (SecurityException e) {
            throw new AssertionError("CRT: couldn't get source file modification date: " + e.getMessage());
        }
        return mod;
    }