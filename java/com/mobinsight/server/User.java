package com.mobinsight.server;

import java.io.IOException;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Query;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.stream.JsonReader;

@Entity
public class User {

    public static final int OS_ANDROID = 1;
    public static final int OS_IPHONE = 2;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int mId;
    //private Key mId;

    private int mOS;
    private String mOSVersion;
    private String mDeviceId;
    private String mEmail;

    private Key mLastSurveyAnswered;  // Last survey answered by the client
    private Key mLastQuestionAnswered;  // Last question in the above survey answered by the client
    private String appIdAsPackageName;

    public String getPackageName() {
    	return appIdAsPackageName;
    }

    public String getIdString() {
        return Integer.toString(mId);
        //return KeyFactory.keyToString(mId);
    }

    public int getId() {
    //public Key getId() {
        return mId;
    }

    public String getEmail() {
        return mEmail;
    }

    public Key getLastSurveyAnswered() {
        return mLastSurveyAnswered;
    }

    public Key getLastQuestionAnswered() {
        return mLastQuestionAnswered;
    }

    public void setLastQuestionAnswered(Key lastQuestionAnswered) {
        mLastQuestionAnswered = lastQuestionAnswered;
    }

    public void setLastSurveyAnswered(Key lastSurveyAnswered) {
        mLastSurveyAnswered = lastSurveyAnswered;
    }

    public static User readFromJson(JsonReader reader) throws IOException {
        User item = new User();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("os")) {
                int value = reader.nextInt();
                if (value != OS_ANDROID && value != OS_IPHONE) {
                    throw new IOException("Invalid value for field 'os'");
                }
                item.mOS = value;
            } else if (name.equals("osVersion")) {
                item.mOSVersion = reader.nextString();
            } else if (name.equals("deviceId")) {
                item.mDeviceId = reader.nextString();
            } else if (name.equals("email")) {
                item.mEmail = reader.nextString();
            } else if (name.equals("packageName")) {
                item.appIdAsPackageName = reader.nextString();
            } else {
                throw new IOException("Unknown field in user " + name);
            }
        }
        reader.endObject();
        if (item.mOS == 0 || item.mDeviceId == null || item.mOSVersion == null) {
            throw new IOException("Mandatory fields missing in user");
        }
        return item;
    }

    public User getUserWithSameOSAndDeviceId(EntityManager em) {
        Query query = em.createQuery("select u from User u where u.mOS = " + mOS +
                                     " and u.mDeviceId = '" + mDeviceId + "'");
        @SuppressWarnings("unchecked")
        List<User> results = (List<User>)query.getResultList();
        return results.size() > 0 ? results.get(0) : null;
    }

    public void copyFrom(User user) {
        mOS = user.mOS;
        mOSVersion = user.mOSVersion;
        mDeviceId = user.mDeviceId;
        mEmail = user.mEmail;
        appIdAsPackageName = user.appIdAsPackageName;
    }

}
