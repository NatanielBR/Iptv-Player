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
package iptv;

import java.io.*;
import java.util.Properties;

/**
 * @author Nataniel
 */
public class Propriedades {

    public static Propriedades instancia;
    private final String arquivo = "data.properties";

    Propriedades() {
        instancia = this;
    }

    public String getM3u() {
        return carregar().getProperty("m3u", null);
    }

    public String getM3uLocal() {
        return carregar().getProperty("m3uLocal", null);
    }

    public void setM3u(String nv) {
        Properties prop = carregar();
        prop.put("m3u", nv);
        try {
            prop.store(saida(), "");
        } catch (IOException ex) {
            iptv.IPTVPlayer.error(ex, getClass());
        }
    }

    public void setM3uLocal(String nv) {
        Properties prop = carregar();
        prop.put("m3uLocal", nv);
        try {
            prop.store(saida(), "");
        } catch (IOException ex) {
            iptv.IPTVPlayer.error(ex, getClass());
        }
    }

    private Properties carregar() {
        Properties prop = new Properties();
        File f = new File(arquivo);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            prop.load(new FileInputStream(f));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return prop;
    }

    private OutputStream saida() {
        OutputStream out = null;
        try {
            out = new FileOutputStream(arquivo);
        } catch (FileNotFoundException ex) {
            IPTVPlayer.error(ex, getClass());
        }
        return out;
    }
}
