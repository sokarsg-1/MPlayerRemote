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

import java.util.ArrayList;



import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Aktywność pozwalająca przeglądać katalogi znajdujące się na serwerze, z którym aplikacja MPlayerRemote się połączyła oraz wskazanie pliku z napisami do odtwarzanego w programie <code>MPlayer</code> filmu. 
 * @author sokar
 * @see android.app.Activity
 * @see android.app.ListActivity
 */
public class SubtitleFileChooser extends ListActivity{
		//w celach diagnostycznych nazwa logu dla tego Activity
	private static final String TAG = "SubtitleFileChooser";
		//ArreaList z zawartościa obecnie przeglądanego katalogu
	/**
	 *	Lista zawierająca nazwy wszystkich plików i folderów znajdujących się w obecnie przeglądanym przez użytkownika katalogu. 
	 */
	private ArrayList<String> dir_contain = new ArrayList<String>();
	
	/**
	 * Lista zawierająca nazwy wszystkich plików znajdujących się w obecnie przeglądanym przez użytkownika katalogu.
	 */
	private ArrayList<String> only_file_from_absolute_path = new ArrayList<String>();
	

	/**
	 * Łańcuch znaków zawierający ścieżkę absolutną, w której znajduje się plik wskazany przez użytkownika do odtworzenia w programie MPlayer. Need to by returned to RemoteControl.
	 */
	private String absolute_path = "";
	
	/**
	 * Łańcuch znaków zawierający pełna nazwę wraz ze ścieżką absolutną prowadzącą do pliku wskazanego przez użytkownika w celu odtworzenia go w programie MPlayer. Need to by returned to RemoteControl.
	 */
	private String file_to_play = "";

	//where i am now

	private String absolute_path_to_subtitle = "";

	private String subtitle_to_load = "";
	
