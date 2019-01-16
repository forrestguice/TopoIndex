/**
    Copyright (C) 2019 Forrest Guice
    This file is part of TopoIndex.

    TopoIndex is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TopoIndex is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TopoIndex.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.topoindex.database.tasks;

/**
 * DatabaseTaskResult
 */
public class DatabaseTaskResult
{
    public DatabaseTaskResult(boolean result, int count, long date)
    {
        this.result = result;
        this.count = count;
        this.date = date;
    }

    private boolean result;
    public boolean getResult()
    {
        return result;
    }

    private int count;
    public int numItems() {
        return count;
    }

    private long date;
    public long getDate()
    {
        return date;
    }
}
