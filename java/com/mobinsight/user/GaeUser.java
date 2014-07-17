package com.mobinsight.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.annotations.Persistent;
import javax.persistence.CascadeType;
import javax.persistence.OneToMany;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;

import com.google.appengine.api.datastore.Blob;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Cache
@Entity
public class GaeUser {
	static final Logger LOG = Logger.getLogger(GaeUser.class.getSimpleName());

    public static final int HASH_ITERATIONS = 1;
    public static final String HASH_ALGORITHM = Sha256Hash.ALGORITHM_NAME;
	
	@Id
	private String name;
	private String passwordHash;
    private byte[] salt;

    private Set<String> roles;
    private Set<String> permissions;

    @Index
    private Date dateRegistered;
    private boolean isSuspended;
    
    @OneToMany(cascade = CascadeType.PERSIST)
    private ArrayList<Blob> images = new ArrayList<Blob>();
    /** For objectify to create instances on retrieval */
    private GaeUser() {
        this.roles = new HashSet<String>();
        this.permissions = new HashSet<String>();
    }

    GaeUser(String name) {
        this(name, null, new HashSet<String>(), new HashSet<String>());
    }

    
    GaeUser(String name, String password) {
        this(name, password, new HashSet<String>(), new HashSet<String>());
    }

    public GaeUser(String name, Set<String> roles, Set<String> permissions) {
        this(name, null, roles, permissions);
    }
    
    public GaeUser(String name, String password, Set<String> roles, Set<String> permissions) {
        this(name, password, roles, permissions, false);
    }

    GaeUser(String name, String password, Set<String> roles, Set<String> permissions, boolean isRegistered) {
        Preconditions.checkNotNull(name, "User name (email) can't be null");
        Preconditions.checkNotNull(roles, "User roles can't be null");
        Preconditions.checkNotNull(permissions, "User permissions can't be null");
        this.name = name;

        this.salt = salt().getBytes();
        this.passwordHash = hash(password, salt);
        this.roles = Collections.unmodifiableSet(roles);
        this.permissions = Collections.unmodifiableSet(permissions);
        this.dateRegistered = isRegistered ? new Date() : null;
        this.isSuspended = false;
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    public void setSuspended(boolean suspended) {
        isSuspended = suspended;
    }

    public void setPassword(String password) {
        Preconditions.checkNotNull(password);
        this.salt = salt().getBytes();
        this.passwordHash = hash(password, salt);
    }

    public Date getDateRegistered() {
        return dateRegistered == null ? null : new Date(dateRegistered.getTime());
    }

    public boolean isRegistered() {
        return getDateRegistered() != null;
    }

    public void register() {
        dateRegistered = new Date();
    }

    public String getName() {
        return name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public byte[] getSalt() {
        return salt;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    private static ByteSource salt() {
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        return rng.nextBytes();
    }

    private static String hash(String password, byte[] salt) {
        return (password == null) ? null : new Sha256Hash(password, new SimpleByteSource(salt), HASH_ITERATIONS).toHex();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GaeUser) {
            GaeUser u = (GaeUser)o;
            return Objects.equal(getName(), u.getName()) &&
                   Objects.equal(getPasswordHash(), u.getPasswordHash());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, passwordHash);
    }

	public void setImage(Blob imageBlob) {
		images.add(imageBlob);
		LOG.severe("Images size: " + images.size());
	}
}
