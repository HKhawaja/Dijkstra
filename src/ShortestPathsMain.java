/**
 *
 * @author Harith Khawaja
 */

import java.util.*;

public class ShortestPathsMain
{
    public static void main(String[] args)
    {
	Scanner in = new Scanner(System.in);

	// read the first line with the dimensions of the grid
	int width = in.nextInt();
	int height = in.nextInt();
	int n = in.nextInt();

	// THIS WILL MAKE A ARRAY (okay, a list of lists since Java won't allow
	// arrays of generics) OF GRAPHS FOR THE INDIVIDUAL CELLS --
	// g.get(r).get(c) IS THE GRAPH FOR THE CELL IN ROW r COLUMN c

	// make an empty graph for each cell
	//making a list of a list of weighted graphs
	//creating the actual grid and graphs
	List<List<WeightedGraph<String>>> g = new ArrayList<List<WeightedGraph<String>>>();
	
	for (int r = 0; r < height; r++)
	    {
		List<WeightedGraph<String>> row = new ArrayList<WeightedGraph<String>>();
		for (int c = 0; c < width; c++)
		    {
			// make the list of vertices in this cell starting
			// with the corners...
			List<String> verts = new ArrayList<String>();
			verts.add("g" + r + "." + c); // upper left
			verts.add("g" + (r + 1) + "." + c); // lower left
			verts.add("g" + r + "." + (c + 1)); // upper right
			verts.add("g" + (r + 1) + "." + (c + 1)); // lower right

			//...then the interior vertices
			for (int k = 0; k < n; k++)
			    {
				verts.add("v" + r + "." + c + "." + k);
			    }

			//create a new graph of those vertices
			// add that graph!
			row.add(new WeightedGraph<String>(verts));
		    }
		g.add(row);
	    }

	// loop over edges to add
	
	String from;
	while (!(from = in.next()).equals("queries"))
	    {
		String to = in.next();
		int w = in.nextInt();
		
		// the to vertex is always in the interior of the cell
		//if not, throws an error
		assert to.charAt(0) == 'v';

		// figure out from the to vertex which cell we're in
		StringTokenizer tok = new StringTokenizer(to.substring(1), ".");	
		int r = Integer.parseInt(tok.nextToken());
		int c = Integer.parseInt(tok.nextToken());
		
		// add the edge to the correct cell
		g.get(r).get(c).addEdge(from, to, w);
	    }

	// MAKE YOUR CORNER GRAPH HERE (might want the ability to label edges with paths
	// they represent)
	
	//first run Dijkstra's on each cell from each corner vertex
	//we need to ensure we keep these Dijkstra objects safe and reaccessible
	//so we store each Dijkstra object in an Arraylist that corresponds to the cell it is referencing
	
	List<List<List<Dijkstra<String>>>> cornerDijkstras = new ArrayList<List<List<Dijkstra<String>>>> ();
	
	//iterate through each graph
	for (int row = 0; row < height; row ++) {
		
		List<List<Dijkstra<String>>> rowList = new ArrayList<List<Dijkstra<String>>> ();
		
		for (int col = 0; col<width; col++) {
			
			List<Dijkstra<String>> colList = new ArrayList <Dijkstra<String>> ();
			
			//since the corners were always created in the order UL, LL, UR, LR
			//List<Dijkstra<String>>.get(x) will work from 0 to 3 with these mappings
			
			WeightedGraph<String> graph = g.get(row).get(col);
			List<String> corners = graph.getCorners();
			
			
			for (String x: corners) {
				Dijkstra<String> here = new Dijkstra<String> (x, graph);
				here.execute();
				colList.add(here);
			}
			
			rowList.add(colList);
		}
		
		cornerDijkstras.add(rowList);
	}
	
	//but some min paths must be reassessed
	//so we iterate through all the cornerDijkstras
	//and figure out which corner to corner edges need re-updating
	
	
	for (int row = 0; row < height; row ++) {
		
		for (int col = 0; col < width; col++) {
			
			for (int d = 0; d<4; d++) {
				
				//get the actual Dijkstra Object and check what the source vertex is				
				Dijkstra<String> dObject = cornerDijkstras.get(row).get(col).get(d);
				String source = dObject.getSource();
				
				StringTokenizer tok = new StringTokenizer (source.substring(1), ".");
				
				int getRow = Integer.parseInt(tok.nextToken());
				int getCol = Integer.parseInt(tok.nextToken());
				
				//if in topmost row
				if (getRow == 0) {
					
					//if the top left or top right of grid then do nothing
					//because there can be no overlapping paths to other corners 
					//using adjacent cells
					
					if (getCol == 0 || getCol == width) {
						continue;
					}
					
					//if in a middle column in the topmost row, update only a specific path
					//update shortestPath to a corner exactly 1 row greater using current graph
					//and another graph that is exactly 1 column to the right
					
					else {
						
						if (isUpperRight(getRow, getCol, row, col)) {
 						
							//trying to access the Dijkstra Object with the same source of a cell one column to
							//the right
							//the vertex to which we are updating the shortest path
							
							String to = "g" + (getRow+1) + "." + getCol;
							
							//pass in the actual coordinates of the from corner vertex
							//and the coordinates of the cell of the adjacent cell 
							//this will check to tell if the vertex is UL, LL, UR, LR in the adjacent cell
							//and accordingly return an index for the List<Dijkstra<String>> of the cell
							
							int index = returnIndex (getRow, getCol, row, col+1);
							Dijkstra<String> compareObject = cornerDijkstras.get(row).get(col+1).get(index);
							dObject.update(source, to, compareObject);
						}
					}
				}
				
				//if in bottom most row
				else if (getRow == height) {
					
					//if the bottom left or bottom right of grid then do nothing
					//because there can be no overlapping paths to other corners
					//using adjacent cells
					
					if (getCol == 0 || getCol == width) {
						continue;
					}
					
					//if in the middle column, update only a specific shortest path
					//update path to a corner that is exactly 1 row lesser using current graph
					//and another graph that is exactly 1 column to the right
					else {
						
						if (isLowerRight (getRow, getCol, row, col)) {
							
							String to = "g" + (getRow-1) + "." + getCol;
							int index = returnIndex (getRow, getCol, row, col+1);
							Dijkstra<String> compareObject = cornerDijkstras.get(row).get(col+1).get(index);
							dObject.update(source, to, compareObject);
							
						}
					}
				}
				
				//if in a middle row
				else {
					
					//if in the left most column, update only a specific shortest path
					//update shortest path to a corner vertex exactly 1 column greater using 
					//current graph and another graph that is exactly 1 row greater
					//and then update the shortest path b/w corner vertices in BOTH these graphs
					
					if (getCol == 0) {
						
						if (isLowerLeft (getRow, getCol, row, col)) {
							
							String to = "g" + getRow + "." + (getCol+1);
							int index = returnIndex (getRow, getCol, row+1, col);
							Dijkstra<String> compareObject = cornerDijkstras.get(row+1).get(col).get(index);
							dObject.update(source, to, compareObject);
						}
					}
					
					//if in the right most column, update only a specific shortest path
					//update shortest path to corner vertex exactly 1 column lesser using
					//current graph and another graph that is exactly 1 row greater
					//and then update the shortest path b/w corner vertices in BOTH these graphs
					
					else if (getCol == width) {
						
						if (isLowerRight (getRow, getCol, row, col)) {
							
							String to = "g" + getRow + "." + (getCol-1);
							int index = returnIndex (getRow, getCol, row+1, col);
							Dijkstra<String> compareObject = cornerDijkstras.get(row+1).get(col).get(index);
							dObject.update(source, to, compareObject);
						}
					}
					
					//if in a middle column, update multiple edges
					//this is because it is in a middle row, middle column
					//exactly which shortest paths to update will depend on whether the vertex in
					//consideration is UL, LL, UR or LR
						
					else {
						
						//if upper left, update
						//1.shortest path to corner vertex exactly 1 column greater
						//using current graph and graph exactly 1 row above
						//2. shortest path to corner vertex exactly 1 row greater
						//using current graph and graph exactly 1 column to the left
						if (isUpperLeft(getRow, getCol, row, col)) {
							
							//1.
							String to = "g" + getRow + "." + (getCol +1);
							int index = returnIndex (getRow, getCol, row-1, col);
							Dijkstra<String> compareObject = cornerDijkstras.get(row-1).get(col).get(index);
							dObject.update(source, to, compareObject);
							
							//2.
							to = "g" + (getRow+1) + "." + getCol;
							index = returnIndex (getRow, getCol, row, col-1);
							compareObject = cornerDijkstras.get(row).get(col-1).get(index);
							dObject.update(source, to, compareObject);
						}
						
						//if lower left, update
						//1. shortest path to corner vertex exactly 1 row lesser using current graph
						//and graph exactly 1 column to the left
						//2. shortest path to corner vertex exactly 1 column greater using current graph
						//and graph exactly 1 row greater
						
						else if (isLowerLeft(getRow, getCol, row, col)) {
							
							//1.
							String to = "g" + (getRow-1) + "." + getCol;
							int index = returnIndex (getRow, getCol, row, col-1);
							Dijkstra<String> compareObject = cornerDijkstras.get(row).get(col-1).get(index);
							dObject.update (source, to, compareObject);
							
							//2. 
							to = "g" + getRow + "." + (getCol+1);
							index = returnIndex (getRow, getCol, row+1, col);
							compareObject = cornerDijkstras.get(row+1).get(col).get(index);
							dObject.update(source, to, compareObject);
							
						}
						
						
						//if upper right, update
						//1. shortest path to corner vertex exactly 1 column to the left using current
						//graph and graph exactly 1 row lesser
						//2. shortest path to corner vertex exactly 1 row greater using current graph 
						//and graph exactly 1 column to the right
						else if (isUpperRight (getRow, getCol, row, col)) {
							
							//1.
							String to = "g" + getRow + "." + (getCol-1);
							int index = returnIndex (getRow, getCol, row-1, col);
							Dijkstra<String> compareObject = cornerDijkstras.get(row-1).get(col).get(index);
							dObject.update(source, to, compareObject);
							
							//2.
							to = "g" + (getRow+1) + "." + getCol;
							index = returnIndex (getRow, getCol, row, col+1);
							compareObject = cornerDijkstras.get(row).get(col+1).get(index);
							dObject.update(source, to, compareObject);
							
							
						}
						
						
						//other wise it it a lower right corner, so update
						//1. shortest path to corner vertex exactly 1 row above using current graph and
						//graph exactly 1 column to the right
						//2. shortest path to corner vertex exactly 1 column less using current graph and 
						//graph exactly 1 row greater
						
						else if (isLowerRight(getRow, getCol, row, col)){
							
							//1.
							String to = "g" + (getRow - 1) + "." + getCol;
							int index = returnIndex (getRow, getCol, row, col+1);
							Dijkstra<String> compareObject = cornerDijkstras.get(row).get(col+1).get(index);
							dObject.update(source, to, compareObject);
							
							//2.
							to = "g" + getRow + "." + (getCol -1);
							index = returnIndex (getRow, getCol, row +1, col);
							compareObject = cornerDijkstras.get(row+1).get(col).get(index);
							dObject.update(source, to, compareObject);
 						}
						
						else {
							System.out.println("Something wrong, vertex not identified as either");
						}
					}
				}
			}
		}
	}
	
	//now all our Dijkstra objects (source, cell) are updated with shortest path weights
	//now we run Dijkstra only on our corners
	//for this we will need to create a new Graph st. shortestpaths between corners are considered 'edges'
	
	List<String> cornerVertices = new ArrayList<String>	();
	
	for (int row = 0; row <= height; row++) {
		
		for (int col = 0; col <= width; col++) {
			
			String vert = "g" + row + "." + col;
			cornerVertices.add(vert);
		}
	}
	
	//create the cornerGraph with vertices
	WeightedGraph<String> cornerGraph = new WeightedGraph<String> (cornerVertices);
	
	//add all the "edges" to the cornerGraph
	for (int row = 0; row < height; row++) {
		
		for (int col = 0; col<width; col++) {
			
			for (int d = 0; d<4; d++) {
				
				Dijkstra<String> vertex = cornerDijkstras.get(row).get(col).get(d);
				String source = vertex.getSource();
				
				List<String> otherThreeVertices = generateOtherThreeCorners (source,row, col);
				for (String x: otherThreeVertices) {
					cornerGraph.addEdge(source, x, vertex.getShortestPathLength(x));
				}
				
			}
		}
	}
	
	//create a HashMap from each corner vertex to its respective Dijkstra object
	HashMap <String, Dijkstra<String>> cornerGraphDijkstras = new HashMap <String, Dijkstra<String>> ();
	
	for (String x: cornerVertices) {
		
		Dijkstra<String> cornerDijkstra = new Dijkstra<String> (x,cornerGraph);
		cornerDijkstra.execute();
		cornerGraphDijkstras.put(x, cornerDijkstra);
		
	}

	System.out.println();
	
	// process the queries
	while (in.hasNext())
	    {
		from = in.next();
		String to = in.next();

		// determine what cells we're in
		StringTokenizer tok = new StringTokenizer(from.substring(1), ".");
		int fromR = Integer.parseInt(tok.nextToken());
		int fromC = Integer.parseInt(tok.nextToken());

		tok = new StringTokenizer(to.substring(1), ".");
		int toR = Integer.parseInt(tok.nextToken());
		int toC = Integer.parseInt(tok.nextToken());
		
		String[] fromCorners = {"g" + fromR + "." + fromC,
					"g" + (fromR + 1) + "." + fromC,
					"g" + fromR + "." + (fromC + 1),
					"g" + (fromR + 1) + "." + (fromC + 1)};
		String[] toCorners = {"g" + toR + "." + toC,
				      "g" + (toR + 1) + "." + toC,
				      "g" + toR + "." + (toC + 1),
				      "g" + (toR + 1) + "." + (toC + 1)};

		// COMPUTE THE SHORTEST PATHS FROM from AND to TO THEIR CORNERS AND PERHAPS
		// UPDATE THE CORNER GRAPH ACCORDINGLY (existing Graph needs to know all
		// vertices when it is created; need some way to deal with that or an addVertex
		// method)
		
		//run Dijkstra's algorithm on each of the from and to vertices in their respective graph cells
		WeightedGraph <String> fromGraph = g.get(fromR).get(fromC);
		Dijkstra <String> fromDijkstra = new Dijkstra <String> (from, fromGraph);
		fromDijkstra.execute();
				
		WeightedGraph <String> toGraph = g.get(toR).get(toC);
		Dijkstra <String> toDijkstra = new Dijkstra <String> (to, toGraph);
		toDijkstra.execute();
		
		//then take the shortest paths: "from" vertex to all corners and match up with shortest path
		//"to" vertex to all corners
		//and evaluate all possibilities
		//then select the shortest path.
		
		int minLength = Integer.MAX_VALUE;
		String shortestFromCorner = null;
		String shortestToCorner = null;
		
		for (String corner: fromCorners) {
			
			int fromToCorner = fromDijkstra.getShortestPathLength(corner);
			
			Dijkstra<String> fromCorner = cornerGraphDijkstras.get(corner);

			for (String corner2: toCorners) {
				
				int toToCorner = toDijkstra.getShortestPathLength(corner2);
				
				int intermediate = fromCorner.getShortestPathLength(corner2);
				
				//concatenate the paths and compare to our minimum
				//update if we found a shorter path
				//still need to add distance between corner vertices here !!!
				
				if (fromToCorner + toToCorner + intermediate < minLength) { 
					minLength = fromToCorner + toToCorner + intermediate;
					shortestFromCorner = corner;
					shortestToCorner = corner2;
				}
					
			}
		}

		// RECONSTRUCT COMPLETE PATH FROM THE PATH OF CORNERS
		
		System.out.print(minLength + " ");
		
		
		List<String> path1 = fromDijkstra.getActualShortestPath(shortestFromCorner);
		List<String> path2 = toDijkstra.getActualShortestPath(shortestToCorner);
		List<String> intermPath = new ArrayList<String> ();
		
		//just a list of g's to g's
		List<String> path3 = cornerGraphDijkstras.get(shortestFromCorner).getActualShortestPath(shortestToCorner);

		List<String> finalPath = new ArrayList<String> ();
		finalPath.add(from);
		finalPath.addAll(path1);
		
		intermPath.add(shortestFromCorner);
		intermPath.addAll(path3);
		intermPath.add(shortestToCorner);
		
		removeConsecutiveReps (intermPath);
		
		//System.out.println(intermPath.size());
		
		//this part constructs actual shortest intermediate paths
		for (int i = 0; i < intermPath.size()-1; i++) {

			String cellDeterminer = finalPath.get(finalPath.size() -1);
			tok = new StringTokenizer (cellDeterminer.substring(1), ".");
			fromR = Integer.parseInt(tok.nextToken());
			fromC = Integer.parseInt(tok.nextToken());
			//System.out.println(cellDeterminer);
			//System.out.println(fromR);
			finalPath.add(intermPath.get(i));
			
			String fromCorner = intermPath.get(i);
			//System.out.println("fromCorner" + fromCorner);
			tok = new StringTokenizer (fromCorner.substring(1), ".");
			int getRow = Integer.parseInt(tok.nextToken());
			int getCol = Integer.parseInt(tok.nextToken());
					
			String destinationCorner = intermPath.get(i+1);
			//System.out.println ("destinationCorner" + destinationCorner);
			
			//if fromCorner --> destinationCorner lies within this cell
			
			if (within(fromCorner, destinationCorner, fromR, fromC)) {
				int index = returnIndex (getRow, getCol, fromR, fromC);
				//System.out.println(fromR);
				Dijkstra<String> source = cornerDijkstras.get(fromR).get(fromC).get(index);
				finalPath.addAll(source.getActualShortestPath(destinationCorner));
				
				//System.out.println("fine!");
			}
			
			//if fromCorner --> destinationCorner lies above this cell
			else if ( above(fromCorner, destinationCorner, fromR, fromC)) {
				int index = returnIndex (getRow, getCol, fromR-1, fromC);
				Dijkstra<String> source = cornerDijkstras.get(fromR-1).get(fromC).get(index);
				finalPath.addAll(source.getActualShortestPath(destinationCorner));
			}
			
			//if fromCorner ---> destinationCorner lies below this cell
			else if ( below(fromCorner, destinationCorner, fromR, fromC)) {
				int index = returnIndex (getRow, getCol, fromR+1, fromC);
				Dijkstra<String> source = cornerDijkstras.get(fromR+1).get(fromC).get(index);
				finalPath.addAll(source.getActualShortestPath(destinationCorner));
			}
			
			else if (left(fromCorner, destinationCorner, fromR, fromC)) {
				int index = returnIndex (getRow, getCol, fromR, fromC-1);
				Dijkstra<String> source = cornerDijkstras.get(fromR).get(fromC-1).get(index);
				finalPath.addAll(source.getActualShortestPath(destinationCorner));
			}
			
			else if (right (fromCorner, destinationCorner, fromR, fromC)) {
				int index = returnIndex (getRow, getCol, fromR, fromC +1);
				Dijkstra<String> source = cornerDijkstras.get(fromR).get(fromC+1).get(index);
				finalPath.addAll(source.getActualShortestPath(destinationCorner));
			}
			
			else if (northWest (fromCorner, destinationCorner, fromR, fromC)) {
				int index = returnIndex (getRow, getCol, fromR-1, fromC-1);
				Dijkstra<String> source = cornerDijkstras.get(fromR-1).get(fromC-1).get(index);
				finalPath.addAll(source.getActualShortestPath(destinationCorner));
			}
			
			else if (northEast (fromCorner, destinationCorner, fromR, fromC)) {
				int index = returnIndex (getRow, getCol, fromR -1, fromC+1);
				Dijkstra<String> source = cornerDijkstras.get(fromR-1).get(fromC+1).get(index);
				finalPath.addAll(source.getActualShortestPath(destinationCorner));
			}
			
			else if (southWest (fromCorner, destinationCorner, fromR, fromC)) {
				int index = returnIndex (getRow, getCol, fromR+1, fromC-1);
				Dijkstra<String> source = cornerDijkstras.get(fromR+1).get(fromC-1).get(index);
				finalPath.addAll(source.getActualShortestPath(destinationCorner));
			}
			
			else if (southEast (fromCorner, destinationCorner, fromR, fromC)) {
				int index = returnIndex (getRow, getCol, fromR +1, fromC +1);
				Dijkstra<String> source = cornerDijkstras.get(fromR+1).get(fromC+1).get(index);
				finalPath.addAll(source.getActualShortestPath(destinationCorner));
			}
			
			else {
				System.out.println("Couldn't assign destination!");
			}
		}
		
		finalPath.add(shortestToCorner);
				
		for (int i = path2.size()-1; i>=0; i--) {
			finalPath.add(path2.get(i));
		}
		
		finalPath.add(to);
		removeConsecutiveReps(finalPath);
		
		/*
		//now go through all the final path to make sure no consecutive repetitions
		String firstNew = finalPath.get(0);
		
		for (int i = 1; i<finalPath.size(); i++) {
			if (finalPath.get(i).equals(firstNew)) {
					finalPath.set(i,"");
			}
			else {
				firstNew = finalPath.get(i);
			}
		}
		
		for (int i = 0; i<finalPath.size(); i++) {
			if (finalPath.get(i).equals("")) {
				int j = i - 1;
				finalPath.remove(i);
				i = j;
			}
		}
		*/
		/*
		 * for (int j = i+1; j<finalPath.size(); j++) {
						if (finalPath.get(j).equals(finalPath.get(i)))
							finalPath.remove(j);
					}
		 */
		
		//now go through all the 
		System.out.println(finalPath);
		
		// UNDO THE UPDATES (maybe add removeVertex and/or removeEdge to WeightedGraph)
	    }
    }
    
