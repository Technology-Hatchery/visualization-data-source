package com.mobinsight.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.*;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@Entity
public class Question {
    public static final int ANSWER_TYPE_CHOICE = 0;
    public static final int ANSWER_TYPE_RANGE = 1;
    public static final int ANSWER_TYPE_TEXT = 2;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //Specifies the properties of the question
    //TODO-Key
    private int mId;
    //private Key mId;
    private String serialNumber;
    private String mText;
    private Blob mImage;  // TODO: Blobstore is another possibility instead of storing here.
    private int mAnswerType;

    // Valid if mAnswerType==ANSWER_TYPE_CHOICE
    @OneToMany(cascade = CascadeType.PERSIST)
    private List<String> mAnswerChoices;

    // Valid if mAnswerType==ANSWER_TYPE_RANGE
    @OneToOne(cascade = CascadeType.ALL)
    private AnswerRange mAnswerRange;

    @OneToOne(cascade = CascadeType.ALL)
    private Precondition mPrecondition;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ArrayList<Answer> mAnswers;

    // List of user ids matching each Answer item in the above list. Appengine doesn't support query via
    // embedded object properties, so we pull out mUser from the Answer and add to this list whenever an
    // Answer gets added to the above list. Then we can do a quick search with this field for analytics.
    private ArrayList<String> mUsersAnswered = new ArrayList<String>();
    private static final Logger log = Logger.getLogger(Question.class.getName());
    public String getId() { return Integer.toString(mId); }
    //public Key getId() { return mId; }
    public String getIdString() { return Integer.toString(mId); }
    //public String getIdString() { return KeyFactory.keyToString(mId); }
    public String getText() { return mText; }
    public Blob getImage() { return mImage; }
    public void setImage(Blob image) { this.mImage = image; }
    public int getAnswerType() { return mAnswerType; }
    public List<String> getAnswerChoices() { return mAnswerChoices; }
    public AnswerRange getAnswerRange() { return mAnswerRange; }
    public Precondition getPrecondition() { return mPrecondition; }
    public List<Answer> getAnswers() { return mAnswers; }
    public String getSerialNumber() { return serialNumber; }

    public void readFromJson(EntityManager em, JsonReader reader) throws IOException {
        this.mAnswerType = -1;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id")) {
                //TODO-Key
                //this.mId = Integer.parseInt(reader.nextString());
                this.mId = reader.nextInt();
                //this.mId = KeyFactory.stringToKey(reader.nextString());
                // Validate that it is pointing to an object that already exists.
                try {
                    Question existingItem = em.find(Question.class, this.mId);
                    if (existingItem == null) {
                        //TODO-Key
                        this.mId = 0;
                        //this.mId = null;
                    } else {
                        // JSON doesn't have the image data, so when parsing a question that is an update
                        // to an existing question copy over the image data so we don't lose it when overwriting.
                        this.mImage = existingItem.mImage;
                    }
                } catch (IllegalArgumentException e) {
                    //TODO-Key
                    this.mId = 0;
                    //this.mId = null;
                }
            } else if (name.equals("serialNumber")) {
                this.serialNumber = reader.nextString();
            } else if (name.equals("text")) {
                this.mText = reader.nextString();
            } else if (name.equals("answerType")) {
                this.mAnswerType = reader.nextInt();
            } else if (name.equals("image")) {
                // Ignore this value as this is likely the image url sent in the json response earlier.
                // Image uploads are handled separately.
                reader.nextString();
            } else if (name.equals("answerChoices")) {
                this.mAnswerChoices = new ArrayList<String>();
                reader.beginArray();
                while (reader.hasNext()) {
                    this.mAnswerChoices.add(reader.nextString());
                }
                reader.endArray();
            } else if (name.equals("answerRange")) {
                this.mAnswerRange = new AnswerRange();
                reader.beginObject();
                while (reader.hasNext()) {
                    name = reader.nextName();
                    if (name.equals("low")) {
                        this.mAnswerRange.mLow = (float) reader.nextDouble();
                    } else if (name.equals("high")) {
                        this.mAnswerRange.mHigh = (float) reader.nextDouble();
                    } else if (name.equals("step")) {
                        this.mAnswerRange.mStep = (float) reader.nextDouble();
                    } else {
                        throw new IOException("Unknown field in answerRange " + name);
                    }
                }
                reader.endObject();
            } else if (name.equals("precondition")) {
                this.mPrecondition = new Precondition();
                reader.beginObject();
                while (reader.hasNext()) {
                    name = reader.nextName();
                    if (name.equals("question")) {
                        getReferredQuestion(reader, this);
                    } else if (name.equals("questionSerialNumber")) {
                        getReferredSerialNumber(reader, this);
                    } else if (name.equals("answerIndex")) {
                        this.mPrecondition.mAnswerIndex = reader.nextInt();
                    } else if (name.equals("answerText")) {
                        this.mPrecondition.mAnswerText = reader.nextString();
                    } else if (name.equals("answerRangeLow")) {
                        this.mPrecondition.mAnswerRangeLow = (float) reader.nextDouble();
                    } else if (name.equals("answerRangeHigh")) {
                        this.mPrecondition.mAnswerRangeHigh = (float) reader.nextDouble();
                    } else {
                        throw new IOException("Unknown field in precondition " + name);
                    }
                }
                reader.endObject();
            } else {
                throw new IOException("Unknown field in question: " + name);
            }
        }
        reader.endObject();
        if (this.mText == null || this.mAnswerType == -1) {
            throw new IOException("Mandatory fields missing in question");
        }
