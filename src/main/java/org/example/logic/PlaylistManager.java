package org.example.logic;

import org.example.AppMusicPlayer;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaylistManager {
    private final AppMusicPlayer app;
    private final List<String> mediaFiles;
    private int currentTrackIndex = -1;

    public PlaylistManager(AppMusicPlayer app) {
        this.app = app;
        this.mediaFiles = new ArrayList<>();
    }

    public void addSongsThroughChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Media Files", "mp3", "wav", "aac", "m4a", "mp4", "avi", "mkv", "mov"));

        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        fileChooser.setCurrentDirectory(new File(desktopPath));

        if (fileChooser.showOpenDialog(app) == JFileChooser.APPROVE_OPTION) {
            DefaultListModel<String> model = (DefaultListModel<String>) app.getUIComponents()
                    .getPlaylistList().getModel();

            boolean addedAny = false;
            for (File file : fileChooser.getSelectedFiles()) {
                if (isSupported(file)) {
                    String filePath = file.getAbsolutePath();
                    if (!mediaFiles.contains(filePath)) {
                        mediaFiles.add(filePath);
                        model.addElement(file.getName());
                        addedAny = true;
                    }
                }
            }

            app.getUIComponents().refreshPlaylistDisplay(mediaFiles);

            if (addedAny) {
                SwingUtilities.invokeLater(() -> {
                    app.getUIComponents().getPlaylistList().setModel(model);
                    app.getUIComponents().setStatus("Added " + fileChooser.getSelectedFiles().length + " files");
                });

                if (currentTrackIndex == -1 && !mediaFiles.isEmpty()) {
                    currentTrackIndex = 0;
                }
            }
        }
    }

    public void playNextTrack() {
        if (mediaFiles.isEmpty()) {
            app.getUIComponents().setStatus("Playlist empty");
            return;
        }

        if (currentTrackIndex == -1) {
            currentTrackIndex = 0;
            String firstFile = mediaFiles.get(currentTrackIndex);
            if (new File(firstFile).exists()) {
                app.getUIComponents().setSelectedPlaylistIndex(currentTrackIndex);
                app.getMediaController().loadMediaFile(firstFile);
            } else {
                removeAt(currentTrackIndex);
                playNextTrack();
            }
            return;
        }

        int newIndex = (currentTrackIndex + 1) % mediaFiles.size();
        if (newIndex == currentTrackIndex) {
            String currentFile = mediaFiles.get(currentTrackIndex);
            if (new File(currentFile).exists()) {
                app.getUIComponents().setSelectedPlaylistIndex(currentTrackIndex);
                app.getMediaController().loadMediaFile(currentFile);
            } else {
                removeAt(currentTrackIndex);
                playNextTrack();
            }
            return;
        }

        String nextFile = mediaFiles.get(newIndex);
        if (new File(nextFile).exists()) {
            currentTrackIndex = newIndex;
            app.getUIComponents().setSelectedPlaylistIndex(currentTrackIndex);
            app.getMediaController().loadMediaFile(nextFile);
        } else {
            removeAt(newIndex);
            playNextTrack();
        }
    }

    public void playPreviousTrack() {
        if (mediaFiles.isEmpty()) {
            app.getUIComponents().setStatus("Playlist empty");
            return;
        }

        int newIndex = (currentTrackIndex - 1 + mediaFiles.size()) % mediaFiles.size();
        String prevFile = mediaFiles.get(newIndex);

        if (new File(prevFile).exists()) {
            currentTrackIndex = newIndex;
            app.getUIComponents().setSelectedPlaylistIndex(currentTrackIndex);
            app.getMediaController().loadMediaFile(prevFile);
        } else {
            removeAt(newIndex);
            playPreviousTrack();
        }
    }

    public void playCurrentTrack() {
        if (currentTrackIndex >= 0 && currentTrackIndex < mediaFiles.size()) {
            app.getUIComponents().setSelectedPlaylistIndex(currentTrackIndex);
            app.getMediaController().loadMediaFile(mediaFiles.get(currentTrackIndex));
            app.getMediaController().playMedia();
        }
    }

    public void removeAt(int index) {
        if (index >= 0 && index < mediaFiles.size()) {
            String removedFile = mediaFiles.remove(index);
            app.getUIComponents().setStatus("Removed: " + new File(removedFile).getName());
            app.getUIComponents().refreshPlaylistDisplay(mediaFiles);

            DefaultListModel<String> model = new DefaultListModel<>();
            for (String file : mediaFiles) {
                model.addElement(new File(file).getName());
            }
            app.getUIComponents().getPlaylistList().setModel(model);
            if (currentTrackIndex == index) {
                app.getMediaController().stopMedia();
                currentTrackIndex = -1;
            } else if (currentTrackIndex > index) {
                currentTrackIndex--;
            }
        }
    }

    public void clearPlaylist() {
        mediaFiles.clear();
        app.getUIComponents().clearPlaylist();
        app.getMediaController().stopMedia();
        app.getUIComponents().refreshPlaylistDisplay(mediaFiles);
        currentTrackIndex = -1;
        app.getUIComponents().setStatus("Playlist cleared");
    }

    public List<String> getMediaFiles() {
        return mediaFiles;
    }

    public String getCurrentFilePath() {
        return (currentTrackIndex >= 0 && currentTrackIndex < mediaFiles.size())
                ? mediaFiles.get(currentTrackIndex) : null;
    }

    public void setCurrentTrackIndex(int index) {
        if (index >= 0 && index < mediaFiles.size()) {
            currentTrackIndex = index;
        }
    }

    private boolean isSupported(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".aac") ||
                name.endsWith(".m4a") || name.endsWith(".mp4") || name.endsWith(".avi") ||
                name.endsWith(".mkv") || name.endsWith(".mov");
    }

    public void refreshPlaylist() {
        app.getUIComponents().refreshPlaylistDisplay(mediaFiles);
    }

    public boolean hasNextTrack() {
        if (mediaFiles.isEmpty()) return false;
        if (currentTrackIndex == -1) return true;
        return (currentTrackIndex + 1) < mediaFiles.size();
    }
}