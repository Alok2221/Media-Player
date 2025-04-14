package org.example.user_interface;

import org.example.AppMusicPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

public class UIComponents {
    private final JPanel mainPanel;
    private final JLabel nowPlayingLabel;
    private final JLabel statusLabel;
    private final JButton playPauseButton;
    private final JButton previousButton;
    private final JButton nextButton;
    private final JButton stopButton;
    private final JButton muteButton;
    private final JButton addSongsButton;
    private final JSlider volumeSlider;
    private final JSlider progressSlider;
    private final JLabel timeLabel;
    private final DefaultListModel<String> playlistModel;
    private final JList<String> playlistList;
    private final JButton removeButton;
    private final JButton clearButton;

    public UIComponents() {
        mainPanel = new JPanel(new BorderLayout());
        applyDarkTheme(mainPanel);

        // Playlist Sidebar
        playlistModel = new DefaultListModel<>();
        playlistList = new JList<>(playlistModel);
        playlistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlistList.setBackground(Color.DARK_GRAY);
        playlistList.setForeground(Color.WHITE);
        playlistList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        playlistList.setFixedCellHeight(30);

        // Playlist Scroll Pane
        JScrollPane playlistScroll = new JScrollPane(playlistList);
        playlistScroll.setPreferredSize(new Dimension(200, 0));

        // Playlist Control Buttons
        removeButton = new JButton("Remove");
        clearButton = new JButton("Clear");
        setButtonStyle(removeButton);
        setButtonStyle(clearButton);

        JPanel playlistControls = new JPanel(new GridLayout(0, 1));
        playlistControls.setBackground(Color.DARK_GRAY);
        playlistControls.add(removeButton);
        playlistControls.add(clearButton);

        JPanel leftSidebar = new JPanel(new BorderLayout());
        leftSidebar.setBackground(Color.DARK_GRAY);
        leftSidebar.add(playlistScroll, BorderLayout.CENTER);
        leftSidebar.add(playlistControls, BorderLayout.SOUTH);

        mainPanel.add(leftSidebar, BorderLayout.WEST);


        // === Controls Panel ===
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.setBackground(Color.BLACK);

        previousButton = new JButton("⏮");
        playPauseButton = new JButton("▶");
        stopButton = new JButton("⏹");
        nextButton = new JButton("⏭");
        muteButton = new JButton("Mute");
        addSongsButton = new JButton("➕ Add Songs");

        volumeSlider = new JSlider(0, 100, 80);
        progressSlider = new JSlider(0, 100, 0);
        timeLabel = new JLabel("00:00 / 00:00");

        setButtonStyle(previousButton);
        setButtonStyle(playPauseButton);
        setButtonStyle(stopButton);
        setButtonStyle(nextButton);
        setButtonStyle(muteButton);
        setButtonStyle(addSongsButton);

        controlsPanel.add(previousButton);
        controlsPanel.add(playPauseButton);
        controlsPanel.add(stopButton);
        controlsPanel.add(nextButton);
        controlsPanel.add(muteButton);
        controlsPanel.add(new JLabel("Volume"));
        controlsPanel.add(volumeSlider);
        controlsPanel.add(addSongsButton);
        controlsPanel.add(timeLabel);

        mainPanel.add(controlsPanel, BorderLayout.NORTH);

        // === Bottom Panel: Progress + Status Labels ===
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.DARK_GRAY);

        nowPlayingLabel = new JLabel("Now Playing: ");
        statusLabel = new JLabel("Status: Ready");
        nowPlayingLabel.setForeground(Color.WHITE);
        statusLabel.setForeground(Color.LIGHT_GRAY);

        JPanel statusPanel = new JPanel(new GridLayout(2, 1));
        statusPanel.setBackground(Color.DARK_GRAY);
        statusPanel.add(nowPlayingLabel);
        statusPanel.add(statusLabel);

