package com.cattsoft.coolsql.adapters.dialect;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.cattsoft.coolsql.pub.exception.UnifyException;
import com.cattsoft.coolsql.sql.ISQLDatabaseMetaData;
import com.cattsoft.coolsql.sql.model.Column;
import com.cattsoft.coolsql.sql.model.Table;

/**
 * An extension to the standard Hibernate Ingres dialect
 * 
 */
public class IngresDialect extends org.hibernate.dialect.IngresDialect 
                             implements HibernateDialect {
    
    public IngresDialect() {
        super();
        
        registerColumnType(Types.BIGINT, "bigint");
        // SQL Reference Guide says 32k, but I get:
        //
        // The specified  row size exceeded the maximum allowable row width., 
        // SQL State: 50002, Error Code: 2045
        //
        // when I go above 8000.
        registerColumnType(Types.BINARY, 8000,"byte($l)");
        registerColumnType(Types.BINARY, "long byte" );
        registerColumnType(Types.BIT, "tinyint" );
        registerColumnType(Types.BLOB, "long byte");
        registerColumnType(Types.BOOLEAN, "tinyint");
        registerColumnType(Types.CHAR, 2000, "char($l)");
        registerColumnType(Types.CHAR, "long varchar");
        registerColumnType(Types.CLOB, "long varchar");
        registerColumnType(Types.DATE, "date");
        registerColumnType(Types.DECIMAL, "decimal($p, $s)");
        registerColumnType(Types.DOUBLE, "double precision");
        registerColumnType(Types.FLOAT, "float($p)" );
        registerColumnType(Types.INTEGER, "integer");        
        registerColumnType(Types.LONGVARBINARY, "long byte" );
        registerColumnType(Types.LONGVARCHAR, "long varchar" );
        registerColumnType(Types.NUMERIC, "numeric($p, $s)" );
        registerColumnType(Types.REAL, "real" );
        registerColumnType(Types.SMALLINT, "smallint");
        registerColumnType(Types.TIME, "date");
        registerColumnType(Types.TIMESTAMP, "date");
        registerColumnType(Types.TINYINT, "tinyint");
        // I tried the following for values under 8000 but I get 
        // Encountered unexpected exception - line 1, You cannot assign  a 
        // value of type 'long byte' to a column of type 'byte varying'. 
        // Explicitly convert the value to the required type.
        // registerColumnType(Types.VARBINARY, 8000, "byte varying($l)");
        registerColumnType(Types.VARBINARY, "long byte" );        
        // I tried 8000 for the max length of VARCHAR and ingres gives an exception
        // (cannot assign a value of type long varchar to a varchar field).  So
        // I limit this field to 4000 for now - the Ingres product documentation
        // indicated that 32k was acceptable.  I've tested 4k and it seems to 
        // work fine.
        registerColumnType(Types.VARCHAR, 4000, "varchar($l)" );
        registerColumnType(Types.VARCHAR, "long varchar" );
    }    
    
    /* (non-Javadoc)
     * @see com.coolsql.adapters.dialect.HibernateDialect#supportsSchemasInTableDefinition()
     */
    public boolean supportsSchemasInTableDefinition() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.coolsql.adapters.dialect.HibernateDialect#getLengthFunction()
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
        // float(54) produces an exception:
        //
        // invalid column format 'float' on column 'float_column'., 
        // SQL State: 42000, Error Code: 2014        
        if (dataType == Types.FLOAT)
        {
            return 53;
        } else {
            return 31;
        }
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
        return "Ingres";
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
    	if (databaseProductName.trim().toLowerCase().startsWith("ingres")) {
    		// We don't yet have the need to discriminate by version.
    		return true;
    	}
		return false;
	}
    
    /**
     * Returns the SQL statement to use to add a column to the specified table
     * using the information about the new column specified by info.
     * 
     * ALTER TABLE tableName ADD COLUMN columnName type
     *
     * alter table tableName alter column columnName type default defVal
     *
     * alter table tableName alter column columnName type not null
     *
     * @param info information about the new column such as type, name, etc. 
     * @return
     * @throws UnsupportedOperationException if the database doesn't support 
     *         adding columns after a table has already been created.
     */
    public String[] getColumnAddSQL(Column info) throws UnsupportedOperationException {
        ArrayList<String> result = new ArrayList<String>();
        
        result.add(DialectUtils.getColumnAddSQL(info, this, false, false, false));
        
        // Ingres requires that columns that are to become not null must have a
        // default value.  
        if (!info.isNullable()) {
            result.add(getColumnDefaultAlterSQL(info));
            result.add(getColumnNullableAlterSQL(info));
        }
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
        String dropClause = DialectUtils.DROP_COLUMN_CLAUSE;
        // TODO: Need to allow user to specify this
        String constraintClause = "CASCADE";
        
        return DialectUtils.getColumnDropSQL(tableName, 
                                             columnName, 
                                             dropClause, 
                                             true,
                                             constraintClause);
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
        return DialectUtils.getTableDropSQL(Table, false, cascadeConstraints, false, DialectUtils.CASCADE_CLAUSE, false);
    }
    
    /**
     * Returns the SQL that forms the command to add a primary key to the 
     * specified table composed of the given column names.
     * 
     * 
     * alter table test alter column pkcol integer not null
     * 
     * alter table test add constraint pk_test primary key (pkcol)
     * 
     * @param pkName the name of the constraint
     * @param columnNames the columns that form the key
     * @return
     */
    public String[] getAddPrimaryKeySQL(String pkName, 
                                        Column[] columns, Table ti) 
    {
        ArrayList<String> result = new ArrayList<String>();
        String alterClause = DialectUtils.ALTER_COLUMN_CLAUSE;
        for (int i = 0; i < columns.length; i++) {
            Column info = columns[i];
            String notNullSQL = 
                DialectUtils.getColumnNullableAlterSQL(info, 
                                                       false, 
                                                       this, 
                                                       alterClause, 
                                                       true);
            result.add(notNullSQL);
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
     * alter table test alter column notnullint integer not null
     * 
     * @param info the column to modify
     * @return the SQL to execute
     */
    public String getColumnNullableAlterSQL(Column info) {
        String alterClause = DialectUtils.ALTER_COLUMN_CLAUSE;
        return DialectUtils.getColumnNullableAlterSQL(info, this, alterClause, true);
    }

    /**
     * Returns a boolean value indicating whether or not this database dialect
     * supports renaming columns.
     * 
     * @return true if the database supports changing the name of columns;  
     *         false otherwise.
     */
    public boolean supportsRenameColumn() {
        return false;
    }
    
    /**
     * Returns the SQL that is used to change the column name.
     * 
     * 
     * @param from the Column as it is
     * @param to the Column as it wants to be
     * 
     * @return the SQL to make the change
     */
    public String getColumnNameAlterSQL(Column from, Column to) {
        int featureId = DialectUtils.COLUMN_NAME_ALTER_TYPE;
        String msg = DialectUtils.getUnsupportedMessage(this, featureId);
        throw new UnsupportedOperationException(msg);        
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
     * alter table tableName alter column columnName columnTypeSpec
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
     *   alter table test2 alter column mycol char(10) default 'foo'
     *   
     * @param info the column to modify and it's default value.
     * @return SQL to make the change
     */
    public String getColumnDefaultAlterSQL(Column info) {
        String alterClause = DialectUtils.ALTER_COLUMN_CLAUSE;
        String defaultClause = DialectUtils.DEFAULT_CLAUSE;
        return DialectUtils.getColumnDefaultAlterSQL(this, 
                                                     info, 
                                                     alterClause, 
                                                     true, 
                                                     defaultClause);
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
        return DialectUtils.getDropPrimaryKeySQL(pkName, tableName, true, true);
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
