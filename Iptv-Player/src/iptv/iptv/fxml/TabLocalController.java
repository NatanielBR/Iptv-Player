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

import com.nataniel.Parser;
import com.nataniel.builder.ExtInfoBuilder;
import com.nataniel.inter.ExtInfo;
import com.nataniel.list.ExtInfoList;
import iptv.IPTVPlayer;
import iptv.Propriedades;
import iptv.inter.TabControle;
import iptv.service.Channel;
import iptv.service.ChannelUpdate;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TabLocalController extends TabControle implements Initializable {

    //Objetos da classe
    private final String M3U;

    public TabLocalController(String m3U) {
        super(Propriedades.instancia.getLocalTime());
        M3U = m3U;
    }

    public void update() {
        update.restart();
    }

    private void saveList(List<ExtInfo> lis) {
        try {
            String m3u = Propriedades.instancia.getM3uLocal();
            File f;
            if (m3u == null) {
                f = new File("local.m3u");
                Propriedades.instancia.setM3uLocal("local.m3u");
                f.createNewFile();
            } else {
                f = new File(m3u);
            }
            Parser.ParserExtInfoListToFile(new ExtInfoList(lis), f);
            update();
        } catch (Exception e) {
            IPTVPlayer.error(e, getClass());
        }
    }

    private Alert getAlertEditor(ExtInfoEditor editor) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(editor);
        Parent par = loader.load(editor.getClass().getResourceAsStream("ExtInfoEditor.fxml"));
        Alert alert = new Alert(Alert.AlertType.NONE);
        DialogPane pane = new DialogPane();
        pane.setContent(par);
        alert.setDialogPane(pane);
        alert.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        return alert;
    }

    /**
     * acao para editar um canal
     *
     * @param evt
     */
    private void handleEditarCanal(ActionEvent evt) {
        ExtInfo newinfo;
        ExtInfoEditor editor;
        if (canalSelecionado.size() > 1) {
            ObservableList<Channel> channels = Canais.getSelectionModel().getSelectedItems();
            editor = new ExtInfoEditor();
            try {
                Alert alert = getAlertEditor(editor);
                if (alert.showAndWait().get() == ButtonType.OK) {
                    List<ExtInfo> info = new ArrayList<>();
                    String grupo = editor.getExtInfo().getGrupo();
                    channels.forEach((a) -> {
                        ExtInfo temp = a.getChannel();
                        ExtInfoBuilder bu = new ExtInfoBuilder();
                        bu.setCanalNome(temp.getCanalNome());
                        bu.setCanalURL(temp.getCanalURL());
                        bu.setLogoURL(temp.getLogoURL());
                        bu.setId(temp.getId());
                        bu.setGrupo(grupo);
                        info.add(bu.builder());
                    });
                    saveList(info);
                }
            } catch (IOException e) {
                IPTVPlayer.error(e, getClass());
            }
        } else {
            ExtInfo selec = canalSelecionado.get(0).getChannel();
            try {
                editor = new ExtInfoEditor(selec);
                Alert alert = getAlertEditor(editor);
                if (alert.showAndWait().get() == ButtonType.OK) {
                    newinfo = editor.getExtInfo();
                    List<ExtInfo> infos = CANAIS.getAllExtInfo();
                    infos.remove(selec);
                    infos.add(newinfo);
                    saveList(infos);
                }
            } catch (IOException e) {
                IPTVPlayer.error(e, PrincipalController.class);
                e.printStackTrace();
                System.exit(3);
            }
        }
    }

    /**
     * acao para remover um canal na lista local
     *
     * @param evt
     */
    private void handleRemoverCanal(ActionEvent evt) {
        saveList(removerCanais(canalSelecionado));
    }

    private List<ExtInfo> removerCanais(List<Channel> apS) {
        List<ExtInfo> lista = CANAIS.getAllExtInfo();
        for (Channel info : apS) {
            lista = removerCanal(info.getChannel(), lista);
        }
        return lista;
    }

    private List<ExtInfo> removerCanal(ExtInfo info, List<ExtInfo> infos) {
        infos.removeIf((a) ->
                a.getCanalURL().equals(info.getCanalURL())
        );
        return infos;
    }

    private void handleRemoverCanaisOff(ActionEvent evt) {
        List<Channel> onlines = new ArrayList<>();
        onlines.addAll(canais.stream().filter((a) -> (a.isChanged() && a.isAlive())).collect(Collectors.toList()));
        saveList(removerCanais(onlines));
    }

    /**
     * Metodo para criar o contextmenu do canal.
     *
     * @return
     */
    protected ContextMenu contextMenuForCanais() {
        ContextMenu contextMenu = new ContextMenu();
        //MenuItem
        MenuItem abrirMenu = new MenuItem("Abrir Canal");
        MenuItem editarMenu = new MenuItem("Editar Canal");
        MenuItem removerMenu = new MenuItem("Excluir Canal");
        MenuItem removerOffMenu = new MenuItem("Excluir Canais Offline");
        MenuItem atualizarMenu = new MenuItem("Atualizar Agora");
        CheckMenuItem autoAtualizar = new CheckMenuItem("Auto Atualizar");
        //adicionando na ordem
        contextMenu.getItems().add(abrirMenu);
        contextMenu.getItems().add(editarMenu);
        contextMenu.getItems().add(removerMenu);
        contextMenu.getItems().add(removerOffMenu);
        contextMenu.getItems().add(atualizarMenu);
        contextMenu.getItems().add(autoAtualizar);
        //Acoes
        abrirMenu.setOnAction(this::handleAbrirCanal);
        editarMenu.setOnAction(this::handleEditarCanal);
        removerMenu.setOnAction(this::handleRemoverCanal);
        autoAtualizar.setOnAction(this::handleAutoAtualiar);
        removerOffMenu.setOnAction(this::handleRemoverCanaisOff);
        atualizarMenu.setOnAction(this::handleAtualizarAgora);
        //Exibir ou ocultar alguns itens.
        autoAtualizar.setSelected(true);
        contextMenu.showingProperty().addListener(a -> {
            if (Canais.getSelectionModel().getSelectedItems().size() == 1) {
                abrirMenu.setVisible(true);
                removerMenu.setVisible(true);
                editarMenu.setVisible(true);
            } else if (Canais.getSelectionModel().getSelectedItems().size() > 1) {
                editarMenu.setVisible(true);
                removerMenu.setVisible(true);
                abrirMenu.setVisible(false);
            }
            removerOffMenu.setVisible(true);
        });
        return contextMenu;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Configurar a  interface
        entCanal.setOnAction(this::handleCanalBusca);
        entGrupo.setOnAction(this::handleGrupoBusca);

        Canais.setContextMenu(contextMenuForCanais());
        Canais.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> {
            canalSelecionado = Canais.getSelectionModel().getSelectedItems();

        });
        Canais.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Canais.setCellFactory(new CallBackCanal());

        update = new ChannelUpdate(M3U);
        update.valueProperty().addListener((a, b, c) -> {
            if (c == null) {
                status.setText("Lista vazia.");
                return;
            }
            injectExtInfoListInInterface(c);
        });
        update.stateProperty().addListener((a, b, c) -> {
            if (c.equals(Worker.State.RUNNING)) {
                status.setText("Carregando lista...");
                isloaded = false;
            } else if (c.equals(Worker.State.FAILED)) {
                IPTVPlayer.error(update.getException().toString(), getClass());
                status.setText("Erro ao carregar a lista.");
            }
        });
        update.start();
    }
}