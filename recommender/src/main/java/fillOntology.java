import virtuoso.jena.driver.*;

import org.apache.jena.query.*;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.*;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;

import static spark.Spark.get;
import java.util.List;
import java.util.ArrayList;
import static spark.Spark.*;


public class fillOntology {
	
	public static void main(String []args) throws Exception{
		
		port(8008);
		
		VirtGraph set = new VirtGraph("company","jdbc:virtuoso://50.18.123.50:1111","dba","dba");
		
		
		
		/*FileReader file = new FileReader("ConsultaCompany.txt");
		
		
		
		BufferedReader reader = new BufferedReader(file);
	    String s = reader.readLine();*/
	    
	    String query = "PREFIX ds:<https://www.datos.gov.co/resource/yigb-eqpm/> "
	    		+ "SELECT ?direccion ?sector ?nombreinstitucion WHERE "
	    		+ "{ ?x ds:nombre_de_la_instituci_n ?nombreinstitucion . "
	    		+ "?x ds:direcci_n ?direccion. ?x ds:sector ?sector }";
	    		
	    ResultSet results = queriesHelper.runQuery(query, "company");
	    
	    List<String> list = new ArrayList<String>();
	   
	   while (results.hasNext()){
		   QuerySolution soln = results.nextSolution();
		   
		  //System.out.println(StringUtils.stripAccents(soln.getLiteral("direccion").getString()));
		   
		   list.add(StringUtils.stripAccents(soln.getLiteral("direccion").getString()));
		   
		   
	   }
	    
	   
	    /*Model model = ModelFactory.createDefaultModel();
	    InputStream fileOntology = FileManager.get().open("Ontology.owl");
	    
	    
	    model.read(fileOntology,null,"RDF/XML");
	    int indice = 0;
	
	    String URIOntology = "http://www.entrega1/ontologies/";	        
	    String URIDbpedia= "http://dbpedia.org/ontology/";	 
	        
	    ///Obtener la clase.
	    Resource classCompany = model.createResource(URIDbpedia+"Company");
	    ///Propiedades.
	    Property companyName_property = model.createProperty(URIOntology+"companyName");
	    Property address_property = model.createProperty(URIDbpedia+"address");
	    Property sector_property = model.createProperty(URIOntology+"hasSector");*/
        
	   Gson gson = new Gson();


	   get("/hello", (req, res)->"Hello, world");
	   
	   get("/lista", (req, res) -> {
			res.type("application/json");
			System.out.print("entro a pedir este mostro");
			return list;
		}, gson ::toJson);
	   
	   System.out.println("hola");
    
    }



public static ResultSet executeQuery(String queryString) throws Exception {

	 QueryExecution exec =  QueryExecutionFactory.create(QueryFactory.create(queryString), new
			 DatasetImpl(ModelFactory.createDefaultModel()));
	 return exec.execSelect();
	 
}

}
