/*
 * Copyright (c) 2014 
 * Nathan Ross <nrossit2@gmail.com> (unafilliated with Google)
 * Google Inc. [1]
 *  
 *  [1] features of this class developed from Google's 
 *      groupsmigration-cmdline-sample project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.github.nathanross.listupload;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow.Builder;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.groupsmigration.GroupsMigration;
import com.google.api.services.groupsmigration.GroupsMigration.Archive;
import com.google.api.services.groupsmigration.GroupsMigration.Archive.Insert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * Main class for the Groups Migration API command line sample.
 */
public class GroupsMigrationBackend {
  
  /**
   *  Suggested projectname format is "MyCompany-ProductName/1.0".
   */
  
  //create a project (if you don't have one) at code.google.com/apis/console
  // put its name here.
  //private static final String APPLICATION_NAME = "sampleproject";
  private static String APPLICATION_NAME = "sampleproject";
  

  //create an Oauth client id for it (if you don't already have one) 
  // apis and auths -> credentials -> create new Client ID -> Installed application
  // click Download Json and move the json to this folder.
  // rename it to the CLIENT_SECRETS_FNAME, or enter the filename here.
  //private static final String CLIENT_SECRETS_FNAME = "../client_secrets.json"
  private static String CLIENT_SECRETS_FNAME = "../client_secrets.json";
  
  // copy the client ID to clipboard
  //login as the google apps administrator to your apps admin console
  // e.g. google.com/admin/yourdomain.com
  // add Oauth client id of your project as a project permitted 
  // 		to use the group migrations scope on your groups. to do this:
  //				security -> advanced -> Oauth Client IDs
  //	put the client ID in the left box, and this scope:
  //	https://www.googleapis.com/auth/apps.groups.migration
  //	in its scope.
  
  //enter here the email address of the group you want to archive emails in.
 
  //enter here the email you use to administrate Google Apps
  //private static String APPS_ADMIN_EMAIL = "admingoogle@yourdomain.org";
  private static String APPS_ADMIN_EMAIL = "admingoogle@yourdomain.org";
  
  

  public static String getAPPLICATION_NAME() {
    return APPLICATION_NAME;
  }
  public static void setAPPLICATION_NAME(String aPPLICATION_NAME) {
    APPLICATION_NAME = aPPLICATION_NAME;
  }
  public static String getCLIENT_SECRETS_FNAME() {
    return CLIENT_SECRETS_FNAME;
  }
  public static void setCLIENT_SECRETS_FNAME(String cLIENT_SECRETS_FNAME) {
    CLIENT_SECRETS_FNAME = cLIENT_SECRETS_FNAME;
  }
  public static String getAPPS_ADMIN_EMAIL() {
    return APPS_ADMIN_EMAIL;
  }
  public static void setAPPS_ADMIN_EMAIL(String aPPS_ADMIN_EMAIL) {
    APPS_ADMIN_EMAIL = aPPS_ADMIN_EMAIL;
  }
  public int getMIN_BEFORE_REFRESH() {
    return MIN_BEFORE_REFRESH;
  }
  public void setMIN_BEFORE_REFRESH(int mIN_BEFORE_REFRESH) {
    MIN_BEFORE_REFRESH = mIN_BEFORE_REFRESH;
  }
  
  /** Global instance of the JSON factory. */
  private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /** Global instance of the HTTP transport. */
  private static HttpTransport httpTransport;

  private static GroupsMigration client;

  private static GoogleClientSecrets clientSecrets;
  