	/**
	 * Nazwa ostatnio wskazanego pliku lub folderu wyświetlana przez GUI aktywności jako jeden z elementów <code>ListView</code>. 
	 */	
	private String string_from_selected_view_in_ListView = "";
	
	
	/** 
	 * Metoda wywoływana przez system Android przy starcie aktywności.
	 * Na podstawie danych zwróconych przez serwer tworzy GUI zawierające listę plików i folderów zawartych w katalogu domowym użytkownika.
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(false);


		if (savedInstanceState == null){
	        Intent intent_from_RemoteControl = getIntent(); //getIntent() zwraca obiekt Intent który wystartował Activity
			absolute_path = intent_from_RemoteControl.getStringExtra("absolute_path");
			file_to_play = intent_from_RemoteControl.getStringExtra("file_to_play");

	        absolute_path_to_subtitle = intent_from_RemoteControl.getStringExtra("absolute_path");
	        
	        Log.v(TAG, "Przekazany w intent absolute_path_to_subtitle = " + absolute_path_to_subtitle);
	      	      
	        dir_contain = new ArrayList<String>();//erasing
	        dir_contain.add("..");
	        ConnectToServer.sendCommandAndSaveOutputToArrayList("ls --group-directories-first  " + "'" + absolute_path_to_subtitle + "/" + "'",dir_contain);
	        ConnectToServer.sendCommandAndSaveOutputToArrayList("ls -p " + "'" + absolute_path_to_subtitle + "/" + "'" + "| grep -v /",only_file_from_absolute_path);
	        		
			for (int i = 0; i < dir_contain.size(); i++){
				Log.v(TAG, dir_contain.get(i));
			}
			
        }else{
			absolute_path = savedInstanceState.getString("absolute_path");
			file_to_play = savedInstanceState.getString("file_to_play");
        	absolute_path_to_subtitle = savedInstanceState.getString("absolute_path_to_subtitle");
        	subtitle_to_load = savedInstanceState.getString("subtitle_to_load");
        	Log.v(TAG,"absolute_path_to_subtitle from savedInstanceState: " + absolute_path_to_subtitle);
        	
	        dir_contain = new ArrayList<String>();//erasing
	        dir_contain.add("..");
	        ConnectToServer.sendCommandAndSaveOutputToArrayList("ls --group-directories-first  " + "'" + absolute_path_to_subtitle + "/" + "'",dir_contain);
	        ConnectToServer.sendCommandAndSaveOutputToArrayList("ls -p " + "'" + absolute_path_to_subtitle + "/" + "'" + "| grep -v /",only_file_from_absolute_path);
	        		
			for (int i = 0; i < dir_contain.size(); i++){
				Log.v(TAG, dir_contain.get(i));
			}
        }
        
			// ustawianie GUI
		setListAdapter(new MyBaseAdapter(dir_contain, only_file_from_absolute_path));
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		
    }

    /**
     * Metoda wywoływana kiedy użytkownik wybierze dowolną pozycje z listy wyświetlanej przez aktywność. 
     * W przypadku wybrania pliku z napisami do odtwarzacza <code>MPlayer</code> zostanie wysłane polecenie wczytania i wyświetlenia tych napisów na tle odtwarzanego filmu.
     * Jeśli użytkownik wskaże katalog, to do serwera zostaną wysłane polecenia, które wydrukują na <code>stdout</code> zawartość tego katalogu.
     * Na podstawie tych danych metoda aktualizuje graficzny interfejs użytkownika wyświetlany przez aktywność.   
     * 
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		string_from_selected_view_in_ListView = dir_contain.get(position);		///name of file or dir that was choose by user
		
		only_file_from_absolute_path = new ArrayList<String>();
		ConnectToServer.sendCommandAndSaveOutputToArrayList("ls -p " + "'" + absolute_path_to_subtitle + "/" + "'" + "| grep -v /",only_file_from_absolute_path);
		if  (only_file_from_absolute_path.indexOf(string_from_selected_view_in_ListView) != -1){
			subtitle_to_load = absolute_path_to_subtitle + "/" + string_from_selected_view_in_ListView;
			Log.v(TAG,"subtitle to load: " + subtitle_to_load);
			Log.v(TAG,"absolute_path_to_subtitle to: " + absolute_path_to_subtitle);
			
			if (isMyServiceRunning() == true){
				String subtitleFilePathWithBackslash = subtitle_to_load.replace(" ", "\\ ");		//mplayer do not understand bash quoting, so space must be replaced by backslash space characters
				Log.v(TAG, "path containing backslash space instead space to file  with subtitle: " + subtitleFilePathWithBackslash);
				ConnectToServer.sendCommandAndWaitForExitStatus("echo pausing_keep sub_remove > fifofile");
				ConnectToServer.sendCommandAndWaitForExitStatus("echo pausing_keep sub_load " + "'" + subtitleFilePathWithBackslash + "'" + " > fifofile");
				ConnectToServer.sendCommandAndWaitForExitStatus("echo pausing_keep sub_file > fifofile");
				Intent intent_start_RemoteControl = new Intent(getApplicationContext(), RemoteControl.class);
				intent_start_RemoteControl.putExtra("absolute_path", absolute_path);
				intent_start_RemoteControl.putExtra("file_to_play", file_to_play);
				startActivity(intent_start_RemoteControl);
				this.finish();
			}else{
				this.finish();
    		}
		}else if (ConnectToServer.sendCommandAndWaitForExitStatus("cd " + "'" + absolute_path_to_subtitle + "/" + string_from_selected_view_in_ListView + "/" + "'") == 0){
			absolute_path_to_subtitle = absolute_path_to_subtitle + "/" + string_from_selected_view_in_ListView;
			Log.v(TAG,"obecne absolute_path_to_subtitle to: " + absolute_path_to_subtitle);
			dir_contain = new ArrayList<String>();
			dir_contain.add("..");
			ConnectToServer.sendCommandAndSaveOutputToArrayList("ls --group-directories-first  " + "'" + absolute_path_to_subtitle + "/" + "'", dir_contain);
			only_file_from_absolute_path = new ArrayList<String>();
			ConnectToServer.sendCommandAndSaveOutputToArrayList("ls -p " + "'" + absolute_path_to_subtitle + "/" + "'" + "| grep -v /",only_file_from_absolute_path);
			setListAdapter(new MyBaseAdapter(dir_contain, only_file_from_absolute_path));
		}else{
			Toast.makeText(getApplicationContext(), R.string.text_for_toast_you_do_not_have_rights_to_open_this_directory, Toast.LENGTH_SHORT).show();
		}
		
	}
    
    /**
     * Sprawdza czy usługa ServicePlayAFile działa w tle.
     * @return <code>true</code> jeśli usługa ServicePlayAFile działa w tle, <code>false</code> w przeciwnym wypadku.
     */
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.mplayer_remote.ServicePlayAFile".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    /** 
     * Metoda wywoływana przez system Android przed zniszczeniem aktywności, służy do zapamiętywania stanu aktywności, tu konkretnie miejsca w drzewie katalogów serwera, które jest aktualnie wyświetlane przez GUI. 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle instanceStateToSave){
    	instanceStateToSave.putString("absolute_path_to_subtitle", absolute_path_to_subtitle);
    	instanceStateToSave.putString("subtitle_to_load", subtitle_to_load);
		instanceStateToSave.putString("absolute_path", absolute_path);
		instanceStateToSave.putString("file_to_play", file_to_play);
	}
    
    /**
     * Klasa pomocnicza, pośrednicząca pomiędzy listami katalogów i plików oraz samych plików, a <code>ListView</code>(pionowa przewijalna lista) wyświetlanym przez aktywność SubtitleFileChooser.  
     * Decyduje jak ma wyglądać dany element listy.
     * @see android.app.ListActivity
     * @see android.widget.ListView
     * @see android.widget.BaseAdapter
     * @author sokar
     */
    private class MyBaseAdapter extends BaseAdapter{
    	private ArrayList<String>dir_contain;
    	private ArrayList<String>only_file_from_absolute_path;
    	private LayoutInflater mInflater;
    	private static final int TYPE_DIR = 0;
        private static final int TYPE_FILE = 1;
        private static final int TYPE_MAX_COUNT = 2;
        
    	public MyBaseAdapter(ArrayList<String> dir_contain, ArrayList<String> only_file_from_absolute_path){
    		this.dir_contain = dir_contain;
    		this.only_file_from_absolute_path = only_file_from_absolute_path;
    		mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	}
		@Override
		public int getCount() {
			
			return dir_contain.size();
		}

		@Override
		public Object getItem(int position) {
			
			return dir_contain.get(position);
		}

		@Override
		public long getItemId(int position) {
			
			return position;
		}
		@Override
		public int getItemViewType (int position){
			if (only_file_from_absolute_path.indexOf(dir_contain.get(position)) == -1){
				return TYPE_DIR;
			}else{
				return TYPE_FILE;
			}
			
		}
		@Override
        public int getViewTypeCount() {
            return TYPE_MAX_COUNT;
        }
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			int view_type = getItemViewType(position); 
			if(convertView == null){
				switch (view_type) {
					case TYPE_DIR: 
						convertView = mInflater.inflate(R.layout.layout_for_dir_list_item, null);
						holder = new ViewHolder();
						holder.textView = (TextView)convertView.findViewById(R.id.textView1);
		                
						break;
					case TYPE_FILE:
						convertView = mInflater.inflate(R.layout.layout_for_file_list_item, null);
						holder = new ViewHolder();
						holder.textView = (TextView)convertView.findViewById(R.id.textView2);
						break;
				}	
				convertView.setTag(holder);	
			} else {
                holder = (ViewHolder)convertView.getTag();
            }
			holder.textView.setText(dir_contain.get(position));
			return convertView;
		}
		
	    /**
	     * Klasa pomocnicza dla metody {@link com.mplayer_remote.SubtitleFileChooser.MyBaseAdapter#getView}. Implementacja wzorca projektowego ViewHolder.
	     * @author sokar
	     *
	     */
	    private class ViewHolder {
	        public TextView textView;
	        ImageView icon;

	    }
		
    }

}
