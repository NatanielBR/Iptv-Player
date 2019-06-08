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

import com.nataniel.builder.ExtInfoBuilder;
import com.nataniel.inter.ExtInfo;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ExtInfoEditor implements Initializable {
    private ExtInfo extInfo;
    @FXML
    private TextField grupoDoCanalField;
    @FXML
    private TextField nomeDoCanalField;
    @FXML
    private TextField logoDoCanalField;
    @FXML
    private TextField idDoCanalField;

    public ExtInfoEditor(ExtInfo extInfo) {
        this.extInfo = extInfo;
    }

    public ExtInfo getExtInfo() {
        ExtInfoBuilder bu = new ExtInfoBuilder();
        bu.setGrupo(grupoDoCanalField.getText());
        bu.setCanalNome(nomeDoCanalField.getText());
        bu.setLogoURL(logoDoCanalField.getText());
        bu.setId(idDoCanalField.getText());
        bu.setCanalURL(extInfo.getCanalURL());
        return bu.builder();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        grupoDoCanalField.setText(extInfo.getGrupo());
        nomeDoCanalField.setText(extInfo.getCanalNome());
        logoDoCanalField.setText(extInfo.getLogoURL());
        idDoCanalField.setText(extInfo.getId());
    }
}
