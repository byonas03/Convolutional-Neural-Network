
public class Filter {
	public Matrix mat;
	public Filter(int dim) {
		mat = new Matrix(dim);
		for (int i = 0; i < mat.matrix.length; i++) {
			for (int j = 0; j < mat.matrix[i].length; j++) {
				double x = Math.random();
				if (x < (1/(double)3)) 
					x = -1;
				 else if (x < (2/(double)3)) 
					x = 0;
				 else 
					x = 1;
				mat.matrix[i][j] = new Value(x);
			}
		}
	}
	public Filter(Matrix m) {
		mat = m;
	}
}
