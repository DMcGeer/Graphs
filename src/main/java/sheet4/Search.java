package sheet4;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ImmutableValueGraph.Builder;
import com.google.common.graph.ValueGraphBuilder;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.size;

@SuppressWarnings("UnstableApiUsage")
public class Search {

	/**
	 * Lists all nodes values in a given graph.
	 *
	 * @param graph the graph to query the nodes from
	 * @return set of all the nodes in the given graph
	 */
	static Set<Integer> listAllNodes(ImmutableValueGraph<Integer, Integer> graph) {
		return graph.nodes();
	}

	/**
	 * Lists all edge values in a given graph.
	 *
	 * @param graph the graph to query the edges from
	 * @return list of all the edges in the given graph, the order is not important
	 */
	static List<Integer> listAllEdgeValues(ImmutableValueGraph<Integer, Integer> graph) {
		List<Integer> list = new ArrayList<Integer>(size(graph.edges()));
		for(EndpointPair<Integer> edge : graph.edges()){
			graph.edgeValue(edge).ifPresent(x -> list.add(x));
		}
		return list;
	}

	/**
	 * Lists all nodes with 4 or more edges
	 *
	 * @param graph the graph to query the edges from
	 * @return set of all nodes that satisfy the condition
	 */
	static Set<Integer> findAllNodeWith4OrMoreEdges(
			ImmutableValueGraph<Integer, Integer> graph) {
		Set<Integer> nodes = graph.nodes();
		Set<Integer> newNodes = new HashSet<Integer>();
		Iterator<Integer> nodeIterator = nodes.iterator();
		while (nodeIterator.hasNext()){
			Integer next = nodeIterator.next();
			if (graph.degree(next) >= 4){
				newNodes.add(next);
			}
		}
		return newNodes;
	}

	/**
	 * Lists all nodes with edges values that when summed, is > 20
	 * For example, a node with three connected edges containing the value: 1, 2, 3 has an edge
	 * sum of 6.
	 *
	 * @param graph the graph to query the edges from
	 * @return set of all nodes that satisfy the condition
	 */
	static Set<Integer> findAllNodesWithEdgeSumGreaterThan20(
			ImmutableValueGraph<Integer, Integer> graph) {
		Set<Integer> nodes = graph.nodes();
		Set<Integer> newNodes = new HashSet<Integer>();
		Iterator<Integer> nodeIterator = nodes.iterator();
		while(nodeIterator.hasNext()){
			Integer oldNext = nodeIterator.next();
			Set<Integer> nextTo = graph.adjacentNodes(oldNext);
			Iterator<Integer> adjIterator = nextTo.iterator();
			int count = 0;
			while(adjIterator.hasNext()){
				Integer next = adjIterator.next();
				count += graph.edgeValue(oldNext, next).orElse(0);
			}
			if (count >= 20){
				newNodes.add(oldNext);
			}
		}
		return newNodes;
	}


