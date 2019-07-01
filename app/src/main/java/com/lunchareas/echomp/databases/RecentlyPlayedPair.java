/*
Echo Music Player
Copyright (C) 2019 David Zhang

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lunchareas.echomp.databases;


public class RecentlyPlayedPair {

    private long id;
    private int played;

    public RecentlyPlayedPair(long id, int played) {
        this.id = id;
        this.played = played;
    }

    public long getKey() {
        return this.id;
    }

    public int getVal() {
        return this.played;
    }

    @Override
    public boolean equals(Object other) {
        RecentlyPlayedPair otherPair = (RecentlyPlayedPair) other;
        return (otherPair.getKey() == this.getKey());
    }
}