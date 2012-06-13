package org.stanwood.media.store.db;

import java.io.File;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;

/**
 * Used to store and retrieve {@link File} class within a hibernate database
 */
public class FileType implements UserType {

	/** {@inheritDoc} */
	@Override
	public Object assemble(Serializable value, Object owner) throws HibernateException {
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public Object deepCopy(Object o) throws HibernateException {
		if (o == null) {
			return null;
		}
		return new File(((File)o).getAbsolutePath());
	}

	/** {@inheritDoc} */
	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable)value;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object arg0, Object arg1) throws HibernateException {
		if (arg0 == arg1) {
			return true;
		}
		if (arg0 == null) {
			if (arg1!= null) {
				return false;
			}
		} else if (!arg0.equals(arg1)) {
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode(Object arg0) throws HibernateException {
		return arg0.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isMutable() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object arg3) throws HibernateException,
			SQLException {
		String val =  StringType.INSTANCE.nullSafeGet(rs, names[0]);
		File file = new File(val);
		return file;
	}

	/** {@inheritDoc} */
	@Override
	public void nullSafeSet(PreparedStatement stmt, Object o, int i) throws HibernateException, SQLException {
		File value = (File)o;
		if (value!=null) {
			stmt.setString(i, value.getAbsolutePath());
		}
		else {
			stmt.setNull(i, Types.VARCHAR);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("rawtypes")
	@Override
	public Class returnedClass() {
		return String.class;
	}

	/** {@inheritDoc} */
	@Override
	public int[] sqlTypes() {
		return new int[] { Types.VARCHAR };
	}

}
