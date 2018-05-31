import org.apache.jena.query.ResultSet;
import virtuoso.jena.driver.*;
import org.apache.jena.query.*;

import static spark.Spark.get;

public class queriesHelper{
	
	
	static ResultSet runQuery(String consulta, String nameOfGraph)
	{
		Query sparql = QueryFactory.create(consulta);
		
		VirtGraph set = new VirtGraph(nameOfGraph,"jdbc:virtuoso://50.18.123.50:1111","dba","dba");
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
	    
	    ResultSet results = vqe.execSelect();
	    
	    return results;
	    
	    
	}

}

