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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class TabController implements Initializable {


    @FXML
    private HBox hbox;

    @FXML
    private ListView<Channel> Canais;

    @FXML
    private TextField entCanal;

    @FXML
    private TextField entGrupo;

    @FXML
    private AnchorPane GrupoPane;

    @FXML
    private ListView<Button> Grupos;

    @FXML
    private Label status;

    //Objetos da classe
    private ExtInfoList CANAIS = null;
    private List<Channel> canais = null;
    private static List<TabController> controllers = null;
    private final boolean isLocal;

    public static void closeAllTab() {
        controllers.forEach((a) -> {
            a.closeTab();
        });
    }

    private final String M3U;
    private Channel canalSelecionado = null;
    private ChannelObserver observer;
    private ChannelUpdate update;
    private List<Button> GRUPOS = null;
    private boolean isloaded = false;
    private boolean isAllow = true;
    private Timer timer;

    public TabController(String m3U) {
        this(m3U, false);
    }

    public TabController(String m3U, boolean local) {
        if (controllers == null) {
            controllers = new ArrayList<>();
        }
        controllers.add(this);
        M3U = m3U;
        isLocal = local;
        timer = new Timer("timer");
        timer.schedule(getTimeTask(), 0, (isLocal ? Propriedades.instancia.getLocalTime() : Propriedades.instancia.getLinkTime()));
    }

    public String getM3U() {
        return M3U;
    }

    public void closeTab() {
        update.cancel();
        observer.cancel();
        timer.cancel();
    }

    private TimerTask getTimeTask() {
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
     * Utilizado pra fazer a busca de canal
     */
    private final EventHandler<ActionEvent> canalBusca = (a) -> {
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
    };
    /**
     * Açao do botao grupo.
     */
    private final EventHandler<ActionEvent> grupoAct = (e) -> {
        Button button = (Button) e.getSource();
        novosCanais(canais.stream().filter(a -> a.getChannel().getGrupo().equals(button.getText())).collect(Collectors.toList()));
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

    /**
     * Metodo para atualizar graficamente o ListView.
     */
    private void updateCanais() {
        ObservableList<Channel> canaiss = Canais.getItems();
        int i;
        if (Canais.getSelectionModel().isEmpty()) {
            i = -1;
        } else {
            i = Canais.getSelectionModel().getSelectedIndex();
        }
        Canais.setItems(null);
        Canais.setItems(canaiss);
        if (i != -1) Canais.getSelectionModel().select(i);
    }

    private void handleAutoAtualiar(ActionEvent evt) {
        CheckMenuItem check = (CheckMenuItem) evt.getSource();
        isAllow = check.isSelected();
    }
    /**
     * acao para abrir um canal
     *
     * @param evt
     */
    private void handleAbrirCanal(ActionEvent evt) {
        if (!PrincipalController.player.get()) {
            Player play = new Player(canalSelecionado.getChannel().getCanalURL(), PrincipalController.player);
            play.setVisible(true);
        }
    }

    /**
     * acao para editar um canal
     *
     * @param evt
     */
    private void handleEditarCanal(ActionEvent evt) {
        if (canalSelecionado == null) return;
        ExtInfo selec = canalSelecionado.getChannel();
        ExtInfoEditor editor = new ExtInfoEditor(selec);
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

                CANAIS.getAllExtInfo().remove(selec);
                CANAIS.getAllExtInfo().add(newinfo);
                Parser.ParserExtInfoListToFile(CANAIS, new File(m3u));

                update.setM3u(m3u);
                update.restart();
            }
        } catch (IOException e) {
            IPTVPlayer.error(e, PrincipalController.class);
            e.printStackTrace();
            System.exit(3);
        }
    }

    /**
     * acao de salvar o canal na lista local
     *
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
                File f;
                if (m3u == null) {
                    f = new File("local.m3u");
                    Propriedades.instancia.setM3uLocal("local.m3u");
                    f.createNewFile();
                } else {
                    f = new File(m3u);
                }
                ExtInfoList infoList = Parser.parserExtM3u8(new FileInputStream(f));
                infoList.getAllExtInfo().add(newinfo);
                Parser.ParserExtInfoListToFile(infoList, f);
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
     * Metodo para injetar uma ExtInfo na janela.
     *
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
        observer.stateProperty().addListener((a, b, c) -> {
            if (c.equals(Worker.State.SUCCEEDED)) {
                isloaded = true;
            }
        });
        observer.start();
    }

    /**
     * Metodo para criar o contextmenu do canal.
     *
     * @return
     */
    private ContextMenu contextMenuForCanais() {
        ContextMenu contextMenu = new ContextMenu();
        //MenuItem
        MenuItem abrirMenu = new MenuItem("Abrir Canal");
        MenuItem editarMenu = new MenuItem("Editar Canal");
        MenuItem salvarMenu = new MenuItem("Salvar Canal");
        MenuItem removerMenu = new MenuItem("Excluir Canal");
        CheckMenuItem autoAtualizar = new CheckMenuItem("Auto Atualizar");
        //adicionando na ordem
        contextMenu.getItems().add(abrirMenu);
        contextMenu.getItems().add(editarMenu);
        contextMenu.getItems().add(salvarMenu);
        contextMenu.getItems().add(removerMenu);
        contextMenu.getItems().add(autoAtualizar);
        //Acoes
        salvarMenu.setOnAction(this::handleSalvarCanal);
        abrirMenu.setOnAction(this::handleAbrirCanal);
        editarMenu.setOnAction(this::handleEditarCanal);
        removerMenu.setOnAction(this::handleRemoverCanal);
        autoAtualizar.setOnAction(this::handleAutoAtualiar);
        //Exibir ou ocultar alguns itens.
        autoAtualizar.setSelected(true);
        contextMenu.showingProperty().addListener(a -> {
            if (canalSelecionado != null) {
                salvarMenu.setVisible(!isLocal);
                removerMenu.setVisible(isLocal);
                editarMenu.setVisible(isLocal);
            }
        });
        return contextMenu;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Configurar a  interface
        entCanal.setOnAction(canalBusca);
        entGrupo.setOnAction(grupoBusca);

        Canais.setContextMenu(contextMenuForCanais());
        Canais.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> canalSelecionado = c);
        Canais.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
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