        bottomPanel.add(progressSlider, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }


    private void setButtonStyle(JButton button) {
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }

    private void applyDarkTheme(JComponent component) {
        component.setBackground(Color.BLACK);
        component.setForeground(Color.WHITE);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JButton getPlayPauseButton() {
        return playPauseButton;
    }

    public JButton getStopButton() {
        return stopButton;
    }

    public JButton getMuteButton() {
        return muteButton;
    }

    public JSlider getVolumeSlider() {
        return volumeSlider;
    }

    public JSlider getProgressSlider() {
        return progressSlider;
    }

    public JLabel getNowPlayingLabel() {
        return nowPlayingLabel;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public JLabel getTimeLabel() {
        return timeLabel;
    }

    public JList<String> getPlaylistList() {
        return playlistList;
    }

    public JButton getAddSongsButton() {
        return addSongsButton;
    }

    public void addToPlaylist(String name) {
        playlistModel.addElement(name);
    }

    public void setSelectedPlaylistIndex(int index) {
        playlistList.setSelectedIndex(index);
    }

    public void clearPlaylist() {
        playlistModel.clear();
    }


    public void initListeners(AppMusicPlayer app) {
        addSongsButton.addActionListener(e -> app.getPlaylistManager().addSongsThroughChooser());

        playlistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = playlistList.getSelectedIndex();
                if (index != -1) {
                    app.getPlaylistManager().setCurrentTrackIndex(index);
                    app.getMediaController().loadMediaFile(app.getPlaylistManager().getCurrentFilePath());
                    app.getMediaController().playMedia();
                }
            }
        });

        playPauseButton.addActionListener(e -> app.getMediaController().togglePlayPause());

        stopButton.addActionListener(e -> app.getMediaController().stopMedia());

        muteButton.addActionListener(e -> app.getMediaController().toggleMute());

        volumeSlider.addChangeListener(e -> {
            if (app.getMediaController().getMediaPlayer() != null) {
                double volume = volumeSlider.getValue() / 100.0;
                app.getMediaController().getMediaPlayer().setVolume(volume);
            }
        });

        progressSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                app.getMediaController().setDragging(true);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                app.getMediaController().setDragging(false);
                app.getMediaController().seekMedia();
            }
        });

        progressSlider.addChangeListener(e -> {
            if (app.getMediaController().isDragging()) {
                app.getMediaController().seekMedia();
            }
        });

        nextButton.addActionListener(e -> {
            app.getPlaylistManager().playNextTrack();
        });

        previousButton.addActionListener(e -> {
            app.getPlaylistManager().playPreviousTrack();
        });

        removeButton.addActionListener(e -> {
            int index = playlistList.getSelectedIndex();
            if (index >= 0) {
                app.getPlaylistManager().removeAt(index);
            }
        });

        clearButton.addActionListener(e -> {
            app.getPlaylistManager().clearPlaylist();
        });
    }

    public void setNowPlaying(String text) {
        nowPlayingLabel.setText("Now Playing: " + text);
    }

    public void setStatus(String text) {
        statusLabel.setText("Status: " + text);
    }

    public JPopupMenu getControlPanel() {
        JPopupMenu popupMenu = new JPopupMenu();

        // Add menu items to the popup menu
        JMenuItem playPauseItem = new JMenuItem("Play/Pause");
        JMenuItem stopItem = new JMenuItem("Stop");
        JMenuItem nextItem = new JMenuItem("Next");
        JMenuItem previousItem = new JMenuItem("Previous");

        popupMenu.add(playPauseItem);
        popupMenu.add(stopItem);
        popupMenu.add(nextItem);
        popupMenu.add(previousItem);

        playPauseItem.addActionListener(e -> {
        });

        stopItem.addActionListener(e -> {
        });

        nextItem.addActionListener(e -> {
        });

        previousItem.addActionListener(e -> {

        });

        return popupMenu;
    }
}