    public static void removeConsecutiveReps (List<String> finalPath) {
    	
    		String firstNew = finalPath.get(0);
		
			for (int i = 1; i<finalPath.size(); i++) {
				if (finalPath.get(i).equals(firstNew)) {
					finalPath.set(i,"");
				}
				
				else {
					firstNew = finalPath.get(i);
				}
			}
		
			for (int i = 0; i<finalPath.size(); i++) {
				if (finalPath.get(i).equals("")) {
					int j = i - 1;
					finalPath.remove(i);
					i = j;
				}
			}
    }
    
    public static boolean southEast (String fromCorner, String destinationCorner, int fromR, int fromC) {
    
    	StringTokenizer tokenMe = new StringTokenizer (fromCorner.substring(1), ".");
    	int fRow = Integer.parseInt(tokenMe.nextToken());
    	int fCol = Integer.parseInt(tokenMe.nextToken());
    	
    	tokenMe = new StringTokenizer (destinationCorner.substring(1), ".");
    	int tRow = Integer.parseInt(tokenMe.nextToken());
    	int tCol = Integer.parseInt (tokenMe.nextToken());
    
    	boolean isSouthEast = false;
    	
    	if (isLowerRight (fRow, fCol, fromR, fromC)) {
    		if (tRow == fRow+1 && tCol == fCol+1) 
    			isSouthEast = true;
    	}
    	
    	else {
    		//do nothing
    	}
    	
    	return isSouthEast;
    }
    
