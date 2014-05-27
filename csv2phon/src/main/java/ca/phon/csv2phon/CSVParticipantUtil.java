package ca.phon.csv2phon;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ca.phon.csv2phon.io.ObjectFactory;
import ca.phon.csv2phon.io.ParticipantType;
import ca.phon.csv2phon.io.SexType;
import ca.phon.session.Participant;
import ca.phon.session.ParticipantRole;
import ca.phon.session.SessionFactory;
import ca.phon.session.Sex;

public class CSVParticipantUtil {
	
	private final static Logger LOGGER = Logger
			.getLogger(CSVParticipantUtil.class.getName());

	public static ParticipantType copyPhonParticipant(ObjectFactory factory, Participant part) {
		final ParticipantType retVal = factory.createParticipantType();
		
		retVal.setId(part.getId());
		retVal.setName(part.getName());
		
		final DateTime bday = part.getBirthDate();
		if(bday != null) {
			try {
				final DatatypeFactory df = DatatypeFactory.newInstance();
				final XMLGregorianCalendar cal = df.newXMLGregorianCalendar(bday.toGregorianCalendar());
				cal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
				retVal.setBirthday(cal);
			} catch (DatatypeConfigurationException e) {
				LOGGER.log(Level.WARNING, e.toString(), e);
			}
		}
		
		retVal.setEducation(part.getEducation());
		retVal.setGroup(part.getGroup());
		
		final String lang = part.getLanguage();
		final String langs[] = lang.split(",");
		for(String l:langs) {
			retVal.getLanguage().add(StringUtils.strip(l));
		}

		retVal.setSex(part.getSex() == Sex.MALE ? SexType.MALE : SexType.FEMALE);
		
		ParticipantRole prole = part.getRole();
		if(prole == null)
			prole = ParticipantRole.TARGET_CHILD;
		retVal.setRole(prole.toString());
		
		retVal.setSES(part.getSES());
			
		return retVal;
	}
	
	public static Participant copyXmlParticipant(SessionFactory factory, ParticipantType pt, DateTime sessionDate) {
		final Participant retVal = factory.createParticipant();
		
		retVal.setId(pt.getId());
		retVal.setName(pt.getName());
		
		final XMLGregorianCalendar bday = pt.getBirthday();
		if(bday != null) {
			final DateTime bdt = new DateTime(bday.getYear(), bday.getMonth(), bday.getDay(), 12, 0);
			retVal.setBirthDate(bdt);
			
			// calculate age up to the session date
			final Period period = new Period(bdt, sessionDate);
			retVal.setAgeTo(period);
		}
		
		retVal.setEducation(pt.getEducation());
		retVal.setGroup(pt.getGroup());
		
		String langs = "";
		for(String lang:pt.getLanguage())
			langs += (langs.length() > 0 ? ", " : "") + lang;
		retVal.setLanguage(langs);

		retVal.setSex(pt.getSex() == SexType.MALE ? Sex.MALE : Sex.FEMALE);
		
		ParticipantRole prole = ParticipantRole.fromString(pt.getRole());
		if(prole == null)
			prole = ParticipantRole.TARGET_CHILD;
		retVal.setRole(prole);
		
		retVal.setSES(pt.getSES());
			
		return retVal;
	}
	
}
