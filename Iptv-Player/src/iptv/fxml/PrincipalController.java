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
import com.nataniel.Parser;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Nataniel
 */
public class PrincipalController implements Initializable {
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
    private Channel canalSelecionado = null;
    private ChannelObserver observer;
    private ChannelUpdate update;
    private List<Button> GRUPOS = null;
    private boolean isLocal = false;
    private String urlUltimo = null;
    /**
     * Açao do botao grupo.
     */
    private final EventHandler<ActionEvent> grupoAct = (e) -> {
        Button button = (Button) e.getSource();
        novosCanais(canais.stream().filter(a -> a.getChannel().getGrupo().equals(button.getText())).collect(Collectors.toList()));
    };
    /**
     * Utilizado pra fazer a busca de canal
     */
    private final EventHandler<ActionEvent> canalBusca = (a) -> {
        List<ExtInfo> lista = CANAIS.getAllExtInfo();
        Channel canal = canais.stream().filter((ab) -> StringUtils.containsIgnoreCase(ab.getChannel().getCanalNome(), entCanal.getText())).findFirst().orElse(null);
        if (canal == null) return;
        Canais.scrollTo(canal);
        Canais.getSelectionModel().select(canal);
    };
    /**
     * Utilizado para fazer a busca de grupos
     */
    private final EventHandler<ActionEvent> grupoBusca = (a) -> {
        Button ob = GRUPOS.stream().filter((aa) -> StringUtils.containsIgnoreCase(aa.getText(), entGrupo.getText())).findFirst().orElse(null);
        if (ob == null) return;
        Grupos.scrollTo(ob);
        Grupos.getSelectionModel().select(ob);
    };

    /**
     * acao de troocar de lista
     *
     * @param evt
     */
    private void handleTrocaRapida(ActionEvent evt) {
        String m3u = getM3ULink();
        Propriedades.instancia.setM3u(m3u);
        if (update.isRunning()) update.cancel();
        update.setM3u(Propriedades.instancia.getM3u());
        update.restart();
        isLocal = false;
    }

    /**
     * acaao de trocar para ultima lista
     *
     * @param evt
     */
    private void handleUltimaLista(ActionEvent evt) {
        String m3u = urlUltimo;
        Propriedades.instancia.setM3u(m3u);
        if (update.isRunning()) update.cancel();
        update.setM3u(urlUltimo);
        update.restart();
        isLocal = false;
    }

    /**
     * acao para abrir um canal
     *
     * @param evt
     */
    private void handleAbrirCanal(ActionEvent evt) {
        if (!player.get()) {
            Player play = new Player(canalSelecionado.getChannel().getCanalURL(), player);
            play.setVisible(true);
        }
    }

    /**
     * acao para abrir a lista local
     * @param evt
     */
    private void handleTrocaLocal(ActionEvent evt) {
        String m3u = Propriedades.instancia.getM3uLocal();
        File f = null;
        if (m3u == null) {
            m3u = "local.m3u";
            Propriedades.instancia.setM3uLocal(m3u);
        }
        f = new File(m3u);
        update.setM3u(m3u);
        if (update.isRunning()) update.cancel();
        update.restart();
        isLocal = true;
        urlUltimo = Propriedades.instancia.getM3u();
    }

    /**
     * acao de salvar o canal na lista local
     * @param evt
     */
    private void handleSalvarCanal(ActionEvent evt) {
        ExtInfoEditor editor = new ExtInfoEditor(canalSelecionado.getChannel());
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
                ExtInfo newinfo = editor.getExtInfo();
                String m3u = Propriedades.instancia.getM3uLocal();
                if (m3u == null) {
                    IPTVPlayer.error("Arquivo não existe.\nPrimeiro troque pelo menos uma vez para a Lista local.", PrincipalController.class);
                    return;
                }
                ExtInfoList infoList = Parser.parserExtM3u8(new FileInputStream(Propriedades.instancia.getM3uLocal()));
                infoList.getAllExtInfo().add(newinfo);
                Parser.ParserExtInfoListToFile(infoList, new File(m3u));
            }
        } catch (IOException e) {
            IPTVPlayer.error(e, PrincipalController.class);
            e.printStackTrace();
            System.exit(3);
        }

    }

    /**
     * acao para remover um canal na lista local
     *
     * @param evt
     */
    private void handleRemoverCanal(ActionEvent evt) {
        ExtInfo info = canalSelecionado.getChannel();
        CANAIS.getAllExtInfo().remove(info);
        try {
            Parser.ParserExtInfoListToFile(CANAIS, new File(Propriedades.instancia.getM3uLocal()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        update.setM3u(Propriedades.instancia.getM3uLocal());
        update.restart();
    }

    /**
     * Metodo para facilitar a inserção da lista de canais
     *
     * @param lis Lista de infos
     */
    private void novosCanais(List<Channel> lis) {
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

    /**
     * Metodo para injetar uma ExtInfo na janela.
     * @param infoList
     */
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

    /**
     * Metodo para atualizar graficamente o ListView.
     */
    private void updateCanais() {
        ObservableList<Channel> canaiss = Canais.getItems();
        Canais.setItems(null);
        Canais.setItems(canaiss);
    }

    /**
     * Metodo para criar o contextmenu do canal.
     * @return
     */
    private ContextMenu contextMenuForCanais() {
        ContextMenu contextMenu = new ContextMenu();
        //MenuItem
        MenuItem novaLista = new MenuItem("Abrir nova lista");
        MenuItem ultimaLista = new MenuItem("Abrir lista externa");
        MenuItem listaLocal = new MenuItem("Abrir lista local");
        MenuItem abrirMenu = new MenuItem("Abrir Canal");
        MenuItem salvarMenu = new MenuItem("Salvar Canal");
        MenuItem removerMenu = new MenuItem("Excluir Canal");
        //adicionando na ordem
        contextMenu.getItems().add(novaLista);
        contextMenu.getItems().add(ultimaLista);
        contextMenu.getItems().add(listaLocal);
        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().add(abrirMenu);
        contextMenu.getItems().add(salvarMenu);
        contextMenu.getItems().add(removerMenu);
        //Acoes
        salvarMenu.setOnAction(this::handleSalvarCanal);
        abrirMenu.setOnAction(this::handleAbrirCanal);
        ultimaLista.setOnAction(this::handleUltimaLista);
        novaLista.setOnAction(this::handleTrocaRapida);
        listaLocal.setOnAction(this::handleTrocaLocal);
        removerMenu.setOnAction(this::handleRemoverCanal);
        //Exibir ou ocultar alguns itens.
        contextMenu.showingProperty().addListener(a -> {
            salvarMenu.setVisible(canalSelecionado != null);
            removerMenu.setVisible(isLocal);
            ultimaLista.setVisible(isLocal);
            listaLocal.setVisible(!isLocal);
        });
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
            Canais.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> canalSelecionado = c);
            Canais.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            Canais.setCellFactory(new CallBackCanal());
        } catch (Exception ex) {
            IPTVPlayer.error(ex, getClass());
        }
    }

}
