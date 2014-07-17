package com.mobinsight.server;

import java.io.IOException;
import java.util.Date;

import javax.persistence.*;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.stream.JsonWriter;
import com.googlecode.objectify.annotation.Index;
import com.mobinsight.user.GaeUser;

@Entity
public class Image {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int mId;
    //private Key mId;
    private Blob image;

    @Basic @Index
    private GaeUser userId;
    private String userName;
    @Index
    private Date uploadDate;
    private String description;

    public String getIdString() {
    return Integer.toString(mId);
    //return KeyFactory.keyToString(mId);
    }

	public void writeToJsonShort(JsonWriter writer) {
        try {
			writer.beginObject();
	        writer.name("id").value(getIdString());
	        writer.name("description").value(description);
	        writer.name("uploadDate").value(uploadDate.toString());
	        writer.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setUserImage(Blob imageBlob, GaeUser user) {
		this.userId = user;
		this.userName = user.getName();
		this.image = imageBlob;
		this.uploadDate = new Date();
	}

	public Object getImage() {
		return image;
	}
}
