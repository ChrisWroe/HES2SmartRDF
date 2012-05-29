package org.smartvisuals.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.smartvisuals.rdf.model.CodedEntity;
import org.smartvisuals.rdf.model.Diagnosis;
import org.smartvisuals.rdf.model.ExternalCodeScheme;
import org.smartvisuals.rdf.model.Procedure;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

import au.com.bytecode.opencsv.CSVReader;

public class Translator {

	// open the file

	String rdfNamespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	String termsNamespace = "http://smartplatforms.org/terms#";
	String codesNamespace = "http://smartplatforms.org/terms/codes/";
	String codeSchemeNamespace = "http://purl.bioontology.org/ontology/";
	private Property typeProperty;
	private Model model;
	private Property belongs;
	private Resource procType;
	private Resource demoType;
	private Property procDate;
	private Property problemName;
	private Resource problemType;
	private Date today;
	private Resource encounterType;
	private Property startDateProperty;
	private Resource icd10Type;
	private Property codeProperty;
	private Resource genericCodeResourceType;
	private Property codeSystemProperty;
	private Resource inpatientEncounter;
	private Resource encounterTypeType;
	private Resource inpatientEncounterCode;
	private Resource encounterCodeSystem;
	private Resource codeType;
	private Property encounterTypeProperty;
	private Resource inpatientEncounterCodedValue;
	private Resource codedValueType;

	public void translateToRDF(String fileIn, String fileOut)
			throws IOException, ParseException {

		
		
		today= new Date();

		FileOutputStream out = FileUtils.openOutputStream(new File(fileOut+"complete.xml"));

		model = ModelFactory.createDefaultModel();
		RDFWriter writer = model.getWriter();//("RDF/XML-ABBREV");
		writer.setProperty("showXmlDeclaration","true");
		setupModel();
		
		CSVReader reader = new CSVReader(new FileReader(fileIn));
		String[] nextLine;
		reader.readNext();
		Integer currentLine = 1;
		Integer numberOfRecords = 25000;
		while ((nextLine = reader.readNext()) != null) {
			//FileOutputStream out = FileUtils.openOutputStream(new File(fileOut+currentLine+".xml"));

			//model = ModelFactory.createDefaultModel();
			//setupModel();
			String hesid = nextLine[0];

			Resource record = model
					.createResource("http://sandbox-api.smartplatforms.org/records/"
							+ hesid);

			String sex = nextLine[1];
			String startAge = nextLine[2];
			String admissionDate = nextLine[3];
			List<CodedEntity> diagnoses = readCodedEntity(nextLine, 4, 16);
			List<CodedEntity> procedures = readCodedEntity(nextLine, 16, 18);

			if (procedures.size() > 0) {
				Procedure proc = new Procedure(procedures.get(0));
				String procDate = nextLine[18];
				proc.setDate(procDate);
				createProcedure(model, proc, record);
			}

			for (CodedEntity diagnosis : diagnoses) {

				createProblem(model, diagnosis, record);

			}

			createDemographics(model, sex, startAge, record);
			
			createEncounter(model,admissionDate,record);
			

			// create an empty Model
			//RDFWriter writer = model.getWriter();//("RDF/XML-ABBREV");
			//writer.setProperty("showXmlDeclaration","true");
			//writer.write(model,out,"");
			//model.write(out, "RDF/XML-ABBREV");

			//out.close();
			currentLine++;
			if (currentLine > numberOfRecords) {
				break;
			}
		}
		writer.write(model,out,"");
		out.close();
		reader.close();

	}

	private void setupModel() {
		typeProperty = model.createProperty(rdfNamespace, "type");

		procType = model.createResource(termsNamespace + "Procedure");

		demoType = model.createResource(termsNamespace + "Demographics");
		
		codeType = model.createResource(termsNamespace + "Code");
		//encounterType = model.createResource(codesNamespace + "EncounterType");

		codeProperty = model.createProperty(termsNamespace,"code");
		codeSystemProperty = model.createProperty(rdfNamespace,"system");
		encounterType = model.createResource(termsNamespace+"Encounter");
		
		
		encounterTypeType = model.createResource(codesNamespace+"EncounterType");
		
		encounterCodeSystem = model.createResource(codesNamespace+"EncounterType#");
		
		inpatientEncounterCode = model.createResource(codesNamespace+"EncounterType#inpatient");
		inpatientEncounterCode.addProperty(RDF.type, codeType);
		inpatientEncounterCode.addProperty(RDF.type, encounterTypeType);
		
		inpatientEncounterCode.addProperty(DCTerms.title, "Inpatient Encounter");
		inpatientEncounterCode.addProperty(codeSystemProperty,encounterCodeSystem);
		inpatientEncounterCode.addProperty(DCTerms.identifier,"inpatient");
		
		inpatientEncounterCodedValue = model.createResource();
		
		codedValueType = model.createResource(termsNamespace
				+ "CodedValue");
		inpatientEncounterCodedValue.addProperty(DCTerms.title, "Inpatient Encounter");
		inpatientEncounterCodedValue.addProperty(typeProperty, codedValueType);
		inpatientEncounterCodedValue.addProperty(codeProperty, inpatientEncounterCode);
		
		
		belongs = model.createProperty(termsNamespace,
				"belongsTo");

		procDate = model.createProperty(termsNamespace,
				"procedureDate");

		problemName = model.createProperty(termsNamespace,
				"procedureName");
		
		startDateProperty = model.createProperty(termsNamespace,
				"startDate");
		
		encounterTypeProperty = model.createProperty(termsNamespace,
				"encounterType");

		problemType = model.createResource(termsNamespace + "Problem");
		
		genericCodeResourceType = model.createResource(termsNamespace + "Code");
		
		
		

		model.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
		model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
		model.setNsPrefix("sp", "http://smartplatforms.org/terms#");
		model.setNsPrefix("v", "http://www.w3.org/2006/vcard/ns#");
		model.setNsPrefix("spcode", "http://smartplatforms.org/terms/codes/");
	}

