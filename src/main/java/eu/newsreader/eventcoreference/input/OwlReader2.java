package eu.newsreader.eventcoreference.input;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.PrintStream;

//import javax.annotation.Nonnull;

/**
 * Created by piek on 10/5/14.
 */
@SuppressWarnings("javadoc")


public class OwlReader2 {


    private static final int INDENT = 4;
   // @Nonnull
    private final OWLReasonerFactory reasonerFactory;
   // @Nonnull
    private final OWLOntology ontology;
    private final PrintStream out;

    private OwlReader2(OWLReasonerFactory reasonerFactory,
                                    OWLOntology inputOntology) {
        this.reasonerFactory = reasonerFactory;
        ontology = inputOntology;
        out = System.out;
    }

    /**
     * Print the class hierarchy for the given ontology from this class down,
     * assuming this class is at the given level. Makes no attempt to deal
     * sensibly with multiple inheritance.
     */
    private void printHierarchy(OWLClass clazz) throws OWLException {
        OWLReasoner reasoner = reasonerFactory
                .createNonBufferingReasoner(ontology);
        printHierarchy(reasoner, clazz, 0);
        /* Now print out any unsatisfiable classes */
        for (OWLClass cl : ontology.getClassesInSignature()) {
            assert cl != null;
            if (!reasoner.isSatisfiable(cl)) {
                out.println("XXX: " + labelFor(cl));
            }
        }
        reasoner.dispose();
    }

    private String labelFor(OWLClass clazz) {
        /*
         * Use a visitor to extract label annotations
         */
        /*LabelExtractor le = new LabelExtractor();
        for (OWLAnnotation anno : annotations(ontology
                .getAnnotationAssertionAxioms(clazz.getIRI()))) {
            anno.accept(le);
        }
        *//* Print out the label if there is one. If not, just use the class URI *//*
        if (le.getResult() != null) {
            return le.getResult();
        } else {
            return clazz.getIRI().toString();
        }*/
        return "";
    }

    /**
     * Print the class hierarchy from this class down, assuming this class is at
     * the given level. Makes no attempt to deal sensibly with multiple
     * inheritance.
     */
    private void printHierarchy(OWLReasoner reasoner,
                                OWLClass clazz, int level) throws OWLException {
        /*
         * Only print satisfiable classes -- otherwise we end up with bottom
         * everywhere
         */
        if (reasoner.isSatisfiable(clazz)) {
            for (int i = 0; i < level * INDENT; i++) {
                out.print(" ");
            }
            out.println(labelFor(clazz));
            /* Find the children and recurse */
            for (OWLClass child : reasoner.getSubClasses(clazz, true)
                    .getFlattened()) {
                if (!child.equals(clazz)) {
                    printHierarchy(reasoner, child, level + 1);
                }
            }
        }
    }

    static public void main (String args) throws OWLException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        String reasonerFactoryClassName = null;

        String pathToOwlOntology = "/Users/piek/Desktop/NWR/NWR-ontology/version-0.6/ESO_version_0.6.owl";
        OWLOntologyManager m = (OWLOntologyManager) OWLManager.getOWLDataFactory();
        @SuppressWarnings("null")
        //@Nonnull
        IRI documentIRI = IRI.create(pathToOwlOntology);
        OWLOntology ontology = m
                .loadOntologyFromOntologyDocument(documentIRI);
        // Report information about the ontology
        System.out.println("Ontology Loaded...");
        System.out.println("Document IRI: " + documentIRI);
        System.out.println("Ontology : " + ontology.getOntologyID());
        System.out.println("Format      : "
                + m.getOntologyFormat(ontology));

        @SuppressWarnings("null")

        OwlReader2 simpleHierarchy = new OwlReader2(
                (OWLReasonerFactory) Class.forName(reasonerFactoryClassName)
                        .newInstance(), ontology);
        // Get Thing
        OWLClass clazz = m.getOWLDataFactory().getOWLThing();
        System.out.println("Class       : " + clazz);
        // Print the hierarchy below thing
        simpleHierarchy.printHierarchy(clazz);
    }
}
