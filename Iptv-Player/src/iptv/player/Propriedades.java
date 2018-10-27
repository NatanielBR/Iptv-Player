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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nataniel
 */
public class Propriedades {
    public String M3U_URL;
    public static Propriedades instancia;
    public Propriedades() {
        Properties prop = new Properties();
        instancia = this;
        try {
            prop.load(carregar());
            M3U_URL = prop.getProperty("m3u",null);
        } catch (FileNotFoundException ex) {
            try {
                new File("data.properties").createNewFile();
                prop.load(carregar());
                M3U_URL = prop.getProperty("m3u",null);
            } catch (IOException ex1) {
                Logger.getLogger(Propriedades.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (IOException ex) {
            Logger.getLogger(Propriedades.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }
    private InputStream carregar() throws FileNotFoundException{
        return new FileInputStream("data.properties");
    }
    private OutputStream saida() throws FileNotFoundException{
        return new FileOutputStream("data.properties");
    }
    public void salvar(String chave, String valor){
        Properties prop = new Properties();
        try {
            prop.load(carregar());
        } catch (Exception ex) {
            Logger.getLogger(Propriedades.class.getName()).log(Level.SEVERE, null, ex);
        }
        prop.put(chave, valor);
        try {
            prop.store(saida(), "");
        } catch (Exception ex) {
            Logger.getLogger(Propriedades.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
