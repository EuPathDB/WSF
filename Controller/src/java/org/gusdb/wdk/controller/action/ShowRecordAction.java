package org.gusdb.wdk.controller.action;

import java.util.Map;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.gusdb.wdk.controller.CConstants;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordInstance;


/**
 * This Action is called by the ActionServlet when a WDK record is requested.
 * It 1) reads param values from request,
 *    2) makes record
 *    3) forwards control to a jsp page that displays a record
 */

public class ShowRecordAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	WdkModel wdkModel = (WdkModel)getServlet().getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
	RecordClass wdkRecordClass = wdkModel.getRecordClass(request.getParameter("name"));
	RecordInstance wdkRecord = wdkRecordClass.makeRecordInstance();
	wdkRecord.setPrimaryKey(request.getParameter("id"));

	request.getSession().setAttribute(CConstants.WDK_RECORD_KEY, wdkRecord);

	ActionForward forward = mapping.findForward(CConstants.SHOW_RECORD_MAPKEY);
	return forward;
    }
}