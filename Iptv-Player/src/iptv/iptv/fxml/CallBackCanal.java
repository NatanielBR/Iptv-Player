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

import iptv.service.Channel;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

public class CallBackCanal implements Callback<ListView<Channel>, ListCell<Channel>> {
    @Override
    public ListCell<Channel> call(ListView<Channel> param) {
        return new ListCell<Channel>() {
            @Override
            protected void updateItem(Channel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String uri = String.format("iptv/res/%d.png", item.isChanged() ? (item.isAlive() ? 0 : 2) : 1);
                    setText(item.toString());
                    setGraphic(new ImageView(uri));
                }

            }
        };
    }
}
