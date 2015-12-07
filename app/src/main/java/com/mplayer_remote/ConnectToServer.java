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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * Aktywność odpowiedzialna za łączenie z serwerem SSH oraz dostarczanie interfejsu umożliwiającego wysyłanie komend do tego serwera. 
 * 
 * @author sokar
 * @see android.app.Activity
 */

public class ConnectToServer extends Activity{
		//w celach diagnostycznych nazwa logu dla tego Activity
	private static final String TAG = "ConnectToServer";
		//to check is wi-fi or internet enable
	
	/**
	 * Obiekt klasy {@link ConnectToAsyncTask}.
	 */
	private AsyncTask myConnectToAsyncTask;
	/**
	 * Obiekt klasy {@link android.net.ConnectivityManager}, tu służący do uzyskiwania informacji o połączeniach sieciowych nawiązywanych przez urządzenie.
	 */
	private ConnectivityManager myConnectivityManager;
		// serwer data
	/**String zawierający nazwę serwera.*/
	private String serverNameString;
	/**String zawierający adres IP serwera.*/
	private String iPAddressString;
	/**String zawierający nazwę konta użytkownika zarejestrowanego na serwerze.*/
	private String usernameString;
	/**Tablica znaków zawierająca hasło do konta użytkownika zarejestrowanego na serwerze.*/
	private char[] serverPasswordchararray;
		//dialogs
	/**
	 * Okno dialogowe informujące o trwającym łączeniu z danym serwerem SSH.
	 */
	private ProgressDialog connectingDialog;
		
	/**
	 * Służy do zamykania okna dialogowego <code>connectingDialog</code> i kończenia AsyncTask oraz aktywności.
	 */
	private boolean running = true;
		
	/**
	 * Obiekt klasy {@link ch.ethz.ssh2.Connection} służący do nawiązywania szyfrowanego połączenia z serwerem SSH.
	 */
	private static Connection connection;  	// pole statyczne (klasowe) 
	
	/**
	 * Zmienna logiczna zawierająca informacje czy łączenie z serwerem SSH się powiodło.
	 */
	private Boolean isConnectedBoolean = false;
	
	/**
	 * Zawiera status wyjściowy ostatnio wykonywanej na serwerze komendy.
	 */
	private static int lastComandExitStatus;
	/*
	public static int getlastComandExitStatus(){
		return lastComandExitStatus;
	}
	public static void setlastComandExitStatus(int exitStatus){
		lastComandExitStatus = exitStatus;
	}
	*/
	
	
	/**
	 * AsyncTask odpowiedzialny za wyświetlanie okna dialogowego w czasie trwania łączenia z serwerem SSH, samo łączenie z serwerem (wykonywane w oddzielnym wątku) oraz wyświetlenie komunikatów w przypadku wystąpienia problemów. 
	 * @author sokar
	 */
	class ConnectToAsyncTask extends AsyncTask<String, Void, Boolean>{		
    	
	   	@Override
	   	protected void onPreExecute() {
	   			
		   		connectingDialog = ProgressDialog.show(ConnectToServer.this, "",getString(R.string.text_for_progressdialog_from_connecttoserver), true);
		   		
		   		connectingDialog.setCanceledOnTouchOutside(false);	//Zapobiega zamykaniu dialogu przy dotknięciach poza jego obrębem. 
		   		connectingDialog.setCancelable(true);
		   		connectingDialog.setOnCancelListener(new OnCancelListener() {
		            @Override
		            public void onCancel(DialogInterface dialog) {
		                cancel(true);
		                finish();
		            }
		        });
	    
	    }
	   		   	
	   	@Override
		protected Boolean doInBackground(String... params) {
	   				   				   		
	   		
	   			connectToServer(serverNameString, iPAddressString, usernameString, serverPasswordchararray);
	   			
	   		
			return isConnectedBoolean; // isConnected == true if connectToServer succes
		}

