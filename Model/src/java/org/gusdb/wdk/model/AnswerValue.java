package org.gusdb.wdk.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.BooleanQueryInstance;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.user.Answer;
import org.gusdb.wdk.model.user.AnswerFactory;
import org.json.JSONException;

/**
 * Answer.java
 * 
 * Created: Fri June 4 13:01:30 2004 EDT
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2007-01-22 12:33:24 -0500 (Mon, 22 Jan
 *          2007) $ $Author$
 */

/**
 * A list of RecordInstances representing one page of the answer to a Question.
 * The constructor of the Answer provides a handle (QueryInstance) on the
 * ResultList that is the list of primary keys for the all the records (not *
 * just one page) that are the answer to the Question. The ResultList also has a
 * column that contains the row number (RESULT_TABLE_I) so that a list of
 * primary keys for a single page can be efficiently accessed.
 * 
 * The Answer is lazy in that it only constructs the set of RecordInstances for
 * the page when the first RecordInstance is requested.
 * 
 * The initial request triggers the creation of skeletal RecordInstances for the
 * page. They contain only primary keys (these being acquired from the
 * ResultList).
 * 
 * These skeletal RecordInstances are also lazy in that they only run an
 * attributes query when an attribute provided by that query is requested. When
 * they do run an attribute query, its QueryInstance is put into joinMode. This
 * means that the attribute query joins with the table containing the primary
 * keys, and, in one database query, generates rows containing the attribute
 * values for all the RecordInstances in the page.
 * 
 * The method <code>integrateAttributesQueryResult</> is invoked by the
 * first RecordInstance in the page upon the first request for an attribute 
 * provided by an attributes query. The query is a join with the list of 
 * primary keys, and so has a row for each RecordInstance in the page, and
 * columns that provide the attribute values (plus RESULT_TABLE_I).  The 
 * values in the rows are integrated into the corresponding RecordInstance 
 * (now no longer skeletal).  <code>integrateAttributesQueryResult</> may
 * be called a number of times, depending upon how many attribute queries
 * the record class contains.
 * 
 * Attribute queries are guaranteed to provide one row for each RecordInstance
 * in the page.  An exception is thrown otherwise.
 * 
 */
public class AnswerValue {

    private static final Logger logger = Logger.getLogger(AnswerValue.class);

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    private Answer answer;

    private ResultFactory resultFactory;
    private Question question;

    private QueryInstance idsQueryInstance;

    private int startIndex;
    private int endIndex;

    private String pagedIdSql;

    private Map<PrimaryKeyAttributeValue, RecordInstance> pageRecordInstances;

    private Integer resultSize; // size of total result
    private Map<String, Integer> resultSizesByFilter = new LinkedHashMap<String, Integer>();
    private Map<String, Integer> resultSizesByProject;

    private Map<String, Boolean> sortingMap;
    private Map<String, AttributeField> summaryFieldMap;

    private AnswerFilterInstance filter;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    /**
     * @param question
     *            The <code>Question</code> to which this is the
     *            <code>Answer</code>.
     * @param idsQueryInstance
     *            The <co de>QueryInstance</code> that provides a handle on the
     *            ResultList containing all primary keys that are the result for
     *            the question (not just one page worth).
     * @param startRecordInstanceI
     *            The index of the first <code>RecordInstance</code> in the
     *            page. (>=1)
     * @param endRecordInstanceI
     *            The index of the last <code>RecordInstance</code> in the page,
     *            inclusive.
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     */
    AnswerValue(Question question, QueryInstance idsQueryInstance,
            int startIndex, int endIndex, Map<String, Boolean> sortingMap,
            AnswerFilterInstance filter) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        this.question = question;
        this.resultFactory = question.getWdkModel().getResultFactory();
        this.idsQueryInstance = idsQueryInstance;
        this.startIndex = startIndex;
        this.endIndex = endIndex;

        // get sorting columns
        if (sortingMap == null)
            sortingMap = new LinkedHashMap<String, Boolean>();
        this.sortingMap = sortingMap;
        this.summaryFieldMap = new LinkedHashMap<String, AttributeField>();

        // get the view
        this.filter = filter;

