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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.mplayer_remote.R.drawable;

import java.util.ArrayList;

/**
 * Aktywność wyświetlająca przyciski oraz pasek przewijania pozwalające na sterowanie odtwarzaniem pliku multimedialnego.
 * Graficzny interfejs użytkownika tej aktywności wyświetla również informacje o długości odtwarzanego pliku i miejscu, w którym obecnie znajduje się odtwarzanie. 
 * Menu aktywności zawiera pozycje, której wybranie uruchamia aktywność <code>SubtitleFileChooser</code>.
 * @author sokar
 * @see android.app.Activity
 */
public class RemoteControl extends Activity{
	
		//w celach diagnostycznych nazwa logu dla tego Activity
	private static final String TAG = "RemoteControl";

	//public static Activity RemoteControlActivityObject;

	/**
	 * Progress message number. For distinguish from other messages.
	 * @see http://developer.android.com/reference/android/os/Handler.html#obtainMessage(int)
	 */
	private static final int WHAT_FOR_PROGRESS_MESSAGE = 0;

	/**
	 * Łańcuch znaków zawierający ścieżkę absolutną, w której znajduje się plik wskazany przez użytkownika.
	 */
	//private String absolutePathString;
	
	/**
	 * Łańcuch znaków zawierający pełna nazwę wraz ze ścieżką absolutną do pliku wskazanego przez użytkownika. 
	 */
	private String fileToPlayString;
	
	/**
	 * Długość pliku multimedialnego wyrażona w sekundach.  
	 */
	private int timeLengthInSecondsint;
	
	/**
	 * Ilość sekund liczonych od początku pliku multimedialnego do miejsca, które jest aktualnie odtwarzane.   
	 */
	private int timePositionInSecondsint;
	
	/**
	 * Zmienna logiczna przechowująca informacje czy pole tekstowe <code>TimeLengthTextView </code>zostało już ustawione przez wątek <code>read_time_lengthThread</code>.  
	 */
	private boolean isTimeLengthTextViewSetboolean = false;		// read_time_lengthThread stops when true
	
	/**
	 * Łańcuch znaków zawierający długość pliku multimedialnego wyrażoną takiej samej notacji jakiej używa MPlayer czyli <code>godziny:minuty:sekundy</code>.
	 */
	private String timeLengthMPlayerLikeString;
	
	/**
	 * Łańcuch znaków zawierający czas, wyrażony w notacji używanej przez odtwarzacz MPlayer (<code>godziny:minuty:sekundy</code>),
	 * który upłynął od początku pliku multimedialnego do miejsca obecnie odtwarzanego.
	 */
	private String timePositionMPlayerLikeString;
	
	/**
	 * Wątek utworzony z klasy <code>AskMplayerRunnable</code> implementującej interfejs <code>Runnable</code>, wysyłający do Mplayer' a polecenia <code>get_time_length</code>, <code>get_time_pos</code> i <code>get_percent_pos</code>.
	 * @see com.mplayer_remote.RemoteControl.AskMplayerRunnable
	 */
	private Thread askMplayerThread;
	
	/**
	 * Wątek utworzony z klasy <code>readProgressRunnable</code> implementującej interfejs <code>Runnable</code>.
	 * @see com.mplayer_remote.readProgressRunnable
	 */
	private Thread readProgressThread;
	
	/**
	 *  Wątek utworzony z klasy <code>readTimeLengthRunnable</code> implementującej interfejs <code>Runnable</code>.
	 *  @see com.mplayer_remote.readTimeLengthRunnable
	 */
	private Thread readTimeLengthThread;
	
	/**
	 * Wątek utworzony z klasy <code>readTimePositionRunnable</code> implementującej interfejs <code>Runnable</code>.
	 * @see com.mplayer_remote.readTimePositionRunnable
	 */
	private Thread readTimePositionThread;
	
	/**
	 * Pasek przewijania pokazujący postęp odtwarzania. Udostępnia on również możliwość przewijania odtwarzacza do dowolnego miejsca.  
	 */
	private SeekBar seekBar;
	
	/**
	 * Pole tekstowe wyświetlające łańcuch znaków zawierający długość pliku multimedialnego wyrażoną takiej samej notacji jakiej używa MPlayer czyli <code>godziny:minuty:sekundy</code>.
	 */
	private TextView timeLengthTextView;
	
