package com.mplayer_remote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KnowFileExtensionsDialogFragment extends DialogFragment {

    String TAG = "KnowFileExtensionsDialogFragment";
    private static final String FILENAME = "userSelectedMediaFileExtensions";

    ArrayList<String> defaultAllKnowMediaFileExtensionsArrayList;   //Sorted items from KnowMediaFileExtensions.xml
    CharSequence[] defaultAllKnowMediaFileExtensionsCharSequence;   //Sorted items from KnowMediaFileExtensions.xml, parm of AlertDialog.Builder.setMultiChoiceItems
    boolean[] checkedFileExtensionbooleanarray;     //Where we track the selected items
    //ArrayList selectedMediaFileExtensionsArrayList;         // Where we track the selected items
    SharedPreferences checkedFileExtensionsSharedPreferences;   //Where are saved selected by user file extensions

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v(TAG, "tworze KnowFileExtensionsDialogFragment");

        String[] tempArray = getResources().getStringArray(R.array.know_media_file_extensions);
        Arrays.sort(tempArray);
        List<String> tempList = new ArrayList<String>();
        tempList = Arrays.asList(tempArray);
        defaultAllKnowMediaFileExtensionsCharSequence = tempList.toArray(new CharSequence[tempList.size()]);
        defaultAllKnowMediaFileExtensionsArrayList = new ArrayList<String>(tempList);
        checkedFileExtensionbooleanarray = new boolean[defaultAllKnowMediaFileExtensionsArrayList.size()];


        File f = new File("/data/data/" + getActivity().getPackageName() +  "/shared_prefs/" + FILENAME + ".xml");
        if (f.exists() == false){
            Log.v(TAG,"f.exists() = false");

            for(int i = 0; i < defaultAllKnowMediaFileExtensionsArrayList.size(); i++){
                checkedFileExtensionbooleanarray[i] = true;
            }

            checkedFileExtensionsSharedPreferences = getActivity().getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = checkedFileExtensionsSharedPreferences.edit();
            for (int i = 0; i < defaultAllKnowMediaFileExtensionsArrayList.size(); i++){
                editor.putBoolean(defaultAllKnowMediaFileExtensionsArrayList.get(i), checkedFileExtensionbooleanarray[i]);
            }
            editor.commit();

        }else{
            Log.v(TAG,"f.exists() = true");

            checkedFileExtensionsSharedPreferences = getActivity().getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
            for (int i = 0; i < defaultAllKnowMediaFileExtensionsArrayList.size(); i++){
                checkedFileExtensionbooleanarray[i] = checkedFileExtensionsSharedPreferences.getBoolean(defaultAllKnowMediaFileExtensionsArrayList.get(i), true);
                //Log.v(TAG, Boolean.toString(checkedFileExtensionbooleanarray[i]));
            }
        }





        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.text_for_title_of_dialog_choose_know_file_extensions)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(defaultAllKnowMediaFileExtensionsCharSequence, checkedFileExtensionbooleanarray,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items

                                    checkedFileExtensionbooleanarray[which] = true;
                                } else if (checkedFileExtensionbooleanarray[which] == true) {
                                    // Else, if the item is already in the array, remove it

                                    checkedFileExtensionbooleanarray[which] = false;
                                }
                            }
                        })
                        // Set the action buttons
                .setPositiveButton(R.string.text_for_ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the checkedFileExtensionbooleanarray
                        checkedFileExtensionsSharedPreferences = getActivity().getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = checkedFileExtensionsSharedPreferences.edit();
                        for (int i = 0; i < defaultAllKnowMediaFileExtensionsArrayList.size(); i++){
                            editor.putBoolean(defaultAllKnowMediaFileExtensionsArrayList.get(i), checkedFileExtensionbooleanarray[i]);
                        }
                        editor.commit();
                        /*
                        for (int i = 0; i < checkedFileExtensionbooleanarray.length; i++){
                            Log.v(TAG, Boolean.toString(checkedFileExtensionbooleanarray[i]));
                        }
                        */
                    }
                })
                .setNegativeButton(R.string.text_for_cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.create();
    }
}