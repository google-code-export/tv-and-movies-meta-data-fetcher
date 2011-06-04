package org.stanwood.media.jna;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jna.FromNativeContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

@SuppressWarnings("all")
public class EnumConverter implements TypeConverter {

	private final static Log log = LogFactory.getLog(EnumConverter.class);


    @Override
	public Object fromNative(Object input, FromNativeContext context) {
        Integer i = (Integer) input;
        Class targetClass = context.getTargetType();
        if (!JnaEnum.class.isAssignableFrom(targetClass)) {
            return null;
        }
        Object[] enums = targetClass.getEnumConstants();
        if (enums.length == 0) {
            log.error("Could not convert desired enum type (), no valid values are defined: "+targetClass.getName());
            return null;
        }
        // In order to avoid nasty reflective junk and to avoid needing
        // to know about every subclass of JnaEnum, we retrieve the first
        // element of the enum and make IT do the conversion for us.

        JnaEnum instance = (JnaEnum) enums[0];
        return instance.getForValue(i);

    }

    @Override
	public Object toNative(Object input, ToNativeContext context) {
        JnaEnum j = (JnaEnum) input;
        return Integer.valueOf(j.getIntValue());
    }

    @Override
	public Class nativeType() {
        return Integer.class;
    }
}
