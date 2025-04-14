package org.example;

import org.example.logic.MediaController;
import org.example.logic.PlaylistManager;
import org.example.user_interface.UIComponents;

import javax.swing.*;
import java.awt.*;

public class AppMusicPlayer extends JFrame {
    private final UIComponents uiComponents;
    private final MediaController mediaController;
    private final PlaylistManager playlistManager;
    private final JPanel videoPanel;
    private boolean isVideoMode = false;
    private final int AUDIO_HEIGHT = 600;

    public AppMusicPlayer() {
        setTitle("JavaFX Music & Video Player 🎵🎬");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, AUDIO_HEIGHT);
        setLocationRelativeTo(null);

        applyDarkTheme();

        playlistManager = new PlaylistManager(this);
        uiComponents = new UIComponents();
        mediaController = new MediaController(this);

        uiComponents.setStatus("Initializing...");

        videoPanel = new JPanel(new BorderLayout());
        videoPanel.setBackground(Color.BLACK);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(uiComponents.getMainPanel(), BorderLayout.CENTER);
        mainPanel.add(videoPanel, BorderLayout.SOUTH);



        setContentPane(mainPanel);
        uiComponents.setStatus("Ready");
        setVisible(true);

        uiComponents.initListeners(this);
        initVideoToggle();
    }

    private void initVideoToggle() {
        JToggleButton videoToggle = new JToggleButton("Toggle Video Mode");
        videoToggle.addActionListener(e -> toggleVideoMode());
        uiComponents.getControlPanel(this).add(videoToggle);
    }

    private void toggleVideoMode() {
        final int VIDEO_HEIGHT = 800;
        isVideoMode = !isVideoMode;
        if (isVideoMode) {
            setSize(800, VIDEO_HEIGHT);
            videoPanel.setPreferredSize(new Dimension(800, 400));
            SwingUtilities.invokeLater(() -> {
                mediaController.showVideo(videoPanel);
                revalidate();
                repaint();
            });
        } else {
            setSize(800, AUDIO_HEIGHT);
            videoPanel.setPreferredSize(new Dimension(0, 0));
            SwingUtilities.invokeLater(() -> {
                mediaController.hideVideo();
                revalidate();
                repaint();
            });
        }
    }

    private void applyDarkTheme() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("control", new Color(30, 30, 30));
            UIManager.put("info", new Color(30, 30, 30));
            UIManager.put("nimbusBase", new Color(50, 50, 50));
            UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
            UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
            UIManager.put("nimbusFocus", new Color(115, 164, 209));
            UIManager.put("nimbusGreen", new Color(176, 179, 50));
            UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
            UIManager.put("nimbusLightBackground", new Color(43, 43, 43));
            UIManager.put("nimbusOrange", new Color(191, 98, 4));
            UIManager.put("nimbusRed", new Color(169, 46, 34));
            UIManager.put("nimbusSelectedText", Color.WHITE);
            UIManager.put("nimbusSelectionBackground", new Color(60, 63, 65));
            UIManager.put("text", Color.WHITE);
        } catch (Exception e) {
            System.err.println("Failed to apply dark theme");
        }
    }

    public UIComponents getUIComponents() {
        return uiComponents;
    }

    public MediaController getMediaController() {
        return mediaController;
    }

    public PlaylistManager getPlaylistManager() {
        return playlistManager;
    }

    public boolean isVideoMode() {
        return isVideoMode;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AppMusicPlayer::new);
    }
}