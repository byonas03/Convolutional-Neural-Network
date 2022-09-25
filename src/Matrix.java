import java.util.Arrays;

public class Matrix {
	public Value[][] matrix;
	public Matrix(int dim) {
		matrix = new Value[dim][dim];
	}
	public Matrix(int x, int y) {
		matrix = new Value[x][y];
	}
	public Matrix(int[][] mat) {
		matrix = new Value[mat.length][mat[0].length];
		for (int i = 0; i < mat.length; i++) {
			for (int j = 0; j < mat[i].length; j++) {
				matrix[i][j] = new Value(mat[i][j]);
			}
		}
	}
	public String toString() {
		return Arrays.deepToString(matrix);
	}
}
