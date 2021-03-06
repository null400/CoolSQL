/*
 * Copyright 2004, 2005, 2006 H2 Group.
 */
package com.cattsoft.coolsql.adapters.dialect;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.exception.TemplatedViolatedConstraintNameExtracter;
import org.hibernate.exception.ViolatedConstraintNameExtracter;

import com.cattsoft.coolsql.pub.exception.UnifyException;
import com.cattsoft.coolsql.sql.ISQLDatabaseMetaData;
import com.cattsoft.coolsql.sql.model.Column;
import com.cattsoft.coolsql.sql.model.Table;

public class H2Dialect extends Dialect implements HibernateDialect {

    
    static final String DEFAULT_BATCH_SIZE = "15";
    
    public H2Dialect() {
        super();
        registerColumnType(Types.ARRAY, "array");
        registerColumnType(Types.BIGINT, "bigint");
        registerColumnType(Types.BINARY, "binary");
        registerColumnType(Types.BIT, "boolean");
        registerColumnType(Types.BOOLEAN, "boolean");
        registerColumnType(Types.BLOB, "blob");
        registerColumnType(Types.CHAR, "varchar($l)");
        registerColumnType(Types.CLOB, "clob");
        registerColumnType(Types.DATE, "date");
        registerColumnType(Types.DECIMAL, "decimal($p,$s)");
        registerColumnType(Types.DOUBLE, "double");
        registerColumnType(Types.FLOAT, "float");
        registerColumnType(Types.INTEGER, "integer");
        registerColumnType(Types.LONGVARBINARY, "longvarbinary");
        registerColumnType(Types.LONGVARCHAR, "longvarchar");
        registerColumnType(Types.NUMERIC, "numeric");
        registerColumnType(Types.REAL, "real");
        registerColumnType(Types.SMALLINT, "smallint");
        registerColumnType(Types.TIME, "time");
        registerColumnType(Types.TIMESTAMP, "timestamp");
        registerColumnType(Types.TINYINT, "tinyint");
        registerColumnType(Types.VARBINARY, "binary($l)");
        registerColumnType(Types.VARCHAR, "varchar($l)");        
        
        // select topic, syntax from information_schema.help
        // where section like 'Function%' order by section, topic

//        registerFunction("abs", new StandardSQLFunction("abs"));
        registerFunction("acos", new StandardSQLFunction("acos", Hibernate.DOUBLE));
        registerFunction("asin", new StandardSQLFunction("asin", Hibernate.DOUBLE));
        registerFunction("atan", new StandardSQLFunction("atan", Hibernate.DOUBLE));
        registerFunction("atan2", new StandardSQLFunction("atan2", Hibernate.DOUBLE));
        registerFunction("bitand", new StandardSQLFunction("bitand", Hibernate.INTEGER));
        registerFunction("bitor", new StandardSQLFunction("bitor", Hibernate.INTEGER));
        registerFunction("bitxor", new StandardSQLFunction("bitxor", Hibernate.INTEGER));
        registerFunction("ceiling", new StandardSQLFunction("ceiling", Hibernate.DOUBLE));
        registerFunction("cos", new StandardSQLFunction("cos", Hibernate.DOUBLE));
        registerFunction("cot", new StandardSQLFunction("cot", Hibernate.DOUBLE));
        registerFunction("degrees", new StandardSQLFunction("degrees", Hibernate.DOUBLE));
        registerFunction("exp", new StandardSQLFunction("exp", Hibernate.DOUBLE));
        registerFunction("floor", new StandardSQLFunction("floor", Hibernate.DOUBLE));
        registerFunction("log", new StandardSQLFunction("log", Hibernate.DOUBLE));
        registerFunction("log10", new StandardSQLFunction("log10", Hibernate.DOUBLE));
//        registerFunction("mod", new StandardSQLFunction("mod", Hibernate.INTEGER));
        registerFunction("pi", new NoArgSQLFunction("pi", Hibernate.DOUBLE));
        registerFunction("power", new StandardSQLFunction("power", Hibernate.DOUBLE));
        registerFunction("radians", new StandardSQLFunction("radians", Hibernate.DOUBLE));
        registerFunction("rand", new NoArgSQLFunction("rand", Hibernate.DOUBLE));
        registerFunction("round", new StandardSQLFunction("round", Hibernate.DOUBLE));
        registerFunction("roundmagic", new StandardSQLFunction("roundmagic", Hibernate.DOUBLE));
        registerFunction("sign", new StandardSQLFunction("sign", Hibernate.INTEGER));
        registerFunction("sin", new StandardSQLFunction("sin", Hibernate.DOUBLE));
//        registerFunction("sqrt", new StandardSQLFunction("sqrt", Hibernate.DOUBLE));
        registerFunction("tan", new StandardSQLFunction("tan", Hibernate.DOUBLE));
        registerFunction("truncate", new StandardSQLFunction("truncate", Hibernate.DOUBLE));

        registerFunction("compress", new StandardSQLFunction("compress", Hibernate.BINARY));
        registerFunction("expand", new StandardSQLFunction("compress", Hibernate.BINARY));
        registerFunction("decrypt", new StandardSQLFunction("decrypt", Hibernate.BINARY));
        registerFunction("encrypt", new StandardSQLFunction("encrypt", Hibernate.BINARY));
        registerFunction("hash", new StandardSQLFunction("hash", Hibernate.BINARY));

        registerFunction("ascii", new StandardSQLFunction("ascii", Hibernate.INTEGER));
//        registerFunction("bit_length", new StandardSQLFunction("bit_length", Hibernate.INTEGER));
        registerFunction("char", new StandardSQLFunction("char", Hibernate.CHARACTER));
        registerFunction("concat", new VarArgsSQLFunction(Hibernate.STRING, "(", "||", ")"));
        registerFunction("difference", new StandardSQLFunction("difference", Hibernate.INTEGER));
        registerFunction("hextoraw", new StandardSQLFunction("hextoraw", Hibernate.STRING));
        registerFunction("lower", new StandardSQLFunction("lower", Hibernate.STRING));
        registerFunction("insert", new StandardSQLFunction("lower", Hibernate.STRING));
        registerFunction("left", new StandardSQLFunction("left", Hibernate.STRING));
//        registerFunction("length", new StandardSQLFunction("length", Hibernate.INTEGER));
//        registerFunction("locate", new StandardSQLFunction("locate", Hibernate.INTEGER));
//        registerFunction("lower", new StandardSQLFunction("lower", Hibernate.STRING));
        registerFunction("lcase", new StandardSQLFunction("lcase", Hibernate.STRING));
        registerFunction("ltrim", new StandardSQLFunction("ltrim", Hibernate.STRING));
        registerFunction("octet_length", new StandardSQLFunction("octet_length", Hibernate.INTEGER));
        registerFunction("position", new StandardSQLFunction("position", Hibernate.INTEGER));
        registerFunction("rawtohex", new StandardSQLFunction("rawtohex", Hibernate.STRING));
        registerFunction("repeat", new StandardSQLFunction("repeat", Hibernate.STRING));
        registerFunction("replace", new StandardSQLFunction("replace", Hibernate.STRING));
        registerFunction("right", new StandardSQLFunction("right", Hibernate.STRING));
        registerFunction("rtrim", new StandardSQLFunction("rtrim", Hibernate.STRING));
        registerFunction("soundex", new StandardSQLFunction("soundex", Hibernate.STRING));
        registerFunction("space", new StandardSQLFunction("space", Hibernate.STRING));
        registerFunction("stringencode", new StandardSQLFunction("stringencode", Hibernate.STRING));
        registerFunction("stringdecode", new StandardSQLFunction("stringdecode", Hibernate.STRING));
//        registerFunction("substring", new StandardSQLFunction("substring", Hibernate.STRING));
//        registerFunction("upper", new StandardSQLFunction("upper", Hibernate.STRING));
        registerFunction("ucase", new StandardSQLFunction("ucase", Hibernate.STRING));

        registerFunction("stringtoutf8", new StandardSQLFunction("stringtoutf8", Hibernate.BINARY));
        registerFunction("utf8tostring", new StandardSQLFunction("utf8tostring", Hibernate.STRING));

        registerFunction("current_date", new NoArgSQLFunction("current_date", Hibernate.DATE));
        registerFunction("current_time", new NoArgSQLFunction("current_time", Hibernate.TIME));
        registerFunction("current_timestamp", new NoArgSQLFunction("current_timestamp", Hibernate.TIMESTAMP));
        registerFunction("datediff", new NoArgSQLFunction("datediff", Hibernate.INTEGER));
        registerFunction("dayname", new StandardSQLFunction("dayname", Hibernate.STRING));
        registerFunction("dayofmonth", new StandardSQLFunction("dayofmonth", Hibernate.INTEGER));
        registerFunction("dayofweek", new StandardSQLFunction("dayofweek", Hibernate.INTEGER));
        registerFunction("dayofyear", new StandardSQLFunction("dayofyear", Hibernate.INTEGER));
//        registerFunction("hour", new StandardSQLFunction("hour", Hibernate.INTEGER));
//        registerFunction("minute", new StandardSQLFunction("minute", Hibernate.INTEGER));
//        registerFunction("month", new StandardSQLFunction("month", Hibernate.INTEGER));
        registerFunction("monthname", new StandardSQLFunction("monthname", Hibernate.STRING));
        registerFunction("quater", new StandardSQLFunction("quater", Hibernate.INTEGER));
//        registerFunction("second", new StandardSQLFunction("second", Hibernate.INTEGER));
        registerFunction("week", new StandardSQLFunction("week", Hibernate.INTEGER));
//        registerFunction("year", new StandardSQLFunction("year", Hibernate.INTEGER));

        registerFunction("curdate", new NoArgSQLFunction("curdate", Hibernate.DATE));
        registerFunction("curtime", new NoArgSQLFunction("curtime", Hibernate.TIME));
        registerFunction("curtimestamp", new NoArgSQLFunction("curtimestamp", Hibernate.TIME));
        registerFunction("now", new NoArgSQLFunction("now", Hibernate.TIMESTAMP));

        registerFunction("database", new NoArgSQLFunction("database", Hibernate.STRING));
        registerFunction("user", new NoArgSQLFunction("user", Hibernate.STRING));

        getDefaultProperties().setProperty(Environment.STATEMENT_BATCH_SIZE, DEFAULT_BATCH_SIZE);

    }

