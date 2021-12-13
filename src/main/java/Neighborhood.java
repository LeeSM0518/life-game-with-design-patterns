import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

// 219p
public class Neighborhood implements Cell {

  private static final ConditionVariable readingPermitted = new ConditionVariable(true);

  private boolean amActive = false;

  private boolean oneLastRefreshRequired = false;

  private final Cell[][] grid;

  private final int gridSize;

  private Direction activeEdges = new Direction(Direction.NONE);

  private static int nestingLevel = -1;

  public Neighborhood(int gridSize, Cell prototype) {
    this.gridSize = gridSize;
    this.grid = new Cell[gridSize][gridSize];

    for (int row = 0; row < gridSize; row++) {
      for (int column = 0; column < gridSize; column++) {
        grid[row][column] = prototype.create();
      }
    }
  }

  @Override
  public Cell create() {
    return new Neighborhood(gridSize, grid[0][0]);
  }

  @Override
  public Direction isDisruptiveTo() {
    return activeEdges;
  }

  @Override
  public boolean figureNextState(
      final Cell north,
      final Cell south,
      final Cell east,
      final Cell west,
      final Cell northeast,
      final Cell northwest,
      final Cell southeast,
      final Cell southwest) {
    boolean nothingHappened = true;

    if (amActive
        || north.isDisruptiveTo().the(Direction.SOUTH)
        || south.isDisruptiveTo().the(Direction.NORTH)
        || east.isDisruptiveTo().the(Direction.WEST)
        || west.isDisruptiveTo().the(Direction.EAST)
        || northeast.isDisruptiveTo().the(Direction.SOUTHWEST)
        || northwest.isDisruptiveTo().the(Direction.SOUTHEAST)
        || southeast.isDisruptiveTo().the(Direction.NORTHWEST)
        || southwest.isDisruptiveTo().the(Direction.NORTHEAST)
    ) {
      Cell
          northCell,
          southCell,
          eastCell,
          westCell,
          northeastCell,
          northwestCell,
          southeastCell,
          southwestCell;

      activeEdges.clear();

      for (int row = 0; row < gridSize; row++) {
        for (int column = 0; column < gridSize; column++) {
          if (row == 0) {
            northwestCell = (column == 0)
                ? northwest.edge(gridSize - 1, gridSize - 1)
                : north.edge(gridSize - 1, column - 1);

            northCell = north.edge(gridSize - 1, column);

            northeastCell = (column == gridSize - 1)
                ? northeast.edge(gridSize - 1, 0)
                : north.edge(gridSize - 1, column + 1);
          } else {
            northwestCell = (column == 0)
                ? west.edge(row - 1, gridSize - 1)
                : grid[row - 1][column - 1];

            northCell = grid[row - 1][column];

            northeastCell = (column == gridSize - 1)
                ? east.edge(row - 1, 0)
                : grid[row - 1][column + 1];
          }

          westCell = (column == 0)
              ? west.edge(row, gridSize - 1)
              : grid[row][column - 1];

          eastCell = (column == gridSize - 1)
              ? east.edge(row, 0)
              : grid[row][column + 1];

          if (row == gridSize - 1) {
            southwestCell = (column == 0)
                ? southwest.edge(0, gridSize - 1)
                : south.edge(0, column - 1);

            southCell = south.edge(0, column);

            southeastCell = (column == gridSize - 1)
                ? southeast.edge(0, 0)
                : south.edge(0, column + 1);
          } else {
            southwestCell = (column == 0)
                ? west.edge(row + 1, gridSize - 1)
                : grid[row + 1][column - 1];

            southCell = grid[row + 1][column];

            southeastCell = (column == gridSize - 1)
                ? east.edge(row + 1, 0)
                : grid[row + 1][column + 1];
          }

          if (grid[row][column].figureNextState(
              northCell,
              southCell,
              eastCell,
              westCell,
              northeastCell,
              northwestCell,
              southeastCell,
              southwestCell
          )) {
            nothingHappened = false;
          }
        }
      }
    }
    if (amActive && nothingHappened) oneLastRefreshRequired = true;
    amActive = !nothingHappened;
    return amActive;
  }

  @Override
  public boolean transition() {
    boolean someSubcellChangedState = false;

    if (++nestingLevel == 0) readingPermitted.set(false);

    for (int row = 0; row < gridSize; row++)
      for (int column = 0; column < gridSize; column++)
        if (grid[row][column].transition()) {
          rememberThatCellAtEdgeChangedState(row, column);
          someSubcellChangedState = true;
        }

    if (nestingLevel-- == 0) readingPermitted.set(true);

    return someSubcellChangedState;
  }

