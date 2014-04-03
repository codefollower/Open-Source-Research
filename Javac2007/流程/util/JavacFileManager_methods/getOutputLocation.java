    private File getOutputLocation(File dir, OptionName defaultOptionName) {
        if (dir != null)
            return dir;
        String arg = options.get(defaultOptionName);
        if (arg == null)
            return null;
        return new File(arg);
    }