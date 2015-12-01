package com.mplayer_remote.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;
import com.mplayer_remote.ConnectToServer;

public class ConnectToServerStateManagementTest extends ActivityInstrumentationTestCase2<ConnectToServer> {
	
		//w celach diagnostycznych nazwa logu dla tej TestCaseClass
	private static final String TAG = "ConnectToServerStateManagementTest";	
	private Solo solo;		//from robotium a "Activity"
	private Instrumentation instrumentation;
	private Activity mActivity;
	
	public ConnectToServerStateManagementTest() {
		super(ConnectToServer.class);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void setUp() throws Exception{
		super.setUp();
		//setActivityInitialTouchMode(false);
		
		
		//instrumentation = getInstrumentation();
		//settingsForActivityServerListSharedPreferences = instrumentation.getTargetContext().getSharedPreferences("settings_for_activity_ServerList", 0);
		
		
		
	}

	
	public void testStateDestroy(){
		final Intent intent_start_ConnectToServer = new Intent();
			intent_start_ConnectToServer.putExtra("server_name", "fake");
			intent_start_ConnectToServer.putExtra("IP_address", "111.111.111.111");
			intent_start_ConnectToServer.putExtra("username", "fake");
			intent_start_ConnectToServer.putExtra("password", "fake".toCharArray());	
		setActivityIntent(intent_start_ConnectToServer);
		mActivity = getActivity(); 
		solo = new Solo(getInstrumentation(),getActivity());
		
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_progressdialog_from_connecttoserver)));
		solo.setActivityOrientation(Solo.LANDSCAPE);
		assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_progressdialog_from_connecttoserver)));
	}
	
	@Override
	public void tearDown() throws Exception {
		//tearDown() is run after a test case has finished. 
		//finishOpenedActivities() will finish all the activities that have been opened during the test execution.
		solo.finishOpenedActivities();
	}
	
}
