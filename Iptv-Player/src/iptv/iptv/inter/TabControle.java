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

package iptv.inter;

import com.nataniel.inter.ExtInfo;
import com.nataniel.list.ExtInfoList;
import iptv.fxml.PrincipalController;
import iptv.player.Player;
import iptv.service.Channel;
import iptv.service.ChannelObserver;
import iptv.service.ChannelUpdate;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public abstract class TabControle {
    @FXML
    protected ListView<Channel> Canais;

    @FXML
    protected TextField entCanal;

    @FXML
    protected TextField entGrupo;
    @FXML
    protected Label status;
    protected List<Channel> canalSelecionado = null;
    protected ExtInfoList CANAIS = null;
    protected List<Channel> canais = null;
    protected ChannelUpdate update;
    protected boolean isloaded = false;
    @FXML
    private ListView<Button> Grupos;
    private ChannelObserver observer;
    private List<Button> GRUPOS = null;
    private Player player;
    private Timer timer;
    private boolean isAllow = true;

    public TabControle(int time) {
        timer = new Timer();
        timer.schedule(getTimeTask(), time);
    }

    public void closeTab() {
        if (update != null) update.cancel();
        if (observer != null) observer.cancel();
        if (timer != null) timer.cancel();
    }

    protected TimerTask getTimeTask() {
        return new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (isloaded && isAllow) {
                        update.restart();
                    }
                });
            }
        };
    }

    /**
     * Utilizado para fazer a busca de grupos
     */
    private final EventHandler<ActionEvent> grupoBusca = (a) -> {
        Button ob = GRUPOS.stream().filter((aa) -> StringUtils.containsIgnoreCase(aa.getText(), entGrupo.getText())).findFirst().orElse(null);
        if (ob == null) return;
        Grupos.scrollTo(ob);
        Grupos.getSelectionModel().select(ob);
    };

    protected void handleCanalBusca(ActionEvent a) {
        List<ExtInfo> lista = CANAIS.getAllExtInfo();
        int index = 0;
        for (int i = 0; i < canais.size(); i++) {
            Channel ab = canais.get(i);
            if (StringUtils.containsIgnoreCase(ab.getChannel().getCanalNome(), entCanal.getText())) {
                index = i;
                break;
            }
        }
        Canais.scrollTo(index);
        Canais.getSelectionModel().select(index);
    }

    /**
     * Açao do botao grupo.
     */
    protected void handleGrupoBusca(ActionEvent e) {
        Button button = (Button) e.getSource();
        novosCanais(canais.stream().filter(a -> a.getChannel().getGrupo().equals(button.getText())).collect(Collectors.toList()));
    }

    /**
     * Metodo para facilitar a inserção da lista de canais
     *
     * @param lis Lista de infos
     */
    protected void novosCanais(List<Channel> lis) {
        Callback<ListView<Channel>, ListCell<Channel>> cellFactory = Canais.getCellFactory();
        Canais.setCellFactory(null);
        Canais.setItems(FXCollections.observableList(lis));
        Canais.setCellFactory(cellFactory);
    }

    /**
     * Metodo para facilitar a inserçao da lista de grupos.
     *
     * @param lis Lista de botoes
     */
    protected void novosGrupos(List<Button> lis) {
        Grupos.setItems(FXCollections.observableList(lis));
    }

    /**
     * Metodo para atualizar graficamente o ListView.
     */
    protected void updateCanais() {
        if (Canais.getContextMenu().isShowing()) return;
        ObservableList<Channel> canaiss = Canais.getItems();
        Canais.setItems(null);
        Canais.setItems(canaiss);

    }

    protected void handleAtualizarAgora(ActionEvent evt) {
        if (isloaded) {
            update.restart();
        }
    }

    protected void handleAutoAtualiar(ActionEvent evt) {
        CheckMenuItem check = (CheckMenuItem) evt.getSource();
        isAllow = check.isSelected();
    }

    /**
     * acao para abrir um canal
     *
     * @param evt
     */
    protected void handleAbrirCanal(ActionEvent evt) {
        if (!PrincipalController.player.get()) {
            player = new Player(canalSelecionado.get(0).getChannel().getCanalURL(), PrincipalController.player);
            PrincipalController.player.set(true);
            player.setVisible(true);
        } else {
            player.toFront();
        }
    }

    /**
     * Metodo para injetar uma ExtInfo na janela.
     *
     * @param infoList
     */
    protected void injectExtInfoListInInterface(ExtInfoList infoList) {
        CANAIS = infoList;
        canais = infoList.getAllExtInfo().stream().map(Channel::new).collect(Collectors.toList());
        novosCanais(canais);
        List<Button> grupos = new ArrayList<>();
        Button todos = new Button("Todos");
        todos.setOnAction((a) -> novosCanais(canais));
        todos.setBackground(Background.EMPTY);
        infoList.getAllGroups().forEach((a) -> {
            if (a == null || a.isEmpty()) return;
            Button bt = new Button(a);
            bt.setOnAction(this::handleGrupoBusca);
            bt.setBackground(Background.EMPTY);
            grupos.add(bt);
        });
        if (!grupos.isEmpty()) grupos.add(todos);
        GRUPOS = grupos;
        novosGrupos(grupos);
        if (observer != null && observer.isRunning()) {
            observer.cancel();
        }
        observer = new ChannelObserver(canais);
        observer.messageProperty().addListener((a, b, c) -> {
            status.setText(c);
            updateCanais();
        });
        observer.stateProperty().addListener((a, b, c) -> {
            if (c.equals(Worker.State.SUCCEEDED)) {
                isloaded = true;
            }
        });
        observer.start();
    }

    protected abstract ContextMenu contextMenuForCanais();
}
