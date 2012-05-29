package org.smartvisuals.rdf.model;

import java.util.Date;
import java.text.ParseException;

import org.apache.commons.lang.time.DateUtils;

public class Procedure extends CodedEntity {

	private Date date;

	public Procedure(CodedEntity codedEntity) {
		this.code = codedEntity.getCode();
		this.text = codedEntity.getText();
	}

	public void setDate(String procDate) throws ParseException {
		 String[] dateFormats = new String[1];
		 dateFormats[0]="dd/MM/yyyy";
		 date= DateUtils.parseDateStrictly(procDate, dateFormats);
		
	}

	public Date getDate() {
		return date;
	}

}
