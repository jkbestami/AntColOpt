import java.util.*;

class ACO{
	public static void main (String [] args) {
		
		String line;
		double alpha;
		double beta;
		double rho;
		int thr;
		int numberOfAnts;
		
		Scanner reader = new Scanner(System.in); 
		System.out.println("Enter filename:");
		WorldGraph wg = new WorldGraph(reader.nextLine());
		
		System.out.println("Enter alpha (importance of pheromones)  default:1");
		line=reader.nextLine();
		if(line.isEmpty()){
			alpha = 1.0;
		}else{
			alpha = Double.parseDouble(line);
		}
		
		
		System.out.println("Enter beta (importance of heuristic) default: 2");
		line=reader.nextLine();
		if(line.isEmpty()){
			beta = 2.0;
		}else{
			beta = Double.parseDouble(line);
		}
		
		System.out.println("Enter rho (evaporation rate) default: 0.5");
			line=reader.nextLine();
		if(line.isEmpty()){
			rho = 0.5;
		}else{
			rho = Double.parseDouble(line);
		}
		
		System.out.println("Enter number of parallel colonies (threads) default: 1");
			line=reader.nextLine();
		if(line.isEmpty()){
			thr = 1;
		}else{
			thr = Integer.parseInt(line);
		}
		
		
		System.out.println("param: " + alpha + " " + beta + " " + rho);
		Random rr = new Random();
		List<Thread> threads = new ArrayList<Thread>();
		List<Colony> listCol = new ArrayList<Colony>();
		for(int k = 0; k<thr; k++){
			if(wg.getnumCities()>50){
				numberOfAnts = 50;
			}else{
				numberOfAnts = wg.getnumCities();
			}
			Colony col = new Colony(alpha, beta+k, rho, numberOfAnts, wg, 100);
			listCol.add(col);
			Thread t = new Thread(col);
			threads.add(t);
			t.setName("#"+k);
			t.start();
		}
		try{
			for (Thread thread : threads) {
				thread.join();
			}
			double minL = Double.MAX_VALUE;
			String minPath ="";
			Colony bestColony = new Colony(0,0,0,1,wg,1);
			for(Colony co: listCol){
				if(co.OverallOptimalAnt.l<minL){
					minPath = co.OverallOptimalAnt.getPathString();
					minL = co.OverallOptimalAnt.l;
					bestColony = co;
				}
			}
			System.out.println("best path over all threads: ");
			System.out.println(minPath);
		}catch(Exception e){
			System.err.println("thread problem");
		}
	}
}