		@Override
		protected void onPostExecute(Boolean isConnectedBoolean){
			connectingDialog.dismiss();
			
			myConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	   		NetworkInfo info = myConnectivityManager.getActiveNetworkInfo();
	   		
	   		boolean isTetheringEnable = false;
	   		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	   		Method[] wmMethods = wifi.getClass().getDeclaredMethods();
		   	for(Method method: wmMethods){
			   	if(method.getName().equals("isWifiApEnabled")) {
			   		try {
			   			isTetheringEnable = (Boolean) method.invoke(wifi);
			   		} catch (IllegalArgumentException e) {
			   		  e.printStackTrace();
			   		} catch (IllegalAccessException e) {
			   		  e.printStackTrace();
			   		} catch (InvocationTargetException e) {
			   		  e.printStackTrace();
			   		}
			   	}
		   	}
	   		Log.v(TAG, "istetheringEnable" + isTetheringEnable);
		   	
	   		if (info == null && isTetheringEnable == false){		//wi-fi or date transfer not enabled nor wi-fi tethering
	   			Log.v(TAG, "Wi-fi ani transmisja danych ani tethering nie włączone ");
	   			//connectingDialog.dismiss();
	   			Toast.makeText(getApplicationContext(), R.string.text_for_toast_turn_wifi_or_3g_on_from_connecttoserver, Toast.LENGTH_LONG).show();	
	   			finish();
	   		}else if (isConnectedBoolean == true){	//connect succes
				//connectingDialog.dismiss();
				Log.v(TAG, "Połączono z serwerem");
				final Intent intent_start_FileChooser = new Intent(getApplicationContext(), FileChooser.class);
				startActivity(intent_start_FileChooser);
				finish();
			}else{
				Log.v(TAG, "Nie udało się połączyć z serwerem");
				//connectingDialog.dismiss();
				//dodadć dialog z press back to chose andoder server albo z informacją z exeption który wystąpił czyli przyczyne błędu
				Toast.makeText(getApplicationContext(), R.string.text_for_toast_check_server_data_from_connecttoserver, Toast.LENGTH_LONG).show();	
				finish();
			}
		}
		
		@Override
		protected void onCancelled() {
			Log.v(TAG, "wywołałem onCancelled klasy ConnectToAsyncTask");
			super.onCancelled();
		}
	}
	
	/**
	 * Metoda wywoływana przez system Android przy starcie aktywności, odpowiedzialna za łączenie z serwerem. 
	 * Definiuje i wywołuje metodę <code>execute</code> klasy wewnętrznej <code>ConnectToAsyncTask</code> dziedziczącej po <a href="http://developer.android.com/reference/android/os/AsyncTask.html">AsyncTask</a>. 
	 *  
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		
		Intent intentFromServerList = getIntent(); //getIntent() zwraca obiekt Intent który wystartował Activity
        serverNameString = intentFromServerList.getStringExtra("server_name");
        iPAddressString = intentFromServerList.getStringExtra("IP_address");
        usernameString = intentFromServerList.getStringExtra("username");
        serverPasswordchararray = intentFromServerList.getCharArrayExtra("password");
        Log.v(TAG, "Przekazany w intent server_name = " + serverNameString);
        Log.v(TAG, "Przekazany w intent IP_address = " + iPAddressString);
        Log.v(TAG, "Przekazany w intent username = " + usernameString);
        if (serverPasswordchararray != null)
        	Log.v(TAG, "Przekazany w intent server_password = " + new String(serverPasswordchararray));
                
		myConnectToAsyncTask = new ConnectToAsyncTask().execute();


	}
	
    @Override
    public void onPause(){

        super.onPause();
        if(connectingDialog != null)
        	connectingDialog.dismiss();
        
        myConnectToAsyncTask.cancel(true);
        
    }
	
	/**
	 * Zwraca obiekt klasy {@link ch.ethz.ssh2.Connection} odpowiedzialny za nawiązanie połączenia z danym serwerem SSH.
	 * @return obiekt klasy {@link ch.ethz.ssh2.Connection}
	 */
	private static Connection getConnection() {			//for acces from other android component such as Activity, static bo potrzebuje dostępu do pola statycznego klasy ConnectToServer
		return connection;
	}
	
