package com.cattsoft.coolsql.adapters.dialect;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.dialect.SAPDBDialect;

import com.cattsoft.coolsql.pub.exception.UnifyException;
import com.cattsoft.coolsql.sql.ISQLDatabaseMetaData;
import com.cattsoft.coolsql.sql.model.Column;
import com.cattsoft.coolsql.sql.model.Table;

public class MAXDBDialect extends SAPDBDialect 
                          implements HibernateDialect {
    
    public MAXDBDialect() {
        super();
        registerColumnType( Types.BIGINT, "fixed(19,0)" );
        registerColumnType( Types.BINARY, 8000, "char($l) byte" );
        registerColumnType( Types.BINARY, "long varchar byte" );
        registerColumnType( Types.BIT, "boolean" );
        registerColumnType( Types.BLOB, "long byte" );
        registerColumnType( Types.BOOLEAN, "boolean" );
        registerColumnType( Types.CLOB, "long varchar" );
        registerColumnType( Types.CHAR, 8000, "char($l) ascii" );
        registerColumnType( Types.CHAR, "long varchar ascii" );
        registerColumnType( Types.DECIMAL, "decimal($p,$s)" );
        registerColumnType( Types.DOUBLE, "double precision" );
        registerColumnType( Types.DATE, "date" );
        registerColumnType( Types.FLOAT, "float($p)" );
        registerColumnType( Types.INTEGER, "int" );
        registerColumnType( Types.LONGVARBINARY, 8000, "varchar($l) byte");
        registerColumnType( Types.LONGVARBINARY, "long byte");
        registerColumnType( Types.LONGVARCHAR, "long ascii");
        registerColumnType( Types.NUMERIC, "fixed($p,$s)" );
        registerColumnType( Types.REAL, "float($p)");
        registerColumnType( Types.SMALLINT, "smallint" );
        registerColumnType( Types.TIME, "time" );
        registerColumnType( Types.TIMESTAMP, "timestamp" );
        registerColumnType( Types.TINYINT, "fixed(3,0)" );
        registerColumnType( Types.VARBINARY, "long byte" );
        registerColumnType( Types.VARCHAR, 8000, "varchar($l)");
        registerColumnType( Types.VARCHAR, "long ascii");
    }
    

    public boolean supportsSchemasInTableDefinition() {
        return true;
    }

    public String getMaxFunction() {
        return "max";
    }

    public String getLengthFunction(int dataType) {
        return "length";
    }

    public int getMaxPrecision(int dataType) {
        return 38;
    }

    public int getMaxScale(int dataType) {
        return getMaxPrecision(dataType);
    }

    public int getPrecisionDigits(int columnSize, int dataType) {
        return columnSize * 2;
    }

    /* (non-Javadoc)
     * @see com.coolsql.adapters.dialect.HibernateDialect#getColumnLength(int, int)
     */
    public int getColumnLength(int columnSize, int dataType) {
        // driver returns 8 for "long byte", yet it can store 2GB of data. 
        if (dataType == Types.LONGVARBINARY) {
            return Integer.MAX_VALUE;
        }
        return columnSize;
    }
    
    /**
     * The string which identifies this dialect in the dialect chooser.
     * 
     * @return a descriptive name that tells the user what database this dialect
     *         is design to work with.
     */
    public String getDisplayName() {
        return "MaxDB";
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
    	String lname = databaseProductName.trim().toLowerCase();
    	if (lname.startsWith("sap") || lname.startsWith("maxdb")) {
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
        result.add(DialectUtils.getColumnAddSQL(info, this, true, false, true));
        if (info.getRemarks() != null && !"".equals(info.getRemarks())) {
            result.add(getColumnCommentAlterSQL(info));
        }
        return result.toArray(new String[result.size()]);
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
        return DialectUtils.getColumnDropSQL(tableName, columnName);
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
     * ALTER TABLE test ADD constraint test_pk PRIMARY KEY (notnullint)
     * 
     * @param pkName the name of the constraint
     * @param columnNames the columns that form the key
     * @return
     */
    public String[] getAddPrimaryKeySQL(String pkName, 
                                        Column[] columns, Table ti) 
    {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < columns.length; i++) {
            Column info = columns[i];
            result.add(getColumnNullableAlterSQL(info, false));
        }
        result.add(DialectUtils.getAddPrimaryKeySQL(ti, pkName, columns, false));
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
     * Returns a boolean value indicating whether or not this database dialect
     * supports changing a column from null to not-null and vice versa.
     * 
     * @return true if the database supports dropping columns; false otherwise.
     */    
    public boolean supportsAlterColumnNull() {
        return true;
    }
    
    /**
     * Returns the SQL used to alter the specified column to not allow null 
     * values
     * 
     * ALTER TABLE table_name COLUMN column_name DEFAULT NULL
     * 
     * ALTER TABLE table_name COLUMN column_name NOT NULL
     * 
     * @param info the column to modify
     * @return the SQL to execute
     */
    public String getColumnNullableAlterSQL(Column info) {
        boolean nullable = info.isNullable();
        return getColumnNullableAlterSQL(info, nullable);
    }
    
    /**
     * Returns the SQL used to alter the specified column to not allow null 
     * values
     * 
     * ALTER TABLE table_name COLUMN column_name DEFAULT NULL
     * 
     * ALTER TABLE table_name COLUMN column_name NOT NULL
     * 
     * @param info the column to modify
     * @param nullable whether or not the column should allow nulls
     * 
     * @return the SQL to execute
     */
    public String getColumnNullableAlterSQL(Column info, 
                                            boolean nullable) {
        StringBuffer result = new StringBuffer();
        result.append("ALTER TABLE ");
        result.append(info.getParentEntity().getQualifiedName());
        result.append(" COLUMN ");
        result.append(info.getName());
        if (nullable) {
            result.append(" DEFAULT NULL");
        } else {
            result.append(" NOT NULL");
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
     * RENAME COLUMN table_name.column_name TO new_column_name
     * 
     * @param from the Column as it is
     * @param to the Column as it wants to be
     * 
     * @return the SQL to make the change
     */
    public String getColumnNameAlterSQL(Column from, Column to) {
        return DialectUtils.getColumnRenameSQL(from, to);
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
        String alterClause = DialectUtils.MODIFY_CLAUSE;
        return DialectUtils.getColumnTypeAlterSQL(this, 
                                                  alterClause, 
                                                  "", 
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
     * alter table test column mychar drop default
     * 
     * alter table test column mychar add default 'a default'
     *   
     * @param info the column to modify and it's default value.
     * @return SQL to make the change
     */
    public String getColumnDefaultAlterSQL(Column info) {
        String alterClause = DialectUtils.COLUMN_CLAUSE;
        String newDefault = info.getDefaultValue();
        String defaultClause = null;
        if (newDefault != null && !"".equals(newDefault)) {
            defaultClause = DialectUtils.ADD_DEFAULT_CLAUSE;
        } else {
            defaultClause = DialectUtils.DROP_DEFAULT_CLAUSE;
        }
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
