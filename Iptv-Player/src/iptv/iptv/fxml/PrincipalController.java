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
package iptv.fxml;

import iptv.IPTVPlayer;
import iptv.Propriedades;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Nataniel
 */
public class PrincipalController implements Initializable {
    public static AtomicBoolean player = null;
    //FXML componentes
    @FXML
    private TabPane TabManager;
    private iptv.fxml.TabController local;

    private String getM3ULink() {
        TextInputDialog diag = new TextInputDialog();
        diag.setHeaderText("Informe uma URL");
        String m3u = diag.showAndWait().orElse(null);
        return m3u;
    }

    private Tab createLocalTab(String m3uLocal) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("tab.fxml"));
        local = new iptv.fxml.TabController(m3uLocal, true);
        loader.setController(local);
        Tab tb = loader.load();
        tb.setClosable(false);
        tb.setText("Local");
        return tb;
    }

    private Tab createTab(String m3uLocal) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("tab.fxml"));
        local = new TabController(m3uLocal, true);
        loader.setController(local);
        Tab tb = loader.load();
        tb.setClosable(false);
        tb.setText("Local");
        return tb;
    }

    /**
     * acao de troocar de lista
     *
     * @param evt
     */
    private void handleTrocaRapida(ActionEvent evt) {
        String m3u = getM3ULink();
        if (m3u != null) {
            List<String> lista = Arrays.asList(Propriedades.instancia.getM3u());
            lista.add(m3u);
            Propriedades.instancia.setM3u(lista.toArray(new String[0]));
            try {
                TabManager.getTabs().add(createTab(m3u));
            } catch (IOException e) {
                IPTVPlayer.error(e, getClass());
            }
        }
    }

    /**
     * Metodo para criar o contextmenu do canal.
     *
     * @return
     */
    private ContextMenu contextMenuForTabManager() {
        ContextMenu contextMenu = new ContextMenu();
        //MenuItem
        MenuItem novaLista = new MenuItem("Abrir nova lista");
        //adicionando na ordem
        contextMenu.getItems().add(novaLista);
        //Acoes
        novaLista.setOnAction(this::handleTrocaRapida);
        //Exibir ou ocultar alguns itens.

        return contextMenu;
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        player = new AtomicBoolean(false);
        TabManager.setContextMenu(contextMenuForTabManager());


        String[] m3u = Propriedades.instancia.getM3u();

        try {
            if (m3u == null || m3u.length == 0) {
                String m3 = getM3ULink();
                if (m3 == null) System.exit(0);
                m3u = new String[]{m3};
                Propriedades.instancia.setM3u(m3u);
            }
            for (String m : m3u) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("tab.fxml"));
                TabController controller = new TabController(m);
                loader.setController(controller);
                Tab tb = loader.load();
                tb.setText("Lista " + TabManager.getTabs().size() + 1);
                TabManager.getTabs().add(tb);
            }
            String m3uLocal = Propriedades.instancia.getM3uLocal();
            if (m3uLocal != null && !m3uLocal.isEmpty()) {
                TabManager.getTabs().add(createLocalTab(m3uLocal));
            }
            Propriedades.instancia.addConfigurationListener((event -> {
                if (event.getType() == 3 && !event.isBeforeUpdate() && event.getPropertyName().equals("m3uLocal")) {
                    String m3uL = Propriedades.instancia.getM3uLocal();
                    if (m3uL != null && !m3uL.isEmpty()) {
                        try {
                            TabManager.getTabs().add(createLocalTab(m3uL));
                        } catch (IOException e) {
                            IPTVPlayer.error(e, getClass());
                        }
                    }
                }
            }));
        } catch (Exception err) {
            IPTVPlayer.error(err, getClass());
        }


    }

}
