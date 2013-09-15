package com.ibatis.sqlmap.engine.mapping.statement;

public final class StatementType {

    public static final StatementType UNKNOWN = new StatementType();
    public static final StatementType INSERT = new StatementType();
    public static final StatementType UPDATE = new StatementType();
    public static final StatementType DELETE = new StatementType();
    public static final StatementType SELECT = new StatementType();
    public static final StatementType PROCEDURE = new StatementType();

    private StatementType() {
    }

}
