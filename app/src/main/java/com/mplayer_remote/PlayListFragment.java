package com.mplayer_remote;

import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayListFragment extends ListFragment {

    private Activity activity = null;
    private Context applicationContext = null;


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


            //setListAdapter(new ArrayAdapter<String>(activity, R.layout.layout_for_playlist_item, R.id.text1, filesNamePlayListList));
            //setListAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_activated_1,filesNamePlayListList ));
            PlayListListAdapter myPlayListListAdapter = new PlayListListAdapter(activity, mConnectAndPlayService.getPlayListArrayList());
            setListAdapter(myPlayListListAdapter);
            //Headlight now played file
            getListView().setItemChecked(mConnectAndPlayService.getPlayListArrayList().indexOf(mConnectAndPlayService.getNowPlayingFileString()), true); //headlight first file playlist
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String newnowPlayingFileString = intent.getStringExtra("NewnowPlayingFileString");
            Log.d("receiver", "Got message: " + newnowPlayingFileString);

            if (mBound == true) {
                int position = mConnectAndPlayService.getPlayListArrayList().indexOf(newnowPlayingFileString);
                getListView().setItemChecked(position, true);
            }

        }
    };



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public PlayListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayListFragment newInstance(String param1, String param2) {
        PlayListFragment fragment = new PlayListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_play_list, container, false);
        return v;
    }

    /*
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if(mBound == true) {
            mConnectAndPlayService.playPlayListFromIndex(position);
        }
    }
    */

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Bind to ConnectAndPlayService
        Intent intent = new Intent(applicationContext, ConnectAndPlayService.class);
        applicationContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(mMessageReceiver, new IntentFilter("nowPlayingFileStringChange"));

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

    private class PlayListListAdapter extends BaseAdapter implements ListAdapter {

        private Context context = null;
        private List<String> playListList = null;   //a list of full path to file
        private List<String> filesNamePlayListList = null;  //just a files names

        PlayListListAdapter(Context context, List<String> playListList){
            this.context = context;
            this.playListList = playListList;
        }

        @Override
        public int getCount() {
            return playListList.size();
        }

        @Override
        public Object getItem(int i) {
            return playListList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.layout_for_playlist_item, null);
            }

            playListList = mConnectAndPlayService.getPlayListArrayList();
            filesNamePlayListList = new ArrayList<String>();
            for (String fullFileName: playListList) {
                int positionOfLastDash = fullFileName.lastIndexOf("/");
                String justFileName = fullFileName.substring(positionOfLastDash + 1);
                filesNamePlayListList.add(justFileName);
            }

            //Handle TextView and display string from your list
            TextView listItemText = (TextView)view.findViewById(R.id.text1);
            listItemText.setText(filesNamePlayListList.get(position));

            //Handle buttons and add onClickListeners
            ImageButton removeFromPlaylistButton = (ImageButton)view.findViewById(R.id.remove_from_playlist_button);
            ImageButton upInPlaylistButton = (ImageButton)view.findViewById(R.id.up_in_playlist_button);
            ImageButton down_in_playlist_button =(ImageButton)view.findViewById(R.id.down_in_playlist_button);

            listItemText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getListView().setItemChecked(position, true); //headlight first file playlist
                    if(mBound == true) {
                        mConnectAndPlayService.playPlayListFromIndex(position);
                    }
                }
            });

            removeFromPlaylistButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(mBound == true) {
                        if (!filesNamePlayListList.get(position).equals(mConnectAndPlayService.getNowPlayingFileString().substring(mConnectAndPlayService.getNowPlayingFileString().lastIndexOf("/") + 1))) {
                            //for ListView
                            filesNamePlayListList.remove(position);
                            notifyDataSetChanged();

                            //for ConnectAndPlayService
                            mConnectAndPlayService.removeFileFromPlayList(position);
                        }
                    }
                }
            });
            upInPlaylistButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (position > 0){
                        if (mBound == true) {
                            mConnectAndPlayService.swapFilesInPlayList(position, position - 1);
                        }
                    }
                    notifyDataSetChanged();

                }
            });
            down_in_playlist_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if (position < playListList.size() - 1){
                        if(mBound == true) {
                            mConnectAndPlayService.swapFilesInPlayList(position, position + 1);
                        }
                    }
                    notifyDataSetChanged();
                }
            });

            return view;
        }
    }

}
