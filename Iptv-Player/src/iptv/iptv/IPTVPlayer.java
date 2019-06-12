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

import iptv.fxml.PrincipalController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

/**
 * @author Nataniel
 */
public class IPTVPlayer extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();
        PropertyConfigurator.configure("log4j.properties");
        new Propriedades();
        new NativeDiscovery().discover();
        launch(args);
    }

    public static void error(Exception err, Class clse) {
        Platform.runLater(() -> {
            try {
                Alert ale = new Alert(Alert.AlertType.ERROR);
                ale.setContentText(err.toString());
                ale.showAndWait();
                Logger.getLogger(clse).error(err.getMessage(), err);
            } catch (Exception er) {
                Logger.getLogger(clse).error("Erro ao exibir a mensagem.");
                Logger.getLogger(clse).error(err.getMessage(), er);
                Logger.getLogger(clse).error(err.getMessage(), err);
            }
        });
    }

    public static void error(Throwable err, Class clse) {
        Platform.runLater(() -> {
            try {
                Alert ale = new Alert(Alert.AlertType.ERROR);
                ale.setContentText(err.toString());
                ale.showAndWait();
                Logger.getLogger(clse).error(err.getMessage(), err);
            } catch (Exception er) {
                Logger.getLogger(clse).error("Erro ao exibir a mensagem.");
                Logger.getLogger(clse).error(err.getMessage(), er);
                Logger.getLogger(clse).error(err.getMessage(), err);
            }
        });
    }

    public static void error(String err, Class clse) {
        Platform.runLater(() -> {
            try {
                Alert ale = new Alert(Alert.AlertType.ERROR);
                ale.setContentText(err);
                ale.showAndWait();
                Logger.getLogger(clse).error(err);
            } catch (Exception er) {
                Logger.getLogger(clse).error("Erro ao exibir a mensagem.");
                Logger.getLogger(clse).error(er.getMessage(), er);
                Logger.getLogger(clse).error(err);
            }
        });
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("fxml/Principal.fxml"));
        Scene scene = new Scene(root);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle("Iptv Player 2.3");
        stage.setOnCloseRequest((a) -> {
            if (PrincipalController.player.get()) {
                Alert ale = new Alert(Alert.AlertType.WARNING);
                ale.setTitle("Player ainda aberto");
                ale.setContentText("Feche o player para sair da aplicação");
                ale.show();
                a.consume();
            } else {
                PrincipalController.closeAllTab();
                Platform.exit();
            }
        });

        stage.show();
    }
}
