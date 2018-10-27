/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iptv.player;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
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

    public static boolean isvlcopen = false;

    private final EventHandler<ActionEvent> grupos = (e) -> {
        Button bt = (Button) e.getSource();
        novaLista((List<Info>) bt.getUserData());
    };

    private void novaLista(List<Info> lis) {
        Canais.getItems().clear();
        Canais.getItems().addAll(lis);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String m3u = Propriedades.instancia.M3U_URL;
        if (m3u==null){
            FileChooser fil = new FileChooser();
            File f = fil.showOpenDialog(null);
            if (f==null){
                System.exit(0);
            }else{
                m3u = f.getPath();
                Propriedades.instancia.salvar("m3u", m3u);
            }
        }
        try {
            InputStream stream = new FileInputStream(m3u);
            List<Info> list = M3uParser.parser(stream);
            Canais.setCellFactory((param) -> {
                return new ListCell<Info>() {
                    @Override
                    protected void updateItem(Info item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
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
                                    }
                                });
                                diag.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                diag.setVisible(true);
                            });
                            setText(item.getNome());
                        }
                    }

                }; //To change body of generated lambdas, choose Tools | Templates.
            });
            novaLista(list);
            M3uParser.getGrupos(list).forEach((chave, valor) -> {
                Button bt = new Button(chave);
                bt.setOnAction(grupos);
                bt.setUserData(valor);
                bt.setBackground(Background.EMPTY);
                Grupos.getItems().add(bt);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(3);
        }
    }

}