    public static boolean southWest (String fromCorner, String destinationCorner, int fromR, int fromC) {
    
    	StringTokenizer tokenMe = new StringTokenizer (fromCorner.substring(1), ".");
    	int fRow = Integer.parseInt(tokenMe.nextToken());
    	int fCol = Integer.parseInt(tokenMe.nextToken());
    	
    	tokenMe = new StringTokenizer (destinationCorner.substring(1), ".");
    	int tRow = Integer.parseInt(tokenMe.nextToken());
    	int tCol = Integer.parseInt (tokenMe.nextToken());
    	
    	boolean isSouthWest = false;
    	
    	if (isLowerLeft (fRow, fCol, fromR, fromC)) {
    		if (tRow == fRow+1 && tCol == fCol-1)
    			isSouthWest = true;
    	}
    	
    	else {
    		//do nothing
    	}
    	
    	return isSouthWest;
    }
    
    
    public static boolean northEast (String fromCorner, String destinationCorner, int fromR, int fromC) {
    	StringTokenizer tokenMe = new StringTokenizer (fromCorner.substring(1), ".");
    	int fRow = Integer.parseInt(tokenMe.nextToken());
    	int fCol = Integer.parseInt(tokenMe.nextToken());
    	
    	tokenMe = new StringTokenizer (destinationCorner.substring(1), ".");
    	int tRow = Integer.parseInt(tokenMe.nextToken());
    	int tCol = Integer.parseInt (tokenMe.nextToken());
    	
    	boolean isNorthEast = false;
    	
    	if (isUpperRight(fRow,fCol,fromR,fromC)) {
    		if (tRow == fRow -1 && tCol == fCol+1)
    			isNorthEast = true;
    	}
    	
    	else {
    		//do nothing
    	}
    	
    	return isNorthEast;
    }
    
