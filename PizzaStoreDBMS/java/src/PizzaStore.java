import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("\nMAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;

               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch

            if (authorisedUser != null) {
              
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should ony be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(authorisedUser, esql); break;
                   case 2: updateProfile(authorisedUser, esql); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql); break;
                   case 5: viewAllOrders(authorisedUser, esql); break;
                   case 6: viewRecentOrders(authorisedUser, esql); break;
                   case 7: viewOrderInfo(authorisedUser, esql); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(authorisedUser, esql); break;
                   case 10: updateMenu(authorisedUser, esql); break;
                   case 11: updateUser(authorisedUser, esql); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(PizzaStore esql){
      try {
         System.out.println("\n- - - - - - - - - - - - - - - - -\n");
         System.out.println("CREATE NEW USER");
         System.out.println("---------------");
         System.out.print("Welcome new user! Please enter a username: ");
         String usernameInput = in.readLine();

         if (usernameInput.equals("exit")) {
            return;
         }

         String checkUserAvail = "SELECT DISTINCT(U.login) FROM Users U WHERE U.login = '" + usernameInput + "'";
         int userAvail = esql.executeQuery(checkUserAvail);

         while (userAvail != 0) {
            System.out.print("That username has already been taken. Please enter another username: ");

            usernameInput = in.readLine();
            checkUserAvail = "SELECT DISTINCT(U.login) FROM Users U WHERE U.login = '" + usernameInput + "'";
            userAvail = esql.executeQuery(checkUserAvail);
         }

         System.out.print("\nPlease enter your password: ");
         String pass1Input = in.readLine();

         if (pass1Input.equals("exit")) {
            return;
         }

         System.out.print("Please reenter your password for verification: ");
         String pass2Input = in.readLine();

         while (true) {
            if (pass1Input.equals(pass2Input)) {
               break;
            }
            if (pass1Input.equals("exit")) {
               return;
            }

            System.out.print("The password did not match. Please try again.\n\n");
            System.out.print("Please enter your password: ");
            pass1Input = in.readLine();

            System.out.print("Please re-enter your password for verification: ");
            pass2Input = in.readLine();
         }

         System.out.print("\nLastly, please enter in your phone: ");
         String phoneInput = in.readLine();

         if (phoneInput.equals("exit")) {
            return;
         }

         String addNewUser = "INSERT INTO Users VALUES ('" + usernameInput + "', '" + pass1Input + "', 'customer', NULL, '" + phoneInput + "')";
         esql.executeUpdate(addNewUser);

         System.out.println("Account has been created!");
         System.out.println("\n- - - - - - - - - - - - - - - - -\n");

         return;

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(PizzaStore esql){
      try {
         System.out.println("\n- - - - - - - - - - - - - - - - -\n");
         System.out.println("USER LOGIN");
         System.out.println("----------");
         System.out.println("Please enter your username: ");

         String usernameInput = in.readLine();

         String checkUser = "SELECT DISTINCT(U.login) FROM Users U WHERE U.login = '" + usernameInput + "'";
         int userAvail = esql.executeQuery(checkUser);

         if (userAvail == 1) {
            System.out.print("Please enter your password: ");
            String passInput = in.readLine();

            String checkPass = "SELECT DISTINCT(U.login) FROM Users U WHERE U.login = '" + usernameInput + "' AND U.password = '" + passInput + "'";
            int passCorrect = esql.executeQuery(checkPass);

            if (passCorrect == 1) {
               System.out.println(
                           "\n\n*******************************************************\n" +
                           "              Welcome back " + usernameInput + "      	       \n" +
                           "*******************************************************\n");
               return usernameInput;

            }
         }

      } catch(Exception e){
         System.err.println (e.getMessage());
      }

      System.out.print("Invalid username or password. \n\n");
      return null;
   }//end

// Rest of the functions definition go in here

   public static void viewProfile(String authorisedUser, PizzaStore esql) {
      try {
         String userProfile = "SELECT login, password, favoriteItems, phonenum FROM Users WHERE login = '" + authorisedUser + "'";

         System.out.println();
         List<String> output = esql.executeQueryAndReturnResult(userProfile).get(0);

         System.out.println("- - - - - - - - - - - - - - - - -\n");
         System.out.println("USER PROFILE");
         System.out.println("------------");
         System.out.println("Username: " + output.get(0));
         System.out.println("Password: " + output.get(1));


         String favItems = output.get(2);
         if (favItems == null) {
            favItems = "Currently empty";
         }

         System.out.println("Favorite Item: " + favItems);
         System.out.println("Phone Number: " + output.get(3));
         System.out.println("\n- - - - - - - - - - - - - - - - -\n");

      } catch(Exception e){
         System.err.println (e.getMessage());
      }
   }


   public static void updateProfile(String authorisedUser, PizzaStore esql) {
      try {
         boolean viewingProfile = true;

         System.out.println("\n- - - - - - - - - - - - - - - - -");

         while (viewingProfile) {
            System.out.println("\nUPDATE PROFILE");
            System.out.println("--------------");
            System.out.println("1. Update favorite item");
            System.out.println("2. Change password");
            System.out.println("3. Change phone number");
            System.out.println("4. Exit");

            switch(readChoice()) {
               case 1:
                  String itemList = "SELECT itemName FROM Items";
                  List<List<String>> menu = esql.executeQueryAndReturnResult(itemList);

                  System.out.println("List of items:");
                  for (int i = 0; i < menu.size(); i++) {
                     System.out.println(i + ": " + menu.get(i));
                  }

                  System.out.print("Select the number of your favorite item: ");
                  Integer favItemInput = Integer.parseInt(in.readLine());

                  if (favItemInput < 0 || favItemInput > menu.size()) {
                     System.out.println("Not an option.");
                     break;
                  }
                  
                  String favItemQuery = "UPDATE Users SET favoriteItems = '" + menu.get(favItemInput).get(0) + "' WHERE login = '" + authorisedUser + "'";
                  esql.executeUpdate(favItemQuery);
                  System.out.println("Your favorite item has been changed to " + menu.get(favItemInput).get(0));

                  break;

               case 2:
                  System.out.println("\nChanging password!");
                  System.out.print("Please enter your new password: ");
                  String pass1Input = in.readLine();
                  System.out.print("Please re-enter your new password: ");
                  String pass2Input = in.readLine();

                  if (pass1Input.equals(pass2Input)) {
                     String passUpdateQuery = "UPDATE Users SET password = '" + pass1Input + "' WHERE login = '" + authorisedUser + "'";
                     esql.executeUpdate(passUpdateQuery);
                     System.out.println("Your password has been changed.");
                     
                  } else {
                     System.out.println("The passwords did not match!");
                  }
                  break;

               case 3:
                  System.out.println("\nChanging phone number!");
                  System.out.print("Please enter your new phone number: ");
                  String phoneInput = in.readLine();

                  String phoneQuery = "UPDATE Users SET phoneNum = '" + phoneInput + "' WHERE login = '" + authorisedUser + "'";
                  esql.executeUpdate(phoneQuery);
                  System.out.println("Your phone number has been changed!");
                     
                  break;

               case 4: viewingProfile = false; break;

               default : System.out.println("Unrecognized choice!"); break;
            }

            System.out.println("\n- - - - - - - - - - - - - - - - -\n");

         }

      } catch(Exception e){
         System.err.println (e.getMessage());
      }
   }


   public static void viewMenu(PizzaStore esql) {
      try {
         System.out.println("\n- - - - - - - - - - - - - - - - -\n");
         System.out.println("VIEWING MENU");
         System.out.println("---------------");
         String itemList = "SELECT itemName, typeOfItem, price FROM Items";

         List<List<String>> menu = esql.executeQueryAndReturnResult(itemList);

         System.out.println("| Number ----- Items ----- Food Type ----- Price |");
         for (int i = 0; i < menu.size(); i++) {
            System.out.println("| " + (i + 1) + " ----- " + menu.get(i).get(0) + " ----- " + menu.get(i).get(1) + " ----- " + menu.get(i).get(2));
         }

         boolean adjustMenuView = true;
         int ordered = 0;

         System.out.println("\n- - - - - - - - - - - - - - - - -");

         while (adjustMenuView) {
            System.out.println("\nMenu Filters and Searches");
            System.out.println("--------------");
            System.out.println("1. Filter price (Highest to lowest)");
            System.out.println("2. Filter price (Lowest to Highest)");
            System.out.println("3. Remove price filter");
            System.out.println("4. Search by food type");
            System.out.println("5. Search for food under certain price");
            System.out.println("6. Get full menu");
            System.out.println("9. Exit");

            switch(readChoice()) {

               case 1:
                  if (ordered == 0) {
                     itemList += " ORDER BY price DESC";

                  } else if (ordered == 2) {
                     itemList = itemList.substring(0, itemList.length() - 5);
                     itemList += " DESC";
                  }

                  ordered = 1;

                  menu = esql.executeQueryAndReturnResult(itemList);


                  break;

               case 2: 
                  if (ordered == 0) {
                     itemList += " ORDER BY price ASC ";
                     
                  } else if (ordered == 1) {
                     itemList = itemList.substring(0, itemList.length() - 5);
                     itemList += " ASC ";
                  }

                  ordered = 2;

                  menu = esql.executeQueryAndReturnResult(itemList);
                  
                  break;

               case 3: 
                  itemList = itemList.substring(0, itemList.length() - 19);
                  menu = esql.executeQueryAndReturnResult(itemList);

                  ordered = 0;

                  break;

               case 4: 
                  String foodTypeList = "SELECT DISTINCT(typeOfItem) FROM Items";
                  menu = esql.executeQueryAndReturnResult(foodTypeList);

                  System.out.println("Available options:");
                  for (int i = 0; i < menu.size(); i++) {
                     System.out.println("| " + (i + 1) + ": " + menu.get(i).get(0));
                  }

                  System.out.print("Select a food type: ");
                  String userInput = in.readLine();

                  itemList = "SELECT itemName, typeOfItem, price FROM Items WHERE typeOfItem = '" + menu.get(Integer.parseInt(userInput) - 1).get(0) + "'";

                  menu = esql.executeQueryAndReturnResult(itemList);
               
                  ordered = 0;
                  break;

               case 5:
                  System.out.println("Enter a price:");
                  userInput = in.readLine();

                  itemList = "SELECT itemName, typeOfItem, price FROM Items WHERE price < '" + Double.parseDouble(userInput) + "'";
                  menu = esql.executeQueryAndReturnResult(itemList);

                  ordered = 0;
                  break;

               case 6:
                  itemList = "SELECT itemName, typeOfItem, price FROM Items";
                  menu = esql.executeQueryAndReturnResult(itemList);

                  ordered = 0;
                  break;

               case 9: adjustMenuView = false; break;

               default: System.out.println("Unrecognized choice!"); break;
            }

            System.out.println("VIEWING MENU");
            System.out.println("---------------");

            System.out.println("| Number ----- Items ----- Food Type ----- Price |");
            for (int i = 0; i < menu.size(); i++) {
               System.out.println("| " + (i + 1) + " ----- " + menu.get(i).get(0) + " ----- " + menu.get(i).get(1) + " ----- " + menu.get(i).get(2));
            }

         }
          
      System.out.println("\n- - - - - - - - - - - - - - - - -\n");
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
         
   }
	
   public static void placeOrder(PizzaStore esql) {}
   
   public static void viewAllOrders(String authorisedUser, PizzaStore esql) {
      try {
         System.out.println("\n- - - - - - - - - - - - - - - - -\n");
         System.out.println("VIEW ALL ORDERS");
         System.out.println("--------------");

         //first determine whether a user is a customer or not
         String getRole = "SELECT role FROM Users WHERE login = '" + authorisedUser + "'";
         String role = esql.executeQueryAndReturnResult(getRole).get(0).get(0);


         String getAllOrders;
         //implement customerID only order
         if (role.equals("customer")) {
            getAllOrders = "SELECT orderID, storeID, totalPrice, orderTimeStamp, orderStatus " + 
            "FROM FoodOrder WHERE login = '" + authorisedUser +
            "' ORDER BY orderTimeStamp DESC";
         } //next, implement driver and manager view
         else {
            getAllOrders = "SELECT orderID, storeID, totalPrice, orderTimeStamp, orderStatus " + 
            "FROM FoodOrder ORDER BY orderTimeStamp DESC";
         }

         //catch no-result queries
         int results = esql.executeQueryAndPrintResult(getAllOrders);
         if (results == 0) {
            System.out.println("No order history was found");
         }

         System.out.println("\n- - - - - - - - - - - - - - - - -\n");
   
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewRecentOrders(String authorisedUser, PizzaStore esql) {
       try {
          System.out.println("\n- - - - - - - - - - - - - - - - -\n");
          System.out.println("VIEW YOUR 5 MOST RECENT ORDERS"); 
          System.out.println("---------------");
 
          String getRole = "SELECT role FROM Users WHERE login = '" + authorisedUser +"'";
          String role = esql.executeQueryAndReturnResult(getRole).get(0).get(0).trim();
          
          String getRecent = "";
 
          //implement customer view
          if (role.equals("customer")) {
             getRecent = "SELECT orderID, storeID, totalPrice, orderTimeStamp, orderStatus " +
             "FROM FoodOrder WHERE login = '" + authorisedUser + "' ORDER BY orderTimeStamp DESC LIMIT 5";
          } //implement manager and driver view
          else {
             System.out.println("Press '0' to search all orders");
             System.out.println("Press any other number (1-9) to search by specific user");
 
             int choice = readChoice();
 
             if (choice == 0) {
                getRecent = "SELECT orderID, storeID, totalPrice, orderTimeStamp, orderStatus " +
                "FROM FoodOrder ORDER BY orderTimeStamp DESC LIMIT 5";
             }
             else {
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter a username: "); //get the customer/username
                String userName = scanner.nextLine();
                getRecent = "SELECT orderID, storeID, totalPrice, orderTimeStamp, orderStatus " +
                "FROM FoodOrder WHERE login = '" + userName + "' ORDER BY orderTimeStamp DESC LIMIT 5";
             }
             
          }
 
          int results = esql.executeQueryAndPrintResult(getRecent);
          if (results == 0) {
             System.out.println("No order history was found\n");
          }
 
          System.out.println("\n- - - - - - - - - - - - - - - - -\n");
 
       } catch(Exception e) {
          System.err.println(e.getMessage());
       }
    }
   public static void viewOrderInfo(String authorisedUser, PizzaStore esql) {
       try {
          System.out.println("\n- - - - - - - - - - - - - - - - -\n");
          System.out.println("VIEWING ORDER INFO");
          System.out.println("---------------");
 
          //first get the role from the user to determine whether they're a customer or not
          String getRole = "SELECT role FROM Users WHERE login = '" + authorisedUser +"'";
          String role = esql.executeQueryAndReturnResult(getRole).get(0).get(0).trim();
 
          //implement customer only query
          String getOrderInfo;
 
          if (role.equals("customer")) {
             System.out.println("Would you like to view your most recent orders first? (0 for no, 1-9 for yes)");
             int choice = readChoice();
             if (choice != 0) {
                viewRecentOrders(authorisedUser, esql);
             }
             //TODO: if they have no orders on record exit out immediately
             System.out.print("Please enter your orderID: "); //get orderID in order to better help the customer
             int orderID = Integer.parseInt(in.readLine());
 
             getOrderInfo = "SELECT orderTimeStamp, totalPrice, orderStatus, itemName, quantity FROM FoodOrder " +
                                   "NATURAL JOIN ItemsInOrder WHERE login = '" + authorisedUser + "'" +
                                   "AND orderID = '" + orderID + "'";
 
          }
          else {//implement function to get any order
             System.out.print("Please enter the orderID: "); //get orderID in order to better help the manager/driver
             int orderID = Integer.parseInt(in.readLine());
             
             getOrderInfo = "SELECT orderTimeStamp, totalPrice, orderStatus, itemName, quantity FROM FoodOrder " +
                                   "NATURAL JOIN ItemsInOrder WHERE orderID = '" + orderID + "'";
             
          }
 
          List<List<String>> result = esql.executeQueryAndReturnResult(getOrderInfo);
 
          if (result.isEmpty()) {
             System.out.println("Sorry, either this order was not found, or you do not have access to this order.");
             return;
          }
 
         //print order details
         System.out.println("\nORDER DETAILS:");
         System.out.println("Order Timestamp: " + result.get(0).get(0));
         System.out.println("Total Price: " + result.get(0).get(1));
         System.out.println("Order Status: " + result.get(0).get(2));
 
         //print items in the order
         System.out.println("\nITEMS IN ORDER:");
         System.out.println("Item Name\tQuantity");
         for (List<String> row : result) {
             System.out.println(row.get(3) + "\t\t" + row.get(4));
         }
          System.out.println("\n- - - - - - - - - - - - - - - - -\n");
          
 
       } catch(Exception e) {
          System.err.println(e.getMessage());
       }
    }
	
   public static void viewStores(PizzaStore esql) {
       try {
          System.out.println("\n- - - - - - - - - - - - - - - - -\n");
          System.out.println("VIEWING STORES");
          System.out.println("---------------");
          
 
       } catch(Exception e) {
          System.err.println(e.getMessage());
       }
    }
	
   public static void updateOrderStatus(String authorisedUser, PizzaStore esql) {
       try {
          System.out.println("\n- - - - - - - - - - - - - - - - -\n");
          System.out.println("UPDATE ORDER STATUS");
          System.out.println("---------------");
          
 
       } catch(Exception e) {
          System.err.println(e.getMessage());
       }
    }
	
   public static void updateMenu(String authorisedUser, PizzaStore esql) {
       try {
          System.out.println("\n- - - - - - - - - - - - - - - - -\n");
          System.out.println("UPDATE MENU");
          System.out.println("---------------");
          
 
       } catch(Exception e) {
          System.err.println(e.getMessage());
       }
    }
   
   
   public static void updateUser(String authorisedUser, PizzaStore esql) {
       try {
          System.out.println("\n- - - - - - - - - - - - - - - - -\n");
          System.out.println("UPDATING USER INFO");
          System.out.println("---------------");
          
 
       } catch(Exception e) {
          System.err.println(e.getMessage());
       }
   }


}//end PizzaStore