	/**
	 * Pole tekstowe wyświetlające łańcuch znaków zawierający czas, wyrażony w notacji używanej przez odtwarzacz MPlayer (<code>godziny:minuty:sekundy</code>),
	 * który upłynął od początku pliku multimedialnego do miejsca obecnie odtwarzanego.
	 */
	private TextView timePositionTextView;

	/**
	 * TextView with title of now played file.
	 */
	private TextView nowPlayFileNameTextView;

	/**
	 * Przycisk po naciśnięciu, którego do odtwarzacza MPlayer zostanie wysłane polecenie <code>pausing_keep pause</code> nakazujące wstrzymanie odtwarzacza.
	 */
	private Button pauseButton;
	
	/**
	 * Zmienna logiczna przechowująca informacje jaką ikonę wyświetla przycisk <code>pauseButon</code>. 
	 * <code>True</code> w przypadku ikony symbolizującej polecenie wstrzymania odtwarzania, <code>False</code> dla ikony symbolizującej polecenie wznowienia odtwarzania. 
	 */
	private Boolean showingPlayButtonboolean = false;
	
	/**
	 * Interfejs służący do przechowywania preferencji, w tym przypadku stanu GUI aktywności.
	 * @see android.content.SharedPreferences  
	 */
	private SharedPreferences sharedPreferencesForActivityRemotControl;		//for saving UI persistant state
	
	/**
	 * <code>Handler</code> służący do przekazywania wiadomości z wątku <code>readProgressThread</code> do głównego wątku aplikacji (<code>UI thread</code>) zarządzającego GUI aktywności <code>RemoteControl</code>.
	 * @see android.os.Handler
	 */
	private Handler progressHandler;
	
	/**
	 * <code>Handler</code> służący do przekazywania wiadomości z wątku <code>readTimeLengthThread</code> do głównego wątku aplikacji (<code>UI thread</code>) zarządzającego GUI aktywności <code>RemoteControl</code>.
	 * @see android.os.Handler
	 */
	private Handler timeLengthTextViewUpdateHandler;
	
	/**
	 * <code>Handler</code> służący do przekazywania wiadomości z wątku <code>readTimePositionThread</code> do głównego wątku aplikacji (<code>UI thread</code>) zarządzającego GUI aktywności <code>RemoteControl</code>.
	 * @see android.os.Handler
	 */
	private Handler timePositionUpdateHandler;

	/**
	 * Służy do zarządzania Lock Screen' em.
	 */
	private KeyguardManager keyguardManager;
	
	/**
	 * Służy do zarządzania Lock Screen' em.
	 */
	private KeyguardLock lock;

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
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	
	/**Metoda wywoływana przez system Android przy starcie aktywności.
	 * Wczytuje definicje GUI z pliku XML. Definiuje akcje wywoływane poprzez interakcji użytkownika z graficznym interfejsem użytkownika aktywności.
	 * Definiuje <code>progressHandler</code>, <code>timeLengthTextViewUpdateHandler</code>, <code>timePositionUpdateHandler</code>. 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */

	public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		 getActionBar().setDisplayHomeAsUpEnabled(false);

		 //RemoteControlActivityObject = this;

		 Intent intentFromstartActivity = getIntent(); //getIntent() zwraca obiekt Intent który wystartował Activity
		 //absolutePathString = intentFromstartActivity.getStringExtra("absolute_path");
		 fileToPlayString = intentFromstartActivity.getStringExtra("file_to_play");
	     //Log.v(TAG, "absolute_path przekazane przez intent z ServicePlayAFile: " + absolutePathString);
	     Log.v(TAG, "file_to_play przekazane przez intent z ConnectAndPlayService: " + fileToPlayString);
	     
	     	//gui
	     setContentView(R.layout.layout_for_remotecontrol);
	    	     
    	 final Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    	 
    	 keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
    	 lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
	     
