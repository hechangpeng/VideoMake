package com.videocreator.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.videocreator.example.R;

import java.io.File;

/**
 * Date：2018/4/19
 * Author：HeChangPeng
 * Describe：主要是使用谷歌的ExoPlayer来播放本地或网络视频
 */
public class ExoPlayerActivity extends Activity {
    private SimpleExoPlayerView playerView;
    private SimpleExoPlayer player;
    private static final String URL = "http://192.168.1.51:8080/server/files/screen_1527761053547.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exoplayer);
        playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        initPlayer();
    }

    private void initPlayer() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        DefaultBandwidthMeter xbandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory xdataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, this.getApplicationInfo().name), xbandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        String file = Environment.getExternalStorageDirectory() + "/videocreator/screen_1527825220922.mp4";
        if (!(new File(file).exists())) {
            Toast.makeText(this, "please makevideo first by click record button", Toast.LENGTH_SHORT).show();
            return;
        }
        MediaSource xvideoSource = new ExtractorMediaSource(Uri.parse(file), xdataSourceFactory, extractorsFactory, null, null);
        player.prepare(xvideoSource);
        player.setPlayWhenReady(true);

        playerView.setUseController(true);
        playerView.requestFocus();
        playerView.setPlayer(player);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }
}

