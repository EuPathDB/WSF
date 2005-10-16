/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.gusdb.wdk.model.WdkModelException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlDataLoader {

    private URL schemaURL;

    /**
     * 
     */
    public XmlDataLoader(URL schemaURL) {
        this.schemaURL = schemaURL;
    }

    public XmlAnswer parseDataFile(URL dataXmlURL) throws WdkModelException {
        // validate the xml data file
        if (!validDataFile(dataXmlURL, schemaURL))
            throw new WdkModelException("Validation to data xml file failed: "
                    + dataXmlURL.toExternalForm());

        // configure the digester
        Digester digester = configureDigester();

        try {
            // load and parse the data source
            InputStream dataXmlStream = dataXmlURL.openStream();
            XmlAnswer answer = (XmlAnswer) digester.parse(dataXmlStream);

            return answer;
        } catch (IOException ex) {
            throw new WdkModelException(ex);
        } catch (SAXException ex) {
            throw new WdkModelException(ex);
        }
    }

    private boolean validDataFile(URL dataXmlURL, URL schemaURL)
            throws WdkModelException {
        System.setProperty(
                "org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XIncludeParserConfiguration");

        try {

            ErrorHandler errorHandler = new ErrorHandlerImpl(System.err);
            PropertyMap schemaProperties = new SinglePropertyMap(
                    ValidateProperty.ERROR_HANDLER, errorHandler);
            ValidationDriver vd = new ValidationDriver(schemaProperties,
                    PropertyMap.EMPTY, null);

            vd.loadSchema(ValidationDriver.uriOrFileInputSource(schemaURL.toExternalForm()));

            // validate the data xml file
            InputSource is = ValidationDriver.uriOrFileInputSource(dataXmlURL.toExternalForm());
            return vd.validate(is);
        } catch (SAXException e) {
            throw new WdkModelException(e);
        } catch (IOException e) {
            throw new WdkModelException(e);
        }
    }

    private Digester configureDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);

        // set the root
        digester.addObjectCreate("xmlAnswer", XmlAnswer.class);
        digester.addSetProperties("xmlAnswer");

        // xmlRecord
        digester.addObjectCreate("xmlAnswer/xmlRecord", XmlRecordInstance.class);
        digester.addSetProperties("xmlAnswer/xmlRecord");

        // xmlAttribute
        digester.addObjectCreate("xmlAnswer/xmlRecord/xmlAttribute",
                XmlAttributeValue.class);
        digester.addSetProperties("xmlAnswer/xmlRecord/xmlAttribute");
        digester.addSetNext("xmlAnswer/xmlRecord/xmlAttribute", "addAttribute");

        // xmlTable
        digester.addObjectCreate("xmlAnswer/xmlRecord/xmlTable",
                XmlTableValue.class);
        digester.addSetProperties("xmlAnswer/xmlRecord/xmlTable");

        // xmlRow
        digester.addObjectCreate("xmlAnswer/xmlRecord/xmlTable/xmlRow",
                XmlRowValue.class);
        digester.addSetProperties("xmlAnswer/xmlRecord/xmlTable/xmlRow");

        // xmlAttribute - columns
        digester.addObjectCreate(
                "xmlAnswer/xmlRecord/xmlTable/xmlRow/xmlAttribute",
                XmlAttributeValue.class);
        digester.addSetProperties("xmlAnswer/xmlRecord/xmlTable/xmlRow/xmlAttribute");
        digester.addSetNext("xmlAnswer/xmlRecord/xmlTable/xmlRow/xmlAttribute",
                "addColumn");

        digester.addSetNext("xmlAnswer/xmlRecord/xmlTable/xmlRow", "addRow");

        digester.addSetNext("xmlAnswer/xmlRecord/xmlTable", "addTable");

        digester.addSetNext("xmlAnswer/xmlRecord", "addRecordInstance");

        return digester;
    }

}