  private static Credential authorize() throws Exception {

		Vector<String> scopes = new Vector<String>();
		scopes.add("https://www.googleapis.com/auth/apps.groups.migration");
    // load client secrets
    clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
      new InputStreamReader(new FileInputStream( new File(CLIENT_SECRETS_FNAME))));
    if (clientSecrets.getDetails().getClientId().startsWith("Enter")
        || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
      System.out.println(
          "Enter Client ID and Secret from https://code.google.com/apis/console/ "
          + "into oauth2-cmdline-sample/src/main/resources/client_secrets.json");
      System.exit(1);
    }
    // set up authorization code flow
    Builder builder = new GoogleAuthorizationCodeFlow.Builder(
            httpTransport, JSON_FACTORY, clientSecrets, scopes);
    GoogleAuthorizationCodeFlow flow = builder.setDataStoreFactory(
        dataStoreFactory).build();
    // authorize
    LocalServerReceiver lsreceiver = new LocalServerReceiver();
    AuthorizationCodeInstalledApp d = new AuthorizationCodeInstalledApp(
                                          flow, lsreceiver);
    Credential e = d.authorize(APPS_ADMIN_EMAIL);
    return e;
  }

  private static FileDataStoreFactory dataStoreFactory;

  
  private static Credential cred;
  private static Archive accessor;
  private static Long tokenInitializationTime = Long.valueOf(0);
  private static int MIN_BEFORE_REFRESH = 45;
  private static void setupAccessor() {
    client = new GroupsMigration.Builder(httpTransport, JSON_FACTORY, cred)
      .setApplicationName(APPLICATION_NAME)
      .build();
    accessor = client.archive();
  }
  public static void initializeClient() {
    initializeClient(false);
  }
  public static void initializeClient(boolean force) {
    try {
      //if we're not forcing a reinitialization,
      // and already initialized, just return;
      if (tokenInitializationTime > 0 && ! force) { return; }
      tokenInitializationTime = System.currentTimeMillis();
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      dataStoreFactory = new FileDataStoreFactory(
	      new java.io.File("C:/email/store/oauth2_sample"));
		      // service account credential (uncomment setServiceAccountUser for domain-wide delegation)
      cred= GroupsMigrationBackend.authorize();
      setupAccessor();
      // set up global GroupsMigration instance
		//get this by having a message sent off from your group.
		//in the header of the original should be a param x-google-group-id
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
  public static void sendEmail(String groupEmailAddr, String emailPath) {
    sendEmail(groupEmailAddr, new File(emailPath));
  }
  public static void sendEmail(String groupEmailAddr, File emailFile) {
    try {
      //make sure we have an accessor and to refresh its token as needed.
      if (tokenInitializationTime == 0) {
        initializeClient();
      } else if ((tokenInitializationTime +
          MIN_BEFORE_REFRESH*60*1000) < System.currentTimeMillis()) {
        cred.refreshToken();
        setupAccessor();        
      }
      
      AbstractInputStreamContent mediaContent = 
          new FileContent("message/rfc822", emailFile);
      
      Insert req = accessor.insert(groupEmailAddr,mediaContent);
      req.setOauthToken(cred.getAccessToken());
      req.execute();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
  
  //only used in sendEmailTest
  public static final String GROUP_EMAIL_ADDR = "testmigrate@yourdomain.org";
  public static void sendEmailTest(String[] args) {
    //this is like calling initializeClient() and sendEmail() at once.
    // but here, the client is stored in a local variable,
    // and not as a class member variable. so after the email is sent
    // and the function ends, the client is closed.
    // this is kept here for easy comparison to Google's sample code.
    // obviously you will have to set the constants back to static to
    // make this work.
    try {
      // initialize the transport
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      dataStoreFactory = new FileDataStoreFactory(
    	      new java.io.File("C:/email/store/oauth2_sample"));

            // service account credential (uncomment setServiceAccountUser for domain-wide delegation)
      Credential cred= GroupsMigrationBackend.authorize();

      // set up global GroupsMigration instance
      client = new GroupsMigration.Builder(httpTransport, JSON_FACTORY, cred)
      		.setApplicationName(APPLICATION_NAME)
      		.build();
      Archive accessor = client.archive();
      //get this by having a message sent off from your group.
      //in the header of the original should be a param x-google-group-id
      String groupID = GROUP_EMAIL_ADDR;
      
      AbstractInputStreamContent mediaContent = new FileContent("message/rfc822", 
    		  new File("test_email.eml"));
      
      Insert req = accessor.insert(groupID,mediaContent);
      req.setOauthToken(cred.getAccessToken());
      //System.out.println("hello world");
      req.execute();

    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
  } 
}
