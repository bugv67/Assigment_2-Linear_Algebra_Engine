package spl.lae;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.List;

import memory.SharedMatrix;
import memory.VectorOrientation;
import parser.ComputationNode;
import parser.ComputationNodeType;

import static org.junit.jupiter.api.Assertions.*;

class LinearAlgebraEngineTest {

        private ComputationNode matrixNode(double[][] m) {
                return new ComputationNode(m);
        }

        private ComputationNode opNode(ComputationNodeType type, ComputationNode... children) {
                return new ComputationNode(type, List.of(children));
        }

        private LinearAlgebraEngine lae;
        private SharedMatrix leftMatrix;
        private SharedMatrix rightMatrix;

        @BeforeEach
        void setUp() throws Exception {
                lae = new LinearAlgebraEngine(2);

                Field leftField = LinearAlgebraEngine.class.getDeclaredField("leftMatrix");
                Field rightField = LinearAlgebraEngine.class.getDeclaredField("rightMatrix");
                leftField.setAccessible(true);
                rightField.setAccessible(true);

                leftMatrix = (SharedMatrix) leftField.get(lae);
                rightMatrix = (SharedMatrix) rightField.get(lae);
        }

        // CREATE ADD TASKS TESTS
        @Test
        void createAddTasks_2x2Matrix_addsCorrectly() {
                double[][] a = {
                                { 1, 2 },
                                { 3, 4 }
                };
                double[][] b = {
                                { 5, 6 },
                                { 7, 8 }
                };

                leftMatrix.loadRowMajor(a);
                rightMatrix.loadRowMajor(b);

                List<Runnable> tasks = lae.createAddTasks();

                assertEquals(2, tasks.size(), "Should create one task per row");

                tasks.forEach(Runnable::run);

                double[][] expected = {
                                { 6, 8 },
                                { 10, 12 }
                };

                assertArrayEquals(expected, leftMatrix.readRowMajor());
        }

        @Test
        void createAddTasks_singleRowMatrix() {
                double[][] a = { { 1, 2, 3 } };
                double[][] b = { { 4, 5, 6 } };

                leftMatrix.loadRowMajor(a);
                rightMatrix.loadRowMajor(b);

                List<Runnable> tasks = lae.createAddTasks();
                assertEquals(1, tasks.size());

                tasks.forEach(Runnable::run);

                double[][] expected = { { 5, 7, 9 } };
                assertArrayEquals(expected, leftMatrix.readRowMajor());
        }

        @Test
        void createAddTasks_singleColumnMatrix() {
                double[][] a = {
                                { 1 },
                                { 2 },
                                { 3 }
                };
                double[][] b = {
                                { 4 },
                                { 5 },
                                { 6 }
                };

                leftMatrix.loadRowMajor(a);
                rightMatrix.loadRowMajor(b);

                List<Runnable> tasks = lae.createAddTasks();
                assertEquals(3, tasks.size());

                tasks.forEach(Runnable::run);

                double[][] expected = {
                                { 5 },
                                { 7 },
                                { 9 }
                };

                assertArrayEquals(expected, leftMatrix.readRowMajor());
        }

        @Test
        void createAddTasks_emptyMatrix_createsNoTasks() {
                double[][] empty = new double[0][0];

                leftMatrix.loadRowMajor(empty);
                rightMatrix.loadRowMajor(empty);

                List<Runnable> tasks = lae.createAddTasks();

                assertTrue(tasks.isEmpty(), "No tasks should be created for empty matrices");
        }

        @Test
        void createAddTasks_largeMatrix() {
                int size = 100;
                double[][] a = new double[size][size];
                double[][] b = new double[size][size];

                for (int i = 0; i < size; i++) {
                        for (int j = 0; j < size; j++) {
                                a[i][j] = i;
                                b[i][j] = j;
                        }
                }

                leftMatrix.loadRowMajor(a);
                rightMatrix.loadRowMajor(b);

                List<Runnable> tasks = lae.createAddTasks();
                assertEquals(size, tasks.size());

                tasks.forEach(Runnable::run);

                double[][] result = leftMatrix.readRowMajor();
                assertEquals(size, result.length);
                assertEquals(size, result[0].length);
                assertEquals(0 + 0, result[0][0]);
                assertEquals((size - 1) + (size - 1), result[size - 1][size - 1]);
        }

        // CREATE MULTIPLY TASKS TESTS
        @Test
        void createMultiplyTasks_2x2Matrices() {
                double[][] a = {
                                { 1, 2 },
                                { 3, 4 }
                };

                double[][] b = {
                                { 5, 6 },
                                { 7, 8 }
                };

                leftMatrix.loadRowMajor(a);
                rightMatrix.loadColumnMajor(b);

                List<Runnable> tasks = lae.createMultiplyTasks();
                assertEquals(2, tasks.size(), "Should create one task per row");
                tasks.forEach(Runnable::run);

                double[][] expected = {
                                { 19, 22 },
                                { 43, 50 }
                };

                assertArrayEquals(expected, leftMatrix.readRowMajor());
        }

