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
                   case 4: placeOrder(authorisedUser, esql); break;
                   case 5: viewAllOrders(authorisedUser, esql); break;
                   case 6: viewRecentOrders(authorisedUser, esql); break;
                   case 7: viewOrderInfo(authorisedUser, esql); break;
                   case 8: viewStores(esql); break;
                   case 9: 
                      if (!userRole.equals("customer")) {
                         updateOrderStatus(authorisedUser, esql); 
                      }
                      break;
                    case 10: 
                      if (!userRole.equals("customer")) {
                         updateMenu(authorisedUser, esql); 
                      }
                      break;
                    case 11: 
                      if (!userRole.equals("customer")) {
                         updateUser(authorisedUser, esql); 
                      }
                      break;
 



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
	
   public static void placeOrder(String authorisedUser, PizzaStore esql) {
       try {
          System.out.println("\n- - - - - - - - - - - - - - - - -\n");
          System.out.println("PLACING ORDER");
          System.out.println("---------------");
          System.out.print("Input the city you are in: ");
          String cityLoc = in.readLine();
 
          String locationQuery = "SELECT address, city, state FROM Store WHERE city = '" + cityLoc + "'";
          List<List<String>> location = esql.executeQueryAndReturnResult(locationQuery);
 
          System.out.println("| Store Number ----- Store Location |");
          for (int i = 0; i < location.size(); i++) {
             System.out.println("| " + (i + 1) + " ----- " + location.get(i).get(0) + ", " + location.get(i).get(1) + ", " + location.get(i).get(2));
          }
 
          System.out.print("Select the store number to place order at: ");
          String inputNum = in.readLine();
 
          String findNewestOrder = "SELECT orderID FROM FoodOrder ORDER BY orderID DESC LIMIT 1";
          int newOrderId = Integer.parseInt(esql.executeQueryAndReturnResult(findNewestOrder).get(0).get(0)) + 1;
 
          locationQuery = "SELECT storeID FROM Store WHERE address = '" + location.get(Integer.parseInt(inputNum) - 1).get(0) + "'";
          int storeID = Integer.parseInt(esql.executeQueryAndReturnResult(locationQuery).get(0).get(0));
 
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          Timestamp timestamp = new Timestamp(System.currentTimeMillis());
 
          String newOrderQuery = "INSERT INTO FoodOrder VALUES (" + newOrderId + ", '" +  authorisedUser + "', " + storeID + ", 0, timestamp '" + dateFormat.format(timestamp) + "', 'incomplete')";
 
          esql.executeUpdate(newOrderQuery);
          
          boolean placingOrder = true;
          double totalPrice = 0.0;
          boolean cancelOrder = false;
 
          while (placingOrder) {
             System.out.print("Input the name of the item you want to order: ");
             String itemName = in.readLine();
 
             String validItem = "SELECT DISTINCT(itemName) FROM Items WHERE itemName = '" + itemName + "'";
             int itemCheck = esql.executeQuery(validItem);
             
             while (itemCheck == 0) {
                System.out.print("Invalid item.  Please try again or type cancel to stop placing your order: ");
                itemName = in.readLine();
 
                if (itemName.equals("cancel")) {
                   cancelOrder = true;
                   break;
                }
 
                validItem = "SELECT DISTINCT(itemName) FROM Items WHERE itemName = '" + itemName + "'";
                itemCheck = esql.executeQuery(validItem);
 
             }
 
             if (cancelOrder) {
                System.out.println("Canceling order.\n");
                break;
             }
 
             System.out.print("Input the amount of the item you want to order: ");
             String quantity = in.readLine();
 
             System.out.print("As a confirmation, you want to add " + quantity + " " + itemName + " to your order (yes or no)? ");
             String confirmation = in.readLine();
 
             if (confirmation.equals("yes")) {
                if (Integer.parseInt(quantity) > 1) {
                   System.out.println("Items added to order.");
                } else {
                   System.out.println("Item added to order.");
                }
 
                String inputOrderQuery = "INSERT INTO ItemsInOrder VALUES (" + newOrderId + ", '" + itemName + "', " + Integer.parseInt(quantity) + ")";
                esql.executeUpdate(inputOrderQuery);
 
                String itemPriceQuery = "SELECT price FROM Items WHERE itemName = '" + itemName + "'";
                double itemPrice = Double.parseDouble(esql.executeQueryAndReturnResult(itemPriceQuery).get(0).get(0));
 
                double total = (itemPrice * Integer.parseInt(quantity));
                totalPrice += total;
 
             } else {
                System.out.println("Item has not been added.");
             }
 
             System.out.print("Type 1 to place the order or type anything else to add another item: ");
             String place1 = in.readLine();
 
             if (Integer.parseInt(place1) == 1) {
                System.out.print("Type 1 for confirmation to place your order: ");
                String place2 = in.readLine();
 
                if (Integer.parseInt(place2) == 1) {
                   placingOrder = false;
                   System.out.println("Order has been placed!");
 
                   String updateTotalPrice = "UPDATE FoodOrder SET totalPrice = " + totalPrice + " WHERE orderID = " + newOrderId;
                   esql.executeUpdate(updateTotalPrice);
                }
 
             }
             System.out.println("\n");
 
          }
  
       } catch(Exception e) {
          System.err.println(e.getMessage());
       }
    }
   
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
	
   public static void updateOrderStatus(String authorisedUser, PizzaStore esql) {
       try {
          String getRole = "SELECT role FROM Users WHERE login = '" + authorisedUser + "'";
          String role = esql.executeQueryAndReturnResult(getRole).get(0).get(0).trim();
 
          if (role.equals("customer")) {
             System.out.println("Sorry, you do not have access to this feature.");
             return;
          }
 
          System.out.println("\n- - - - - - - - - - - - - - - - -\n");
          System.out.println("UPDATE ORDER STATUS");
          System.out.println("---------------");
          
          System.out.println("Would you like to view the most recent orders first? (0 for no, 1-9 for yes)");
          int choice = readChoice();
          if (choice != 0) {
             viewRecentOrders(authorisedUser, esql);
          }
          
          System.out.print("Please enter the orderID to update: "); //get orderID in order to better help the manager/driver
          int orderID = Integer.parseInt(in.readLine());
 
          String getStatus = "SELECT orderStatus FROM FoodOrder WHERE orderID = '" + orderID + "'";
          String status = esql.executeQueryAndReturnResult(getStatus).get(0).get(0).trim();
 
          String updatedStatus = "complete";
          if(status.equals("complete")) {
             updatedStatus = "incomplete";
          }
          String updateOrder = "UPDATE FoodOrder SET orderStatus = '" + updatedStatus + "' WHERE orderID = '"
             + orderID + "'";
          
          System.out.println("Updating order " + orderID + " to " + updatedStatus);
          esql.executeUpdate(updateOrder);
 
       } catch(Exception e) {
          System.err.println(e.getMessage());
       }
    }
	
   public static void updateMenu(String authorisedUser, PizzaStore esql) {
       try {
          String getRole = "SELECT role FROM Users WHERE login = '" + authorisedUser + "'";
          String role = esql.executeQueryAndReturnResult(getRole).get(0).get(0).trim();
 
          if (!role.equals("manager")) {
          System.out.println("Sorry, you do not have access to this feature.");
          return;
          }
          System.out.println("\n- - - - - - - - - - - - - - - - -\n");
          System.out.println("UPDATE MENU");
          System.out.println("---------------");
 
          System.out.println("(1) Add Item");
          System.out.println("(2) Delete Item");
          System.out.println("(3) Edit Item");
          System.out.println("(4) Cancel");
 
          String menuAction = "";
 
          switch(readChoice()) {
             case 1:
                System.out.print("Type in the name of the item you wish to add: ");
                String newItem = in.readLine();
 
                //check if menu item already exists in the database
                String checkItemQuery = "SELECT itemName FROM Items WHERE itemName = '" + newItem + "'";
                int existingItem = esql.executeQuery(checkItemQuery);
 
                if (existingItem > 0) {
                   System.out.println("Error: An item with the name " + newItem + " already exists.");
                   break; 
                }
 
                System.out.print("List the ingredients of " + newItem + " as a comma(,) separated list: ");
                String newIng = in.readLine();
 
                System.out.print("Type in the category of " + newItem + ": ");
                String newCategory = in.readLine();
 
                System.out.print("Type in the price of " + newItem + ": ");
                float newPrice = Float.parseFloat(in.readLine());
 
                System.out.print("Enter a description for " + newItem + " (press 'Enter' to skip): ");
                String newDescription = in.readLine().trim();
 
                if (newDescription.isEmpty()) {
                   newDescription = "";
                }
 
                menuAction = "INSERT INTO Items VALUES ('" + newItem + "','" + newIng + "','" + newCategory + "', '" +
                   newPrice + "', '" + newDescription + "')";
 
                esql.executeUpdate(menuAction);
 
                break;
 
             case 2:
                System.out.println("Enter the name of the item you'd like to delete: ");
                String itemToDelete = in.readLine();
                menuAction = "DELETE FROM Items WHERE itemName = '" + itemToDelete + "'";
                esql.executeUpdate(menuAction);
                break;
             case 3:
                System.out.println("Enter the name of the item you'd like to edit: ");
                String itemName = in.readLine();
 
                System.out.println("(1) Edit ingredients");
                System.out.println("(2) Edit price");
                System.out.println("(3) Edit description");
                
                switch(readChoice()) {
                   case 1:
                      System.out.print("Type in the new ingredients list as a comma(,) separated list: ");
                      String newIng1 = in.readLine();
                      menuAction = "UPDATE Items SET ingredients = '" + newIng1 + "' WHERE itemName = '" + itemName + "'";
                      esql.executeUpdate(menuAction);
                      break;
                   case 2:
                      System.out.print("Set the new price for " + itemName + ": ");
                      float newPrice1 = Float.parseFloat(in.readLine());
                      menuAction = "UPDATE Items SET price = '" + newPrice1 + "' WHERE itemName = '" + itemName + "'";
                      esql.executeUpdate(menuAction);
                      break;
                   case 3:
                      System.out.print("Update the description for " + itemName + ": ");
                      String newDesc = in.readLine();
                      menuAction = "UPDATE Items SET description = '" + newDesc + "' WHERE itemName = '" + itemName + "'";
                      esql.executeUpdate(menuAction);
                      break;
                   default: System.out.println("Unrecognized choice!"); break;
                }
                break;
             case 4:
                //cancel
                break;
             default: System.out.println("Unrecognized choice!"); break;
          }
       } catch(Exception e) {
          System.err.println(e.getMessage());
       }
    }
   
   
   public static void updateUser(String authorisedUser, PizzaStore esql) {
       try {
          String getRole = "SELECT role FROM Users WHERE login = '" + authorisedUser + "'";
          String role = esql.executeQueryAndReturnResult(getRole).get(0).get(0).trim();
 
          if (!role.equals("manager")) {
             System.out.println("Sorry, you do not have access to this feature.");
             return;
          }
          System.out.println("\n- - - - - - - - - - - - - - - - -\n");
          System.out.println("UPDATING USER INFO");
          System.out.println("---------------");
          
          System.out.println("(1) Change a username");
          System.out.println("(2) Change a password");
          System.out.println("(3) Change a user role");
          System.out.println("(4) Set/Change a favorite item");
          System.out.println("(5) Change a phone number");
          System.out.println("(6) Cancel");
          System.out.println("\n\n");
 
          System.out.print("Enter the username of the user whose details you wish to edit: ");
          String userName0 = in.readLine();
 
          String managerAction = "";
 
          switch(readChoice()) {
          case 1:
             System.out.print("Enter a new username: ");
             String userName1 = in.readLine();
 
             //check if username is already taken
             String checkUsernameQuery = "SELECT login FROM Users WHERE login = '" + userName1 + "'";
             int existingUser = esql.executeQuery(checkUsernameQuery);
 
             if (existingUser == 1) {
                System.out.println("Error: The username " + userName1 + " is already taken.");
                break;
             }
 
             managerAction = "UPDATE Users SET login = '" + userName1 + "' WHERE login = '" + userName0 + "'";
             esql.executeUpdate(managerAction);
             System.out.println("Username updated successfully!");
             break;
 
          case 2:
             System.out.print("Enter a new password: ");
             String newPass = in.readLine();
             System.out.print("Enter the new password again: ");
             String newPassCheck = in.readLine();
 
             if (!newPassCheck.equals(newPass)) {
                System.out.println("Error: your new passwords do not match. Aborting.");
                break;
             }
 
             managerAction = "UPDATE Users SET password = '" + newPass + "' WHERE login = '" + userName0 + "'";
             esql.executeUpdate(managerAction);
             System.out.println("Password updated successfully!");
             break;
 
          case 3:
             //we're going to assume you can still set 'customer' to 'customer' and so forth
             System.out.println("(1) Demote a user back to 'customer'");
             System.out.println("(2) Change user to 'driver'");
             System.out.println("(3) Promote user to 'manager'");
             System.out.println("(4) Cancel");
             System.out.println("\n\n");
 
             String getRole1 = "SELECT role FROM Users WHERE login = '" + userName0 + "'";
             String role1 = esql.executeQueryAndReturnResult(getRole1).get(0).get(0).trim();
 
             String roleChange = role1;
 
             switch(readChoice()) {
                case 1:
                   roleChange = "customer";
                   break;
                case 2:
                   roleChange = "driver";
                   break;
                case 3:
                   roleChange = "manager";
                   break;
                case 4:
                   break;
                default: System.out.println("Unrecognized choice!"); break;
             }
 
             managerAction = "UPDATE users SET role = '" + roleChange + "' WHERE login = '" + userName0 + "'";
             esql.executeUpdate(managerAction);
             if (!roleChange.equals(role1)) {
                System.out.println("Role successfully updated!");
             }
             break;
 
          case 4:
             System.out.print("Enter the name of the user's favorite item: ");
             String favoriteItem = in.readLine();
 
             //check if item exists
             String checkItem = "SELECT itemName FROM items WHERE itemName = '" + favoriteItem + "'";
 
             if (esql.executeQueryAndReturnResult(checkItem).get(0).isEmpty()) {
                System.out.println("Error: The item you entered does not currently exist.");
                break;
             }
 
             managerAction = "UPDATE Users SET favoriteItems = '" + favoriteItem + "' WHERE login = '" + userName0 + "'";
             esql.executeUpdate(managerAction);
             System.out.println("Favorite item updated successfully!");
 
             break;
 
          case 5:
             System.out.print("Enter a new phone number: ");
             String newPhoneNumber = in.readLine();
 
             managerAction = "UPDATE Users SET phoneNum = '" + newPhoneNumber + "' WHERE login = '" + userName0 + "'";
             esql.executeUpdate(managerAction);
             System.out.println("Phone number updated successfully!");
 
             break;
 
          case 6:
             //cancel
             break;
          default: System.out.println("Unrecognized choice!"); break;
          }
       } catch(Exception e) {
          System.err.println(e.getMessage());
       }
   }


}//end PizzaStore

