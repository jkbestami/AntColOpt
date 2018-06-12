import java.io.*;
import java.nio.*;
import java.util.*;
import java.lang.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class WorldGraph{

	int numCities;
	double[][] distance;

	String[] cityNames;
	double[] lats;
	double[] lons;

	public double calcDistance(double lat1,double lon1,double lat2,double lon2){

		double p = 0.017453292519943295;    // Math.PI / 180
		double a = 0.5 - Math.cos((lat2 - lat1) * p)/2 +
				Math.cos(lat1 * p) * Math.cos(lat2 * p) *
				(1 - Math.cos((lon2 - lon1) * p))/2;

		return 12742 * Math.asin(Math.sqrt(a)); // 2 * R; R = 6371 km
	}


	public WorldGraph(String filename){
		parse(filename);
		updateDistances();
	}

	/**
	 * [parse description]
	 * @method parse
	 * @param  String filename [description]
	 */
	public void parse(String filename){

		File file = new File(filename);
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;

			text = reader.readLine();
			numCities = Integer.parseInt(text);

			cityNames = new String[numCities];
			lats = new double[numCities];
			lons = new double[numCities];
			int i = 0;
			while ((text = reader.readLine()) != null) {
				String[] parts = text.split(",");
				cityNames[i] = parts[0];
				lats[i] = Double.parseDouble(parts[1]);
				lons[i] = Double.parseDouble(parts[2]);
				i++;
			}
		}catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				}
		}

	}

	public double getDistance(int o, int d){
		return distance[o][d];
	}

	public int getnumCities(){
		return numCities;
	}

	public String getLabel(int n){
		return cityNames[n];
	}

	public void updateDistances(){
		distance = new double[numCities][numCities];
		for(int i = 0; i<numCities; i++){
			for(int j = 0; j<numCities; j++){
				if(i==j){
					distance[i][j] = 0.0;
				}else{
					distance[i][j] = calcDistance(lats[i], lons[i], lats[j], lons[j]);
				}
			}
		}

	}

}
