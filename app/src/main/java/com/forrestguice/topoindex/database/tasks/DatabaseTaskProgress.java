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
 * DatabaseTaskProgress
 * @see DatabaseTask
 */
public class DatabaseTaskProgress
{
    public DatabaseTaskProgress(String msg, int itemNum, int numItems )
    {
        this.message = msg;
        this.count[0] = itemNum;
        this.count[1] = numItems;
    }

    protected String message;
    public String getMessage() {
        return message;
    }
    public void setMessage(String value)
    {
        this.message = value;
    }

    protected int[] count = new int[] {0, 0};
    public int itemNumber() {
        return count[0];
    }
    public int numItems() {
        return count[1];
    }

    public String toString()
    {
        return message + " (" + this.count[0] + "/" + this.count[1] + ")";
    }
}
