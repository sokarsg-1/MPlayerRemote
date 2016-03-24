/*
MPlayer Remote
    Copyright (C) 2015  Rafał Kałęcki

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package com.mplayer_remote;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;



/**
 * Aktywność wyświetlająca listę przycisków symbolizujących serwery SSH, których dane są zapisane w pamięci urządzenia. 
 * Dostarcza graficzny interfejs umożliwiający rozpoczęcie łączenia z serwerem oraz dodawanie, edycje i usuwanie danych serwerów SSH.
 * Pozwala również na wyłączenie aplikacji i uruchomienie aktywności SettingsForAPP.
 * @author sokar
 * @see android.app.Activity
 */
public class ServerList extends Activity{
		//w celach diagnostycznych nazwa logu dla tego Activity
	private static final String TAG = "ServerList";
		//Dialogs
	/**
	 * Numer okna dialogowego <code>DIALOG_FIRST_TIME_RUNING</code>.
	 */
	private static final int DIALOG_FIRST_TIME_RUNING = 0;
	
	/**
	 * Numer okna dialogowego <code>DIALOG_GIVE_ME_A_APP_PASSWORD</code>.
	 */
	private static final int DIALOG_GIVE_ME_A_APP_PASSWORD = 1;
	
	/**
	 * Numer okna dialogowego <code>DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE</code>.
	 */
	private static final int DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE = 2;
	
	/**
	 * Numer okna dialogowego <code>DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST</code>.
	 */
	private static final int DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST = 3;
	
	/**
	 * Numer okna dialogowego <code>DIALOG_GIVE_ME_A_SERVER_PASSWORD</code>.
	 */
	private static final int DIALOG_GIVE_ME_A_SERVER_PASSWORD = 4;
	
	/**
	 * Numer okna dialogowego <code>DIALOG_ADD_NEW_SERVER_CRYPTO_ENABLED</code>.
	 */
	private static final int DIALOG_ADD_NEW_SERVER_CRYPTO_ENABLED = 5;
	
	/**
	 * Numer okna dialogowego <code>DIALOG_ADD_NEW_SERVER_CRYPTO_DISABLED</code>.
	 */
	private static final int DIALOG_ADD_NEW_SERVER_CRYPTO_DISABLED = 6;
	
	/**
	 * Numer okna dialogowego <code>DIALOG_CHOOSE_SERVER_TO_EDIT</code>.
	 */
	private static final int DIALOG_CHOOSE_SERVER_TO_EDIT = 7;
	
	/**
	 * Numer okna dialogowego <code>DIALOG_EDIT_SERVER_CRYPTO_ENABLED</code>.
	 */
	private static final int DIALOG_EDIT_SERVER_CRYPTO_ENABLED = 8;
	
	/**
	 * Numer okna dialogowego <code>DIALOG_EDIT_SERVER_CRYPTO_DISABLED</code>.
	 */
	private static final int DIALOG_EDIT_SERVER_CRYPTO_DISABLED = 9;
	
	/**
	 * Numer okna dialogowego <code>DIALOG_DELETE_SERVER</code>.
	 */
	private static final int DIALOG_DELETE_SERVER = 10;
	
	/**
	 * Numer okna dialogowego <code>DIALOG_DO_DELATE</code>.
	 */
	private static final int DIALOG_DO_DELATE = 11;
	
	/**
	 * Numer okna dialogowego <code>DIALOG_LICENSE</code>.
	 */
	private static final int DIALOG_LICENSE = 12;
	
//Okna dialogowe.
	
	/**
	 * Okno dialogowe <code>dialog_FIRST_TIME_RUNING</code>;
	 */
	private Dialog dialog_FIRST_TIME_RUNING;
	
	/**
	 * Okno dialogowe <code>dialog_GIVE_ME_A_APP_PASSWORD</code>;
	 */
	private Dialog dialog_GIVE_ME_A_APP_PASSWORD;
	
	/**
	 * Okno dialogowe <code>dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOILEAN_IS_FALSE</code>;
	 */
	private Dialog dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOILEAN_IS_FALSE;
	
	/**
	 * Okno dialogowe <code>dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST</code>;
	 */
	private Dialog dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST;
	
	/**
	 * Okno dialogowe <code>dialog_GIVE_ME_A_SERVER_PASSWORD</code>;
	 */
	private Dialog dialog_GIVE_ME_A_SERVER_PASSWORD;
	
	/**
	 * Okno dialogowe <code>dialog_ADD_NEW_SERVER_CRYPTO_ENABLED</code>;
	 */
	private Dialog dialog_ADD_NEW_SERVER_CRYPTO_ENABLED;
	
	/**
	 * Okno dialogowe <code>dialog_ADD_NEW_SERVER_CRYPTO_DISABLED</code>;
	 */
	private Dialog dialog_ADD_NEW_SERVER_CRYPTO_DISABLED;
	
	/**
	 * Okno dialogowe <code>dialog_DELETE_SERVER</code>;
	 */
	private Dialog dialog_DELETE_SERVER;
	
	/**
	 * Okno dialogowe <code>dialog_CHOSE_SERVER_TO_EDIT</code>;
	 */
	private Dialog dialog_CHOSE_SERVER_TO_EDIT;
	
	/**
	 * Okno dialogowe <code>dialog_EDIT_SERVER_CRYPTO_ENABLED</code>;
	 */
	private Dialog dialog_EDIT_SERVER_CRYPTO_ENABLED;
	
	/**
	 * Okno dialogowe <code>dialog_EDIT_SERVER_CRYPTO_DISABLED</code>;
	 */
	private Dialog dialog_EDIT_SERVER_CRYPTO_DISABLED;
	
	/**
	 * Okno dialogowe <code>dialog_DO_DELATE</code>;
	 */
	private Dialog dialog_DO_DELATE;
		
	/**
	 * Okno dialogowe z licencjami <code>dialog_LICENSE</code>. 
	 */
	private Dialog dialog_LICENSE;

	/**
	 * Progress dialog showed when connecting to SSH server
	 */
	private ProgressDialog connectingToSshProgressDialog;

	// Our handler for received Intents. This will be called whenever an Intent
	// with an action named "NewnowPlayingFileString" is broadcasted.
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent
			String newnowPlayingFileString = intent.getStringExtra("NewnowPlayingFileString");
			Log.d("receiver", "Got message: " + newnowPlayingFileString);

