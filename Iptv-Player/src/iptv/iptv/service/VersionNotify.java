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
import org.apache.commons.lang3.StringUtils;

import java.net.HttpURLConnection;
import java.net.URL;

public class VersionNotify extends Service<Boolean> {
    public static final double VERSAO = 2.3;
    public static final int BUILD = 27;

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                HttpURLConnection connection = getInputStreamByURL("https://github.com/NatanielBR/Iptv-Player/releases/latest");
                String[] nums = StringUtils.
                        substringAfterLast(connection.getURL().getPath(), "/").
                        split("v");
                updateMessage(String.format("%sv%s", nums[0], nums[1]));
                double versao = Double.valueOf(nums[0]);
                int build = Integer.valueOf(nums[1]);
                if (versao > VERSAO || build > BUILD) {
                    updateValue(true);
                } else {
                    updateValue(false);
                }
                return null;
            }
        };
    }

    private HttpURLConnection getInputStreamByURL(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 Firefox/26.0");
        while (connection.getResponseCode() == 301 || connection.getResponseCode() == 302) {
            connection = (HttpURLConnection) new URL(connection.getHeaderField("Location")).openConnection();
        }

        return connection;
    }
}