    public static boolean northWest (String fromCorner, String destinationCorner, int fromR, int fromC) {
    	
    	StringTokenizer tokenMe = new StringTokenizer (fromCorner.substring(1), ".");
    	int fRow = Integer.parseInt(tokenMe.nextToken());
    	int fCol = Integer.parseInt(tokenMe.nextToken());
    	
    	tokenMe = new StringTokenizer (destinationCorner.substring(1), ".");
    	int tRow = Integer.parseInt(tokenMe.nextToken());
    	int tCol = Integer.parseInt (tokenMe.nextToken());
    
    	boolean isNorthWest = false;
    	
    	if (isUpperLeft(fRow, fCol, fromR, fromC)) {
    		if ((tRow == fRow-1 && tCol== fCol-1))
    			isNorthWest = true;
    	}
    	
    	else{
    		//do nothing
    	}
    	
    	return isNorthWest;
    }
    
    
    
    public static boolean right (String fromCorner, String destinationCorner, int fromR, int fromC) {
    	StringTokenizer tokenMe = new StringTokenizer (fromCorner.substring(1), ".");
    	int fRow = Integer.parseInt(tokenMe.nextToken());
    	int fCol = Integer.parseInt(tokenMe.nextToken());
    	
    	tokenMe = new StringTokenizer (destinationCorner.substring(1), ".");
    	int tRow = Integer.parseInt(tokenMe.nextToken());
    	int tCol = Integer.parseInt (tokenMe.nextToken());
    	
    	boolean isRight = false;
    	
    	if (isUpperRight (fRow, fCol, fromR, fromC)) {
    		if ((tRow == fRow && tCol == fCol +1) || (tRow == fRow+1 && tCol == fCol +1)) 
    			isRight = true;
    	}
    	
    	else if (isLowerRight (fRow, fCol, fromR, fromC)) {
    		if ((tRow==fRow && tCol ==fCol +1) || (tRow == fRow-1 && tCol == fCol +1))
    			isRight = true;
    	}
    	
    	else {
    		//do nothing
    	}
    	
    	return isRight;
    }
    public static boolean left (String fromCorner, String destinationCorner, int fromR, int fromC) {
    	
    	StringTokenizer tokenMe = new StringTokenizer (fromCorner.substring(1), ".");
    	int fRow = Integer.parseInt(tokenMe.nextToken());
    	int fCol = Integer.parseInt(tokenMe.nextToken());
    	
    	tokenMe = new StringTokenizer (destinationCorner.substring(1), ".");
    	int tRow = Integer.parseInt(tokenMe.nextToken());
    	int tCol = Integer.parseInt (tokenMe.nextToken());
    	
    	boolean isLeft = false;
    	
    	if (isUpperLeft (fRow, fCol, fromR, fromC)) {
    		if ((tRow == fRow && tCol == fCol-1) || (tRow == fRow+1 && tCol == fCol-1))
    			isLeft = true;
    	}
    	
    	else if (isLowerLeft (fRow, fCol, fromR, fromC)) {
    		if ((tRow == fRow && tCol == fCol-1) || (tRow == fRow -1 && tCol == fCol-1))
    			isLeft = true;
    	}
    	
    	else {
    		//do nothing
    	}
    	
    	return isLeft;
    }
    
