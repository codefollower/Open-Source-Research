    /** A class for predefined operators.
     */
    public static class OperatorSymbol extends MethodSymbol {

        public int opcode;

        public OperatorSymbol(Name name, Type type, int opcode, Symbol owner) {
            super(PUBLIC | STATIC, name, type, owner);
            this.opcode = opcode;
        }
    }