    public String getAddColumnString() {
        return "add column";
    }

    public boolean supportsIdentityColumns() {
        return true;
    }

    public String getIdentityColumnString() {
        return "generated by default as identity"; // not null is implicit
    }

    public String getIdentitySelectString() {
        return "call identity()";
    }

    public String getIdentityInsertString() {
        return "null";
    }

    public String getForUpdateString() {
        return " for update";
    }

    public boolean supportsUnique() {
        return true;
    }

    public boolean supportsLimit() {
        return true;
    }

    public String getLimitString(String sql, boolean hasOffset) {
        return new StringBuffer(sql.length() + 20).
            append(sql).
            append(hasOffset ? " limit ? offset ?" : " limit ?").
            toString();
    }
    
    public boolean bindLimitParametersInReverseOrder() {
        return true;
    }    

    public boolean bindLimitParametersFirst() {
        return false;
    }

    public boolean supportsIfExistsAfterTableName() {
        return true;
    }

    public String[] getCreateSequenceStrings(String sequenceName) {
        return new String[] {
                "create sequence " + sequenceName
        };
    }

    public String[] getDropSequenceStrings(String sequenceName) {
        return new String[] {
                "drop sequence " + sequenceName
        };
    }

    public String getSelectSequenceNextValString(String sequenceName) {
        return "next value for " + sequenceName;
    }

