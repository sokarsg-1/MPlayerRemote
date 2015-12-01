package com.mplayer_remote.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.Button;

import com.robotium.solo.Solo;
import com.mplayer_remote.Server;
import com.mplayer_remote.ServerList;
import com.mplayer_remote.XMLReaderWriter;

public class ServerListonCreateTest extends ActivityInstrumentationTestCase2<ServerList> {
		//w celach diagnostycznych nazwa logu dla tej TestCaseClass
	private static final String TAG = "ServerListonCreateTest";	
	Activity mServerListActivity;
	//Instrumentation instrumentation;
	//SharedPreferences.Editor editor;
		//Zapisany stan elementów interfejsu i ustawiń dla activity lub aplikacji
	//private SharedPreferences settingsForActivityServerListSharedPreferences;
	private boolean isThisFirstRunboolean;
	private boolean isCryptoEnabledboolean;
	private boolean rememberAppPasswordInSesionboolean;
	
	private Solo solo;		//from robotium a "Activity"
	
	//private List <Server> serverListList = new ArrayList<Server>();
	//private XMLReaderWriter aXMLReaderWriter;
	
	public ServerListonCreateTest() {
		super(ServerList.class);
	}
	@Override
	protected void setUp() throws Exception{
		super.setUp();
		setActivityInitialTouchMode(false);
		//serverListList= new ArrayList<Server>();
		//Instrumentation instrumentation = getInstrumentation();
		//SharedPreferences settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		//SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
		//assertTrue(instrumentation.getTargetContext().deleteFile("servers.crypted"));
		
		//solo = new Solo(getInstrumentation(),getActivity());
	}
	@Override
	protected void tearDown() throws Exception{
		super.tearDown();
			
			//back to default value for settings_for_activity_ServerListSharedPreferences
		//if (serverListList != null && serverListList.isEmpty() == false){
			//serverListList.clear();
		//}
		//serverListList = new ArrayList<Server>();
		Instrumentation instrumentation = getInstrumentation();
		SharedPreferences settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
		editor.clear();
	       	// Commit the edits!
	    editor.commit();
	    com.mplayer_remote.ServerList.setappPasswordcharArrayForTesting(null);

		//assertNull(com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting());
		String[] appFiles = instrumentation.getTargetContext().fileList();
		Log.v(TAG, "app have a: " + appFiles.length + "private files");
		for( int i = 0; i <= appFiles.length -1; i++){
			Log.v(TAG, appFiles[i] + " is one of private file");
		}
		for( int i = 0; i <= appFiles.length - 1; i++){
			if (appFiles[i].equals("servers.crypted")){
				assertTrue(instrumentation.getTargetContext().deleteFile("servers.crypted"));
				Log.v(TAG, "tearDown delate servers.crypto");
			}
			/*
			if (appFiles[i].equals("salt")){
				assertTrue(instrumentation.getTargetContext().deleteFile("salt"));
				Log.v(TAG, "tearDown delate salt");
			}
			*/
		}

	    //solo.finishOpenedActivities();
		//ServerListActivity.finish();
	}
	public void testPreconditions(){
		//Assert.assertTrue(ServerListActivity != null);
		//Assert.assertNotNull(settingsForActivityServerListSharedPreferences); //it's does'n test that real file exist only that object settingsForActivityServerListSharedPreferences exist

	}
	
