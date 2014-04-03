    /** Emit a class file for a given class.
     *  @param c      The class from which a class file is generated.
     */
    public JavaFileObject writeClass(ClassSymbol c)
        throws IOException, PoolOverflow, StringOverflow
    {
    	DEBUG.P(this,"writeClass(ClassSymbol c)");
		DEBUG.P("c="+c);
		DEBUG.P("c.sourcefile="+c.sourcefile);
		
        JavaFileObject outFile
            = fileManager.getJavaFileForOutput(CLASS_OUTPUT,
                                               c.flatname.toString(),
                                               JavaFileObject.Kind.CLASS,
                                               c.sourcefile);
        OutputStream out = outFile.openOutputStream();
        DEBUG.P("outFile="+outFile);
        try {
            writeClassFile(out, c);
            if (verbose)
                log.errWriter.println(log.getLocalizedString("verbose.wrote.file", outFile));
            out.close();
            out = null;
        } finally {
            if (out != null) {
                // if we are propogating an exception, delete the file
                out.close();
                outFile.delete();
                outFile = null;
            }
        }
        DEBUG.P(0,this,"writeClass(ClassSymbol c)");
        return outFile; // may be null if write failed
    }