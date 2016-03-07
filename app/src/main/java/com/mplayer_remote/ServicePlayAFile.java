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

import android.app.IntentService;

import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Usługa odpowiedzialna za włączenie na serwerze odtwarzania, w programie <code>MPlayer</code>, wybranego przez użytkownika pliku multimedialnego oraz uruchomienie aktywności <code>RemoteControl</code>.
 * Usługa wyświetla w oknie wiadomości systemu <code>Android</code> komunikat informujący o trwającym odtwarzaniu oraz ikonę aplikacji <code>MPlayer Remote</code> na pasku stanu.
 * <code>ServicePlayAFile</code> zakłada w czasie swojego działania blokadę uniemożliwiając automatyczne wyłączenie sieci <code>WiFi</code>.
 *
 * @author sokar
 *
 */
public class ServicePlayAFile extends IntentService{
	private String TAG = "ServicePlayAFile";

	/**
	 * Boolean used to stop executing intents sended to IntentService.onHandleIntent() from FileChooser in case when user long click on directory.
	 */
	private boolean stopExecuteIntents = false;

	private NotificationManager mNotificationManager;

	/**
	 * Łańcuch znaków zawierający pełna nazwę wraz ze ścieżką absolutną prowadzącą do pliku wskazanego przez użytkownika w celu odtworzenia go w programie MPlayer.
	 */
	private String fileToPlayString;

	/**
	 * Łańcuch znaków zawierający ścieżkę absolutną, w której znajduje się plik wskazany przez użytkownika do odtworzenia w programie MPlayer.
	 */
	private String absolutePathString;

	/**
	 * Lista, do której zostaną zapisane dane zwrócone przez polecenie <code>export DISPLAY=:0.0 && mplayer -slave -quiet -input file=fifofile fileToPlayString</code> na <code>stdout</code> serwera.
	 */
	protected static ArrayList<String> mplayerOutputArrayList = new ArrayList<String>();

	/**
	 * Zamek <a href=''http://docs.oracle.com/javase/7/docs/api/''> Lock </a> umożliwiający kontrolowanie dostępu przez wątki aplikacji do współdzielonej listy {@link com.mplayer_remote.ServicePlayAFile#mplayerOutputArrayList}.
	 */
	protected static Lock mplayerOutputArrayListLock = new ReentrantLock(true);		//true means this is a fair lock

	/**
	 * Warunek <a href=''http://docs.oracle.com/javase/7/docs/api/''>Condition </a> informujący wątki o pojawieniu się nowych danych na liście {@link com.mplayer_remote.ServicePlayAFile#mplayerOutputArrayList}.
	 */
	protected static Condition newMplayerOutputCondition = mplayerOutputArrayListLock.newCondition();

	/**
	 * Zamek <a href=''http://docs.oracle.com/javase/7/docs/api/''> Lock </a> umożliwiający kontrolowanie dostępu przez wątki aplikacji do pliku fifofile znajdującego się na serwerze SSH.
 	 */
	protected static Lock fifofileLock = new ReentrantLock(true);

	/**
	 * Służy do zakładania blokady uniemożliwaijącej automatyczne wyłączanie sieci WiFi.
	 * @see android.net.wifi.WifiManager.WifiLock
	 */
	private WifiLock wifiLock;

	private int mId;		//for notyfication

	public ServicePlayAFile() {
		super("ServicePlayFile");
	}

