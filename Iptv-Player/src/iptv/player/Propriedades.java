/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iptv.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author Nataniel
 */
public class Propriedades {

    public static Propriedades instancia;
    private final String arquivo = "data.properties";
    
    public Propriedades() {
        instancia = this;
    }
    public String getM3u() {
        return carregar().getProperty("m3u", null);
    }

    public void setM3u(String nv) {
        Properties prop = carregar();
        prop.put("m3u", nv);
        try {
            prop.store(saida(), "");
        } catch (IOException ex) {
            IPTVPlayer.error(ex,getClass());
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
            IPTVPlayer.error(ex,getClass());
        }
        return out;
    }
}
