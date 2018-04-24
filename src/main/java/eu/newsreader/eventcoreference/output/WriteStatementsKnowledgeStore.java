package eu.newsreader.eventcoreference.output;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import eu.fbk.knowledgestore.KnowledgeStore;
import eu.fbk.knowledgestore.Session;
import eu.fbk.knowledgestore.client.Client;
import eu.fbk.rdfpro.RDFProcessor;
import eu.fbk.rdfpro.RDFSource;
import eu.fbk.rdfpro.RDFSourceException;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class WriteStatementsKnowledgeStore implements RDFProcessor {

    static boolean DEBUG = false;


   // private static final Logger LOGGER = LoggerFactory.getLogger(WriteStatementsKnowledgeStore.class);

    static public void main (String[] args) {
        Dataset dataset  = null;
        String address = "http://145.100.57.176:50053/";
        ArrayList<org.openrdf.model.Statement> statements = new ArrayList<org.openrdf.model.Statement>();
                Iterator<String> it = dataset.listNames();
                while (it.hasNext()) {
                    String name = it.next();

                    Model namedModel = dataset.getNamedModel(name);
                    StmtIterator siter = namedModel.listStatements();
                    while (siter.hasNext()) {
                        com.hp.hpl.jena.rdf.model.Statement s = siter.nextStatement();
                        org.openrdf.model.Statement statement =castJenaOpenRdf(s, name);
                        if (statement!=null) {
                            statements.add(statement);
                        }
                    }
                }
                if (DEBUG) {
                    try {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        RDFDataMgr.write(os, dataset, RDFFormat.TRIG_PRETTY);
                        String rdfString = new String(os.toByteArray(), "UTF-8");
                        System.out.println("rdfString = " + rdfString);
                        os.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
               // System.out.println("address = " + address);
        WriteStatementsKnowledgeStore.storeTriples(statements, address);
    }


    static public void storeTriples (ArrayList<Statement> statements, String ksAddress) {
        KnowledgeStore ksClient  = null;
        if (ksAddress != null) {
            ksClient = Client.builder(ksAddress).compressionEnabled(true).maxConnections(2).validateServer(false).build();
            Session session = ksClient.newSession();
            try {
                session.sparqlupdate().statements(statements).exec();
            } catch (Exception e) {
              //  e.printStackTrace();
            } finally {
                session.close();
            }
        }
    }

    @Override
    public int getExtraPasses() {
        return 0;
    }

    @Override
    public RDFSource wrap(RDFSource source) {
        return null;
    }

    @Override
    public RDFHandler wrap(RDFHandler rdfHandler) {
        return null;
    }

    @Override
    public void apply(RDFSource input, RDFHandler output, int passes) throws RDFSourceException, RDFHandlerException {

    }

    static public org.openrdf.model.Statement castJenaOpenRdf(com.hp.hpl.jena.rdf.model.Statement jenaStatement, String modelName) {
            org.openrdf.model.Statement statement = null;
            try {
                ValueFactory valueFactory = ValueFactoryImpl.getInstance();
                URI modelURI = valueFactory.createURI(modelName);
                URI subject = valueFactory.createURI(jenaStatement.getSubject().getURI());
                URI sem = valueFactory.createURI(jenaStatement.getPredicate().getURI());
                if (jenaStatement.getObject().isLiteral()) {
                    Literal objectLiteral = valueFactory.createLiteral(jenaStatement.getObject().toString());
                     statement = valueFactory.createStatement(subject, sem, objectLiteral, modelURI);
                }

                else {
                    URI objectUri = valueFactory.createURI(jenaStatement.getObject().asResource().getURI());
                     statement = valueFactory.createStatement(subject, sem, objectUri, modelURI);
                }
            } catch (Exception e) {
                System.out.println("jenaStatement.toString() = " + jenaStatement.toString());
                e.printStackTrace();
            }
            return statement;
        }
}