	/**
	 * Metoda uruchamiana po przekazaniu do usługi dziedziczącej po <code>IntentService</code> wiadomości <code>Intent</code>, w tym wypadku zawierającej nazwę i ścieżkę do pliku multimedialnego wybranego przez użytkownika.
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {


		Log.v(TAG, "stopExecuteIntents = " + stopExecuteIntents);

		if( stopExecuteIntents == false) {

			fileToPlayString = intent.getStringExtra("file_to_play");
			Log.v(TAG, "file_to_play przekazane przez intent z FileChoosera: " + fileToPlayString);
			absolutePathString = intent.getStringExtra("absolute_path");
			Log.v(TAG, "absolute_path przekazane przez intent z FileChoosera: " + absolutePathString);

			showNotyfications();

			WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "MyWifiLock");
			if (!wifiLock.isHeld()) {
				wifiLock.acquire();
			}

			if (isfifofileExist() == false){ // isfifofileExist() luck for a true fifo
				while (ConnectToServer.sendCommandAndWaitForExitStatus("mkfifo fifofile") != 0) {
					ConnectToServer.sendCommand("rm fifofile");
				}
			}

			Intent intent_start_RemoteControl = new Intent(getApplicationContext(), RemoteControl.class);
			intent_start_RemoteControl.putExtra("file_to_play", fileToPlayString);
			intent_start_RemoteControl.putExtra("absolute_path", absolutePathString);
			intent_start_RemoteControl.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//intent_start_RemoteControl.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			startActivity(intent_start_RemoteControl);


			ConnectToServer.sendCommandAndSaveOutputToLockedArrayList("export DISPLAY=:0.0 && mplayer -fs -slave -quiet -input file=fifofile " + "\"" + fileToPlayString + "\"", mplayerOutputArrayList, mplayerOutputArrayListLock, newMplayerOutputCondition);

			//RemoteControl.RemoteControlActivityObject.finish();

			//ConnectToServer.sendCommand("rm fifofile");

		}
	}
	/**
	 * Usuwa z serwera plik <code>fifofile</code> odpowiedzialny za przekazywanie poleceń do odtwarzacza MPlayer oraz zdejmuje blokadę uniemożliwiającą wyłączenie sieci WiFi urządzenia mobilnego. Usuwa też wiadomość z okna wiadomości.
	 * @see android.app.IntentService#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Log.v(TAG, "Android call ServicePlayAfile.onDestroy");
		stopExecuteIntents = true;		// for stopService(new Intent(getApplicationContext(),com.mplayer_remote.ServicePlayAFile.class)); to work propery, stop playing directory content.

		//if (isfifofileExist() == true) {
			ConnectToServer.sendCommand("rm fifofile");
		//}

		wifiLock.release();

		mNotificationManager.cancelAll();

	}

	private void showNotyfications(){

		int positionOfLastDot = fileToPlayString.lastIndexOf(".");
		String filenameExtension = fileToPlayString.substring(positionOfLastDot + 1);

		int positionOfLastDashint = fileToPlayString.lastIndexOf("/");
		String secondLineOfNotification = fileToPlayString.substring(positionOfLastDashint + 1);

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, RemoteControl.class);
		resultIntent.putExtra("file_to_play", fileToPlayString);
		resultIntent.putExtra("absolute_path", absolutePathString);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(RemoteControl.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);

		//for adding fileToPlayString and absolutePathString to intents form stackBuilder
		for( int i = 0; i < stackBuilder.getIntentCount(); i++ ){
			Intent intentToEdit = stackBuilder.editIntentAt(i);
			intentToEdit.putExtra("file_to_play", fileToPlayString);
			intentToEdit.putExtra("absolute_path", absolutePathString);
		}

		PendingIntent resultPendingIntent =	stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentTitle(getString(R.string.text_for_first_line_of_notification_from_serviceplayafile))
						.setContentText(secondLineOfNotification);

		mBuilder.setContentIntent(resultPendingIntent);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(mId, mBuilder.build());

	};

	private boolean isfifofileExist() {

		ArrayList<String> fifoFilesArrayList= new ArrayList<String>();
		ConnectToServer.sendCommandAndSaveOutputToArrayList("ls --file-type|grep fifofile'|'", fifoFilesArrayList);
		if (fifoFilesArrayList.size() == 1){
			return true;
		}else {
			return false;
		}

	}

	private String escapeEveryCharacterInGivenString (String stringToEscape){
		String escapedString = "";
		for (int i = 0; i < stringToEscape.length(); i++){
			escapedString = escapedString + "\\" + Character.toString(stringToEscape.charAt(i));
		}

		return escapedString;
	}



}


