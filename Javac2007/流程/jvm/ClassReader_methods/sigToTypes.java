    /** Convert (implicit) signature to list of types
     *  until `terminator' is encountered.
     */
    List<Type> sigToTypes(char terminator) {
		DEBUG.P(this,"sigToTypes(1)");
		DEBUG.P("terminator="+terminator);
		
        List<Type> head = List.of(null);
        List<Type> tail = head;
        while (signature[sigp] != terminator)
            tail = tail.setTail(List.of(sigToType()));
        sigp++;
        
        DEBUG.P("head.tail="+head.tail);
        DEBUG.P(0,this,"sigToTypes(1)");
        return head.tail;
    }