import java.awt.*;

// 212p
public interface Cell {

  boolean figureNextState(Cell north,
                          Cell south,
                          Cell east,
                          Cell west,
                          Cell northeast,
                          Cell northwest,
                          Cell southeast,
                          Cell southwest);

  Cell edge(int row, int column);

  boolean transition();

  void redraw(Graphics g, Rectangle here, boolean drawAll);

  void userClicked(Point here, Rectangle surface);

  boolean isAlive();

  int widthInCells();

  Cell create();

  Direction isDisruptiveTo();

  void clear();

  interface Memento extends Storable {
    void markAsAlive(Point location);

    boolean isAlive(Point location);
  }

  boolean transfer(Storable memento, Point upperLeftCorner, boolean doLoad);

  boolean STORE = false;
  boolean LOAD = true;

  Storable createMemento();

  Cell DUMMY = new Cell() {
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
      return true;
    }

    @Override
    public Cell edge(final int row, final int column) {
      return this;
    }

    @Override
    public boolean transition() {
      return false;
    }

    @Override
    public void redraw(final Graphics g, final Rectangle here, final boolean drawAll) {
    }

    @Override
    public void userClicked(final Point here, final Rectangle surface) {
    }

    @Override
    public boolean isAlive() {
      return false;
    }

    @Override
    public int widthInCells() {
      return 0;
    }

    @Override
    public Cell create() {
      return this;
    }

    @Override
    public Direction isDisruptiveTo() {
      return Direction.NONE;
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean transfer(final Storable memento, final Point upperLeftCorner, final boolean doLoad) {
      return false;
    }

    @Override
    public Storable createMemento() {
      throw new UnsupportedOperationException("Cannot create memento of dummy block");
    }
  };


}
