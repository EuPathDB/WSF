package org.gusdb.gus.wdk.view.taglibs.query.HTMLParamViews;

import org.gusdb.gus.wdk.model.FlatVocabParam;
import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.WdkModelException;

import java.io.IOException;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Simple HTML form view on a SqlEnumParam
 */
public class FlatVocabParamView implements ParamViewI {
  
    public static void showParam(Param param, String formQuery, JspWriter out, JspContext context) throws IOException {
    	
        FlatVocabParam p = (FlatVocabParam) param;
    	ResultFactory rf = (ResultFactory) context.getAttribute("wdk.queryResultFactory", PageContext.APPLICATION_SCOPE);
    	String[] m = null;
    	try {
    		m = p.getVocab();
    	}
    	catch (WdkModelException exp) {
    	    throw new RuntimeException(exp);
    	}
    	if (m != null && m.length>0) {
    		out.println("<select name=\""+formQuery+"."+p.getName()+"\">");
    		
    		for (int i = 0; i < m.length ; i++) {
    			String entry = m[i];
    			out.print("<option value=\""+entry+"\">"+entry);
    		}
    		out.println("</select>");
    	} else {
         out.println("No values to select from");   
        }
    }

}
