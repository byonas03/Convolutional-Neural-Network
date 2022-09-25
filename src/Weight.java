
public class Weight {
	public Neuron prev, next;
	public double value;
	public Weight(Neuron n, Neuron p) {
		value = Math.random() * .2 - .1;
		prev = p;
		next = n;
	}
	public void nudge(double n) {
		value += n;
	}
}
