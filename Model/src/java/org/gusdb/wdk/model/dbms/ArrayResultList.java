/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric Gao
 * 
 */
public class ArrayResultList<T> implements ResultList {

    private Map<String, Integer> columns;
    private T[][] result;
    private int rowIndex;

    /**
     * @param columns
     * @throws WdkModelException
     */
    public ArrayResultList(Column[] columns, T[][] result)
            throws WdkModelException {
        this.result = result;
        this.rowIndex = 0;
        this.columns = new LinkedHashMap<String, Integer>();

        int columnIndex = 0;
        for (Column column : columns) {
            this.columns.put(column.getName(), columnIndex++);
        }

        // verify the columns and result
        if (result.length > 0 && result[0].length < columns.length)
            throw new WdkModelException(
                    "The result has fewer columns than the column definition");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.ResultList#close()
     */
    public void close() {
        // move the current index out of the boundary
        rowIndex = result.length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.ResultList#contains(java.lang.String)
     */
    public boolean contains(String columnName) {
        return columns.containsKey(columnName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.ResultList#get(java.lang.String)
     */
    public Object get(String columnName) throws WdkModelException {
        if (!contains(columnName))
            throw new WdkModelException(
                    "The column does not exist in ResultList");
        if (rowIndex >= result.length)
            throw new WdkModelException("There is no next row available");

        int columnIndex = columns.get(columnName);
        return result[rowIndex][columnIndex];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.ResultList#next()
     */
    public boolean next() {
        rowIndex++;
        return (rowIndex < result.length);
    }
}
