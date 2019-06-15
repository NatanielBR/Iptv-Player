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
import iptv.inter.TabControle;
import iptv.service.VersionNotify;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.commons.configuration2.event.ConfigurationEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
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
    public static TabLocalController local;
    private static List<TabControle> controllers = null;
    private String tabRemove;
    private static VersionNotify notify;

    public PrincipalController() {
    }

    public static void closeAllTab() {
        controllers.forEach((a) -> {
            a.closeTab();
        });
        notify.cancel();
    }

    private Comparator<Tab> getCompartor() {
        return (a, b) -> {
            if (a.getText().equals("Local")) {
                return 1;
            } else {
                return a.getText().compareTo(b.getText());
            }
        };
    }
    private void sortTab() {
        List<Tab> lis = new ArrayList<>(TabManager.getTabs());
        int i = 1;
        for (Tab tb : lis) {
            if (tb.getText().equals("Local") || tb.getText().equals(tabRemove)) {
                continue;
            } else {
                tb.setText("Lista " + i++);
            }
        }
        tabRemove = null;
        lis.sort(getCompartor());
        TabManager.getTabs().clear();
        TabManager.getTabs().setAll(lis);
    }
    private String getM3ULink() {
        TextInputDialog diag = new TextInputDialog();
        diag.setHeaderText("Informe uma URL");
        String m3u = diag.showAndWait().orElse(null);
        return m3u;
    }

    private Tab createLocalTab(String m3uLocal) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("tab.fxml"));
        local = new TabLocalController(m3uLocal);
        loader.setController(local);
        Tab tb = loader.load();
        tb.setClosable(false);
        tb.setText("Local");
        controllers.add(local);
        return tb;
    }

    private Tab createTab(String m3uLocal) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("tab.fxml"));
        TabController local = new TabController(m3uLocal);
        loader.setController(local);
        Tab tb = loader.load();
        tb.setUserData(local);
        tb.setOnCloseRequest((a) -> {
            local.closeTab();
            List<String> lista = new ArrayList<>();
            for (String m : Propriedades.instancia.getM3u()) {
                lista.add(m);
            }
            lista.remove(local.getM3U());
            Propriedades.instancia.setM3u(lista.toArray(new String[0]));
            tabRemove = tb.getText();
            sortTab();

        });
        controllers.add(local);
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
            List<String> lista = new ArrayList<>();
            for (String m : Propriedades.instancia.getM3u()) {
                lista.add(m);
            }
            lista.add(m3u);
            Propriedades.instancia.setM3u(lista.toArray(new String[0]));
            try {
                Tab tb = createTab(m3u);
                tb.setText("Lista " + TabManager.getTabs().size());
                TabManager.getTabs().add(tb);
                sortTab();
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
        controllers = new ArrayList<>();

        String[] m3u = Propriedades.instancia.getM3u();

        try {
            if (m3u == null || m3u.length == 0) {
                String m3 = getM3ULink();
                if (m3 == null) System.exit(0);
                m3u = new String[]{m3};
                Propriedades.instancia.setM3u(m3u);
            }
            int i = 1;
            for (String m : m3u) {
                Tab tb = createTab(m);
                tb.setText("Lista " + (i++));
                TabManager.getTabs().add(tb);
            }
            String m3uLocal = Propriedades.instancia.getM3uLocal();
            if (m3uLocal != null && !m3uLocal.isEmpty()) {
                TabManager.getTabs().add(createLocalTab(m3uLocal));
            }
            Propriedades.instancia.addConfigurationListener(ConfigurationEvent.SET_PROPERTY, (event -> {
                if (!event.isBeforeUpdate() && event.getPropertyName().equals("m3uLocal")) {
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
            notify = new VersionNotify();
            notify.valueProperty().addListener((a, b, c) -> {
                if (c != null && c.booleanValue()) {
                    Alert novaVersao = new Alert(Alert.AlertType.INFORMATION);
                    novaVersao.setTitle("Nova versão");
                    novaVersao.setHeaderText("Nova versão disponivel");
                    novaVersao.setContentText(String.format(
                            "Uma nova versão esta disponivel.\nSua versão: %sv%d\nNova versão: %s"
                            , VersionNotify.VERSAO
                            , VersionNotify.BUILD
                            , notify.getMessage()));
                    ButtonType abrir = new ButtonType("Atualizar", ButtonBar.ButtonData.OK_DONE);
                    ButtonType ignore = new ButtonType("Não Atualizar", ButtonBar.ButtonData.CANCEL_CLOSE);
                    novaVersao.getButtonTypes().setAll(abrir, ignore);
                    ButtonType resposta = novaVersao.showAndWait().orElse(ignore);
                    if (resposta.equals(abrir)) {
                        try {
                            java.awt.Desktop.getDesktop().browse(new URL("https://github.com/NatanielBR/Iptv-Player/releases/latest").toURI());
                        } catch (Exception e) {
                            IPTVPlayer.error(e, getClass());
                        }
                    }
                }
            });
            if (Propriedades.instancia.isVersionNotify()) notify.start();
        } catch (Exception err) {
            IPTVPlayer.error(err, getClass());
        }
    }

}
