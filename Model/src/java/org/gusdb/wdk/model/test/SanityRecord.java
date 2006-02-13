package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.WdkModelException;

/**
 * SanityRecord.java
 *
 * Object used in running a sanity test; represents a record in a wdk model.  
 *
 * Created: Mon August 23 12:00:00 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$Author: sfischer $
 */


public class SanityRecord implements SanityElementI {

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------
    
    /**
     * Name of the wdk record (in recordSetName.recordName format) represented by this
     * SanityRecord.
     */
    protected String twoPartName;
    
    /**
     * Modified by Jerric
     * Primary key of the element that this record represents.
     */
    protected String projectID;
    protected String primaryKey;
    //protected PrimaryKeyValue primaryKey;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public SanityRecord(){

    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------
    public void setRef(String twoPartName){
	this.twoPartName = twoPartName;
    }
    
    public String getRef(){
	return this.twoPartName;
    }

    public void setPrimaryKey(String primaryKey){
	this.primaryKey = primaryKey;
    }
    
    public String getPrimaryKey(){
	return this.primaryKey;
    }
    
    // Added by Jerric
    public void setProjectID(String projectID) {
    	this.projectID = projectID;
    }
    
    public String getProjectID() {
    	return this.projectID;
    }

    public String toString(){
	return "SanityRecord twoPartName = " + twoPartName + " pk = " + primaryKey;
    }

    // ------------------------------------------------------------------
    // SanityElementI
    // ------------------------------------------------------------------
    
    public String getName(){
	return twoPartName;
    }

    public String getType(){
	return "record";
    }

    public String getCommand(String globalArgs) throws WdkModelException{

        String projectID = getProjectID();
        String pk = getPrimaryKey();

	StringBuffer command = new StringBuffer ("wdkRecord " + globalArgs);

	command.append(" -record " + getRef()  + " -primaryKey " + pk);
	
	if (projectID != null) command.append(" -project " + projectID);
 
	return command.toString();
    }

}