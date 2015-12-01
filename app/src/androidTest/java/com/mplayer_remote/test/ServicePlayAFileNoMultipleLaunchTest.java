package com.mplayer_remote.test;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;

import com.mplayer_remote.*;
import com.robotium.solo.Solo;

public class ServicePlayAFileNoMultipleLaunchTest extends ActivityInstrumentationTestCase2<FileChooser> {

	public ServicePlayAFileNoMultipleLaunchTest() {
		super(FileChooser.class);
		// TODO Auto-generated constructor stub
	}
		
		//w celach diagnostycznych nazwa logu dla tej TestCaseClass
	private static final String TAG = "ConnectToServerStateManagementTest";	
	private Solo solo;		//from robotium a "Activity"
	private Instrumentation instrumentation;
	private Activity mActivity;
	Method[] methods;
	Method methodconnectToServer;
	String serverName = "servername";
	String serverIP = "192.168.0.110";
	String user = "sokar";
	char[] password = {'m','a','t','r','i','x'};
	Object[] parameters = {serverName, serverIP, user, password};
	private String absolute_path = "";
	private ArrayList<String> dir_contain = new ArrayList<String>();
	
	@Override
	protected void setUp() throws Exception{
		super.setUp();
		methods  = ConnectToServer.class.getDeclaredMethods();
		for (Method m : methods){
			if (m.getName().equals("connectToServer")){
				m.setAccessible(true);
				m.invoke(ConnectToServer.class.newInstance(), parameters);
			}
		}
		
		mActivity = getActivity(); 
		solo = new Solo(getInstrumentation(),getActivity());
				
	}
	
	public void testPreConditions() {
		assertNotNull(solo);
			//check is on server a movie.mp4 file
		boolean dir_contain_moviemp4Stringboolean = false;
		ConnectToServer.sendCommandAndSaveOutputToArrayList("echo $HOME", dir_contain);
        absolute_path = dir_contain.get(0);
		ConnectToServer.sendCommandAndSaveOutputToArrayList("ls --group-directories-first  " + "'" + absolute_path + "/" + "'",dir_contain);
		for (int i = 0; i < dir_contain.size(); i++){
			if (dir_contain.get(i).equals("movie.mp4") == true){
				dir_contain_moviemp4Stringboolean = true;
			}
		}
		assertTrue(dir_contain_moviemp4Stringboolean);
	}
	
	public void testShowDIALOG_PLAY_A_FILEWhenUserWantToStartNextPlay(){

		//solo.clickInList(1);
	 	solo.clickOnText("movie.mp4");
	 	solo.goBack();
	 	solo.clickOnText("movie.mp4");
	 	assertTrue(solo.searchText(solo.getString(com.mplayer_remote.R.string.text_for_DIALOG_PLAY_A_FILE_from_FileChooser)));
	 	solo.clickOnButton(solo.getString(com.mplayer_remote.R.string.text_for_positiveButton_from_DIALOG_PLAY_A_FILE));
	 	//solo.sleep(1000);
	 	//solo.assertCurrentActivity("", com.mplayer_remote.RemoteControl.class);
	 	//ConnectToServer.sendCommandAndWaitForExitStatus("echo stop > fifofile");
	 	solo.clickOnButton(1);
	}
	
	
	@Override
	public void tearDown() throws Exception {
		//tearDown() is run after a test case has finished. 
		//finishOpenedActivities() will finish all the activities that have been opened during the test execution.
		solo.finishOpenedActivities();
	}
}
