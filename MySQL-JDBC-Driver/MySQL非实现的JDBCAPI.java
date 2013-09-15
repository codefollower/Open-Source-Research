com.mysql.jdbc.PreparedStatement :

未实现:
void setArray(int i, Array x)

setRef(int i, Ref x)


==========================
void setNull(int parameterIndex,
             int sqlType)
             throws SQLException

void setNull(int parameterIndex,
             int sqlType,
             String typeName)
             throws SQLException
未使用sqlType 和 typeName 这两个参数


在JDBC4PreparedStatement中实现下面三个方法
void setNClob(int parameterIndex,
              NClob value)
              throws SQLException

void setRowId(int parameterIndex,
              RowId x)
              throws SQLException

void setSQLXML(int parameterIndex,
               SQLXML xmlObject)
               throws SQLException























