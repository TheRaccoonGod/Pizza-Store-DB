# Project: Pizza Store Database management system

### Purpose:
I took a database class at UCR focusing on relational database management and I wanted to apply 
the knowledge that I learned in the class as well as practice my SQL and Java experience.

### Implementation:
I was given starter code to work off of, where I added several functions that called upon SQL statements to achieve each
function within the program. After loading in the data given to me, I would first prompt the user to login, and then allow
them to interact with the program depending on their role (for example, customers have limited access to certain capabilities
whereas managers will have full access.) Some of the functions (such as updateProfile and viewStores) have nested functions, designed
to better serve the user. For instance, the updateProfile will offer the user the choice to update their own login username, password,
phone number, or favorite food item; the user should not be forced to update all four simultaneously. Furthermore, viewStores also has
a “filter by state” and “filter by city” feature in order to save the user the trouble of scrolling through all 1000 rows of data. In conclusion
I first coded the base functions, then took quality assurance to tweak the code.

### Problems/Solutions:
One big problem that I had to deal with was some errors with the queries in the updating functions.  There was an error that outputted 
"Index:0 Value:0” because I failed to uppercase the table names in the “FROM” part of the query.  
Another error was that we forgot that we needed to add an extra single apostrophe mark (‘) right before and after an input for the query string.  
This error also caused the index:0 output.  One finding that I made from solving this problem is that print statements are very good at locating an 
issue.  I placed temporary print statements around parts of the code where I thought the issue occurred and was easily able to locate the issue 
to fix.
