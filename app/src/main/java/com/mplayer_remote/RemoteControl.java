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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Aktywność wyświetlająca przyciski oraz pasek przewijania pozwalające na sterowanie odtwarzaniem pliku multimedialnego.
 * Graficzny interfejs użytkownika tej aktywności wyświetla również informacje o długości odtwarzanego pliku i miejscu, w którym obecnie znajduje się odtwarzanie.
 * Menu aktywności zawiera pozycje, której wybranie uruchamia aktywność <code>SubtitleFileChooser</code>.
 * @author sokar
 * @see android.app.Activity
 */
public class RemoteControl extends FragmentActivity implements RemoteControlFragment.OnFragmentInteractionListener{

		//w celach diagnostycznych nazwa logu dla tego Activity
	private static final String TAG = RemoteControl.class.getSimpleName();

	/**
	 * Łańcuch znaków zawierający ścieżkę absolutną, w której znajduje się plik wskazany przez użytkownika.
	 */
	private String absolutePathString;

	/**
	 * Łańcuch znaków zawierający pełna nazwę wraz ze ścieżką absolutną do pliku wskazanego przez użytkownika.
	 */
	private String fileToPlayString;

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

	static final int NUM_ITEMS = 2;

	ViewPager mViewPager = null;
	FragmentPagerAdapter myFragmentPagerAdapter = null;

	/**Metoda wywoływana przez system Android przy starcie aktywności.
	 * Wczytuje definicje GUI z pliku XML. Definiuje akcje wywoływane poprzez interakcji użytkownika z graficznym interfejsem użytkownika aktywności.
	 * Definiuje <code>progressHandler</code>, <code>timeLengthTextViewUpdateHandler</code>, <code>timePositionUpdateHandler</code>.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//getActionBar().setDisplayHomeAsUpEnabled(false);

		Intent intentFromstartActivity = getIntent(); //getIntent() zwraca obiekt Intent który wystartował Activity
		fileToPlayString = intentFromstartActivity.getStringExtra("file_to_play");
		absolutePathString = intentFromstartActivity.getStringExtra("absolute_path");

		    //gui
		setContentView(R.layout.layout_for_remotecontrol);
		myFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        Log.v(TAG, "mViewPager" + mViewPager.toString());
        Log.v(TAG,"myFragmentPagerAdapter" + myFragmentPagerAdapter.toString());
        mViewPager.setAdapter(myFragmentPagerAdapter);

	}

    @Override
    public void onStart() {
        super.onStart();
        // Bind to ConnectAndPlayService
        Intent intent = new Intent(this, ConnectAndPlayService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }

	///////////////////////////THIS MAST STAY IN ACTIVITY//////////////////////////////////////
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
			return true; //because I handled the event
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
		public MyFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return NUM_ITEMS;
		}

		@Override
		public Fragment getItem(int position) {
			if(position == 0){
                return RemoteControlFragment.newInstance(fileToPlayString, absolutePathString);
            }else {
                return RemoteControlFragment.newInstance("dupa", "dupa");
            }
		}
	}


}

