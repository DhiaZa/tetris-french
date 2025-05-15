import java.util.Random;

// Énumération des différentes formes de Tetrominoes
enum Tetrominoe {
    NoShape, ZShape, SShape, LineShape, TShape, SquareShape, LShape, MirroredLShape
}

public class Shape {

    private Tetrominoe pieceShape;
    private int[][] coords;
    private int[][][] coordsTable;


    public Shape() {
        coords = new int[4][2];
        initCoordsTable();
        setShape(Tetrominoe.NoShape);
    }

    private void initCoordsTable() {
        coordsTable = new int[][][] {
                { { 0, 0 },   { 0, 0 },   { 0, 0 },   { 0, 0 } }, // NoShape
                { { 0, -1 },  { 0, 0 },   { -1, 0 },  { -1, 1 } }, // ZShape
                { { 0, -1 },  { 0, 0 },   { 1, 0 },   { 1, 1 } },   // SShape
                { { 0, -1 },  { 0, 0 },   { 0, 1 },   { 0, 2 } },   // LineShape
                { { -1, 0 },  { 0, 0 },   { 1, 0 },   { 0, 1 } },   // TShape
                { { 0, 0 },   { 1, 0 },   { 0, 1 },   { 1, 1 } },   // SquareShape
                { { -1, -1 }, { 0, -1 },  { 0, 0 },   { 0, 1 } },   // LShape
                { { 1, -1 },  { 0, -1 },  { 0, 0 },   { 0, 1 } }    // MirroredLShape
        };
    }

    public void setShape(Tetrominoe shape) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; ++j) {
                coords[i][j] = coordsTable[shape.ordinal()][i][j];
            }
        }
        pieceShape = shape;
    }

    public void setRandomShape() {
        Random r = new Random();
        int x = r.nextInt(7) + 1;
        Tetrominoe[] values = Tetrominoe.values();
        setShape(values[x]);
    }

    public int x(int index) { return coords[index][0]; }
    public int y(int index) { return coords[index][1]; }

    public Tetrominoe getShape() { return pieceShape; }

    public int minY() {
        int m = coords[0][1];
        for (int i = 0; i < 4; i++) {
            m = Math.min(m, coords[i][1]);
        }
        return m;
    }

    public Shape rotateLeft() {
        if (pieceShape == Tetrominoe.SquareShape) {
            return this;
        }

        Shape result = new Shape();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; ++i) {
            result.coords[i][0] = y(i);
            result.coords[i][1] = -x(i);
        }
        return result;
    }

    public Shape rotateRight() {
        if (pieceShape == Tetrominoe.SquareShape) {
            return this;
        }

        Shape result = new Shape();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; ++i) {
            result.coords[i][0] = -y(i);
            result.coords[i][1] = x(i);
        }
        return result;
    }
}