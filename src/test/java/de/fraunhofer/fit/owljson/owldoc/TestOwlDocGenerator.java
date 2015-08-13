package de.fraunhofer.fit.owljson.owldoc;

import junit.framework.TestCase;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;

/**
 * User: Quix
 * Date: 17.10.2014
 */
public class TestOwlDocGenerator extends TestCase {

  public void testOwlDocGenerator() {
    try {
      // new OwlDocGenerator().generate("kdsf-pub");
      new OwlDocGenerator().generate("kdsf-aggr-merged");
      new OwlDocGenerator().generate("kdsf-merged");
      new OwlDocGenerator().generate("kdsf");
      new OwlDocGenerator().generate("kdsf-aggr");
    } catch(OWLOntologyCreationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    } catch(IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

}
