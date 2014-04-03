    /** Generate code and emit a class file for a given class
     *  @param env    The attribution environment of the outermost class
     *                containing this class.
     *  @param cdef   The class definition from which code is generated.
     */
    JavaFileObject genCode(Env<AttrContext> env, JCClassDecl cdef) throws IOException {
        try {//我加上的
        DEBUG.P(this,"genCode(2)"); 
	    DEBUG.P("env="+env);
        DEBUG.P("cdef.sym="+cdef.sym);
        
        try {
            if (gen.genClass(env, cdef)) 
                return writer.writeClass(cdef.sym);
        } catch (ClassWriter.PoolOverflow ex) {
            log.error(cdef.pos(), "limit.pool");
        } catch (ClassWriter.StringOverflow ex) {
            log.error(cdef.pos(), "limit.string.overflow",
                      ex.value.substring(0, 20));
        } catch (CompletionFailure ex) {
            chk.completionError(cdef.pos(), ex);
        }
        return null;
        
        }finally{//我加上的
		DEBUG.P(1,this,"genCode(2)"); 
		}
    }