	/**
	 * Nawiązuje połączenie z serwerem i uwierzytelnia na nim użytkownika. 
	 * Tworzy obiekt klasy {@link ch.ethz.ssh2.Connection} odpowiedzialny za nawiązanie połączenia z serwerem SSH o adresie IP <code> iPAddressString</code>.
	 * Następnie wywołuje metodę {@link ch.ethz.ssh2.Connection#connect} na rzecz tego obiektu i {@link ch.ethz.ssh2.Connection#authenticateWithPassword} z parametrami 
	 * <code>usernameString</code> i <code>serverPasswordchararray</code> odpowiedzialną za uwierzytelnienie użytkownika.
	 * @param serverNameString nazwa serwera.
	 * @param iPAddressString adres IP serwera.
	 * @param usernameString nazwa konta użytkownika zarejestrowanego na serwerze
	 * @param serverPasswordchararray hasło do konta użytkownika <code>usernameString</code> zarejestrowanego na serwerze.
	 */
	private void connectToServer (String serverNameString, String iPAddressString, String usernameString, char[] serverPasswordchararray){
		// łączenie z przekazanym przez Intent serwerem
		try
		{
			String justIPAddressString = "";
			int portint = 22;

			justIPAddressString = this.getIP(iPAddressString);
			portint = this.getPort(iPAddressString);

			/* Create a connection instance */

			connection = new Connection(justIPAddressString, portint);

			/* Now connect */

			connection.connect();

			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */

			boolean isAuthenticated = connection.authenticateWithPassword(usernameString, new String(serverPasswordchararray));
				//Zero out the password.
		    //Arrays.fill(serverPasswordchararray,'0');
		    //Arrays.fill(this.serverPasswordchararray,'0');
			
			
			
			if (isAuthenticated == false){
				isConnectedBoolean = false;
				throw new IOException("Authentication failed.");
			} else {
				isConnectedBoolean = true;
			}
		}catch (Exception e) {
			Log.v(TAG,"wystąpił błąd w connectToServer: " + e);
			e.printStackTrace(System.err);
			//System.exit(2);
		}	
    }

	private String getIP(String iPAddressString){
		String justIPAddressString = "";
		int numberOfColons = 0;
		for(int i = 0; i < iPAddressString.length(); i++){
			if (iPAddressString.charAt(i) == ':'){
				numberOfColons++;
			}
		}

		if (numberOfColons == 1 || numberOfColons == 8){
			justIPAddressString = iPAddressString.substring(0, iPAddressString.lastIndexOf(":"));
		}else{
			justIPAddressString = iPAddressString;
		}

		return justIPAddressString;
	}

	private int getPort(String iPAddressString){
		int portint = 22;
		int numberOfColons = 0;
		for(int i = 0; i < iPAddressString.length(); i++){
			if (iPAddressString.charAt(i) == ':'){
				numberOfColons++;
			}
		}

		if (numberOfColons == 1 || numberOfColons == 8){
			portint = Integer.parseInt(iPAddressString.substring(iPAddressString.lastIndexOf(":") + 1, iPAddressString.length()));
		}else{
			portint = 22;
		}

		return portint;
	}

