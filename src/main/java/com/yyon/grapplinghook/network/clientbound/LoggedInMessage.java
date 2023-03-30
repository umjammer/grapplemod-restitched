package com.yyon.grapplinghook.network.clientbound;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.config.GrappleConfig;
import com.yyon.grapplinghook.network.NetworkContext;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/*
 * This file is part of GrappleMod.

    GrappleMod is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GrappleMod is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GrappleMod.  If not, see <http://www.gnu.org/licenses/>.
 */

public class LoggedInMessage extends BaseMessageClient {
    GrappleConfig.Config conf;

    public LoggedInMessage(PacketByteBuf buf) {
    	super(buf);
    }
    
    public LoggedInMessage(GrappleConfig.Config serverconf) {
    	this.conf = serverconf;
    }

    public <T> void decodeClass(PacketByteBuf buf, Class<T> theClass, T theObject) {
    	Field[] fields = theClass.getDeclaredFields();
    	Arrays.sort(fields, Comparator.comparing(Field::getName));
    	
    	for (Field field : fields) {
    		Type fieldtype = field.getGenericType();
    		try {
        		if (fieldtype.getTypeName().equals("int")) {
        			field.setInt(theObject, buf.readInt());

        		} else if (fieldtype.getTypeName().equals("double")) {
        			field.setDouble(theObject, buf.readDouble());

        		} else if (fieldtype.getTypeName().equals("boolean")) {
        			field.setBoolean(theObject, buf.readBoolean());

        		} else if (fieldtype.getTypeName().equals("java.lang.String")) {
        			int len = buf.readInt();
        			CharSequence charseq = buf.readCharSequence(len, Charset.defaultCharset());
        			field.set(theObject, charseq.toString());

        		} else if (Object.class.isAssignableFrom(field.getType())) {
        			Class newClass = field.getType();
        			decodeClass(buf, newClass, newClass.cast(field.get(theObject)));
        		} else {
        			GrappleMod.LOGGER.warn("Unknown Type");
        			GrappleMod.LOGGER.warn(fieldtype.getTypeName());
        		}
    		} catch (IllegalAccessException e) {
    			GrappleMod.LOGGER.warn(e);
    		}
    	}
    }

	@Override
    public void decode(PacketByteBuf buf) {
    	Class<GrappleConfig.Config> confclass = GrappleConfig.Config.class;
    	this.conf = new GrappleConfig.Config();
    	
    	decodeClass(buf, confclass, this.conf);
    }

    public <T> void encodeClass(PacketByteBuf buf, Class<T> theClass, T theObject) {
    	Field[] fields = theClass.getDeclaredFields();
    	Arrays.sort(fields, Comparator.comparing(Field::getName));
    	
    	for (Field field : fields) {
    		Type fieldtype = field.getGenericType();
    		try {
        		if (fieldtype.getTypeName().equals("int")) {
        			buf.writeInt(field.getInt(theObject));
        		} else if (fieldtype.getTypeName().equals("double")) {
        			buf.writeDouble(field.getDouble(theObject));
        		} else if (fieldtype.getTypeName().equals("boolean")) {
        			buf.writeBoolean(field.getBoolean(theObject));
        		} else if (fieldtype.getTypeName().equals("java.lang.String")) {
        			String str = (String) field.get(theObject);
        			buf.writeInt(str.length());
        			buf.writeCharSequence(str.subSequence(0, str.length()), Charset.defaultCharset());
        		} else if (Object.class.isAssignableFrom(field.getType())) {
        			Class newClass = field.getType();
        			encodeClass(buf, newClass, newClass.cast(field.get(theObject)));
        		} else {
        			GrappleMod.LOGGER.warn("Unknown Type");
        			GrappleMod.LOGGER.warn(fieldtype.getTypeName());
        		}
    		} catch (IllegalAccessException e) {
    			GrappleMod.LOGGER.warn(e);
    		}
    	}
    }

	@Override
    public void encode(PacketByteBuf buf) {
    	Class<GrappleConfig.Config> confclass = GrappleConfig.Config.class;
    	encodeClass(buf, confclass, this.conf);
    }

	@Override
	public Identifier getChannel() {
		return GrappleMod.id("logged_in");
	}

	@Override
	public void processMessage(NetworkContext ctx) {
    	GrappleConfig.setServerOptions(this.conf);
    }
}
