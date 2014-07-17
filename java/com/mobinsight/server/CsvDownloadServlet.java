package com.mobinsight.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.csvreader.CsvWriter;
import com.google.appengine.api.datastore.KeyFactory;
import com.mobinsight.user.GaeUserDAO;

@Singleton
public class CsvDownloadServlet extends BaseServlet {

	/** serial number. */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(CsvDownloadServlet.class.getName());
	/** BaseServlet Constructor satisfy graph injection. */
	@Inject
	protected CsvDownloadServlet(Provider<GaeUserDAO> daoProvider) {
		super(daoProvider);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String surveyId = req.getParameter("surveyId");
		EntityManager em = EMF.get().createEntityManager();
		Survey survey = em.find(Survey.class, KeyFactory.stringToKey(surveyId));
		

		CsvWriter csvWriter = new CsvWriter(resp.getWriter(), '\t');
		
		resp.setHeader("Content-Type", "text/csv");
		resp.setHeader("Content-Disposition", "attachment; filename=\"csvFile.csv\"");
		
		csvWriter.write(survey.getName());
		csvWriter.endRecord();
		
		List<Question> surveyQuestions = survey.getQuestions();
		ArrayList<String> questionsArray = new ArrayList<String>();
		List<ArrayList<String>> listAnswers = new ArrayList<ArrayList<String>>();
		ListIterator<Question> li = surveyQuestions.listIterator();
		while(li.hasNext()) {
			Question question = (Question) li.next();
			questionsArray.add(question.getText());
			ArrayList<Answer> tempAnswerList1 = new ArrayList<Answer>();
			tempAnswerList1 = (ArrayList<Answer>) question.getAnswers();
			ListIterator<Answer> answerListIteratorTemp =
					(ListIterator<Answer>) tempAnswerList1.iterator();
			ArrayList<String> tempAnswerStringList2 = new ArrayList<String>();
			while (answerListIteratorTemp.hasNext()) {
				Answer currentAnswer = answerListIteratorTemp.next();
				if (question.getAnswerType() == Question.ANSWER_TYPE_CHOICE) {
					tempAnswerStringList2.add(currentAnswer.mAnswerIndex + "");
				} else if (question.getAnswerType() == Question.ANSWER_TYPE_RANGE) {
					tempAnswerStringList2.add(currentAnswer.mAnswerRangeValue + "");
				} else if (question.getAnswerType() == Question.ANSWER_TYPE_TEXT) {
					tempAnswerStringList2.add(currentAnswer.mAnswerText);
				}
			}
			listAnswers.add(tempAnswerStringList2);
		}
		
		csvWriter.write(Arrays.asList(questionsArray).toString());
		csvWriter.endRecord();
		
//		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&");
//		for (int j=0; j<allAnswers.get(0).size(); j++) {
//			ArrayList<String> newRow = new ArrayList<String>();
//			for(int i=0; i<3; i++) {
//				newRow.add(allAnswers.get(i).get(j));
//				System.out.print(allAnswers.get(i).get(j).toString() + " ");
//			}
//			System.out.println();
//		}
//		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&");

		// Collated list
		List<ArrayList<String>> collatedAnswers = new ArrayList<ArrayList<String>>();
		// Create and get all the questions.size() lists.
		// listAnswers
	
		for (int j=0; j<listAnswers.get(0).size(); j++) { // Equal to total answers.
			log.severe("j: " + j);
			log.severe(" listAnswers.toString() " + listAnswers.toString());
			
			ArrayList<String> newList = new ArrayList<String>();
			log.severe(listAnswers.get(0).size() + "");
			log.severe(listAnswers.size() + "");
			
			for (int i=0; i< listAnswers.size(); i++) {
				log.severe("i: " + i);
				try{
					newList.add(listAnswers.get(i).get(j));
					log.severe(" listAnswers.get(i).get(j) " + listAnswers.get(i).get(j).toString());
					log.severe(listAnswers.get(i).get(j).toString());
				} catch (IndexOutOfBoundsException e) {
					newList.add("No response");
				}
			}
			collatedAnswers.add(newList);
		}

		
		for (List<String> answersList : collatedAnswers ) {
			csvWriter.write(Arrays.asList(answersList).toString());
			csvWriter.endRecord();	
		}

		csvWriter.flush();
		csvWriter.close();
		// issueJson(resp, 200, "Message", "No Parameters Specified.");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPost(req, resp);
	}

	
	
	

}
