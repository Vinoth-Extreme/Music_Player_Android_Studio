package com.example.music_player_02;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listview;
    String[] songs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listview = (ListView) findViewById(R.id.listview);
        runtimePermission();
    }

    public void runtimePermission() {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySongs();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public ArrayList<File> findSong(File file) {
        ArrayList<File> arrList = new ArrayList<>();
        File[] files = file.listFiles();
        for(File singleFile: files) {
            if(singleFile.isDirectory() && !singleFile.isHidden()) {
                arrList.addAll(findSong(singleFile));
            } else {
                if(singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                    arrList.add(singleFile);
                }
            }
        }
        return arrList;
    }

    public void displaySongs() {
        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());
        songs = new String[mySongs.size()];
        for(int i = 0; i < mySongs.size(); i++) {
            songs[i] = mySongs.get(i).getName().replace(".mp3", "").replace(".wav", "");
        }

        customAdapter customAdapter = new customAdapter();
        listview.setAdapter(customAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songTitle = (String) listview.getItemAtPosition(position);
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                    .putExtra("songs", mySongs)
                        .putExtra("songname", songTitle)
                        .putExtra("pos", position)
                );
            }
        });
    }



    class customAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return songs.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.list_items, null);
            TextView txtSong = view.findViewById(R.id.txtSong);
            txtSong.setSelected(true);
            txtSong.setText(songs[position]);
            return view;
        }
    }
}