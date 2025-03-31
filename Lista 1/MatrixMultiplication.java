import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultiplication {

    // Classe para representar dimensões de uma matriz
    static class MatrixDimension {
        final int rows, cols;

        public MatrixDimension(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
        }

        @Override
        public String toString() {
            return rows + "x" + cols;
        }
    }

    // Classe para armazenar um par de dimensões de matrizes
    static class MatrixPair {
        final MatrixDimension a, b;

        public MatrixPair(MatrixDimension a, MatrixDimension b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public String toString() {
            return "(" + a + ", " + b + ")";
        }
    }

    // Método principal
    public static void main(String[] args) {
        // Definindo as dimensões das matrizes conforme solicitado
        List<MatrixPair> testCases = new ArrayList<>();
        testCases.add(new MatrixPair(new MatrixDimension(50, 50), new MatrixDimension(50, 100)));
        testCases.add(new MatrixPair(new MatrixDimension(2000, 1000), new MatrixDimension(1000, 4000)));
        testCases.add(new MatrixPair(new MatrixDimension(4000, 2000), new MatrixDimension(2000, 8000)));

        // Executando os testes para cada par de matrizes
        for (MatrixPair pair : testCases) {
            System.out.println("\nTestando multiplicação de matrizes: " + pair);

            // Criando matrizes aleatórias
            int[][] matrixA = createRandomMatrix(pair.a.rows, pair.a.cols);
            int[][] matrixB = createRandomMatrix(pair.b.rows, pair.b.cols);

            // Multiplicando com threads diretas
            long startTime = System.currentTimeMillis();
            int[][] resultThreads = multiplyWithThreads(matrixA, matrixB);
            long endTime = System.currentTimeMillis();
            System.out.println("Tempo com threads diretas: " + (endTime - startTime) + "ms");

            // Multiplicando com executor
            startTime = System.currentTimeMillis();
            int[][] resultExecutor = multiplyWithExecutor(matrixA, matrixB);
            endTime = System.currentTimeMillis();
            System.out.println("Tempo com executor: " + (endTime - startTime) + "ms");
        }
    }

    // Cria uma matriz com valores aleatórios
    private static int[][] createRandomMatrix(int rows, int cols) {
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = (int) (Math.random() * 10);
            }
        }
        return matrix;
    }

    // Multiplicação de matrizes usando threads diretas
    private static int[][] multiplyWithThreads(int[][] a, int[][] b) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int colsB = b[0].length;

        int[][] result = new int[rowsA][colsB];
        List<Thread> threads = new ArrayList<>();

        // Criando uma thread para cada linha da matriz resultante
        for (int i = 0; i < rowsA; i++) {
            final int row = i;
            Thread thread = new Thread(() -> {
                for (int j = 0; j < colsB; j++) {
                    for (int k = 0; k < colsA; k++) {
                        result[row][j] += a[row][k] * b[k][j];
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }

        // Aguardando todas as threads terminarem
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    // Multiplicação de matrizes usando ExecutorService
    private static int[][] multiplyWithExecutor(int[][] a, int[][] b) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int colsB = b[0].length;

        int[][] result = new int[rowsA][colsB];

        // Criando um pool de threads com número de threads igual ao número de
        // processadores disponíveis
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Submetendo tarefas para o executor
        for (int i = 0; i < rowsA; i++) {
            final int row = i;
            executor.submit(() -> {
                for (int j = 0; j < colsB; j++) {
                    for (int k = 0; k < colsA; k++) {
                        result[row][j] += a[row][k] * b[k][j];
                    }
                }
            });
        }

        // Encerrando o executor e aguardando todas as tarefas terminarem
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }
}
