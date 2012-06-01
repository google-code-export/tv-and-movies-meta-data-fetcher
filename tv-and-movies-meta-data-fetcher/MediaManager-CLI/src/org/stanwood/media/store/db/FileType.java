package org.stanwood.media.store.db;

import java.io.File;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;

public class FileType implements UserType {

	@Override
	public Object assemble(Serializable value, Object owner) throws HibernateException {
		return value;
	}

	@Override
	public Object deepCopy(Object o) throws HibernateException {
		if (o == null) {
			return null;
		}
		return new File(((File)o).getAbsolutePath());
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable)value;
	}

	@Override
	public boolean equals(Object arg0, Object arg1) throws HibernateException {
		return arg0.equals(arg1);
	}

	@Override
	public int hashCode(Object arg0) throws HibernateException {
		return arg0.hashCode();
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names,
			SessionImplementor session, Object arg3) throws HibernateException,
			SQLException {
		String val =  StringType.INSTANCE.nullSafeGet(rs, names[0],session);
		File file = new File(val);
		return file;
	}

	@Override
	public void nullSafeSet(PreparedStatement stmt, Object o, int i,
			SessionImplementor arg3) throws HibernateException, SQLException {
		File value = (File)o;
		if (value!=null) {
			stmt.setString(i, value.getAbsolutePath());
		}
		else {
			stmt.setNull(i, Types.VARCHAR);
		}
	}

	@Override
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	@Override
	public Class returnedClass() {
		return String.class;
	}

	@Override
	public int[] sqlTypes() {
		return new int[] { Types.VARCHAR };
	}

}
