/**
 * 
 */
package org.gusdb.wsf.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.gusdb.wsf.WsfPortalService;
import org.gusdb.wsf.WsfServiceException;

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WsfPortalServiceTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * Test method for 'org.gusdb.wdk.service.ProcessService.invoke(String[],
     * String[], String[])'
     */
    public void testInvoke() {
        // get plugin name; required
        String pluginName = System.getProperty("plugin.name");
        assertNotNull(pluginName);

        // get columns for the result; required
        String columnTemp = System.getProperty("columns");
        assertNotNull(columnTemp);
        String[] columns = columnTemp.split(",");
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].trim();
        }

        // get parameters for the plugin; optional
        String paramTemp = System.getProperty("parameters");
        String[] params, values;
        if (paramTemp != null) {
            String[] parts = paramTemp.split(",");
            Map<String, String> paramMap = new HashMap<String, String>();
            for (String part : parts) {
                String[] subpart = part.trim().split("=");
                String param = subpart[0].trim();
                if (param.length() > 0) {
                    String value = "";
                    if (subpart.length > 1) value = subpart[1].trim();
                    paramMap.put(param, value);
                }
            }
            params = new String[paramMap.size()];
            values = new String[paramMap.size()];
            paramMap.keySet().toArray(params);
            paramMap.values().toArray(values);
        } else {
            params = new String[0];
            values = new String[0];
        }

        // create column map for printing purpose
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < columns.length; i++) {
            map.put(columns[i], i);
        }

        WsfPortalService service = new WsfPortalService();
        try {
            String[][] result = service.invoke(pluginName, params, values,
                    columns);

            // print out the result
            System.out.println("");
            for (int i = 0; i < result.length; i++) {
                System.out.println("================ " + result[i][0]
                        + " ================");
                for (String col : columns) {
                    System.out.println("------------ " + col + " ------------");
                    System.out.println(result[i][map.get(col)]);
                }
                System.out.println();
            }
        } catch (WsfServiceException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

}