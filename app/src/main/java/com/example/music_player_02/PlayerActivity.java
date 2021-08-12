package com.example.music_player_02;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
//import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button btnPlay, btnNext, btnPrevious, btnFastForward, btnFastBackward;
    TextView txtSongTitle, txtSongStart, txtSongEnd;
    SeekBar seekMusicBar;
//    BarVisualizer barVisualizer;
    ImageView imageView;
    String songName;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread SeekBarThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        btnPrevious = (Button) findViewById(R.id.btnPrevious);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnFastBackward = (Button) findViewById(R.id.btnFastBackward);
        btnFastForward = (Button) findViewById(R.id.btnFastForward);
        txtSongTitle = (TextView) findViewById(R.id.txtSong);
        txtSongStart = (TextView) findViewById(R.id.txtSongStart);
        txtSongEnd = (TextView) findViewById(R.id.txtSongEnd);
        seekMusicBar = (SeekBar) findViewById(R.id.seekbar);
//        barVisualizer = (BarVisualizer) findViewById(R.id.wave);
        imageView = (ImageView) findViewById(R.id.songImg);

        if(mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String sName = intent.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        txtSongTitle.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        songName = mySongs.get(position).getName();
        txtSongTitle.setText(songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        SeekBarThread = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;

                while(currentPosition < totalDuration) {
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekMusicBar.setProgress(currentPosition);
                    } catch(InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        seekMusicBar.setMax(mediaPlayer.getDuration());
        SeekBarThread.start();
        seekMusicBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_700), PorterDuff.Mode.MULTIPLY);
        seekMusicBar.getThumb().setColorFilter(getResources().getColor(R.color.purple_200), PorterDuff.Mode.SRC_IN);

        seekMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        txtSongEnd.setText(createTime(mediaPlayer.getDuration()));

        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                txtSongStart.setText(createTime(mediaPlayer.getCurrentPosition()));
                handler.postDelayed(this, delay);
            }
        }, delay);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()) {
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                } else {
                    btnPlay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                    TranslateAnimation anim = new TranslateAnimation(-25, 25, -25, 25);
                    anim.setInterpolator(new AccelerateInterpolator());
                    anim.setDuration(600);
                    anim.setFillEnabled(true);
                    anim.setFillAfter(true);
                    anim.setRepeatMode(Animation.REVERSE);
                    anim.setRepeatCount(1);
                    imageView.startAnimation(anim);
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnNext.performClick();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position + 1) % mySongs.size());
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                songName = mySongs.get(position).getName();
                txtSongTitle.setText(songName);
                mediaPlayer.start();

                rotateAnimation(imageView, 360f);
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position - 1) < 0) ? (mySongs.size() - 1) : (position - 1);
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                songName = mySongs.get(position).getName();
                txtSongTitle.setText(songName);
                mediaPlayer.start();

                rotateAnimation(imageView, -360f);
            }
        });
    }

    public void rotateAnimation(View view, Float degree) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, degree);
        objectAnimator.setDuration(1000);
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.playTogether(objectAnimator);
        animationSet.start();
    }

    public String createTime(int duration) {
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time = time + min + ":";
        if(sec < 10) {
            time += "0" + sec;
        } else {
            time += sec;
        }
        return time;
    }
}