  private void rememberThatCellAtEdgeChangedState(final int row, final int column) {
    if (row == 0) {
      activeEdges.add(Direction.NORTH);
    }
    if (column == 0) {
      activeEdges.add(Direction.NORTHWEST);
    } else if (column == gridSize - 1) {
      activeEdges.add(Direction.NORTHEAST);
    } else if (row == gridSize - 1) {
      activeEdges.add(Direction.SOUTH);
      if (column == 0) activeEdges.add(Direction.SOUTHWEST);
      else if (column == gridSize - 1) activeEdges.add(Direction.SOUTHEAST);
    }

    if (column == 0) {
      activeEdges.add(Direction.WEST);
    } else if (column == gridSize - 1) {
      activeEdges.add(Direction.EAST);
    }
  }

  @Override
  public void redraw(Graphics g, final Rectangle here, final boolean drawAll) {
    if (!amActive && !oneLastRefreshRequired && !drawAll) return;

    try {
      oneLastRefreshRequired = false;
      final int computedWidth = here.width;
      final Rectangle subcell = new Rectangle(here.x, here.y, here.width / gridSize, here.height / gridSize);

      if (!readingPermitted.isTrue()) return;

      readingPermitted.waitForTrue();

      for (int row = 0; row < gridSize; row++) {
        for (int column = 0; column < gridSize; column++) {
          grid[row][column].redraw(g, subcell, drawAll);
          subcell.translate(subcell.width, 0);
        }
        subcell.translate(-computedWidth, subcell.height);
      }

      g = g.create();
      g.setColor(Colors.LIGHT_ORANGE);
      g.drawRect(here.x, here.y, here.width, here.height);

      if (amActive) {
        g.setColor(Color.BLUE);
        g.drawRect(here.x + 1, here.y + 1, here.width - 2, here.height - 2);
      }

      g.dispose();
    } catch (InterruptedException e) {
    }
  }

  public Cell edge(int row, int column) {
    assert (row == 0 || row == gridSize - 1)
        || (column == 0 || column == gridSize - 1)
        : "central cell requested from edge()";

    return grid[row][column];
  }

  @Override
  public void userClicked(final Point here, final Rectangle surface) {
    final int pixelPerCell = surface.width / gridSize;
    final int row = here.y / pixelPerCell;
    final int column = here.x / pixelPerCell;
    final int rowOffset = here.y % pixelPerCell;
    final int columnOffset = here.x % pixelPerCell;

    final Point position = new Point(columnOffset, rowOffset);
    final Rectangle subcell = new Rectangle(0, 0, pixelPerCell, pixelPerCell);

    grid[row][column].userClicked(position, subcell);
    amActive = true;
    rememberThatCellAtEdgeChangedState(row, column);
  }

  @Override
  public boolean isAlive() {
    return true;
  }

  @Override
  public int widthInCells() {
    return gridSize * grid[0][0].widthInCells();
  }

  @Override
  public void clear() {
    activeEdges.clear();

    for (int row = 0; row < gridSize; row++)
      for (int column = 0; column < gridSize; column++)
        grid[row][column].clear();

    amActive = false;
  }

  @Override
  public boolean transfer(final Storable memento, final Point corner, final boolean load) {
    final int subcellWidth = grid[0][0].widthInCells();
    final int myWidth = widthInCells();
    final Point upperLeft = new Point(corner);

    for (int row = 0; row < gridSize; row++) {
      for (int column = 0; column < gridSize; column++) {
        if (grid[row][column].transfer(memento, upperLeft, load))
          amActive = true;

        final Direction d = grid[row][column].isDisruptiveTo();

        if (!d.equals(Direction.NONE)) activeEdges.add(d);

        upperLeft.translate(subcellWidth, 0);
      }
      upperLeft.translate(-myWidth, subcellWidth);
    }
    return amActive;
  }

  @Override
  public Storable createMemento() {
    final Memento m = new NeighborhoodState();
    transfer(m, new Point(0, 0), Cell.STORE);
    return m;
  }

  private static class NeighborhoodState implements Cell.Memento {
    Collection liveCells = new LinkedList();

    public NeighborhoodState(InputStream in) throws IOException {
      load(in);
    }

    public NeighborhoodState() {
    }

    public void load(InputStream in) throws IOException {
      try {
        ObjectInputStream source = new ObjectInputStream(in);
        liveCells = (Collection) (source.readObject());
      } catch (ClassNotFoundException e) {
        throw new IOException("Internal Error: Class not found on load");
      }
    }

    public void flush(OutputStream out) throws IOException {
      final ObjectOutputStream sink = new ObjectOutputStream(out);
      sink.writeObject(liveCells);
    }

    public void markAsAlive(Point location) {
      liveCells.add(new Point(location));
    }

    public boolean isAlive(Point location) {
      return liveCells.contains(location);
    }

    @Override
    public String toString() {
      StringBuffer b = new StringBuffer();

      b.append("NeighborhoodState:\n");
      for (Iterator i = liveCells.iterator(); i.hasNext(); b.append(i.next().toString()).append("\n"));
      return b.toString();
    }
  }

}
