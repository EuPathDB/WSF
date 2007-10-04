/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import junit.framework.TestCase;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.xml.sax.SAXException;

/**
 * @author xingao
 * 
 */
public class UserFactoryTest extends TestCase {

    private static String[] addKeys = { "model", "email", "password",
            "firstName", "lastName", "middleName", "title", "organization",
            "department", "address", "city", "state", "country", "phoneNumber",
            "zipCode" };
    private static boolean[] addRequired = { true, true, true, true, true,
            false, false, true, false, false, false, false, false, false, false };

    private static Map<String, Boolean> addParams = new LinkedHashMap<String, Boolean>();

    static {
        for (int i = 0; i < addKeys.length; i++) {
            addParams.put(addKeys[i].toLowerCase(), addRequired[i]);
        }
    }

    private static String[] listKeys = { "model", "email" };
    private static boolean[] listRequired = { true, false };

    private static Map<String, Boolean> listParams = new LinkedHashMap<String, Boolean>();

    static {
        for (int i = 0; i < listKeys.length; i++) {
            listParams.put(listKeys[i].toLowerCase(), listRequired[i]);
        }
    }

    private static String[] deleteKeys = { "model", "email" };
    private static boolean[] deleteRequired = { true, true };

    private static Map<String, Boolean> deleteParams = new LinkedHashMap<String, Boolean>();

    static {
        for (int i = 0; i < deleteKeys.length; i++) {
            deleteParams.put(deleteKeys[i].toLowerCase(), deleteRequired[i]);
        }
    }

    private static String[] checkKeys = { "model" };
    private static boolean[] checkRequired = { true };

    private static Map<String, Boolean> checkParams = new LinkedHashMap<String, Boolean>();

    static {
        for (int i = 0; i < checkKeys.length; i++) {
            checkParams.put(checkKeys[i].toLowerCase(), checkRequired[i]);
        }
    }

    private static UserFactory factory;

    private String email = "jerric@uga.edu";
    private String password = "jerric";

    public static void main(String[] args)
            throws WdkModelException, WdkUserException, SAXException,
            IOException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException {
        if (args.length < 1) {
            printUsage("Command is missing.");
            System.exit(-1);
        }

        // determine the command
        String cmd = args[0].trim();
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);

        Map<String, String> params = null;
        if (cmd.equalsIgnoreCase("add")) {
            params = prepareParameters(subArgs, addParams);
        } else if (cmd.equalsIgnoreCase("list")) {
            params = prepareParameters(subArgs, listParams);
        } else if (cmd.equalsIgnoreCase("delete")) {
            params = prepareParameters(subArgs, deleteParams);
        } else if (cmd.equalsIgnoreCase("check")) {
            params = prepareParameters(subArgs, checkParams);
        } else {
            printUsage("Unknown command for userTester: " + cmd);
            System.exit(-1);
        }

        // set model for TestUtility use
        String modelName = params.get("model");
        System.setProperty("model", modelName);
        // put model into the system property
        TestUtility utility = TestUtility.getInstance();
        WdkModel model = utility.getWdkModel();
        factory = model.getUserFactory();