        logger.debug("Answer created.");
    }

    /**
     * A copy constructor, and
     * 
     * @param answerValue
     * @param startIndex
     * @param endIndex
     */
    public AnswerValue(AnswerValue answerValue, int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;

        this.answer = answerValue.answer;
        this.idsQueryInstance = answerValue.idsQueryInstance;
        this.question = answerValue.question;
        this.resultFactory = answerValue.resultFactory;
        this.resultSize = answerValue.resultSize;
        this.resultSizesByFilter = new LinkedHashMap<String, Integer>(
                answerValue.resultSizesByFilter);
        if (answerValue.resultSizesByProject != null)
            this.resultSizesByProject = new LinkedHashMap<String, Integer>(
                    answerValue.resultSizesByProject);

        this.sortingMap = new LinkedHashMap<String, Boolean>(
                answerValue.sortingMap);
        this.summaryFieldMap = new LinkedHashMap<String, AttributeField>(
                answerValue.summaryFieldMap);
        this.filter = answerValue.filter;
    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    /**
     * provide property that user's term for question
     */
    public Question getQuestion() {
        return this.question;
    }

    public int getPageSize() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initPageRecordInstances();
        return pageRecordInstances.size();
    }

    public int getPageCount() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        int total = getResultSize();
        int pageSize = endIndex - startIndex + 1;
        int pageCount = (int) Math.round(Math.ceil((float) total / pageSize));
        logger.debug("#Pages: " + pageCount + ",\t#Total: " + total
                + ",\t#PerPage: " + pageSize);
        return pageCount;
    }

    public int getResultSize() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        if (resultSize == null) {
            if (getFilter() == null) {
                resultSize = idsQueryInstance.getResultSize();
            } else {
                resultSize = getFilterSize(getFilter().getName());
            }
        }
        return resultSize;
    }

    public Map<String, Integer> getResultSizesByProject()
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        if (resultSizesByProject == null) {
            resultSizesByProject = new LinkedHashMap<String, Integer>();
            // need to run the query first
            ResultList resultList;
            String message = null;
            if (filter == null) {
                resultList = idsQueryInstance.getResults();
                message = idsQueryInstance.getResultMessage();
            } else resultList = filter.getResults(this);

            try {
                boolean hasMessage = (message != null && message.length() > 0);
                if (hasMessage) {
                    String[] sizes = message.split(",");
                    for (String size : sizes) {
                        String[] parts = size.split(":");
                        resultSizesByProject.put(parts[0],
                                Integer.parseInt(parts[1]));
                    }
                } else {
                    while (resultList.next()) {
                        if (!hasMessage) {
                            // also count by project
                            String project = resultList.get(
                                    Utilities.COLUMN_PROJECT_ID).toString();
                            int subCounter = 0;
                            if (resultSizesByProject.containsKey(project))
                                subCounter = resultSizesByProject.get(project);
                            resultSizesByProject.put(project, ++subCounter);
                        }
                    }
                }
            } finally {
                resultList.close();
            }
        }
        return resultSizesByProject;

    }

    public boolean isDynamic() {
        return getQuestion().isDynamic();
    }

    /**
     * @return Map where key is param display name and value is param value
     */
    public Map<String, String> getParamDisplays() {
        Map<String, String> displayParamsMap = new LinkedHashMap<String, String>();
        Map<String, String> paramsMap = idsQueryInstance.getValues();
        Param[] params = question.getParams();
        for (int i = 0; i < params.length; i++) {
            Param param = params[i];
            displayParamsMap.put(param.getPrompt(),
                    paramsMap.get(param.getName()));
        }
        return displayParamsMap;
    }

    public QueryInstance getIdsQueryInstance() {
        return idsQueryInstance;
    }

    public RecordInstance[] getRecordInstances() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initPageRecordInstances();

        RecordInstance[] array = new RecordInstance[pageRecordInstances.size()];
        pageRecordInstances.values().toArray(array);
        return array;
    }

    public RecordInstance getRecordInstance(PrimaryKeyAttributeValue primaryKey) {
        return pageRecordInstances.get(primaryKey);
    }

    public String getChecksum() throws WdkModelException,
            NoSuchAlgorithmException, JSONException, WdkUserException,
            SQLException {
        return idsQueryInstance.getChecksum();
    }

    // ///////////////////////////////////////////////////////////////////
    // print methods
    // ///////////////////////////////////////////////////////////////////

    public String printAsRecords() throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();

        initPageRecordInstances();

        for (RecordInstance recordInstance : pageRecordInstances.values()) {
            buf.append(recordInstance.print());
            buf.append("---------------------" + newline);
        }
        return buf.toString();
    }

    /**
     * print summary attributes, one per line Note: not sure why this is needed
     * 
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public String printAsSummary() throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        StringBuffer buf = new StringBuffer();

        initPageRecordInstances();

        for (RecordInstance recordInstance : pageRecordInstances.values()) {
            buf.append(recordInstance.printSummary());
        }
        return buf.toString();
    }

    /**
     * print summary attributes in tab delimited table with header of attr.
     * names
     * 
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public String printAsTable() throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();

        initPageRecordInstances();

        // print summary info
        buf.append("# of Records: " + getResultSize() + ",\t# of Pages: "
                + getPageCount() + ",\t# Records per Page: " + getPageSize()
                + newline);

        if (pageRecordInstances.size() == 0) return buf.toString();

        Map<String, AttributeField> attributes = getSummaryAttributeFields();
        for (String nextAttName : attributes.keySet()) {
            buf.append(nextAttName + "\t");
        }
        buf.append(newline);
        for (RecordInstance recordInstance : pageRecordInstances.values()) {
            // only print
            for (String nextAttName : attributes.keySet()) {
                // make data row
                AttributeValue value = recordInstance.getAttributeValue(nextAttName);
                // only print part of the string
                String str = value.getBriefValue();
                buf.append(str + "\t");
            }
            buf.append(newline);
        }
        return buf.toString();
    }

    public Reporter createReport(String reporterName, Map<String, String> config)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        // get the full answer
        int endI = getResultSize();
        return createReport(reporterName, config, 1, endI);
    }

    public Reporter createReport(String reporterName,
            Map<String, String> config, int startI, int endI)
            throws WdkModelException {
        // get Reporter
        Map<String, ReporterRef> rptMap = question.getRecordClass().getReporterMap();
        ReporterRef rptRef = rptMap.get(reporterName);
        if (rptRef == null)
            throw new WdkModelException("The reporter " + reporterName + " is "
                    + "not registered for "
                    + question.getRecordClass().getFullName());
        String rptImp = rptRef.getImplementation();
        if (rptImp == null)
            throw new WdkModelException("The reporter " + reporterName + " is "
                    + "not registered for "
                    + question.getRecordClass().getFullName());

        try {
            Class<?> rptClass = Class.forName(rptImp);
            Class<?>[] paramClasses = { AnswerValue.class, int.class, int.class };
            Constructor<?> constructor = rptClass.getConstructor(paramClasses);

            Object[] params = { this, startI, endI };
            Reporter reporter = (Reporter) constructor.newInstance(params);
            reporter.setProperties(rptRef.getProperties());
            reporter.configure(config);
            reporter.setWdkModel(rptRef.getWdkModel());
            return reporter;
        } catch (ClassNotFoundException ex) {
            throw new WdkModelException(ex);
        } catch (InstantiationException ex) {
            throw new WdkModelException(ex);
        } catch (IllegalAccessException ex) {
            throw new WdkModelException(ex);
        } catch (SecurityException ex) {
            throw new WdkModelException(ex);
        } catch (NoSuchMethodException ex) {
            throw new WdkModelException(ex);
        } catch (IllegalArgumentException ex) {
            throw new WdkModelException(ex);
        } catch (InvocationTargetException ex) {
            throw new WdkModelException(ex);
        }
    }

    // ------------------------------------------------------------------
    // Package Methods
    // ------------------------------------------------------------------

    /**
     * Integrate into the page's RecordInstances the attribute values from a
     * particular attributes query. The attributes query result includes only
     * rows for this page.
     * 
     * The query is obtained from Column, and the query should not be modified.
     * 
     * @throws SQLException
     * @throws JSONException
     * @throws NoSuchAlgorithmException
     * @throws WdkModelException
     * @throws WdkUserException
     */
    void integrateAttributesQuery(Query attributeQuery) throws SQLException,
            NoSuchAlgorithmException, JSONException, WdkModelException,
            WdkUserException {
        initPageRecordInstances();

        WdkModel wdkModel = question.getWdkModel();
        // has to get a clean copy of the attribute query, without pk params
        // appended
        attributeQuery = (Query) wdkModel.resolveReference(attributeQuery.getFullName());

        // get and run the paged attribute query sql
        String sql = getPagedAttributeSql(attributeQuery);
        DBPlatform platform = wdkModel.getQueryPlatform();
        DataSource dataSource = platform.getDataSource();
        ResultSet resultSet = SqlUtils.executeQuery(dataSource, sql);
        ResultList resultList = new SqlResultList(resultSet);

        // fill in the column attributes
        PrimaryKeyAttributeField pkField = question.getRecordClass().getPrimaryKeyAttributeField();
        Map<String, AttributeField> fields = question.getAttributeFieldMap();
        while (resultList.next()) {
            // get primary key
            Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
            for (String column : pkField.getColumnRefs()) {
                pkValues.put(column, resultList.get(column));
            }
            PrimaryKeyAttributeValue primaryKey = new PrimaryKeyAttributeValue(
                    pkField, pkValues);
            RecordInstance record = pageRecordInstances.get(primaryKey);

            if (record == null) {
                StringBuffer error = new StringBuffer();
                error.append("Paged attribute query [");
                error.append(attributeQuery.getFullName());
                error.append("] returns rows that doesn't match the paged ");
                error.append("records. (");
                for (String pkName : pkValues.keySet()) {
                    error.append(pkName).append(" = ");
                    error.append(pkValues.get(pkName)).append(", ");
                }
                error.append(").\nPaged Attribute SQL:\n").append(sql);
                error.append("\n").append("Paged ID SQL:\n").append(
                        getPagedIdSql());
                throw new WdkModelException(error.toString());
            }

            // fill in the column attributes
            for (String columnName : attributeQuery.getColumnMap().keySet()) {
                AttributeField field = fields.get(columnName);
                if (field != null && (field instanceof ColumnAttributeField)) {
                    // valid attribute field, fill it in
                    Object objValue = resultList.get(columnName);
                    ColumnAttributeValue value = new ColumnAttributeValue(
                            (ColumnAttributeField) field, objValue);
                    record.addColumnAttributeValue(value);
                }
            }
        }
        logger.debug("Attribute query [" + attributeQuery.getFullName()
                + "] integrated.");
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------

    private String getPagedAttributeSql(Query attributeQuery)
            throws SQLException, NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException {
        // get the paged SQL of id query
        String idSql = getPagedIdSql();

        PrimaryKeyAttributeField pkField = question.getRecordClass().getPrimaryKeyAttributeField();

        // combine the id query with attribute query
        String attributeSql = getAttributeSql(attributeQuery);
        StringBuffer sql = new StringBuffer("SELECT aq.* FROM (");
        sql.append(idSql);
        sql.append(") pidq, (").append(attributeSql).append(") aq WHERE ");

        boolean firstColumn = true;
        for (String column : pkField.getColumnRefs()) {
            if (firstColumn) firstColumn = false;
            else sql.append(" AND ");
            sql.append("aq.").append(column).append(" = pidq.").append(column);
        }
        return sql.toString();
    }

    private String getAttributeSql(Query attributeQuery)
            throws NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException, WdkUserException {
        String queryName = attributeQuery.getFullName();
        Query dynaQuery = question.getDynamicAttributeQuery();
        if (dynaQuery != null && queryName.equals(dynaQuery.getFullName())) {
            // the dynamic query doesn't have sql defined, the sql will be
            // constructed from the id query cache table.
            return idsQueryInstance.getCachedSql();
        } else {
            // make an instance from the attribute query, and attribute query
            // has no params
            Map<String, String> params = new LinkedHashMap<String, String>();
            QueryInstance queryInstance = attributeQuery.makeInstance(params);
            return queryInstance.getSql();
        }
    }

    private String getPagedIdSql() throws NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException, WdkUserException {
        if (pagedIdSql != null) return pagedIdSql;

        // get id sql
        String idSql = getIdSql();

        // get sorting attribute queries
        Map<String, String> attributeSqls = new LinkedHashMap<String, String>();
        List<String> orderClauses = new ArrayList<String>();
        prepareSortingSqls(attributeSqls, orderClauses);

        StringBuffer sql = new StringBuffer("SELECT idq.* FROM (");
        sql.append(idSql).append(") idq");
        // add all tables involved
        for (String shortName : attributeSqls.keySet()) {
            sql.append(", (").append(attributeSqls.get(shortName)).append(") ");
            sql.append(shortName);
        }

        // add primary key join conditions
        String[] pkColumns = question.getRecordClass().getPrimaryKeyAttributeField().getColumnRefs();
        boolean firstClause = true;
        for (String shortName : attributeSqls.keySet()) {
            for (String column : pkColumns) {
                if (firstClause) {
                    sql.append(" WHERE ");
                    firstClause = false;
                } else sql.append(" AND ");

                sql.append("idq.").append(column);
                sql.append(" = ");
                sql.append(shortName).append(".").append(column);
            }
        }

        // add order clause
        if (orderClauses.size() > 0) {
            sql.append(" ORDER BY ");
            firstClause = true;
            for (String clause : orderClauses) {
                if (firstClause) firstClause = false;
                else sql.append(", ");
                sql.append(clause);
            }
        }

        DBPlatform platform = question.getWdkModel().getQueryPlatform();
        pagedIdSql = platform.getPagedSql(sql.toString(), startIndex, endIndex);

        logger.debug("paged id sql constructed.");

        return pagedIdSql;
    }

    public String getIdSql() throws NoSuchAlgorithmException, SQLException,
            WdkModelException, JSONException, WdkUserException {
        String[] pkColumns = question.getRecordClass().getPrimaryKeyAttributeField().getColumnRefs();

        StringBuffer sql = new StringBuffer("SELECT DISTINCT ");
        boolean firstColumn = true;
        for (String column : pkColumns) {
            if (firstColumn) firstColumn = false;
            else sql.append(", ");
            sql.append(column);
        }
        sql.append(" FROM (");

        String innerSql = idsQueryInstance.getSql();
        // get a filter
        if (filter != null) innerSql = filter.applyFilter(innerSql);
        sql.append(innerSql).append(") bidq");

        logger.debug("id sql constructed.");

        return sql.toString();
    }

    private void prepareSortingSqls(Map<String, String> sqls,
            Collection<String> orders) throws WdkModelException, JSONException,
            NoSuchAlgorithmException, SQLException, WdkUserException {
        Map<String, AttributeField> fields = question.getAttributeFieldMap();
        Map<String, String> querySqls = new LinkedHashMap<String, String>();
        Map<String, String> queryNames = new LinkedHashMap<String, String>();
        Map<String, String> orderClauses = new LinkedHashMap<String, String>();
        WdkModel wdkModel = question.getWdkModel();
        for (String fieldName : sortingMap.keySet()) {
            AttributeField field = fields.get(fieldName);
            boolean ascend = sortingMap.get(fieldName);
            for (AttributeField dependent : field.getDependents()) {
                if (!(dependent instanceof ColumnAttributeField)) continue;

                Column column = ((ColumnAttributeField) dependent).getColumn();
                Query query = column.getQuery();
                String queryName = query.getFullName();
                // cannot use the attribute query from record, need to get it
                // back from wdkModel, since the query has pk params appended
                query = (Query) wdkModel.resolveReference(queryName);

                // handle query
                if (!queryNames.containsKey(queryName)) {
                    // query not processed yet, process it
                    String shortName = "aq" + queryNames.size();
                    String sql = getAttributeSql(query);
                    queryNames.put(queryName, shortName);
                    querySqls.put(queryName, sql);
                }

                // handle column
                String sortingColumn = column.getSortingColumn();
                if (sortingColumn == null) sortingColumn = column.getName();
                boolean ignoreCase = column.isIgnoreCase();
                if (!orderClauses.containsKey(sortingColumn)) {
                    // dependent not processed, process it
                    StringBuffer clause = new StringBuffer();
                    if (ignoreCase) clause.append("lower(");
                    clause.append(queryNames.get(queryName));
                    clause.append(".");
                    clause.append(sortingColumn);
                    if (ignoreCase) clause.append(")");
                    clause.append(ascend ? " ASC" : " DESC");
                    orderClauses.put(sortingColumn, clause.toString());
                }
            }
        }

        // fill the map of short name and sqls
        for (String queryName : queryNames.keySet()) {
            String shortName = queryNames.get(queryName);
            String sql = querySqls.get(queryName);
            sqls.put(shortName, sql);
        }
        orders.addAll(orderClauses.values());
    }

    /**
     * If not already initialized, initialize the page's record instances,
     * setting each with its id (either just primary key or that and project, if
     * using a federated data source).
     * 
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     * @throws WdkModelException
     * @throws WdkUserException
     */
    private void initPageRecordInstances() throws NoSuchAlgorithmException,
            SQLException, JSONException, WdkModelException, WdkUserException {
        if (pageRecordInstances != null) return;

        logger.debug("Initializing paged records......");
        this.pageRecordInstances = new LinkedHashMap<PrimaryKeyAttributeValue, RecordInstance>();

        String sql = getPagedIdSql();
        DBPlatform platform = question.getWdkModel().getQueryPlatform();
        DataSource dataSource = platform.getDataSource();
        ResultSet resultSet = SqlUtils.executeQuery(dataSource, sql);
        ResultList resultList = new SqlResultList(resultSet);
        RecordClass recordClass = question.getRecordClass();
        PrimaryKeyAttributeField pkField = recordClass.getPrimaryKeyAttributeField();
        while (resultList.next()) {
            // get primary key. the primary key is supposed to be translated to
            // the current ones from the id query, and no more translation
            // needed.
            // 
            // If this assumption is false, then we need to join the alias query
            // into the paged id query as well.
            Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
            for (String column : pkField.getColumnRefs()) {
                pkValues.put(column, resultList.get(column));
            }
            RecordInstance record = new RecordInstance(this, pkValues);
            pageRecordInstances.put(record.getPrimaryKey(), record);
        }

        // check if the number of records is expected
        int expected = Math.min(endIndex, getResultSize()) - startIndex + 1;

        if (expected != pageRecordInstances.size()) {
            StringBuffer buffer = new StringBuffer();
            for (String name : summaryFieldMap.keySet()) {
                if (buffer.length() == 0) buffer.append(", ");
                buffer.append(name);
            }

            throw new WdkModelException("The expacted result size is different"
                    + " from the actual size. Please check the id query "
                    + idsQueryInstance.getQuery().getFullName() + " and the "
                    + "attribute queries that return the attributes (" + buffer
                    + ")");
        }

        logger.debug("Paged records initialized.");
    }

    /**
     * @return Returns the endRecordInstanceI.
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * @return Returns the startRecordInstanceI.
     */
    public int getStartIndex() {
        return startIndex;
    }

    public String getResultMessage() {
        return idsQueryInstance.getResultMessage();
    }

    public Map<String, Boolean> getSortingMap() {
        return new LinkedHashMap<String, Boolean>(sortingMap);
    }

    public List<AttributeField> getDisplayableAttributes() {
        List<AttributeField> displayAttributes = new ArrayList<AttributeField>();
        Map<String, AttributeField> attributes = question.getAttributeFieldMap(FieldScope.NON_INTERNAL);
        Map<String, AttributeField> summaryAttributes = question.getSummaryAttributeFieldMap();
        for (String attriName : attributes.keySet()) {
            AttributeField attribute = attributes.get(attriName);

            // skip the attributes that are already displayed
            if (summaryAttributes.containsKey(attriName)) continue;

            displayAttributes.add(attribute);
        }
        return displayAttributes;
    }

    public Map<String, AttributeField> getSummaryAttributeFields() {
        Map<String, AttributeField> fields;
        if (summaryFieldMap.size() > 0) {
            fields = new LinkedHashMap<String, AttributeField>(summaryFieldMap);
        } else fields = question.getSummaryAttributeFieldMap();
        return fields;
    }

    public void setSumaryAttributes(String[] attributeNames) {
        Map<String, AttributeField> summaryFields = new LinkedHashMap<String, AttributeField>();
        // always put the primary key as the first attribute
        PrimaryKeyAttributeField pkField = question.getRecordClass().getPrimaryKeyAttributeField();
        summaryFields.put(pkField.getName(), pkField);
        Map<String, AttributeField> fields = question.getAttributeFieldMap();
        for (String attributeName : attributeNames) {
            AttributeField field = fields.get(attributeName);
            summaryFields.put(attributeName, field);
        }
        summaryFieldMap.clear();
        summaryFieldMap.putAll(summaryFields);
    }

    /**
     * @return returns a list of all primary key values.
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws SQLException
     * @throws JSONException
     * @throws WdkUserException
     */
    public Object[][] getPrimaryKeyValues() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        String[] columns = question.getRecordClass().getPrimaryKeyAttributeField().getColumnRefs();
        List<Object[]> buffer = new ArrayList<Object[]>();

        ResultList resultList;
        if (filter == null) resultList = idsQueryInstance.getResults();
        else resultList = filter.getResults(this);

        while (resultList.next()) {
            Object[] pkValues = new String[columns.length];
            for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
                pkValues[columnIndex] = resultList.get(columns[columnIndex]);
            }
            buffer.add(pkValues);
        }
        Object[][] ids = new String[buffer.size()][columns.length];
        buffer.toArray(ids);
        return ids;
    }

    public Answer getAnswer() throws NoSuchAlgorithmException, SQLException,
            WdkModelException, JSONException, WdkUserException {
        if (answer == null) {
            AnswerFactory answerFactory = question.getWdkModel().getAnswerFactory();
            answer = answerFactory.getAnswer(getChecksum());
            if (answer == null) answer = answerFactory.saveAnswerValue(this);
        }
        return answer;
    }

    public int getFilterSize(String filterName)
            throws NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException, WdkUserException {
        Integer size = resultSizesByFilter.get(filterName);
        if (size == null) {
            RecordClass recordClass = question.getRecordClass();
            AnswerFilterInstance filter = recordClass.getFilter(filterName);

            String innerSql = idsQueryInstance.getSql();
            innerSql = filter.applyFilter(innerSql);

            StringBuffer sql = new StringBuffer("SELECT count(*) FROM ");
            sql.append("(").append(innerSql).append(") f");

            WdkModel wdkModel = question.getWdkModel();
            DataSource dataSource = wdkModel.getQueryPlatform().getDataSource();
            Object result = SqlUtils.executeScalar(dataSource, sql.toString());
            size = Integer.parseInt(result.toString());

            resultSizesByFilter.put(filterName, size);
        }
        return size;
    }

    /**
     * @return the filter
     */
    public AnswerFilterInstance getFilter() {
        return filter;
    }

    public void setFilter(String filterName) throws WdkModelException {
        if (filterName != null) {
            RecordClass recordClass = question.getRecordClass();
            setFilter(recordClass.getFilter(filterName));
        } else this.filter = null;
    }

    /**
     * @param filter
     *            the filter to set
     */
    public void setFilter(AnswerFilterInstance filter) {
        this.filter = filter;
    }

    public void setAnswerInfo(Answer answer) {
        this.answer = answer;
    }

    public PrimaryKeyAttributeValue[] getAllPkValues()
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        ResultList resultList = idsQueryInstance.getResults();
        PrimaryKeyAttributeField pkField = question.getRecordClass().getPrimaryKeyAttributeField();
        String[] pkColumns = pkField.getColumnRefs();
        List<PrimaryKeyAttributeValue> pkValues = new ArrayList<PrimaryKeyAttributeValue>();
        while (resultList.next()) {
            Map<String, Object> values = new LinkedHashMap<String, Object>();
            for (String column : pkColumns) {
                values.put(column, resultList.get(column));
            }
            PrimaryKeyAttributeValue pkValue = new PrimaryKeyAttributeValue(
                    pkField, values);
            pkValues.add(pkValue);
        }
        resultList.close();

        PrimaryKeyAttributeValue[] array = new PrimaryKeyAttributeValue[pkValues.size()];
        pkValues.toArray(array);
        return array;
    }

    /**
     * @return the combined
     */
    public boolean isCombined() {
        // check if the query contains any of the step param
        for (Param param : idsQueryInstance.getQuery().getParams()) {
            if (param instanceof AnswerParam) return true;
        }
        return false;
    }

    public boolean isBoolean() {
        return (idsQueryInstance instanceof BooleanQueryInstance);
    }

    public boolean isTransform() {
        return (!isBoolean() && isCombined());
    }
}