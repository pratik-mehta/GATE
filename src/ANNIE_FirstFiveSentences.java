import java.util.*;
import java.io.*;
import java.net.*;

import gate.*;
import gate.util.*;
import gate.util.persistence.PersistenceManager;
import gate.corpora.RepositioningInfo;

public class ANNIE_FirstFiveSentences  {

  /** The Corpus Pipeline application to contain ANNIE */
  private CorpusController annieController;

  /**
   * Initialize the ANNIE system. This creates a "corpus pipeline"
   * application that can be used to run sets of documents through
   * the extraction system.
   */
  public void initAnnie() throws GateException, IOException {
    Out.prln("Initialising ANNIE...");
    
    // load the ANNIE application from the saved state in plugins/ANNIE
    File pluginsHome = Gate.getPluginsHome();
    File anniePlugin = new File(pluginsHome, "ANNIE");
    File annieGapp = new File(anniePlugin, "ANNIE_with_FirstFiveSentences.gapp");
    annieController = (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);

    Out.prln("...ANNIE loaded");
  } // initAnnie()

  /** Tell ANNIE's controller about the corpus you want to run on */
  public void setCorpus(Corpus corpus) {
    annieController.setCorpus(corpus);
  } // setCorpus

  /** Run ANNIE */
  public void execute() throws GateException {
    Out.prln("Running ANNIE...");
    annieController.execute();
    Out.prln("...ANNIE complete");
  } // execute()

  public static void main(String args[]) throws GateException, IOException {
    // initialise the GATE library
    Out.prln("Initialising GATE...");
    Gate.init();
    Out.prln("...GATE initialised");

    // initialise ANNIE (this may take several minutes)
    ANNIE_FirstFiveSentences annie = new ANNIE_FirstFiveSentences();
    annie.initAnnie();

    // create a GATE corpus 
    Corpus corpus = Factory.newCorpus("StandAloneAnnie corpus");
    
      URL u = new URL("file:///Users/PM/Documents/Resume_Test.doc");
      FeatureMap params = Factory.newFeatureMap();
      params.put("sourceUrl", u);
      params.put("preserveOriginalContent", new Boolean(true));
      params.put("collectRepositioningInfo", new Boolean(true));
      Out.prln("Creating doc for " + u);
      Document doc = (Document)
        Factory.createResource("gate.corpora.DocumentImpl", params);
      corpus.add(doc);
    
    // tell the pipeline about the corpus and run it
    annie.setCorpus(corpus);
    annie.execute();

    // for each document, get an XML document with the location names added
    Iterator iter = corpus.iterator();
    int count = 0;
    String startTagPart_1 = "<span GateID=\"";
    String startTagPart_2 = "\" title=\"";
    String startTagPart_3 = "\" style=\"background:Red;\">";
    String endTag = "</span>";

    while(iter.hasNext()) {
      Document doc1 = (Document) iter.next();
      AnnotationSet defaultAnnotSet = doc1.getAnnotations();
      Set annotTypesRequired = new HashSet();
      annotTypesRequired.add("First_Five_Sentences");
      Set<Annotation> Places = new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));

      FeatureMap features = doc1.getFeatures();
      String originalContent = (String) features.get(GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);
      RepositioningInfo info = (RepositioningInfo) features.get(GateConstants.DOCUMENT_REPOSITIONING_INFO_FEATURE_NAME);

      ++count;
      File file = new File("StANNIE_" + count + ".HTML");
      Out.prln("File name: '"+file.getAbsolutePath()+"'");

        Iterator it = Places.iterator();
        Annotation currAnnot;
        SortedAnnotationsList sortedAnnotations = new SortedAnnotationsList();

        while(it.hasNext()) {
          currAnnot = (Annotation) it.next();
          sortedAnnotations.addSortedExclusive(currAnnot);
        } // while

        // diplay annotation count and details
        Out.prln("First five sentences annotations count: "+sortedAnnotations.size());
        for(int i=0; i<=sortedAnnotations.size()-1; i++) {
          currAnnot = (Annotation) sortedAnnotations.get(i);
          Out.prln(currAnnot);
        } // for
      } // while
    } // for each doc
  } // main

