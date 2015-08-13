package de.fraunhofer.fit.owljson.owldoc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.MissingImportEvent;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.MissingImportListener;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLOntologyWalker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Loads an ontology and passes it to JSONDataModelGenerator to generate a JSON representation of the ontology.
 *
 */
public class OwlDocGenerator {

  private static final Logger logger = Logger.getLogger(OwlDocGenerator.class.getName());

  public static void main(String[] args) throws OWLOntologyCreationException, IOException {
    (new OwlDocGenerator()).generate(args);
  }

  @SuppressWarnings("null")
  public void generate(String[] args) throws OWLOntologyCreationException, IOException {
    logger.info("Working dir: " + System.getProperty("user.dir"));

    String filename=null;
    String outputPrefix="";
    String mappingFilename=null;
    String outputFilename=null;
    try {
      if(args.length > 0) {
        filename = args[0];
        outputFilename=filename + ".js";
        for(int i = 1; i < args.length; i++) {
          switch(args[i]) {
            case "-p":
              outputPrefix = "ontology = ";
              i++;
              if(args[i].startsWith("-")) {
                i--;
              } else {
                outputPrefix = args[i];
              }
              break;
            case "-m":
              i++;
              mappingFilename = args[i];
              break;
            case "-o":
              i++;
              outputFilename = args[i];
              break;
          }
        }
      } else {
        printUsage();
      }
    } catch(ArrayIndexOutOfBoundsException a) {
      printUsage();
    }
    logger.info("Generating JS file for " + filename);
    logger.info("Output prefix " + outputPrefix);
    logger.info("Mapping file " + mappingFilename);
    logger.info("Output file " + outputFilename);

    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
    config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);

    manager.addMissingImportListener(new MyMissingImportListener(mappingFilename, manager,config));
    InputStream inpstr=this.getClass().getResourceAsStream(filename);
    if(inpstr==null) {
      inpstr = new FileInputStream(filename);
    }

    OWLOntology ont = manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(inpstr), config);

    OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(ont));

    JSONDataModelGenerator visitor = new JSONDataModelGenerator(walker, ont);

    walker.walkStructure(visitor);

    Gson gson = new GsonBuilder().setPrettyPrinting().create();


    File f = new File(outputFilename);
    BufferedWriter writer = new BufferedWriter(new FileWriter(f));
    /* BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream("js" + File.separator + filename + ".js"), "UTF-8")); */
    writer.write(outputPrefix);
    writer.write(gson.toJson(visitor.getOntologyMap()));
    writer.newLine();
    writer.flush();
    writer.close();

  }

  public static void printUsage() {
    System.out.println("java -jar OwlJsonDataModel filename [-p [prefix]] [-m mapping filename] [-o output filename]");
    System.out.println("  filename: filename of the input ontology");
    System.out.println("  -p: put the prefix before the JSON object in the output file, default is \"ontology=\"");
    System.out.println("  -m: mapping file to map URIs of imported ontologies to local filenames);");
    System.out.println("      The file has the syntax of a Java properties file, e.g., lines of the form");
    System.out.println("      URI=filename");
    System.out.println("  -o: name of the output file, default is \"input file\" + \".js\"");
  }
}

class MyMissingImportListener implements MissingImportListener {

  private static final Logger logger = Logger.getLogger(MyMissingImportListener.class.getName());

  private OWLOntologyManager mManager;
  private OWLOntologyLoaderConfiguration mConfig;
  private Properties ontMappings;

  MyMissingImportListener(String mappingFilename, OWLOntologyManager manager, OWLOntologyLoaderConfiguration config) {
    ontMappings=new Properties();
    try {
      if(mappingFilename != null) {
        ontMappings.load(new FileInputStream(mappingFilename));
      }
    } catch(FileNotFoundException e) {
      logger.severe("Mapping file not found: " + mappingFilename);
      OwlDocGenerator.printUsage();
    } catch(IOException e) {
      logger.severe("IOException in reading mapping file: " + mappingFilename);
      logger.severe(e.getMessage());
      e.printStackTrace();
      OwlDocGenerator.printUsage();
    }
    mManager=manager;
    mConfig=config;
  }


  @Override
  public void importMissing(MissingImportEvent missingImportEvent) {
    String sUri=missingImportEvent.getImportedOntologyURI().toString();
    logger.fine("Ontology missing: " + sUri);
    String fname=ontMappings.getProperty(sUri);
    try {
      if(fname!=null) {
        InputStream inpstr = this.getClass().getResourceAsStream(fname);
        if(inpstr==null) {
          inpstr=new FileInputStream(fname);
        }
        mManager.loadOntologyFromOntologyDocument(new StreamDocumentSource(inpstr), mConfig);
      }
    } catch(OWLOntologyCreationException e) {
      logger.log(Level.SEVERE, "Exception in MissingImportHandler", e);
      logger.severe("Exception in MissingImportHandler: " + e.getMessage());
      logger.severe("Could not load imported ontology: " + sUri);
      OwlDocGenerator.printUsage();
    } catch(FileNotFoundException e) {
      logger.log(Level.SEVERE,"Exception in MissingImportHandler", e);
      OwlDocGenerator.printUsage();
    }
  }
}

