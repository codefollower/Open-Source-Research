    private File getClassOutDir() {
        if (classOutDir == uninited)
            classOutDir = getOutputLocation(null, D);
        return classOutDir;
    }