	/**
	 * Wysyła do serwera polecenie <code>commandToSendString</code>.
	 * @param commandToSendString polecenie, które ma być wysłane do serwera.
	 */
	public static void sendCommand(String commandToSendString){
		try{
	    	/* Create a session */

			Session sess = ConnectToServer.getConnection().openSession();


			sess.execCommand(commandToSendString);

			Log.v(TAG,"wysłąłem komende: " + commandToSendString);

			//InputStream stdout = new StreamGobbler(sess.getStdout());

			//BufferedReader brForStdout = new BufferedReader(new InputStreamReader(stdout));

			//InputStream stderr = new StreamGobbler(sess.getStderr());

			//BufferedReader brForStderr = new BufferedReader(new InputStreamReader(stderr));

			/* Show exit status, if available (otherwise "null") */

			//System.out.println("ExitCode from sendCommand: " + sess.getExitStatus());




			/* Close this session */
			sess.close();

		}catch (Exception e) {
			//Log.v(TAG,"wystąpił błąd w sendCommandAndWaitForExitStatus: " + e);
			//e.printStackTrace(System.err);
			//System.exit(2);
		}


	}
	
	/**
	 * Wysyła do serwera polecenie <code>commandToSendString</code>. Zwrócony przez komendę <code>commandToSendString</code> status wyjścia zapisuje do pola lastComandExitStatus.
	 * @param commandToSendString polecenie, które ma być wysłane do serwera.
	 * @return lastComandExitStatus status wyjścia polecenia <code>commandToSendString</code>.
	 */
	public static int sendCommandAndWaitForExitStatus(String commandToSendString){
    	try{
	    	/* Create a session */
	
			Session sess = ConnectToServer.getConnection().openSession();
			
			sess.execCommand(commandToSendString);
			
			Log.v(TAG,"wysłąłem komende: " + commandToSendString);
			
			//InputStream stdout = new StreamGobbler(sess.getStdout());
			
			//BufferedReader brForStdout = new BufferedReader(new InputStreamReader(stdout));
			
			//InputStream stderr = new StreamGobbler(sess.getStderr());

			//BufferedReader brForStderr = new BufferedReader(new InputStreamReader(stderr));
			
			/* Show exit status, if available (otherwise "null") */

			while (sess.getExitStatus() == null){
				Thread.sleep(5);
			}
			lastComandExitStatus = sess.getExitStatus();
			System.out.println("ExitCode from sendCommandAndWaitForExitStatus: " + sess.getExitStatus());


			/* Close this session */
			sess.close();
			
    	}catch (Exception e) {
			//Log.v(TAG,"wystąpił błąd w sendCommandAndWaitForExitStatus: " + e);
			//e.printStackTrace(System.err);
			//System.exit(2);
		}
		return lastComandExitStatus;
    	
    }
	
	/**
	 * Wysyła do serwera polecenie <code>commandToSendString</code>, a zwrócone przez nie dane na <code>stdout</code> serwera zapisuje do lisyty <code>outputArrayList</code>.
	 * Zwrócony przez komendę <code>commandToSendString</code> status wyjścia zapisuje do pola lastComandExitStatus.
	 * @param commandToSendString polecenie, które ma być wysłane do serwera.
	 * @param outputArrayList lista, do której zostaną zapisane dane zwrócone przez polecenie <code>commandToSendString</code> na <code>stdout</code> serwera.
	 */
	public static void sendCommandAndSaveOutputToArrayList(String commandToSendString, ArrayList<String> outputArrayList){
		
		try{
	    	/* Create a session */
	
			Session sess = ConnectToServer.getConnection().openSession();
			
			sess.execCommand(commandToSendString);
			
			Log.v(TAG,"wysłąłem komende: " + commandToSendString);
			
			InputStream stdout = new StreamGobbler(sess.getStdout());
	
			BufferedReader brForStdout = new BufferedReader(new InputStreamReader(stdout));
	
			InputStream stderr = new StreamGobbler(sess.getStderr());

			BufferedReader brForStderr = new BufferedReader(new InputStreamReader(stderr));
			
			while (true)
			{
				String line = brForStdout.readLine();
				if (line == null)
					break;
				
				//ServicePlayAFile.mplayerOutputArrayListLock.lock();
				//try
				//{
					System.out.println(line);
					outputArrayList.add(line);
					//ServicePlayAFile.newMplayerOutputCondition.signalAll();
				//}
				//finally
				//{
					//ServicePlayAFile.mplayerOutputArrayListLock.unlock();
				//}
					
			}

			while (true)
			{
				String line = brForStderr.readLine();
				if (line == null)
					break;
				System.out.println(line);
			}
			
			/* Show exit status, if available (otherwise "null") */

			if (sess.getExitStatus() != null){
				lastComandExitStatus = sess.getExitStatus();
			}else{
				lastComandExitStatus = -1;
			}

			System.out.println("ExitCode from sendCommandAndSaveOutputToArrayList: " + sess.getExitStatus());

			/* Close this session */
			sess.close();
    	}catch (Exception e) {
			Log.v(TAG,"wystąpił błąd w sendCommandAndSaveOutputToArrayList: " + e);
			e.printStackTrace(System.err);
			//System.exit(2);
		}
    	
    }
	
