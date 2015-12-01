package com.mplayer_remote.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import android.app.Instrumentation;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;
import android.widget.EditText;

import com.robotium.solo.Solo;
import com.mplayer_remote.Server;
import com.mplayer_remote.ServerList;
import com.mplayer_remote.XMLReaderWriter;

public class ServerListStateManagementTests extends ActivityInstrumentationTestCase2<ServerList> {

		//w celach diagnostycznych nazwa logu dla tej TestCaseClass
	private static final String TAG = "ServerListStateManagementTests";	
	//Activity ServerListActivity;
	private Instrumentation instrumentation;
		//Zapisany stan elementów interfejsu i ustawiń dla activity lub aplikacji
	private SharedPreferences settingsForActivityServerListSharedPreferences;
	private boolean isThisFirstRunboolean;
	private boolean isCryptoEnabledboolean;
	private boolean rememberAppPasswordInSesionboolean;
	
	private Solo solo;		//from robotium a "Activity"
	
	private List <Server> serverListList = new ArrayList<Server>();
	private XMLReaderWriter aXMLReaderWriter;
	
	private EditText testedEditText;
	private int lastOrientation = Solo.PORTRAIT;
	
	public ServerListStateManagementTests(){
			super(ServerList.class);
	}
	
	@Override
	protected void setUp() throws Exception{
		super.setUp();
		setActivityInitialTouchMode(false);
		serverListList= new ArrayList<Server>();
		//instrumentation = getInstrumentation();
		//settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		
		
		//solo = new Solo(getInstrumentation(),getActivity());
	}
	@Override
	protected void tearDown() throws Exception{
		super.tearDown();
			
			//back to default value for settings_for_activity_ServerListSharedPreferences
		if (serverListList != null && serverListList.isEmpty() == false){
			serverListList.clear();
		}
		instrumentation = getInstrumentation();
		settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
		editor.clear();
	       	// Commit the edits!
	    editor.commit();
	    com.mplayer_remote.ServerList.setappPasswordcharArrayForTesting(null);

		assertNull(com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting());
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
		//instrumentation.getTargetContext().deleteFile("servers.crypted");
		//assertTrue(instrumentation.getTargetContext().deleteFile("servers.crypted"));
	    //solo.finishOpenedActivities();
		//ServerListActivity.finish();
	}
	public void testPreconditions(){
		//Assert.assertTrue(ServerListActivity != null);
		//Assert.assertNotNull(settingsForActivityServerListSharedPreferences); //it's does'n test that real file exist only that object settingsForActivityServerListSharedPreferences exist

	}
	
