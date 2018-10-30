/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iptv.player;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

/**
 *
 * @author Nataniel
 */
public class IPTVPlayer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setOnCloseRequest((a) -> {
            if (FXMLDocumentController.isvlcopen) {
                Alert ale = new Alert(Alert.AlertType.WARNING);
                ale.setTitle("Player ainda aberto");
                ale.setContentText("Feche o player para sair da aplicação");
                ale.show();
                a.consume();
            } else {
                Platform.exit();
            }
        });

        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Propriedades propriedades = new Propriedades();
        boolean found = new NativeDiscovery().discover();
        launch(args);
    }

    public static void error(Exception err, Class clse) {
        Platform.runLater(() -> {
            try {
                Alert ale = new Alert(Alert.AlertType.ERROR);
                ale.setContentText(err.toString());
                ale.showAndWait();
                err.printStackTrace();
                System.exit(3);
            } catch (Exception er) {
                er.printStackTrace();
                System.out.println(clse.getName());
                err.printStackTrace();
            }
        });
    }
}
