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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import android.support.v4.app.DialogFragment;

/**
 * Aktywność umożliwiająca zmianę ustawień aplikacji. 
 * Pozwala zdecydować czy aplikacja ma szyfrować przechowywane w pamięci urządzenia dane serwerów SSH, a tym samym pytać za każdym uruchomieniem o hasło aplikacji.  
 * @author sokar
 * @see android.app.Activity
 */
public class SettingsForAPP extends FragmentActivity{
		//w celach diagnostycznych nazwa logu dla tego Activity
	private static final String TAG = "SettingsForAPP";
		//Zapisany stan elementów interfejsu i ustawiń dla activity lub aplikacji
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
	
		//a app_password taken from intent
	/**
	 * Hasło aplikacji, na jego podstawie jest generowany klucz szyfru AES potrzebny do zaszyfrowania w pamięci urządzenia danych serwerów SSH. 
	 */
	private char[] appPasswordcharArray = null;
	
	/**
     * Obiekt klasy {@link com.mplayer_remote.XMLReaderWriter}, służący do przekształcania listy {@link SettingsForAPP#serverListArrayList} w zaszyfrowany algorytmem AES plik XML,
     * udostępnia on również możliwość odszyfrowania tego pliku XML i przekształcenie go z powrotem w listę {@link SettingsForAPP#serverListArrayList}.
     */
	XMLReaderWriter aXMLReaderWriter;
		
	/**
	 * Pole wyboru opcji, element GUI aktywności pozwalający decydować użytkownikowi, czy aplikacja ma przechowywać w pamięci urządzenia zaszyfrowane hasła serwerów SSH.
	 */
	private CheckBox useEencryptionCheckBox = null;

	/**
	 * Pole wyboru opcji pozwala decydować czy aktywność FileChooser ma pokazywać jedynie znane pliki multimedialne.
	 */
	private CheckBox showOnlyMediaTypeFilesCheckBox = null;

	/**
	 * Key showOnlyMediaTypeFilesBoolean for SharedPreferences.
	 */
	private static final String SHOW_ONLY_MEDIA_TYPE_FILES = "showOnlyMediaTypeFilesBoolean";

	/**
	 * Zmienna logiczna mająca wrtość <code>true</code> kiedy aktywność FileChooser ma pokazywać jedynie zznane pliki multimedialne.
	 * Wartość odczytywana za pomocą interfejsu {@link com.mplayer_remote.ServerList#settingsForAPPSharedPreferences}.
	 */
	private Boolean showOnlyMediaTypeFilesBoolean = true;

	/**
	 * TextView with open FileExtensionChooserDialog
	 */
	private TextView knowFileExtensionsTextView;

		//Lista serverów
	/**
	 * Lista obiektów klasy {@link com.mplayer_remote.Server}. Tu jest zapisywana odszyfrowana z pamięci urządzenia lista serwerów SSH. 
	 */
	private List <Server> serverListArrayList = new ArrayList<Server>();
	
	//private Boolean mConfigurationChange = false;		//a idea for zero out appPassowrdcharArray in onfrom http://stackoverflow.com/questions/8314935/how-do-i-tell-when-a-configuration-change-is-happening-in-froyo not implemented yet
	
