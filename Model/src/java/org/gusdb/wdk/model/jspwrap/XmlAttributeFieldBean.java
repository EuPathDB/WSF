/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.xml.XmlAttributeField;

/**
 * @author Jerric
 * @created Oct 19, 2005
 */
public class XmlAttributeFieldBean {

    private XmlAttributeField field;

    /**
     * 
     */
    public XmlAttributeFieldBean(XmlAttributeField field) {
        this.field = field;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeField#getDisplayName()
     */
    public String getDisplayName() {
        return this.field.getDisplayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeField#getHelp()
     */
    public String getHelp() {
        return this.field.getHelp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeField#getIsInternal()
     */
    public Boolean getIsInternal() {
        return this.field.getIsInternal();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeField#getName()
     */
    public String getName() {
        return this.field.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeField#getTruncate()
     */
    public Integer getTruncate() {
        return this.field.getTruncate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeField#getType()
     */
    public String getType() {
        return this.field.getType();
    }
}