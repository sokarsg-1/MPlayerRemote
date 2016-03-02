package com.mplayer_remote;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ch.ethz.ssh2.Connection;

public class ConnectAndPlayService extends Service {
    public ConnectAndPlayService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String serverNameString = intent.getStringExtra("server_name");
        String iPAddressString = intent.getStringExtra("IP_address");
        String usernameString = intent.getStringExtra("username");
        char[] serverPasswordchararray = intent.getCharArrayExtra("password");
        String serverPasswordString = new String(serverPasswordchararray);

        new ConnectToAsyncTask().execute(serverNameString, iPAddressString, usernameString, serverPasswordString);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * AsyncTask odpowiedzialny za wyświetlanie okna dialogowego w czasie trwania łączenia z serwerem SSH, samo łączenie z serwerem (wykonywane w oddzielnym wątku) oraz wyświetlenie komunikatów w przypadku wystąpienia problemów.
     * @author sokar
     */
    private class ConnectToAsyncTask extends AsyncTask<String, Void, Boolean> {

        private final String TAG = ConnectToAsyncTask.class.getSimpleName();
        /**
         * Okno dialogowe informujące o trwającym łączeniu z danym serwerem SSH.
         */
        private ProgressDialog connectingDialog;
        /**
         * Obiekt klasy {@link android.net.ConnectivityManager}, tu służący do uzyskiwania informacji o połączeniach sieciowych nawiązywanych przez urządzenie.
         */
        private ConnectivityManager myConnectivityManager;
        /**
         * Obiekt klasy {@link ch.ethz.ssh2.Connection} służący do nawiązywania szyfrowanego połączenia z serwerem SSH.
         */
        private Connection connection;  	// pole statyczne (klasowe)

        @Override
        protected void onPreExecute() {

            connectingDialog = ProgressDialog.show(ConnectAndPlayService.this, "", getString(R.string.text_for_progressdialog_from_connecttoserver), true);

            connectingDialog.setCanceledOnTouchOutside(false);	//Zapobiega zamykaniu dialogu przy dotknięciach poza jego obrębem.
            connectingDialog.setCancelable(true);
            connectingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);

                }
            });

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

            }else if (isConnectedBoolean == true){	//connect succes
                //connectingDialog.dismiss();
                Log.v(TAG, "Połączono z serwerem");
                final Intent intent_start_FileChooser = new Intent(getApplicationContext(), FileChooser.class);
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


