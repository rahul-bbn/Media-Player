package rahul.cyntech.mediaplayer;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;


public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    TextView title;
    ArrayList<MediaFiles> mVideoFiles = new ArrayList<>();
    PlayerView playerView;
    SimpleExoPlayer simpleExoPlayer;
    int position;
    String videoTitle;
    ImageView videoBack, lock, unlock, scaling ,videoList;
    VideosFileAdapter videosFileAdapter;

    RelativeLayout root;
    ConcatenatingMediaSource concatenatingMediaSource;
    ImageView nextBtn, preBtn;


    // horizental recyclerview variables

    private ArrayList<IconModel> iconModelArrayList = new ArrayList<>();
    PlaybackIconAdapter playbackIconAdapter;
    RecyclerView recyclerViewIcon;
    boolean expand = false;
    View nightMode;
    boolean dark = false;
    boolean mute = false;
    PlaybackParameters parameters;
    float speed;
//    DialogProperties dialogProperties;
//    FilePickerDialog filePickerDialog;
//    Uri uriSubTitle;

    // horizental recyclerview variables

    private ControlsMode controlsMode;

    public enum ControlsMode {
        LOCK, FULLSCREEN;
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_video_player);
        getSupportActionBar().hide();
        playerView = findViewById(R.id.exoplayer_view);
        nextBtn = findViewById(R.id.exo_next);
        preBtn = findViewById(R.id.exo_prev);

        position = getIntent().getIntExtra("position", 1);
        videoTitle = getIntent().getStringExtra("video_title");
        mVideoFiles = getIntent().getExtras().getParcelableArrayList("videoArrayList");
        screenOrientation();

        title = findViewById(R.id.video_title);
        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
        scaling = findViewById(R.id.scaling);
        root = findViewById(R.id.root_layout);
        nightMode = findViewById(R.id.nightMode);
         videoList = findViewById(R.id.video_list);


        title.setText(videoTitle);

        recyclerViewIcon = findViewById(R.id.recyclerview_icon);


        /////Register nextBTn and preBTn to setOnclickListener///
        nextBtn.setOnClickListener(this);
        preBtn.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        videoList.setOnClickListener(this);
        scaling.setOnClickListener(firstListener);

