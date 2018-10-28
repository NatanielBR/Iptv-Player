/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package m3uteste.pkg2;

import iptv.player.IPTVPlayer;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javafx.application.Platform;

/**
 *
 * @author Nataniel
 */
public class M3uParser {
    private static String nome = "", logo = "", grupo = "",m3u = "";
    public static Map<String,List<Info>> getGrupos(List<Info> infos ){
        HashMap<String,List<Info>> byGrupos =new HashMap<>();
        infos.forEach((i) -> {
            if (!byGrupos.containsKey(i.getGrupo())){
                List<Info> lis = new ArrayList<>();
                lis.add(i);
                byGrupos.put(i.getGrupo(), lis);
            }else{
                byGrupos.get(i.getGrupo()).add(i);
            }
        });
        return byGrupos;
    }
    public static void parserVideo(InputStream url) throws Exception {
        Scanner ent = new Scanner(url);
        while (ent.hasNextLine()){
            System.out.println(ent.nextLine());
        }
    }
    public static List<Info> parser(String url) throws Exception {
        InputStream input = null;
        Exception ers = null;
        try{
            for (int i = 0; i < 3; i++) {
                input = url.contains("http")?new URL(url).openStream():new FileInputStream(url);
            }
        }catch(Exception err){
            ers = err;
        }
        if (input == null){
            iptv.player.IPTVPlayer.error(ers);
        }
        List<Info> infos=null;
        try (Scanner ent = new Scanner(input)) {
            infos = new ArrayList<>();
            while (ent.hasNextLine()){
                String line = ent.nextLine();
                boolean bre = line.startsWith("#EXTINF");
                
                //tvg-id="" tvg-name="A Fazenda 10 HD" tvg-logo="https://i.imgur.com/sP4EhwA.png" group-title="FHD e 4K",A Fazenda 10 HD
                if (bre) {
                    nome = line.substring(line.indexOf("tvg-name=\"")+10, line.indexOf("\" tvg-logo"));
                    logo = line.substring(line.indexOf(" tvg-logo=\"")+11, line.indexOf("\" group-title=\""));
                    grupo = line.substring(line.indexOf("\" group-title=\"")+15,line.length()-(nome.length()+2));
                }else if (!line.startsWith("#")){
                    m3u = !line.endsWith("jpg")?line:null;
                    if (m3u!=null){
                        final String nom = nome;
                        final String m3=m3u;
                        final String log=logo;
                        final String grup=grupo;
                        Info info = new Info() {
                            @Override
                            public String getNome() {
                                return nom;
                            }
                            
                            @Override
                            public String getURLLogo() {
                                return log;
                            }
                            
                            @Override
                            public String getGrupo() {
                                return grup;
                            }
                            
                            @Override
                            public String getURL() {
                                return m3;
                            }
                            @Override
                            public String toString(){
                                return getNome();
                            }
                        };
                        infos.add(info);
                    }
                    nome = "";
                    logo = "";
                    grupo = "";
                    m3u = "";
                }
            }
        }catch(Exception err){
            IPTVPlayer.error(err);
            Platform.exit();
        }
        return infos;
    }
}
