package com.mobinsight.server;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class AnswerRange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    //private Key id;

    public float mLow;
    public float mHigh;
    public float mStep;
}
