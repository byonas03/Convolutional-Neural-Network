import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;

public class Network {
	public String arg = "";
	public LinkedList<LinkedList<Filter>> filters = new LinkedList<>();
	public LinkedList<LinkedList<Neuron>> neurons = new LinkedList<>();
	public LinkedList<int[]> pools = new LinkedList<>();
	public LinkedList<Weight> weights = new LinkedList<>();
	public double gamma;

	public Network(String[][] arguments, int x, int y, double ingamma) {
		gamma = ingamma;
		int hX = x, hY = y, filterQuantity = 1;
		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i][0].equals("f")) {
				filters.add(new LinkedList<Filter>());
				for (int j = 0; j < Integer.parseInt(arguments[i][2]); j++)
					filters.getLast().add(new Filter(Integer.parseInt(arguments[i][1])));
				hX = hX - (Integer.parseInt(arguments[i][1]) - 1);
				hY = hY - (Integer.parseInt(arguments[i][1]) - 1);
				filterQuantity *= Integer.parseInt(arguments[i][2]);
				arg += "f";
			} else if (arguments[i][0].equals("hl")) {
				neurons.add(new LinkedList<Neuron>());
				for (int j = 0; j < Integer.parseInt(arguments[i][1]); j++)
					neurons.get(neurons.size() - 1).add(new Neuron());
				arg += "h";
			} else if (arguments[i][0].equals("o")) {
				neurons.add(new LinkedList<Neuron>());
				for (int j = 0; j < Integer.parseInt(arguments[i][1]); j++)
					neurons.get(neurons.size() - 1).add(new Neuron());
				arg += "o";
			} else if (arguments[i][0].equals("p")) {
				arg += "p";
				pools.add(new int[] { Integer.parseInt(arguments[i][1]), Integer.parseInt(arguments[i][2]) });
				hX = (int) Math.ceil((double) hX / (double) (Integer.parseInt(arguments[i][1])));
				hY = (int) Math.ceil((double) hY / (double) (Integer.parseInt(arguments[i][1])));
			} else if (arguments[i][0].equals("n")) {
				arg += "n";
			}
		}
		LinkedList<Neuron> firstLayer = new LinkedList<>();
		for (int i = 0; i < 3 * (hX) * (hY) * filterQuantity; i++)
			firstLayer.add(new Neuron());
		neurons.add(0, firstLayer);

		for (int i = 0; i < neurons.size(); i++) {
			for (int j = 0; j < neurons.get(i).size(); j++) {
				if (i == 0) {
					neurons.get(i).get(j).addToNext(neurons.get(1));
				} else if (i == neurons.size() - 1) {
					neurons.get(i).get(j).addToPrev(neurons.get(i - 1));
				} else {
					neurons.get(i).get(j).addToNext(neurons.get(i + 1));
					neurons.get(i).get(j).addToPrev(neurons.get(i - 1));
				}
			}
		}
		for (int i = 1; i < neurons.size(); i++) {
			for (int j = 0; j < neurons.get(i).size(); j++) {
				for (int c = 0; c < neurons.get(i - 1).size(); c++) {
					Weight w = new Weight(neurons.get(i).get(j), neurons.get(i - 1).get(c));
					neurons.get(i).get(j).weights_back.add(w);
					neurons.get(i - 1).get(c).weights_forward.add(w);
					weights.add(w);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void runData(BufferedImage bi, int[] out) {
		Matrix[] rgbmats = new Matrix[] { new Matrix(bi.getWidth(), bi.getHeight()),
				new Matrix(bi.getWidth(), bi.getHeight()), new Matrix(bi.getWidth(), bi.getHeight()) };
		for (int a = 0; a < 3; a++) {
			for (int i = 0; i < bi.getWidth(); i++) {
				for (int j = 0; j < bi.getHeight(); j++) {
					rgbmats[a].matrix[i][j] = new Value(bi.getRaster().getPixel(i, j, new int[3])[a]);
				}
			}
		}
		int f = 0;
		for (int l = 0; l < 3; l++) {
			// Parse Argumentative 'DNA'
			int fIndex = 0, pIndex = 0;
			Matrix input = rgbmats[l];
			LinkedList<Matrix> currentSet = new LinkedList<>();
			currentSet.add(input);
			LinkedList<Matrix> nextSet = new LinkedList<>();
			for (int i = 0; i < arg.length(); i++) {
				if (arg.charAt(i) == 'f') {
					for (int c = 0; c < currentSet.size(); c++)
						for (int j = 0; j < filters.get(fIndex).size(); j++)
							nextSet.add(convolve(currentSet.get(c), filters.get(fIndex).get(j)));
					fIndex++;
				} else if (arg.charAt(i) == 'p') {
					for (int c = 0; c < currentSet.size(); c++)
						nextSet.add(pool(currentSet.get(c), pools.get(pIndex)[0], pools.get(pIndex)[1]));
					pIndex++;
				} else if (arg.charAt(i) == 'n') {
					for (int c = 0; c < currentSet.size(); c++)
						nextSet.add(normalize(currentSet.get(c)));
				} else if (arg.charAt(i) == 'h')
					break;
				currentSet.clear();
				currentSet = (LinkedList<Matrix>) nextSet.clone();
				nextSet.clear();
			}
			// Forward propagation
			for (int i = 0; i < currentSet.size(); i++) {
				for (int j = 0; j < currentSet.get(i).matrix.length; j++) {
					for (int c = 0; c < currentSet.get(i).matrix[j].length; c++) {
						neurons.get(0).get(f).activation = currentSet.get(i).matrix[j][c].val;
						f++;
					}
				}
			}
		}
		for (int a = 1; a < neurons.size(); a++) {
			LinkedList<Neuron> currentLayer = neurons.get(a);
			for (int i = 0; i < currentLayer.size(); i++) {
				double sum = 0.0;
				LinkedList<Neuron> previousLayer = neurons.get(a).get(i).connections_back;
				for (int j = 0; j < previousLayer.size(); j++) {
					sum += previousLayer.get(j).activation * neurons.get(a).get(i).weights_back.get(j).value;
				}
				currentLayer.get(i).activation = rectify(sum);
			}
		}
		System.out.println(neurons.getLast().get(0).activation + "/" + neurons.getLast().get(1).activation);
		// Backward propagation of FF
		for (int i = 0; i < neurons.getLast().size(); i++) {
			for (int j = 0; j < neurons.getLast().get(i).weights_back.size(); j++) {
				Neuron fr = neurons.getLast().get(i);
				Neuron ba = neurons.getLast().get(i).weights_back.get(j).prev;
				double sensitivity = (ba.activation) * (rectify_deriv(fr.activation)) * (2 * (fr.activation - out[i]));
				neurons.getLast().get(i).weights_back.get(j).nudge(sensitivity * gamma);
				sensitivity = (rectify_deriv(fr.activation)) * (2 * (fr.activation - out[i]));
				fr.nudge(sensitivity * gamma);
				ba.lastsensitivity += (neurons.getLast().get(i).weights_back.get(j).value)
						* (rectify_deriv(fr.activation)) * (2 * (fr.activation - out[i]));
			}
		}
		for (int a = neurons.size() - 2; a >= 1; a--) {
			for (int i = 0; i < neurons.get(a).size(); i++) {
				for (int j = 0; j < neurons.get(a).get(i).weights_back.size(); j++) {
					Neuron fr = neurons.get(a).get(i);
					Neuron ba = neurons.get(a).get(i).weights_back.get(j).prev;
					double sensitivity = fr.lastsensitivity * (rectify_deriv(fr.activation)) * (ba.activation);
					neurons.get(a).get(i).weights_back.get(j).nudge(sensitivity * gamma);
					sensitivity = fr.lastsensitivity * (rectify_deriv(fr.activation));
					fr.nudge(sensitivity * gamma);
					ba.lastsensitivity += fr.lastsensitivity * rectify_deriv(fr.activation)
							* (neurons.get(a).get(i).weights_back.get(j).value);
				}
			}
		}
		for (int i = 0; i < neurons.size(); i++) {
			for (int j = 0; j < neurons.get(i).size(); j++) {
				neurons.get(i).get(j).lastsensitivity = 0.0;
			}
		}
		// Backward propagation of filters

	}

	public static Matrix convolve(Matrix m, Filter f) {
		Matrix ret = new Matrix(m.matrix.length - (f.mat.matrix.length - 1),
				m.matrix[0].length - (f.mat.matrix[0].length - 1));
		for (int i = 0; i < ret.matrix.length; i++) {
			for (int j = 0; j < ret.matrix[0].length; j++) {
				double sum = 0.0;
				for (int a = 0; a < f.mat.matrix.length; a++)
					for (int b = 0; b < f.mat.matrix[a].length; b++)
						sum += f.mat.matrix[a][b].val * m.matrix[i + a][j + b].val;
				ret.matrix[i][j] = new Value(sum);
			}
		}
		return ret;
	}

	public static Matrix pool(Matrix m, int stride, int dim) {
		Matrix ret = new Matrix((int) Math.ceil((double) m.matrix.length / stride),
				(int) Math.ceil((double) m.matrix[0].length / stride));
		int g = 0, h = 0;
		for (int i = 0; i < m.matrix.length; i += stride) {
			for (int j = 0; j < m.matrix[i].length; j += stride) {
				double max = Integer.MIN_VALUE;
				for (int a = i; a < i + dim; a++) {
					for (int b = j; b < j + dim; b++) {
						if (a < m.matrix.length && b < m.matrix[a].length && max < m.matrix[a][b].val)
							max = m.matrix[a][b].val;
					}
				}
				ret.matrix[g][h] = new Value(max);
				h++;
			}
			g++;
			h = 0;
		}
		return ret;
	}

	public static Matrix normalize(Matrix m) {
		for (int i = 0; i < m.matrix.length; i++)
			for (int j = 0; j < m.matrix[i].length; j++)
				m.matrix[i][j] = new Value(Math.max(0, m.matrix[i][j].val));
		return m;
	}

	public double rectify(double in) {
		return Math.max(0, in);
	}

	public double rectify_deriv(double in) {
		if (in > 0)
			return 1;
		else
			return 0;
	}

	public static void main(String args[]) throws IOException {
		String[][] arg = new String[][] { { "f", "3", "2" }, { "n" }, { "p", "1", "2" }, { "f", "3", "2" }, { "n" },
				{ "hl", "10" }, { "hl", "2" } };
		Network n = new Network(arg, 28, 28, -.3);
		BufferedImage img = ImageIO.read(new File("img_22.jpg"));
		long start = System.currentTimeMillis();
		n.runData(img, new int[] { 0, 1 });
		System.out.println(System.currentTimeMillis() - start);
	}
}
