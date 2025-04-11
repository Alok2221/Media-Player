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

    public AppMusicPlayer() {
        setTitle("JavaFX Music Player 🎵");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        applyDarkTheme();

        playlistManager = new PlaylistManager(this);
        uiComponents = new UIComponents();
        mediaController = new MediaController(this);

        setContentPane(uiComponents.getMainPanel());
        setVisible(true);

        uiComponents.initListeners(this);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AppMusicPlayer::new);
    }
}
