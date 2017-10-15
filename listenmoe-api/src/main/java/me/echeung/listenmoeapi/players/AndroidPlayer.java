package me.echeung.listenmoeapi.players;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import me.echeung.listenmoeapi.APIClient;

import static com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC;
import static com.google.android.exoplayer2.C.USAGE_MEDIA;

public class AndroidPlayer implements StreamPlayer {

    private final Player.EventListener eventListener;
    private SimpleExoPlayer player;

    private Context context;
    private String streamUrl;

    public AndroidPlayer(Context context, String streamUrl) {
        this.context = context;
        this.streamUrl = streamUrl;

        this.eventListener = new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                // Try to reconnect to the stream
                final boolean wasPlaying = isPlaying();

                releasePlayer();

                init();
                if (wasPlaying) {
                    play();
                }
            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
            }

            @Override
            public void onPositionDiscontinuity() {
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            }
        };
    }

    @Override
    public boolean isStarted() {
        return player != null;
    }

    @Override
    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady();
    }

    @Override
    public boolean play() {
        if (player == null) {
            init();
        }

        if (!isPlaying()) {
            player.setPlayWhenReady(true);
            player.seekToDefaultPosition();

            return true;
        }

        return false;
    }

    @Override
    public boolean pause() {
        if (player != null) {
            player.setPlayWhenReady(false);

            return true;
        }

        return false;
    }

    @Override
    public boolean stop() {
        if (player != null) {
            player.setPlayWhenReady(false);

            releasePlayer();

            return true;
        }

        return false;
    }

    @Override
    public void duck() {
        if (player != null) {
            player.setVolume(0.5f);
        }
    }

    @Override
    public void unduck() {
        if (player != null) {
            player.setVolume(1f);
        }
    }

    private void init() {
        // In case there's already an instance somehow
        releasePlayer();

        final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, APIClient.USER_AGENT);
        final ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        final MediaSource streamSource = new ExtractorMediaSource(Uri.parse(streamUrl), dataSourceFactory, extractorsFactory, null, null);

        player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
        player.prepare(streamSource);
        player.addListener(eventListener);

        final AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(CONTENT_TYPE_MUSIC)
                .setUsage(USAGE_MEDIA)
                .build();
        player.setAudioAttributes(audioAttributes);
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player.removeListener(eventListener);
            player = null;
        }
    }
}
