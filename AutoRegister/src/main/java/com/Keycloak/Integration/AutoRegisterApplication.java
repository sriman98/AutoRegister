package com.Keycloak.Integration;



import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;  
import org.apache.poi.ss.usermodel.Row;  
import org.apache.poi.xssf.usermodel.XSSFSheet;  
import org.apache.poi.xssf.usermodel.XSSFWorkbook;  

public class AutoRegisterApplication {
	
	static List<String> failedUsernames= new ArrayList<>();


	public static void AddUser(Keycloak keycloak,  String realm,  String role,  
							   boolean tempPassword,  String username,  String password,
							   String firstname,  String lastname,  String email)
	
	{
   
	try {	
	   
        // Define user
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(username);
        user.setFirstName(firstname);
        user.setLastName(lastname);
        user.setEmail(email);
        

        // Get realm
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersRessource = realmResource.users();
        
        
        // Create user (requires manage-users role)
        Response response = usersRessource.create(user);
        System.out.printf("Repsonse: %s %s%n", response.getStatus(), response.getStatusInfo());
        System.out.println(response.getLocation());
        String userId = CreatedResponseUtil.getCreatedId(response);

        System.out.printf("User created with userId: %s%n", userId);

        // Define password credential
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(tempPassword);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);

        UserResource userResource = usersRessource.get(userId);

        // Set password credential
        userResource.resetPassword(passwordCred);

        // Get realm role "user" (requires view-realm role)
        RoleRepresentation testerRealmRole = realmResource.roles()//
                .get(role).toRepresentation();

        // Assign realm role "user" to user
        userResource.roles().realmLevel() //
                .add(Arrays.asList(testerRealmRole));
        
	    }
	
	catch(Exception e)
	    {
		   System.out.println("Couldn't add UserName :"+username);
		   e.printStackTrace();		  
		   failedUsernames.add(username);
	    }		
	}
	
	
	
	
    public static void main(String[] args) {
        //intialize the given variables accordingly
        String serverUrl = "http://localhost:8080/auth/";
        String masterRealm="master";
        String adminUsername="Sriman";
        String adminPassword="password";
        String adminClient="admin-cli";
        String adminClientSecret="8f97b260-06ad-413a-ba41-88f2b44b2cc6";

        String realm = "Myrealm";
        String role="user";
        boolean tempPassword=false;
        
        String fileLocation ="C:\\Users\\srima\\OneDrive\\Documents\\userstore.xlsx";
        int sheetNumber=0;
        int rowStartIndex=2;
        int usernameIndex=4;
        int firstnameIndex=1;
        int lastnameIndex=2;
        int emailIndex=3;
        int passwordIndex=5;
        
        String username="";
        String password="";
        String firstname="";
        String lastname="";
        String email="";
        
       //read Excel file 
        Iterator<Row> itr=null;
        try 
        {
        	
        File file = new File(fileLocation);   
        FileInputStream fis = new FileInputStream(file); 
        XSSFWorkbook workBook = new XSSFWorkbook(fis);   
        XSSFSheet sheet = workBook.getSheetAt(sheetNumber); 
        itr = sheet.iterator();
        
        //iterating to first row of data
        int rowOffset=1;
        for(rowOffset=1;rowOffset<rowStartIndex;rowOffset++)
          {
        	itr.next();
          }
        
        }
        
        catch(Exception e)
        {
        	System.out.println("Couldn't read file");
        	e.printStackTrace();
        	System.exit(0);
        }
        
        
        
     try
       {
        //intialize keycloak
        Keycloak keycloak = KeycloakBuilder.builder() 
                .serverUrl(serverUrl)
                .realm(masterRealm)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(adminClient)
                .clientSecret(adminClientSecret)
                .username(adminUsername)
                .password(adminPassword)
                .build();
        
        
        while(itr.hasNext())
        {	
        	username="";
            password="";
            firstname="";
            lastname="";
            email="";
          Row row=itr.next();	
          try {
                 username  = row.getCell(usernameIndex-1) .getStringCellValue();           
                 password  = row.getCell(passwordIndex-1) .getStringCellValue();
          	   
                  try {
                        firstname = row.getCell(firstnameIndex-1).getStringCellValue();
                      } catch(NullPointerException e) {}
                  try {
                        lastname  = row.getCell(lastnameIndex-1) .getStringCellValue();
                      } catch(NullPointerException e) {}
                  try {
                        email     = row.getCell(emailIndex-1)    .getStringCellValue();
                       } catch(NullPointerException e) {}
                  AddUser(keycloak,realm,role,tempPassword,username,password,firstname,lastname,email);
               }       
           catch(NullPointerException e)
               {
        	      System.out.println("Username/password cant be empty and hence not added");
        	      e.printStackTrace();
        	      failedUsernames.add(username);
                }
         }  
        System.out.println("******Couldn't add the following usernames*******");
        for(int i=0;i<failedUsernames.size();i++)
        { System.out.println(failedUsernames.get(i));}
        
      } 
      
      catch(Exception e)
      {
    	  e.printStackTrace();
      }
     
    }
}