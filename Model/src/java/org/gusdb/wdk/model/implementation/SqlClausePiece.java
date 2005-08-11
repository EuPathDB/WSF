package org.gusdb.wdk.model.implementation;

import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.ResultFactory;

import java.util.regex.Pattern;

public class SqlClausePiece {

    String origSql;
    String joinTableName;
    //locations relative to original sql
    int start;
    int end;

    /**
     * A piece of sql that belongs to an sql clause and that has no kid
     * clauses.  If the sql clause has no kids, then the piece is its
     * full length (exluding bounding parens)
     *
     * @param start index of the start of this piece (non-paren)
     * @param end index of end of this piece (non-paren), ie, index of last char
     */
    public SqlClausePiece(String origSql, int start, int end, String joinTableName) {
	this.origSql = origSql;
	this.start = start;
	this.end = end;
	this.joinTableName = joinTableName;
    }

    String getFinalPieceSql(boolean needsSelectFix, 
			    boolean needsFromFix,
			    boolean needsWhereFix,
			    int pageStartIndex,
			    int pageEndIndex) throws WdkModelException {
	String finalSql = origSql.substring(start, end+1);
	if (needsSelectFix) finalSql = addJoinTableIndexToSelect(finalSql);
	if (needsFromFix) finalSql = addJoinTableToFrom(finalSql);
	if (needsWhereFix) finalSql = addConstraintsToWhere(finalSql,
							    pageStartIndex,
							    pageEndIndex);
	return finalSql;
    }

    String addJoinTableIndexToSelect(String sql) {
	return sql.replaceAll("select|SELECT", 
			      "SELECT " + joinTableName + "." +
			      ResultFactory.RESULT_TABLE_I + "," );
    }

    String addJoinTableToFrom(String sql) {
	return sql.replaceAll("from|FROM", "FROM " + joinTableName + ",");
    }

    String addConstraintsToWhere(String sql, int pageStartIndex, 
				 int pageEndIndex) throws WdkModelException {

	String macro = RecordClass.PRIMARY_KEY_MACRO; // shorter var. name

	// add AND clauses for page constraints
	String newline = System.getProperty("line.separator");
	String resultTableIndex = 
	    joinTableName + "." + ResultFactory.RESULT_TABLE_I;

	String andClause = 
	    newline + "AND " + resultTableIndex + " >= " + pageStartIndex +
	    newline + "AND " + resultTableIndex + " <= " + pageEndIndex;
	
	String newSql = sql;
	// case 1:  "blah = $$primaryKey$$"
	if (newSql.matches(".*=\\s*" + macro + ".*")) {
	    newSql = newSql.replaceAll("(" + macro + ")", 
				       "$1" + andClause );	    
	    
	// case 2:  "$$primaryKey$$ = blah"
	} else if (newSql.matches(".*" + macro + "\\s*=.*")) {
	    newSql = newSql.replaceAll("(" + macro + "\\s*=\\s*\\S+)", 
				       "$1" + andClause );	    
	    
	} else {
	    throw new WdkModelException("Invalid use of primary key macro in:"
					+ newline + sql);
	}

	// add order by at the end
	newSql = newSql + "\nORDER BY " + resultTableIndex;

	return newSql;
    }

    boolean containsSelect() {	
	String regex = ".*select\\s+.*";
	return origSql.substring(start, end+1).toLowerCase().matches(regex);
    }

    boolean containsFrom() {
	String regex = ".*\\s+from\\s+.*";
	return origSql.substring(start, end+1).toLowerCase().matches(regex);
    }

    boolean containsPrimaryKey() {
	String regex = ".*" + RecordClass.PRIMARY_KEY_MACRO + ".*";
	return origSql.substring(start, end+1).matches(regex);
    }

    ///////////////////////////////////////////////////////////////////
    /////  private methods
    ///////////////////////////////////////////////////////////////////

    private boolean contains(String regex) {
	return origSql.substring(start, end+1).matches(regex);
    }

}

