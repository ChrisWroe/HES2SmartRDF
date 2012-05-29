package org.smartvisuals.rdf;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class RDFReadTest {

	@Test
	public void test() {
		// create an empty model
		 Model model = ModelFactory.createDefaultModel();

		 String inputFileName = "example.xml";
		 // use the FileManager to find the input file
		 InputStream in = FileManager.get().open( inputFileName );
		if (in == null) {
		    throw new IllegalArgumentException(
		                                 "File: " + inputFileName + " not found");
		}

		// read the RDF/XML file
		model.read(in, null);

		// write it to standard out
		model.write(System.out);
	}

}
