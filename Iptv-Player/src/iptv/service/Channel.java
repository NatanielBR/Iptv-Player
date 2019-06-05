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

package iptv.service;

import com.nataniel.ExtInfo;

import java.util.concurrent.atomic.AtomicBoolean;

public class Channel {
    private AtomicBoolean alive;
    private boolean changed;
    private ExtInfo canal;

    public Channel(ExtInfo canal) {
        alive = new AtomicBoolean();
        this.canal = canal;
        changed = false;
    }

    public boolean isAlive() {
        return alive.get();
    }

    public void setAlive(boolean alive) {
        this.alive.set(alive);
        changed = true;
    }

    public boolean isChanged() {
        return changed;
    }

    public ExtInfo getChannel() {
        return canal;
    }

    @Override
    public String toString() {
        return canal.getCanalNome();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Channel) && o.hashCode() == this.getChannel().hashCode();
    }
}
