import java.util.ArrayList;
import java.util.LinkedList;

public class Neuron {
	public LinkedList<Neuron> connections_back, connections_forward;
	public LinkedList<Weight> weights_back, weights_forward;
	public int type;
	public double activation = 0.0, bias = Math.random() * .1;
	public double lastsensitivity = 0.0;
	
	public Neuron() {
		connections_back = new LinkedList<Neuron>();
		connections_forward = new LinkedList<Neuron>();
		weights_back = new LinkedList<Weight>();
		weights_forward = new LinkedList<Weight>();
	}
	
	public void addToNext(LinkedList<Neuron> list) {
		connections_forward.addAll(list);
	}
	
	public void addToPrev(LinkedList<Neuron> list) {
		connections_back.addAll(list);
	}	
	
	public void nudge(double n) {
		bias += n;
	}
}
