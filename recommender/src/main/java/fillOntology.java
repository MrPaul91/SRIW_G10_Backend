import models.Company;
import models.Person;
import models.Task;
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
import java.util.Map;
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
				"SELECT DISTINCT ?id ?password \n" +
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
           Node vNeedsCompetence = NodeFactory.createURI("http://www.entrega1/ontologies/needsCompetence");

           RDFDatatype nultype = null;
           VirtGraph set = new VirtGraph("companies","jdbc:virtuoso://50.18.123.50:1111","dba","dba");

           set.add(new Triple(vSkill,vType,vskillType));
           set.add(new Triple(vSkill,vnameOfSkill,NodeFactory.createLiteral(nameOfSkill,nultype)));
           set.add(new Triple(vjobPosition,vNeedsCompetence,vSkill));

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
	       class personTree{
	           String id, name;
	           TreeMap<String, Integer> taskList;
	           personTree(String id, String name){
	               this.id = id;
	               this.name = name;
	               taskList = new TreeMap<>();
               }
           }

	   		String query1 = "PREFIX ds:<http://www.entrega1/ontologies/> \n" +
			"PREFIX dbo:<http://dbpedia.org/ontology/>\n" +
			"SELECT DISTINCT ?taskItem ?quality\n" +
					"WHERE " +
                    "{ <" + request.params(":jobPos") + "> ds:needsCompetence ?skill .\n" +
					"?skill ds:hasTask ?task .\n"+
					"?task ds:taskItem ?taskItem .\n" +
					"?task ds:qualityGrade ?quality \n" +
					"}";
	   		ResultSet resultSet = queriesHelper.runQuery(query1,"companies");

	   		TreeMap<String, Float> TaskList = new TreeMap<>();
	   		while(resultSet.hasNext()){
	   			QuerySolution task = resultSet.nextSolution();
	   			String taskItem = task.getLiteral("taskItem").getString();
	   			float quality = task.getLiteral("quality").getFloat();
	   			if(!TaskList.containsKey(taskItem) || TaskList.get(taskItem)<quality) TaskList.put(taskItem,quality);
			}

			/**
			for(Map.Entry<String, Integer> entry : TaskList.entrySet()){
	   		    System.out.println(entry.getKey()+": "+entry.getValue());
           }
           /**/

			String query2 = "PREFIX vocab: <http://localhost:2020/resource/vocab/>\n" +
                    "SELECT DISTINCT ?id ?name ?taskItem ?quality WHERE {\n" +
                    "service<http://50.18.123.50:2020/sparql>{\n" +
                    "  ?p vocab:person_personId ?id .\n" +
                    "  ?p vocab:person_birthName ?name .\n" +
                    "  ?ps vocab:hasskill_personId ?id .\n" +
                    "  ?ps vocab:hasskill_skillId ?skill .\n" +
                    "  ?st vocab:hastask_skillId ?skill .\n" +
                    "  ?st vocab:hastask_taskId ?ti .\n" +
                    "  ?t vocab:task_id ?ti .\n" +
                    "  ?t vocab:task_taskItem ?taskItem .\n" +
                    "  ?t vocab:task_qualityGrade ?quality \n" +
                    "}}";
			resultSet = executeQuery(query2);

			TreeMap<String,personTree> personList = new TreeMap<>();

			while(resultSet.hasNext()){
			    QuerySolution sol = resultSet.nextSolution();
			    String id = sol.getLiteral("id").getString();
			    String name = sol.getLiteral("name").getString();
                String taskItem = sol.getLiteral("taskItem").getString();
                int quality = sol.getLiteral("quality").getInt();
			    if(!personList.containsKey(id)){
			        personList.put(id,new personTree(id,sol.getLiteral("name").getString()));
                }

                if(!personList.get(id).taskList.containsKey(taskItem) || personList.get(id).taskList.get(taskItem) < quality){
                    personList.get(id).taskList.put(taskItem,quality);
                }
            }

            ArrayList<String> toRemove = new ArrayList<>();
           for(Map.Entry<String, Float> entry : TaskList.entrySet()){
               for(Map.Entry<String, personTree> entry2 : personList.entrySet()){
                   if(!entry2.getValue().taskList.containsKey(entry.getKey())){
                       toRemove.add(entry2.getKey());
                   }
               }
           }

           for(String s : toRemove){
               personList.remove(s);
           }

           for(Map.Entry<String, personTree> entry2 : personList.entrySet()){
               toRemove = new ArrayList<>();
               for(Map.Entry<String,Integer> entry3 : entry2.getValue().taskList.entrySet()){
                   if(!TaskList.containsKey(entry3.getKey())) {
                       toRemove.add(entry3.getKey());
                   }
               }
               for(String s : toRemove){
                   entry2.getValue().taskList.remove(s);
               }
           }

           ArrayList<Person> personArrayList = new ArrayList<>();
           for(Map.Entry<String, personTree> entry2 : personList.entrySet()){
               ArrayList<Task> t = new ArrayList<>();
               for(Map.Entry<String, Integer> entry3 : entry2.getValue().taskList.entrySet()){
                   t.add(new Task(entry3.getValue(),entry3.getKey()));
               }
               Person p = new Person(t.toArray(new Task[0]), entry2.getValue().name, entry2.getKey());
               personArrayList.add(p);
           }

           ArrayList<Task> task = new ArrayList<>();
           for(Map.Entry<String,Float> entry : TaskList.entrySet()){
               task.add(new Task(entry.getValue(),entry.getKey()));
           }

           Person scores[] = pearsonScore(personArrayList.toArray(new Person[0]),task.toArray(new Task[0]));

	   		return scores;
	   }, gson ::toJson);

	   System.out.println("escuchando en puerto 8008");
    
    }

    //Para cada persona actualizo la distancia.
    public static Person[] pearsonScore(
            Person p[],
            Task y[]
    ) {

        double N = y.length;

        double SumatoriaY = 0;

        //Sumatoria Y.
        for (int i = 0; i < y.length; i++) {
            SumatoriaY = y[i].calificacion + SumatoriaY;
        }



        //Sumatoria Y^2.

        double SumatoriaY2 = 0;
        for (int i = 0; i < y.length; i++) {
            SumatoriaY2 = Math.pow(y[i].calificacion, 2) + SumatoriaY2;
        }
        //System.out.println("Suma Y: "+SumatoriaY);
        //System.out.println("Suma Y2: "+SumatoriaY2);

        for (int i = 0; i < p.length; i++) {

            //Tomo la primera persona y le actualizo su d.

            double SumatoriaXi = 0;
            double SumatoriaXiPorYi = 0;
            double SumatoriaXi2 = 0;
            double distancia = 0;

            //Sumatoria Xi.
            for (int j = 0; j < p[i].tasks.length; j++) {
                SumatoriaXi = SumatoriaXi + p[i].tasks[j].calificacion;

            }

            //Sumatoria Xi^2

            for (int j = 0; j < p[i].tasks.length; j++) {
                SumatoriaXi2 = SumatoriaXi2 + Math.pow(p[i].tasks[j].calificacion,2);

            }

            //Sumatoria Xi*Y.
            for (int j = 0; j < p[i].tasks.length; j++) {

                //System.out.println(p[i].tasks[j].calificacion + " con " + y[j].calificacion);

                SumatoriaXiPorYi = SumatoriaXiPorYi + (p[i].tasks[j].calificacion*y[j].calificacion);
            }

            /*
            System.out.println("Suma Xi: "+SumatoriaXi);
            System.out.println("Suma XY: "+SumatoriaXiPorYi);
            System.out.println("Suma X2: "+SumatoriaXi2);
            */

            double num = SumatoriaXiPorYi - ((SumatoriaXi)*(SumatoriaY)/N);
            //System.out.println(num);
            double denParte1 = Math.sqrt(SumatoriaXi2-(Math.pow(SumatoriaXi,2)/N));
            //System.out.println(denParte1);
            double denParte2 = Math.sqrt(SumatoriaY2-(Math.pow(SumatoriaY,2)/N));
            //System.out.println(denParte2);
            double den = denParte1*denParte2;

            distancia = num/den;

            //System.out.println(distancia);

            p[i].d = (float)(Double.isNaN(distancia) ? 0 : distancia) ;

        }


        return p;

    }



public static ResultSet executeQuery(String queryString) throws Exception {

	 QueryExecution exec =  QueryExecutionFactory.create(QueryFactory.create(queryString), new
			 DatasetImpl(ModelFactory.createDefaultModel()));
	 return exec.execSelect();
	 
}

}
