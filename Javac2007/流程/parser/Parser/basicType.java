    /** BasicType = BYTE | SHORT | CHAR | INT | LONG | FLOAT | DOUBLE | BOOLEAN
     */
    JCPrimitiveTypeTree basicType() {
    	DEBUG.P(this,"basicType");
    	DEBUG.P("S.token()="+S.token());
    	
        JCPrimitiveTypeTree t = to(F.at(S.pos()).TypeIdent(typetag(S.token())));
        S.nextToken();
        
        DEBUG.P(0,this,"basicType");
        return t;
    }