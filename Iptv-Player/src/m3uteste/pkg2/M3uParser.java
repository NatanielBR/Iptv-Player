/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package m3uteste.pkg2;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 *
 * @author Nataniel
 */
public class M3uParser {

    public static Map<String, List<Info>> getGrupos(List<Info> infos) {
        HashMap<String, List<Info>> byGrupos = new HashMap<>();
        infos.forEach((i) -> {
            if (!byGrupos.containsKey(i.getGrupo())) {
                List<Info> lis = new ArrayList<>();
                lis.add(i);
                byGrupos.put(i.getGrupo(), lis);
            } else {
                byGrupos.get(i.getGrupo()).add(i);
            }
        });
        return byGrupos;
    }

    public static void parserVideo(InputStream url) throws Exception {
        Scanner ent = new Scanner(url);
        while (ent.hasNextLine()) {
            System.out.println(ent.nextLine());
        }
    }

    private static List<Info> parser(LineIterator iterador) {
        String[] dados = new String[4];
        String line;
        List<Info> infos = new ArrayList<>();
        while (iterador.hasNext()) {
            line = iterador.nextLine().replace("#EXTM3U", "");
            if (line.isEmpty()) {
                continue;
            }
            boolean bre = line.startsWith("#EXTINF");
            if (bre) {
                dados[0] = line.substring(line.indexOf("tvg-name=\"") + 10, line.indexOf("\" tvg-logo"));
                dados[1] = line.substring(line.indexOf(" tvg-logo=\"") + 11, line.indexOf("\" group-title=\""));
                dados[2] = line.substring(line.indexOf("\" group-title=\"") + 15, line.length() - (dados[0].length() + 2));
            } else if (!line.startsWith("#")) {
                dados[3] = !line.endsWith("jpg") ? line : null;
                if (dados[3] != null) {
                    infos.add(criarInfo(dados[0], dados[2], dados[1], dados[3]));
                }
            }
        }
        return infos;
    }

    public static List<Info> parser(URL ur) {
        File temp = new File("cache.m3u");
        Exception ers = null;
        List<Info> infos = null;
        for (int i = 0; i < 5; i++) {
            try {
                FileUtils.copyURLToFile(ur, temp);
                infos = parser(FileUtils.lineIterator(temp));
                ers = null;
                break;
            } catch (Exception e) {
                ers = e;
            }

        }

        if (ers != null) {
            ers.printStackTrace();
        }
        return infos;
    }

    public static List<Info> parser(File f) {
        //nome,logo,grupo,url
        LineIterator iterador = null;
        List<Info> infos = null;
        Exception ers = null;
        try {
            for (int i = 0; i < 3; i++) {
                infos = parser(FileUtils.lineIterator(f));
                break;
            }
        } catch (Exception err) {
            ers = err;
        }
        if (ers != null) {
            ers.printStackTrace();
        }

        return infos;
    }

    private static Info criarInfo(String nome, String grupo, String logo, String url) {
        return new Info() {
            @Override
            public String getNome() {
                return nome;
            }

            @Override
            public String getURLLogo() {
                return logo;
            }

            @Override
            public String getGrupo() {
                return grupo;
            }

            @Override
            public String getURL() {
                return url;
            }

            @Override
            public String toString() {
                return getNome();
            }
        };
    }
}
