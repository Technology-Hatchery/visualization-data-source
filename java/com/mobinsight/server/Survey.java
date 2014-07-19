package com.mobinsight.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.*;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@Entity
//@NamedEntityGraph(name="allProps",
//        attributeNodes={@NamedAttributeNode("mId"), @NamedAttributeNode("mQuestions")})
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //TODO-Key
    private int mId;
    //private Key mId;

    private Date mCreationTime;  // Used for ordering in queries.
    private String mName;
    private String creator;

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private List<Question> mQuestions;

    public String getId() {
    //public Key getId() {
        //TODO-Key
        return Integer.toString(mId);
    }
    public String getIdString() { return Integer.toString(mId); }
    //public String getIdString() { return KeyFactory.keyToString(mId); }
    public String getName() { return mName; }
    public String getCreator() { return creator; }
    public List<Question> getQuestions() { return mQuestions; }
    private static final Logger log = Logger.getLogger(Survey.class.getName());
    public Survey() {
        mCreationTime = new Date();
    }

    public void readFromJson(EntityManager em, JsonReader reader) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id")) {
                //TODO-Key
                this.mId = reader.nextInt();
                //this.mId = Integer.parseInt(reader.nextString());
                //this.mId = KeyFactory.stringToKey(reader.nextString());
                // Validate that it is pointing to an object that already exists.
                try {
                    Survey oldItem = em.find(Survey.class, this.mId);
                    if (oldItem == null) {
                        //TODO-Key
                        this.mId = 0;
                        //this.mId = null;
                    } else {
                        // Copy over the creation time so we preserve it across edits of the same survey.
                        this.mCreationTime = oldItem.mCreationTime;
                    }
                } catch (IllegalArgumentException e) {
                    this.mId = 0;
                    //this.mId = null;
                }
            } else if (name.equals("name")) {
                this.mName = reader.nextString();
            } else if (name.equals("creator")) {
                this.creator = reader.nextString();    
            } else if (name.equals("questions")) {
                this.mQuestions = new ArrayList<Question>();
                reader.beginArray();
                while (reader.hasNext()) {
                    try {
                    	addQuestionToSurvey(em, reader, this);
                    } catch (IOException e) {
                        throw new IOException("Error in parsing question " + this.mQuestions.size() + ": " +
                                e.getMessage());
                    }
                }
                reader.endArray();
            } else {
                throw new IOException("Unknown field in survey " + name);
            }
        }
        reader.endObject();
        if (this.mName == null || this.mQuestions == null || this.creator == null) {
            throw new IOException("Mandatory fields missing in survey"
            		+ " Name:" + mName
            		+ " Creator:" + creator
            		+ " Questions:" + mQuestions);
        }
    }
	private void addQuestionToSurvey(EntityManager em,
			JsonReader reader, Survey item) throws IOException {
		Question question = new Question();
		question.readFromJson(em, reader);
		Precondition precondition = question.getPrecondition();
		if (precondition != null) {
			log.info("precondition is not null");
			if (precondition.getReferredQuestion() == null) {
				log.severe("precondition.getReferredQuestion() == null");
				Question referredQuestion = findQuestionBySerialNumber(precondition.getSerialNumber(), item);
					if (referredQuestion != null) {
						log.info("referredQuestion != null");
						//precondition.mQuestion = referredQuestion.getId();
                        log.severe("question id referred to" + referredQuestion.getId());
						question.setPreconditionKey(precondition.mQuestion);
					} else {
						log.severe("referredQuestion == null");
						throw new IOException("Precondition could not be attached to a Question");
					}
			}
		} else {
			log.info("precondition is null");
		}
		item.mQuestions.add(question);
		for (Question q : item.mQuestions) {
			log.info("Printing q");
			log.info(q.toString());
		}
	}

    private Question findQuestionBySerialNumber(String serialNumber, Survey item) {
    	for (Question q : mQuestions) {
    		log.severe("Looking for " + serialNumber + " This is " + q.getSerialNumber());
    		if (q.getSerialNumber().equals(serialNumber)) {
    			log.severe("Returning Q! " + q.toString());
    			return q;
    		}
    	}
		return null;
	}

	public void writeToJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("id").value(getIdString());
        writer.name("name").value(mName);
        if(creator != null){
        	writer.name("creator").value(creator);
        } else {
        	writer.name("creator").value("unknown");
        }
        	
        if (mQuestions != null) {
            writer.name("questions");
            writer.beginArray();
            for (Question item : mQuestions) {
            	log.severe("item.getPrecondition() " + item.getPrecondition());
            	if (item.getPrecondition() != null && item.getPrecondition().mQuestion == null) {
            		Question question = findQuestionBySerialNumber(item.getPrecondition().questionSerialNumber, this);
            		item.writeToJson(writer, question);
            	} else {
            		item.writeToJson(writer);
            	}
                
            }
            writer.endArray();
        }
        writer.endObject();
    }

	public class WriteToJsonQuestionsAfterReturn {
        int questionsWritten = 0;
        Key lastQuestionWritten = null;
    }

    /**
     * Writes only those questions that come after the given question/key.
     * @param maxQuestions max number of questions from this survey to write.
     * @param afterQuestion Key of the question up to which we should skip, inclusive of this question.
     *   pass null if it should start from the first question.
     * @return the number of questions written out & the last question's key
     * @throws IOException
     */
    public WriteToJsonQuestionsAfterReturn writeToJsonQuestionsAfter(EntityManager em,
                                                                     User user,
                                                                     JsonWriter writer,
                                                                     int maxQuestions,
                                                                     Key afterQuestion) throws IOException {
        WriteToJsonQuestionsAfterReturn ret = new WriteToJsonQuestionsAfterReturn();

        if (mQuestions == null) {
            return ret;
        }

        int index = 0;
        if (afterQuestion != null) {
            /*while (index < mQuestions.size() && !mQuestions.get(index).getId().equals(afterQuestion))
                ++index;
            ++index;*/
        }
        if (index >= mQuestions.size())  // Not found or found as the last item, so nothing more to write.
            return ret;
        int maxIndex = Math.min(mQuestions.size(), index + maxQuestions);
        ret.questionsWritten = 0;

        writer.beginObject();
        writer.name("id").value(getIdString());
        writer.name("name").value(mName);
        writer.name("length").value(mQuestions.size());
        writer.name("questions");
        writer.beginArray();
        int questionStart = maxIndex;
        for (; index < maxIndex; ++index) {
            Question question = mQuestions.get(index);
            if (question.shouldSkipDueToPrecondition(em, user)) {
                continue;
            }
            if (question.getPrecondition() != null) {
            	if (question.getPrecondition().mQuestion == null) {
            		for (Question q : mQuestions) {
            			if (q.getSerialNumber().equals(question.getPrecondition().questionSerialNumber)) {
            				//question.getPrecondition().mQuestion = q.getId();
            			}
            		}
            	}
            }
            question.writeToJson(writer);
            //ret.lastQuestionWritten = question.getId();
            ret.questionsWritten++;
            if (index < questionStart) questionStart = index;
        }
        writer.endArray();
        writer.name("questionStart").value(questionStart);
        writer.endObject();

        return ret;
    }

    public Key addAnswersFromJson(JsonReader reader, Key afterQuestion, EntityManager em, User user, double lat, double lng) throws IOException {
        if (mQuestions == null) {
            return null;
        }

        int index = 0;
        if (afterQuestion != null) {
            /*while (index < mQuestions.size() && !mQuestions.get(index).getId().equals(afterQuestion))
                ++index;
            ++index;*/
        }
        if (index >= mQuestions.size())  // Not found or found as the last item.
            return null;

        while (index < mQuestions.size() && reader.hasNext()) {
            Question question = mQuestions.get(index);
            if (question.shouldSkipDueToPrecondition(em, user)) {
                ++index;
                continue;
            }
            if (!question.addAnswerFromJson(em, reader, user, lat, lng)) {
                continue;
            }
            ++index;
            //afterQuestion = question.getId();
        }

        return afterQuestion;
    }

    public void writeToJsonShort(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("id").value(getIdString());
        writer.name("name").value(mName);
        writer.name("creator").value(creator);
        writer.endObject();
    }
}
