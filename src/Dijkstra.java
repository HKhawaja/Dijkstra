/**
 * An implementation of Dijstra's algorithm
 *
 * @param X the type of values to perform Dijkstra's on
 *
 * @author Harith Khawaja
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dijkstra <X> {

	//source vertex
	private String from;
	
	//list of all the vertices in the graph passed in
	private List<String> vertices;
	
	//map from String "fromto" to length of shortest path
	private Map <String, Integer> shortestPaths;
	
	//Weighted Graph itself
	private WeightedGraph<X> graph;
	
	//get weights of edges
	private Map<String, Integer> weights;
	
	//get shortest paths Map --> map from "fromto" to a list containing intermediate vertices
	private Map<String, List<String>> shortestPathMap;
	
	//create Dijkstra object
	//get the list of vertices in the graph, the weights Map, the shortestPath Map
	//create a shortest Paths HashMap between String "fromto" and length of shortest path between from and to
	//get the ShortestPath Map that is a Mapping of the "fromto" and the list of intermediate vertices on
	//the shortest path between from and to
	
	public Dijkstra (String from, WeightedGraph <X> graph) {
		this.from = from;
		this.graph = graph;
		vertices = graph.getVerts();
		this.weights = graph.getWeights();
		shortestPaths = new HashMap <String, Integer> ();
		shortestPathMap = new HashMap<String, List<String>> ();
		
		if (!vertices.contains(from))
			System.out.println ("The graph doesn't contain the source");
	}
	
	//actually run the Dijkstra algorithm
	public void execute () {
		
		//first initialize the shortest paths from source to all other vertices
		//this means all shortest paths fromto are infinity (kind of)
		//and all shortestPathMaps should basically be empty
		
		for (String vertex: vertices) {
			shortestPaths.put(from+vertex, Integer.MAX_VALUE);
			
			List<String> thePath = new ArrayList<String> ();
			shortestPathMap.put(from+vertex, thePath);
		}
		
		//but shortest Path form source to source vertex is always zero
		shortestPaths.put(from+from, 0);
		
		//now build the Priority Queue for all the vertices in the graph
		//first instantiate the queue
		//then add each vertex to the queue with the priority basically being the Mapping in our
		//shortestPaths HashMap
		
		PriorityQueue <String, Integer> DijkstraQ = new PriorityQueue <String, Integer> ();
		for (String vertex: vertices) {
			
			int priority = shortestPaths.get(from+vertex);
			DijkstraQ.addItem(vertex, priority);
		}
		
		//now actually iterate through all the items in the priority Queue
		//our infamous while not empty loop
		//at each iteration remove the minimum element and get its adjacency list
		//then for each vertex to which there is an edge
		//check if that vertex is part of the Priority Queue
		//and update shortestPaths if the new total is lesser
		//also update ShortestPathsMap
		
		while (DijkstraQ.getSize() != 0) {
			
			//extract min
			String vertexToConsider = DijkstraQ.removeItem();
			
			//get adjacency list
			List<String> adjacencyList = graph.getAdjList(vertexToConsider);
			
			//for all members in the adjacency list
			for (String x: adjacencyList) {
				
				//first get weight of the edge
				int edgeWeight = weights.get(vertexToConsider+x);
				
				//then decrease key. if not lesser, then won't decrease key. if not present in queue
				//then won't do anything
				//get distance from source to extracted vertex
				
				int s = shortestPaths.get(from+vertexToConsider);
				
				boolean decreased = DijkstraQ.decreasePriority(x, s+edgeWeight);
				
				if (decreased) {
					shortestPaths.put(from+x, s+edgeWeight);
					List<String> path = shortestPathMap.get(from+vertexToConsider);
					
					List<String> pathCopy = new ArrayList<String> ();
					
					for (int i = 0; i<path.size(); i++) {
						pathCopy.add(path.get(i));
					}
					
					pathCopy.add(vertexToConsider);
					shortestPathMap.put(from+x, pathCopy);
				}
			}
		}		
		
	}
	
	public int getShortestPathLength (String to) {
		if (shortestPaths.get(from+to) != null)
			return shortestPaths.get(from+to);
		else {
			System.out.println("There is an error");
			return -1;
		}
	}	
	
	public List<String> getActualShortestPath (String to) {
		return shortestPathMap.get(from+to);
	}
	
	public String getSource() {
		return from;
	}
	
	public void update (String from, String to, Dijkstra<String> compareObject) {
		
		//get the shortest paths "fromto"
		int first = shortestPaths.get(from+to);
		int second = compareObject.shortestPaths.get(from+to);
		
		//if first is shorter, update compareObject's shortestPaths HashMap
		//also update the actual shortest path
		
		if (first <= second) {
			compareObject.shortestPaths.put(from+to, first);
			compareObject.shortestPathMap.put(from+to, shortestPathMap.get(from+to));
		}
		
		//otherwise update this instance's shortestPathsMap
		else {
			shortestPaths.put(from+to, second);
			shortestPathMap.put(from+to, compareObject.shortestPathMap.get(from+to));
		}
	}
}