    public static boolean below (String fromCorner, String destinationCorner, int fromR, int fromC) {
    	
    	StringTokenizer tokenMe = new StringTokenizer (fromCorner.substring(1), ".");
    	int fRow = Integer.parseInt(tokenMe.nextToken());
    	int fCol = Integer.parseInt(tokenMe.nextToken());
    	
    	tokenMe = new StringTokenizer (destinationCorner.substring(1), ".");
    	int tRow = Integer.parseInt(tokenMe.nextToken());
    	int tCol = Integer.parseInt (tokenMe.nextToken());
    	
    	boolean isBelow = false;
    	
    	if (isLowerLeft (fRow, fCol, fromR, fromC)) {
    		if ((tRow == fRow+1 && tCol == fCol) || (tRow == fRow +1 && tCol == fCol+1))
    			isBelow = true;
    	}
    	
    	else if (isLowerRight (fRow, fCol, fromR, fromC)) {
    		if ((tRow == fRow +1 && tCol == fCol) || (tRow == fRow +1 && tCol == fCol-1))
    			isBelow = true;
    	}
    	
    	else{
    		//do nothing
    	}
    	
    	return isBelow;
    }
    
    public static boolean above (String fromCorner, String destinationCorner, int fromR, int fromC) {
    	
    	StringTokenizer tokenMe = new StringTokenizer (fromCorner.substring(1), ".");
    	int fRow = Integer.parseInt(tokenMe.nextToken());
    	int fCol = Integer.parseInt(tokenMe.nextToken());
    	
    	tokenMe = new StringTokenizer (destinationCorner.substring(1), ".");
    	int tRow = Integer.parseInt(tokenMe.nextToken());
    	int tCol = Integer.parseInt (tokenMe.nextToken());
    	
    	boolean isAbove = false;
    	
    	if (isUpperLeft (fRow, fCol, fromR, fromC)) {
    		if ((tRow == fRow-1 && tCol == fCol) || (tRow == fRow -1 && tCol == fCol+1))
    			isAbove = true;
    	}
    	
    	else if (isUpperRight (fRow,fCol, fromR, fromC)) {
    		if ((tRow == fRow-1 && tCol == fCol) || (tRow==fRow-1 && tCol == fCol-1)) {
    			isAbove = true;
    		}
    	}
    	else {
    		//do nothing
    	}
    	
    	return isAbove;
    }
    //takes in source corner, destination corner and the row and col of the cell we are in
    //and tells us whether the edge is internal to the cell or not
    
