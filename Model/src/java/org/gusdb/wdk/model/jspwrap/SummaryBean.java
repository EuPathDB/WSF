package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.WdkModelException;

import java.util.Map;
import java.util.Iterator;

/**
 * A wrapper on a {@link Answer} that provides simplified access for 
 * consumption by a view
 */ 
public class SummaryBean {

    Answer summary;
    

    public SummaryBean(Answer summary) {
	this.summary = summary;
    }

    /**
     * @return A Map of paramName --> {@link ParamBean}.
     */
    public Map getParams() {
	return summary.getDisplayParams();
    }

    public int getPageSize() {
	return summary.getPageSize();
    }

    public int getResultSize() {
	try {
	    return summary.getResultSize();
	} catch (WdkModelException e) {
	    throw new RuntimeException(e);
	}
    }

    public RecordClassBean getRecordClass() {
	return new RecordClassBean(summary.getQuestion().getRecordClass());
    }

    /**
     * @return A list of {@link RecordBean}s.
     */
    public Iterator getRecords() {
	return new RecordBeanList();
    }

    ////////////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////////////

    class RecordBeanList implements Iterator {

	public int getSize() {
	    return summary.getPageSize();
	}
    
	public boolean hasNext() {
	    return summary.hasMoreRecordInstances();
	}
	
	public Object next() {
	    try {
		return new RecordBean(summary.getNextRecordInstance());
	    }
	    catch (WdkModelException exp) {
		throw new RuntimeException(exp);
	    }
	}
    
	public void remove() {
	    throw new UnsupportedOperationException("remove isn't allowed on this iterator");
	} 
	
    }
    

}