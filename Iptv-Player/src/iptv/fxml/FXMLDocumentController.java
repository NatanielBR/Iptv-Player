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

import com.nataniel.ExtInfo;
import com.nataniel.ExtInfoList;
import iptv.IPTVPlayer;
import iptv.Propriedades;
import iptv.player.Player;
import iptv.service.Channel;
import iptv.service.ChannelObserver;
import iptv.service.ChannelUpdate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Nataniel
 */
public class FXMLDocumentController implements Initializable {
    public static AtomicBoolean player = null;
    //FXML componentes
    @FXML
    private ListView<Channel> Canais;
    @FXML
    private ListView<Button> Grupos;
    @FXML
    private TextField entGrupo, entCanal;
    @FXML
    private Label status;
    @FXML
    private AnchorPane GrupoPane;
    @FXML
    private HBox hbox;
    //Objetos da classe
    private ExtInfoList CANAIS = null;
    private List<Channel> canais = null;
    /**
     * Açao do botao grupo.
     */
    private final EventHandler<ActionEvent> grupoAct = (e) -> {
        Button button = (Button) e.getSource();
        novosCanais(canais.stream().filter(a -> a.getCanal().getGrupo().equals(button.getText())).collect(Collectors.toList()));
    };
    /**
     * Utilizado pra fazer a busca de canal
     */
    private final EventHandler<ActionEvent> canalBusca = (a) -> {
        List<ExtInfo> lista = CANAIS.getAllExtInfo();
        Channel canal = canais.stream().filter((ab) -> StringUtils.containsIgnoreCase(ab.getCanal().getCanalNome(), entCanal.getText())).findFirst().orElse(null);
        if (canal == null) return;
        Canais.scrollTo(canal);
        Canais.getSelectionModel().select(canal);
    };
    private ChannelObserver observer;
    private ChannelUpdate update;
    private List<Button> GRUPOS = null;
    /**
     * Utilizado para fazer a busca de grupos
     */
    private final EventHandler<ActionEvent> grupoBusca = (a) -> {
        Button ob = GRUPOS.stream().filter((aa) -> StringUtils.containsIgnoreCase(aa.getText(), entGrupo.getText())).findFirst().orElse(null);
        if (ob == null) return;
        Grupos.scrollTo(ob);
        Grupos.getSelectionModel().select(ob);
    };

    //FXML metodos
    @FXML
    private void handleGrupoDisable(ActionEvent evt) {
        CheckMenuItem menu = (CheckMenuItem) evt.getSource();
        if (menu.isSelected()) {
            hbox.getChildren().add(GrupoPane);
        } else {
            hbox.getChildren().remove(GrupoPane);
        }
        hbox.autosize();
    }

    @FXML
    private void handleTrocaRapidaChange(ActionEvent evt) {
        String m3u = getM3ULink();
        Propriedades.instancia.setM3u(m3u);
        if (update.isRunning()) update.cancel();
        update.restart();
    }

    /**
     * Metodo para facilitar a inserção da lista de canais
     *
     * @param lis Lista de infos
     */
    private void novosCanais(List<Channel> lis) {
        Canais.setItems(FXCollections.observableList(lis));
    }

    /**
     * Metodo para facilitar a inserçao da lista de grupos.
     *
     * @param lis Lista de botoes
     */
    private void novosGrupos(List<Button> lis) {
        Grupos.setItems(FXCollections.observableList(lis));
    }


    private String getM3ULink() {
        TextInputDialog diag = new TextInputDialog();
        diag.setHeaderText("Informe uma URL");
        String m3u = diag.showAndWait().orElse(null);
        if (m3u == null) System.exit(0);
        return m3u;
    }

    private void injectExtInfoListInInterface(ExtInfoList infoList) {
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
            bt.setOnAction(this.grupoAct);
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
        observer.start();
    }

    private void updateCanais() {
        ObservableList<Channel> canaiss = Canais.getItems();
        Canais.setItems(null);
        Canais.setItems(canaiss);
    }

    private ContextMenu contextMenuForCanais() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem canalRapido = new MenuItem("Troca rapida de lista");
        canalRapido.setOnAction(this::handleTrocaRapidaChange);
        contextMenu.getItems().add(canalRapido);
        return contextMenu;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        entCanal.setOnAction(canalBusca);
        entGrupo.setOnAction(grupoBusca);
        player = new AtomicBoolean(false);

        String m3u = Propriedades.instancia.getM3u();
        if (m3u == null) {
            m3u = getM3ULink();
            Propriedades.instancia.setM3u(m3u);
        }
        try {
            update = new ChannelUpdate();
            update.valueProperty().addListener((a, b, c) -> {
                if (c == null) return;
                injectExtInfoListInInterface(c);
            });
            update.stateProperty().addListener((a, b, c) -> {
                if (c.equals(Worker.State.RUNNING)) {
                    status.setText("Carregando lista...");
                }
            });
            update.start();
            Canais.setContextMenu(contextMenuForCanais());
            Canais.setCellFactory((param) -> new ListCell<Channel>() {
                @Override
                protected void updateItem(Channel item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        ExtInfo ext = item.getCanal();
                        setOnMouseClicked((event) -> {
                            if (event.getButton() == MouseButton.SECONDARY) {
                                param.getContextMenu().show(this, event.getScreenX(), event.getScreenY());
                                return;
                            }
                            if (!player.get()) {
                                Player play = new Player(ext.getCanalURL(), player);
                                player.set(true);
                                play.setVisible(true);
                            }
                        });
                        setText(ext.getCanalNome());
                        int state;
                        if (item.isChanged()) {
                            state = item.isAlive() ? 0 : 2;

                        } else {
                            state = 1;
                        }
                        setGraphic(new ImageView(String.format("iptv/res/%d.png", state)));
                    }
                }

            });

        } catch (Exception ex) {
            IPTVPlayer.error(ex, getClass());
        }
    }

}