	public void testisThisFirstRunbooleantrueshowDIALOG_FIRST_TIME_RUNING(){
		
		Instrumentation instrumentation = getInstrumentation();
		SharedPreferences settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
	    editor.putBoolean("is_this_first_run", true);
	    editor.putBoolean("is_crypto_enabled", false);
	    editor.putBoolean("remember_app_password_in_sesion_boolean", true);
	    	// Commit the edits!
	    editor.commit();
	    com.mplayer_remote.ServerList.setappPasswordcharArrayForTesting(null);
	    
	    solo = new Solo(getInstrumentation(),getActivity());
	    //ServerListActivity = getActivity();
		assertTrue(solo != null);
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", false);
		assertTrue(isThisFirstRunboolean);
		isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", true);
		assertFalse(isCryptoEnabledboolean);
		rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
		assertNull(com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting());
		//TextView tile_for_dialog_FIRST_TIME_RUNINGTextView = (TextView)ServerListActivity.findViewById(com.mplayer_remote.R.id.explenation_TextView);
		//assertNotNull(tile_for_dialog_FIRST_TIME_RUNINGTextView);
		//String tile_for_dialog_FIRST_TIME_RUNINGString = tile_for_dialog_FIRST_TIME_RUNINGTextView.getText().toString();
		//assertEquals(tile_for_dialog_FIRST_TIME_RUNINGString, com.mplayer_remote.R.string.tile_for_dialog_FIRST_TIME_RUNING);
		
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.tile_for_dialog_FIRST_TIME_RUNING)));
		solo.finishOpenedActivities();
		//ServerListActivity.finish();
	}
	 
	public void testisCryptoEnabledbooleantrueandappPasswordcharArraynullshowDIALOG_GIVE_ME_A_APP_PASSWORD(){
		
		Instrumentation instrumentation = getInstrumentation();
		SharedPreferences settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
	    editor.putBoolean("is_this_first_run", false);
	    editor.putBoolean("is_crypto_enabled", true);
	    editor.putBoolean("remember_app_password_in_sesion_boolean", true);
	    	// Commit the edits!
	    editor.commit();
	    com.mplayer_remote.ServerList.setappPasswordcharArrayForTesting(null);
	   
	    solo = new Solo(getInstrumentation(),getActivity());
	    //ServerListActivity = getActivity();
	    assertTrue(solo != null);
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", true);
		assertFalse(isThisFirstRunboolean);
	    isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", false);
	    assertTrue(isCryptoEnabledboolean);
	    rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
		assertNull(com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting());
		
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_GIVE_ME_A_APP_PASSWORD)));
		solo.finishOpenedActivities();
		//ServerListActivity.finish();
	}
	
	public void testisCryptoEnabledbooleantrueandappPasswordcharArraynotnulldecryptFileWithXMLAndParseItTo_server_listappPasswordcharArray(){		//pass when start alone
		Instrumentation instrumentation = getInstrumentation();
		SharedPreferences settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
	    editor.putBoolean("is_this_first_run", false);
	    editor.putBoolean("is_crypto_enabled", true);
	    editor.putBoolean("remember_app_password_in_sesion_boolean", true);
	    	// Commit the edits!
	    editor.commit();
	        
	    char[] appPasswordcharArray = "default_password".toCharArray();
	    com.mplayer_remote.ServerList.setappPasswordcharArrayForTesting(appPasswordcharArray);
		assertTrue(Arrays.equals("default_password".toCharArray(), com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));

	    Server server = new Server();
        server.setServerName("q");
        server.setIPAddress("w");
        server.setUsername("e");
        server.setPassword("r".toCharArray());  
        
        List <Server> serverListList = new ArrayList<Server>();
    	XMLReaderWriter aXMLReaderWriter;
		serverListList.add(server);
		aXMLReaderWriter = new XMLReaderWriter(getInstrumentation().getTargetContext());
	    aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListList, appPasswordcharArray);
	    	       
	    solo = new Solo(getInstrumentation(),getActivity());
	    //mServerListActivity = getActivity();
	    assertTrue(solo != null);
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", true);
		assertFalse(isThisFirstRunboolean);
	    isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", false);
	    assertTrue(isCryptoEnabledboolean);
	    rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
		//assertEquals("testpassword".toCharArray(),com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting());
		Log.v(TAG, "getappPasswordcharArrayForTesting() zwraca: " +  new String(com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));
		assertTrue(Arrays.equals("default_password".toCharArray(), com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));
	    //assertTrue(solo.searchButton("q"));
	    //Button mButton = (Button) mServerListActivity.findViewById(0);
		//assertEquals("q", mButton.getText());
	    assertTrue(solo.searchButton("q"));
	    solo.finishOpenedActivities();
		//mServerListActivity.finish();
	}
	
	public void testisCryptoEnabledbooleanfalsedecryptFileWithXMLAndParseItTo_server_listappPasswordcharArray(){
		Instrumentation instrumentation = getInstrumentation();
		SharedPreferences settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
	    editor.putBoolean("is_this_first_run", false);
	    editor.putBoolean("is_crypto_enabled", false);
	    editor.putBoolean("remember_app_password_in_sesion_boolean", true);
	    	// Commit the edits!
	    editor.commit();
	        
	    char[] appPasswordcharArray = "default_password".toCharArray();
	    //com.mplayer_remote.ServerList.setappPasswordcharArrayForTesting(appPasswordcharArray);
	    	    
	    Server server = new Server();
        server.setServerName("q");
        server.setIPAddress("w");
        server.setUsername("e");
        server.setPassword("r".toCharArray());  
        
        List <Server> serverListList = new ArrayList<Server>();
    	XMLReaderWriter aXMLReaderWriter;
		serverListList.add(server);
		aXMLReaderWriter = new XMLReaderWriter(getInstrumentation().getTargetContext());
	    aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListList, appPasswordcharArray);
	    	       
	    solo = new Solo(getInstrumentation(),getActivity());
	    //ServerListActivity = getActivity();
	    assertTrue(solo != null);
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", true);
		assertFalse(isThisFirstRunboolean);
	    isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", true);
	    assertFalse(isCryptoEnabledboolean);
	    rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
		assertTrue(Arrays.equals("default_password".toCharArray(), com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));
		
	    assertTrue(solo.searchButton("q"));
	    
	    solo.finishOpenedActivities();
		//ServerListActivity.finish();
	}
	

}