			connectingToSshProgressDialog.dismiss();
		}
	};

	/**
	 * Pamięta czy okna dialogowe dialog_FIRST_TIME_RUNING i dialog_GIVE_ME_A_APP_PASSWORD tworzone w onCreate są wyświetlane na ekranie. Klasa StateHolder wraz z metodami onRetainNonConfigurationInstance, onCreate,
	 * showdialog_FIRST_TIME_RUNING, dismissdialog_FIRST_TIME_RUNING, showdialog_GIVE_ME_A_APP_PASSWORD, dismissdialog_GIVE_ME_A_APP_PASSWORD, onPause i onResume poprawnie zarządza wyświetlaniem okien dialogowych
	 * dialog_FIRST_TIME_RUNING i dialog_GIVE_ME_A_APP_PASSWORD z czym nie radziły sobie standardowe mechanizmy systemu Android.  
	 */
	private StateHolder mStateHolder;	 
	
	//private Intent intent_start_ConnectToServer;
		
		//do przekazywania wyboru użytkownika między oknami dialogowymi
	/**
	 * Numer serwera, którego dane użytkownik chce edytować. Używany przez okna dialogowe <code>dialog_CHOSE_SERVER_TO_EDIT</code>, <code>dialog_EDIT_SERVER_CRYPTO_ENABLED</code>, <code>dialog_EDIT_SERVER_CRYPTO_DISABLED</code>.
	 */
	private int serverToEditint;
	
	/**
	 * Numer serwera, którego dane użytkownik chce usunąć. Używany przez okna dialogowe <code>dialog_DELETE_SERVER</code> i <code>dialog_DO_DELATE</code>.
	 */
	private int serverToDelete;
	
		//Zapisany stan elementów interfejsu i ustawień dla Activity lub całej aplikacji
	/**
	 * Interfejs służący do przechowywania preferencji, w tym przypadku ustawień aplikacji, w pamięci wewnętrznej urządzenia.
	 * @see android.content.SharedPreferences  
	 */
	private SharedPreferences settingsForAPPSharedPreferences;
	
	/**
	 *  Zmienna logiczna mająca wartość <code>true</code> w przypadku pierwszego uruchomienia aplikacji i <code>false</code> w przeciwnym. 
	 *  Wartość odczytywana za pomocą interfejsu {@link com.mplayer_remote.ServerList#settingsForAPPSharedPreferences}.
	 */
	private boolean isThisFirstRunboolean;
	
	/**
	 * Zmienna logiczna mająca wartość <code>true</code> kiedy użytkownik aplikacji zdecydował, że ta ma używać szyfrowania do przechowywania danych serwerów SSH.
	 * Wartość odczytywana za pomocą interfejsu {@link com.mplayer_remote.ServerList#settingsForAPPSharedPreferences}.
	 */
	private boolean isCryptoEnabledboolean;
	
	/**
	 * Zmienna logiczna pamiętająca wybór użytkownika odnośnie tego czy hasło aplikacji ma być zapamiętane przez cały okres uruchomienia aplikacji czy też zapomniane od razu po użyciu.
	 * <code>True</code> jeśli użytkownik przy podawaniu hasła aplikacji w oknie dialogowym <code>DIALOG_GIVE_ME_A_APP_PASSWORD</code> zaznaczył pole wyboru nakazujące aplikacji zapamiętać hasło,
	 * <code>false</code> w przeciwnym wypadku.
	 */
	private boolean rememberAppPasswordInSesionboolean; //= false;	
	
		//items dla DIALOG_DELETE_SERVER i DIALOG_EDIT_SERVER
	/**
	 * Tablica przechowująca nazwy serwerów na potrzebę okien dialogowych dialog_DELETE_SERVER, dialog_DO_DELATE.
	 */
	private CharSequence[] itemsFor_DIALOG_DELETE_SERVER;
	
	/**
	 * Tablica przechowująca nazwy serwerów na potrzebę okna dialogowego dialog_CHOSE_SERVER_TO_EDIT.
	 */
	private CharSequence[] itemsFor_DIALOG_EDIT_SERVER;
	
	
		//Lista serverów
	/**
	 * Lista obiektów klasy {@link com.mplayer_remote.Server}. Tu jest zapisywana odszyfrowana z pamięci urządzenia lista serwerów SSH. 
	 */
	private List<Server> serverListArrayList = new ArrayList<Server>();
	//private List<Server> input =  new ArrayList<Server>(); //tu wczytuje 
		
		//XML 
    /**
     * Obiekt klasy {@link com.mplayer_remote.XMLReaderWriter}, służący do przekształcania listy {@link com.mplayer_remote.ServerList#serverListArrayList} w zaszyfrowany algorytmem AES plik XML,
     * udostępnia on również możliwość odszyfrowania tego pliku XML i przekształcenie go z powrotem w listę {@link com.mplayer_remote.ServerList#serverListArrayList}.
     */
    private XMLReaderWriter aXMLReaderWriter;
		
    	//kryptografia
	/**
	 * Hasło aplikacji, na jego podstawie jest generowany klucz szyfru AES potrzebny do zaszyfrowania w pamięci urządzenia danych serwerów SSH. 
	 */
	private static char[] appPasswordcharArray = null; 	//a app password used to creating a AES key for encrypt a files with SSH servers date (e.g., password).
	/*
	public static void setappPasswordcharArrayForTesting(char[] newvalue){		//only for testing remove before deploy
		appPasswordcharArray = newvalue;
	}	
	public static char[]  getappPasswordcharArrayForTesting(){		//only for testing remove before deploy
		return appPasswordcharArray;
	}
	*/
	//private static final String IP_PATTERN = "\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b";

	
	/**
	 * Element GUI potrzebny do jego dynamicznego generowania. 
	 */
	private LinearLayout ll;
		
    
	
	
	
	/** 
     * Metoda wywoływana przez system Android między funkcjami <code>onStop</code> i <code>onDestroy</code> w czasie restartu aktywności wywołanego zmianą konfiguracji urządzenia (np. zmianą orientacji ekranu).
     * Tu zwraca (do metody onCreate) obiekt prywatnej klasy StateHolder zawierający informacje czy okna dialogowe dialog_FIRST_TIME_RUNING i dialog_GIVE_ME_A_APP_PASSWORD są wyświetlane na ekranie.
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     * @return obiekt prywatnej klasy <code>StateHolder</code> służący do zapamiętywania czy dialogi <code>dialog_FIRST_TIME_RUNING</code> i <code>dialog_GIVE_ME_A_APP_PASSWORD</code> były wyświetlane przed restartem aktywności.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {		//called by android system on configuration change between onStop() and onDestroy()
      return mStateHolder;
    }
	   
    /**
     * Metoda wywoływana przez system Android przy starcie aktywności.
     * Tu następuje wyczytanie ustawień aplikacji, zainicjowanie GUI aktywności, wyświetlenie jednego z okien dialogowych dialog_FIRST_TIME_RUNING lub dialog_GIVE_ME_A_APP_PASSWORD 
     * albo odszyfrowanie pliku z zapisaną listą serwerów (w przypadku gdy metoda onCreate wywoływana jest po restarcie aktywności lub gdy aplikacja szyfruje dane domyślnym hasłem). 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        	//ustawianie GUI
        ScrollView sv = new ScrollView(this);
        ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        TextView text = new TextView(this);
        text.setText(R.string.activity_server_list_title);
        text.setTextSize(18);
        ll.addView(text);
        sv.addView(ll);
        this.setContentView(sv);

           	//creating a XML
        Context mContext = getApplicationContext();
        aXMLReaderWriter = new XMLReaderWriter(mContext);
        
        settingsForAPPSharedPreferences = getSharedPreferences("settings_for_APP", 0);
        isThisFirstRunboolean = settingsForAPPSharedPreferences.getBoolean("is_this_first_run", true);
        isCryptoEnabledboolean = settingsForAPPSharedPreferences.getBoolean("is_crypto_enabled", true);	//było false
        rememberAppPasswordInSesionboolean = settingsForAPPSharedPreferences.getBoolean("remember_app_password_in_sesion_boolean", true);
    		
        Log.v(TAG, "aktualny isThisFirstRunboolean: " + isThisFirstRunboolean);
        Log.v(TAG, "aktualny isCryptoEnabledboolean: " + isCryptoEnabledboolean);
        Log.v(TAG, "aktualny rememberAppPasswordInSesionboolean: " + rememberAppPasswordInSesionboolean);
        /*
        Intent intent_from_SettingsForServerList = getIntent(); //getIntent() zwraca obiekt Intent który wystartował Activity
        appPasswordcharArray = intent_from_SettingsForServerList.getCharArrayExtra("app_password");
        */
        if (appPasswordcharArray != null){
        	String appPasswordcharArrayConvertedToString = new String(appPasswordcharArray);
        	Log.v(TAG, "Aktuane appPasswordcharArray: " + appPasswordcharArrayConvertedToString);
        }

		if (savedInstanceState != null) {
			serverToEditint = savedInstanceState.getInt("serverToEditint");
			serverToDelete = savedInstanceState.getInt("serverToDelete");
		}
        Object retained = getLastNonConfigurationInstance();	
        if (retained != null && retained instanceof StateHolder) {	//if onCreate follow configuration change
          mStateHolder = (StateHolder) retained;
        } else {
          mStateHolder = new StateHolder();		//a normal activity start
        }
        
        //if (rememberAppPasswordInSesionboolean == false){
        	//Intent intent_from_SettingsForServerList = getIntent(); //getIntent() zwraca obiekt Intent który wystartował Activity
        	//appPasswordcharArray = intent_from_SettingsForServerList.getCharArrayExtra("app_password");
        //}
    	
        if (isThisFirstRunboolean == true && mStateHolder.mIsShowingDialog_FIRST_TIME_RUNING == false){		//if configuration change happens them onResume will show dialog
        	Log.v(TAG, "(isThisFirstRunboolean == true && mStateHolder.mIsShowingDialog_FIRST_TIME_RUNING == false) ");
        	showdialog_FIRST_TIME_RUNING();
        	
        }else if (isCryptoEnabledboolean == true && appPasswordcharArray == null && mStateHolder.mIsShowingDialog_GIVE_ME_A_PASSWORD == false && isThisFirstRunboolean == false){
        		//I check isThisFirstRunboolean == false because screen rotation can trigger this on real first app run
        	Log.v(TAG, "(isCryptoEnabledboolean == true && appPasswordcharArray == null && mStateHolder.mIsShowingDialog_GIVE_ME_A_PASSWORD == false && isThisFirstRunboolean == false)");
        	showdialog_GIVE_ME_A_APP_PASSWORD();
        	
        }else if (isCryptoEnabledboolean == true && appPasswordcharArray != null && mStateHolder.mIsShowingDialog_FIRST_TIME_RUNING == false){		// I check && mStateHolder.mIsShowingDialog_FIRST_TIME_RUNING == false because screen rotation can trigger this when back form SettingsForAPP
        	Log.v(TAG, "(isCryptoEnabledboolean == true && appPasswordcharArray != null && mStateHolder.mIsShowingDialog_FIRST_TIME_RUNING == false)");
        	try {
        		serverListArrayList = aXMLReaderWriter.decryptFileWithXMLAndParseItToServerList(appPasswordcharArray);
				
		        if (serverListArrayList != null){ 
		 	       for ( int i = 0; i < serverListArrayList.size(); i++){

					   createConnectButtons(i);
		 	        	
		 	       }
		         }
			} catch (WrongPasswordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      	
        }else if (isCryptoEnabledboolean == false ){		//isThisFirstRunboolean == false is checked because def values // usunąłem && isThisFirstRunboolean == false
        	Log.v(TAG, "(isCryptoEnabledboolean == false )");
        	appPasswordcharArray = "default_password".toCharArray();
      		try {
				serverListArrayList = aXMLReaderWriter.decryptFileWithXMLAndParseItToServerList(appPasswordcharArray);
				
		        if (serverListArrayList != null){
		 	       for ( int i = 0; i < serverListArrayList.size(); i++){

		 	        	createConnectButtonsThatAskForServerPassword(i);

				   }
				}
				
			} catch (WrongPasswordException e) {
				Toast.makeText(getApplicationContext(), R.string.wrong_app_password_exeption, Toast.LENGTH_SHORT).show();
				
			}
      		
      	}

      	
    }

    /**
     * Metoda wywoływana przez system Android przed zniszczeniem aktywności, służy do zapamiętywania stanu aktywności. Tu do usuwania okien dialogowych z ekranu gdy aplikacja ma nie zapamiętywać hasła w sesji. 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState (Bundle outState){	//http://stackoverflow.com/questions/11368863/why-wont-dismissdialog-removedialog-or-dialog-dismiss-work-in-ondestroy-or-on
    	 if (rememberAppPasswordInSesionboolean == false){
	       	  removeDialog(DIALOG_ADD_NEW_SERVER_CRYPTO_ENABLED);
	       	  removeDialog(DIALOG_DELETE_SERVER);
	       	  removeDialog(DIALOG_CHOOSE_SERVER_TO_EDIT);
	       	  removeDialog(DIALOG_EDIT_SERVER_CRYPTO_ENABLED);
	       	  removeDialog(DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE);
	       	  removeDialog(DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST);
	       	  removeDialog(DIALOG_GIVE_ME_A_SERVER_PASSWORD);
       	 }
		outState.putInt("serverToEditint",serverToEditint);
		outState.putInt("serverToDelete",serverToDelete);
    }
    
    /**
     * Metoda wywoływana przez system Android, kiedy aktywność przestaje być wyświetlana na ekranie urządzenia. Odpowiada za zapisanie listy serwerów w pamięci urządzenia.
     * Kończy ona wyświetlanie okien dialogowych <code>dialog_FIRST_TIME_RUNING</code> i <code>dialog_GIVE_ME_A_APP_PASSWORD</code>.  
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause(){
      super.onPause();
      if(dialog_FIRST_TIME_RUNING != null && dialog_FIRST_TIME_RUNING.isShowing()) {
    	  dialog_FIRST_TIME_RUNING.dismiss();
      }
      
      if(dialog_GIVE_ME_A_APP_PASSWORD != null && dialog_GIVE_ME_A_APP_PASSWORD.isShowing()) {
    	  dialog_GIVE_ME_A_APP_PASSWORD.dismiss();
      }
     
      if (isCryptoEnabledboolean == false){
    	  appPasswordcharArray = "default_password".toCharArray();
    	  aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListArrayList, appPasswordcharArray);
      }else if (appPasswordcharArray != null && appPasswordcharArray != "default_password".toCharArray()){

	    	  aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListArrayList, appPasswordcharArray);
	      
      }
      
    }
    /**
     * Metoda wywoływana przez system Android przy wznawianiu wyświetlania aktywności. 
     * Tu odpowiadająca za ponowne wyświetlenie okien dialogowych <code>dialog_FIRST_TIME_RUNING</code> i <code>dialog_GIVE_ME_A_APP_PASSWORD</code> po restarcie aktywności.
     * @see android.app.Activity#onResume()
     */
    @Override
    public void onResume() {
    	super.onResume();
    	if(mStateHolder.mIsShowingDialog_FIRST_TIME_RUNING) {
    		dialog_FIRST_TIME_RUNING.show();
    	}
    	if(mStateHolder.mIsShowingDialog_GIVE_ME_A_PASSWORD) {
    		dialog_GIVE_ME_A_APP_PASSWORD.show();
    	}
    }

	@Override
	protected void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("dismissconnectingToSshProgressDialog"));
	}

	@Override
	protected void onStop() {
		super.onStop();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	}

	/**
     * Metoda odpowiedzialna za tworzenie okien dialogowych wyświetlanych przez aktywność.
     * @see android.app.Activity#onCreateDialog(int, android.os.Bundle)
     */
    protected Dialog onCreateDialog(int id, final Bundle retrievedBundle) {
    
    		// przypisanie kontekstu do dialog
        final Context mContext = this; // ważne w oficjalnej dokumentacji jest błąd
   	 	Dialog dialog = new Dialog (mContext);
   	 	dialog_FIRST_TIME_RUNING = new Dialog(mContext);
   	 	dialog_GIVE_ME_A_APP_PASSWORD = new Dialog(mContext);
   	 	dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOILEAN_IS_FALSE = new Dialog(mContext);
   	 	dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST = new Dialog(mContext);
   	 	dialog_GIVE_ME_A_SERVER_PASSWORD = new Dialog(mContext);
        dialog_ADD_NEW_SERVER_CRYPTO_ENABLED = new Dialog(mContext);
        dialog_ADD_NEW_SERVER_CRYPTO_DISABLED = new Dialog(mContext);
   	 	dialog_DELETE_SERVER = new Dialog(mContext);
    	dialog_CHOSE_SERVER_TO_EDIT = new Dialog(mContext);
    	dialog_EDIT_SERVER_CRYPTO_ENABLED = new Dialog(mContext);
    	dialog_EDIT_SERVER_CRYPTO_DISABLED = new Dialog(mContext);
    	dialog_DO_DELATE = new Dialog(mContext);
    	dialog_LICENSE = new Dialog(mContext);
    	
    	switch(id) {
    	case DIALOG_FIRST_TIME_RUNING:
    		
    		//dialog_FIRST_TIME_RUNING.requestWindowFeature(Window.FEATURE_NO_TITLE);
    		dialog_FIRST_TIME_RUNING.setContentView(R.layout.layout_for_dialog_first_time_runing);
			dialog_FIRST_TIME_RUNING.setTitle(R.string.tile_for_dialog_FIRST_TIME_RUNING);
    		dialog_FIRST_TIME_RUNING.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
					
				}
			});
    		
    		if (appPasswordcharArray != null){	//appPasswordcharArray == null on first start for example
	    		try {
	    			serverListArrayList = aXMLReaderWriter.decryptFileWithXMLAndParseItToServerList(appPasswordcharArray);
	    			
	    		} catch (WrongPasswordException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
    		}
    		
    		final EditText set_app_passwordEditText = (EditText) dialog_FIRST_TIME_RUNING.findViewById(R.id.set_app_passswordEditText);
    		set_app_passwordEditText.setOnKeyListener(new OnKeyListener() {
			    public boolean onKey(View v, int keyCode, KeyEvent event) {
			        // If the event is a key-down event on the "enter" button
			        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
			        	// Perform action on key press
			        	return true;
			        }
			        return false;
			    }
			});
    		
    		final TextView explanation_set_a_password_for_this_appTextView = (TextView) dialog_FIRST_TIME_RUNING.findViewById(R.id.explanation_set_a_password_for_this_app);
    		final ColorStateList explanation_set_a_password_for_this_appTextViewColorStateList = explanation_set_a_password_for_this_appTextView.getTextColors();
    		final CheckBox use_encryption_checkBox = (CheckBox) dialog_FIRST_TIME_RUNING.findViewById(R.id.use_encryption_checkBox);
    		use_encryption_checkBox.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					if (use_encryption_checkBox.isChecked() == true){
						set_app_passwordEditText.setVisibility(View.VISIBLE);
						explanation_set_a_password_for_this_appTextView.setVisibility(View.VISIBLE);
						/*
						//explanation_set_a_password_for_this_appTextView.setTextColor(explanation_set_a_password_for_this_appTextViewColorStateList);
						set_app_passwordEditText.setClickable(true);
						set_app_passwordEditText.setFocusable(true);
						set_app_passwordEditText.setFocusableInTouchMode(true);
						set_app_passwordEditText.setCursorVisible(true);
						set_app_passwordEditText.setLongClickable(true);
						set_app_passwordEditText.setBackgroundResource(android.R.drawable.edit_text);
						set_app_passwordEditText.setTextColor(android.graphics.Color.BLACK);
						*/
					}else{
						set_app_passwordEditText.setVisibility(View.INVISIBLE);
						explanation_set_a_password_for_this_appTextView.setVisibility(View.INVISIBLE);
						/*
						//explanation_set_a_password_for_this_appTextView.setTextColor(0);
						set_app_passwordEditText.setClickable(false);
						set_app_passwordEditText.setFocusable(false);
						set_app_passwordEditText.setFocusableInTouchMode(false);
						set_app_passwordEditText.setCursorVisible(false);
						set_app_passwordEditText.setLongClickable(false);
						set_app_passwordEditText.setBackgroundColor(android.graphics.Color.GRAY);
						set_app_passwordEditText.setTextColor(android.graphics.Color.GRAY);
						*/
					}
					
				}
    			
    		});
    		
    		final Button exit_dialog_first_time_runing_button = (Button) dialog_FIRST_TIME_RUNING.findViewById(R.id.exit_dialog_first_time_runing_button);
    		exit_dialog_first_time_runing_button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					if (set_app_passwordEditText.getText().length() == 0 && use_encryption_checkBox.isChecked() == true){
						Toast.makeText(getApplicationContext(), R.string.text_for_toast_fill_up_the_empty_spaces, Toast.LENGTH_LONG).show();
						
				    }else{
				        
						if (use_encryption_checkBox.isChecked() == true){
							isCryptoEnabledboolean = true;
						}else{
							isCryptoEnabledboolean = false;
						}
						
						SharedPreferences settings_for_APP = getSharedPreferences("settings_for_APP", 0);
					    SharedPreferences.Editor editor = settings_for_APP.edit();
					    editor.putBoolean("is_this_first_run", false);
					    editor.putBoolean("is_crypto_enabled", isCryptoEnabledboolean);
					    	// Commit the edits!
					    editor.commit();
				    	
					    	//a new salt should be created for every new app passwort. Watch a XMLReaderWriter.createKey and SettingsForAPP.
					    File file = mContext.getFileStreamPath("salt");
					    if (file.exists()){
					    	file.delete();	//Usuwanie salt dla poprzedniego hasła aplikacji.
					    	Log.v(TAG, "Usuwam stary salt");
					    }
					    
					    
					    if(isCryptoEnabledboolean == true){
					    	appPasswordcharArray = set_app_passwordEditText.getText().toString().toCharArray();
					    	aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListArrayList, appPasswordcharArray);
				        }else{
				        	appPasswordcharArray = "default_password".toCharArray();
				        	aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListArrayList, appPasswordcharArray);
				        }
					    if (serverListArrayList != null){ 
					 	       for ( int i = 0; i < serverListArrayList.size(); i++){

								   createConnectButtons(i);
					 	        	
					 	       }
					    }
					    dismissdialog_FIRST_TIME_RUNING();
				    }
				}
			});
    		break;
        case DIALOG_GIVE_ME_A_APP_PASSWORD:

        	dialog_GIVE_ME_A_APP_PASSWORD.setContentView(R.layout.layout_for_dialog_give_me_a_app_password);
			dialog_GIVE_ME_A_APP_PASSWORD.setTitle(R.string.title_for_dialog_GIVE_ME_A_APP_PASSWORD);
        	dialog_GIVE_ME_A_APP_PASSWORD.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
					
				}
			});
        	final Button check_app_passwordButton = (Button) dialog_GIVE_ME_A_APP_PASSWORD.findViewById(R.id.check_app_password_Button);
        	check_app_passwordButton.setOnClickListener(new OnClickListener() {
     			
     			@Override
     			public void onClick(View v) {
     				
     				EditText app_password_EditText = (EditText) dialog_GIVE_ME_A_APP_PASSWORD.findViewById(R.id.app_password_EditText);
     				if (app_password_EditText.getText().length() == 0){
     	            	Toast.makeText(getApplicationContext(), R.string.text_for_toast_fill_up_the_empty_spaces, Toast.LENGTH_LONG).show();
				    }else{
     					app_password_EditText.setOnKeyListener(new OnKeyListener() {
	     				    public boolean onKey(View v, int keyCode, KeyEvent event) {
	     				        // If the event is a key-down event on the "enter" button
	     				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
	     				        	// Perform action on key press
	     				        	return true;
	     				        }
	     				        return false;
	     				    }
	     				});
	     				appPasswordcharArray = (app_password_EditText.getText().toString()).toCharArray();
	     				
	     				
	     				try {
							serverListArrayList = aXMLReaderWriter.decryptFileWithXMLAndParseItToServerList(appPasswordcharArray);
							
					        if (serverListArrayList != null){ 
					 	       for ( int i = 0; i < serverListArrayList.size(); i++){

								   createConnectButtons(i);
					 	        	
					 	       }
					         }
					        final CheckBox remember_app_password_in_sesion_CheckBox = (CheckBox) dialog_GIVE_ME_A_APP_PASSWORD.findViewById(R.id.remember_app_password_in_sesion_CheckBox);
		     	        	if (remember_app_password_in_sesion_CheckBox.isChecked() == true){
		     					rememberAppPasswordInSesionboolean = true;
		     					SharedPreferences settings_for_APP = getSharedPreferences("settings_for_APP", 0);
		    				    SharedPreferences.Editor editor = settings_for_APP.edit();
		    				    editor.putBoolean("remember_app_password_in_sesion_boolean", rememberAppPasswordInSesionboolean);
		    				    	// Commit the edits!
		    				    editor.commit();
		     				}else{
		     					Arrays.fill(appPasswordcharArray,'0');
		     					appPasswordcharArray = null;
		     					rememberAppPasswordInSesionboolean = false;
		     					SharedPreferences settings_for_APP = getSharedPreferences("settings_for_APP", 0);
		    				    SharedPreferences.Editor editor = settings_for_APP.edit();
		    				    editor.putBoolean("remember_app_password_in_sesion_boolean", rememberAppPasswordInSesionboolean);
		    				    	// Commit the edits!
		    				    editor.commit();
		     				}
					        dismissdialog_GIVE_ME_A_APP_PASSWORD();	
						} catch (WrongPasswordException e) {
							appPasswordcharArray = null;
							Toast.makeText(getApplicationContext(), R.string.wrong_app_password_exeption, Toast.LENGTH_SHORT).show();
							showdialog_GIVE_ME_A_APP_PASSWORD();
						}
	     				
	     				
				    }	
				}
        	});
        	
        	break;
        		//called in dialogs DIALOG_ADD_NEW_SERVER... and DIALOG_EDIT_SERVER.. 
        case DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE:
        	dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOILEAN_IS_FALSE.setContentView(R.layout.layout_for_dialog__because_remember_app_password_in_sesion_boolean_is_false);
        	dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOILEAN_IS_FALSE.setTitle(R.string.title_for_dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOILEAN_IS_FALSE);
        	Button continue_with_given_app_password_Button = (Button) dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOILEAN_IS_FALSE.findViewById(R.id.continue_with_given_app_password_Button);
        	continue_with_given_app_password_Button.setOnClickListener(new OnClickListener() {
     			
     			@Override
     			public void onClick(View v) {
     				     				
     				EditText app_password_EditText = (EditText) dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOILEAN_IS_FALSE.findViewById(R.id.app_password_EditText_in_layout_for_dialog__because_remember_app_password_in_sesion_boolean_is_false);
     				if (app_password_EditText.getText().length() == 0){
     	            	Toast.makeText(getApplicationContext(), R.string.text_for_toast_fill_up_the_empty_spaces, Toast.LENGTH_LONG).show();
				    }else{
	     				app_password_EditText.setOnKeyListener(new OnKeyListener() {
	     				    public boolean onKey(View v, int keyCode, KeyEvent event) {
	     				        // If the event is a key-down event on the "enter" button
	     				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
	     				        	// Perform action on key press
	     				        	return true;
	     				        }
	     				        return false;
	     				    }
	     				});
	     				Log.v(TAG, "app_password przez odczytaniem z app_password_EditText w: DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE" + appPasswordcharArray);
	     				appPasswordcharArray = (app_password_EditText.getText().toString()).toCharArray();
	     				Log.v(TAG, "app_password po odczytaniu z app_password_EditText w: DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE" + appPasswordcharArray.toString());
	     				try {
	     					List<Server> test_input_server_list =  new ArrayList<Server>();
	     					test_input_server_list = aXMLReaderWriter.decryptFileWithXMLAndParseItToServerList(appPasswordcharArray);	//catch if password is wrong
	     					aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListArrayList, appPasswordcharArray);
	         				//Log.v(TAG,server.getServer_name());
	        				//Log.v(TAG,server.getIP_address());
	        				//Log.v(TAG,server.getUsername());
	        				//Log.v(TAG,new String(server.getPassword())); 
	         	           
	        				//removeDialog(DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE);
	         				Arrays.fill(appPasswordcharArray,'0');
	         				appPasswordcharArray = null;
	        				finish();
	         				Intent intent = new Intent(mContext, ServerList.class);
	         	        	startActivity(intent);
	     				} catch (WrongPasswordException e) {
							appPasswordcharArray = null;
							Toast.makeText(getApplicationContext(), R.string.wrong_app_password_exeption, Toast.LENGTH_SHORT).show();
							//showDialog(DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE);
						}
				    }	
     			}
        	});
        	break;
        	
        case DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST:
        	dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST.setContentView(R.layout.layout_for_dialog__because_remember_app_password_in_sesion_boolean_is_false);
        	dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST.setTitle(R.string.title_for_dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOILEAN_IS_FALSE);
        	Button continue_with_given_app_password_Button2 = (Button) dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST.findViewById(R.id.continue_with_given_app_password_Button);
        	continue_with_given_app_password_Button2.setOnClickListener(new OnClickListener() {
     			
     			@Override
     			public void onClick(View v) {
     				     				
     				EditText app_password_EditText = (EditText) dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST.findViewById(R.id.app_password_EditText_in_layout_for_dialog__because_remember_app_password_in_sesion_boolean_is_false);
     				if (app_password_EditText.getText().length() == 0){
     	            	Toast.makeText(getApplicationContext(), R.string.text_for_toast_fill_up_the_empty_spaces, Toast.LENGTH_LONG).show();
				    }else{
	     				app_password_EditText.setOnKeyListener(new OnKeyListener() {
	     				    public boolean onKey(View v, int keyCode, KeyEvent event) {
	     				        // If the event is a key-down event on the "enter" button
	     				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
	     				        	// Perform action on key press
	     				        	return true;
	     				        }
	     				        return false;
	     				    }
	     				});
	     				Log.v(TAG, "app_password przez odczytaniem z app_password_EditText w: DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST" + appPasswordcharArray);
	     				appPasswordcharArray = (app_password_EditText.getText().toString()).toCharArray();
	     				Log.v(TAG, "app_password po odczytaniu z app_password_EditText w: DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST" + appPasswordcharArray.toString());
	     				try {
	     					List<Server> test_input_server_list =  new ArrayList<Server>();
	     					test_input_server_list = aXMLReaderWriter.decryptFileWithXMLAndParseItToServerList(appPasswordcharArray); //catch if password is wrong
	     					aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListArrayList, appPasswordcharArray);
	     		        	
	     					final Intent intent_start_settings_activity_for_ServerList = new Intent(getApplicationContext(), SettingsForAPP.class);
	     		        	intent_start_settings_activity_for_ServerList.putExtra("app_password", appPasswordcharArray);		
	     		        	startActivity(intent_start_settings_activity_for_ServerList);
	     		        	finish();
	         	           
	        				//removeDialog(DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE);
	         				Arrays.fill(appPasswordcharArray,'0');
	         				appPasswordcharArray = null;
	        				
	     				} catch (WrongPasswordException e) {
							appPasswordcharArray = null;
							Toast.makeText(getApplicationContext(), R.string.wrong_app_password_exeption, Toast.LENGTH_SHORT).show();
							//showDialog(DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE);
						}
				    }	
     			}
        	});
        	break;
        	
        case DIALOG_GIVE_ME_A_SERVER_PASSWORD:
        	
        	dialog_GIVE_ME_A_SERVER_PASSWORD.setContentView(R.layout.layout_for_dialog_give_me_a_server_password);
			dialog_GIVE_ME_A_SERVER_PASSWORD.setTitle(R.string.title_for_dialog_GIVE_ME_A_SERVER_PASSWORD);

        	final Button connect_to_server_button_in_DIALOG_GIVE_ME_A_SERVER_PASSWORD = (Button) dialog_GIVE_ME_A_SERVER_PASSWORD.findViewById(R.id.connect_to_server_Button_in_dialog_give_me_a_server_password);
        	connect_to_server_button_in_DIALOG_GIVE_ME_A_SERVER_PASSWORD.setOnClickListener(new OnClickListener() {
     			
     			@Override
     			public void onClick(View v) {
     				     				
     				EditText server_password_EditText = (EditText) dialog_GIVE_ME_A_SERVER_PASSWORD.findViewById(R.id.server_password_EditText);
     				if (server_password_EditText.getText().length() == 0){
     	            	Toast.makeText(getApplicationContext(), R.string.text_for_toast_fill_up_the_empty_spaces, Toast.LENGTH_LONG).show();
				    }else{
	     				server_password_EditText.setOnKeyListener(new OnKeyListener() {
	     				    public boolean onKey(View v, int keyCode, KeyEvent event) {
	     				        // If the event is a key-down event on the "enter" button
	     				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
	     				        	// Perform action on key press
	     				        	return true;
	     				        }
	     				        return false;
	     				    }
	     				});
	     				char[] server_password = (server_password_EditText.getText().toString().toCharArray());
	     				Log.v(TAG, "server_password przeczytane z server_password_EditText: " + new String(server_password));
	     				int id_of_clicked_button = retrievedBundle.getInt("clicked_button");
	     				Log.v(TAG, "id of clicked button: " + id_of_clicked_button);
	     				Intent intent_start_ConnectToServer = new Intent(getApplicationContext(), ConnectToServer.class);
						final Intent intent_start_ConnectAndPlayService = new Intent(getApplicationContext(), ConnectAndPlayService.class);
						intent_start_ConnectAndPlayService.putExtra("server_name", serverListArrayList.get(id_of_clicked_button).getServerName());
						intent_start_ConnectAndPlayService.putExtra("IP_address", serverListArrayList.get(id_of_clicked_button).getIPAddress());
						intent_start_ConnectAndPlayService.putExtra("username", serverListArrayList.get(id_of_clicked_button).getUsername());
						intent_start_ConnectAndPlayService.putExtra("password", server_password);
	     				startService(intent_start_ConnectAndPlayService);

						connectingToSshProgressDialog = ProgressDialog.show(ServerList.this, "",getString(R.string.text_for_progressdialog_from_connecttoserver), true, true);

	     				removeDialog(DIALOG_GIVE_ME_A_SERVER_PASSWORD);
	     				Arrays.fill(server_password, '0');
				    }	
     			}
        	});
        	break;
        case DIALOG_ADD_NEW_SERVER_CRYPTO_ENABLED:
        	dialog_ADD_NEW_SERVER_CRYPTO_ENABLED.setContentView(R.layout.layout_for_dialog_add_new_server_crypto_enabled);
        	dialog_ADD_NEW_SERVER_CRYPTO_ENABLED.setTitle(R.string.title_for_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED);
        	
        		//Buttons
        	final Button saveButton_in_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED = (Button) dialog_ADD_NEW_SERVER_CRYPTO_ENABLED.findViewById(R.id.saveButton_crypto_enabled);
        	final Button cancelButton_in_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED = (Button) dialog_ADD_NEW_SERVER_CRYPTO_ENABLED.findViewById(R.id.cancelButton_crypto_enabled);
        	saveButton_in_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED.setOnClickListener(new OnClickListener() {
     			
     			@Override
     			public void onClick(View v) {
     				     				
     				EditText server_nameEditText = (EditText) dialog_ADD_NEW_SERVER_CRYPTO_ENABLED.findViewById(R.id.server_nameEditText_crypto_enabled);
     				server_nameEditText.setOnKeyListener(new OnKeyListener() {
     				    public boolean onKey(View v, int keyCode, KeyEvent event) {
     				        // If the event is a key-down event on the "enter" button
     				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
     				        	// Perform action on key press
     				        	return true;
     				        }
     				        return false;
     				    }
     				});
     				EditText IP_addressEditText = (EditText) dialog_ADD_NEW_SERVER_CRYPTO_ENABLED.findViewById(R.id.IP_addressEditText_crypto_enabled);
     				IP_addressEditText.setOnKeyListener(new OnKeyListener() {
     				    public boolean onKey(View v, int keyCode, KeyEvent event) {
     				        // If the event is a key-down event on the "enter" button
     				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
     				        	// Perform action on key press
     				        	return true;
     				        }
     				        return false;
     				    }
     				});
     				EditText usernameEditText = (EditText) dialog_ADD_NEW_SERVER_CRYPTO_ENABLED.findViewById(R.id.usernameEditText_crypto_enabled);
     				usernameEditText.setOnKeyListener(new OnKeyListener() {
     				    public boolean onKey(View v, int keyCode, KeyEvent event) {
     				        // If the event is a key-down event on the "enter" button
     				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
     				        	// Perform action on key press
     				        	return true;
     				        }
     				        return false;
     				    }
     				});
     				EditText passwordEditText = (EditText) dialog_ADD_NEW_SERVER_CRYPTO_ENABLED.findViewById(R.id.passwordEditText_crypto_enabled);
	     			passwordEditText.setOnKeyListener(new OnKeyListener() {
     				    public boolean onKey(View v, int keyCode, KeyEvent event) {
     				        // If the event is a key-down event on the "enter" button
     				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
     				        	// Perform action on key press
     				        	return true;
     				        }
     				        return false;
     				    }
     				});
     				
     	            Log.v(TAG,"obecna ilosc zapisanych serverow wynosi: " + serverListArrayList.size());
     	        	
     	            if (server_nameEditText.getText().length() == 0 || IP_addressEditText.getText().length() == 0 || usernameEditText.getText().length() == 0 || passwordEditText.getText().length() == 0){
     	            	Toast.makeText(getApplicationContext(), R.string.text_for_toast_fill_up_the_empty_spaces, Toast.LENGTH_LONG).show();
     	            //}else if(!validateIP(IP_addressEditText.getText().toString())){
     	            	//Toast.makeText(getApplicationContext(), R.string.text_for_toast_correct_IP_address, Toast.LENGTH_LONG).show();
     	            //}else if (server_nameEditText.getText().toString().matches(".*\\s+.*") || IP_addressEditText.getText().toString().matches(".*\\s+.*") || usernameEditText.getText().toString().matches(".*\\s+.*") || passwordEditText.getText().toString().matches(".*\\s+.*")){	
     	            	//Toast.makeText(getApplicationContext(), R.string.text_for_toast_fields_should_not_contain_a_whitespace_character, Toast.LENGTH_LONG).show();
     	            }else if (!(isIPv4OrIPv6(IP_addressEditText.getText().toString()))){
    	            	Toast.makeText(getApplicationContext(), R.string.text_for_toast_correct_IP_address, Toast.LENGTH_LONG).show();
    	            
     	            }else{
	     	            Server server = new Server();
	     	            server.setServerName(server_nameEditText.getText().toString());
	     	            server.setIPAddress(IP_addressEditText.getText().toString());
	     	            server.setUsername(usernameEditText.getText().toString());
	     	            server.setPassword(passwordEditText.getText().toString().toCharArray());  
	     	                 	                 	           	
						serverListArrayList.add(server);
						if (appPasswordcharArray == null){
							showDialog(DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE);
							//a_XMLReaderWrriter.create_encrypted_XMLFile_with_server_list(server_list, app_password);	//sprawdzić czy będzie działać bez tego że niby w onPause() wystarczy
							removeDialog(DIALOG_ADD_NEW_SERVER_CRYPTO_ENABLED);
							
						}else{	
		    				Log.v(TAG,server.getServerName());
		    				Log.v(TAG,server.getIPAddress());
		    				Log.v(TAG,server.getUsername());
		    				Log.v(TAG,new String(server.getPassword())); 
		     	           
		    				removeDialog(DIALOG_ADD_NEW_SERVER_CRYPTO_ENABLED);
		    				finish();
		     				Intent intent = new Intent(mContext, ServerList.class);
		     	        	startActivity(intent);
						}
     	            }     				
     	            
     	           	
     	        	
     			}
        		
     		});
        	
        	cancelButton_in_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					
					removeDialog(DIALOG_ADD_NEW_SERVER_CRYPTO_ENABLED);
				}
        		
        	});
			dialog_ADD_NEW_SERVER_CRYPTO_ENABLED.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					
				}
			});
            break;
        case DIALOG_ADD_NEW_SERVER_CRYPTO_DISABLED:
        	dialog_ADD_NEW_SERVER_CRYPTO_DISABLED.setContentView(R.layout.layout_for_dialog_add_new_server_crypto_disabled);
        	dialog_ADD_NEW_SERVER_CRYPTO_DISABLED.setTitle(R.string.title_for_dialog_ADD_NEW_SERVER_CRYPTO_ENABLED);	//title is the same
        	
        		//Buttons
        	final Button saveButton_in_dialog_ADD_NEW_SERVER_CRYPTO_DISABLED = (Button) dialog_ADD_NEW_SERVER_CRYPTO_DISABLED.findViewById(R.id.saveButton_crypto_disabled);
        	final Button cancelButton_in_dialog_ADD_NEW_SERVER_CRYPTO_DISABLED = (Button) dialog_ADD_NEW_SERVER_CRYPTO_DISABLED.findViewById(R.id.cancelButton_crypto_disabled);
        	saveButton_in_dialog_ADD_NEW_SERVER_CRYPTO_DISABLED.setOnClickListener(new OnClickListener() {
     			
     			@Override
     			public void onClick(View v) {
     				     				
     				EditText server_nameEditText = (EditText) dialog_ADD_NEW_SERVER_CRYPTO_DISABLED.findViewById(R.id.server_nameEditText_crypto_disabled);
     				server_nameEditText.setOnKeyListener(new OnKeyListener() {
     				    public boolean onKey(View v, int keyCode, KeyEvent event) {
     				        // If the event is a key-down event on the "enter" button
     				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
     				        	// Perform action on key press
     				        	return true;
     				        }
     				        return false;
     				    }
     				});
     				EditText IP_addressEditText = (EditText) dialog_ADD_NEW_SERVER_CRYPTO_DISABLED.findViewById(R.id.IP_addressEditText_crypto_disabled);
     				IP_addressEditText.setOnKeyListener(new OnKeyListener() {
     				    public boolean onKey(View v, int keyCode, KeyEvent event) {
     				        // If the event is a key-down event on the "enter" button
     				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
     				        	// Perform action on key press
     				        	return true;
     				        }
     				        return false;
     				    }
     				});
     				EditText usernameEditText = (EditText) dialog_ADD_NEW_SERVER_CRYPTO_DISABLED.findViewById(R.id.usernameEditText_crypto_disabled);
     				usernameEditText.setOnKeyListener(new OnKeyListener() {
     				    public boolean onKey(View v, int keyCode, KeyEvent event) {
     				        // If the event is a key-down event on the "enter" button
     				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
     				        	// Perform action on key press
     				        	return true;
     				        }
     				        return false;
     				    }
     				});
     				
     	            Log.v(TAG,"obecna ilosc zapisanych serverow wynosi: " + serverListArrayList.size());
     	            if (server_nameEditText.getText().length() == 0 || IP_addressEditText.getText().length() == 0 || usernameEditText.getText().length() == 0 ){
     	            	Toast.makeText(getApplicationContext(), R.string.text_for_toast_fill_up_the_empty_spaces, Toast.LENGTH_LONG).show();
     	            //}else if(!validateIP(IP_addressEditText.getText().toString())){
     	            	//Toast.makeText(getApplicationContext(), R.string.text_for_toast_correct_IP_address, Toast.LENGTH_LONG).show();
     	            //}else if (server_nameEditText.getText().toString().matches(".*\\s+.*") || IP_addressEditText.getText().toString().matches(".*\\s+.*") || usernameEditText.getText().toString().matches(".*\\s+.*") || passwordEditText.getText().toString().matches(".*\\s+.*")){	
     	            	//Toast.makeText(getApplicationContext(), R.string.text_for_toast_fields_should_not_contain_a_whitespace_character, Toast.LENGTH_LONG).show();
     	            }else if (!(isIPv4OrIPv6(IP_addressEditText.getText().toString()))){
    	            	Toast.makeText(getApplicationContext(), R.string.text_for_toast_correct_IP_address, Toast.LENGTH_LONG).show();
    	            
     	            }else{	   	           
	     	            Server server = new Server();
	     	           
	     	            server.setServerName(server_nameEditText.getText().toString());
	     	            server.setIPAddress(IP_addressEditText.getText().toString());
	     	            server.setUsername(usernameEditText.getText().toString());
	     	            server.setPassword("a_blank_password".toCharArray());
	     	                	           	
						serverListArrayList.add(server);
						
						//a_XMLReaderWrriter.create_encrypted_XMLFile_with_server_list(server_list, app_password);
						
	    				Log.v(TAG,server.getServerName());
	    				Log.v(TAG,server.getIPAddress());
	    				Log.v(TAG,server.getUsername());
	    				Log.v(TAG,new String(server.getPassword())); 
	     	           
	    				removeDialog(DIALOG_ADD_NEW_SERVER_CRYPTO_DISABLED);
	    				
	    				finish();
	     				Intent intent = new Intent(mContext, ServerList.class);
	     	        	startActivity(intent);
    	            }
     	        }
    		});
        	
        	cancelButton_in_dialog_ADD_NEW_SERVER_CRYPTO_DISABLED.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					
					removeDialog(DIALOG_ADD_NEW_SERVER_CRYPTO_DISABLED);
				}
        		
        	});

            break;

        
        case DIALOG_CHOOSE_SERVER_TO_EDIT:
        	Log.v(TAG,"Wszedłem do onCreate DIALOG_CHOOSE_SERVER_TO_EDIT");
        	itemsFor_DIALOG_EDIT_SERVER = new CharSequence[serverListArrayList.size()];
    		for (int i = 0; i < serverListArrayList.size(); i++){
    			
    			itemsFor_DIALOG_EDIT_SERVER[i] = serverListArrayList.get(i).getServerName();
    			Log.v(TAG,"Server_name :" + itemsFor_DIALOG_EDIT_SERVER[i]);
    		}
    		AlertDialog.Builder builder_for_DIALOG_CHOSE_SERVER_TO_EDIT = new AlertDialog.Builder(this);
    		builder_for_DIALOG_CHOSE_SERVER_TO_EDIT.setTitle(R.string.title_for_dialog_CHOSE_SERVER_TO_EDIT);
    		builder_for_DIALOG_CHOSE_SERVER_TO_EDIT.setItems(itemsFor_DIALOG_EDIT_SERVER, new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog_CHOSE_SERVER_TO_EDIT, int item) {
        	        //Toast.makeText(getApplicationContext(), items_for_DIALOG_EDIT_SERVER[item], Toast.LENGTH_SHORT).show();
        	        serverToEditint = item;
        	        Log.v(TAG,"server do edycji ma numer: " + item);
        	        removeDialog(DIALOG_CHOOSE_SERVER_TO_EDIT);
        	        if (isCryptoEnabledboolean == true){
        	        	showDialog(DIALOG_EDIT_SERVER_CRYPTO_ENABLED);	
        	        }else{
        	        	showDialog(DIALOG_EDIT_SERVER_CRYPTO_DISABLED);
        	        }
        	    }
        	});
    		dialog_CHOSE_SERVER_TO_EDIT = builder_for_DIALOG_CHOSE_SERVER_TO_EDIT.create();
    		
    		
            break; 
        case DIALOG_EDIT_SERVER_CRYPTO_ENABLED:
        	
        	dialog_EDIT_SERVER_CRYPTO_ENABLED.setContentView(R.layout.layout_for_dialog_edit_server_crypto_enabled);
        	dialog_EDIT_SERVER_CRYPTO_ENABLED.setTitle(R.string.title_for_dialog_EDIT_SERVER_CRYPTO_ENABLED);
        	
        		//Buttons
        	final Button saveButton_from_DIALOG_EDIT_SERVER = (Button) dialog_EDIT_SERVER_CRYPTO_ENABLED.findViewById(R.id.saveButton_in_dialog_edit_server_crypto_enabled);
        	final Button cancelButton_from_DIALOG_EDIT_SERVER = (Button) dialog_EDIT_SERVER_CRYPTO_ENABLED.findViewById(R.id.cancelButton_in_dialog_edit_server_crypto_enabled);
        	
        	final EditText server_nameEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED = (EditText) dialog_EDIT_SERVER_CRYPTO_ENABLED.findViewById(R.id.server_name_in_dialog_edit_server_EditText_crypto_enabled_from_edit_server);
        	server_nameEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.setText(serverListArrayList.get(serverToEditint).getServerName());
        	server_nameEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.setOnKeyListener(new OnKeyListener() {
				    public boolean onKey(View v, int keyCode, KeyEvent event) {
				        // If the event is a key-down event on the "enter" button
				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
				        	// Perform action on key press
				        	return true;
				        }
				        return false;
				    }
			});
			final EditText IP_addressEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED = (EditText) dialog_EDIT_SERVER_CRYPTO_ENABLED.findViewById(R.id.IP_address_in_dialog_EditText_crypto_enabled_from_edit_server);
			IP_addressEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.setText(serverListArrayList.get(serverToEditint).getIPAddress());
			IP_addressEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.setOnKeyListener(new OnKeyListener() {
				    public boolean onKey(View v, int keyCode, KeyEvent event) {
				        // If the event is a key-down event on the "enter" button
				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
				        	// Perform action on key press
				        	return true;
				        }
				        return false;
				    }
			});
			final EditText usernameEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED = (EditText) dialog_EDIT_SERVER_CRYPTO_ENABLED.findViewById(R.id.username_in_dialog_edit_server_EditText_crypto_enabled_from_edit_server);
			usernameEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.setText(serverListArrayList.get(serverToEditint).getUsername());
			usernameEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.setOnKeyListener(new OnKeyListener() {
				    public boolean onKey(View v, int keyCode, KeyEvent event) {
				        // If the event is a key-down event on the "enter" button
				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
				        	// Perform action on key press
				        	return true;
				        }
				        return false;
				    }
			});
			
			
			final EditText passwordEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED = (EditText) dialog_EDIT_SERVER_CRYPTO_ENABLED.findViewById(R.id.password_in_dialog_edit_server_EditText_crypto_enabled_from_edit_server);
			passwordEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.setText(new String(serverListArrayList.get(serverToEditint).getPassword()));
			passwordEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.setOnKeyListener(new OnKeyListener() {
				    public boolean onKey(View v, int keyCode, KeyEvent event) {
				        // If the event is a key-down event on the "enter" button
				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
				        	// Perform action on key press
				        	return true;
				        }
				        return false;
				    }
			});
			
        	
        	saveButton_from_DIALOG_EDIT_SERVER.setOnClickListener(new OnClickListener() {
     			
     			@Override
     			public void onClick(View v) {
  				
     				
     	            Log.v(TAG,"obecna ilosc zapisanych serverow wynosi: " + serverListArrayList.size());
     	            if (server_nameEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.getText().length() == 0 || IP_addressEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.getText().length() == 0 || usernameEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.getText().length() == 0 || passwordEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.getText().length() == 0){
    	            	Toast.makeText(getApplicationContext(), R.string.text_for_toast_fill_up_the_empty_spaces, Toast.LENGTH_LONG).show();
     	            }else if (!(isIPv4OrIPv6(IP_addressEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.getText().toString()))){
    	            	Toast.makeText(getApplicationContext(), R.string.text_for_toast_correct_IP_address, Toast.LENGTH_LONG).show();
    	            
     	            }else{   	           
	     	            Server server = new Server();
	     	           
	     	            server.setServerName(server_nameEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.getText().toString());
	     	            server.setIPAddress(IP_addressEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.getText().toString());
	     	            server.setUsername(usernameEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.getText().toString());
	     	            server.setPassword(passwordEditText_in_dialog_EDIT_SERVER_CRYPTO_ENABLED.getText().toString().toCharArray());  //server_nameEditText.getText().toString() to nazwa pliku
			                 	                 	            
	     	            serverListArrayList.set(serverToEditint, server);
	     	            if (appPasswordcharArray == null){
							showDialog(DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE);
							//a_XMLReaderWrriter.create_encrypted_XMLFile_with_server_list(server_list, app_password);	//sprawdzić czy będzie działać bez tego że niby w onPause() wystarczy
							removeDialog(DIALOG_EDIT_SERVER_CRYPTO_ENABLED);
	     	            	
						}else{	
		    				Log.v(TAG,server.getServerName());
		    				Log.v(TAG,server.getIPAddress());
		    				Log.v(TAG,server.getUsername());
		    				Log.v(TAG,new String(server.getPassword())); 
		     	           
		    				removeDialog(DIALOG_EDIT_SERVER_CRYPTO_ENABLED);
		    				finish();
		     				Intent intent = new Intent(mContext, ServerList.class);
		     	        	startActivity(intent);
						}
    	            }
     			}
        		
     		});
        	
        	cancelButton_from_DIALOG_EDIT_SERVER.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					
					removeDialog(DIALOG_EDIT_SERVER_CRYPTO_ENABLED);
				}
        		
        	});
			dialog_EDIT_SERVER_CRYPTO_ENABLED.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					
				}
			});
        	break;
        	
        case DIALOG_EDIT_SERVER_CRYPTO_DISABLED:
        	
        	dialog_EDIT_SERVER_CRYPTO_DISABLED.setContentView(R.layout.layout_for_dialog_edit_server_crypto_disabled);
        	dialog_EDIT_SERVER_CRYPTO_DISABLED.setTitle(R.string.title_for_dialog_EDIT_SERVER_CRYPTO_ENABLED);
        	
        		//Buttons
        	final Button saveButton_in_dialog_EDIT_SERVER_CRYPTO_DISABLED = (Button) dialog_EDIT_SERVER_CRYPTO_DISABLED.findViewById(R.id.saveButton_in_dialog_edit_server_crypto_disabled);
        	final Button cancelButton_in_dialog_EDIT_SERVER_CRYPTO_DISABLED = (Button) dialog_EDIT_SERVER_CRYPTO_DISABLED.findViewById(R.id.cancelButton_in_dialog_edit_server_crypto_disabled);
        	
        	final EditText server_nameEditText = (EditText) dialog_EDIT_SERVER_CRYPTO_DISABLED.findViewById(R.id.server_name_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
			server_nameEditText.setText(serverListArrayList.get(serverToEditint).getServerName());
			server_nameEditText.setOnKeyListener(new OnKeyListener() {
				    public boolean onKey(View v, int keyCode, KeyEvent event) {
				        // If the event is a key-down event on the "enter" button
				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
				        	// Perform action on key press
				        	return true;
				        }
				        return false;
				    }
			});
			final EditText IP_addressEditText = (EditText) dialog_EDIT_SERVER_CRYPTO_DISABLED.findViewById(R.id.IP_address_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
			IP_addressEditText.setText(serverListArrayList.get(serverToEditint).getIPAddress());
			IP_addressEditText.setOnKeyListener(new OnKeyListener() {
				    public boolean onKey(View v, int keyCode, KeyEvent event) {
				        // If the event is a key-down event on the "enter" button
				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
				        	// Perform action on key press
				        	return true;
				        }
				        return false;
				    }
			});
			final EditText usernameEditText = (EditText) dialog_EDIT_SERVER_CRYPTO_DISABLED.findViewById(R.id.username_in_dialog_edit_server_EditText_crypto_disabled_from_edit_server);
			usernameEditText.setText(serverListArrayList.get(serverToEditint).getUsername());
			usernameEditText.setOnKeyListener(new OnKeyListener() {
				    public boolean onKey(View v, int keyCode, KeyEvent event) {
				        // If the event is a key-down event on the "enter" button
				        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
				        	// Perform action on key press
				        	return true;
				        }
				        return false;
				    }
			});
			
        	saveButton_in_dialog_EDIT_SERVER_CRYPTO_DISABLED.setOnClickListener(new OnClickListener() {
     			
     			@Override
     			public void onClick(View v) {
     					
     	           Log.v(TAG,"obecna ilosc zapisanych serverow wynosi: " + serverListArrayList.size());
     	           if (server_nameEditText.getText().length() == 0 || IP_addressEditText.getText().length() == 0 || usernameEditText.getText().length() == 0){
     	        	   Toast.makeText(getApplicationContext(), R.string.text_for_toast_fill_up_the_empty_spaces, Toast.LENGTH_LONG).show();
     	           }else if (!(isIPv4OrIPv6(IP_addressEditText.getText().toString()))){
     	        	   Toast.makeText(getApplicationContext(), R.string.text_for_toast_correct_IP_address, Toast.LENGTH_LONG).show();
  	            
     	           }else{	  	   	           
	     	            Server server = new Server();
	     	           
	     	            server.setServerName(server_nameEditText.getText().toString());
	     	            server.setIPAddress(IP_addressEditText.getText().toString());
	     	            server.setUsername(usernameEditText.getText().toString());
	     	            server.setPassword("a_blank_password".toCharArray());
	     	          
	     	            serverListArrayList.set(serverToEditint, server);
	     	            //a_XMLReaderWrriter.create_encrypted_XMLFile_with_server_list(server_list, app_password);
	     	            
	    				Log.v(TAG,server.getServerName());
	    				Log.v(TAG,server.getIPAddress());
	    				Log.v(TAG,server.getUsername());
	    				Log.v(TAG,new String(server.getPassword())); 
	     	           
	    				removeDialog(DIALOG_EDIT_SERVER_CRYPTO_DISABLED);
	    				finish();
	     				Intent intent = new Intent(mContext, ServerList.class);
	     	        	startActivity(intent);
     	           }
     			}
        		
     		});
        	
        	cancelButton_in_dialog_EDIT_SERVER_CRYPTO_DISABLED.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					
					removeDialog(DIALOG_EDIT_SERVER_CRYPTO_DISABLED);
				}
        		
        	});

        	break;

        case DIALOG_DELETE_SERVER:
        	Log.v(TAG,"Wszedłem do onCreate DIALOG_DELETE_SERVER");
        	itemsFor_DIALOG_DELETE_SERVER = new CharSequence[serverListArrayList.size()];
    		for (int i = 0; i < serverListArrayList.size(); i++){
    			
    			itemsFor_DIALOG_DELETE_SERVER[i] = serverListArrayList.get(i).getServerName();
    			Log.v(TAG,"Server_name :" + itemsFor_DIALOG_DELETE_SERVER[i]);
    		}
        	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        	builder.setTitle(R.string.title_for_dialog_DELETE_SERVER);
        	builder.setItems(itemsFor_DIALOG_DELETE_SERVER, new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog_DELETE_SERVER, int item) {
        	        
        	    	serverToDelete = item;
        	    	showDialog(DIALOG_DO_DELATE);
        	    	/*
        	        serverListArrayList.remove(item);
        	        if (appPasswordcharArray == null){
						showDialog(DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE);
						//a_XMLReaderWrriter.create_encrypted_XMLFile_with_server_list(server_list, app_password);	//sprawdzić czy będzie działać bez tego że niby w onPause() wystarczy
        	        	
					}else{	
						    				
	    				finish();
	     				Intent intent = new Intent(mContext, ServerList.class);
	     	        	startActivity(intent);
					}
     				*/
        	    }
        	});
        	
        	dialog_DELETE_SERVER = builder.create();

        	break;
        
        case DIALOG_DO_DELATE:
        	Log.v(TAG, "Wszedłem do onCreate DIALOG_DO_DELATE");
        	AlertDialog.Builder builderDIALOG_DO_DELATE = new AlertDialog.Builder(mContext);
			builderDIALOG_DO_DELATE.setTitle(getResources().getString(R.string.title_for_dialog_DO_DELETE));
			builderDIALOG_DO_DELATE.setMessage(getResources().getString(R.string.message_in_dialog_DO_DELATE) + " " + itemsFor_DIALOG_DELETE_SERVER[serverToDelete] + "?");
        	// Add the buttons
        	builderDIALOG_DO_DELATE.setPositiveButton(R.string.text_for_do_delete_button, new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	               	// User clicked OK button
        	        	   	serverListArrayList.remove(serverToDelete);
	               	        if (appPasswordcharArray == null){
	       						showDialog(DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE);
	       						//a_XMLReaderWrriter.create_encrypted_XMLFile_with_server_list(server_list, app_password);	//sprawdzić czy będzie działać bez tego że niby w onPause() wystarczy
	       						removeDialog(DIALOG_DO_DELATE);
	       					}else{	
	       						    				
	       	    				finish();
	       	     				Intent intent = new Intent(mContext, ServerList.class);
	       	     	        	startActivity(intent);
	       	     	        	removeDialog(DIALOG_DO_DELATE);
	       					}
        	        	   
        	           }
        	       });
        	builderDIALOG_DO_DELATE.setNegativeButton(R.string.text_for_cancel_button, new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	               // User cancelled the dialog
        	        	   removeDialog(DIALOG_DO_DELATE);
        	           }
        	       });
        	// Set other dialog properties
        	
        	// Create the AlertDialog
        	dialog_DO_DELATE = builderDIALOG_DO_DELATE.create();
        	
        	break;
        	
        case DIALOG_LICENSE:
        	// EULA title
            String title = getResources().getString(R.string.app_name);
 
            // EULA text
            String message = getResources().getString(R.string.Licences_text);
 
 
            AlertDialog.Builder builderDIALOG_LICENSE = new AlertDialog.Builder(mContext)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.text_for_cancel_button,
                            new Dialog.OnClickListener() {
 
                                @Override
                                public void onClick(
                                    DialogInterface dialogInterface, int i) {
                                    
                                    dialogInterface.dismiss();
 

                                }
                            });
                    
            	// Create the AlertDialog
            dialog_LICENSE = builderDIALOG_LICENSE.create();      	        	
            
            break;
        	
        default:
            dialog = null;
        }
		if (id == DIALOG_FIRST_TIME_RUNING){
	     	dialog =  dialog_FIRST_TIME_RUNING;
	    }
		if (id == DIALOG_GIVE_ME_A_APP_PASSWORD){
	     	dialog = dialog_GIVE_ME_A_APP_PASSWORD;
	    }
		if (id == DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE){
			dialog = dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOILEAN_IS_FALSE;
		}
		if (id == DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST){
			dialog = dialog_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST;
		}
		if (id == DIALOG_GIVE_ME_A_SERVER_PASSWORD){
			dialog = dialog_GIVE_ME_A_SERVER_PASSWORD;
		}
        if (id == DIALOG_ADD_NEW_SERVER_CRYPTO_ENABLED){
        	dialog =  dialog_ADD_NEW_SERVER_CRYPTO_ENABLED;
        }
        if (id == DIALOG_ADD_NEW_SERVER_CRYPTO_DISABLED){
        	dialog = dialog_ADD_NEW_SERVER_CRYPTO_DISABLED;
        }
        if (id == DIALOG_DELETE_SERVER){
        	dialog =  dialog_DELETE_SERVER;
        }
        if (id == DIALOG_DO_DELATE){
        	dialog = dialog_DO_DELATE;
        }
        if (id == DIALOG_CHOOSE_SERVER_TO_EDIT){
        	dialog = dialog_CHOSE_SERVER_TO_EDIT;
        }
        if (id == DIALOG_EDIT_SERVER_CRYPTO_ENABLED){
        	dialog = dialog_EDIT_SERVER_CRYPTO_ENABLED;
        }
        if (id == DIALOG_EDIT_SERVER_CRYPTO_DISABLED){
        	dialog = dialog_EDIT_SERVER_CRYPTO_DISABLED;
        }
        if (id == DIALOG_LICENSE){
        	dialog = dialog_LICENSE;
        }
        	
        return dialog;
    }
    
   
	
    /**
     * Metoda wywoływana przez system Android w reakcji na pierwsze wybranie przycisku menu urządzenia. Funkcja odpowiedzialna za wczytanie pliku XML z definicją menu aktywności.
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_for_serverlist, menu);
        return true;
    }
    /**
     * Metoda wywoływana przez system Android w reakcji na każde wybranie przycisku menu urządzenia. 
     * Służy do dynamicznego wprowadzania zmian w menu aktywności, tu za wyłączenie pozycji odpowiedzialnych za edycje i usuwanie danych serwera w przypadku gdy w pamięci urządzenia nie są zapisane, żadne informacje o serwerach SSH.   
     * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {

    	if (serverListArrayList.size() > 0 ){
        	menu.getItem(2).setEnabled(true);
        	menu.getItem(3).setEnabled(true);
        }

        return true;
    }
    /**
     * Metoda wywoływana przez system Android w reakcji na wybranie pozycji w menu aktywności. Służy do definiowania akcji, np. wyświetlenie okna dialogowego, wywoływanych po wybraniu konkretnej pozycji menu aktywności. 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_item_settings:
        	if (appPasswordcharArray == null && rememberAppPasswordInSesionboolean == false){
        		showDialog(DIALOG_GIVE_ME_A_APP_PASSWORD_BECAUSE_REMEMBER_APP_PASSWORD_IN_SESION_BOOLEAN_IS_FALSE_AND_I_NEED_IT_TO_START_SETTINGSFORSERVERLIST);
        	}else{
	        	final Intent intent_start_settings_activity_for_ServerList = new Intent(getApplicationContext(), SettingsForAPP.class);
	        	intent_start_settings_activity_for_ServerList.putExtra("app_password", appPasswordcharArray);		//app_password will be a "default_password" if is_crypto_eanbled is false
	        	startActivity(intent_start_settings_activity_for_ServerList);
	        	finish();
        	}
        	return true;
        case R.id.menu_item_add_new_server:
            //removeDialog(DIALOG_ADD_NEW_SERVER);
        	if (isCryptoEnabledboolean == true){
        		showDialog(DIALOG_ADD_NEW_SERVER_CRYPTO_ENABLED);
        	}else{
        		showDialog(DIALOG_ADD_NEW_SERVER_CRYPTO_DISABLED);
        	}
        	return true;
        case R.id.menu_item_exit_from_app:
        	if (appPasswordcharArray != null){
        		Arrays.fill(appPasswordcharArray,'0');
        		appPasswordcharArray = null;
					//ensure that service ConnectAndPlayService is stoped
				Intent intent_start_ConnectAndPlayService = new Intent(getApplicationContext(), ConnectAndPlayService.class);
				stopService(intent_start_ConnectAndPlayService);
        	}
            this.finish();
            return true;
        case R.id.menu_item_delete_server:
        	//removeDialog(DIALOG_DELETE_SERVER);
        	showDialog(DIALOG_DELETE_SERVER);
        	return true;
        case R.id.menu_item_edit_server:
        	//removeDialog(DIALOG_CHOOSE_SERVER_TO_EDIT);
        	showDialog(DIALOG_CHOOSE_SERVER_TO_EDIT);
        	return true;
        case R.id.medu_item_license:
        	showDialog(DIALOG_LICENSE);
        default:
            return super.onOptionsItemSelected(item);
        }
    }
  

   /*
   public static boolean validateIP(final String ip){          
	      Pattern pattern = Pattern.compile(IP_PATTERN);
	      Matcher matcher = pattern.matcher(ip);
	      return matcher.matches();             
   }
   */
   /*
   public static boolean isIPv4OrIPv6(String ip){
	   if (InetAddressUtils.isIPv4Address(ip) || InetAddressUtils.isIPv6Address(ip)){
		   return true;
	   }else{
		   return false;
	   }
   }
    */
   
   /**
    * Metoda weryfikująca poprawność tekstowej reprezentacji adresu IP w wersji czwartej i szóstej. Korzysta z {@link com.mplayer_remote.IPv6Verification#isIPv6Address}.
    * @param ip adres IP do zweryfikowania
    * @return true w przypadku poprawnego adresu IP, false w przypadku nieporawnego adresu IP.
    */
   public static boolean isIPv4OrIPv6(String ip){
	   /*
	   if (InetAddressUtils.isIPv4Address(ip) || IPv6Verification.isIPv6Address(ip)){
		   return true;
	   }else{
		   return false;
	  }
	  */
	   return true;
   }

	/**
	 * Klasa pomocnicza służąca do zapamiętywania czy okna dialogowe dialog_FIRST_TIME_RUNING i dialog_GIVE_ME_A_APP_PASSWORD tworzone w onCreate są wyświetlane na ekranie. Klasa StateHolder wraz z metodami
	 * onRetainNonConfigurationInstance, onCreate, showdialog_FIRST_TIME_RUNING, dismissdialog_FIRST_TIME_RUNING, showdialog_GIVE_ME_A_APP_PASSWORD, dismissdialog_GIVE_ME_A_APP_PASSWORD, onPause i onResume
	 * poprawnie zarządza wyświetlaniem okien dialogowych dialog_FIRST_TIME_RUNING i dialog_GIVE_ME_A_APP_PASSWORD w czasie restartu aktywności wywołanego zmianą konfiguracji urządzenia z czym nie radziły
	 * sobie standardowe mechanizmy systemu Android.
	 *
	 * @author sokar
	 *
	 */
	private static class StateHolder {
		boolean mIsShowingDialog_FIRST_TIME_RUNING;
		boolean mIsShowingDialog_GIVE_ME_A_PASSWORD;
		public StateHolder() {
			mIsShowingDialog_FIRST_TIME_RUNING = false;
			mIsShowingDialog_GIVE_ME_A_PASSWORD = false;
		}
	}
	/*
	private void createConnectButtons(int i){
		Button button_connect_to = new Button(ServerList.this);
		button_connect_to.setPadding(5, 5, 5, 5);
		button_connect_to.setText(serverListArrayList.get(i).getServerName());
		button_connect_to.setId(i);
		//custom look of button
		button_connect_to.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttontheme_btn_default_holo_light));
		//enabling menu item menu_item_edit_server and menu_item_delete_server
		invalidateOptionsMenu();

		final Intent intent_start_ConnectToServer = new Intent(getApplicationContext(), ConnectToServer.class);
		intent_start_ConnectToServer.putExtra("server_name", serverListArrayList.get(i).getServerName());
		intent_start_ConnectToServer.putExtra("IP_address", serverListArrayList.get(i).getIPAddress());
		intent_start_ConnectToServer.putExtra("username", serverListArrayList.get(i).getUsername());
		intent_start_ConnectToServer.putExtra("password", serverListArrayList.get(i).getPassword());

		button_connect_to.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(intent_start_ConnectToServer);

			}
		});
		ll.addView(button_connect_to);
	}

	private void createConnectButtonsThatAskForServerPassword(int i){
		final Button button_connect_to = new Button(ServerList.this);
		button_connect_to.setPadding(5, 5, 5, 5);
		button_connect_to.setText(serverListArrayList.get(i).getServerName());
		button_connect_to.setId(i);
		//custom look of button
		button_connect_to.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttontheme_btn_default_holo_light));
		//enabling menu item menu_item_edit_server and menu_item_delete_server
		invalidateOptionsMenu();

		button_connect_to.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Bundle bundleWithClickedButtonID = new Bundle();
				bundleWithClickedButtonID.putInt("clicked_button", button_connect_to.getId());
				Log.v(TAG, "button_connect_to.getId():" + button_connect_to.getId());
				showDialog(DIALOG_GIVE_ME_A_SERVER_PASSWORD, bundleWithClickedButtonID);


			}
		});
		ll.addView(button_connect_to);
	}
	*/

	private void createConnectButtons(int i){
		Button button_connect_to = new Button(ServerList.this);
		button_connect_to.setPadding(5, 5, 5, 5);
		button_connect_to.setText(serverListArrayList.get(i).getServerName());
		button_connect_to.setId(i);
		//custom look of button
		button_connect_to.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttontheme_btn_default_holo_light));
		//enabling menu item menu_item_edit_server and menu_item_delete_server
		invalidateOptionsMenu();

		final Intent intent_start_ConnectAndPlayService = new Intent(getApplicationContext(), ConnectAndPlayService.class);
		intent_start_ConnectAndPlayService.putExtra("server_name", serverListArrayList.get(i).getServerName());
		intent_start_ConnectAndPlayService.putExtra("IP_address", serverListArrayList.get(i).getIPAddress());
		intent_start_ConnectAndPlayService.putExtra("username", serverListArrayList.get(i).getUsername());
		intent_start_ConnectAndPlayService.putExtra("password", serverListArrayList.get(i).getPassword());

		button_connect_to.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				startService(intent_start_ConnectAndPlayService);
				connectingToSshProgressDialog = ProgressDialog.show(ServerList.this, "",getString(R.string.text_for_progressdialog_from_connecttoserver), true, true);
			}
		});
		ll.addView(button_connect_to);
	}

	private void createConnectButtonsThatAskForServerPassword(int i){ //TODO Correct DIALOG_GIVE_ME_A_SERVER_PASSWORD
		final Button button_connect_to = new Button(ServerList.this);
		button_connect_to.setPadding(5, 5, 5, 5);
		button_connect_to.setText(serverListArrayList.get(i).getServerName());
		button_connect_to.setId(i);
		//custom look of button
		button_connect_to.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttontheme_btn_default_holo_light));
		//enabling menu item menu_item_edit_server and menu_item_delete_server
		invalidateOptionsMenu();

		button_connect_to.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Bundle bundleWithClickedButtonID = new Bundle();
				bundleWithClickedButtonID.putInt("clicked_button", button_connect_to.getId());
				Log.v(TAG, "button_connect_to.getId():" + button_connect_to.getId());
				showDialog(DIALOG_GIVE_ME_A_SERVER_PASSWORD, bundleWithClickedButtonID);


			}
		});
		ll.addView(button_connect_to);
	}

	/**
	 * Wywołuje metodę <code>showDialog(DIALOG_FIRST_TIME_RUNING)</code> i zmienia wartość pola {@link mStateHolder.mIsShowingDialog_FIRST_TIME_RUNING} na <code>true</code>.
	 * @see android.app.Activity#showDialog(int)
	 */
	private void showdialog_FIRST_TIME_RUNING(){
		mStateHolder.mIsShowingDialog_FIRST_TIME_RUNING = true;
		showDialog(DIALOG_FIRST_TIME_RUNING);
	}

	/**
	 * Wywołuje metodę <code>dialog_FIRST_TIME_RUNING.dismiss()</code> i zmienia wartość pola {@link mStateHolder.mIsShowingDialog_FIRST_TIME_RUNING} na <code>false</code>.
	 * @see android.app.Dialog#dismiss()
	 */
	private void dismissdialog_FIRST_TIME_RUNING(){
		mStateHolder.mIsShowingDialog_FIRST_TIME_RUNING = false;
		dialog_FIRST_TIME_RUNING.dismiss();
	}

	/**
	 * Wywołuje metodę <code>showDialog(DIALOG_GIVE_ME_A_APP_PASSWORD)</code> i zmienia wartość pola {@link mStateHolder.mIsShowingDialog_GIVE_ME_A_PASSWORD} na <code>true</code>.
	 * @see android.app.Activity#showDialog(int)
	 */
	private void showdialog_GIVE_ME_A_APP_PASSWORD() {
		mStateHolder.mIsShowingDialog_GIVE_ME_A_PASSWORD = true;
		showDialog(DIALOG_GIVE_ME_A_APP_PASSWORD);
	}

	/**
	 * Wywołuje metodę <code>removeDialog(DIALOG_GIVE_ME_A_APP_PASSWORD)</code> i zmienia wartość pola {@link mStateHolder.mIsShowingDialog_GIVE_ME_A_PASSWORD} na <code>false</code>.
	 * @see android.app.Activity#removeDialog(int);
	 */
	private void dismissdialog_GIVE_ME_A_APP_PASSWORD(){
		mStateHolder.mIsShowingDialog_GIVE_ME_A_PASSWORD = false;
		//dialog_GIVE_ME_A_APP_PASSWORD.dismiss();
		removeDialog(DIALOG_GIVE_ME_A_APP_PASSWORD);
	}
}