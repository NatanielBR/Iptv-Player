/*
 * Copyright (C) 2019 Nataniel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see [http://www.gnu.org/licenses/].
 */

package iptv.player;

import uk.co.caprica.vlcj.component.EmbeddedMediaListPlayerComponent;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Nataniel
 */
public class Player extends JDialog {

    private final String URL;
    private final AtomicBoolean PLAYER;
    private final EmbeddedMediaPlayer play;
    private final String VOLUME = "Volume: ";
    private int volume;
    private JLabel lbVol;

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