//        if (this.mPrecondition != null) {
//            Question refQuestion = em.find(Question.class, this.mPrecondition.mQuestion);
//            if (refQuestion == null) {
//            	// throw new IOException("Precondition refers to non-existent question");
//            	// The question probably refers to an internal question
//            	// and will need be found by the survey object
//            }
//
//            if (refQuestion.mAnswerType == ANSWER_TYPE_CHOICE && this.mPrecondition.mAnswerIndex == -1) {
//                throw new IOException("Precondition should specify answerIndex");
//            } else if (refQuestion.mAnswerType == ANSWER_TYPE_TEXT && this.mPrecondition.mAnswerText == null) {
//                throw new IOException("Precondition should specify answerText");
//            }
//        }
    }
	private static void getReferredSerialNumber(JsonReader reader, Question item)
			throws IOException {
		item.mPrecondition.questionSerialNumber = reader.nextString();
	}

	private static void getReferredQuestion(JsonReader reader, Question item)
			throws IOException {
		String id = reader.nextString();
		item.mPrecondition.mQuestion = KeyFactory.stringToKey(id);
	}

    public void writeToJson(JsonWriter writer, Question preConditionQuestion) throws IOException {
        writer.beginObject();
        String idStr = Integer.toString(mId);
        //String idStr = KeyFactory.keyToString(mId);
        writer.name("id").value(idStr);
        writer.name("serialNumber").value(serialNumber);
        writer.name("text").value(mText);
        writer.name("answerType").value(mAnswerType);
        if (mImage != null) {
            writer.name("image").value(Utils.getHostUrl() + "/api/image?question=" + idStr);
        }
        if (mAnswerType == ANSWER_TYPE_CHOICE && mAnswerChoices != null) {
            writer.name("answerChoices");
            writer.beginArray();
            for (String str : mAnswerChoices) {
                writer.value(str);
            }
            writer.endArray();
        }
        if (mAnswerType == ANSWER_TYPE_RANGE && mAnswerRange != null) {
            writer.name("answerRange");
            writer.beginObject();
            writer.name("low").value(mAnswerRange.mLow);
            writer.name("high").value(mAnswerRange.mHigh);
            writer.name("step").value(mAnswerRange.mStep);
            writer.endObject();
        }
        if (mPrecondition != null) {
            writer.name("precondition");
            writer.beginObject();
            if (mPrecondition.mQuestion != null) {
            	writer.name("question").value(KeyFactory.keyToString(mPrecondition.mQuestion));
            } else {
            	log.severe("mQuestion is null");
            	//writer.name("question").value(KeyFactory.keyToString(preConditionQuestion.getId()));
            	log.severe("Giving it the id of serial number: " + preConditionQuestion.getSerialNumber());
            }
            writer.name("questionSerialNumber").value(mPrecondition.questionSerialNumber);
            writer.name("answerIndex").value(mPrecondition.mAnswerIndex);
            writer.name("answerText").value(mPrecondition.mAnswerText);
            writer.name("answerRangeLow").value(mPrecondition.mAnswerRangeLow);
            writer.name("answerRangeHigh").value(mPrecondition.mAnswerRangeHigh);
            writer.endObject();
        }
        writer.endObject();
    }

    public void writeToJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        //TODO-Key
        String idStr = Integer.toString(mId);
        //String idStr = KeyFactory.keyToString(mId);
        writer.name("id").value(idStr);
        
        if(serialNumber == null){
        	writer.name("serialNumber").value(0);
        } else {
        	writer.name("serialNumber").value(serialNumber);
        }
        
        writer.name("text").value(mText);
        writer.name("answerType").value(mAnswerType);
        if (mImage != null) {
            writer.name("image").value(Utils.getHostUrl() + "/api/image?question=" + idStr);
        }
        if (mAnswerType == ANSWER_TYPE_CHOICE && mAnswerChoices != null) {
            writer.name("answerChoices");
            writer.beginArray();
            for (String str : mAnswerChoices) {
                writer.value(str);
            }
            writer.endArray();
        }
        if (mAnswerType == ANSWER_TYPE_RANGE && mAnswerRange != null) {
            writer.name("answerRange");
            writer.beginObject();
            writer.name("low").value(mAnswerRange.mLow);
            writer.name("high").value(mAnswerRange.mHigh);
            writer.name("step").value(mAnswerRange.mStep);
            writer.endObject();
        }
        if (mPrecondition != null) {
            writer.name("precondition");
            writer.beginObject();
            if (mPrecondition.mQuestion != null) {
            	writer.name("question").value(KeyFactory.keyToString(mPrecondition.mQuestion));
            } else {
            	log.severe("mQuestion is null");
            	//writer.name("question").value(mPrecondition.questionSerialNumber);
            }
            writer.name("questionSerialNumber").value(mPrecondition.questionSerialNumber);
            writer.name("answerIndex").value(mPrecondition.mAnswerIndex);
            writer.name("answerText").value(mPrecondition.mAnswerText);
            writer.name("answerRangeLow").value(mPrecondition.mAnswerRangeLow);
            writer.name("answerRangeHigh").value(mPrecondition.mAnswerRangeHigh);
            writer.endObject();
        }
        writer.endObject();
    }

	/**
     * Writes out json for this question + answer for the given user.
     */
    public void writeAnalyticsToJson(User user, JsonWriter writer) throws IOException {
        int answerIndex = mUsersAnswered.indexOf(user.getIdString());
        if (answerIndex == -1) {
            throw new IOException("Question was not answered by this user");
        }
        writer.beginObject();
        //TODO-Key
        String idStr = Integer.toString(mId);
        //String idStr = KeyFactory.keyToString(mId);
        writer.name("id").value(idStr);
        writer.name("text").value(mText);
        if (mImage != null) {
            writer.name("image").value(Utils.getHostUrl() + "/api/image?question=" + idStr);
        }
        writer.name("answerType").value(mAnswerType);
        writer.name("answer");
        Answer answer = mAnswers.get(answerIndex);
        switch (mAnswerType) {
        case ANSWER_TYPE_TEXT:
            writer.value(answer.mAnswerText);
            break;
        case ANSWER_TYPE_CHOICE:
            writer.value(mAnswerChoices.get(answer.mAnswerIndex));
            break;
        case ANSWER_TYPE_RANGE:
            writer.value(answer.mAnswerRangeValue);
            break;
        }
        writer.name("timeTaken").value(answer.mTimeTakenMs);
        writer.endObject();
    }

    /**
     * Parses the given json as an answer for the given user and saves it as part of this question.
     * @param lng 
     * @param lat 
     * @return true if the answer was matching this question, false if not.
     */
    public boolean addAnswerFromJson(EntityManager em, JsonReader reader, User user, double lat, double lng) throws IOException {
        Answer item = new Answer();
        //item.mUser = user.getId();
        reader.beginObject();
        Key question = null;
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("question")) {
                question = KeyFactory.stringToKey(reader.nextString());
                if (!question.equals(mId)) {
                    // Answer does not belong to this question. This happens if the client had sent the answer
                    // earlier and we received it, stored it and sent a response which never reached the client.
                    // So return false to the caller indicating they can ignore this json and try with the next
                    // object.
                    return false;
                }
            } else if (name.equals("answerIndex")) {
                item.mAnswerIndex = reader.nextInt();
            } else if (name.equals("answerText")) {
                item.mAnswerText = reader.nextString();
            } else if (name.equals("answerRangeValue")) {
                item.mAnswerRangeValue = (float) reader.nextDouble();
            } else if (name.equals("timeTaken")) {
                item.mTimeTakenMs = reader.nextLong();
            } else {
                throw new IOException("Invalid field in answer " + name);
            }
        }
        // Record the latitude and longitude.
        item.latitude = lat;
        item.longitude = lng;
        
        reader.endObject();
        if (question == null) {
            throw new IOException("Please give a valid key for the question attribute");
        }
        if (item.mTimeTakenMs == 0) {
            throw new IOException("Answer missing mandatory fields");
        }

        //Create new array if there were no previous answers
        if (mAnswers == null) {
            mAnswers = new ArrayList<Answer>();
            mUsersAnswered = new ArrayList<String>();
        }
        mAnswers.add(item);

        // Add the user id to our index array so we can search quickly all questions answered by
        // this user.
        mUsersAnswered.add(KeyFactory.keyToString(item.mUser));

        em.persist(this);
        return true;
    }

    /**
     * Returns true if this question should be skipped for the given user. A question gets skipped if it has a
     * precondition on an earlier question and the given user has answered that in a way that doesn't match
     * the precondition.
     */
    public boolean shouldSkipDueToPrecondition(EntityManager em, User user) {
        if (mPrecondition == null || mPrecondition.mQuestion == null) {
        	log.severe("In shouldSkipDueToPrecondition - Don't skip.");
            return false;
        }
        Question refQuestion = em.find(Question.class, mPrecondition.mQuestion);
        log.info("In shouldSkipDueToPrecondition refQuestion: " + refQuestion);
        if (refQuestion == null) {
            return false;
        }
        // TODO: This should be optimised so instead of looping through answers from all users it should
        // query just for the answer of this user.
        for (Answer item : refQuestion.mAnswers) {
            if (!item.mUser.equals(user.getId())) {
                continue;
            }

            if (refQuestion.mAnswerType == ANSWER_TYPE_CHOICE) {
                return item.mAnswerIndex != mPrecondition.mAnswerIndex;
            } else if (refQuestion.mAnswerType == ANSWER_TYPE_RANGE) {
                return item.mAnswerRangeValue < mPrecondition.mAnswerRangeLow ||
                       item.mAnswerRangeValue > mPrecondition.mAnswerRangeHigh;
            } else if (refQuestion.mAnswerType == ANSWER_TYPE_TEXT) {
                return !item.mAnswerText.equals(mPrecondition.mAnswerText);
            }
        }

        return false;
    }
	public void setPreconditionKey(Key mQuestion) {
		this.mPrecondition.mQuestion = mQuestion;
	}
	
	public String toString() {
		String q = "Id: " + mId 
				+ " serialNumber: " + serialNumber
				+ " text: " + mText;
		String p = " No Precondition";
		if (mPrecondition != null) {
			p = " precondition: " + mPrecondition.toString();
		}
		return q + p;
			
	}

}
