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

package iptv;

import iptv.fxml.FXMLDocumentController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

/**
 * @author Nataniel
 */
public class IPTVPlayer extends Application {

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
            } catch (Exception er) {
                er.printStackTrace();
                System.out.println(clse.getName());
                err.printStackTrace();
            }
        });
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("fxml/FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setOnCloseRequest((a) -> {
            if (FXMLDocumentController.player.get()) {
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
}
