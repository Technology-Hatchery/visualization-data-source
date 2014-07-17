package com.mobinsight.user;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.cmd.*;
import com.googlecode.objectify.impl.*;
//import com.googlecode.objectify.impl.cmd.*;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class BaseDAO<T> {
	private final Logger LOG = Logger.getLogger(BaseDAO.class.getSimpleName()); 
	private final Class clazz;
	
	public BaseDAO(Class clazz) {
		this.clazz = clazz;
	}

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }

	public T get(String id) {
		if (id == null || "".equals(id)) {
			return null;
		}
		
		T user = (T) ofy().load().key(Key.create(clazz, id)).now();
		return (user == null) ? newInstance(id) : user;
	}

    @SuppressWarnings({"unchecked"})
    private T newInstance(String id) {
        Constructor[] ctors = clazz.getDeclaredConstructors();
        Constructor ctor = null;
        for (int i = 0; i < ctors.length; i++) {
            ctor = ctors[i];
            if (ctor.getParameterTypes().length == 1 && ctor.getParameterTypes()[0].equals(String.class)) {
                try {
                    return (T)ctor.newInstance(id);
                } catch (Exception e) {
                    LOG.warning("Cannot construct instance of " + clazz.getName() + " with arg " + id + ": " + e.getMessage());
                    return null;
                }
            }
        }
        LOG.warning("Cannot construct instance of " + clazz.getName() + " as there are no single-arg constructors");
        return null;
    }
	
    public void put(T object) {
        ofy().save().entity(object).now();
    }

    @SuppressWarnings({"unchecked"})
    public void delete(String id) {
        ofy().delete().key(Key.create(clazz, id));
    }
	
}
