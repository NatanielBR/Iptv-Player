/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iptv.player;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Background;
import javafx.stage.FileChooser;
import m3uteste.pkg2.Info;
import m3uteste.pkg2.M3uParser;

/**
 *
 * @author Nataniel
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private ListView<Info> Canais;
    @FXML
    private ListView<Button> Grupos;
    @FXML
    private TextField entGrupo, entCanal;
    private List<Info> CANAIS = null;
    private List<Button> GRUPOS = null;
    /**
     * Utilizado pra fazer a busca de canal
     */
    private final EventHandler<ActionEvent> canalBusca = (a) -> {
        String cont = entCanal.getText();
        if (cont.isEmpty()) {
            novosCanais(CANAIS);
        } else {
            List<Info> inf = CANAIS.stream().filter((as) -> as.getNome().toLowerCase().contains(cont.toLowerCase())).collect(Collectors.toList());
            novosCanais(inf);
        }
    };
    /**
     * Utilizado para fazer a busca de grupos
     */
    private final EventHandler<ActionEvent> grupoBusca = (a) -> {
        String cont = entGrupo.getText();
        if (cont.isEmpty()) {
            novosGrupos(GRUPOS);
        } else {
            List<Button> inf = GRUPOS.stream().filter((as) -> as.getText().toLowerCase().contains(cont.toLowerCase())).collect(Collectors.toList());
            novosGrupos(inf);
        }
    };
    //Utilizado para saber se a caixa de dialogo esta aberta (Obsoleto)
    public static boolean isvlcopen = false;
    private AtomicBoolean player = null;
    /**
     * Açao do botao grupo.
     */
    private final EventHandler<ActionEvent> grupoAct = (e) -> {
        Button bt = (Button) e.getSource();
        novosCanais((List<Info>) bt.getUserData());
    };

    /**
     * Metodo para facilitar a inserção da lista de canais(tinha mais comandos,
     * agora esta mais simples)
     *
     * @param lis Lista de infos
     */
    private void novosCanais(List<Info> lis) {
        Canais.setItems(FXCollections.observableList(lis));
    }

    /**
     * Metodo para facilitar a inserçao da lista de grupos.
     *
     * @param lis
     */
    private void novosGrupos(List<Button> lis) {
        Grupos.setItems(FXCollections.observableList(lis));
    }

    /**
     * Longo initialize kk
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String m3u = Propriedades.instancia.getM3u();
        entCanal.setOnAction(canalBusca);
        entGrupo.setOnAction(grupoBusca);
        player = new AtomicBoolean(true);
        if (m3u == null) {
            TextInputDialog diag = new TextInputDialog();
            diag.setHeaderText("Informe a url/caminho\nDeixe vazio para a escolha de arquivo");
            m3u = diag.showAndWait().orElse("");
            if (m3u.isEmpty()) {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo M3U", "*.m3u"));
                File f = chooser.showOpenDialog(null);
                if (f == null) {
                    System.exit(3);
                } else {
                    m3u = f.getPath();
                    Propriedades.instancia.setM3u(m3u);
                }
            } else {
                Propriedades.instancia.setM3u(m3u);
            }
        }
        try {
            List<Info> list = m3u.contains("http")?M3uParser.parser(new URL(m3u)):M3uParser.parser(new File(m3u));
            CANAIS = list;
            Canais.setCellFactory((param) -> {
                return new ListCell<Info>() {
                    @Override
                    protected void updateItem(Info item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setOnMouseClicked((event) -> {
                                if (player.get()) {
                                    Player play = new Player(item.getURL(), player);
                                    play.setVisible(true);
                                }
                            });
                            setText(item.getNome());
                        }
                    }

                };
            });
            novosCanais(list);
            List<Button> grupos = new ArrayList<>();
            M3uParser.getGrupos(list).forEach((chave, valor) -> {
                Button bt = new Button(chave);
                bt.setOnAction(this.grupoAct);
                bt.setUserData(valor);
                bt.setBackground(Background.EMPTY);
                grupos.add(bt);
            });
            GRUPOS = grupos;
            novosGrupos(grupos);
        } catch (Exception ex) {
            IPTVPlayer.error(ex, getClass());
        }
    }

}