        @Test
        void createMultiplyTasks_singleRowMatrix() {
                double[][] a = {
                                { 1, 2, 3 }
                };

                double[][] b = {
                                { 4, 5 },
                                { 6, 7 },
                                { 8, 9 }
                };

                leftMatrix.loadRowMajor(a);
                rightMatrix.loadColumnMajor(b);

                List<Runnable> tasks = lae.createMultiplyTasks();
                assertEquals(1, tasks.size());
                tasks.forEach(Runnable::run);
                double[][] expected = {
                                {
                                                1 * 4 + 2 * 6 + 3 * 8,
                                                1 * 5 + 2 * 7 + 3 * 9
                                }
                };

                assertArrayEquals(expected, leftMatrix.readRowMajor());
        }

        @Test
        void createMultiplyTasks_singleColumnTimesRow() {
                double[][] a = {
                                { 1 },
                                { 2 },
                                { 3 }
                };
                double[][] b = {
                                { 4, 5, 6 }
                };

                leftMatrix.loadRowMajor(a);
                rightMatrix.loadColumnMajor(b);

                List<Runnable> tasks = lae.createMultiplyTasks();
                assertEquals(3, tasks.size());
                tasks.forEach(Runnable::run);

                double[][] expected = {
                                { 4, 5, 6 },
                                { 8, 10, 12 },
                                { 12, 15, 18 }
                };
                assertArrayEquals(expected, leftMatrix.readRowMajor());
        }

        @Test
        void createMultiplyTasks_largeMatrix() {
                int size = 50;

                double[][] a = new double[size][size];
                double[][] b = new double[size][size];
                for (int i = 0; i < size; i++) {
                        for (int j = 0; j < size; j++) {
                                a[i][j] = 1;
                                b[i][j] = 1;
                        }
                }

                leftMatrix.loadRowMajor(a);
                rightMatrix.loadColumnMajor(b);
                List<Runnable> tasks = lae.createMultiplyTasks();
                assertEquals(size, tasks.size());

                tasks.forEach(Runnable::run);

                double[][] result = leftMatrix.readRowMajor();

                assertEquals(size, result[0][0]);
                assertEquals(size, result[size - 1][size - 1]);
        }

        // Negated Tests
        @Test
        void createNegateTasks_2x2Matrix() {
                double[][] a = {
                                { 1, -2 },
                                { -3, 4 }
                };

                leftMatrix.loadRowMajor(a);

                List<Runnable> tasks = lae.createNegateTasks();
                assertEquals(2, tasks.size(), "Should create one task per row");

                tasks.forEach(Runnable::run);

                double[][] expected = {
                                { -1, 2 },
                                { 3, -4 }
                };

                assertArrayEquals(expected, leftMatrix.readRowMajor());
        }

        @Test
        void createNegateTasks_emptyMatrix_createsNoTasks() {
                double[][] empty = new double[0][0];

                leftMatrix.loadRowMajor(empty);

                List<Runnable> tasks = lae.createNegateTasks();

                assertTrue(tasks.isEmpty(), "No tasks should be created for empty matrix");
        }

        @Test
        void createNegateTasks_doubleNegateReturnsOriginal() {
                double[][] a = {
                                { 1, -2 },
                                { 3, -4 }
                };

                leftMatrix.loadRowMajor(a);

                lae.createNegateTasks().forEach(Runnable::run);
                lae.createNegateTasks().forEach(Runnable::run);

                assertArrayEquals(a, leftMatrix.readRowMajor());
        }

        @Test
        void createNegateTasks_zeroValuesRemainZero() {
                double[][] a = {
                                { 0, 0 },
                                { 0, 0 }
                };

                leftMatrix.loadRowMajor(a);

                List<Runnable> tasks = lae.createNegateTasks();
                tasks.forEach(Runnable::run);

                assertArrayEquals(a, leftMatrix.readRowMajor());
        }

        // TRANSPOSE TESTS
        @Test
        void createTransposeTasks_flipsOrientationToColumnMajor() {
                double[][] a = {
                                { 1, 2 },
                                { 3, 4 }
                };

                leftMatrix.loadRowMajor(a);

                List<Runnable> tasks = lae.createTransposeTasks();
                assertEquals(2, tasks.size());

                tasks.forEach(Runnable::run);

                assertEquals(
                                VectorOrientation.COLUMN_MAJOR,
                                leftMatrix.get(0).getOrientation(),
                                "Orientation should flip to COLUMN_MAJOR");
        }

        @Test
        void createTransposeTasks_doubleTransposeRestoresRowMajor() {
                double[][] a = {
                                { 1, 2 },
                                { 3, 4 }
                };

                leftMatrix.loadRowMajor(a);

                lae.createTransposeTasks().forEach(Runnable::run);
                lae.createTransposeTasks().forEach(Runnable::run);

                assertEquals(
                                VectorOrientation.ROW_MAJOR,
                                leftMatrix.get(0).getOrientation(),
                                "Double transpose should restore ROW_MAJOR");
        }