        // execute the method
        if (cmd.equalsIgnoreCase("add")) addUser(params);
        else if (cmd.equalsIgnoreCase("list")) listUsers(params);
        else if (cmd.equalsIgnoreCase("delete")) deleteUser(params);
        else if (cmd.equalsIgnoreCase("check")) checkConsistancy(params);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        // load WdkModel
        TestUtility utility = TestUtility.getInstance();
        WdkModel model = utility.getWdkModel();
        factory = model.getUserFactory();
    }

    /*
     * Test method for 'org.gusdb.wdk.model.user.UserFactory.sendEmail(String,
     * String, String, String)'
     */
    public void testSendEmail() {
        try {
            factory.sendEmail("jerric@pcbi.upenn.edu", email,
                    "UserFactory Unit Test",
                    "Here is a test email generated by the UserFactoryTest automatically.");
        } catch (WdkUserException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.user.UserFactory.createUser(String,
     * String, String, String, String, String, String, String, String, String,
     * String, String, String)'
     */
    public void testCreateUser() {
        try {
            User user = factory.createUser(email, "Gao", "Jerric", "", "Mr.",
                    "UGA", "Biology", "C210 Life Science", "Athens", "GA",
                    "36302", "706-542-1447", "USA",
                    new LinkedHashMap<String, String>(),
                    new LinkedHashMap<String, String>());
            factory.savePassword(email, password);
            assertNotNull(user);
            assertFalse(user.isGuest());
        } catch (WdkUserException ex) {
            ex.printStackTrace();
            assertTrue(false);
        } catch (WdkModelException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.user.UserFactory.createGuestUser()'
     */
    public void testCreateGuestUser() {
        try {
            User user = factory.createGuestUser();
            assertNotNull(user);
            assertTrue(user.isGuest());
        } catch (WdkUserException ex) {
            ex.printStackTrace();
            assertTrue(false);
        } catch (WdkModelException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /*
     * Test method for
     * 'org.gusdb.wdk.model.user.UserFactory.authenticate(String, String)'
     */
    public void testAuthenticate() {
        try {
            User guest = factory.createGuestUser();
            User user = factory.login(guest, email, "jerric");
            assertNotNull(user);
            assertFalse(user.isGuest());
        } catch (WdkUserException ex) {
            ex.printStackTrace();
            assertTrue(false);
        } catch (WdkModelException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.user.UserFactory.saveUser(User)'
     */
    public void testSaveUser() {
        try {
            User guest = factory.createGuestUser();
            User user = factory.login(guest, email, "jerric");
            assertNotNull(user);
            assertFalse(user.isGuest());

            // update user information, and save it
            user.setAddress("524 Guardian dr. 1428 Blockley Hall");
            user.setCity("Philadelphia");
            user.setDepartment("Center for Bioinformatics");
            user.setOrganization("University of Pennsylvania");
            user.addUserRole("administrator");
            user.save();
        } catch (WdkUserException ex) {
            ex.printStackTrace();
            assertTrue(false);
        } catch (WdkModelException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.user.UserFactory.deleteUser(User)'
     */
    public void testDeleteUser() {
        try {
            factory.deleteUser(email);
        } catch (WdkUserException ex) {
            ex.printStackTrace();
            assertTrue(false);
        } catch (WdkModelException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    // =========================================================================
    // The following methods are for the use of the administration command
    // =========================================================================

    private static Map<String, String> prepareParameters(String[] args,
            Map<String, Boolean> knownParams) {

        // get the parameters
        Map<String, String> params = new LinkedHashMap<String, String>();

        // there must be even number of args, the key value pairs
        if (args.length % 2 != 0) {
            printUsage("Unmatched key & values of the arguments");
            System.exit(-1);
        }

        for (int i = 0; i < args.length; i += 2) {
            // the key part should start with a '-'
            String key = args[i].trim().toLowerCase();
            String value = args[i + 1].trim();

            // check if the key starts with '-'
            if (key.charAt(0) != '-') {
                printUsage("Invalid key format: " + key);
                System.exit(-1);
            }
            // check if the key is known
            key = key.substring(1).trim();
            if (!knownParams.containsKey(key)) {
                printUsage("Undefined argument: " + key);
                System.exit(-1);
            }
            params.put(key, value);
        }
        // check if all required params are present
        for (String key : knownParams.keySet()) {
            if (knownParams.get(key) && !params.containsKey(key)) {
                printUsage("The required argument is missing: " + key);
                System.exit(-1);
            }
        }
        return params;
    }

    private static void printUsage(String message) {
        System.err.println("Error occurred: " + message);
        System.err.println();
        System.err.print("Usage:\twdkUser add");
        for (String key : addParams.keySet()) {
            boolean required = addParams.get(key);
            if (!required) System.err.print(" [-" + key + " <" + key + ">]");
            else System.err.print(" -" + key + " <" + key + ">");
        }
        System.err.println();
        System.err.println();

        System.err.print("\twdkUser list");
        for (String key : listParams.keySet()) {
            boolean required = listParams.get(key);
            if (!required) System.err.print(" [-" + key + " <" + key + ">]");
            else System.err.print(" -" + key + " <" + key + ">");
        }
        System.err.println();
        System.err.println();

        System.err.print("\twdkUser delete");
        for (String key : deleteParams.keySet()) {
            boolean required = deleteParams.get(key);
            if (!required) System.err.print(" [-" + key + " <" + key + ">]");
            else System.err.print(" -" + key + " <" + key + ">");
        }
        System.err.println();
        System.err.println();

        System.err.print("\twdkUser check");
        for (String key : listParams.keySet()) {
            boolean required = listParams.get(key);
            if (!required) System.err.print(" [-" + key + " <" + key + ">]");
            else System.err.print(" -" + key + " <" + key + ">");
        }
        System.err.println();
    }

    public static void addUser(Map<String, String> params)
            throws WdkUserException {
        String email = params.get("email");
        String password = params.get("password");
        String lastName = params.get("lastname");
        String firstName = params.get("firstname");
        String middleName = params.get("middlename");
        String title = params.get("title");
        String organization = params.get("organization");
        String department = params.get("department");
        String address = params.get("email");
        String city = params.get("email");
        String state = params.get("email");
        String country = params.get("email");
        String zipCode = params.get("email");
        String phoneNumber = params.get("email");

        // create the user without resetting the password
        try {
            factory.createUser(email, lastName, firstName, middleName, title,
                    organization, department, address, city, state, zipCode,
                    phoneNumber, country, new LinkedHashMap<String, String>(),
                    new LinkedHashMap<String, String>());
            // now save the password
            factory.savePassword(email, password);

            System.out.println("User " + email
                    + " has been added into the database successfully.");
        } catch (WdkUserException ex) {
            ex.printStackTrace();
            assertTrue(false);
        } catch (WdkModelException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    public static void listUsers(Map<String, String> params)
            throws WdkUserException, WdkModelException {
        String emailPattern = params.get("email");
        User[] users = factory.queryUsers(emailPattern);
        System.out.println("Email,\tFirst Name,\tLast Name,\tOrganization");
        for (User user : users) {
            System.out.print(user.getEmail() + ",\t");
            System.out.print(user.getFirstName() + ",\t");
            System.out.print(user.getLastName() + ",\t");
            System.out.println(user.getOrganization());
        }
    }

    public static void deleteUser(Map<String, String> params)
            throws WdkUserException, WdkModelException {
        String email = params.get("email");
        factory.deleteUser(email);

        System.out.println("User " + email
                + " has be deleted from the database.");
    }

    public static void checkConsistancy(Map<String, String> params)
            throws WdkUserException, WdkModelException {
        factory.checkConsistancy();
        System.out.println("Consistancy check finished.");
    }
}