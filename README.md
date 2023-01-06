## Student Deliverables

**1- APK file of final project** 
   - You will find it in FWD_LocationReminder/apk/

**2- Git Repository with code**
   - https://github.com/dev-anasmohammed/FWD_LocationReminder
   
   
**Test Explanation:**
1- androidTest:
   - Unit Test ReminderDao to test CRUD opertations that include: 
     - Insert and get all  
     - Insert and get by id 
     - delete all 
     
   - Unit Test ReminderLocalRepository that include: 
     - save reminder and retrive reminder by id 
     - get reimnder with no reminders 
     - get reminder not exist 
     
   - UI Testing for ReminderListFragmentTest that include: 
     - click on add reminder button to check if navigate to reminder fragment 
     - add reminder then check if displayed on the ui 
     - delete all reminder then assert that no data is displayed with icon 
     
   - UI Testing for RemindersActivity that include: 
     - add reminder with all requirements (title, description and location) 
     - add reminder with out enter title to test the snack bar error for title  
     - add reminder with out enter location to test the snack bar error for location 
     
**2- test:**
   - Test RemindersListViewModel that include: 
     - load reminders and assert that is shows data 
     - load reminder and pause dispater to shows loading 
     - load reminder and pause dispater and set error to true to shows error
     
   - Test SaveReminderViewModel that include:
     -  validate entered data when invalid title is entered and show snack bar of error
     -  validate entered data when invalid location is entered show snack bar of error
     -  validate entered data when all entered and show toast of Reminder Saved ! 
     -  save reminder and navigate back after save 
     -  save reminder and pause dispater show loading  
     
