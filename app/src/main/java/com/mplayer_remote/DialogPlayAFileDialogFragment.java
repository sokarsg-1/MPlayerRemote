package com.mplayer_remote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by sokar on 29.09.15.
 */
public class DialogPlayAFileDialogFragment extends DialogFragment {

    private String file_to_play;
    private String absolute_path;

    /**
     * Create a new instance of DialogPlayAFileDialogFragment, providing "file_to_play" and "absolute_path"
     * as an argument.
     */
    static DialogPlayAFileDialogFragment newInstance(String file_to_play, String absolute_path) {
        DialogPlayAFileDialogFragment f = new DialogPlayAFileDialogFragment();

        // Supply input as an argument.
        Bundle args = new Bundle();
        args.putString("file_to_play", file_to_play);
        args.putString("absolute_path", absolute_path);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        file_to_play = getArguments().getString("file_to_play");
        absolute_path = getArguments().getString("absolute_path");

        int pathSeparator_position;
        String only_file_name_from_file_to_play;
        AlertDialog.Builder builder;


        Log.v("TAG", "file_to_play to:" + file_to_play);
        pathSeparator_position = file_to_play.lastIndexOf("/");
        only_file_name_from_file_to_play = file_to_play.substring(pathSeparator_position + 1);
        builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.text_for_DIALOG_PLAY_A_FILE_from_FileChooser) + " " + only_file_name_from_file_to_play + "?")
                //.setCancelable(false)
                .setPositiveButton(R.string.text_for_positiveButton_from_DIALOG_PLAY_A_FILE, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        boolean mBound = ((FileChooser)getActivity()).getmBound();

                        if (mBound == true){
                            ConnectAndPlayService mConnectAndPlayService = ((FileChooser) getActivity()).getmConnectAndPlayService();
                            mConnectAndPlayService.stopPlaying();
                            mConnectAndPlayService.playAFile(file_to_play, absolute_path);
                        }

                    }
                })
                .setNegativeButton(R.string.text_for_negativeButton_from_DIALOG_PLAY_A_FILE, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //dialog.cancel();
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}