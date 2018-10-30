/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iptv.player;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import uk.co.caprica.vlcj.component.EmbeddedMediaListPlayerComponent;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/**
 *
 * @author Nataniel
 */
public class Player extends JDialog {

    private final String URL;
    private final AtomicBoolean PLAYER;
    private final EmbeddedMediaPlayer play;
    private int volume = 0;
    private JLabel lbVol;
    private final String VOLUME = "Volume: ";

    public Player(String url, AtomicBoolean player) {
        URL = url;
        PLAYER = player;
        EmbeddedMediaPlayerComponent comp = new EmbeddedMediaListPlayerComponent();
        JPanel data = new JPanel();
        lbVol = new JLabel();
        play = comp.getMediaPlayer();
        volume = play.getVolume();

        setLayout(new BorderLayout());
        data.setLayout(new GridLayout());
        lbVol.setText(VOLUME + volume);
        setSize(500, 470);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        play.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void volumeChanged(MediaPlayer mediaPlayer, float volum) {
                super.volumeChanged(mediaPlayer, volum);
                lbVol.setText(VOLUME + volume);
            }
        });

        data.add(lbVol);
        add(comp, BorderLayout.CENTER);
        add(data, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent we) {
                super.windowOpened(we);
                play.playMedia(URL);
            }

            @Override
            public void windowClosed(WindowEvent we) {
                super.windowClosed(we);
                comp.release();
                PLAYER.set(false);
            }
        });
        comp.getVideoSurface().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent ke) {
                switch (ke.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        if (volume < 200) {
                            play.setVolume(++volume);
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (volume > 0) {
                            play.setVolume(--volume);
                        }
                        break;
                    case KeyEvent.VK_ESCAPE:
                        
                        break;
                }
            }
        });
    }
}