    public static boolean within (String fromCorner, String destinationCorner, int fromR, int fromC) {
    	StringTokenizer tokenMe = new StringTokenizer (fromCorner.substring(1), ".");
    	int fRow = Integer.parseInt(tokenMe.nextToken());
    	int fCol = Integer.parseInt(tokenMe.nextToken());
    	
    	tokenMe = new StringTokenizer (destinationCorner.substring(1), ".");
    	int tRow = Integer.parseInt(tokenMe.nextToken());
    	int tCol = Integer.parseInt (tokenMe.nextToken());
    	
    	boolean isWithin = false;
    	
    	if (isUpperLeft(fRow, fCol, fromR, fromC)) {
    		if ((fRow == tRow && tCol == fCol+1) || (tRow == fRow+1 && tCol == fCol) || (tRow==fRow+1 && 
    			tCol == fCol+1)) {
    			isWithin = true;
    		}
    	}
    	else if (isLowerLeft (fRow, fCol, fromR, fromC)) {
    		if ((tRow == fRow-1 && tCol == fCol) || (tRow==fRow && tCol==fCol+1) || (tRow==fRow-1 && 
    			tCol == fCol+1))
    			isWithin = true;
    	}
    	
    	else if (isUpperRight (fRow, fCol, fromR, fromC)) {
    		if ((tRow==fRow && tCol == fCol-1) || (tRow == fRow+1 && tCol == fCol) || (tRow==fRow+1 &&
    			tCol == fCol-1))
    			isWithin = true;
    	}
    	
    	else if (isLowerRight (fRow, fCol, fromR, fromC)) {
    		if ((tRow==fRow && tCol == fCol-1) || (tRow==fRow-1 && tCol == fCol) || (tRow == fRow-1 &&
    			tCol == fCol -1))
    			isWithin = true;
    	}
    	
    	else {
    		//System.out.println("not identified as either!");
    	}
    	
    	return isWithin;
    }
    
