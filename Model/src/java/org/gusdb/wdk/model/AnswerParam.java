package org.gusdb.wdk.model;

import org.gusdb.wdk.model.Answer;


public class AnswerParam extends Param {
    
    public AnswerParam () {}

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////


    /////////////////////////////////////////////////////////////////
    // protected methods
    /////////////////////////////////////////////////////////////////

    protected void resolveReferences(WdkModel model) throws WdkModelException {}

    public String validateValue(Object value) throws WdkModelException {
        if (!(value instanceof Answer)) {
            throw new WdkModelException("Value must be a Answer " + value) ;
        }
	return null;
    }
}