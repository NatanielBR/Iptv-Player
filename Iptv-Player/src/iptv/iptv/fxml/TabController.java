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
import com.nataniel.inter.ExtInfo;
import com.nataniel.list.ExtInfoList;
import iptv.IPTVPlayer;
import iptv.Propriedades;
import iptv.inter.TabControle;
import iptv.service.ChannelUpdate;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TabController extends TabControle implements Initializable {
    //Objetos da classe
    private final String M3U;

    public TabController(String m3U) {
        super(Propriedades.instancia.getLinkTime());
        M3U = m3U;

    }

    public String getM3U() {
        return M3U;
    }

    private void saveOnLocal(List<ExtInfo> lis) {
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
            ExtInfoList infoList = Parser.parserExtM3u8(new FileInputStream(f));
            infoList.getAllExtInfo().addAll(lis);
            Parser.ParserChannelListToFile(infoList, f);
            PrincipalController.local.update();
        } catch (Exception e) {
            IPTVPlayer.error(e, getClass());
        }
    }
    /**
     * acao de salvar o canal na lista local
     *
     * @param evt
     */
    public void handleSalvarCanal(ActionEvent evt) {
        if (canalSelecionado.size() == 1) {
            ExtInfoEditor editor = new ExtInfoEditor(canalSelecionado.get(0).getChannel());
            FXMLLoader loader = new FXMLLoader();
            loader.setController(editor);
            try {
                Parent par = loader.load(editor.getClass().getResourceAsStream("ExtInfoEditor.fxml"));
                Alert alert = new Alert(Alert.AlertType.NONE);
                DialogPane pane = new DialogPane();
                pane.setContent(par);
                alert.setDialogPane(pane);
                alert.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                ButtonType type = alert.showAndWait().get();
                if (type == ButtonType.OK) {
                    saveOnLocal(Arrays.asList(editor.getExtInfo()));
                }
            } catch (IOException e) {
                IPTVPlayer.error(e, PrincipalController.class);
                System.exit(3);
            }
        } else {
            saveOnLocal(canalSelecionado.stream().map(a -> a.getChannel()).collect(Collectors.toList()));

        }

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
        MenuItem salvarMenu = new MenuItem("Salvar Canal");
        MenuItem atualizarMenu = new MenuItem("Atualizar Agora");
        CheckMenuItem autoAtualizar = new CheckMenuItem("Auto Atualizar");
        //adicionando na ordem
        contextMenu.getItems().add(abrirMenu);
        contextMenu.getItems().add(salvarMenu);
        contextMenu.getItems().add(atualizarMenu);
        contextMenu.getItems().add(autoAtualizar);
        //Acoes
        salvarMenu.setOnAction(this::handleSalvarCanal);
        abrirMenu.setOnAction(this::handleAbrirCanal);
        autoAtualizar.setOnAction(this::handleAutoAtualiar);
        atualizarMenu.setOnAction(this::handleAtualizarAgora);
        //Exibir ou ocultar alguns itens.
        autoAtualizar.setSelected(true);
        contextMenu.showingProperty().addListener(a -> {
            if (Canais.getSelectionModel().getSelectedItems().size() == 1) {
                abrirMenu.setVisible(true);
                salvarMenu.setVisible(true);
            } else if (Canais.getSelectionModel().getSelectedItems().size() > 1) {
                salvarMenu.setVisible(true);
                abrirMenu.setVisible(false);
            }
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
