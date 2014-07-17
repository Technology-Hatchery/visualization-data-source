package com.mobinsight.server;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.google.appengine.api.datastore.Key;

/**
 * A precondition that has to be satisfied for the parent question to be served for a user.
 */
@Entity
public class Precondition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    //private Key id;

    // Question which the user should have answered earlier.
    @ManyToOne
    public Key mQuestion;
    @ManyToOne
    public String questionSerialNumber;
    // Answer expected for the above question to satisfy this condition.
    public int mAnswerIndex;  // valid if answer type was CHOICE
    public String mAnswerText;  // valid if answer type was TEXT
    public float mAnswerRangeLow;  // valid if answer type was RANGE
    public float mAnswerRangeHigh;

    Precondition() {
        mAnswerIndex = -1;
    }

	//public Key getId() {
	public int getId() {
			return id;
	}

	public String getSerialNumber() {
		return questionSerialNumber;
	}

	public Key getReferredQuestion() {
		return mQuestion;
	}
	
	public String toString() {
		return " Precondition q id: " + mQuestion + " serialNumber: " + questionSerialNumber;
	}
}
