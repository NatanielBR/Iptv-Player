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


import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventType;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * @author Nataniel
 */
public class Propriedades {

    public static Propriedades instancia;

    private final String ARQUIVO = "data.properties";
    private Logger logger;
    private FileBasedConfigurationBuilder<PropertiesConfiguration> builder;
    private PropertiesConfiguration property;

    Propriedades() {
        instancia = this;
        logger = Logger.getLogger(getClass());
        Configurations config = new Configurations();
        try {
            File f = new File(ARQUIVO);
            if (!f.exists()) {
                f.createNewFile();
            }
            builder = config.propertiesBuilder(f);
            property = builder.getConfiguration();
            builder.setAutoSave(true);
        } catch (Exception e) {
            IPTVPlayer.error(e, getClass());
            logger.error("Saindo...");
            System.exit(4);
        }
    }

    public String[] getM3u() {
        return property.getStringArray("m3u");
    }

    public void setM3u(String[] m3u) {
        property.setProperty("m3u", m3u);
        logger.debug("Inserido " + m3u.length + " URLs na chave m3u.");
    }

    public void addConfigurationListener(EventType tipo, EventListener<ConfigurationEvent> listener) {
        builder.addEventListener(tipo, listener);
    }

    public boolean isDebug() {
        String deb = property.getString("debug", "");
        boolean ret;
        if (deb.isEmpty()) {
            ret = false;
            property.setProperty("debug", false);
        } else {
            ret = Boolean.valueOf(deb);
        }
        logger.info("Debug esta " + (ret ? "Ativado" : "Desativado"));
        return ret;
    }

    public boolean isVersionNotify() {
        String not = property.getString("versionNotify", "");
        boolean ret;
        if (not.isEmpty()) {
            ret = false;
            property.setProperty("versionNotify", true);
        } else {
            ret = Boolean.valueOf(not);
        }
        return ret;
    }
    public String getM3uLocal() {
        return property.getString("m3uLocal");
    }

    public void setM3uLocal(String m3uLocal) {
        property.setProperty("m3uLocal", m3uLocal);
    }

    public int getLocalTime() {
        int a = property.getInt("localTime", -1);
        if (a == -1) {
            a = 5000;
            property.setProperty("localTime", a);
        }
        return a;

    }

    public int getLinkTime() {
        int a = property.getInt("linkTime", -1);
        if (a == -1) {
            a = 10000;
            property.setProperty("linkTime", a);
        }
        return a;

    }
}
