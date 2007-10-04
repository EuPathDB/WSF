/**
 * 
 */
package org.gusdb.wdk.model;

/**
 * @author: xingao
 * @created: Jun 6, 2007
 * @updated: Jun 6, 2007
 */
public class ReporterProperty extends WdkModelBase {
    
    private String name;
    private String value;
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name
     *            the name to set
     */
    public void setName( String name ) {
        this.name = name;
    }

    
    /**
     * @return the value
     */
    public String getValue() {
        return this.value;
    }

    
    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
        // do nothing
    }
    
}