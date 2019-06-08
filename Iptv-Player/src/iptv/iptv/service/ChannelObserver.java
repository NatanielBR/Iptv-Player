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

package iptv.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ChannelObserver extends Service<String> {
    private List<Channel> canais;

    public ChannelObserver(List<Channel> canais) {
        this.canais = canais;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                int online = 0;
                int offline = 0;
                for (int index = 0; index < canais.size(); index++) {
                    Channel canal = canais.get(index);
                    int status = -1;
                    try {
                        URL url = new URL(canal.getChannel().getCanalURL());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0 Firefox/26.0");
                        while (connection.getResponseCode() == 301 || connection.getResponseCode() == 302) {
                            connection = (HttpURLConnection) new URL(connection.getHeaderField("Location")).openConnection();
                            connection.setRequestProperty("User-Agent", "Mozilla/5.0 Firefox/26.0");
                        }
                        status = connection.getResponseCode();
                    } catch (Exception err) {
                    }
                    boolean on = (status == 200);
                    canal.setAlive(on);
                    if (on) online++;
                    else offline++;
                    updateMessage(String.format("%s: %s | %d/%d Online: %d Offline: %d",
                            canal.getChannel().getCanalNome(),
                            on ? "Online" : "Offline",
                            index + 1,
                            canais.size(),
                            online,
                            offline));
                }
                updateMessage(String.format("Finalizado | Online: %d Offline: %d",
                        online,
                        offline));
                return null;
            }
        };
    }

    @Override
    protected void failed() {
        getException().printStackTrace();
        super.failed();
    }
}
