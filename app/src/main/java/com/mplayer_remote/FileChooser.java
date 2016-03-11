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

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Aktywność pozwalająca przeglądać katalogi znajdujące się na serwerze, z którym aplikacja MPlayerRemote się połączyła oraz wskazanie pliku do odtworzenia w programie <code>MPlayer</code>.
 * @author sokar
 * @see android.app.Activity
 * @see android.app.ListActivity
 */
public class FileChooser extends ListActivity{

		//w celach diagnostycznych nazwa logu dla tego Activity
	private static final String TAG = "FileChooser";

	//ArreaListy z zawartością obecnie przeglądanego katalogu
	/**
	 *	Lista zawierająca nazwy wszystkich plików i folderów znajdujących się w obecnie przeglądanym przez użytkownika katalogu.
	 */
	private ArrayList<String> dir_contain = new ArrayList<String>();

	/**
	 * Lista zawierająca nazwy wszystkich plików znajdujących się w obecnie przeglądanym przez użytkownika katalogu.
	 */
	private ArrayList<String> only_file_from_absolute_path = new ArrayList<String>();

	/**
	 * Łańcuch znaków zawierający ścieżkę absolutną, w której znajduje się plik wskazany przez użytkownika do odtworzenia w programie MPlayer.
	 */
	private String absolute_path = null;

	/**
	 * Łańcuch znaków zawierający pełna nazwę wraz ze ścieżką absolutną prowadzącą do pliku wskazanego przez użytkownika w celu odtworzenia go w programie MPlayer.
	 */
	private String file_to_play = null;

	/**
	 *Content of long pressed dir form ListView.
	 */
	private ArrayList<String> long_pressed_dir_contain = new ArrayList<String>();

	/**
	 * List that contain only files form long pressed dir.
	 */
	private ArrayList<String> only_file_from_absolute_path_of_long_pressed_dir = new ArrayList<String>();

	/**
	 * String with absolute path of long pressed dir.
	 */
	private String absolute_path_of_long_pressed_dir = "";

	/**
	 * String that contain files from long pressed directory.
	 */
	private ArrayList<String> fileS_to_palyArrayList = new ArrayList<String>();

	/**
	 * Nazwa ostatnio wskazanego pliku lub folderu wyświetlana przez GUI aktywności jako jeden z elementów <code>ListView</code>.
	 */
	private String string_from_selected_view_in_ListView = "";

	/**
	 * String with name of long pressed dir.
	 */
	private String string_from_long_pressed_view_in_ListView = "";

	/**
	 * ListView holded by ListActivity.
	 */
	private ListView lV;

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
	 *
	 */
	private SharedPreferences settingsForAPPSharedPreferences;
		
		//dialogs
	/**
	 * Numer okna dialogowego <code>DIALOG_PLAY_A_FILE</code>.
	 */
	private static final int DIALOG_PLAY_A_FILE = 0;

	/**
	 * Numer okna dialogowego <code>DIALOG_PLAY_A_FILES</code>
	 */
	private static final int DIALOG_PLAY_A_FILES = 1;

	private static final String FILENAME = "userSelectedMediaFileExtensions";	//Filename for checkedFileExtensionsSharedPreferences XML file.
	private ArrayList<String> defaultAllKnowMediaFileExtensionsArrayList;   //From KnowMediaFileExtensions.xml
	private boolean[] checkedFileExtensionbooleanarray;     //Where we track the selected items
	private SharedPreferences checkedFileExtensionsSharedPreferences;   //Where are saved selected by user file extensions

