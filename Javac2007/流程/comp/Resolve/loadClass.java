    /** Load toplevel or member class with given fully qualified name and
     *  verify that it is accessible.
     *  @param env       The current environment.
     *  @param name      The fully qualified name of the class to be loaded.
     */
    Symbol loadClass(Env<AttrContext> env, Name name) {
        try {
            DEBUG.P(this,"loadClass(Env<AttrContext> env, Name name)");
            DEBUG.P("env="+env);
            DEBUG.P("name="+name);

            ClassSymbol c = reader.loadClass(name);
            DEBUG.P("ClassSymbol c="+c);
            return isAccessible(env, c) ? c : new AccessError(c);
        } catch (ClassReader.BadClassFile err) {
			DEBUG.P("ClassReader.BadClassFile="+err);
            throw err;
        } catch (CompletionFailure ex) {
			DEBUG.P("CompletionFailure="+ex);
            return typeNotFound;
        }finally{
        DEBUG.P(0,this,"loadClass(Env<AttrContext> env, Name name)");
        }
    }