	/**
     * Metoda wywoływana przez system Android przy starcie aktywności.
     * Tu następuje wyczytanie ustawień aplikacji i zainicjowanie GUI aktywności.
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(false);
        	//ustawianie GUI
        setContentView(R.layout.layout_for_settingsforserverlist);
        settingsForAPPSharedPreferences = getSharedPreferences("settings_for_APP", 0);
        isThisFirstRunboolean = settingsForAPPSharedPreferences.getBoolean("is_this_first_run", true);
        isCryptoEnabledboolean = settingsForAPPSharedPreferences.getBoolean("is_crypto_enabled", true);		//było false
		showOnlyMediaTypeFilesBoolean = settingsForAPPSharedPreferences.getBoolean(SHOW_ONLY_MEDIA_TYPE_FILES, true);
        
        if (savedInstanceState == null){
        	Intent intent_from_ServerList = getIntent(); //getIntent() zwraca obiekt Intent który wystartował Activity
        	appPasswordcharArray = intent_from_ServerList.getCharArrayExtra("app_password");
        }else{
        	appPasswordcharArray = savedInstanceState.getCharArray("appPasswordcharArray");
        }
        
        Log.v(TAG, "aktualny isThisFirstRunboolean: " + isThisFirstRunboolean);
        Log.v(TAG, "aktualny isCryptoEnabledboolean: " + isCryptoEnabledboolean);
               
        if (appPasswordcharArray != null){
        	String appPasswordcharArrayConvertedToString = new String(appPasswordcharArray);
        	Log.v(TAG, "Aktuane appPasswordcharArray: " + appPasswordcharArrayConvertedToString);
        }
        
    		//creating a XML
		
		Context mContext = getApplicationContext();
        aXMLReaderWriter = new XMLReaderWriter(mContext);
        
        if(isCryptoEnabledboolean == true && appPasswordcharArray != null){		//appPasswordcharArray == null 
			try {
				serverListArrayList = aXMLReaderWriter.decryptFileWithXMLAndParseItToServerList(appPasswordcharArray);
			} catch (WrongPasswordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), R.string.wrong_app_password_exeption, Toast.LENGTH_SHORT).show();
				finish();	//something is wrong
			}
        }else{
        	try {
				serverListArrayList = aXMLReaderWriter.decryptFileWithXMLAndParseItToServerList("default_password".toCharArray());
			} catch (WrongPasswordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), R.string.wrong_app_password_exeption, Toast.LENGTH_SHORT).show();
				finish();	//something is wrong
			}
        }

        useEencryptionCheckBox = (CheckBox) findViewById(R.id.use_encryption_in_SettingForServer_checkBox);
        useEencryptionCheckBox.setChecked(isCryptoEnabledboolean);
		useEencryptionCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if (useEencryptionCheckBox.isChecked() == true) {
					isCryptoEnabledboolean = true;
					isThisFirstRunboolean = true;

				} else {
					isCryptoEnabledboolean = false;
				}

				SharedPreferences settings_for_activity_ServerList = getSharedPreferences("settings_for_APP", 0);
				SharedPreferences.Editor editor = settings_for_activity_ServerList.edit();
				editor.putBoolean("is_crypto_enabled", isCryptoEnabledboolean);
				editor.putBoolean("is_this_first_run", isThisFirstRunboolean);
				// Commit the edits!
				editor.commit();

				if (useEencryptionCheckBox.isChecked() == true) {
					Toast.makeText(getApplicationContext(), R.string.text_for_toast_Edit_server_data_fill_server_password_field, Toast.LENGTH_LONG).show();
					aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListArrayList, appPasswordcharArray);
				} else {
					//appPasswordcharArray = "default_password".toCharArray();
					for (int i = 0; i < serverListArrayList.size(); i++) {
						serverListArrayList.get(i).setPassword("".toCharArray());
					}
					aXMLReaderWriter.createEncryptedXMLFileWithServerList(serverListArrayList, "default_password".toCharArray());
				}

			}
		});

		showOnlyMediaTypeFilesCheckBox = (CheckBox) findViewById((R.id.show_only_media_type_files_checkBox));
		showOnlyMediaTypeFilesCheckBox.setChecked(showOnlyMediaTypeFilesBoolean);
		showOnlyMediaTypeFilesCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

				if (showOnlyMediaTypeFilesCheckBox.isChecked() == true){
					showOnlyMediaTypeFilesBoolean = true;
				} else {
					showOnlyMediaTypeFilesBoolean = false;
				}

				SharedPreferences settings_for_activity_ServerList = getSharedPreferences("settings_for_APP", 0);
				SharedPreferences.Editor editor = settings_for_activity_ServerList.edit();
				editor.putBoolean(SHOW_ONLY_MEDIA_TYPE_FILES, showOnlyMediaTypeFilesBoolean);
				//Commit the edits!
				editor.commit();
			}
		});

		knowFileExtensionsTextView = (TextView) findViewById(R.id.know_file_extensions);
		knowFileExtensionsTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.v(TAG,"klikłem knowFileExtensionsTextView");
				DialogFragment newFragment = new KnowFileExtensionsDialogFragment();
				newFragment.show(getSupportFragmentManager(), "KnowFileExtensionsDialogFragment");

			}
		});

    }

    /**
     * Metoda wywoływana przez system Android przed zniszczeniem aktywności, służy do zapamiętywania stanu aktywności. Tu do zapamiętania hasła aplikacji pomiędzy restartami aktywności.  
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState (Bundle outState){
    	outState.putCharArray("appPasswordcharArray", appPasswordcharArray);
    }
    /*
	@Override
	protected void onPause() {
		super.onPause();
		//finish();
		
	}
	*/
    /*
    @Override
    public Object onRetainNonConfigurationInstance() {
        mConfigurationChange = true;
        return null;
    }
	*/
    
	/** 
	 * Metoda wywoływana przez system Android, kiedy użytkownik naciśnie przycisk wstecz swojego urządzenia.
	 * Tu odpowiada za zakończenie wyświetlania aktywności SettingsForAPP, uruchomienie aktywności ServerList i wymazanie z pamięci hasła do aplikacji.
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
    public void onBackPressed(){		//when user press a back key
    	final Intent intent_start_ServerList = new Intent(getApplicationContext(), ServerList.class);
    	//intent_start_ServerList.putExtra("app_password", appPasswordcharArray);
    	startActivity(intent_start_ServerList);
    	Arrays.fill(appPasswordcharArray,'0');
		appPasswordcharArray = null;
    	finish();
    }
  
}



