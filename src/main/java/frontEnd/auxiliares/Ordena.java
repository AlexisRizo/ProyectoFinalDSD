package frontEnd.auxiliares;

import java.util.Comparator;

public class Ordena implements Comparator<Text>{
	public int compare(Text P1, Text P2){
		return Double.compare(P1.fitness,P2.fitness);
	}
}