	public void testStateDestroydialog_FIRST_TIME_RUNING(){		//this also check onPause() onResume()  showdialog_FIRST_TIME_RUNING()
		//if (serverListList != null && serverListList.isEmpty() == false){
			//serverListList.clear();
		//}
		instrumentation = getInstrumentation();
		settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
	    editor.putBoolean("is_this_first_run", true);
	    editor.putBoolean("is_crypto_enabled", false);
	    editor.putBoolean("remember_app_password_in_sesion_boolean", true);
	       	// Commit the edits!
	    editor.commit();
	    com.mplayer_remote.ServerList.setappPasswordcharArrayForTesting(null);
				
		solo = new Solo(getInstrumentation(),getActivity());
		solo.setActivityOrientation(Solo.PORTRAIT);

		assertTrue(solo != null);
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", false);
		assertTrue(isThisFirstRunboolean);
		isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", true);
		assertFalse(isCryptoEnabledboolean);
		rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
	
		
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.tile_for_dialog_FIRST_TIME_RUNING)));
		
		
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.set_app_passswordEditText);
		solo.enterText(testedEditText, "app_password");
		solo.clickOnCheckBox(0);		//CheckBox is default checked so click on it will uncheck
		//solo.finishOpenedActivities();	//onDestroy() is being called
		//solo = new Solo(getInstrumentation(), getActivity());
		/*if (lastOrientation == Solo.PORTRAIT){
			solo.setActivityOrientation(Solo.LANDSCAPE);
			lastOrientation = Solo.LANDSCAPE;
		}else {
			solo.setActivityOrientation(Solo.PORTRAIT);
			lastOrientation = Solo.PORTRAIT;
		}*/
		solo.setActivityOrientation(Solo.LANDSCAPE);
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.tile_for_dialog_FIRST_TIME_RUNING)));
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.set_app_passswordEditText);
		assertEquals("app_password", testedEditText.getText().toString() );
		assertFalse(solo.isCheckBoxChecked(0));
		
		solo.finishOpenedActivities();
	}
	
	
	public void testStateDestroydialog_GIVE_ME_A_APP_PASSWORD(){	//this also check onPause() onResume() showdialog_FIRST_TIME_RUNING() showdialog_GIVE_ME_A_APP_PASSWORD()
		//if (serverListList != null && serverListList.isEmpty() == false){
			//serverListList.clear();
		//}
		instrumentation = getInstrumentation();
		settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
	    editor.putBoolean("is_this_first_run", false);
	    editor.putBoolean("is_crypto_enabled", true);
	    editor.putBoolean("remember_app_password_in_sesion_boolean", true);
	       	// Commit the edits!
	    editor.commit();
	    com.mplayer_remote.ServerList.setappPasswordcharArrayForTesting(null);
				
		solo = new Solo(getInstrumentation(),getActivity());
		solo.setActivityOrientation(Solo.PORTRAIT);

		assertTrue(solo != null);
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", false);
		assertFalse(isThisFirstRunboolean);
		isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", true);
		assertTrue(isCryptoEnabledboolean);
		rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
		//assertNull(com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting());
		
	
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_GIVE_ME_A_APP_PASSWORD)));
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.app_password_EditText);
		solo.enterText(testedEditText, "app_password");
		solo.clickOnCheckBox(0);		//CheckBox  0 == remember_app_password_in_sesion_CheckBox is default checked so click on it will uncheck
		//solo.finishOpenedActivities();	//onDestroy() is being called
		//solo = new Solo(getInstrumentation(), getActivity());
		
		/*if (lastOrientation == Solo.PORTRAIT){
			solo.setActivityOrientation(Solo.LANDSCAPE);
			lastOrientation = Solo.LANDSCAPE;
		}else {
			solo.setActivityOrientation(Solo.PORTRAIT);
			lastOrientation = Solo.PORTRAIT;
		}*/
		solo.setActivityOrientation(Solo.LANDSCAPE);
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_GIVE_ME_A_APP_PASSWORD)));
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.app_password_EditText);
		assertEquals("app_password", testedEditText.getText().toString() );
		assertFalse(solo.isCheckBoxChecked(0));
		
		solo.finishOpenedActivities();
	}
	public void testStateDestroydialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOILEAN_IS_FALSE(){	//this also check onPause() onResume() showdialog_FIRST_TIME_RUNING() showdialog_GIVE_ME_A_APP_PASSWORD()
		//if (serverListList != null && serverListList.isEmpty() == false){
			//serverListList.clear();
		//}
		instrumentation = getInstrumentation();
		settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
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
              	                 	           	
		serverListList.add(server);
		aXMLReaderWriter = new XMLReaderWriter(getInstrumentation().getTargetContext());
	    aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListList, appPasswordcharArray);	
	    
	    com.mplayer_remote.ServerList.setappPasswordcharArrayForTesting(null);
		
	    solo = new Solo(getInstrumentation(),getActivity());
		//assertTrue(Arrays.equals("default_password".toCharArray(), com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));
		solo.setActivityOrientation(Solo.PORTRAIT);
				
		assertTrue(solo != null);
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", false);
		assertFalse(isThisFirstRunboolean);
		isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", true);
		assertTrue(isCryptoEnabledboolean);
		rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
		assertNull(com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting());
		
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_GIVE_ME_A_APP_PASSWORD)));
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.app_password_EditText);
		solo.enterText(testedEditText, "default_password");
		solo.clickOnCheckBox(0);		//CheckBox  0 == remember_app_password_in_sesion_CheckBox is default checked so click on it will uncheck
		solo.clickOnButton(solo.getString(com.mplayer_remote.R.string.text_for_check_app_password_Button_from_dialog_GIVE_ME_A_APP_PASSWORD));
		
		rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", true);
		//assertFalse(rememberAppPasswordInSesionboolean);
		assertTrue(solo.waitForActivity("ServerList"));
		solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_add_new_server));
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED)));
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_addressEditText_crypto_enabled);
		solo.enterText(testedEditText, "255.255.255.255");
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_nameEditText_crypto_enabled);
		solo.enterText(testedEditText, "test_server_name");
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.usernameEditText_crypto_enabled);
		solo.enterText(testedEditText, "test_username");
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.passwordEditText_crypto_enabled);
		solo.enterText(testedEditText, "test_password");
		solo.clickOnButton(solo.getString(com.mplayer_remote.R.string.text_for_saveButton_crypto_enabled));
		//assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fields_should_not_contain_a_whitespace_character)));
		assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fill_up_the_empty_spaces)));
		assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_correct_IP_address)));
		
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOILEAN_IS_FALSE)));
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.app_password_EditText_in_layout_for_dialog__because_remember_app_password_in_sesion_boolean_is_false);
		solo.enterText(testedEditText, "test_password");
		
		solo.setActivityOrientation(Solo.LANDSCAPE);
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOILEAN_IS_FALSE)));
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.app_password_EditText_in_layout_for_dialog__because_remember_app_password_in_sesion_boolean_is_false);
		assertEquals("test_password", testedEditText.getText().toString() );
		
		solo.finishOpenedActivities();
	}
	
	public void testStateDestroydialog_GIVE_ME_A_SERVER_PASSWORD(){
		//if (serverListList != null && serverListList.isEmpty() == false){
		//serverListList.clear();
		//}
		instrumentation = getInstrumentation();
		settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
	    editor.putBoolean("is_this_first_run", false);
	    editor.putBoolean("is_crypto_enabled", false);
	    editor.putBoolean("remember_app_password_in_sesion_boolean", true);
	       	// Commit the edits!
	    editor.commit();
	    //com.mplayer_remote.ServerList.setappPasswordcharArrayForTesting(null);
	    char[] appPasswordcharArray = "default_password".toCharArray();
	    com.mplayer_remote.ServerList.setappPasswordcharArrayForTesting(appPasswordcharArray);
	    assertTrue(Arrays.equals("default_password".toCharArray(), com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));
	
	    Server server = new Server();
	    server.setServerName("q");
	    server.setIPAddress("w");
	    server.setUsername("e");
	    server.setPassword("r".toCharArray());  
	          	                 	           	
		serverListList.add(server);
		aXMLReaderWriter = new XMLReaderWriter(getInstrumentation().getTargetContext());
	    aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListList, appPasswordcharArray);	
	    
		
	    solo = new Solo(getInstrumentation(),getActivity());
		solo.setActivityOrientation(Solo.PORTRAIT);
		assertTrue(Arrays.equals("default_password".toCharArray(), com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));
		assertTrue(solo != null);
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", true);
		assertFalse(isThisFirstRunboolean);
		isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", true);
		assertFalse(isCryptoEnabledboolean);
		rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
		
		solo.clickOnButton("q");
		
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_GIVE_ME_A_SERVER_PASSWORD)));
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_password_EditText);
		solo.enterText(testedEditText, "server_password");
		
		solo.setActivityOrientation(Solo.LANDSCAPE);
		
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_GIVE_ME_A_SERVER_PASSWORD)));
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_password_EditText);
		assertEquals("server_password", testedEditText.getText().toString() );
		
		solo.finishOpenedActivities();
	}

	public void testStateDestroydialog_ADD_NEW_SERVER_CRYPTO_ENABLED(){
			//with that config onCreate() will not show any app password prompt dialog
		instrumentation = getInstrumentation();
		settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
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
	          	                 	           	
		serverListList.add(server);
		aXMLReaderWriter = new XMLReaderWriter(getInstrumentation().getTargetContext());
	    aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListList, appPasswordcharArray);
		
	    solo = new Solo(getInstrumentation(),getActivity());
	    solo.setActivityOrientation(Solo.PORTRAIT);
	    
	    assertTrue(solo != null);
		assertTrue(Arrays.equals("default_password".toCharArray(), com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", false);
		assertFalse(isThisFirstRunboolean);
		isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", true);
		assertTrue(isCryptoEnabledboolean);
		rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
				
		solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_add_new_server));
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED)));
		
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_addressEditText_crypto_enabled);
		solo.enterText(testedEditText, "255.255.255.255");
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_nameEditText_crypto_enabled);
		solo.enterText(testedEditText, "test_server_name");
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.usernameEditText_crypto_enabled);
		solo.enterText(testedEditText, "test_username");
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.passwordEditText_crypto_enabled);
		solo.enterText(testedEditText, "test_password");
		
		solo.setActivityOrientation(Solo.LANDSCAPE);
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED)));
		
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_addressEditText_crypto_enabled);
		assertEquals("255.255.255.255", testedEditText.getText().toString());
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_nameEditText_crypto_enabled);
		assertEquals("test_server_name", testedEditText.getText().toString());
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.usernameEditText_crypto_enabled);
		assertEquals("test_username",testedEditText.getText().toString());
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.passwordEditText_crypto_enabled);
		assertEquals("test_password",testedEditText.getText().toString());
		
		solo.finishOpenedActivities();
		
	}
	
	public void testStateDestroydialog_ADD_NEW_SERVER_CRYPTO_DISABLED(){
		instrumentation = getInstrumentation();
		settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
	    editor.putBoolean("is_this_first_run", false);
	    editor.putBoolean("is_crypto_enabled", false);
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
	          	                 	           	
		serverListList.add(server);
		aXMLReaderWriter = new XMLReaderWriter(getInstrumentation().getTargetContext());
	    aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListList, appPasswordcharArray);
		
	    solo = new Solo(getInstrumentation(),getActivity());
	    solo.setActivityOrientation(Solo.PORTRAIT);
	    
	    assertTrue(solo != null);
		assertTrue(Arrays.equals("default_password".toCharArray(), com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", true);
		assertFalse(isThisFirstRunboolean);
		isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", true);
		assertFalse(isCryptoEnabledboolean);
		rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
				
		solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_add_new_server));
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED)));
		
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_addressEditText_crypto_disabled);
		solo.enterText(testedEditText, "255.255.255.255");
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_nameEditText_crypto_disabled);
		solo.enterText(testedEditText, "test_server_name");
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.usernameEditText_crypto_disabled);
		solo.enterText(testedEditText, "test_username");
		
		
		solo.setActivityOrientation(Solo.LANDSCAPE);
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED)));
		
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_addressEditText_crypto_disabled);
		assertEquals("255.255.255.255", testedEditText.getText().toString());
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_nameEditText_crypto_disabled);
		assertEquals("test_server_name", testedEditText.getText().toString());
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.usernameEditText_crypto_disabled);
		assertEquals("test_username",testedEditText.getText().toString());
		
		solo.finishOpenedActivities();

	}
	
	public void testStateDestroydialog_DELETE_SERVER(){
		instrumentation = getInstrumentation();
		settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
	    editor.putBoolean("is_this_first_run", false);
	    editor.putBoolean("is_crypto_enabled", false);
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
	          	                 	           	
		serverListList.add(server);
		aXMLReaderWriter = new XMLReaderWriter(getInstrumentation().getTargetContext());
	    aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListList, appPasswordcharArray);
		
	    solo = new Solo(getInstrumentation(),getActivity());
	    solo.setActivityOrientation(Solo.PORTRAIT);
	    
	    assertTrue(solo != null);
		assertTrue(Arrays.equals("default_password".toCharArray(), com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", true);
		assertFalse(isThisFirstRunboolean);
		isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", true);
		assertFalse(isCryptoEnabledboolean);
		rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
				
		solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_form_menu_item_delete_server));
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_DELETE_SERVER)));
		assertTrue(solo.searchText("q", 1));		//serchText only search text in on top container
		assertFalse(solo.searchText("q", 2));
		solo.setActivityOrientation(Solo.LANDSCAPE);
		//com.mplayer_remote.ServerList.setappPasswordcharArrayForTesting(appPasswordcharArray);
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_DELETE_SERVER)));
		assertTrue(solo.searchText("q", 1));		//serchText only search text in on top container
		assertFalse(solo.searchText("q", 2));
		
		solo.finishOpenedActivities();
	}
	
	public void testStateDestroydialog_CHOSE_SERVER_TO_EDIT(){
		instrumentation = getInstrumentation();
		settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
	    editor.putBoolean("is_this_first_run", false);
	    editor.putBoolean("is_crypto_enabled", false);
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
	          	                 	           	
		serverListList.add(server);
		aXMLReaderWriter = new XMLReaderWriter(getInstrumentation().getTargetContext());
	    aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListList, appPasswordcharArray);
		
	    solo = new Solo(getInstrumentation(),getActivity());
	    solo.setActivityOrientation(Solo.PORTRAIT);
	    
	    assertTrue(solo != null);
		assertTrue(Arrays.equals("default_password".toCharArray(), com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", true);
		assertFalse(isThisFirstRunboolean);
		isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", true);
		assertFalse(isCryptoEnabledboolean);
		rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
				
		solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_edit_server));
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_CHOSE_SERVER_TO_EDIT)));
		assertTrue(solo.searchText("q", 1));		//serchText only search text in on top container
		assertFalse(solo.searchText("q", 2));
		
		solo.setActivityOrientation(Solo.LANDSCAPE);
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_CHOSE_SERVER_TO_EDIT)));
		assertTrue(solo.searchText("q", 1));		//serchText only search text in on top container
		assertFalse(solo.searchText("q", 2));
		
		solo.finishOpenedActivities();
		
	}
	
	public void testStateDestroydialog_dialog_EDIT_SERVER_CRYPTO_ENABLED(){
			//with that config onCreate() will not show any app password prompt dialog
		instrumentation = getInstrumentation();
		settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
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
	          	                 	           	
		serverListList.add(server);
		aXMLReaderWriter = new XMLReaderWriter(getInstrumentation().getTargetContext());
	    aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListList, appPasswordcharArray);
		
	    solo = new Solo(getInstrumentation(),getActivity());
	    solo.setActivityOrientation(Solo.PORTRAIT);
	    
	    assertTrue(solo != null);
		assertTrue(Arrays.equals("default_password".toCharArray(), com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", false);
		assertFalse(isThisFirstRunboolean);
		isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", true);
		assertTrue(isCryptoEnabledboolean);
		rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
				
		solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_edit_server));
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_CHOSE_SERVER_TO_EDIT)));
		
		//kliknięcie w dialog_CHOSE_SERVER_TO_EDIT
		solo.clickInList(0);
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_EDIT_SERVER_CRYPTO_ENABLED)));
		
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_address_in_dialog_EditText_crypto_enabled_from_edit_server);
		solo.enterText(testedEditText, "255.255.255.255");
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_name_in_dialog_edit_server_EditText_crypto_enabled_from_edit_server);
		solo.enterText(testedEditText, "test_server_name");
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.username_in_dialog_edit_server_EditText_crypto_enabled_from_edit_server);
		solo.enterText(testedEditText, "test_username");
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.password_in_dialog_edit_server_EditText_crypto_enabled_from_edit_server);
		solo.enterText(testedEditText, "test_password");
		
		solo.setActivityOrientation(Solo.LANDSCAPE);
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_EDIT_SERVER_CRYPTO_ENABLED)));
			//remember about difrent order
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_address_in_dialog_EditText_crypto_enabled_from_edit_server);
		assertEquals("w255.255.255.255", testedEditText.getText().toString());
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_name_in_dialog_edit_server_EditText_crypto_enabled_from_edit_server);
		assertEquals("qtest_server_name", testedEditText.getText().toString());
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.username_in_dialog_edit_server_EditText_crypto_enabled_from_edit_server);
		assertEquals("etest_username",testedEditText.getText().toString());
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.password_in_dialog_edit_server_EditText_crypto_enabled_from_edit_server);
		assertEquals("rtest_password",testedEditText.getText().toString());
		
		solo.finishOpenedActivities();
	}
	
	public void testStateDestroydialog_EDIT_SERVER_CRYPTO_DISABLED(){
		instrumentation = getInstrumentation();
		settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		SharedPreferences.Editor editor = settingsForActivityServerListSharedPreferences.edit();
	    editor.putBoolean("is_this_first_run", false);
	    editor.putBoolean("is_crypto_enabled", false);
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
	          	                 	           	
		serverListList.add(server);
		aXMLReaderWriter = new XMLReaderWriter(getInstrumentation().getTargetContext());
	    aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListList, appPasswordcharArray);
		
	    solo = new Solo(getInstrumentation(),getActivity());
	    solo.setActivityOrientation(Solo.PORTRAIT);
	    
	    assertTrue(solo != null);
		assertTrue(Arrays.equals("default_password".toCharArray(), com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));
		isThisFirstRunboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_this_first_run", true);
		assertFalse(isThisFirstRunboolean);
		isCryptoEnabledboolean = settingsForActivityServerListSharedPreferences.getBoolean("is_crypto_enabled", true);
		assertFalse(isCryptoEnabledboolean);
		rememberAppPasswordInSesionboolean = settingsForActivityServerListSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", false);
		assertTrue(rememberAppPasswordInSesionboolean);
				
		solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_edit_server));
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_CHOSE_SERVER_TO_EDIT)));
		
		//kliknięcie w dialog_CHOSE_SERVER_TO_EDIT
		solo.clickInList(0);
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_EDIT_SERVER_CRYPTO_ENABLED)));
		
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_address_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
		solo.enterText(testedEditText, "255.255.255.255");
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_name_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
		solo.enterText(testedEditText, "test_server_name");
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.username_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
		solo.enterText(testedEditText, "test_username");
		
		
		solo.setActivityOrientation(Solo.LANDSCAPE);
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_EDIT_SERVER_CRYPTO_ENABLED)));
		
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_address_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
		assertEquals("w255.255.255.255", testedEditText.getText().toString());
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_name_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
		assertEquals("qtest_server_name", testedEditText.getText().toString());
		testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.username_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
		assertEquals("etest_username",testedEditText.getText().toString());
		
		
		solo.finishOpenedActivities();
	}
}