    public String getSequenceNextValString(String sequenceName) {
        return "call next value for " + sequenceName;
    }

    public String getQuerySequencesString() {
        return "select name from information_schema.sequences";
    }

    public boolean supportsSequences() {
        return true;
    }

    public ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter() {
        return EXTRACTER;
    }

    private static ViolatedConstraintNameExtracter EXTRACTER = new TemplatedViolatedConstraintNameExtracter() {

        /**
         * Extract the name of the violated constraint from the given SQLException.
         *
         * @param sqle The exception that was the result of the constraint violation.
         * @return The extracted constraint name.
         */
        public String extractConstraintName(SQLException sqle) {
            String constraintName = null;
            // 23000: Check constraint violation: {0}
            // 23001: Unique index or primary key violation: {0}
            if(sqle.getSQLState().startsWith("23")) {
                String message = sqle.getMessage();
                int idx = message.indexOf("violation: ");
                if(idx > 0) {
                    constraintName = message.substring(idx + "violation: ".length());
                }
            }
            return constraintName;
        }

    };

    public boolean supportsTemporaryTables() {
        return true;
    }
    
    public String getCreateTemporaryTableString() {
        return "create temporary table if not exists";
    }

    public boolean supportsCurrentTimestampSelection() {
        return true;
    }
    