	private void createEncounter(Model model2, String admissionDate,
			Resource record) throws ParseException {
		Resource encRes = model.createResource();
		encRes.addProperty(typeProperty, encounterType);
		model.add(encRes, belongs, record);
		String[] dateFormats = new String[1];
		 dateFormats[0]="dd/MM/yyyy";
		Date startDate= DateUtils.parseDate(admissionDate, dateFormats);
		String startDateString = DateFormatUtils.format(startDate, "yyyy-MM-dd");
		
		
		encRes.addProperty(encounterTypeProperty,inpatientEncounterCodedValue);
		
		encRes.addProperty(startDateProperty, startDateString);
	}

	private void createDemographics(Model model2, String sex, String startAge,
			Resource record) {
		Resource demoRes = model.createResource();
		demoRes.addProperty(typeProperty, demoType);
		model.add(demoRes, belongs, record);
		if (sex.equals("1")) {
			demoRes.addProperty(FOAF.gender, "male");
		} else {
			demoRes.addProperty(FOAF.gender, "female");
		}
		
		
		Date birthDate=DateUtils.addYears(today, -(new Integer(startAge)));
		
		String dob = DateFormatUtils.format(birthDate,"yyyy-MM-dd");
		demoRes.addProperty(FOAF.birthday, dob);

	}

	private void createProcedure(Model model2, Procedure proc, Resource record) {
		Resource procRes = model.createResource();

		procRes.addProperty(typeProperty, procType);

		model.add(procRes, belongs, record);
		
		String procDateStr = DateFormatUtils.format(proc.getDate(),"yyyy-MM-dd");
		
		procRes.addProperty(procDate, procDateStr);

		createCodedValue(model, proc, procRes, problemName, ExternalCodeScheme.OPCS);

	}

	private void createProblem(Model model, CodedEntity diagnosis,
			Resource record) {
		Resource problem = model.createResource();

		problem.addProperty(typeProperty, problemType);

		model.add(problem, belongs, record);

		Property problemName = model.createProperty(
				"http://smartplatforms.org/terms#", "problemName");

		createCodedValue(model, diagnosis, problem, problemName, ExternalCodeScheme.ICD10);

	}

	private void createCodedValue(Model model, CodedEntity codedEntity,
			Resource resource, Property resourceName,ExternalCodeScheme codeScheme) {
		Resource codedValue = model.createResource();
		Resource codedValueType = model.createResource(termsNamespace
				+ "CodedValue");
		codedValue.addProperty(DCTerms.title, codedEntity.getText());
		codedValue.addProperty(typeProperty, codedValueType);
		
		Resource codeResource = model.createResource(codeSchemeNamespace+codeScheme.toString()+"/"+codedEntity.getCode());
		Resource codeSystemResource = model.createResource(codeSchemeNamespace+codeScheme.toString());
		Resource codeResourceType = model.createResource(termsNamespace+codeScheme.toString());
		codeResource.addProperty(typeProperty, codeResourceType);
		codeResource.addProperty(typeProperty, genericCodeResourceType);
		codeResource.addProperty(DCTerms.title, codedEntity.getText());
		codeResource.addProperty(DCTerms.identifier,codedEntity.getCode());
		codeResource.addProperty(codeSystemProperty,codeSystemResource);
		codedValue.addProperty(codeProperty, codeResource);
		model.add(resource, resourceName, codedValue);
	}

	private List<CodedEntity> readCodedEntity(String[] nextLine, Integer start,
			Integer end) {
		List<CodedEntity> entities = new ArrayList<CodedEntity>();
		for (int i = start; i < end; i = i + 2) {
			CodedEntity entity = new CodedEntity();
			if (nextLine[i].length() == 0) {
				continue;
			}
			entity.setCode(nextLine[i]);
			entity.setText(nextLine[i + 1]);
			entities.add(entity);
		}
		return entities;
	}

}
