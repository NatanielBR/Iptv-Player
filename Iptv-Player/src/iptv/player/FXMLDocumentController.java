/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iptv.player;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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
import javax.swing.JDialog;
import m3uteste.pkg2.Info;
import m3uteste.pkg2.M3uParser;
import uk.co.caprica.vlcj.component.EmbeddedMediaListPlayerComponent;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

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

    private EventHandler<ActionEvent> canalBusca = (a) -> {
        String cont = entCanal.getText();
        if (cont.isEmpty()) {
            novosCanais(CANAIS);
        } else {
            List<Info> inf = CANAIS.stream().filter((as) -> as.getNome().toLowerCase().contains(cont.toLowerCase())).collect(Collectors.toList());
            novosCanais(inf);
        }
    };

    private EventHandler<ActionEvent> grupoBusca = (a) -> {
        String cont = entGrupo.getText();
        if (cont.isEmpty()) {
            novosGrupos(GRUPOS);
        } else {
            List<Button> inf = GRUPOS.stream().filter((as) -> as.getText().toLowerCase().contains(cont.toLowerCase())).collect(Collectors.toList());
            novosGrupos(inf);
        }
    };

    public static boolean isvlcopen = false;

    private final EventHandler<ActionEvent> grupoAct = (e) -> {
        Button bt = (Button) e.getSource();
        novosCanais((List<Info>) bt.getUserData());
    };

    private void novosCanais(List<Info> lis) {
        Canais.setItems(FXCollections.observableList(lis));
    }

    private void novosGrupos(List<Button> lis) {
        Grupos.setItems(FXCollections.observableList(lis));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String m3u = Propriedades.instancia.getM3u();
        entCanal.setOnAction(canalBusca);
        entGrupo.setOnAction(grupoBusca);
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
            List<Info> list = M3uParser.parser(m3u);
            CANAIS = list;
            Canais.setCellFactory((param) -> {
                return new ListCell<Info>() {
                    @Override
                    protected void updateItem(Info item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setOnMouseClicked((event) -> {
                                JDialog diag = new JDialog();
                                EmbeddedMediaPlayerComponent comp = new EmbeddedMediaListPlayerComponent();
                                comp.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                                });
                                diag.add(comp);
                                diag.setSize(500, 470);
                                diag.addWindowListener(new WindowAdapter() {
                                    @Override
                                    public void windowOpened(WindowEvent we) {
                                        Canais.getScene().getRoot().setDisable(true);
                                        super.windowOpened(we);
                                        comp.getMediaPlayer().playMedia(item.getURL());
                                        isvlcopen = true;
                                    }

                                    @Override
                                    public void windowClosing(WindowEvent we) {
                                        Canais.getScene().getRoot().setDisable(false);
                                        super.windowClosing(we);
                                        comp.release();
                                        isvlcopen = false;
                                        System.gc();
                                    }
                                });
                                diag.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                diag.setVisible(true);
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
            IPTVPlayer.error(ex);
        }
    }

}