    public boolean isCurrentTimestampSelectStringCallable() {
        return false;
    }
    
    public String getCurrentTimestampSelectString() {
        return "call current_timestamp()";
    }    
    
    public boolean supportsUnionAll() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.coolsql.adapters.dialect.HibernateDialect#getLengthFunction(int)
     */
    public String getLengthFunction(int dataType) {
        return "length";
    }

    /* (non-Javadoc)
     * @see com.coolsql.adapters.dialect.HibernateDialect#getMaxFunction()
     */
    public String getMaxFunction() {
        return "max";
    }

    /* (non-Javadoc)
     * @see com.coolsql.adapters.dialect.HibernateDialect#getMaxPrecision(int)
     */
    public int getMaxPrecision(int dataType) {
        return Integer.MAX_VALUE;
    }

    /* (non-Javadoc)
     * @see com.coolsql.adapters.dialect.HibernateDialect#getMaxScale(int)
     */
    public int getMaxScale(int dataType) {
        return getMaxPrecision(dataType);
    }

    /* (non-Javadoc)
     * @see com.coolsql.adapters.dialect.HibernateDialect#getPrecisionDigits(int, int)
     */
    public int getPrecisionDigits(int columnSize, int dataType) {
        return columnSize;
    }

    /* (non-Javadoc)
     * @see com.coolsql.adapters.dialect.HibernateDialect#supportsSchemasInTableDefinition()
     */
    public boolean supportsSchemasInTableDefinition() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.coolsql.adapters.dialect.HibernateDialect#getColumnLength(int, int)
     */
    public int getColumnLength(int columnSize, int dataType) {
        return columnSize;
    }
 
    /**
     * The string which identifies this dialect in the dialect chooser.
     * 
     * @return a descriptive name that tells the user what database this dialect
     *         is design to work with.
     */
    public String getDisplayName() {
        return "H2";
    }

    /**
     * Returns boolean value indicating whether or not this dialect supports the
     * specified database product/version.
     * 
     * @param databaseProductName the name of the database as reported by 
     * 							  DatabaseMetaData.getDatabaseProductName()
     * @param databaseProductVersion the version of the database as reported by
     *                              DatabaseMetaData.getDatabaseProductVersion()
     * @return true if this dialect can be used for the specified product name
     *              and version; false otherwise.
     */
    public boolean supportsProduct(String databaseProductName, 
								   String databaseProductVersion) 
	{
    	if (databaseProductName == null) {
    		return false;
    	}
    	if (databaseProductName.trim().startsWith("H2")) {
    		// We don't yet have the need to discriminate by version.
    		return true;
    	}
		return false;
	}    
    
