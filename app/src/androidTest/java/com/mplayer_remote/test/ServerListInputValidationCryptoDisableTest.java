package com.mplayer_remote.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Instrumentation;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.EditText;

import com.robotium.solo.Solo;
import com.mplayer_remote.Server;
import com.mplayer_remote.ServerList;
import com.mplayer_remote.XMLReaderWriter;

public class ServerListInputValidationCryptoDisableTest extends ActivityInstrumentationTestCase2<ServerList>{


	//w celach diagnostycznych nazwa logu dla tej TestCaseClass
		private static final String TAG = "ServerListInputValidationTestCryptoDisabledTest";	
		Instrumentation instrumentation;
			//Zapisany stan elementów interfejsu i ustawiń dla activity lub aplikacji
		private SharedPreferences settingsForActivityServerListSharedPreferences;
		private boolean isThisFirstRunboolean;
		private boolean isCryptoEnabledboolean;
		private boolean rememberAppPasswordInSesionboolean;
		private Solo solo;		//from robotium a "Activity"
		private List <Server> serverListList = new ArrayList<Server>();
		private XMLReaderWriter aXMLReaderWriter;
		private EditText testedEditText;
		
		public ServerListInputValidationCryptoDisableTest() {
			super(ServerList.class);
		}
		@Override
		protected void setUp() throws Exception{
			super.setUp();
			setActivityInitialTouchMode(false);
				//with that config onCreate() will not show any app password promt dialog
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
			assertTrue(Arrays.equals("default_password".toCharArray(), com.mplayer_remote.ServerList.getappPasswordcharArrayForTesting()));
			//app_password_EditText = com.mplayer_remote.R.id.app_password_EditText;
			//solo.enterText(com.mplayer_remote.R.id.app_password_EditText, "testpassword");
			//solo.clickOnButton(com.mplayer_remote.R.id.check_app_password_Button);
			
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

		}
		public void testPreConditions() {
			assertNotNull(solo);
			
		}
		