    public static boolean isUpperLeft (int getRow, int getCol, int row, int col) {
    	return getRow == row && getCol == col;
    }
    
    public static boolean isUpperRight (int getRow, int getCol, int row, int col) {
    	return getRow == row && getCol == (col + 1);
    }
    
    public static boolean isLowerLeft (int getRow, int getCol, int row, int col) {
    	return getRow == (row+1) && getCol == col;
    }
    
    public static boolean isLowerRight (int getRow, int getCol, int row, int col) {
    	return getRow == (row+1) && getCol == (col+1);
    }
    
    public static int returnIndex (int getRow, int getCol, int row, int col) {
    	if (isUpperLeft(getRow, getCol, row, col))
    		return 0;
    	else if (isLowerLeft(getRow, getCol, row, col))
    		return 1;
    	else if (isUpperRight(getRow, getCol, row, col))
    		return 2;
    	else if (isLowerRight(getRow, getCol, row, col))
    		return 3;
    	else {
    		System.out.println("Something wrong");
    		return -1;
    	}
    }
    
    //takes in a source vertex and whichever cell the source vertex resides in and returns 
    //a list of the other three corners
    public static List<String> generateOtherThreeCorners (String source, int row, int col) {
    	
    	List<String> otherThree = new ArrayList<String> ();
    	StringTokenizer tok = new StringTokenizer (source.substring(1), ".");
    	int getRow = Integer.parseInt(tok.nextToken());
    	int getCol = Integer.parseInt(tok.nextToken());
    	
    	if (isUpperLeft (getRow, getCol, row, col)) {
    		String upperRight = "g" + getRow + "." + (getCol +1);
    		String lowerRight = "g" + (getRow +1) + "." + (getCol+1);
    		String lowerLeft = "g" + (getRow + 1) + "." + getCol;
    		
    		otherThree.add(upperRight);
    		otherThree.add(lowerRight);
    		otherThree.add(lowerLeft);
    	}
    	
    	else if (isLowerLeft (getRow, getCol, row, col)) {
    		String upperLeft = "g" + (getRow - 1) + "." + getCol;
    		String upperRight = "g" + (getRow -1) + "." + (getCol +1);
    		String lowerRight = "g" + getRow + "." + (getCol +1);
    		
    		otherThree.add(upperLeft);
    		otherThree.add(upperRight);
    		otherThree.add(lowerRight);
    	}
    	
    	else if (isUpperRight(getRow, getCol, row, col)) {
    		String upperLeft = "g" + getRow + "." + (getCol-1);
    		String lowerLeft = "g" + (getRow+1) + "." + (getCol-1);
    		String lowerRight = "g" + (getRow+1) + "." + getCol;
    		
    		otherThree.add(upperLeft);
    		otherThree.add(lowerLeft);
    		otherThree.add(lowerRight);
    	}
    	
    	else if (isLowerRight(getRow, getCol, row, col)) {
    		String upperLeft = "g" + (getRow-1) + "." + (getCol-1);
    		String upperRight = "g" + (getRow-1) + "." + getCol;
    		String lowerLeft = "g" + getRow + "." + (getCol -1);
    		
    		otherThree.add(upperLeft);
    		otherThree.add(upperRight);
    		otherThree.add(lowerLeft);
    	}
    	
    	else {
    		System.out.println("There is an issue here!");
    	}
    	
    	return otherThree;
    }
}