    /**
     * Returns the SQL statement to use to add a column to the specified table
     * using the information about the new column specified by info.
     * @param info information about the new column such as type, name, etc.
     * 
     * @return
     * @throws UnsupportedOperationException if the database doesn't support 
     *         adding columns after a table has already been created.
     */
    public String[] getColumnAddSQL(Column info) 
        throws UnsupportedOperationException 
    {
        ArrayList<String> result = new ArrayList<String>();
        result.add(DialectUtils.getColumnAddSQL(info, this, true, true, true));
        if (info.getRemarks() != null && !"".equals(info.getRemarks())) {
            result.add(getColumnCommentAlterSQL(info));
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns a boolean value indicating whether or not this dialect supports
     * adding comments to columns.
     * 
     * @return true if column comments are supported; false otherwise.
     */
    public boolean supportsColumnComment() {
        return true;
    }    
        
    /**
     * Returns a boolean value indicating whether or not this database dialect
     * supports dropping columns from tables.
     * 
     * @return true if the database supports dropping columns; false otherwise.
     */
    public boolean supportsDropColumn() {
        return true;
    }
    
    /**
     * Returns the SQL that forms the command to drop the specified colum in the
     * specified table.
     * 
     * @param tableName the name of the table that has the column
     * @param columnName the name of the column to drop.
     * @return
     * @throws UnsupportedOperationException if the database doesn't support 
     *         dropping columns. 
     */
    public String getColumnDropSQL(String tableName, String columnName) {
        return DialectUtils.getColumnDropSQL(tableName, columnName, "DROP COLUMN", false, null);
    }
    
    /**
     * Returns the SQL that forms the command to drop the specified table.  If
     * cascade contraints is supported by the dialect and cascadeConstraints is
     * true, then a drop statement with cascade constraints clause will be 
     * formed.
     * 
     * @param Table the table to drop
     * @param cascadeConstraints whether or not to drop any FKs that may 
     * reference the specified table.
     * @return the drop SQL command.
     */
    public List<String> getTableDropSQL(Table Table, boolean cascadeConstraints, boolean isMaterializedView){
        return DialectUtils.getTableDropSQL(Table, true, cascadeConstraints, false, DialectUtils.CASCADE_CLAUSE, false);
    }
    
    /**
     * Returns the SQL that forms the command to add a primary key to the 
     * specified table composed of the given column names.
     * 
     * alter table test alter column mychar char(10) not null 
     * 
     * alter table test add primary key (mychar)
     * 
     * alter table pktest add constraint pk_pktest primary key (pkcol)
     * 
     * @param pkName the name of the constraint
     * @param columns the columns that form the key
     * @return
     */
    public String[] getAddPrimaryKeySQL(String pkName, 
                                        Column[] columns, 
                                        Table ti) 
    {
        ArrayList<String> result = new ArrayList<String>();
        StringBuffer addPKSQL = new StringBuffer();
        addPKSQL.append("ALTER TABLE ");
        addPKSQL.append(ti.getQualifiedName());
        addPKSQL.append(" ADD CONSTRAINT ");
        addPKSQL.append(pkName);
        addPKSQL.append(" PRIMARY KEY (");
        for (int i = 0; i < columns.length; i++) {
            Column info = columns[i];
            if (info.isNullable()) {
                result.add(getColumnNullableAlterSQL(info, false));
            }
            addPKSQL.append(info.getName());
            if (i + 1 < columns.length) {
                addPKSQL.append(", ");
            }
        }
        addPKSQL.append(")");
        result.add(addPKSQL.toString());
        return result.toArray(new String[result.size()]);
    }
    
    /**
     * Returns the SQL statement to use to add a comment to the specified 
     * column of the specified table.
     * @param info information about the column such as type, name, etc.
     * @return
     * @throws UnsupportedOperationException if the database doesn't support 
     *         annotating columns with a comment.
     */
    public String getColumnCommentAlterSQL(Column info) 
        throws UnsupportedOperationException
    {       
        return DialectUtils.getColumnCommentAlterSQL(info);
    }
 
    /**
     * Returns the SQL used to alter the nullability of the specified column 
     * 
     * ALTER TABLE tableName ALTER COLUMN columnName 
     * dataType [DEFAULT expression] [NOT [NULL]]
     * 
     * @param info the column to modify
     * @return the SQL to execute
     */
    public String getColumnNullableAlterSQL(Column info) {
        String alterClause = DialectUtils.ALTER_COLUMN_CLAUSE;
        return DialectUtils.getColumnNullableAlterSQL(info, 
                                                      this, 
                                                      alterClause, 
                                                      true);
    }

    /**
     * Returns a boolean value indicating whether or not this database dialect
     * supports changing a column from null to not-null and vice versa.
     * 
     * @return true if the database supports dropping columns; false otherwise.
     */    
    public boolean supportsAlterColumnNull() {
        return true;
    }

    /**
     * Returns the SQL used to alter the nullability of the specified column 
     * 
     * @param info the column to modify
     * @return the SQL to execute
     */    
    private String getColumnNullableAlterSQL(Column info, 
                                             boolean isNullable) 
    {
        StringBuffer result = new StringBuffer();
        result.append("ALTER TABLE ");
        result.append(info.getParentEntity().getQualifiedName());
        result.append(" ALTER COLUMN ");
        result.append(info.getName());
        result.append(" ");
        result.append(DialectUtils.getTypeName(info, this));
        if (isNullable) {
            result.append(" NULL ");
        } else {
            result.append(" NOT NULL ");
        }
        return result.toString();        
        
    }
    
    /**
     * Returns a boolean value indicating whether or not this database dialect
     * supports renaming columns.
     * 
     * @return true if the database supports changing the name of columns;  
     *         false otherwise.
     */
    public boolean supportsRenameColumn() {
        return true;
    }    
    
    /**
     * Returns the SQL that is used to change the column name.
     * 
     * ALTER TABLE tableName ALTER COLUMN columnName 
     * RENAME TO name
     * 
     * @param from the Column as it is
     * @param to the Column as it wants to be
     * 
     * @return the SQL to make the change
     */
    public String getColumnNameAlterSQL(Column from, Column to) {
        String alterClause = DialectUtils.ALTER_COLUMN_CLAUSE;
        String renameToClause = DialectUtils.RENAME_TO_CLAUSE;
        return DialectUtils.getColumnNameAlterSQL(from, 
                                                  to, 
                                                  alterClause, 
                                                  renameToClause);
    }
    
    /**
     * Returns a boolean value indicating whether or not this dialect supports 
     * modifying a columns type.
     * 
     * @return true if supported; false otherwise
     */
    public boolean supportsAlterColumnType() {
        return true;
    }
    
    /**
     * Returns the SQL that is used to change the column type.
     * 
     * ALTER TABLE table_name ALTER COLUMN column_name data_type 
     * 
     * @param from the Column as it is
     * @param to the Column as it wants to be
     * 
     * @return the SQL to make the change
     * @throw UnsupportedOperationException if the database doesn't support 
     *         modifying column types. 
     */
    public List<String> getColumnTypeAlterSQL(Column from, 
                                        Column to)
        throws UnsupportedOperationException
    {
        String alterClause = DialectUtils.ALTER_COLUMN_CLAUSE;
        String setClause = "";
        return DialectUtils.getColumnTypeAlterSQL(this, 
                                                  alterClause, 
                                                  setClause, 
                                                  false, 
                                                  from, 
                                                  to);
    }
    
    /**
     * Returns a boolean value indicating whether or not this database dialect
     * supports changing a column's default value.
     * 
     * @return true if the database supports modifying column defaults; false 
     *         otherwise
     */
    public boolean supportsAlterColumnDefault() {
        return true;
    }
    
    /**
     * Returns the SQL command to change the specified column's default value
     *
     * ALTER TABLE table_name ALTER COLUMN column_name SET DEFAULT 'default value'
     *   
     * @param info the column to modify and it's default value.
     * @return SQL to make the change
     */
    public String getColumnDefaultAlterSQL(Column info) {
        String alterClause = DialectUtils.ALTER_COLUMN_CLAUSE;
        String defaultClause = DialectUtils.SET_DEFAULT_CLAUSE;
        return DialectUtils.getColumnDefaultAlterSQL(this, 
                                                     info, 
                                                     alterClause, false, defaultClause);
    }
    
    /**
     * Returns the SQL command to drop the specified table's primary key.
     * 
     * @param pkName the name of the primary key that should be dropped
     * @param tableName the name of the table whose primary key should be 
     *                  dropped
     * @return
     */
    public String getDropPrimaryKeySQL(String pkName, String tableName) {
        return DialectUtils.getDropPrimaryKeySQL(pkName, tableName, false, false);
    }
    
    /**
     * Returns the SQL command to drop the specified table's foreign key 
     * constraint.
     * 
     * @param fkName the name of the foreign key that should be dropped
     * @param tableName the name of the table whose foreign key should be 
     *                  dropped
     * @return
     */
    public String getDropForeignKeySQL(String fkName, String tableName) {
        return DialectUtils.getDropForeignKeySQL(fkName, tableName);
    }
    
    /**
     * Returns the SQL command to create the specified table.
     * 
     * @param tables the tables to get create statements for
     * @param md the metadata from the ISession
     * @param prefs preferences about how the resultant SQL commands should be 
     *              formed.
     * @param isJdbcOdbc whether or not the connection is via JDBC-ODBC bridge.
     *  
     * @return the SQL that is used to create the specified table
     * @throws UnifyException 
     */
    public List<String> getCreateTableSQL(List<Table> tables, 
                                          ISQLDatabaseMetaData md,
                                          CreateScriptPreferences prefs,
                                          boolean isJdbcOdbc)
        throws SQLException, UnifyException
    {
        return DialectUtils.getCreateTableSQL(tables, md, this, prefs, isJdbcOdbc);
    }
    
}