	/**
	 * Wysyła do serwera polecenie <code>commandToSendString</code>, a zwrócone przez nie dane na <code>stdout</code> serwera zapisuje do lisyty o synchronizowanym dostępie <code>outputArrayList</code>.
	 * Zwrócony przez komendę <code>commandToSendString</code> status wyjścia zapisuje do pola lastComandExitStatus.
	 * @param commandToSendString polecenie, które ma być wysłane do serwera.
	 * @param outputArrayList lista, do której zostaną zapisane dane zwrócone przez polecenie <code>commandToSendString</code> na <code>stdout</code> serwera.
	 * @param outputArrayListLock zamek <a href=''http://docs.oracle.com/javase/7/docs/api/''> Lock </a> umożliwiający kontrolowanie dostępu przez wątki aplikacji do współdzielonej listy <code>outputArrayList</code>.
	 * @param newOutputCondition warunek <a href=''http://docs.oracle.com/javase/7/docs/api/''>Condition </a> informujący wątki o pojawieniu się nowych danych na liście <code>outputArrayList</code>.
	 */
	public static void sendCommandAndSaveOutputToLockedArrayList(String commandToSendString, ArrayList<String> outputArrayList, Lock outputArrayListLock, Condition newOutputCondition){
		
		try{
	    	/* Create a session */
	
			Session sess = ConnectToServer.getConnection().openSession();
			
			sess.execCommand(commandToSendString);
			
			Log.v(TAG,"wysłąłem komende: " + commandToSendString);
			
			InputStream stdout = new StreamGobbler(sess.getStdout());
	
			BufferedReader brForStdout = new BufferedReader(new InputStreamReader(stdout));
	
			InputStream stderr = new StreamGobbler(sess.getStderr());

			BufferedReader brForStderr = new BufferedReader(new InputStreamReader(stderr));
			
			while (true)
			{
				String line = brForStdout.readLine();
				if (line == null)
					break;
				
				outputArrayListLock.lock();
				try
				{
					System.out.println(line);
					outputArrayList.add(line);
					newOutputCondition.signalAll();
				}
				finally
				{
					outputArrayListLock.unlock();
				}
					
			}

			while (true)
			{
				String line = brForStderr.readLine();
				if (line == null)
					break;
				System.out.println(line);
			}
			
			/* Show exit status, if available (otherwise "null") */
	
			System.out.println("ExitCode sendCommandAndSaveOutputToLockedArrayList: " + sess.getExitStatus());
			if (sess.getExitStatus() != null){
				lastComandExitStatus = sess.getExitStatus();
			}else{
				lastComandExitStatus = -1;
			}
			System.out.println("ExitCode sendCommandAndSaveOutputToLockedArrayList: " + sess.getExitStatus());

			//RemoteControl.RemoteControlActivityObject.finish();

			/* Close this session */
			sess.close();
    	}catch (Exception e) {
			Log.v(TAG,"wystąpił błąd w sendCommandAndSaveOutputToArrayList: " + e);
			e.printStackTrace(System.err);
			//System.exit(2);
		}
    	
    }

}
