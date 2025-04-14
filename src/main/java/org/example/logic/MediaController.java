package org.example.logic;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
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
    private MediaView mediaView;
    private boolean isMuted = false;
    private boolean isDragging = false;
    private final Visualization visualization;
    private boolean visualizationActive = true;
    private boolean isVideo = false;
    private static final String[] SUPPORTED_EXTENSIONS = {
            ".mp3", ".wav", ".aac", ".m4a",
            ".mp4", ".avi", ".mkv", ".mov"
    };

    public MediaController(AppMusicPlayer app) {
        this.app = app;
        this.visualization = new Visualization();
        initializeJFXPanel();
    }

    private void initializeJFXPanel() {
        JFXPanel jfxPanel = new JFXPanel();
        jfxPanel.setPreferredSize(new Dimension(640, 360));

        Platform.runLater(() -> {
            Group root = new Group();
            Scene scene = new Scene(root, 640, 360);
            scene.setFill(javafx.scene.paint.Color.BLACK);

            mediaView = new MediaView();
            root.getChildren().add(mediaView);
            root.getChildren().add(visualization.getRoot());

            jfxPanel.setScene(scene);
            scene.getProperties().put("jfxPanel", jfxPanel);

            SwingUtilities.invokeLater(() -> {
                app.getUIComponents().getMainPanel().add(jfxPanel, BorderLayout.CENTER);
                app.getUIComponents().getMainPanel().revalidate();
                app.getUIComponents().getMainPanel().repaint();
            });
        });
    }

    public void loadMediaFile(String filePath) {
        stopMedia();

        if (filePath == null || filePath.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                app.getUIComponents().setStatus("No file selected");
                showErrorDialog("Please select a valid media file");
            });
            return;
        }

        app.getUIComponents().setStatus("Loading...");
        updateTimeLabel(Duration.ZERO, Duration.ZERO); // Reset time display

        File file = new File(filePath);
        if (!file.exists()) {
            SwingUtilities.invokeLater(() -> {
                app.getUIComponents().setStatus("File not found");
                showErrorDialog("The selected file no longer exists: " + filePath);
                int index = app.getPlaylistManager().getMediaFiles().indexOf(filePath);
                if (index != -1) {
                    app.getPlaylistManager().removeAt(index);
                }
            });
            return;
        }

        new Thread(() -> {
            try {
                media = new Media(file.toURI().toString());
                isVideo = isVideoFile(filePath);

                Platform.runLater(() -> {
                    try {
                        mediaPlayer = new MediaPlayer(media);
                        mediaView.setMediaPlayer(mediaPlayer);

                        mediaPlayer.setOnReady(() -> {
                            Duration duration = media.getDuration();
                            SwingUtilities.invokeLater(() -> {
                                UIComponents ui = app.getUIComponents();
                                ui.setNowPlaying(file.getName());
                                ui.setStatus("Ready to play");
                                ui.getPlayPauseButton().setEnabled(true);
                                ui.getStopButton().setEnabled(true);
                                updateTimeLabel(Duration.ZERO, duration);

                                if (isVideo) {
                                    mediaView.setFitWidth(640);
                                    mediaView.setFitHeight(360);
                                    visualization.getRoot().setVisible(false);
                                } else {
                                    mediaView.setFitWidth(0);
                                    mediaView.setFitHeight(0);
                                    visualization.getRoot().setVisible(true);
                                    if (visualizationActive) {
                                        enableVisualization();
                                    }
                                }
                            });
                        });
                        mediaPlayer.setVolume(app.getUIComponents().getVolumeSlider().getValue() / 100.0);
                        setupMediaPlayerEvents();

                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> {
                            app.getUIComponents().setStatus("Load failed");
                            showErrorDialog("Error initializing player: " + e.getMessage());
                        });
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    app.getUIComponents().setStatus("Load error");
                    showErrorDialog("Could not load media: " + e.getMessage());
                });
            }
        }).start();
    }


    private void setupMediaPlayerEvents() {
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!isDragging && media != null) {
                Duration duration = media.getDuration();
                if (duration != null && !duration.isUnknown()) {
                    double progress = newTime.toMillis() / duration.toMillis();
                    app.getUIComponents().getProgressSlider().setValue((int) (progress * 100));
                    updateTimeLabel(newTime, duration);
                }
            }
        });
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

        if (mediaPlayer != null) {
            mediaPlayer.setMute(isMuted);
            SwingUtilities.invokeLater(() -> {
                app.getUIComponents().getMuteButton().setText(isMuted ? "Unmute" : "Mute");
            });
        }

        if (visualizationActive && !isVideo) {
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
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        app.getUIComponents().getProgressSlider().setValue(0);
        updateTimeLabel(Duration.ZERO, Duration.ZERO);
    }

    public void toggleMute() {
        if (mediaPlayer != null) {
            isMuted = !isMuted;
            mediaPlayer.setMute(isMuted);
            app.getUIComponents().getMuteButton().setText(isMuted ? "Unmute" : "Mute");
            app.getUIComponents().setStatus(isMuted ? "Muted" : "Unmuted");
        }
    }

    public void setMute(boolean mute) {
        if (mediaPlayer != null) {
            isMuted = mute;
            mediaPlayer.setMute(isMuted);
            app.getUIComponents().getMuteButton().setText(isMuted ? "Unmute" : "Mute");
            app.getUIComponents().setStatus(isMuted ? "Muted" : "Unmuted");
        }
    }

    public void seekMedia() {
        if (mediaPlayer != null && media != null) {
            double seekMillis = app.getUIComponents().getProgressSlider().getValue() / 100.0 * media.getDuration().toMillis();
            mediaPlayer.seek(new Duration(seekMillis));
        }
    }

    public void updateTimeLabel(Duration current, Duration total) {
        String currentText = formatTime(current);
        String totalText = formatTime(total);
        app.getUIComponents().getTimeLabel().setText(currentText + " / " + totalText);
    }


    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "00:00";
        }
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void toggleVisualization() {
        if (!isVideo) {
            visualizationActive = !visualizationActive;
            if (mediaPlayer != null) {
                if (visualizationActive) {
                    enableVisualization();
                } else {
                    mediaPlayer.setAudioSpectrumListener(null);
                    visualization.getRoot().setVisible(false);
                }
            }
        }
    }

    private void enableVisualization() {
        if (mediaPlayer != null && !isVideo) {
            mediaPlayer.setAudioSpectrumListener((timestamp, duration, magnitudes, phases) ->
                    Platform.runLater(() -> visualization.update(magnitudes))
            );
            mediaPlayer.setAudioSpectrumNumBands(64);
            mediaPlayer.setAudioSpectrumInterval(0.1);
            mediaPlayer.setAudioSpectrumThreshold(-60);
            visualization.getRoot().setVisible(true);
        }
    }

    public void cleanup() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        media = null;
        isVideo = false;
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

    public void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();

        // Set the default directory to the Desktop
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        fileChooser.setCurrentDirectory(new File(desktopPath));

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            loadMediaFile(selectedFile.getAbsolutePath());
        }
    }

    public void showVideo(JPanel videoPanel) {
        if (mediaPlayer != null && isVideo) {
            Scene scene = mediaView.getScene();
            if (scene != null) {
                JFXPanel jfxPanel = (JFXPanel) scene.getProperties().get("jfxPanel");
                if (jfxPanel != null) {
                    videoPanel.removeAll();
                    videoPanel.add(jfxPanel, BorderLayout.CENTER);
                    mediaView.setVisible(true);
                    videoPanel.revalidate();
                    videoPanel.repaint();
                }
            }
        }
    }

    public void hideVideo() {
        if (mediaPlayer != null) {
            Scene scene = mediaView.getScene();
            if (scene != null) {
                JFXPanel jfxPanel = (JFXPanel) scene.getProperties().get("jfxPanel");
                if (jfxPanel != null) {
                    app.getUIComponents().getMainPanel().removeAll();
                    app.getUIComponents().getMainPanel().add(jfxPanel, BorderLayout.CENTER);
                    app.getUIComponents().getMainPanel().revalidate();
                    app.getUIComponents().getMainPanel().repaint();
                }
            }
        }
    }

    public boolean isMuted() {
        return isMuted;
    }

    private boolean isVideoFile(String filePath) {
        String lowercasePath = filePath.toLowerCase();
        return lowercasePath.endsWith(".mp4") || lowercasePath.endsWith(".avi") ||
                lowercasePath.endsWith(".mkv") || lowercasePath.endsWith(".mov");
    }

    private boolean isSupported(File file) {
        String name = file.getName().toLowerCase();
        for (String extension : SUPPORTED_EXTENSIONS) {
            if (name.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private void showError(String message) {
        app.getUIComponents().getStatusLabel().setText("Error");
        JOptionPane.showMessageDialog(
                app,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE,
                null);
    }

    private void showErrorDialog(String message) {
        SwingUtilities.invokeLater(() -> {
            app.getUIComponents().getStatusLabel().setText("Error");
            JOptionPane.showMessageDialog(
                    app,
                    message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }
}