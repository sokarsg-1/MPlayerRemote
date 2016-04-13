package com.mplayer_remote;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class ConnectAndPlayService extends Service {

    private final String TAG = ConnectAndPlayService.class.getSimpleName();
    /**
     * Obiekt klasy {@link android.net.ConnectivityManager}, tu służący do uzyskiwania informacji o połączeniach sieciowych nawiązywanych przez urządzenie.
     */
    private ConnectivityManager myConnectivityManager;
    /**
     * Obiekt klasy {@link ch.ethz.ssh2.Connection} służący do nawiązywania szyfrowanego połączenia z serwerem SSH.
     */
    private Connection connection;
    /**
     * Zawiera status wyjściowy ostatnio wykonywanej na serwerze komendy.
     */
    private int lastComandExitStatus;
    /**
     * Binder given to clients.
     */
    private IBinder mBinder = new LocalBinder();
    /**
     * Lista, do której zostaną zapisane dane zwrócone przez polecenie <code>export DISPLAY=:0.0 && mplayer -slave -quiet -input file=fifofile fileToPlayString</code> na <code>stdout</code> serwera.
     */
    protected static ArrayList<String> mplayerOutputArrayList = new ArrayList<String>();

    /**
     * Zamek <a href=''http://docs.oracle.com/javase/7/docs/api/''> Lock </a> umożliwiający kontrolowanie dostępu przez wątki aplikacji do współdzielonej listy {@link com.mplayer_remote.ServicePlayAFile#mplayerOutputArrayList}.
     */
    protected static Lock mplayerOutputArrayListLock = new ReentrantLock(false);		//true means this is a fair lock

    /**
     * Warunek <a href=''http://docs.oracle.com/javase/7/docs/api/''>Condition </a> informujący wątki o pojawieniu się nowych danych na liście {@link com.mplayer_remote.ServicePlayAFile#mplayerOutputArrayList}.
     */
    protected static Condition newMplayerOutputCondition = mplayerOutputArrayListLock.newCondition();

    /**
     * SingleThreadExecutor for playing multiple file(in thread) one by one.
     */
    private ExecutorService es = Executors.newSingleThreadExecutor();

    /**
     * Now playing file in mplayer;
     */
    private String nowPlayingFileString = null;

    /**
     * Łańcuch znaków zawierający ścieżkę absolutną, w której znajduje się plik wskazany przez użytkownika do odtworzenia w programie MPlayer.
     */
    private String absolutePathString = null;

    /**
     * A playList from FileChooser
     */
    List<String>  playListArrayList = null;
    /**
     * NotificationManager
     */
    private NotificationManager mNotificationManager;
    private int mId = 1;		//id for notyfication

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "notyficationAction" is broadcasted.
    private BroadcastReceiver mynotyficationActionMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String commandFormNotyficationActionReceiverString = intent.getStringExtra("command");

            if (commandFormNotyficationActionReceiverString.equals("previous")){
                playPreviousMedia();
            }else if(commandFormNotyficationActionReceiverString.equals("pause")){
                sendCommand("echo pausing_keep pause > fifofile");
            }else if(commandFormNotyficationActionReceiverString.equals("next")){
                playNextMedia();
            }
            Log.d("receiver", "Got command from notyfication action button: " + commandFormNotyficationActionReceiverString);

        }
    };

    /**
     * Default constructor.
     */
    public ConnectAndPlayService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {      //TODO place somewhere stopSelf()

        LocalBroadcastManager.getInstance(this).registerReceiver(mynotyficationActionMessageReceiver, new IntentFilter("ButtonActionInNotyficationClicked"));


            //connecting to new SSH server so clean fields
        nowPlayingFileString = null;
        absolutePathString = null;
        playListArrayList = null;

        String serverNameString = intent.getStringExtra("server_name");
        String iPAddressString = intent.getStringExtra("IP_address");
        String usernameString = intent.getStringExtra("username");
        char[] serverPasswordchararray = intent.getCharArrayExtra("password");
        String serverPasswordString = new String(serverPasswordchararray);

        Log.v(TAG, "Server data form intent: " + serverNameString + iPAddressString + usernameString + serverPasswordString);

        new ConnectToAsyncTask().execute(serverNameString, iPAddressString, usernameString, serverPasswordString);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "ConnectAndPlayService destroyed");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mynotyficationActionMessageReceiver);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        ConnectAndPlayService getService(){
            return ConnectAndPlayService.this;
        }
    }

    /**
     * Play a user selected media file.
     * @param fileToPlayString user selected media file.
     */
    public void playAFile(String fileToPlayString, String absolutePathString){

        List<String> localPlaylist = new ArrayList<String>();
        localPlaylist.add(fileToPlayString);
        playAFiles(localPlaylist, absolutePathString);

    }

    /**
     * Play a user selected files one by one.
     * @param filesToPlayArrayList selected by user files.
     */
    public void playAFiles (List<String>  filesToPlayArrayList, final String absolutePathString){
        playListArrayList = filesToPlayArrayList;
        this.absolutePathString = absolutePathString;

        es = Executors.newSingleThreadExecutor();

        while (!isfifofileExist()){
            sendCommand("rm fifofile");
            sendCommand("mkfifo fifofile");
        }

        Intent intent_start_RemoteControl = new Intent(getBaseContext(), RemoteControl.class);
        intent_start_RemoteControl.putExtra("file_to_play", filesToPlayArrayList.get(0));
        intent_start_RemoteControl.putExtra("absolute_path", absolutePathString);
        intent_start_RemoteControl.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //When using this flag, if a task is already running for the activity you are now starting, then a new activity will not be started; instead, the current task will simply be brought to the front of the screen with the state it was last in.
        startActivity(intent_start_RemoteControl);

        ArrayList<Runnable> myRunnablesArrayList = new ArrayList<Runnable>(filesToPlayArrayList.size());
        for(final String file : filesToPlayArrayList){
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG,"Starting playing file: " + file + " from absolutePath: " + absolutePathString);
                    nowPlayingFileString = file;
                    sendBroadcastnowPlayingFileStringChange();
                    showNotyfications(file, absolutePathString);

                    sendCommandAndSaveOutputToLockedArrayList("export DISPLAY=:0.0 && mplayer -fs -slave -quiet -input file=fifofile " + "\"" + file + "\"", mplayerOutputArrayList, mplayerOutputArrayListLock, newMplayerOutputCondition);
                }
            };
            myRunnablesArrayList.add(r);
        }
        for(Runnable r : myRunnablesArrayList){
            es.submit(r);
        }

        es.submit(new Runnable() {  //stops after reach end of playlist
            @Override
            public void run() {
                stopPlaying();
                sendBroadcastReachEndOfPlaylist();
                Log.v(TAG, "Reach end of playlist so stop RemoteControl");
            }
        });

    }

    /**
     * Called by playNextMedia() and playPreviousMedia() to play a sublist from user selected playlist
     * @param filesToPlayArrayList sublist of filesToPlayArrayList to play.
     */
    private void playASubPlayList (List<String>  filesToPlayArrayList, final String absolutePathString){

        es = Executors.newSingleThreadExecutor();
        ArrayList<Runnable> myRunnablesArrayList = new ArrayList<Runnable>(filesToPlayArrayList.size());
        for(final String file : filesToPlayArrayList){
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    nowPlayingFileString = file;
                    sendBroadcastnowPlayingFileStringChange();
                    showNotyfications(file, absolutePathString);

                    sendCommandAndSaveOutputToLockedArrayList("export DISPLAY=:0.0 && mplayer -fs -slave -quiet -input file=fifofile " + "\"" + file + "\"", mplayerOutputArrayList, mplayerOutputArrayListLock, newMplayerOutputCondition);
                }
            };
            myRunnablesArrayList.add(r);
        }
        for(Runnable r : myRunnablesArrayList){
            es.submit(r);
        }

        es.submit(new Runnable() {  //stops after reach end of playlist
            @Override
            public void run() {
                stopPlaying();
                sendBroadcastReachEndOfPlaylist();
                Log.v(TAG, "Reach end of playlist so stop RemoteControl");
            }
        });

    }

    /**
     * Stops playing one file or files.
     */
    public void stopPlaying(){
        //sendCommand("echo stop > fifofile");
        es.shutdownNow();   //stops executing all next task from es
        sendCommand("pkill mplayer");   //stops now working mplayer instance
        sendCommand("rm fifofile");
        stopForeground(true);

    }

    /**
     *Play next file from directory.
     */
    public void playNextMedia(){

        int nowPlayingFileintIndex = playListArrayList.indexOf(nowPlayingFileString);
        List<String> localPlaylist;
        if (nowPlayingFileintIndex + 1 == playListArrayList.size()){
            Toast.makeText(getApplicationContext(), R.string.text_for_toast_end_of_playlist, Toast.LENGTH_SHORT).show();
        }else {
            stopPlayingPlayList();
            localPlaylist = playListArrayList.subList(nowPlayingFileintIndex + 1, playListArrayList.size());
            playASubPlayList(localPlaylist, absolutePathString);
        }
    }

    /**
     *Play previous file form directory
     */
    public void playPreviousMedia(){
        int nowPlayingFileintIndex = playListArrayList.indexOf(nowPlayingFileString);
        List<String> localPlaylist;
        if (nowPlayingFileintIndex == 0){
            Toast.makeText(getApplicationContext(), R.string.text_for_toast_end_of_playlist, Toast.LENGTH_SHORT).show();
        }else {
            stopPlayingPlayList();
            localPlaylist = playListArrayList.subList(nowPlayingFileintIndex - 1, playListArrayList.size());
            playASubPlayList(localPlaylist, absolutePathString);
        }
    }

    /**
     * Play files from given index to end of playlist.
     */
    public void playPlayListFromIndex(int index){
        List<String> localPlaylist;
        stopPlayingPlayList();
        localPlaylist = playListArrayList.subList(index,playListArrayList.size());
        playASubPlayList(localPlaylist, absolutePathString);
    }

    /**
     * For using in playNextMedia() and playPreviousMedia()
     */
    private void stopPlayingPlayList(){

        es.shutdownNow();
        sendCommand("pkill mplayer");   //kill now playing MPlayer instance

        try {
            while (es.awaitTermination(100, TimeUnit.MILLISECONDS) == false){   //false if timeout
                Log.v(TAG, "es.awaitTermination(100, TimeUnit.MILLISECONDS) == false so pkill --signal 9 mplayer");
                sendCommand("pkill --signal 9 mplayer");    //SIGKILL (9) – Kill signal. Use SIGKILL as a last resort to kill process. This will not save data or cleaning kill the process.
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return now played file.
     */
    public String getNowPlayingFileString(){
        return nowPlayingFileString;
    }

    /**
     * Return current absolutePathString.
     */
    public String getAbsolutePathString(){
        return absolutePathString;
    }

    /**
     * Return current playListArrayList
     */
    public List<String> getPlayListArrayList(){
        return playListArrayList;
    }

    /**
     * Notifying observers about changing of nowPlayingFileString.
     */
    private void sendBroadcastnowPlayingFileStringChange(){
        Intent intent = new Intent("nowPlayingFileStringChange");
        // You can also include some extra data.
        intent.putExtra("NewnowPlayingFileString", nowPlayingFileString);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Notifying observers about reach to end of playlist
     */
    private void sendBroadcastReachEndOfPlaylist(){
        Intent intent = new Intent("ReachEndOfPlaylist");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Notifying observers about connecting to SSH server.
     */
    private void sendBroadcastdismissconnectingToSshProgressDialog(){
        Intent intent = new Intent("dismissconnectingToSshProgressDialog");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Look on SSH server for a true fifofile.
     * @return true if true fifofile on server exist, false otherwise.
     */
    private boolean isfifofileExist() {

        ArrayList<String> fifoFilesArrayList= new ArrayList<String>();
        sendCommandAndSaveOutputToArrayList("ls --file-type|grep fifofile'|'", fifoFilesArrayList);
        if (fifoFilesArrayList.size() == 1){
            return true;
        }else {
            return false;
        }

    }

    private void showNotyfications(String fileToPlayString, String absolutePathString){

        Log.v(TAG, "In showNotyfications()  fileToPlayString: " + fileToPlayString + " absolutePathString: " + absolutePathString);

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

        PendingIntent resultPendingIntent =	stackBuilder.getPendingIntent(0,PendingIntent.FLAG_CANCEL_CURRENT);

        Intent previousMediaIntent = new Intent(this, NotyficationActionReceiver.class);
        previousMediaIntent.setAction("previous");      //http://stackoverflow.com/questions/15350998/determine-addaction-click-for-android-notifications
        PendingIntent previousMediaPendingIntent = PendingIntent.getBroadcast(this, 0, previousMediaIntent, PendingIntent.FLAG_CANCEL_CURRENT );

        Intent pauseMediaIntent = new Intent(this, NotyficationActionReceiver.class);
        pauseMediaIntent.setAction("pause");
        PendingIntent pauseMediaPendingIntent = PendingIntent.getBroadcast(this, 0, pauseMediaIntent, PendingIntent.FLAG_CANCEL_CURRENT );

        Intent nextMediaIntent = new Intent(this, NotyficationActionReceiver.class);
        nextMediaIntent.setAction("next");
        PendingIntent nextMediaPendingIntent = PendingIntent.getBroadcast(this, 0, nextMediaIntent, PendingIntent.FLAG_CANCEL_CURRENT );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.text_for_first_line_of_notification_from_serviceplayafile))
                        .setContentText(secondLineOfNotification)
                        .addAction(R.drawable.previous_media_button, "", previousMediaPendingIntent)
                        .addAction(R.drawable.play_button, "", pauseMediaPendingIntent)
                        .addAction(R.drawable.next_media_button, "", nextMediaPendingIntent);
                      //.setPriority(NotificationCompat.PRIORITY_MAX);  //for shownig notyfication on top and displaying action buttons

        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
        startForeground(mId, mBuilder.build());

    };







    /**
     * Zwraca obiekt klasy {@link ch.ethz.ssh2.Connection} odpowiedzialny za nawiązanie połączenia z danym serwerem SSH.
     * @return obiekt klasy {@link ch.ethz.ssh2.Connection}
     */
    private Connection getConnection() {			//for acces from other android component such as Activity, static bo potrzebuje dostępu do pola statycznego klasy ConnectToServer
        return connection;
    }

    /**
     * Wysyła do serwera polecenie <code>commandToSendString</code>.
     * @param commandToSendString polecenie, które ma być wysłane do serwera.
     */
    public void sendCommand(String commandToSendString){
        try{
	    	/* Create a session */

            Session sess = getConnection().openSession();


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
    public int sendCommandAndWaitForExitStatus(String commandToSendString){
        try{
	    	/* Create a session */

            Session sess = getConnection().openSession();

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
    public void sendCommandAndSaveOutputToArrayList(String commandToSendString, ArrayList<String> outputArrayList){

        try{
	    	/* Create a session */

            Session sess = getConnection().openSession();

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
    public void sendCommandAndSaveOutputToLockedArrayList(String commandToSendString, ArrayList<String> outputArrayList, Lock outputArrayListLock, Condition newOutputCondition){

        try{
	    	/* Create a session */

            Session sess = getConnection().openSession();

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

    /**
     * AsyncTask odpowiedzialny łączenie z serwerem (wykonywane w oddzielnym wątku) oraz wyświetlenie komunikatów w przypadku wystąpienia problemów.
     * @author sokar
     */
    private class ConnectToAsyncTask extends AsyncTask<String, Void, Boolean> {

        private final String TAG = ConnectToAsyncTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(String... params) {
            String serverNameString = params[0];
            String iPAddressString = params[1];
            String usernameString = params[2];
            String serverPasswordString = params[3];
            boolean isConnectedBoolean;
            isConnectedBoolean = connectToServer(serverNameString, iPAddressString, usernameString, serverPasswordString);

            return isConnectedBoolean; //true if connectToServer success
        }

        @Override
        protected void onPostExecute(Boolean isConnectedBoolean){

            sendBroadcastdismissconnectingToSshProgressDialog();


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

            }else if (isConnectedBoolean == true){	//connect succes
                Log.v(TAG, "Połączono z serwerem");

                //for resetting last_visited_dir in lastVisitedSharedPreferences
                ArrayList<String> userHomeDirString = new ArrayList<String>();
                sendCommandAndSaveOutputToArrayList("echo $HOME", userHomeDirString);
                SharedPreferences lastVisitedSharedPreferences = getSharedPreferences("lastVisitedSharedPreferences", MODE_PRIVATE);
                SharedPreferences.Editor mEditor = lastVisitedSharedPreferences.edit();
                mEditor.putString("last_visited_dir", userHomeDirString.get(0));
                mEditor.commit();

                final Intent intent_start_FileChooser = new Intent(getApplicationContext(), FileChooser.class);
                intent_start_FileChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this start new task may broke app flow
                startActivity(intent_start_FileChooser);

            }else{
                Log.v(TAG, "Nie udało się połączyć z serwerem");
                //connectingDialog.dismiss();
                //dodadć dialog z press back to chose andoder server albo z informacją z exeption który wystąpił czyli przyczyne błędu
                Toast.makeText(getApplicationContext(), R.string.text_for_toast_check_server_data_from_connecttoserver, Toast.LENGTH_LONG).show();

            }
        }

        @Override
        protected void onCancelled() {
            Log.v(TAG, "wywołałem onCancelled klasy ConnectToAsyncTask");
            super.onCancelled();
        }

        private boolean connectToServer (String serverNameString, String iPAddressString, String usernameString, String serverPasswordString){
            // łączenie z przekazanym przez Intent serwerem
            boolean isAuthenticated = false;

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

                isAuthenticated = connection.authenticateWithPassword(usernameString, serverPasswordString);
                //Zero out the password.
                //Arrays.fill(serverPasswordchararray,'0');
                //Arrays.fill(this.serverPasswordchararray,'0');




            }catch (Exception e) {
                Log.v(TAG,"wystąpił błąd w connectToServer: " + e);
                e.printStackTrace(System.err);
                //System.exit(2);
            }

            if (isAuthenticated == false){
                return false;
                //throw new IOException("Authentication failed.");
            } else {
                return true;
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

    }

}


