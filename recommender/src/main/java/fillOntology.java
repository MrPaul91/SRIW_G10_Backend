import models.Company;
import virtuoso.jena.driver.*;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;

import static spark.Spark.get;
import java.util.List;
import java.util.ArrayList;
import static spark.Spark.*;


public class fillOntology {
	
	public static void main(String []args) throws Exception{
		
		port(8008);
		
		//VirtGraph set = new VirtGraph("company","jdbc:virtuoso://50.18.123.50:1111","dba","dba");
		
		
		
		/*FileReader file = new FileReader("ConsultaCompany.txt");
		
		
		
		BufferedReader reader = new BufferedReader(file);
	    String s = reader.readLine();*/

		/**
	    String query = "PREFIX ds:<https://www.datos.gov.co/resource/yigb-eqpm/> "
	    		+ "SELECT ?direccion ?sector ?nombreinstitucion WHERE "
	    		+ "{ ?x ds:nombre_de_la_instituci_n ?nombreinstitucion . "
	    		+ "?x ds:direcci_n ?direccion. ?x ds:sector ?sector }";
	    /**/

		/**/
		String query = "PREFIX ds:<http://www.entrega1/ontologies/> \n" +
				"PREFIX dbo:<http://dbpedia.org/ontology/>\n" +
				"SELECT DISTINCT ?direccion ?sector ?nombreinstitucion\n" +
				"WHERE { \n" +
				"?x ds:companyName ?nombreinstitucion .\n" +
				"?x ds:hasSector ?sector .\n" +
				"?x dbo:address ?direccion\n" +
				"}\n" +
				"order by asc(?x)";
	    /**/

	    ResultSet results = queriesHelper.runQuery(query, "companies");

	    List<Company> list = new ArrayList<Company>();
	   
	   while (results.hasNext()){
		   QuerySolution soln = results.nextSolution();
		   
		  //System.out.println(StringUtils.stripAccents(soln.getLiteral("direccion").getString()));
		   Company c = new Company(StringUtils.stripAccents(soln.getLiteral("sector").getString()),StringUtils.stripAccents(soln.getLiteral("direccion").getString()) );
		   
		   System.out.println(c.toString());
		  
		   
		   list.add(c);
		   
		   
		   
		   
		   
	   }
	    
	   
	    /*Model model = ModelFactory.createDefaultModel();
	    InputStream fileOntology = FileManager.get().open("Ontology.owl");
	    
	    
	    model.read(fileOntology,null,"RDF/XML");
	    int indice = 0;
	
	    String URIOntology = "http://www.entrega1/ontologies/";	        
	    String URIDbpedia= "http://dbpedia.org/ontology/";	 
	        
	    ///Obtener la clase.
	    Resource classCompany = model.createResource(URIDbpedia+"models.Company");
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

	   get("/login/:id/:pass", (request, response) -> {
	   	String loginQuery = "PREFIX ds:<http://www.entrega1/ontologies/> \n" +
				"PREFIX dbo:<http://dbpedia.org/ontology/>\n" +
				"SELECT DISTINCT ?id ?password\n" +
				"WHERE { \n" +
				"?x ds:companyId \"" + request.params(":id") + "\" .\n" +
				"?x ds:companyPassword \"" + request.params(":pass") + "\"\n" +
				"}\n";

	   	ResultSet resultsLogin = queriesHelper.runQuery(loginQuery, "companies");

	   	if(resultsLogin.hasNext()) return "true";
	   	return "id " + request.params(":id")+" or pass "+request.params(":pass") + " are incorrects.";
	   });

	   System.out.println("hola");
    
    }



public static ResultSet executeQuery(String queryString) throws Exception {

	 QueryExecution exec =  QueryExecutionFactory.create(QueryFactory.create(queryString), new
			 DatasetImpl(ModelFactory.createDefaultModel()));
	 return exec.execSelect();
	 
}

}
