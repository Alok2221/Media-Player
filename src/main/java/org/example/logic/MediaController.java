package org.example.logic;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.example.AppMusicPlayer;
import org.example.user_interface.UIComponents;
import org.example.user_interface.Visualization;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MediaController {
    private final AppMusicPlayer app;
    private MediaPlayer mediaPlayer;
    private Media media;
    private boolean isMuted = false;
    private boolean isDragging = false;
    private final Visualization visualization;
    private boolean visualizationActive = true;

    public MediaController(AppMusicPlayer app) {
        this.app = app;
        this.visualization = new Visualization();
        initializeJFXPanel();
    }

    private void initializeJFXPanel() {
        JFXPanel jfxPanel = new JFXPanel();
        jfxPanel.setPreferredSize(new Dimension(640, 150));

        Group root = visualization.getRoot();
        Scene scene = new Scene(root, 640, 360);
        scene.setFill(javafx.scene.paint.Color.BLACK);

        jfxPanel.setScene(scene);
        app.getUIComponents().getMainPanel().add(jfxPanel, BorderLayout.CENTER);
    }

    public void loadMediaFile(String filePath) {
        stopMedia();

        try {
            File file = new File(filePath);
            media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> Platform.runLater(() -> {
                UIComponents ui = app.getUIComponents();
                ui.getNowPlayingLabel().setText("Now Playing: " + file.getName());
                ui.getStatusLabel().setText("Ready to play");
                ui.getPlayPauseButton().setEnabled(true);
                ui.getStopButton().setEnabled(true);
                updateTimeLabel(Duration.ZERO, media.getDuration());
            }));

            mediaPlayer.setVolume(app.getUIComponents().getVolumeSlider().getValue() / 100.0);
            setupMediaPlayerEvents();
        } catch (Exception e) {
            showError("Could not load media: " + e.getMessage());
        }
    }

    private void setupMediaPlayerEvents() {
        mediaPlayer.setOnPlaying(() -> updateStatus("Playing", "Pause"));
        mediaPlayer.setOnPaused(() -> updateStatus("Paused", "Play"));
        mediaPlayer.setOnStopped(() -> updateStatus("Stopped", "Play"));
        mediaPlayer.setOnEndOfMedia(() -> app.getPlaylistManager().playNextTrack());
        mediaPlayer.setOnError(() -> showError("Playback error: " + mediaPlayer.getError()));

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!isDragging && media != null) {
                double progress = newTime.toMillis() / media.getDuration().toMillis();
                app.getUIComponents().getProgressSlider().setValue((int) (progress * 100));
                updateTimeLabel(newTime, media.getDuration());
            }
        });

        if (visualizationActive) {
            enableVisualization();
        }
    }

    private void updateStatus(String status, String playPauseText) {
        UIComponents ui = app.getUIComponents();
        ui.getStatusLabel().setText(status);
        ui.getPlayPauseButton().setText(playPauseText);
    }

    public void playMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        } else if (!app.getPlaylistManager().getMediaFiles().isEmpty()) {
            loadMediaFile(app.getPlaylistManager().getCurrentFilePath());
            playMedia();
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer == null) {
            playMedia();
            return;
        }

        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.play();
        }
    }

    public void pauseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void stopMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            app.getUIComponents().getProgressSlider().setValue(0);
            updateTimeLabel(Duration.ZERO, media.getDuration());
        }
    }

    public void toggleMute() {
        if (mediaPlayer != null) {
            isMuted = !isMuted;
            mediaPlayer.setMute(isMuted);
            app.getUIComponents().getMuteButton().setText(isMuted ? "Unmute" : "Mute");
        }
    }

    public void seekMedia() {
        if (mediaPlayer != null && media != null) {
            double seekMillis = app.getUIComponents().getProgressSlider().getValue() / 100.0 * media.getDuration().toMillis();
            mediaPlayer.seek(new Duration(seekMillis));
        }
    }

    public void updateTimeLabel(Duration current, Duration total) {
        app.getUIComponents().getTimeLabel().setText(formatTime(current) + " / " + formatTime(total));
    }

    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void toggleVisualization() {
        visualizationActive = !visualizationActive;
        if (mediaPlayer != null) {
            if (visualizationActive) {
                enableVisualization();
            } else {
                mediaPlayer.setAudioSpectrumListener(null);
            }
        }
    }

    private void enableVisualization() {
        mediaPlayer.setAudioSpectrumListener((timestamp, duration, magnitudes, phases) ->
                Platform.runLater(() -> visualization.update(magnitudes))
        );
        mediaPlayer.setAudioSpectrumNumBands(64);
        mediaPlayer.setAudioSpectrumInterval(0.1);
        mediaPlayer.setAudioSpectrumThreshold(-60);
    }

    public void cleanup() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
    }

    public void setDragging(boolean dragging) {
        isDragging = dragging;
    }

    public boolean isDragging() {
        return isDragging;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    private void showError(String message) {
        app.getUIComponents().getStatusLabel().setText("Error");
        JOptionPane.showMessageDialog(app, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
