package com.mplayer_remote;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RemoteControlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RemoteControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RemoteControlFragment extends Fragment {


    private Context applicationContext = null;
    private Activity activity = null;
    private View rootView = null;

    //w celach diagnostycznych nazwa logu dla tego Activity
    private static final String TAG = RemoteControlFragment.class.getSimpleName();

    //public static Activity RemoteControlActivityObject;

    /**
     * Progress message number. For distinguish from other messages.
     * @see http://developer.android.com/reference/android/os/Handler.html#obtainMessage(int)
     */
    private static final int WHAT_FOR_PROGRESS_MESSAGE = 0;

    /**
     * Łańcuch znaków zawierający ścieżkę absolutną, w której znajduje się plik wskazany przez użytkownika.
     */
    private String absolutePathString;

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
    private KeyguardManager.KeyguardLock lock;

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

            //set a nowPlayFileNameTextView
            fileToPlayString = mConnectAndPlayService.getNowPlayingFileString();
            int positionOfLastDashint = fileToPlayString.lastIndexOf("/");
            String substringfileToPlayString = fileToPlayString.substring(positionOfLastDashint + 1);
            nowPlayFileNameTextView.setText(substringfileToPlayString);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "NewnowPlayingFileString" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String newnowPlayingFileString = intent.getStringExtra("NewnowPlayingFileString");
            Log.d("receiver", "Got message: " + newnowPlayingFileString);

            fileToPlayString = newnowPlayingFileString;
            nowPlayFileNameTextView = (TextView) rootView.findViewById(R.id.now_play_textView);
            int positionOfLastDashint = fileToPlayString.lastIndexOf("/");
            String substringfileToPlayString = fileToPlayString.substring(positionOfLastDashint + 1);
            nowPlayFileNameTextView.setText(substringfileToPlayString);
        }
    };

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "ReachEndOfPlaylist" is broadcasted.
    private BroadcastReceiver mReachEndOfPlaylistMessageReceivet = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            activity.finish();
        }
    };

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "fileToPlay";
    private static final String ARG_PARAM2 = "absolutePath";

    private OnFragmentInteractionListener mListener;

    public RemoteControlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fileToPlayString Parameter 1.
     * @param absolutePathString Parameter 2.
     * @return A new instance of fragment RemoteControlFragment.
     */
    public static RemoteControlFragment newInstance(String fileToPlayString, String absolutePathString) {
        RemoteControlFragment fragment = new RemoteControlFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, fileToPlayString);
        args.putString(ARG_PARAM2, absolutePathString);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fileToPlayString = getArguments().getString(ARG_PARAM1);
            absolutePathString = getArguments().getString(ARG_PARAM2);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*
        Intent intentFromstartActivity = getIntent(); //getIntent() zwraca obiekt Intent który wystartował Activity
        fileToPlayString = intentFromstartActivity.getStringExtra("file_to_play");
        absolutePathString = intentFromstartActivity.getStringExtra("absolute_path");
        Log.v(TAG, "file_to_play przekazane przez intent z ConnectAndPlayService: " + fileToPlayString);
        Log.v(TAG, "absolute_path przekazane przez intent z ServicePlayAFile: " + absolutePathString);
        */

        rootView = inflater.inflate(R.layout.fragment_remote_control, container, false);

        final Vibrator mVibrator = (Vibrator) applicationContext.getSystemService(Context.VIBRATOR_SERVICE);

        /*
        keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
        lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        */

        Button fullscreenButton = (Button) rootView.findViewById(R.id.fullscreean_button);
        fullscreenButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if( mBound == true ) {
                    mConnectAndPlayService.sendCommand("echo pausing_keep_force vo_fullscreen > fifofile");
                    mVibrator.vibrate(50);
                }

            }
        });

        Button switchAudioButton = (Button) rootView.findViewById(R.id.switch_audio_button);
        switchAudioButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if( mBound == true ) {
                    mConnectAndPlayService.sendCommand("echo pausing_keep_force switch_audio> fifofile");
                    mVibrator.vibrate(50);
                }
            }
        });

        Button stopButton = (Button) rootView.findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //askMplayerThread.interrupt();
                if( mBound == true ) {
                    mConnectAndPlayService.stopPlaying();

                    mVibrator.vibrate(50);
                    activity.finish();


                }

            }
        });

        Button previous_media_button = (Button) rootView.findViewById(R.id.previous_media_button);
        previous_media_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(mBound == true){
                    mConnectAndPlayService.playPreviousMedia();
                    mVibrator.vibrate(50);

                }
            }
        });

        Button next_media_button = (Button) rootView.findViewById(R.id.next_media_button);
        next_media_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //askMplayerThread.interrupt();
                if (mBound == true) {
                    mConnectAndPlayService.playNextMedia();
                    mVibrator.vibrate(50);

                }

            }
        });

        pauseButton = (Button) rootView.findViewById(R.id.pause_button);
	     /*
	     sharedPreferencesForActivityRemotControl = getSharedPreferences("RemoteControl_activity_state", 0);
	     showingPlayButtonboolean = sharedPreferencesForActivityRemotControl.getBoolean("showing_play_button", false);
    	 */
        pauseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if( mBound == true ) {
                    mConnectAndPlayService.sendCommand("echo pausing_keep pause > fifofile");
                    mVibrator.vibrate(50);
					/*
					if (showingPlayButtonboolean == false) {
						pauseButton.setBackgroundColor(000000);
						pauseButton.setCompoundDrawablesWithIntrinsicBounds(drawable.play_button, 0, 0, 0);
						showingPlayButtonboolean = true;
					} else {
						pauseButton.setBackgroundColor(000000);
						pauseButton.setCompoundDrawablesWithIntrinsicBounds(drawable.pause_button, 0, 0, 0);
						showingPlayButtonboolean = false;
					}
					*/
                }
            }
        });

        Button step10SecondForwardButton = (Button) rootView.findViewById(R.id.step_10_second_forward_button);
        step10SecondForwardButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if( mBound == true ) {
                    mConnectAndPlayService.sendCommand("echo pausing_keep_force seek 10  > fifofile");
                    mConnectAndPlayService.sendCommand("echo pausing_keep osd_show_progression > fifofile");
                    mVibrator.vibrate(50);
                }
            }
        });

        Button step10SecondBackwardButton = (Button) rootView.findViewById(R.id.step_10_second_backward_button);
        step10SecondBackwardButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if( mBound == true ) {
                    mConnectAndPlayService.sendCommand("echo pausing_keep_force seek -10  > fifofile");
                    mConnectAndPlayService.sendCommand("echo pausing_keep osd_show_progression > fifofile");
                    mVibrator.vibrate(50);
                }
            }
        });

        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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

        timePositionTextView = (TextView) rootView.findViewById(R.id.time_position_textView);

        timeLengthTextView = (TextView) rootView.findViewById(R.id.time_length_textView);

        nowPlayFileNameTextView = (TextView) rootView.findViewById(R.id.now_play_textView);
        int positionOfLastDashint = fileToPlayString.lastIndexOf("/");
        String substringfileToPlayString = fileToPlayString.substring(positionOfLastDashint + 1);
        nowPlayFileNameTextView.setText(substringfileToPlayString);


        progressHandler = new Handler() {
            public void handleMessage(Message msg) {
                int progress = msg.arg1;
                //int state = msg.arg2;
                seekBar.setProgress(progress);
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

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Bind to ConnectAndPlayService
        Intent intent = new Intent(applicationContext, ConnectAndPlayService.class);
        applicationContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(mMessageReceiver, new IntentFilter("nowPlayingFileStringChange"));
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(mReachEndOfPlaylistMessageReceivet, new IntentFilter("ReachEndOfPlaylist"));
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            applicationContext.unbindService(mConnection);
            mBound = false;
        }

        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(mMessageReceiver);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(mReachEndOfPlaylistMessageReceivet); //activity will be finished even if is in background

    }

    /**
     * Metoda wywoływana przez system Android przy wznawianiu wyświetlania aktywności.
     * Uruchamia wątki <code>askMplayerThread</code>, <code>readTimeLengthThread</code>, <code>readTimePositionThread</code>, <code>readProgressThread</code>.
     * Na podstawie zmiennej <code>showingPlayButtonboolean</code>  ustawia ikonę wyświetlaną przez przycisk <code>pauseButton</code>.
     * @see android.app.Activity#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();

        askMplayerThread = new Thread(new AskMplayerRunnable());
        askMplayerThread.start();

        readTimeLengthThread = new Thread(new ReadTimeLengthRunnable(timeLengthTextViewUpdateHandler));
        readTimeLengthThread.start();

        readTimePositionThread = new Thread(new ReadTimePositionRunnable(timePositionUpdateHandler));
        readTimePositionThread.start();

        readProgressThread = new Thread(new ReadProgressRunnable(progressHandler));
        readProgressThread.start();

        // Restore preferences
        if (isMyServiceRunning() == false)
            activity.finish();
	 	/*
	    showingPlayButtonboolean = sharedPreferencesForActivityRemotControl.getBoolean("showing_play_button", false);
   	 	if (showingPlayButtonboolean == true){
	    	 pauseButton.setBackgroundColor(000000);
	    	 pauseButton.setCompoundDrawablesWithIntrinsicBounds(drawable.play_button, 0, 0, 0);
	    }
   	 	*/
        //wyłącza Lock Screen
        //lock.disableKeyguard();	//disable because error in android > 5.0, when user launch activity by notification on lock screen a back stack is broken, https://code.google.com/p/android/issues/detail?id=158505
    }


    /**
     * Metoda wywoływana przez system Android, kiedy aktywność przestaje być wyświetlana na ekranie urządzenia.
     * Zatrzymuje wątki <code>askMplayerThread</code>, <code>readTimeLengthThread</code>, <code>readTimePositionThread</code>, <code>readProgressThread</code>.
     * Zapisuje za pomocą interfejsu <code>sharedPreferencesForActivityRemotControl</code> informacje jaką ikonę wyświetla przycisk <code>pauseButton</code>.
     * @see android.app.Activity#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();

        askMplayerThread.interrupt();
        readTimeLengthThread.interrupt();
        readTimePositionThread.interrupt();
        readProgressThread.interrupt();
        /*
        	//Saving preferences
        SharedPreferences.Editor editor = sharedPreferencesForActivityRemotControl.edit();
        editor.putBoolean("showing_play_button", showingPlayButtonboolean);
        editor.commit();
        */
        //absolutePathString = null;

        //włącza Lock Screen
        //lock.reenableKeyguard(); //disable because error in android > 5.0, when user launch activity by notification on lock screen a back stack is broken, https://code.google.com/p/android/issues/detail?id=158505
    }

    /**
     * Metoda wywoływana przez system Android w reakcji na pierwsze wybranie przycisku menu urządzenia. Funkcja odpowiedzialna za wczytanie pliku XML z definicją menu aktywności.
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_for_remote_control, menu);
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
                Intent intentStartSubtitleFileChooser = new Intent(applicationContext, SubtitleFileChooser.class);
                intentStartSubtitleFileChooser.putExtra("absolute_path", absolutePathString);
                intentStartSubtitleFileChooser.putExtra("file_to_play", fileToPlayString);
                //Log.v(TAG, "Starting SubtitleFileChooser with absolute_path = " + absolutePathString);
                Log.v(TAG, "Starting SubtitleFileChooser with file_to_play = " + fileToPlayString);
                startActivity(intentStartSubtitleFileChooser);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        applicationContext = activity.getApplicationContext();
        this.activity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Sprawdza czy usługa ConnectAndPlayService działa w tle.
     * @return <code>true</code> jeśli usługa ConnectAndPlayService działa w tle, <code>false</code> w przeciwnym wypadku.
     */
    private boolean isMyServiceRunning() {

        ActivityManager manager = (ActivityManager) applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.mplayer_remote.ConnectAndPlayService".equals(service.service.getClassName())) {
                return true;
            }
        }
        activity.finish(); //to finish activity when user close mplayer by other whey
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

            while (true){
                try{

                    if (mBound == true) {
                        mConnectAndPlayService.sendCommand("echo pausing_keep_force get_time_length > fifofile");
                    }
                    Thread.sleep(500);		//sleep() method check Thread.currentThread().isInterrupted() before actually sleep current thread and throw InterruptedException if someone call interrupt() before.

                    if (mBound == true) {
                        mConnectAndPlayService.sendCommand("echo pausing_keep_force get_time_pos > fifofile");
                    }
                    Thread.sleep(500);		//sleep() method check Thread.currentThread().isInterrupted() before actually sleep current thread and throw InterruptedException if someone call interrupt() before.

                    if (mBound == true) {
                        mConnectAndPlayService.sendCommand("echo pausing_keep_force get_percent_pos > fifofile");
                    }
                    Thread.sleep(500);		//sleep() method check Thread.currentThread().isInterrupted() before actually sleep current thread and throw InterruptedException if someone call interrupt() before.

                } catch (InterruptedException e) {
                    Log.e("ERROR", "AskMplayerThread Interrupted");
                    return;	//to end while interrupt()
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
    private static class ReadTimeLengthRunnable implements Runnable {
        private Handler mHandler;
        private String messageToSendString;
        //private String mplayerAnswerString = " ";
        //private String lastMplayerAnswerString = " ";
        //private boolean isThisAnswerNewboolean = true;

        public ReadTimeLengthRunnable(Handler h) {
            mHandler = h;
        }

        @Override
        public void run() {

            while (true) {	//until interrupt in await

                ConnectAndPlayService.mplayerOutputArrayListLock.lock();
                try {
                    while (ConnectAndPlayService.mplayerOutputArrayList.isEmpty() || !ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1).contains("ANS_LENGTH")) {
                        //nothing to do so await for new MPlayer answer
                        ConnectAndPlayService.newMplayerOutputCondition.await();
                    }

                    String timeLengthString = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);
                    if (timeLengthString.contains(".") == true) {
                        int position = timeLengthString.lastIndexOf("=");
                        int dotPosition = timeLengthString.lastIndexOf(".");
                        messageToSendString = timeLengthString.substring(position + 1, dotPosition);
                        Message msg = mHandler.obtainMessage();
                        try {
                            msg.arg1 = Integer.parseInt(messageToSendString);        //mplayer sometime send weird output
                        } catch (java.lang.NumberFormatException e) {
                            break;
                        }
                        mHandler.sendMessage(msg);
                    } else {
                        int position = timeLengthString.lastIndexOf("=");
                        messageToSendString = timeLengthString.substring(position + 1);
                        Message msg = mHandler.obtainMessage();
                        try {
                            msg.arg1 = Integer.parseInt(messageToSendString);        //mplayer sometime send weird output
                        } catch (java.lang.NumberFormatException e) {
                            break;
                        }
                        mHandler.sendMessage(msg);
                    }

                    ConnectAndPlayService.newMplayerOutputCondition.await();    //this Mplayer answer consumed so await for new MPlayer answer

                } catch (InterruptedException e) {
                    Log.e("ERROR", "readTimeLengthThread Interrupted");
                    return;	//to end while interrupt()
                } finally {
                    ConnectAndPlayService.mplayerOutputArrayListLock.unlock();
                }
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
    private static class ReadTimePositionRunnable implements Runnable{
        private Handler mHandler;
        private String messageToSendString;
        private String mplayerAnswerString = " ";

        public ReadTimePositionRunnable(Handler h) {
            mHandler = h;
        }
        @Override
        public void run() {

            while (true) {	//until interrupt in await

                ConnectAndPlayService.mplayerOutputArrayListLock.lock();
                try{

                    while (ConnectAndPlayService.mplayerOutputArrayList.isEmpty() || !ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1).contains("ANS_TIME_POSITION")) {
                        //nothing to do so await for new MPlayer answer
                        ConnectAndPlayService.newMplayerOutputCondition.await();
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

                    ConnectAndPlayService.newMplayerOutputCondition.await();	//this Mplayer answer consumed so await for new MPlayer answer

                }catch (InterruptedException e){
                    Log.e("ERROR", "readTimePositionThread Interrupted");
                    return; //to end while interrupt()
                }
                finally{
                    ConnectAndPlayService.mplayerOutputArrayListLock.unlock();
                }

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
    private static class ReadProgressRunnable implements Runnable {
        private Handler mHandler;
        private String messageToSendString;

        ReadProgressRunnable(Handler h) {
            mHandler = h;
        }

        public void run() {

            while (true) {

                ConnectAndPlayService.mplayerOutputArrayListLock.lock();
                try
                {
                    while (ConnectAndPlayService.mplayerOutputArrayList.isEmpty() || !ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1).contains("ANS_PERCENT_POSITION")) {
                        //nothing to do so await for new MPlayer answer
                        ConnectAndPlayService.newMplayerOutputCondition.await();
                    }

                    String last_mplayer_output_with_ANS_PERCENT_POSITION = ConnectAndPlayService.mplayerOutputArrayList.get(ConnectAndPlayService.mplayerOutputArrayList.size() - 1);
                    int position = last_mplayer_output_with_ANS_PERCENT_POSITION.lastIndexOf("=");
                    messageToSendString = last_mplayer_output_with_ANS_PERCENT_POSITION.substring(position + 1);

                    Message msg = mHandler.obtainMessage(WHAT_FOR_PROGRESS_MESSAGE);
                    try{
                        msg.arg1 =  Integer.parseInt(messageToSendString);		//mplayer sometime send weird output
                    }catch (java.lang.NumberFormatException e){
                        break;
                    }
                    mHandler.sendMessageDelayed(msg, 1000);

                    ConnectAndPlayService.newMplayerOutputCondition.await();	//this Mplayer answer consumed so await for new MPlayer answer

                }catch (InterruptedException e) {
                    Log.e("ERROR", "readProgressThread Interrupted");
                    return; //to end while interrupt()
                }
                finally
                {
                    ConnectAndPlayService.mplayerOutputArrayListLock.unlock();
                }
            }
        }

    }


}