	     Button fullscreenButton = (Button) findViewById(R.id.fullscreean_button);
	     fullscreenButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					if( mBound == true ) {
						mConnectAndPlayService.sendCommand("echo pausing_keep_force vo_fullscreen > fifofile");
						mVibrator.vibrate(50);
					}
				
			}
	     });

	     Button stopButton = (Button) findViewById(R.id.stop_button);
	     stopButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					//askMplayerThread.interrupt();
				if( mBound == true ) {
					mConnectAndPlayService.stopPlaying();
					//mConnectAndPlayService.sendCommand("echo stop > fifofile");
					//ConnectToServer.sendCommand("rm fifofile");
					//stopService(new Intent(getApplicationContext(), com.mplayer_remote.ServicePlayAFile.class));
					finish();
					mVibrator.vibrate(50);
				}
				
			}
		 });

		Button previous_media_button = (Button) findViewById(R.id.previous_media_button);
		previous_media_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				if(mBound == true){
					mConnectAndPlayService.playPreviousMedia();
					mVibrator.vibrate(50);
				}
			}
		});

		Button next_media_button = (Button) findViewById(R.id.next_media_button);
		next_media_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//askMplayerThread.interrupt();
				if( mBound == true ) {
					mConnectAndPlayService.playNextMedia();
					//mConnectAndPlayService.sendCommand("echo stop > fifofile");
					//ConnectToServer.sendCommand("rm fifofile");
					//RemoteControlActivityObject.finish();
					mVibrator.vibrate(50);
				}

			}
		});
	     
	     pauseButton = (Button) findViewById(R.id.pause_button);
	     
	     sharedPreferencesForActivityRemotControl = getSharedPreferences("RemoteControl_activity_state", 0);
	     showingPlayButtonboolean = sharedPreferencesForActivityRemotControl.getBoolean("showing_play_button", false);
    	  
	     pauseButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if( mBound == true ) {
					mConnectAndPlayService.sendCommand("echo pausing_keep pause > fifofile");
					mVibrator.vibrate(50);
					if (showingPlayButtonboolean == false) {
						pauseButton.setBackgroundColor(000000);
						pauseButton.setCompoundDrawablesWithIntrinsicBounds(drawable.play_button, 0, 0, 0);
						showingPlayButtonboolean = true;
					} else {
						pauseButton.setBackgroundColor(000000);
						pauseButton.setCompoundDrawablesWithIntrinsicBounds(drawable.pause_button, 0, 0, 0);
						showingPlayButtonboolean = false;
					}

				}
			}
	     });

	     Button step10SecondForwardButton = (Button) findViewById(R.id.step_10_second_forward_button);
	     step10SecondForwardButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if( mBound == true ) {
					mConnectAndPlayService.sendCommand("echo pausing_keep_force seek 10  > fifofile");
					mConnectAndPlayService.sendCommand("echo pausing_keep osd_show_progression > fifofile");
					mVibrator.vibrate(50);
				}
			}
		 });

	     Button step10SecondBackwardButton = (Button) findViewById(R.id.step_10_second_backward_button);
	     step10SecondBackwardButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if( mBound == true ) {
					mConnectAndPlayService.sendCommand("echo pausing_keep_force seek -10  > fifofile");
					mConnectAndPlayService.sendCommand("echo pausing_keep osd_show_progression > fifofile");
					mVibrator.vibrate(50);
				}
			}
		 });
	     
	     seekBar = (SeekBar) findViewById(R.id.seekBar);
	 
	     seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			 @Override
			 public void onStartTrackingTouch(SeekBar seekBar) {
				 // TODO Auto-generated method stub
				 // ServicePlayAFile.mplayerOutputArrayListLock.lock();
			 }

			 @Override
			 public void onStopTrackingTouch(SeekBar seekBar) {
				 // TODO Auto-generated method stub

				 //ServicePlayAFile.mplayerOutputArrayListLock.unlock();
			 }

			 @Override
			 public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				 if (fromUser == true) {
					 if( mBound == true ) {
						 mConnectAndPlayService.sendCommand("echo pausing_keep seek " + progress + " 1 > fifofile");

						 mConnectAndPlayService.sendCommand("echo pausing_keep osd_show_progression > fifofile");

						 progressHandler.removeCallbacksAndMessages(null);
						 progressHandler.removeMessages(WHAT_FOR_PROGRESS_MESSAGE);
					 }
				 }
			 }
		 });
	     
		 timePositionTextView = (TextView) findViewById(R.id.time_position_textView);
			     
		 timeLengthTextView = (TextView) findViewById(R.id.time_length_textView);

		 nowPlayFileNameTextView = (TextView) findViewById(R.id.now_play_textView);
		 int positionOfLastDashint = fileToPlayString.lastIndexOf("/");
		 String substringfileToPlayString = fileToPlayString.substring(positionOfLastDashint + 1);
		 nowPlayFileNameTextView.setText(substringfileToPlayString);


		 progressHandler = new Handler() {
		     public void handleMessage(Message msg) {
		         int progress = msg.arg1;
		         //int state = msg.arg2;
		         seekBar.setProgress(progress);

		         
		         if (progress >= 100){
		            
		             
		        	 readProgressThread.interrupt();
		         }
		     };
		 }; 
	     
		timeLengthTextViewUpdateHandler = new Handler() {
		    public void handleMessage(Message msg) {
		        timeLengthInSecondsint = msg.arg1;
		        int hours = timeLengthInSecondsint / 3600;
		        int minuts = (timeLengthInSecondsint - (hours * 3600)) / 60;
		        int seconds = timeLengthInSecondsint - (hours * 3600) - (minuts * 60);
		        if (minuts < 10 && seconds > 10){
		        	timeLengthMPlayerLikeString = hours + ":0" + minuts + ":" + seconds;
		        }else if (minuts < 10 && seconds < 10){
		        	timeLengthMPlayerLikeString = hours + ":0" + minuts + ":0" + seconds;
		        }else if (minuts > 10 && seconds > 10){
		        	timeLengthMPlayerLikeString = hours + ":" + minuts + ":" + seconds;
		        }else if (minuts > 10 && seconds < 10){	
		        	timeLengthMPlayerLikeString = hours + ":" + minuts + ":0" + seconds;
		        }
		        Log.v(TAG, "timeLength in timeLengthUpdateHandler is : " + timeLengthMPlayerLikeString);
		        timeLengthTextView.setText(timeLengthMPlayerLikeString);
		        isTimeLengthTextViewSetboolean = true;
		        
		    };
		};
		
		timePositionUpdateHandler = new Handler() {
	        public void handleMessage(Message msg) {
	        	timePositionInSecondsint = msg.arg1;
	            int hours = timePositionInSecondsint / 3600;
	            int minuts = (timePositionInSecondsint - (hours * 3600)) / 60;
	            int seconds = timePositionInSecondsint - (hours * 3600) - (minuts * 60);
	            if (minuts < 10 && seconds > 10){
	            	timePositionMPlayerLikeString = hours + ":0" + minuts + ":" + seconds;
	            }else if (minuts < 10 && seconds < 10){
	            	timePositionMPlayerLikeString = hours + ":0" + minuts + ":0" + seconds;
	            }else if (minuts > 10 && seconds > 10){
	            	timePositionMPlayerLikeString = hours + ":" + minuts + ":" + seconds;
	            }else if (minuts > 10 && seconds < 10){	
	            	timePositionMPlayerLikeString = hours + ":" + minuts + ":0" + seconds;
	            }
	            
	            Log.v(TAG, "timePositionInSecondsMPlayerLikeString is : " + timePositionMPlayerLikeString);
	            timePositionTextView.setText(timePositionMPlayerLikeString);
	        };
		}; 
	}
	

	@Override
	protected void onNewIntent(Intent intent) {		//this will be called when activity RemoteControl is started true ServicePlayAFile when user touch a notification or when ServicePlayAFile start to handle new Intent
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
		//absolutePathString = intent.getStringExtra("absolute_path");
		//Log.v(TAG, "a onNewIntent was called and it recaive a absolute_path from ServicePlayAFile: " + absolutePathString);

		//for updating nowPlayFileNameTextView when starting to play a next media form long presed directory
		fileToPlayString = intent.getStringExtra("file_to_play");

		if (fileToPlayString != null) {
			nowPlayFileNameTextView = (TextView) findViewById(R.id.now_play_textView);
			int positionOfLastDashint = fileToPlayString.lastIndexOf("/");
			String substringfileToPlayString = fileToPlayString.substring(positionOfLastDashint + 1);
			nowPlayFileNameTextView.setText(substringfileToPlayString);
		}
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

	/**
	 * Metoda wywoływana przez system Android przy wznawianiu wyświetlania aktywności. 
	 * Uruchamia wątki <code>askMplayerThread</code>, <code>readTimeLengthThread</code>, <code>readTimePositionThread</code>, <code>readProgressThread</code>.
	 * Na podstawie zmiennej <code>showingPlayButtonboolean</code>  ustawia ikonę wyświetlaną przez przycisk <code>pauseButton</code>.
	 * @see android.app.Activity#onResume()
	 */
	@Override
    protected void onResume() {
        super.onResume();
        
        askMplayerThread = new Thread(new AskMplayerRunnable()); 
        askMplayerThread.start();
        
	    if (isTimeLengthTextViewSetboolean == false){
	    	readTimeLengthThread = new Thread(new ReadTimeLengthRunnable(timeLengthTextViewUpdateHandler));
	    	readTimeLengthThread.start();
	    }
	     
	    readTimePositionThread = new Thread(new ReadTimePositionRunnable(timePositionUpdateHandler));
	    readTimePositionThread.start();

        readProgressThread = new Thread(new ReadProgressRunnable(progressHandler));
	    readProgressThread.start();
	    
	    	// Restore preferences
	 	if (isMyServiceRunning() == false)
	 		finish();
	 	
	    showingPlayButtonboolean = sharedPreferencesForActivityRemotControl.getBoolean("showing_play_button", false);
   	 	if (showingPlayButtonboolean == true){
	    	 pauseButton.setBackgroundColor(000000);
	    	 pauseButton.setCompoundDrawablesWithIntrinsicBounds(drawable.play_button, 0, 0, 0);
	    }
   	 	
   	 		//wyłącza Lock Screen
   	 	lock.disableKeyguard();
	}
	
	
    /** 
     * Metoda wywoływana przez system Android, kiedy aktywność przestaje być wyświetlana na ekranie urządzenia.
     * Zatrzymuje wątki <code>askMplayerThread</code>, <code>readTimeLengthThread</code>, <code>readTimePositionThread</code>, <code>readProgressThread</code>.
     * Zapisuje za pomocą interfejsu <code>sharedPreferencesForActivityRemotControl</code> informacje jaką ikonę wyświetla przycisk <code>pauseButton</code>. 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        isTimeLengthTextViewSetboolean = false;
        askMplayerThread.interrupt();
        readTimeLengthThread.interrupt();
        readTimePositionThread.interrupt();
        readProgressThread.interrupt();
        
        	//Saving preferences
        SharedPreferences.Editor editor = sharedPreferencesForActivityRemotControl.edit();
        editor.putBoolean("showing_play_button", showingPlayButtonboolean);
        editor.commit();
        
        //absolutePathString = null;
        
        	//włącza Lock Screen
        lock.reenableKeyguard();
    }

	/*
	@Override
	public void onBackPressed() {        //when user press a back key

			ConnectToServer.sendCommand("echo stop > fifofile");
			stopService(new Intent(getApplicationContext(), com.mplayer_remote.ServicePlayAFile.class));
			RemoteControlActivityObject.finish();


	}
	*/

    /**
     * Metoda wywoływana przez system Android w reakcji na pierwsze wybranie przycisku menu urządzenia. Funkcja odpowiedzialna za wczytanie pliku XML z definicją menu aktywności.
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_for_remote_control, menu);
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
	    case R.id.menu_item_load_subtitle:
	    	Intent intentStartSubtitleFileChooser = new Intent(getApplicationContext(), SubtitleFileChooser.class);
	    	//intentStartSubtitleFileChooser.putExtra("absolute_path", absolutePathString);
			intentStartSubtitleFileChooser.putExtra("file_to_play", fileToPlayString);
	    	//Log.v(TAG, "Starting SubtitleFileChooser with absolute_path = " + absolutePathString);
			Log.v(TAG, "Starting SubtitleFileChooser with file_to_play = " + fileToPlayString);
			startActivity(intentStartSubtitleFileChooser);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	/*
	 @Override
	protected void onDestroy() {
		 super.onDestroy();
		 
	}
	*/

	/**
	 * Metoda wywoływana w reakcji na wciśniecie, któregoś z przycisków fizycznych urządzenia. 
	 * W tym wypadku definiuje akcje wykonywane, kiedy użytkownik wciśnie przycisk pogłaśniania i przyciszania.  
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyUp(keyCode, event);
		//onBackPressed();
		if ((keyCode == KeyEvent.KEYCODE_BACK))
			onBackPressed();
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
			if (mBound == true) {
				mConnectAndPlayService.sendCommand("echo key_down_event 42 > fifofile");
			}
		    return true; //because I handled the event
		}
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
			if (mBound == true) {
				mConnectAndPlayService.sendCommand("echo key_down_event 47 > fifofile");
			}
			return true;
		}
		return false; //otherwise the system can handle it        
	}


	/**
	 * Sprawdza czy usługa ConnectAndPlayService działa w tle.
	 * @return <code>true</code> jeśli usługa ConnectAndPlayService działa w tle, <code>false</code> w przeciwnym wypadku.
	 */
	private boolean isMyServiceRunning() {

		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.mplayer_remote.ConnectAndPlayService".equals(service.service.getClassName())) {
				return true;
			}
		}
		this.finish(); //to finish activity when user close mplayer by other whey
		return false;

	}



	/**
	 * Klasa reprezentująca komendę, którą można wykonać np. w oddzielnym wątku.
	 * Wątek utworzony z klasy <code>AskMplayerRunnable</code> implementującej interfejs <code>Runnable</code>, wysyła do Mplayer' a polecenia <code>get_time_length</code>, <code>get_time_pos</code> i <code>get_percent_pos</code>.  
	 * @author sokar
	 *
	 */
	private class AskMplayerRunnable implements Runnable {

		@Override
		public void run() {
			
			while (isMyServiceRunning() == true){
				if (isTimeLengthTextViewSetboolean == false) {
					if (mBound == true) {
						mConnectAndPlayService.sendCommand("echo pausing_keep_force get_time_length > fifofile");
					}
				}


				if (mBound == true) {
					mConnectAndPlayService.sendCommand("echo pausing_keep_force get_time_pos > fifofile");
				}



				try {
            		Thread.sleep(500);
        		} catch (InterruptedException e) {
                    Log.e("ERROR", "Thread Interrupted");
                    break;
                }

				if (mBound == true) {
					mConnectAndPlayService.sendCommand("echo pausing_keep_force get_percent_pos > fifofile");
				}

				try {
            		Thread.sleep(500);
        		} catch (InterruptedException e) {
                    Log.e("ERROR", "Thread Interrupted");
                    break;
                }

			}
			
		}
		
	}
	 
	
	/**
	 * Klasa reprezentująca komendę, którą można wykonać np. w oddzielnym wątku.
	 * Wątek utworzony z klasy <code>ReadTimeLengthRunnable</code> odczytuje z listy {@link com.mplayer_remote.ConnectAndPlayService#mplayerOutputArrayList}
	 * odpowiedź od odtwarzacza <code>MPlayer</code> zawierającą informacje, wyrażoną w sekundach, o długości odtwarzanego pliku multimedialnego. 
	 * Informacja ta poprzez <code>Handler</code> {@link RemoteControl#timeLengthTextViewUpdateHandler} jest przekazywana do głównego wątku aplikacji (<code>UI thread</code>). 
	 * @author sokar
	 *
	 */
	private class ReadTimeLengthRunnable implements Runnable {
		private Handler mHandler;
        private String messageToSendString;
		private String mplayerAnswerString = " ";
		private String lastMplayerAnswerString = " ";
		private boolean isThisAnswerNewboolean = true;
		
        public ReadTimeLengthRunnable(Handler h) {
			mHandler = h;
		}

		@Override
		public void run() {
			try{
				while (ConnectAndPlayService.mplayerOutputArrayList.isEmpty() == true){ 		//waiting for mplayer start
	            	Thread.sleep(100);
        		}

				while (isMyServiceRunning() == true && isTimeLengthTextViewSetboolean == false) {

					ConnectAndPlayService.mplayerOutputArrayListLock.lock();
			        try
			        {

			        	mplayerAnswerString = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);
						if (mplayerAnswerString.equals(lastMplayerAnswerString)){
							isThisAnswerNewboolean = false;
						}else{
							isThisAnswerNewboolean = true;
						}

						while (ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1).contains("ANS_LENGTH") == false || isThisAnswerNewboolean == false) {

								ConnectAndPlayService.newMplayerOutputCondition.await();

								mplayerAnswerString = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);
								if (mplayerAnswerString.equals(lastMplayerAnswerString)){
									isThisAnswerNewboolean = false;
								}else{
									isThisAnswerNewboolean = true;
								}

			        	}


						String timeLengthString = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);
						if (timeLengthString.contains(".") == true){
							int position = timeLengthString.lastIndexOf("=");
							int dotPosition = timeLengthString.lastIndexOf(".");
							messageToSendString = timeLengthString.substring(position + 1, dotPosition);
							Message msg = mHandler.obtainMessage();
							try{
				            msg.arg1 =  Integer.parseInt(messageToSendString);		//mplayer sometime send weird output
							}catch (java.lang.NumberFormatException e){
								break;
							}
				            mHandler.sendMessage(msg);
						}else{
							int position = timeLengthString.lastIndexOf("=");
							messageToSendString = timeLengthString.substring(position + 1);
							Message msg = mHandler.obtainMessage();
							try{
					            msg.arg1 =  Integer.parseInt(messageToSendString);		//mplayer sometime send weird output
							}catch (java.lang.NumberFormatException e){
								break;
							}
				            mHandler.sendMessage(msg);
						}

						lastMplayerAnswerString = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);

			        }
					finally
		            {
		            	ConnectAndPlayService.mplayerOutputArrayListLock.unlock();
		            }

					Log.v(TAG, "readTimeLengthThread wciąż żyje");
					//ConnectToServer.sendCommand("echo pausing_keep get_time_length > fifofile");
				}
				Log.v(TAG, "readTimeLengthThread umarł");
			}
			catch (InterruptedException e)
	    	{
	    		// TODO Auto-generated catch block
				e.printStackTrace();
	    	}
		}
	}
	
	/**
	 * Klasa reprezentująca komendę, którą można wykonać np. w oddzielnym wątku.
	 * Wątek utworzony z klasy <code>ReadTimePositionRunnable</code> odczytuje z listy {@link com.mplayer_remote.ConnectAndPlayService#mplayerOutputArrayList}
	 * odpowiedź od odtwarzacza <code>MPlayer</code> zawierającą informacje, wyrażoną w sekundach, o pozycji, w której znajduje się odtwarzanie pliku multimedialnego. 
	 * Informacja ta poprzez <code>Handler</code> {@link RemoteControl#timePositionUpdateHandler} jest przekazywana do głównego wątku aplikacji (<code>UI thread</code>). 
	 * @author sokar
	 *
	 */
	private class ReadTimePositionRunnable implements Runnable{
		private Handler mHandler;
		private String messageToSendString;
		private String mplayerAnswerString = " ";
		private String lastMplayerAnswerString = " ";
		private boolean isThisAnswerNewboolean = true;
		
        public ReadTimePositionRunnable(Handler h) {
			mHandler = h;
		}
		@Override
		public void run() {
			try{

				while (ConnectAndPlayService.mplayerOutputArrayList.isEmpty() == true){ 		//waiting for mplayer start
            		Thread.sleep(100);
				}

				while (isMyServiceRunning() == true ) {
					//ConnectToServer.sendCommand("echo pausing_keep_force get_time_pos > fifofile");

					ConnectAndPlayService.mplayerOutputArrayListLock.lock();
			        try
			        {
			        	mplayerAnswerString = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);
						if (mplayerAnswerString.equals(lastMplayerAnswerString)){
							isThisAnswerNewboolean = false;
						}else{
							isThisAnswerNewboolean = true;
						}


						while (ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1).contains("ANS_TIME_POSITION") == false || isThisAnswerNewboolean == false) {

								ConnectAndPlayService.newMplayerOutputCondition.await();

								mplayerAnswerString = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);
								if (mplayerAnswerString.equals(lastMplayerAnswerString)){
									isThisAnswerNewboolean = false;
								}else{
									isThisAnswerNewboolean = true;
								}

						}

						String timePositionString = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);

						if (timePositionString.contains(".") == true){
							int position = timePositionString.lastIndexOf("=");
							int dotPositionint = timePositionString.lastIndexOf(".");
							messageToSendString = timePositionString.substring(position + 1, dotPositionint);

							if (messageToSendString.length() > 7 || messageToSendString.contains("-")){		//mplayer sometime send weird output
								Log.v("TAG", "MPlayer send weird output");
							}else{
								Message msg = mHandler.obtainMessage();
								try{
						            msg.arg1 =  Integer.parseInt(messageToSendString);		//mplayer sometime send weird output
								}catch (java.lang.NumberFormatException e){
									break;
								}
					            mHandler.sendMessage(msg);
							}
						}else{
							int position = timePositionString.lastIndexOf("=");
							messageToSendString = timePositionString.substring(position + 1);

							if (messageToSendString.length() > 7 || messageToSendString.contains("-")){		//mplayer sometime send weird output
								Log.v("TAG", "MPlayer send weird output");
							}else{
								Message msg = mHandler.obtainMessage();
								try{
						            msg.arg1 =  Integer.parseInt(messageToSendString);		//mplayer sometime send weird output
								}catch (java.lang.NumberFormatException e){
									break;
								}
					            mHandler.sendMessage(msg);
							}
						}

						lastMplayerAnswerString = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);


			        }
			    	finally
		            {
		            	ConnectAndPlayService.mplayerOutputArrayListLock.unlock();
		            }


					Log.v(TAG, "readTimePositionThread  wciąż żyje");

				}
				Log.v(TAG, "readTimePositionThread umarł");
			}
			catch (InterruptedException e)
        	{
        		// TODO Auto-generated catch block
				e.printStackTrace();
        	}
		}
			
		
		
	}
	
	/**
	 * Klasa reprezentująca komendę, którą można wykonać np. w oddzielnym wątku.
	 * Wątek utworzony z klasy <code>ReadProgressRunnable</code> odczytuje z listy {@link com.mplayer_remote.ConnectAndPlayService#mplayerOutputArrayList}
	 * odpowiedź od odtwarzacza <code>MPlayer</code> zawierającą informacje, wyrażoną w procentach, o pozycji, w której znajduje się odtwarzanie pliku multimedialnego. 
	 * Informacja ta poprzez <code>Handler</code> {@link RemoteControl#progressHandler} jest przekazywana do głównego wątku aplikacji (<code>UI thread</code>). 
	 * @author sokar
	 *
	 */
	private class ReadProgressRunnable implements Runnable {
		private Handler mHandler;
		private String messageToSendString;
		private String mplayerAnswerString = " ";
		private String lastMplayerAnswerString = " ";
		private boolean isThisAnswerNewboolean = true;
		
        ReadProgressRunnable(Handler h) {
            mHandler = h;
        }
       
        public void run() {
        	try {
	            while (ConnectAndPlayService.mplayerOutputArrayList.isEmpty() == true){ 		//waiting for mplayer start
	            	Thread.sleep(100);
	        	}

	            while (isMyServiceRunning() == true) {

					ConnectAndPlayService.mplayerOutputArrayListLock.lock();
			        try
			        {
			        	mplayerAnswerString = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);
						if (mplayerAnswerString.equals(lastMplayerAnswerString)){
							isThisAnswerNewboolean = false;
						}else{
							isThisAnswerNewboolean = true;
						}

						while (ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1).contains("ANS_PERCENT_POSITION") == false || isThisAnswerNewboolean == false) {

								ConnectAndPlayService.newMplayerOutputCondition.await();

								mplayerAnswerString = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);
								if (mplayerAnswerString.equals(lastMplayerAnswerString)){
									isThisAnswerNewboolean = false;
								}else{
									isThisAnswerNewboolean = true;
								}
						}
						String last_mplayer_output_with_ANS_PERCENT_POSITION = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);
						Log.v(TAG, "aktualny procent odtwarzania " + ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size()-1));


				        int position = last_mplayer_output_with_ANS_PERCENT_POSITION.lastIndexOf("=");
				        //Log.v(TAG, "position is: " + position);
			            messageToSendString = last_mplayer_output_with_ANS_PERCENT_POSITION.substring(position + 1);
			            Log.v(TAG, "message_to_send after parsing to int is: " + messageToSendString);

			            Message msg = mHandler.obtainMessage(WHAT_FOR_PROGRESS_MESSAGE);
			            try{
				            msg.arg1 =  Integer.parseInt(messageToSendString);		//mplayer sometime send weird output
						}catch (java.lang.NumberFormatException e){
							break;
						}

			            mHandler.sendMessageDelayed(msg, 1000);

			            lastMplayerAnswerString = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);

			        }
		            finally
		            {
		            	ConnectAndPlayService.mplayerOutputArrayListLock.unlock();
		            }
				}
        	}
        	catch (InterruptedException e)
        	{
        		// TODO Auto-generated catch block
				e.printStackTrace();
        	}
		}
    }
    

    
}