			//IP_address validation test	-- 		do usunięcia bo mam unit test dla metody isIPv4OrIPv6 z ServerList
		public void test1WrongIP_addressEditText_crypto_enabledIndialog_ADD_NEW_SERVER_CRYPTO_ENABLED(){
			solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_add_new_server));
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED)));
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_addressEditText_crypto_disabled);
			solo.enterText(testedEditText, "256.256.256.256");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_nameEditText_crypto_disabled);
			solo.enterText(testedEditText, "test_server_name");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.usernameEditText_crypto_disabled);
			solo.enterText(testedEditText, "test_username");

			solo.clickOnButton(solo.getString(com.mplayer_remote.R.string.text_for_saveButton_crypto_enabled));
			
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_correct_IP_address)));		//toast that notify about wrong IP
			assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fill_up_the_empty_spaces)));
			//assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fields_should_not_contain_a_whitespace_character)));
			
		}
		public void test1GoodIP_addressEditText_crypto_enabledIndialog_ADD_NEW_SERVER_CRYPTO_ENABLED(){
			solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_add_new_server));
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED)));
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_addressEditText_crypto_disabled);
			solo.enterText(testedEditText, "255.255.255.255");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_nameEditText_crypto_disabled);
			solo.enterText(testedEditText, "test_server_name");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.usernameEditText_crypto_disabled);
			solo.enterText(testedEditText, "test_username");

			solo.clickOnButton(solo.getString(com.mplayer_remote.R.string.text_for_saveButton_crypto_enabled));
			
			assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fill_up_the_empty_spaces)));
			//assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fields_should_not_contain_a_whitespace_character)));
			assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_correct_IP_address)));		//toast that notify about wrong IP
			assertTrue(solo.searchButton("test_server_name"));		//button symbolize that server was added
			solo.finishOpenedActivities();		//because activity start yourself durnig this test 
		}
		
			//test that no field can by empty in dialog_ADD_NEW_SERVER_CRYPTO_DISABLED()
		public void testZeroLengthserver_nameEditTextIndialog_ADD_NEW_SERVER_CRYPTO_DISABLED(){
			solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_add_new_server));
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED)));
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_addressEditText_crypto_disabled);
			solo.enterText(testedEditText, "255.255.255.255");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_nameEditText_crypto_disabled);
			solo.enterText(testedEditText, "");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.usernameEditText_crypto_disabled);
			solo.enterText(testedEditText, "test_username");
			
			solo.clickOnButton(solo.getString(com.mplayer_remote.R.string.text_for_saveButton_crypto_enabled));
			//assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fields_should_not_contain_a_whitespace_character)));
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fill_up_the_empty_spaces)));
			assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_correct_IP_address)));	
		}
		public void testZeroLengthip_addressEditTextIndialog_ADD_NEW_SERVER_CRYPTO_DISABLED(){
			solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_add_new_server));
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED)));
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_addressEditText_crypto_disabled);
			solo.enterText(testedEditText, "");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_nameEditText_crypto_disabled);
			solo.enterText(testedEditText, "test_server_name");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.usernameEditText_crypto_disabled);
			solo.enterText(testedEditText, "test_username");
			
			solo.clickOnButton(solo.getString(com.mplayer_remote.R.string.text_for_saveButton_crypto_enabled));
			//assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fields_should_not_contain_a_whitespace_character)));
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fill_up_the_empty_spaces)));
			assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_correct_IP_address)));		//if else if are the rison of this	
		}
		public void testZeroLengthusernameEditTextIndialog_ADD_NEW_SERVER_CRYPTO_DISABLED(){
			solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_add_new_server));
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED)));
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_addressEditText_crypto_disabled);
			solo.enterText(testedEditText, "255.255.255.255");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_nameEditText_crypto_disabled);
			solo.enterText(testedEditText, "test_server_name");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.usernameEditText_crypto_disabled);
			solo.enterText(testedEditText, "");
			
			solo.clickOnButton(solo.getString(com.mplayer_remote.R.string.text_for_saveButton_crypto_enabled));
			//assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fields_should_not_contain_a_whitespace_character)));
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fill_up_the_empty_spaces)));
			//
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_addressEditText_crypto_disabled);
			assertEquals("255.255.255.255", testedEditText.getText().toString());
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_nameEditText_crypto_disabled);
			assertEquals("test_server_name", testedEditText.getText().toString());
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.usernameEditText_crypto_disabled);
			assertEquals("",testedEditText.getText().toString());
		}
		
			//test that no field can by empty in dialog_EDIT_SERVER_CRYPTO_ENABLED()
		public void testZeroLengthserver_name_in_dialog_edit_server_EditText_crypto_disabled_from_edit_serverIndialog_EDIT_SERVER_CRYPTO_DISABLED(){
			solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_edit_server));
			//kliknięcie w dialog_CHOSE_SERVER_TO_EDIT
			solo.clickInList(0);
			
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_EDIT_SERVER_CRYPTO_ENABLED)));
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_address_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
			solo.enterText(testedEditText, "255.255.255.255");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_name_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
			solo.enterText(testedEditText, "");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.username_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
			solo.enterText(testedEditText, "test_username");
			
			solo.clickOnButton(solo.getString(com.mplayer_remote.R.string.text_for_saveButton_crypto_enabled));
			//assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fields_should_not_contain_a_whitespace_character)));
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fill_up_the_empty_spaces)));
			assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_correct_IP_address)));	
		}
		public void testZeroLengthIP_address_in_dialog_EditText_crypto_disabled_from_edit_serverIndialog_EDIT_SERVER_CRYPTO_DISABLED(){
			solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_edit_server));
			//kliknięcie w dialog_CHOSE_SERVER_TO_EDIT
			solo.clickInList(0);
			
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_EDIT_SERVER_CRYPTO_ENABLED)));
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_address_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
			solo.enterText(testedEditText, "");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_name_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
			solo.enterText(testedEditText, "test_server_name");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.username_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
			solo.enterText(testedEditText, "test_username");
			
			solo.clickOnButton(solo.getString(com.mplayer_remote.R.string.text_for_saveButton_crypto_enabled));
			//assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fields_should_not_contain_a_whitespace_character)));
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fill_up_the_empty_spaces)));
			assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_correct_IP_address)));	
		}
		public void testZeroLengthusername_in_dialog_edit_server_EditText_crypto_disabled_from_edit_serverIndialog_EDIT_SERVER_CRYPTO_DISABLED(){
			solo.clickOnMenuItem(solo.getString(com.mplayer_remote.R.string.title_for_menu_item_edit_server));
			//kliknięcie w dialog_CHOSE_SERVER_TO_EDIT
			solo.clickInList(0);
			
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.title_for_dialog_EDIT_SERVER_CRYPTO_ENABLED)));
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.IP_address_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
			solo.enterText(testedEditText, "255.255.255.255");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.server_name_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
			solo.enterText(testedEditText, "test_server_name");
			testedEditText = (EditText)solo.getView(com.mplayer_remote.R.id.username_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
			solo.enterText(testedEditText, "");
			
			solo.clickOnButton(solo.getString(com.mplayer_remote.R.string.text_for_saveButton_crypto_enabled));
			//assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fields_should_not_contain_a_whitespace_character)));
			assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_fill_up_the_empty_spaces)));
			assertFalse(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_toast_correct_IP_address)));	
		}
		

}
