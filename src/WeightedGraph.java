import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeightedGraph<X> {

	//List of vertices in the graph
	private List<String> theVerts;
	
	//List of the lists of vertices to which edges exist in the graph
	private List<List<String>> adjLists;
	
	//Map from String vertex name to the index in the adjLists which contains its adjList
	//so adjLists.get(vertexMapping.get("g0.0")) would give a List of vertices to which there would be edges
	//from g0.0
	
	private Map <String, Integer> vertexMapping;
	
	//Map from String to Integer
	//format is "fromto" and this is mapped onto corresponding weights
	
	private Map <String, Integer> weights;
	
	//Map from String "fromto" to the list of vertices through which the shortest path between
	//from and to travels
	private Map <String, List<String>> pathMap;
	
	private Map <String, Boolean> edgeExists;
	
	public WeightedGraph (List<String> verts) {
		
		//System.out.println(verts.size());
		theVerts = verts;
		adjLists = new ArrayList<List<String>> ();
		vertexMapping = new HashMap <String, Integer> ();
		weights = new HashMap <String, Integer> ();
		pathMap = new HashMap <String, List<String>> ();
		edgeExists = new HashMap <String, Boolean> ();
		
		//for every vertex in this cell
		//there is a list of adjacency lists. One list for each vertex
		//each adjacency list initially contains nothing, ie. zero vertices so zero edges
		//the indexTracker maps which vertex is in which index in our adjLists List 
		//map this indexTracker with the vertex name in our vertexMapping HashMap
		//then increment 
		
		int indexTracker = 0;
		
		for (String x : verts) {
			
			vertexMapping.put(x,indexTracker);
			List<String> Vlist = new ArrayList<String> ();
			adjLists.add(indexTracker, Vlist);
			indexTracker ++;			
		}
	}
	
	public void addEdge (String from, String to, int weight) {
		
		//if edge doesn't already exist
		
		if (edgeExists.get(from+to) == null || edgeExists.get(from+to) == false) {
			
			//add edge in the adjacency list of from and update the weights Map
			edgeExists.put(from+to, true);
			int index = vertexMapping.get(from);
			List<String> edgesTo = adjLists.get(index);
			edgesTo.add(to);
			weights.put(from+to, weight);
			
			//add edge in the adjacency list of to and update the weights Map
			edgeExists.put(to+from, true);
			index = vertexMapping.get(to);
			edgesTo = adjLists.get(index);
			edgesTo.add(from);
			weights.put(to+from, weight);
		}
		
		else {
			//do nothing
		}
	}
	
	public List<String> getVerts () {
		return theVerts;
	}
	
	public Map<String, Integer> getWeights() {
		return weights;
	}
	
	public Map<String, List<String>> getPathMap () {
		return pathMap;
	}
	
	//return the adjacency list of whichever vertex you are looking for
	public List<String> getAdjList (String vertexName) {
		int index = vertexMapping.get(vertexName);
		List<String> theList = adjLists.get(index);
		return theList;
	}
	
	public List<String> getCorners () {
		List<String> theCorners = theVerts.subList(0, 4);
		return theCorners;
	}
}
