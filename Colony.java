import java.io.*;
import java.nio.*;
import java.util.*;
import java.lang.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Colony implements Runnable{

	static String newline = System.lineSeparator();
	Random rand = new Random();

	int numCycle = 0;
	int numCities;
	int numberOfAnts;
	double alpha; //importance of pheromones
	double beta; //importance of heuristic
	double rho; //pheromone evaportation
	WorldGraph graph;
	double[][] pheromoneArray;   //array used to keep track of pheromones
	List<List<Integer>> antPaths;
	List<Ant> Antz;
	int foundOptimumAt = -1;
	Ant OverallOptimalAnt;
	int numberOfCycles;
	public void run() {

        for(int i = 0; i<numberOfCycles ; i++){
            runCycle();
        }
        System.out.println("BEST PATH (for this colony): "+newline+OverallOptimalAnt.getPathString()+newline+"found at: " + foundOptimumAt);
    }

	public Colony(double alpha, double beta, double rho, int numberOfAnts,
				  WorldGraph graph, int numberOfCycles){


		this.numberOfCycles = numberOfCycles;
		this.alpha = alpha;
		this.beta = beta;
		this.rho = rho;
		this.numberOfAnts = numberOfAnts;
		this.graph = graph;
		this.numCities = graph.getnumCities();
		pheromoneArray = new double[numCities][numCities];
		for(int i = 0; i<numCities ; i++){
			for(int j = 0; j<numCities; j++){
				pheromoneArray[i][j] =numberOfAnts/(300000.0);
			}
		}
		Antz = new ArrayList<Ant>();
	}

	 /* evaporates pheromones
	* loops through pheromoneArray and multiply values in it by 1-rho
	* pre: pheromoneArray[][] initiliazed
	* post: pheromoneArray[][] updated
	* */
	public void evaporatePheromones(){
		for(int i = 0; i<numCities ; i++){
			for(int j = 0; j<numCities ; j++){
				pheromoneArray[i][j] = pheromoneArray[i][j]*(1.0-rho);
			}
		}
	}


	public Ant optimalAnt(){
		double min = Double.MAX_VALUE;
		int minIndex = -1;

		for(int i = 0; i<Antz.size(); i++){
			if(Antz.get(i).l  < min){

			min = (int)Antz.get(i).l;
			minIndex = i;
			}
		}
		return Antz.get(minIndex);
    }

	/* puts pheromones on trail of argument ant
	* param: Ant the ant which trail you want pheromone to be put on
	* post: pheromoneArray[][] updated
	* */
	public void putPheromones(Ant andy){

		double Ak = rho*(1/andy.l); //times a thousand???

		for(int i = 0;i<andy.path.size() -1; i++){
			int r = andy.path.get(i);
			int s = andy.path.get(i+1);
			pheromoneArray[r][s] = pheromoneArray[r][s] + Ak;
			pheromoneArray[s][r] = pheromoneArray[s][r] + Ak;
		}
	}


	  /*
   * runs a cycle:
   * constructs numberOfAnts ants and launches run on each of them, sequentelly
   * AFTER ALL ants complete, put pheromone and evaporate pheromone
   * optional: minmax: global optimal ant puts pheromone on each cycle
   * update numCycle
   * */
	public void runCycle(){



// 		System.out.println(
// 		"Thread"+Thread.currentThread().getName()+
// 		" beginning ant wave #" + numCycle );
		List<Thread> threads = new ArrayList<Thread>();


		Antz = new ArrayList<Ant>();


		for(int k = 0; k<numberOfAnts; k++){
			Ant anny = new Ant();
			Antz.add(anny);
			Thread t = new Thread(anny);
			threads.add(t);
			t.start();
// 			System.out.println("ANT ########## "  + k + "!!!");
			//without threads // anny.run();
		}

		try{

			for (Thread thread : threads) {
				thread.join();
			}

			Ant localOptimalAnt = optimalAnt();
 			System.out.println("Colony"+Thread.currentThread().getName()+" wave #"+numCycle+" l: " + localOptimalAnt.l);


			if(numCycle>0){
// 							System.out.println("local optimal l:"
// 								   + localOptimalAnt.l + " overall optimal l: "
// 								   + OverallOptimalAnt.l);
				if(localOptimalAnt.l<OverallOptimalAnt.l){
// 							System.out.println("replacing overall optimal");
					OverallOptimalAnt = localOptimalAnt;
					foundOptimumAt=numCycle;
				}
			}else{
			OverallOptimalAnt = localOptimalAnt;
			}



			for(Ant a: Antz){
			putPheromones(a);
			}

			evaporatePheromones();

			numCycle++ ;


		}catch(Exception e){
			System.err.println("thread problem");
		}
	}

	public class Ant implements Runnable{

		int startNode;
		List<Integer> visited;    //visited nodes
		List<Integer> path;      //path
		List<Integer> neighbors; //valid neighbours (not visited)
		int currentNode;
		double l;


		  /* Constructor for ant
     * initializes: l = 0
     *              visited and adds 0 to it (start)
     *              path and adds 0 to it
     *              currentNode = 0
     * */
    public Ant(){

		startNode = rand.nextInt(numCities); //?
		l= 0;
		visited = new ArrayList<Integer>();
		visited.add(startNode);
		path = new ArrayList<Integer>();
		path.add(startNode);
		currentNode=startNode;
    }

    public void updateCurrentNode(){
      currentNode = path.get(path.size()-1);
    }

    /*  updates denominator field
     *  denom = sum of all the (eta*tau) of the possible states
     * */
    public double getDenominator(){
		double tau = 0.0;
		double eta = 0.0;
		double ret = 0.0;
		for(int i: neighbors){


// 			System.out.print("current to "+i+ " this much phero. "+pheromoneArray[currentNode][i]+newline);
// 			System.out.print("current to "+i+ " this much distance "+graph.getDistance(currentNode, i));
			tau = Math.pow(pheromoneArray[currentNode][i],alpha);
			eta = Math.pow(1/graph.getDistance(currentNode, i),beta);
			ret = ret + eta * tau;
// 						System.out.print("!??!tau: " + tau + "  eta: " + eta + " ret: "
// 			                +ret+newline);
		}
		/*if(ret==0.0)
			ret = Double.MIN_VALUE;*/

		return ret;
    }

    /*  Calculate probability of ant to go associated with param state
     *  p = (tau*eta)/denominator
     *  param: int corresponding to destination node of which to calc prob
     * returns: int probability
     * post: 0<p<=1
     * */
    public double calcP(int neighbor){
			double tau = Math.pow(pheromoneArray[currentNode][neighbor],alpha);
			double eta = Math.pow(1/graph.getDistance(currentNode,neighbor),beta);
			double denominator = getDenominator();

		return (tau*eta)/denominator;
    }

    /* "decides next move" by checking if there is available states to go
     * if not go backwards (backtrack)
     * returns : int corresponding to state decided to go to
     * */
    public int decideNextMove(){

		List<Double> listP = new ArrayList<Double>();

		if(neighbors.isEmpty()){
			System.out.print("EMPTY NEIGHBORS");
			return -1;
		}
		for(int n : neighbors){
			double pp = calcP(n);
			listP.add(pp);
// 			System.out.print(currentNode+"->"+ n + ":" + pp + "||||");
		}

		double sum = 0;
		for(double j : listP){
			sum = sum + j;
		}

		//if value get too small, only ran into it in test for 10000cycles
		if(sum==0.0){
			return rand.nextInt(listP.size());
		}

		double  r = rand.nextDouble();


		sum = 0;
		int index =  0;
		for(double p : listP){
			sum = sum + p;
			if(r<=sum){     //cumulative probability
				return index;
			}else{
				index++;
			}
		}
		return index;
    }

    /*  updates neighbors with available (not visited) states to go to
     *  post: neighbors updated
     *
     * */
    public void unvisitedNeighbors(){

      List<Integer> ret = new ArrayList<Integer>();
      for(int i=0; i<numCities; i++){
			ret.add(i);
      }
      for(Integer visitedCity: visited){
		ret.remove(visitedCity);
      }
      neighbors = ret;
    }

    /* function to launch ant in the world and make it find endpoint
     * post: path updated with path taken
     *       l updated with length of path
     *       currentNode updated
     *       visited updated
     * */
    public void run(){

    int next = 0;
    int i = 0;
        do{
			unvisitedNeighbors();
			i = decideNextMove();
			if(i==-1){
				System.out.println("returned -1?");
			}
// 			System.out.print("neighbors: ");
// 			for(int nei: neighbors){
// 				System.out.print(nei + " ");
// 			}
// 			System.out.print(newline);
			next = neighbors.get(i);
//   			System.out.println("I am @"+graph.getLabel(currentNode) +
//   							"going to " + graph.getLabel(next));
				path.add(next);
			visited.add(next);
			updateCurrentNode();
		}while(visited.size()<numCities);
      updateL();
    }

    /* adds up distance of path and updates ant's l
     * */
    public void updateL(){
      double sum = 0.0;
      for(int i = 0; i<path.size() -1 ; i++){
        sum = sum + graph.getDistance(path.get(i), path.get(i+1));
      }
      l  = sum;
    }

    /* returns string representation of path taken by ant
     *
     * */
    public String getPathString(){

      String s = "";

      for(int i = 0; i<path.size()-1; i++){


        String s1 = graph.getLabel(path.get(i));
        String s2 = graph.getLabel(path.get(i+1));
        int space=40-s1.length();
        while(space!=0){
        s = s + " ";
        space--;
        }
        s = s + s1 + " ===============> " +s2 + newline;
      }
      s = s +newline + "length: " + l + newline;
      return s;
    }

  }

}
