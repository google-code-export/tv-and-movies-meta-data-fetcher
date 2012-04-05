/*
 *  Copyright (C) 2008-2011  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media.store.mp4.itunes;

public class SQLiteToHSQLTranslater {

	public static String translateSQL(String sql) {
		sql = sql.replaceAll("TEXT", "varchar(255)");
		if (sql.startsWith("create unique index")) {
			sql = sql.replaceAll("unique index if not exists 'loction_index'", "unique index location_index");
		}
		return sql;
	}
}