	private ConnectAndPlayService mConnectAndPlayService;
	private boolean mBound = false;

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
									   IBinder service) {
			// We've bound to ConnectAndPlayService, cast the IBinder and get ConnectAndPlayService instance
			ConnectAndPlayService.LocalBinder binder = (ConnectAndPlayService.LocalBinder) service;
			mConnectAndPlayService = binder.getService();
			mBound = true;

			createUI();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	Bundle mSavedInstanceState;	// for createUI


	/**
	 * For fragments.
	 * @return mBound
	 */
	public boolean getmBound(){
		return mBound;
	}
	/**
	 * For fragments.
	 * @return mConnectAndPlayService
	 */
	public ConnectAndPlayService getmConnectAndPlayService(){
		return mConnectAndPlayService;
	}

	/**
	 * Metoda wywoływana przez system Android przy starcie aktywności, która na podstawie danych zwróconych przez serwer tworzy GUI zawierające listę plików i folderów zawartych w katalogu domowym użytkownika.
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(false);

    }


	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		absolute_path = state.getString("absolute_path");

	}

	/**
	 * Metoda wywoływana przez system Android przed zniszczeniem aktywności, służy do zapamiętywania stanu aktywności, tu konkretnie miejsca w drzewie katalogów serwera, które jest aktualnie wyświetlane przez GUI.
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle instanceStateToSave) {
		instanceStateToSave.putString("absolute_path", absolute_path);


	}


	@Override
	protected void onStart() {
		super.onStart();

		// Bind to ConnectAndPlayService
		Intent intent = new Intent(this, ConnectAndPlayService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

	}

	@Override
	protected void onStop() {
		super.onStop();
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		mConnectAndPlayService.stopPlaying();
		Intent intent_start_ConnectAndPlayService = new Intent(getApplicationContext(), ConnectAndPlayService.class);
		stopService(intent_start_ConnectAndPlayService);
	}

	private void createUI(){ //TODO zapisywać file_to_play i absolute_path w onStop w onStart odczytywać, w onCreate resetować zmienne w sharedPreferences
		//file_to_play = getIntent().getStringExtra("file_to_play");	//this will by not null if back from notyfication
		//absolute_path = getIntent().getStringExtra("absolute_path");	//this will by not null if back from notyfication

		Log.v(TAG, "file_to_play: " + file_to_play);
		Log.v(TAG, "absolute_path on start createUI(): " + absolute_path);
		//Log.v(TAG, "mSavedInstanceState: " + mSavedInstanceState);

		if (mBound) {

			if(getIntent().getStringExtra("absolute_path") != null){	//intent from RemoteControl.onBackPressed(), this not work because http://stackoverflow.com/questions/20695522/puzzling-behavior-with-reorder-to-front
				absolute_path = getIntent().getStringExtra("absolute_path");
				Log.v(TAG, "Back button from RemoteControl");

				dir_contain = new ArrayList<String>();//erasing
				dir_contain.add("..");

				mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("ls --group-directories-first  " + "'" + absolute_path + "/" + "'", dir_contain);
				mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("ls -p " + "'" + absolute_path + "/" + "'" + "| grep -v /", only_file_from_absolute_path);

				getIntent().removeExtra("absolute_path"); //intent from RemoteControl.onBackPressed() consumed, erase extra

			}else if (absolute_path == null) { // FileChooser started from ConnectAndPlayService
				Log.v(TAG, "Fresh start");
				//sendCommandAndSaveOutputToArrayList("ls -p | grep -v /");

				dir_contain = new ArrayList<String>();//erasing
				mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("echo $HOME", dir_contain);

				absolute_path = dir_contain.get(0);
				dir_contain = new ArrayList<String>();//erasing
				dir_contain.add("..");

				mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("ls --group-directories-first  " + "'" + absolute_path + "/" + "'", dir_contain);
				mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("ls -p " + "'" + absolute_path + "/" + "'" + "| grep -v /", only_file_from_absolute_path);

				for (int i = 0; i < dir_contain.size(); i++) {
					Log.v(TAG, dir_contain.get(i));
				}

			}else if (absolute_path != null) { //FileChooser is being restarted by for example screen rotation
				Log.v(TAG, "Restart");

				dir_contain = new ArrayList<String>();//erasing
				dir_contain.add("..");

				mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("ls --group-directories-first  " + "'" + absolute_path + "/" + "'", dir_contain);
				mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("ls -p " + "'" + absolute_path + "/" + "'" + "| grep -v /", only_file_from_absolute_path);
			}



			settingsForAPPSharedPreferences = settingsForAPPSharedPreferences = getSharedPreferences("settings_for_APP", 0);
			showOnlyMediaTypeFilesBoolean = settingsForAPPSharedPreferences.getBoolean(SHOW_ONLY_MEDIA_TYPE_FILES, true);
			//removing files with unknown file extensions from dir_contain and only_file_from_absolute_path.

			if (showOnlyMediaTypeFilesBoolean == true) {
				dir_contain = removeUnknownFileType(dir_contain, absolute_path);
				dir_contain.add(0, "..");
				only_file_from_absolute_path = removeUnknownFileType(only_file_from_absolute_path, absolute_path);
			}

			//GUI
			//getting ListView from ListActivity
			lV = getListView();
			// LongClickListener to lV
			lV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
					return onLongListItemClick(v, pos, id);
				}
			});

			setListAdapter(new MyBaseAdapter(dir_contain, only_file_from_absolute_path));
			lV.setTextFilterEnabled(true);

		}
	}



    /**
     * Metoda wywoływana kiedy użytkownik wybierze dowolną pozycje z listy wyświetlanej przez aktywność. 
     * W przypadku wybrania pliku multimedialnego uruchamia usługę ServicePlayAFile. Jeśli użytkownik wskaże katalog, to do serwera zostaną wysłane polecenia, które wydrukują na <code>stdout</code> zawartość tego katalogu.
     * Na podstawie tych danych metoda aktualizuje graficzny interfejs użytkownika wyświetlany przez aktywność.   
     * 
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		if(mBound == true) {
			string_from_selected_view_in_ListView = dir_contain.get(position);		//name of file or dir that was choose by user

			only_file_from_absolute_path = new ArrayList<String>();
			mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("ls -p " + "'" + absolute_path + "/" + "'" + "| grep -v /", only_file_from_absolute_path);
			if (only_file_from_absolute_path.indexOf(string_from_selected_view_in_ListView) != -1){

				file_to_play = absolute_path + "/" + string_from_selected_view_in_ListView;
				Log.v(TAG,"file_to_play to: " + file_to_play);
				Log.v(TAG,"absolute_path to: " + absolute_path);

				if (isMyServiceRunning() == false){

						mConnectAndPlayService.playAFile(file_to_play, absolute_path);

					/*
					Intent intent_start_ServicePlayAFile = new Intent(getApplicationContext(), ServicePlayAFile.class);
					intent_start_ServicePlayAFile.putExtra("file_to_play", file_to_play);
					intent_start_ServicePlayAFile.putExtra("absolute_path", absolute_path);
					startService(intent_start_ServicePlayAFile);
					Log.v(TAG, "startuje ServicePlayAFile z plikiem " + file_to_play);
					*/
					/*
					Intent intent_start_RemoteControl = new Intent(getApplicationContext(), RemoteControl.class);
					intent_start_RemoteControl.putExtra("file_to_play", file_to_play);
					intent_start_RemoteControl.putExtra("absolute_path", absolute_path);
					startActivity(intent_start_RemoteControl);
					*/
				}else{
					showDialogPlayAFileDialogFragment();
				}

			}else if(mConnectAndPlayService.sendCommandAndWaitForExitStatus("cd " + "'" + absolute_path + "/" + string_from_selected_view_in_ListView + "/" + "'") == 0){

				absolute_path = absolute_path + "/" + string_from_selected_view_in_ListView;
				Log.v(TAG,"obecne absolute_path to: " + absolute_path);
				dir_contain = new ArrayList<String>();
				dir_contain.add("..");
				mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("ls --group-directories-first  " + "'" + absolute_path + "/" + "'", dir_contain);
				only_file_from_absolute_path = new ArrayList<String>();
				mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("ls -p " + "'" + absolute_path + "/" + "'" + "| grep -v /",only_file_from_absolute_path);

					//removing files with unknown file extensions from dir_contain and only_file_from_absolute_path.
				if (showOnlyMediaTypeFilesBoolean == true) {
					dir_contain = removeUnknownFileType(dir_contain, absolute_path);
					dir_contain.add(0, "..");
					only_file_from_absolute_path = removeUnknownFileType(only_file_from_absolute_path, absolute_path);
				}

				setListAdapter(new MyBaseAdapter(dir_contain, only_file_from_absolute_path));

			}else{
				Toast.makeText(getApplicationContext(), R.string.text_for_toast_you_do_not_have_rights_to_open_this_directory, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 *
	 * @param v
	 * @param position
	 * @param id
	 * @return
	 */
	protected boolean onLongListItemClick(View v, int position, long id) {
		if(mBound == true) {
			string_from_long_pressed_view_in_ListView = dir_contain.get(position);		//name of file or dir that was choose by user
			Log.v(TAG, "string z długiego kliknięcia to: " + string_from_long_pressed_view_in_ListView);


			only_file_from_absolute_path_of_long_pressed_dir = new ArrayList<String>();
			mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("ls -p " + "'" + absolute_path + "/" + "'" + "| grep -v /", only_file_from_absolute_path_of_long_pressed_dir);
			if (only_file_from_absolute_path_of_long_pressed_dir.indexOf(string_from_long_pressed_view_in_ListView) != -1){		//long press will behave like normal click on file.

				file_to_play = absolute_path + "/" + string_from_long_pressed_view_in_ListView;
				Log.v(TAG,"file_to_play to: " + file_to_play);
				Log.v(TAG,"absolute_path to: " + absolute_path);

				if (isMyServiceRunning() == false){

					mConnectAndPlayService.playAFile(file_to_play, absolute_path);

					/*
					Intent intent_start_ServicePlayAFile = new Intent(getApplicationContext(), ServicePlayAFile.class);
					intent_start_ServicePlayAFile.putExtra("file_to_play", file_to_play);
					intent_start_ServicePlayAFile.putExtra("absolute_path", absolute_path);
					startService(intent_start_ServicePlayAFile);
					Log.v(TAG, "startuje ServicePlayAFile z plikiem " + file_to_play);
					*/
					/*
					Intent intent_start_RemoteControl = new Intent(getApplicationContext(), RemoteControl.class);
					intent_start_RemoteControl.putExtra("file_to_play", file_to_play);
					intent_start_RemoteControl.putExtra("absolute_path", absolute_path);
					startActivity(intent_start_RemoteControl);
					*/
				}else{
					showDialogPlayAFileDialogFragment();
				}


			}else if(mConnectAndPlayService.sendCommandAndWaitForExitStatus("cd " + "'" + absolute_path + "/" + string_from_long_pressed_view_in_ListView + "/" + "'") == 0){	//long press on directory

				only_file_from_absolute_path_of_long_pressed_dir = new ArrayList<String>();

				absolute_path_of_long_pressed_dir = absolute_path + "/" + string_from_long_pressed_view_in_ListView;
				Log.v(TAG, "absolute_path_of_long_pressed_dir to: " + absolute_path_of_long_pressed_dir);
				mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("ls --group-directories-first  " + "'" + absolute_path_of_long_pressed_dir + "/" + "'", long_pressed_dir_contain);
				mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("ls -p " + "'" + absolute_path_of_long_pressed_dir + "/" + "'" + "| grep -v /", only_file_from_absolute_path_of_long_pressed_dir);

					//removing files with unknown file extensions from long_pressed_dir_contain and only_file_from_absolute_path_of_long_pressed_dir.
				long_pressed_dir_contain = removeUnknownFileType(long_pressed_dir_contain, absolute_path_of_long_pressed_dir);
				long_pressed_dir_contain.add(0, "..");
				only_file_from_absolute_path_of_long_pressed_dir = removeUnknownFileType(only_file_from_absolute_path_of_long_pressed_dir, absolute_path_of_long_pressed_dir);

				if(only_file_from_absolute_path_of_long_pressed_dir.size() == 0){	//there is no multimedia file in this directory
					Toast.makeText(getApplicationContext(),R.string.text_for_toast_nothing_to_play,Toast.LENGTH_SHORT).show();
				}else {
						fileS_to_palyArrayList = new ArrayList<String>();
						for (int i = 0; i < only_file_from_absolute_path_of_long_pressed_dir.size(); i++) {
							fileS_to_palyArrayList.add(i, absolute_path_of_long_pressed_dir + "/" + only_file_from_absolute_path_of_long_pressed_dir.get(i));
						}

						if (isMyServiceRunning() == false) {

							mConnectAndPlayService.playAFiles(fileS_to_palyArrayList, absolute_path);

							/*
							for (int i = 0; i < fileS_to_palyArrayList.size(); i++) {
								Intent intent_start_ServicePlayAFile = new Intent(getApplicationContext(), ServicePlayAFile.class);
								intent_start_ServicePlayAFile.putExtra("file_to_play", fileS_to_palyArrayList.get(i));
								intent_start_ServicePlayAFile.putExtra("absolute_path", absolute_path_of_long_pressed_dir);
								startService(intent_start_ServicePlayAFile);
								Log.v(TAG, "startuje ServicePlayAFile z plikiem " + fileS_to_palyArrayList.get(i));
							}
							*/
						} else {
							showDialogPlayAFileSDialogFragment();
						}
						//Log.v(TAG, "pliki z długo klikniętego katalogu: " + fileS_to_paly);

					/*
					Intent intent_start_ServicePlayAFile = new Intent(getApplicationContext(), ServicePlayAFile.class);
					intent_start_ServicePlayAFile.putExtra("file_to_play", fileS_to_paly);
					intent_start_ServicePlayAFile.putExtra("absolute_path", absolute_path);
					startService(intent_start_ServicePlayAFile);
					*/
				}
			}else{
				Toast.makeText(getApplicationContext(), R.string.text_for_toast_you_do_not_have_rights_to_open_this_directory, Toast.LENGTH_SHORT).show();
			}
		}
		return true;
	}





	private void showDialogPlayAFileDialogFragment() {

		// DialogFragment.show() will take care of adding the fragment
		// in a transaction.  We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("DialogPlayAFileDialogFragment");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		DialogFragment newFragment = DialogPlayAFileDialogFragment.newInstance(file_to_play, absolute_path);
		newFragment.show(ft, "DialogPlayAFileDialogFragment");
	}

	private void showDialogPlayAFileSDialogFragment() {

		// DialogFragment.show() will take care of adding the fragment
		// in a transaction.  We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("DialogPlayAFileSDialogFragment");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		DialogFragment newFragment = DialogPlayAFileSDialogFragment.newInstance(only_file_from_absolute_path_of_long_pressed_dir, absolute_path_of_long_pressed_dir);
		newFragment.show(ft, "DialogPlayAFileSDialogFragment");
	}

    /**
     * Sprawdza czy usługa ServicePlayAFile działa w tle.
     * @return <code>true</code> jeśli usługa ServicePlayAFile działa w tle, <code>false</code> w przeciwnym wypadku.
     */
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.mplayer_remote.ConnectAndPlayService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

	private ArrayList<String> removeUnknownFileType(ArrayList <String>FilesArrayList, String absolute_path ){

		String[] tempArray = getResources().getStringArray(R.array.know_media_file_extensions);
		List<String> tempList = new ArrayList<String>();
		tempList = Arrays.asList(tempArray);
		defaultAllKnowMediaFileExtensionsArrayList = new ArrayList<String>(tempList);

		checkedFileExtensionbooleanarray = new boolean[defaultAllKnowMediaFileExtensionsArrayList.size()];	//contain true, false values
		checkedFileExtensionsSharedPreferences = getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
		for (int i = 0; i < defaultAllKnowMediaFileExtensionsArrayList.size(); i++){
			checkedFileExtensionbooleanarray[i] = checkedFileExtensionsSharedPreferences.getBoolean(defaultAllKnowMediaFileExtensionsArrayList.get(i), true);
			//Log.v(TAG, Boolean.toString(checkedFileExtensionbooleanarray[i]));
		}

		ArrayList<String> only_dir_from_absolute_path_with_slash = new ArrayList<String>();
		ArrayList<String> only_dir_from_absolute_path = new ArrayList<String>();
		mConnectAndPlayService.sendCommandAndSaveOutputToArrayList("ls -p " + "'" + absolute_path + "/" + "'" + "| grep /",only_dir_from_absolute_path_with_slash);
		for (int i = 0; i < only_dir_from_absolute_path_with_slash.size(); i++){
			only_dir_from_absolute_path.add(i, only_dir_from_absolute_path_with_slash.get(i).substring(0, only_dir_from_absolute_path_with_slash.get(i).length() - 1));
			//Log.v(TAG, "only_dir_from_absolute_path without / from removeUnknownFileType: " + only_dir_from_absolute_path.get(i));
		}


		ArrayList<String> FilesWithKnowExtensions = new ArrayList<String>();


		String FileName = null;
		int positionOfLastDot = 0;
		for (int i = 0; i < FilesArrayList.size(); i++){
			FileName = FilesArrayList.get(i);


			for(String dirname: only_dir_from_absolute_path){
				if (dirname.equals(FileName)){
					FilesWithKnowExtensions.add(FileName);
				}
			}

			positionOfLastDot = FileName.lastIndexOf(".");
			if (positionOfLastDot != -1){

				String fileNameExtensionString = FileName.substring(positionOfLastDot + 1);
				for (int j = 0; j < defaultAllKnowMediaFileExtensionsArrayList.size(); j++) {
					if (fileNameExtensionString.equals(defaultAllKnowMediaFileExtensionsArrayList.get(j)) && checkedFileExtensionbooleanarray[j] == true) {
						FilesWithKnowExtensions.add(FileName);
						//Log.v(TAG,"Znane rozszrzenie: " + FilesWithKnowExtensions.get(j));
					}

				}
			}
		}
		return FilesWithKnowExtensions;
	}
    
    /**
     * Klasa pomocnicza, pośrednicząca pomiędzy listami katalogów i plików oraz samych plików, a <code>ListView</code>(pionowa przewijalna lista) wyświetlanym przez aktywność FileChooser.  
     * Decyduje jak ma wyglądać dany element listy.
     * @see android.app.ListActivity
     * @see android.widget.ListView
     * @see android.widget.BaseAdapter
     * @author sokar
     */
    private class MyBaseAdapter extends BaseAdapter{
    	private ArrayList<String>dir_contain;
    	private ArrayList<String>only_file_from_absolute_path;
    	private LayoutInflater mInflater;
    	private static final int TYPE_DIR = 0;
        private static final int TYPE_FILE = 1;
        private static final int TYPE_MAX_COUNT = 2;
        
    	public MyBaseAdapter(ArrayList<String> dir_contain, ArrayList<String> only_file_from_absolute_path){
    		this.dir_contain = dir_contain;
    		this.only_file_from_absolute_path = only_file_from_absolute_path;
    		mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	}
		@Override
		public int getCount() {
			
			return dir_contain.size();
		}

		@Override
		public Object getItem(int position) {
			
			return dir_contain.get(position);
		}

		@Override
		public long getItemId(int position) {
			
			return position;
		}
		@Override
		public int getItemViewType (int position){
			if (only_file_from_absolute_path.indexOf(dir_contain.get(position)) == -1){
				return TYPE_DIR;
			}else{
				return TYPE_FILE;
			}
			
		}
		@Override
        public int getViewTypeCount() {
            return TYPE_MAX_COUNT;
        }
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			int view_type = getItemViewType(position); 
			if(convertView == null){
				switch (view_type) {
					case TYPE_DIR: 
						convertView = mInflater.inflate(R.layout.layout_for_dir_list_item, null);
						holder = new ViewHolder();
						holder.textView = (TextView)convertView.findViewById(R.id.textView1);
		                break;
					case TYPE_FILE:
						convertView = mInflater.inflate(R.layout.layout_for_file_list_item, null);
						holder = new ViewHolder();
						holder.textView = (TextView)convertView.findViewById(R.id.textView2);
						break;
				}	
				convertView.setTag(holder);	
			} else {
                holder = (ViewHolder)convertView.getTag();
            }
			holder.textView.setText(dir_contain.get(position));
			return convertView;
		}
		
	    /**
	     * Klasa pomocnicza dla metody {@link com.mplayer_remote.FileChooser.MyBaseAdapter#getView}. Implementacja wzorca projektowego ViewHolder.
	     * @author sokar
	     *
	     */
	    private class ViewHolder { 
	        public TextView textView;
	        public ImageView icon;
	    }
		
    }


}
