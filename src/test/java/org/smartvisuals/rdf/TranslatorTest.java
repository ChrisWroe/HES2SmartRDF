package org.smartvisuals.rdf;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;

public class TranslatorTest {

	@Test
	public void test() throws IOException, ParseException {
		Translator trans = new Translator();
		trans.translateToRDF("hack.csv", "v2/hack");
		
	}

}