//        dialogProperties = new DialogProperties();


        iconModelArrayList.add(new IconModel(R.drawable.right, ""));
        iconModelArrayList.add(new IconModel(R.drawable.night_mode, "Night"));
        iconModelArrayList.add(new IconModel(R.drawable.volume_off, "Mute"));
        iconModelArrayList.add(new IconModel(R.drawable.screen_rotation, "Rotate"));

        playbackIconAdapter = new PlaybackIconAdapter(iconModelArrayList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, true);
        recyclerViewIcon.setLayoutManager(linearLayoutManager);
        recyclerViewIcon.setAdapter(playbackIconAdapter);
        playbackIconAdapter.notifyDataSetChanged();

        playbackIconAdapter.setOnItemClickListener(new PlaybackIconAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                if (position == 0)

                    if (expand) {
                        iconModelArrayList.clear();
                        iconModelArrayList.add(new IconModel(R.drawable.right, ""));
                        iconModelArrayList.add(new IconModel(R.drawable.night_mode, "Night"));
                        iconModelArrayList.add(new IconModel(R.drawable.volume_off, "Mute"));
                        iconModelArrayList.add(new IconModel(R.drawable.screen_rotation, "Rotate"));
                        playbackIconAdapter.notifyDataSetChanged();
                        expand = false;
                    } else {
                        if (iconModelArrayList.size() == 4) {
                            iconModelArrayList.add(new IconModel(R.drawable.volume_up, "Volume"));
                            iconModelArrayList.add(new IconModel(R.drawable.brightness, "Brightness"));
                            iconModelArrayList.add(new IconModel(R.drawable.equalizer, "Equalizer"));
                            iconModelArrayList.add(new IconModel(R.drawable.fast_forward, "Speed"));
                            iconModelArrayList.add(new IconModel(R.drawable.subtitles, "SubTitle"));
                        }
                        iconModelArrayList.set(position, new IconModel(R.drawable.left, ""));
                        playbackIconAdapter.notifyDataSetChanged();
                        expand = true;
                    }

                if (position == 1) {
                    if (dark) {
                        nightMode.setVisibility(View.GONE);
                        iconModelArrayList.set(position, new IconModel(R.drawable.night_mode, "Night"));
                        playbackIconAdapter.notifyDataSetChanged();
                        dark = false;
                    } else {

                        nightMode.setVisibility(View.VISIBLE);
                        iconModelArrayList.set(position, new IconModel(R.drawable.night_mode, "Day"));
                        playbackIconAdapter.notifyDataSetChanged();
                        dark = true;
                    }
                }


                if (position == 2) {
                    if (mute) {
                        simpleExoPlayer.setVolume(100);
                        iconModelArrayList.set(position, new IconModel(R.drawable.volume_off, "Mute"));
                        playbackIconAdapter.notifyDataSetChanged();
                        mute = false;
                    } else {
                        simpleExoPlayer.setVolume(0);
                        iconModelArrayList.set(position, new IconModel(R.drawable.volume_up, "UnMute"));
                        playbackIconAdapter.notifyDataSetChanged();
                        mute = true;
                    }
                }
                if (position == 3) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        playbackIconAdapter.notifyDataSetChanged();
                    } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                        playbackIconAdapter.notifyDataSetChanged();
                    }
                }
                if (position == 4) {
                    VolumeDialog volumeDialog = new VolumeDialog();
                    volumeDialog.show(getSupportFragmentManager(), "dialog");
                    playbackIconAdapter.notifyDataSetChanged();
                }
                if (position == 5) {
                    BrightenessDialog brightenessDialog = new BrightenessDialog();
                    brightenessDialog.show(getSupportFragmentManager(), "Dialog");
                    playbackIconAdapter.notifyDataSetChanged();
                }
                if (position == 6) {
                    Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                    if ((intent.resolveActivity(getPackageManager()) != null)) {
                        startActivityForResult(intent, 123);
                    } else {
                        Toast.makeText(VideoPlayerActivity.this, "No equalizer found", Toast.LENGTH_SHORT).show();
                    }
                    playbackIconAdapter.notifyDataSetChanged();
                }
                if (position == 7) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(VideoPlayerActivity.this);
                    alertDialog.setTitle("Select Playback Speed").setPositiveButton("Ok ", null);
                    String[] items = {"0.5x", "1x Normal Speed", "1.25x", "1.5x", "2x"};
                    int checkedItem = -1;
                    alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    speed =0.5f;
                                    parameters=new PlaybackParameters(speed);
                                    simpleExoPlayer.setPlaybackParameters(parameters);
                                    break;
                                case 1:
                                    speed =1.0f;
                                    parameters=new PlaybackParameters(speed);
                                    simpleExoPlayer.setPlaybackParameters(parameters);
                                    break;
                                case 2:
                                    speed =1.25f;
                                    parameters=new PlaybackParameters(speed);
                                    simpleExoPlayer.setPlaybackParameters(parameters);
                                    break;
                                case 3:
                                    speed =1.5f;
                                    parameters=new PlaybackParameters(speed);
                                    simpleExoPlayer.setPlaybackParameters(parameters);
                                    break;
                                case 4:
                                    speed =2.0f;
                                    parameters=new PlaybackParameters(speed);
                                    simpleExoPlayer.setPlaybackParameters(parameters);
                                    break;
                                default: break;

                            }
                        }
                    });
                    AlertDialog alert = alertDialog.create();
                    alert.show();
                }
                if (position==8){

                }else{}

            }
        });


        playerVideo();
    }

    private void playerVideo() {
        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();

        //////////// For    Progressive    Media   Files
        DefaultDataSourceFactory defaultDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
        //////////// For    Progressive    Media   Files

        concatenatingMediaSource = new ConcatenatingMediaSource();

        for (int i = 0; i < mVideoFiles.size(); i++) {
            new File(String.valueOf(mVideoFiles.get(i)));

            ///////////// MediaSource Define the Media to be Played and Load the Media
            ////////////  DefaultDataSourceFactory For Progressive Media   Files And Smooth Streaming
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(defaultDataSourceFactory).createMediaSource(Uri.parse(String.valueOf(uri)));
            //////////// For    Progressive    Media   Files And Smooth Streaming
            concatenatingMediaSource.addMediaSource(mediaSource);
        }

        playerView.setPlayer(simpleExoPlayer);
        playerView.setKeepScreenOn(true);
        simpleExoPlayer.setPlaybackParameters(parameters);
        simpleExoPlayer.prepare(concatenatingMediaSource);
//        simpleExoPlayer.setMediaSource(concatenatingMediaSource);
        simpleExoPlayer.seekTo(position, C.TIME_UNSET);
        playError();

    }

    private void screenOrientation() {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Bitmap bitmap ;
            String path = mVideoFiles.get(position).getPath();
            Uri uri = Uri.parse(path);
            retriever.setDataSource(this,uri);
            bitmap = retriever.getFrameAtTime();
            int videoWidth = bitmap.getWidth();
            int videoHeight = bitmap.getHeight();
            if (videoHeight < videoWidth){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            }else{
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }catch(Exception e){
            Log.e("MediaMetaDataRetriever","Screen orientation :");
        }
    }
    private void playError() {
        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Video Playing Error", Toast.LENGTH_SHORT);
            }
        });
        simpleExoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (simpleExoPlayer.isPlaying()) {
            simpleExoPlayer.stop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        simpleExoPlayer.setPlayWhenReady(false);
        simpleExoPlayer.getPlaybackState();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.getPlaybackState();
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_back:

                if (simpleExoPlayer != null) {
                    simpleExoPlayer.release();
                }
                finish();
                break;
            case R.id.video_list:
                PlayListDialog playListDialog =new PlayListDialog(mVideoFiles,videosFileAdapter);
                playListDialog.show(getSupportFragmentManager(),playListDialog.getTag());

                break;
            case R.id.lock:
                controlsMode = ControlsMode.FULLSCREEN;
                root.setVisibility(View.VISIBLE);
                lock.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Unlocked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.unlock:
                controlsMode = ControlsMode.LOCK;
                root.setVisibility(View.INVISIBLE);
                lock.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.exo_next:
                try {
                    simpleExoPlayer.stop();
                    position++;
                    playerVideo();
                } catch (Exception e) {
                    Toast.makeText(VideoPlayerActivity.this, "No Next Video", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case R.id.exo_prev:
                try {
                    simpleExoPlayer.stop();
                    position--;
                    playerVideo();
                } catch (Exception e) {
                    Toast.makeText(this, "No Previous Video", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    View.OnClickListener firstListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            simpleExoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fullscreen);
            Toast.makeText(VideoPlayerActivity.this, "Full Screen", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(secondListener);


        }
    };
    View.OnClickListener secondListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            simpleExoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.ic_zoom_in_);
            Toast.makeText(VideoPlayerActivity.this, "Zoom", Toast.LENGTH_SHORT);
            scaling.setOnClickListener(thirdListener);
        }
    };
    View.OnClickListener thirdListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            simpleExoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fit);
            Toast.makeText(VideoPlayerActivity.this, "Fit", Toast.LENGTH_SHORT);
            scaling.setOnClickListener(firstListener);
        }

    };
}