package com.mobinsight.server;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.google.appengine.api.datastore.Key;

/**
 * An answer given by a user for a question.
 */
@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    //private Key id;

    // ID of the user who gave this answer.
    @ManyToOne
    public Key mUser;

    //The values associated with the response
    public int mAnswerIndex;  // valid if answer type was CHOICE
    public String mAnswerText;  // valid if answer type was TEXT
    public float mAnswerRangeValue;  // valid if answer type was RANGE
    public long mTimeTakenMs;
    public double latitude;
    public double longitude;
}
