package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.BooleanQuestionNode;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

import java.util.Hashtable;

public class BooleanQuestionLeafBean {
    
    BooleanQuestionNode bqn;
    BooleanQuestionNodeBean parent;

    int leafId;
    
    public BooleanQuestionLeafBean(BooleanQuestionNode bqn, BooleanQuestionNodeBean parent){
	this.bqn = bqn;
	this.parent = parent;
    }

    private boolean isFirstChild() {
	if(getParent() == null) { return true; }

	return this == parent.getFirstChild();
    } 

    /**
     * 
     */
    public void grow(BooleanQuestionLeafBean leaf, String operation, WdkModelBean modelBean)throws WdkModelException{

	BooleanQuestionNode newBqn = bqn.grow(leaf.getBooleanQuestionNode(), operation, modelBean.getModel());
	BooleanQuestionNodeBean tempParent = parent;
	boolean wasFirstChild = isFirstChild();

	BooleanQuestionNodeBean newNodeBean = new BooleanQuestionNodeBean(newBqn, this, leaf, tempParent);
	
	//setting parent explicitly here; other option is to do inside node bean constructor with discovery
	setParent(newNodeBean);
	leaf.setParent(newNodeBean);

	if (tempParent != null){
	    if (wasFirstChild) {
		tempParent.setFirstChild(newNodeBean);
	    } else {
		tempParent.setSecondChild(newNodeBean);
	    }
	}
    }
    
    public QuestionBean getQuestion(){
	return new QuestionBean(bqn.getQuestion());
    }
    
    private BooleanQuestionNode getBooleanQuestionNode(){
	return bqn;
    }

    public Integer getLeafId(){
	return new Integer(leafId);
    }

    public void setLeafId(int id){
	this.leafId = id;
    }

    public BooleanQuestionNodeBean getParent(){
	return parent;
    }

    public void setValues(Hashtable values){
	bqn.setValues(values);
    }

    protected void setParent(BooleanQuestionNodeBean parent){
	this.parent = parent;
    }

    public String toString() {
	return getQuestion().getFullName() + " (id: " + getLeafId() + ")";
    }
}