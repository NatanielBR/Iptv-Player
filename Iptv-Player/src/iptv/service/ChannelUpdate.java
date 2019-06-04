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

import com.nataniel.ExtInfoList;
import com.nataniel.Parser;
import iptv.Propriedades;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChannelUpdate extends Service<ExtInfoList> {
    @Override
    protected Task<ExtInfoList> createTask() {
        return new Task<ExtInfoList>() {
            @Override
            protected ExtInfoList call() throws Exception {
                String m3u = Propriedades.instancia.getM3u();
                return Parser.parserExtM3u8(getInputStreamByM3U(m3u));
            }
        };
    }

    private InputStream getInputStreamByM3U(String m3u) throws Exception {
        InputStream inputStream;
        if (m3u.startsWith("http")) {
            HttpURLConnection connection = (HttpURLConnection) new URL(m3u).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 Firefox/26.0");
            while (connection.getResponseCode() == 301 || connection.getResponseCode() == 302) {
                connection = (HttpURLConnection) new URL(connection.getHeaderField("Location")).openConnection();
            }
            inputStream = connection.getInputStream();
        } else {
            inputStream = new FileInputStream(m3u);
        }
        return inputStream;
    }
}
