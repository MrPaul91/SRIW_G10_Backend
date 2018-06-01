import models.Company;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import virtuoso.jena.driver.*;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;

import static spark.Spark.get;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import static spark.Spark.*;


public class fillOntology {
	
	public static void main(String []args) throws Exception{
		
		port(8008);

        options("/*",
                (request, response) -> {

                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }

                    return "OK";

                });

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
		   
		   //System.out.println(c.toString());

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

	   	if(resultsLogin.hasNext()) return true;
	   	return false;//"id " + request.params(":id")+" or pass "+request.params(":pass") + " are incorrects.";
	   });

	   post("/insertjobPosition", (request, response) -> {
		   JSONParser parser = new JSONParser();
		   Object obj = parser.parse("[" + request.body() + "]");
		   JSONArray array = (JSONArray) obj;
		   JSONObject obj2 = (JSONObject) array.get(0);

			String companyId = (String)obj2.get("companyId");
			String jobPosition = (String)obj2.get("jobPosition");
			String jobName = (String)obj2.get("jobName");
			String description = (String)obj2.get("description");

			String query1 = "PREFIX ds:<http://www.entrega1/ontologies/> \n" +
					"PREFIX dbo:<http://dbpedia.org/ontology/>\n" +
					"SELECT ?jobPos \n" +
					"WHERE{\n" +
						"<" + jobPosition + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ds:jobPosition" +
					"}";
			ResultSet resultSet = queriesHelper.runQuery(query1, "companies");

			if(resultSet.hasNext()) return "jobPosition "+ jobPosition +" already exist, select anoter name";

		   String query2 = "PREFIX ds:<http://www.entrega1/ontologies/> \n" +
				   "PREFIX dbo:<http://dbpedia.org/ontology/>\n" +
				   "SELECT ?company \n" +
				   "WHERE{\n" +
				   "?company ds:companyId \"" + companyId + "\"\n" +
				   "}";

		   resultSet = queriesHelper.runQuery(query2, "companies");
		   if(!resultSet.hasNext()) return "companyId does not exist";
		   //QuerySolution vcompany = resultSet.nextSolution();
		   String company = resultSet.nextSolution().getResource("company").getURI();

		   VirtGraph set = new VirtGraph("companies","jdbc:virtuoso://50.18.123.50:1111","dba","dba");

		   Node vjobPosition = NodeFactory.createURI(jobPosition);
		   Node vType = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		   Node vjobPosType = NodeFactory.createURI("http://www.entrega1/ontologies/jobPosition");
		   Node vjobName = NodeFactory.createURI("http://www.entrega1/ontologies/jobName");
		   Node vjobDescrip = NodeFactory.createURI("http://purl.org/dc/terms/description");
		   Node vcompanyHasJob = NodeFactory.createURI("http://www.entrega1/ontologies/hasJobPosition");

		   RDFDatatype nultype = null;
			set.add(new Triple(vjobPosition,vType,vjobPosType));
			set.add(new Triple(vjobPosition,vjobName,NodeFactory.createLiteral(jobName,nultype)));
			set.add(new Triple(vjobPosition,vjobDescrip,NodeFactory.createLiteral(description,nultype)));
			set.add(new Triple(NodeFactory.createURI(company),vcompanyHasJob,vjobPosition));

			set.close();
			return true;
	   });

	   post("/insertNeededSkill", (request, response) -> {
           JSONParser parser = new JSONParser();
           Object obj = parser.parse("[" + request.body() + "]");
           JSONArray array = (JSONArray) obj;
           JSONObject obj2 = (JSONObject) array.get(0);

            String jobPosition = (String)obj2.get("jobPos");
            String nameOfSkill = (String)obj2.get("name");
            String skill = (String)obj2.get("skill");

           String query1 = "PREFIX ds:<http://www.entrega1/ontologies/> \n" +
                   "PREFIX dbo:<http://dbpedia.org/ontology/>\n" +
                   "SELECT ?skill \n" +
                   "WHERE{" +
                   "{ <" + skill + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/resource/Skill> }\n" +
                   "UNION\n" +
                   "{ <" + skill + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ds:Competence }\n" +
                   "}";
           ResultSet resultSet = queriesHelper.runQuery(query1, "companies");

           if(resultSet.hasNext()) return "skill already exist, select another name";

           String query2 = "PREFIX ds:<http://www.entrega1/ontologies/> \n" +
                   "PREFIX dbo:<http://dbpedia.org/ontology/>\n" +
                   "SELECT ?jobPosition \n" +
                   "WHERE" +
                   "{ <" + jobPosition + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ds:jobPosition }";
           resultSet = queriesHelper.runQuery(query2, "companies");

           if(!resultSet.hasNext()) return "jobPosition does not exist";

           Node vSkill = NodeFactory.createURI(skill);
           Node vjobPosition = NodeFactory.createURI(jobPosition);
           Node vType = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
           Node vskillType = NodeFactory.createURI("http://dbpedia.org/resource/Skill");
           Node vnameOfSkill = NodeFactory.createURI("http://www.entrega1/ontologies/nameOfSkill");
           Node vhasSkill = NodeFactory.createURI("http://www.entrega1/ontologies/hasSkill");

           RDFDatatype nultype = null;
           VirtGraph set = new VirtGraph("companies","jdbc:virtuoso://50.18.123.50:1111","dba","dba");

           set.add(new Triple(vSkill,vType,vskillType));
           set.add(new Triple(vSkill,vnameOfSkill,NodeFactory.createLiteral(nameOfSkill,nultype)));
           set.add(new Triple(vjobPosition,vhasSkill,vSkill));

           set.close();
	       return true;
       });

	   post("/insertTask", (request, response) -> {
           JSONParser parser = new JSONParser();
           Object obj = parser.parse("[" + request.body() + "]");
           JSONArray array = (JSONArray) obj;
           JSONObject obj2 = (JSONObject) array.get(0);

           String taskItem = (String)obj2.get("taskItem");
           String qualityGrade = (String)obj2.get("quality");
           String skill = (String)obj2.get("skill");
           String task = (String)obj2.get("task");

           String query1 = "PREFIX ds:<http://www.entrega1/ontologies/> \n" +
                   "PREFIX dbo:<http://dbpedia.org/ontology/>\n" +
                   "SELECT ?task \n" +
                   "WHERE{" +
                   "{ <" + task + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/resource/Task> }\n" +
                   "UNION\n" +
                   "{ <" + task + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ds:Task }\n" +
                   "}";
           ResultSet resultSet = queriesHelper.runQuery(query1, "companies");

           if(resultSet.hasNext()) return "task already exist, select another name";

           String query2 = "PREFIX ds:<http://www.entrega1/ontologies/> \n" +
                   "PREFIX dbo:<http://dbpedia.org/ontology/>\n" +
                   "SELECT ?skill \n" +
                   "WHERE{" +
                   "{ <" + skill + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/resource/Skill> }\n" +
                   "UNION\n" +
                   "{ <" + skill + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ds:Competence }\n" +
                   "}";
           resultSet = queriesHelper.runQuery(query2, "companies");

           if(!resultSet.hasNext()) return "skill does not exist";

           Node vSkill = NodeFactory.createURI(skill);
           Node vTask = NodeFactory.createURI(task);
           Node vType = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
           Node vTaskType = NodeFactory.createURI("http://dbpedia.org/resource/Task");
           Node vTaskItem = NodeFactory.createURI("http://www.entrega1/ontologies/taskItem");
           Node vhasTask = NodeFactory.createURI("http://www.entrega1/ontologies/hasTask");
           Node vQualityGrade = NodeFactory.createURI("http://www.entrega1/ontologies/qualityGrade");

           RDFDatatype nultype = null;
           VirtGraph set = new VirtGraph("companies","jdbc:virtuoso://50.18.123.50:1111","dba","dba");

           set.add(new Triple(vTask,vType,vTaskType));
           set.add(new Triple(vTask,vTaskItem,NodeFactory.createLiteral(taskItem,nultype)));
           set.add(new Triple(vTask,vQualityGrade,NodeFactory.createLiteral(qualityGrade,nultype)));
           set.add(new Triple(vSkill,vhasTask,vTask));

           set.close();
	       return true;
       });

	   get("/recommendation/:companyId/:jobPos", (request, response) -> {
	   		String query1 = "PREFIX ds:<http://www.entrega1/ontologies/> \n" +
			"PREFIX dbo:<http://dbpedia.org/ontology/>\n" +
			"SELECT DISTINCT ?taskItem ?quality\n" +
					"WHERE {\n" +
					request.params(":jobPos") + " ds:needsCompetence + ?skill .\n" +
					"?skill ds:hasTask ?task .\n"+
					"?task ds:taskItem ?taskItem .\n" +
					"?task ds:qualityGrade ?quality \n" +
					"}";
	   		ResultSet resultSet = queriesHelper.runQuery(query1,"companies");

	   		TreeMap<String, Integer> TaskList = new TreeMap<>();
	   		while(resultSet.hasNext()){
	   			QuerySolution task = resultSet.nextSolution();
	   			String taskItem = task.getLiteral("taskItem").getString();
	   			Integer quality = task.getLiteral("quality").getInt();
	   			if(!TaskList.containsKey(taskItem) || TaskList.get(taskItem)<quality) TaskList.put(taskItem,quality);
			}
			//Por terminar

	   		return false;
	   }); //Sin terminar

	   System.out.println("escuchando en puerto 8008");
    
    }



public static ResultSet executeQuery(String queryString) throws Exception {

	 QueryExecution exec =  QueryExecutionFactory.create(QueryFactory.create(queryString), new
			 DatasetImpl(ModelFactory.createDefaultModel()));
	 return exec.execSelect();
	 
}

}
