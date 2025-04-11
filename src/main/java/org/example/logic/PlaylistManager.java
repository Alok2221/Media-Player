package org.example.logic;

import org.example.AppMusicPlayer;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaylistManager {
    private final AppMusicPlayer app;
    private final List<File> mediaFiles = new ArrayList<>();
    private int currentIndex = -1;

    public PlaylistManager(AppMusicPlayer app) {
        this.app = app;
    }

    public void setCurrentTrackIndex(int index) {
        if (index >= 0 && index < mediaFiles.size()) {
            currentIndex = index;
        }
    }

    public void addSongsThroughChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Audio Files", "mp3", "wav", "m4a"));

        int result = fileChooser.showOpenDialog(app);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            for (File file : selectedFiles) {
                mediaFiles.add(file);
                app.getUIComponents().addToPlaylist(file.getName());
            }

            if (!mediaFiles.isEmpty() && currentIndex == -1) {
                currentIndex = 0;
                app.getUIComponents().setSelectedPlaylistIndex(0);
                app.getMediaController().loadMediaFile(getCurrentFilePath());
            }
        }
    }

    public void addFiles(File[] files) {
        if (files != null) {
            for (File file : files) {
                mediaFiles.add(file);
                app.getUIComponents().addToPlaylist(file.getName());
            }

            if (currentIndex == -1 && !mediaFiles.isEmpty()) {
                currentIndex = 0;
                app.getMediaController().loadMediaFile(mediaFiles.get(currentIndex).getAbsolutePath());
            }
        }
    }

    public void playSelected(int index) {
        if (index >= 0 && index < mediaFiles.size()) {
            currentIndex = index;
            app.getMediaController().loadMediaFile(mediaFiles.get(index).getAbsolutePath());
            app.getMediaController().playMedia();
        }
    }

    public void playNextTrack() {
        if (currentIndex < mediaFiles.size() - 1) {
            currentIndex++;
            app.getMediaController().loadMediaFile(mediaFiles.get(currentIndex).getAbsolutePath());
            app.getMediaController().playMedia();
            app.getUIComponents().setSelectedPlaylistIndex(currentIndex);
        }
    }

    public void playPreviousTrack() {
        if (currentIndex > 0) {
            currentIndex--;
            app.getMediaController().loadMediaFile(mediaFiles.get(currentIndex).getAbsolutePath());
            app.getMediaController().playMedia();
            app.getUIComponents().setSelectedPlaylistIndex(currentIndex);
        }
    }

    public String getCurrentFilePath() {
        if (currentIndex >= 0 && currentIndex < mediaFiles.size()) {
            return mediaFiles.get(currentIndex).getAbsolutePath();
        }
        return null;
    }

    public List<File> getMediaFiles() {
        return mediaFiles;
    }

    public void removeAt(int index) {
        if (index >= 0 && index < mediaFiles.size()) {
            mediaFiles.remove(index);
            app.getUIComponents().clearPlaylist();
            for (File song : mediaFiles) {
                app.getUIComponents().addToPlaylist(song.getName());
            }
            if (mediaFiles.isEmpty()) {
                currentIndex = -1;
                app.getUIComponents().setNowPlaying("Nothing");
                app.getMediaController().stopMedia();
            } else if (index <= currentIndex) {
                currentIndex = Math.max(0, currentIndex - 1);
                app.getUIComponents().setSelectedPlaylistIndex(currentIndex);
                app.getMediaController().loadMediaFile(getCurrentFilePath());
            }
        }
    }

    public void clearPlaylist() {
        mediaFiles.clear();
        currentIndex = -1;
        app.getUIComponents().clearPlaylist();
        app.getUIComponents().setNowPlaying("Nothing");
        app.getMediaController().stopMedia();
    }
}