	/**
	 * Finds the shortest possible path that travels from the source to destination, factoring the
	 * edge distances.
	 * A path that allows you to travel from the source to the destination with the minimum total
	 * edge distances is the shortest path.
	 *
	 * @param graph the graph to compute the shortest path with
	 * @param source the starting position of the search, the resulting list should start with
	 * this value
	 * @param destination the end position of the search, the resulting list should end with this
	 * value
	 * @return a list of nodes that represent the shortest path from source to destination where
	 * the first element is the source and the last element is the destination
	 */
	static List<Integer> shortestPathFromSourceToDestination(
			ImmutableValueGraph<Integer, Integer> graph,
			Integer source,
			Integer destination) {
		List<Integer> sptSet = new ArrayList<>();
		// set of all nodes in the graph
		Set<Integer> nodes = graph.nodes();
		Iterator<Integer> nodesIterator = nodes.iterator();
		// list of distances from the source node
		List<Integer> dist = new ArrayList<>();
		List<Boolean> present = new ArrayList<>();
		// prior holds the index of the connected node on the path
		List<Integer> prior = new ArrayList<>();
		for (int t = 0; t < size(nodes); t++){
			prior.add(0);
		}
		// initialise dist and present
		int i = 0;
		while(nodesIterator.hasNext()){
			if(Objects.equals(nodesIterator.next(), source)){
				dist.add(i, 0);
			} else {dist.add(i, Integer.MAX_VALUE);}
			present.add(i, Boolean.FALSE);
			i++;
		}
		// while not all nodes have been visited
		Boolean cont = Boolean.TRUE;
		while(cont){
			for (int t = 0; t < size(present)-1; t++){
				cont = !(cont && present.get(t));
			}
			nodesIterator = nodes.iterator();
			int minimum = Integer.MAX_VALUE;
			// finds minimum distance which hasn't been looked at yet
			Integer currentNode = size(nodes)-1;
			int index = size(nodes)-1;
			for(int t = 0; t < size(nodes)-1; t++){
				if((dist.get(t) < minimum) && (present.get(t) == Boolean.FALSE)){
					index = t;
					currentNode = nodesIterator.next();
					minimum = dist.get(t);
				} else {
					nodesIterator.next();
				}
			}
			present.set(index, Boolean.TRUE);
			Set<Integer> successors = graph.successors(currentNode);
			Iterator<Integer> successIterator = successors.iterator();
			// go through successors to assess weights
			while(successIterator.hasNext()){
				AtomicInteger weight = new AtomicInteger();
				Integer successor = successIterator.next();
				Iterator<Integer>nodesIterator2 = nodes.iterator();
				int count = 0;
				while(nodesIterator2.hasNext()){
					if(successor.equals(nodesIterator2.next())){
						break;
					} else {
						count++;
					}
				}
				graph.edgeValue(currentNode, successor).ifPresent(weight::set);
				if ((dist.get(index) + weight.get()) < dist.get(count)){
					dist.set(count, (dist.get(index) + weight.get()));
					prior.set(count, index);
				}
			}
		}
		// find the destination node in the nodes
		nodesIterator = nodes.iterator();
		int DestIndex = 0;
		Integer currentNode;
		while(nodesIterator.hasNext()){
			currentNode = nodesIterator.next();
			if(currentNode.equals(destination)){
				present.set(DestIndex, Boolean.FALSE);
				break;
			}
			else {DestIndex++;}
		}
		// find index of source in nodes
		nodesIterator = nodes.iterator();
		int SourceIndex = 0;
		while(nodesIterator.hasNext()){
			if(nodesIterator.next().equals(source)){
				break;
			}
			else {SourceIndex++;}
		}
		int priorNode = prior.get(DestIndex);
		// while the prior node isn't the source
		sptSet.add(destination);
		while (priorNode != SourceIndex){
			// find the node and add it to the path
			nodesIterator = nodes.iterator();
			for (int t = 0; t < priorNode-1; t++){
				nodesIterator.next();
			}
			sptSet.add((nodesIterator.next())+1);
			priorNode = prior.get(priorNode);
		}
		sptSet.add(source);
		int temp = 0;
		for(int t = 1; t < size(sptSet)/2+1; t++){
			temp = sptSet.get(t-1);
			sptSet.set(t-1, sptSet.get(size(sptSet) - t));
			sptSet.set(size(sptSet)-t, temp);
		}
		return sptSet;
	}



	// reads in a graph stored in plan text, not part of any question but feel free to study at how
	// a graph is constructed
	static ImmutableValueGraph<Integer, Integer> readGraph(String content) {
		List<String> lines = content.lines().collect(Collectors.toList());
		if (lines.isEmpty()) throw new IllegalArgumentException("No lines");
		int currentLine = 0;

		String[] topLine = lines.get(currentLine++).split(" ");
		int numberOfNodes = Integer.parseInt(topLine[0]);
		int numberOfEdges = Integer.parseInt(topLine[1]);

		Builder<Integer, Integer> builder = ValueGraphBuilder
				.undirected()
				.expectedNodeCount(numberOfNodes)
				.immutable();


		for (int i = 0; i < numberOfNodes; i++) {
			String line = lines.get(currentLine++);
			if (line.isEmpty()) continue;
			builder.addNode(Integer.parseInt(line));
		}

		for (int i = 0; i < numberOfEdges; i++) {
			String line = lines.get(currentLine++);
			if (line.isEmpty()) continue;

			String[] s = line.split(" ");
			if (s.length != 3) throw new IllegalArgumentException("Bad edge line:" + line);
			builder.putEdgeValue(Integer.parseInt(s[0]),
					Integer.parseInt(s[1]),
					Integer.parseInt(s[2]));
		}
		return builder.build();
	}


}