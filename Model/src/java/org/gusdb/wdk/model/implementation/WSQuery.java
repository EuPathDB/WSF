package org.gusdb.wdk.model.implementation;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.ResultList;

public class WSQuery extends Query {
    
    String processName;
    String webServiceUrl;

    public WSQuery () {
	super();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public void setProcessName(String name) {
	processName = name;
    }

    public QueryInstance makeInstance() {
	return new WSQueryInstance(this);
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Protected ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    protected void setResources(WdkModel model) throws WdkModelException {
	super.setResources(model);
	this.webServiceUrl = model.getWebServiceUrl();
    }

    String getProcessName() {
	return processName;
    }

    String getWebServiceUrl() {
	return webServiceUrl;
    }

    protected StringBuffer formatHeader() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = super.formatHeader();
       buf.append("  processName='" + processName + "'" + newline);
       return buf;
    }
 }