        // LOAD AND COMPUTE TEST
        @Test
        void loadAndCompute_add_normalCase() {
                double[][] a = {
                                { 1, 2, 3 },
                                { 4, 5, 6 }
                };
                double[][] b = {
                                { 6, 5, 4 },
                                { 3, 2, 1 }
                };

                ComputationNode root = opNode(ComputationNodeType.ADD, matrixNode(a), matrixNode(b));

                ComputationNode resultNode = lae.run(root);

                double[][] expected = {
                                { 7, 7, 7 },
                                { 7, 7, 7 }
                };

                assertArrayEquals(expected, resultNode.getMatrix());
        }

        @Test
        void loadAndCompute_multiply_normalCase() {
                double[][] a = {
                                { 1, 2, 3 },
                                { 4, 5, 6 }
                };
                double[][] b = {
                                { 7, 8 },
                                { 9, 10 },
                                { 11, 12 }
                };

                ComputationNode root = opNode(ComputationNodeType.MULTIPLY, matrixNode(a), matrixNode(b));

                ComputationNode resultNode = lae.run(root);

                double[][] expected = {
                                { 58, 64 },
                                { 139, 154 }
                };

                assertArrayEquals(expected, resultNode.getMatrix());
        }

        @Test
        void loadAndCompute_negate_normalCase() {
                double[][] a = {
                                { 0, -3, 5 }
                };

                ComputationNode root = opNode(ComputationNodeType.NEGATE, matrixNode(a));

                ComputationNode resultNode = lae.run(root);

                double[][] expected = {
                                { 0, 3, -5 }
                };

                assertArrayEquals(expected, resultNode.getMatrix());
        }

        @Test
        void loadAndCompute_transpose_orientationAndValues() {
                double[][] a = {
                                { 1, 2, 3 },
                                { 4, 5, 6 }
                };

                ComputationNode root = opNode(ComputationNodeType.TRANSPOSE, matrixNode(a));
                ComputationNode resultNode = lae.run(root);
                double[][] expected = {
                                { 1, 4 },
                                { 2, 5 },
                                { 3, 6 }
                };

                assertArrayEquals(expected, resultNode.getMatrix());
                for (int i = 0; i < leftMatrix.length(); i++) {
                        assertEquals(VectorOrientation.COLUMN_MAJOR, leftMatrix.get(i).getOrientation());
                }
        }

        @Test
        void loadAndCompute_add_mismatchedDimensions_throws() {
                double[][] a = {
                                { 1, 2 },
                                { 3, 4 }
                };
                double[][] b = {
                                { 1, 2, 3 },
                                { 4, 5, 6 }
                };

                ComputationNode root = opNode(ComputationNodeType.ADD, matrixNode(a), matrixNode(b));

                assertThrows(IllegalArgumentException.class, () -> lae.run(root));
        }

        @Test
        void loadAndCompute_multiply_invalidDimensions_throws() {
                double[][] a = {
                                { 1, 2, 3 }
                };
                double[][] b = {
                                { 1, 2 },
                                { 3, 4 }
                };

                ComputationNode root = opNode(ComputationNodeType.MULTIPLY, matrixNode(a), matrixNode(b));

                assertThrows(IllegalArgumentException.class, () -> lae.run(root));
        }

        // RUN TESTS

        @Test
        void run_threeOperandAddition() {
                double[][] a = { { 1, 2 }, { 3, 4 } };
                double[][] b = { { 5, 6 }, { 7, 8 } };
                double[][] c = { { 1, 0 }, { 0, 1 } };

                ComputationNode root = opNode(ComputationNodeType.MULTIPLY,
                                opNode(ComputationNodeType.ADD, matrixNode(a), matrixNode(b)),
                                matrixNode(c));

                ComputationNode result = lae.run(root);
                double[][] expected = {
                                { 6, 8 },
                                { 10, 12 }
                };

                assertArrayEquals(expected, result.getMatrix());
        }

        @Test
        void run_transposeTwoOperands_edge() {
                double[][] a = { { 1, 2, 3 } };
                double[][] b = { { 4, 5, 6 } };

                ComputationNode root = opNode(ComputationNodeType.TRANSPOSE,
                                matrixNode(a),
                                matrixNode(b));

                // Correct usage: exception is expected from this call
                IllegalStateException ex = assertThrows(IllegalStateException.class, () -> lae.run(root));
                assertTrue(ex.getMessage().contains("TRANSPOSE"));
        }

        @Test
        void run_addOneOperand_edge() {
                double[][] a = { { 1, 2 }, { 3, 4 } };

                ComputationNode root = opNode(ComputationNodeType.ADD, matrixNode(a));

                IllegalStateException ex = assertThrows(IllegalStateException.class, () -> lae.run(root));
                assertTrue(ex.getMessage().contains("ADD"));
        }

}
