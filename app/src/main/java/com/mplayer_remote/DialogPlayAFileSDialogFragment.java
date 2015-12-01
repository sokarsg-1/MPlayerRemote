package com.mplayer_remote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by sokar on 29.09.15.
 */
public class DialogPlayAFileSDialogFragment extends DialogFragment{
    //w celach diagnostycznych nazwa logu dla tego Fragmentu
    private static final String TAG = "DialogPlayAFileS";
    //private String file_to_play;
    // private String absolute_path;
    //private ArrayList<String> fileS_to_palyArrayList = new ArrayList<String>();
    private ArrayList<String> only_file_from_absolute_path_of_long_pressed_dir = new ArrayList<String>();
    private String absolute_path_of_long_pressed_dir = "";

    /**
     * Create a new instance of DialogPlayAFileSDialogFragment, providing "file_to_play", "absolute_path" and "fileS_to_palyArrayList"
     * as an argument.
     */
    static DialogPlayAFileSDialogFragment newInstance(ArrayList<String>only_file_from_absolute_path_of_long_pressed_dir, String absolute_path_of_long_pressed_dir) {
        DialogPlayAFileSDialogFragment f = new DialogPlayAFileSDialogFragment();


        for(int i = 0; i < only_file_from_absolute_path_of_long_pressed_dir.size(); i++){
            Log.v(TAG, "File " + i + " from only_file_from_absolute_path_of_long_pressed_dir: " + only_file_from_absolute_path_of_long_pressed_dir.get(i));
        }
        Log.v(TAG, "absolute_path_of_long_pressed_dir: " + absolute_path_of_long_pressed_dir);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        //args.putString("file_to_play", file_to_play);
        //args.putString("absolute_path", absolute_path);
        //args.putStringArrayList("fileS_to_palyArrayList", fileS_to_palyArrayList);
        args.putStringArrayList("only_file_from_absolute_path_of_long_pressed_dir", only_file_from_absolute_path_of_long_pressed_dir);
        args.putString("absolute_path_of_long_pressed_dir", absolute_path_of_long_pressed_dir);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        //file_to_play = getArguments().getString("file_to_play");
        //absolute_path = getArguments().getString("absolute_path");
        //fileS_to_palyArrayList = getArguments().getStringArrayList("fileS_to_palyArrayList");
        only_file_from_absolute_path_of_long_pressed_dir = getArguments().getStringArrayList("only_file_from_absolute_path_of_long_pressed_dir");
        absolute_path_of_long_pressed_dir = getArguments().getString("absolute_path_of_long_pressed_dir");

        int pathSeparator_position;
        String only_file_name_from_file_to_play;
        AlertDialog.Builder builder;

        //Log.v("TAG", "file_to_play to:" + file_to_play);
        StringBuilder myStringBuilder = new StringBuilder();
        String dialogMessageString = "";

        if (only_file_from_absolute_path_of_long_pressed_dir.size() == 1) {
            dialogMessageString = only_file_from_absolute_path_of_long_pressed_dir.get(0);
        } else if (only_file_from_absolute_path_of_long_pressed_dir.size() == 2) {
            myStringBuilder.append(only_file_from_absolute_path_of_long_pressed_dir.get(0));
            myStringBuilder.append(", ");
            myStringBuilder.append(only_file_from_absolute_path_of_long_pressed_dir.get(1));
            dialogMessageString = myStringBuilder.toString();
        } else if (only_file_from_absolute_path_of_long_pressed_dir.size() > 2) {
            for (int i = 0; i < 2; i++) {    // not fileS_to_palyArrayList.size() because it can by really large and I need this only for dialog message
                pathSeparator_position = only_file_from_absolute_path_of_long_pressed_dir.get(i).lastIndexOf("/");
                myStringBuilder.append(only_file_from_absolute_path_of_long_pressed_dir.get(i).substring(pathSeparator_position + 1));
                myStringBuilder.append(", ");
            }
            myStringBuilder.append("...");
            dialogMessageString = myStringBuilder.toString();
        }

        builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.text_for_DIALOG_PLAY_A_FILE_from_FileChooser) + " " + dialogMessageString + "?")
                //.setCancelable(false)
                .setPositiveButton(R.string.text_for_positiveButton_from_DIALOG_PLAY_A_FILE, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().stopService(new Intent(getActivity(), com.mplayer_remote.ServicePlayAFile.class));
                        ConnectToServer.sendCommandAndWaitForExitStatus("echo stop > fifofile");


                        for (int i = 0; i < only_file_from_absolute_path_of_long_pressed_dir.size(); i++) {

                            String file_to_play = new String();
                            file_to_play = absolute_path_of_long_pressed_dir + "/" + only_file_from_absolute_path_of_long_pressed_dir.get(i);

                            Intent intent_start_ServicePlayAFile = new Intent(getActivity(), ServicePlayAFile.class);
                            intent_start_ServicePlayAFile.putExtra("file_to_play", file_to_play);
                            intent_start_ServicePlayAFile.putExtra("absolute_path", absolute_path_of_long_pressed_dir);
                            getActivity().startService(intent_start_ServicePlayAFile);
                            //Log.v(TAG, "startuje ServicePlayAFile z plikiem " + file_to_play);

                        }
                    }
                })
                .setNegativeButton(R.string.text_for_negativeButton_from_DIALOG_PLAY_A_FILE, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //dialog.cancel();
                    }
                });
        return builder.create();
    }

}
