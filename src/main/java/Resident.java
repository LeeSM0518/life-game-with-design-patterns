import java.awt.*;

// 216p
public class Resident implements Cell {

  public static final Color BORDER_COLOR = Colors.DARK_YELLOW;
  public static final Color LIVE_COLOR = Colors.LIGHT_RED;
  public static final Color DEAD_COLOR = Colors.LIGHT_YELLOW;

  private boolean amAlive = false;
  private boolean willBeAlive = false;

  private boolean isStable() {
    return amAlive == willBeAlive;
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
    verify(north, "north");
    verify(south, "south");
    verify(east, "east");
    verify(west, "west");
    verify(northeast, "northeast");
    verify(northwest, "northwest");
    verify(southeast, "southeast");
    verify(southwest, "southwest");

    int neighbors = 0;

    if (north.isAlive()) ++neighbors;
    if (south.isAlive()) ++neighbors;
    if (east.isAlive()) ++neighbors;
    if (west.isAlive()) ++neighbors;
    if (northeast.isAlive()) ++neighbors;
    if (northwest.isAlive()) ++neighbors;
    if (southeast.isAlive()) ++neighbors;
    if (southwest.isAlive()) ++neighbors;

    willBeAlive = (neighbors == 3 || (amAlive && neighbors == 2));
    return !isStable();
  }

  private void verify(final Cell cell, final String direction) {
    assert (cell instanceof Resident) || (cell == Cell.DUMMY) :
        "incorrect type for " + direction + ": " + cell.getClass().getName();
  }

  @Override
  public Cell edge(final int row, final int column) {
    assert row == 0 && column == 0;
    return this;
  }

  @Override
  public boolean transition() {
    final boolean changed = isStable();
    amAlive = this.willBeAlive;
    return changed;
  }

  @Override
  public void redraw(Graphics g, final Rectangle here, final boolean drawAll) {
    g = g.create();
    g.setColor(amAlive ? LIVE_COLOR : DEAD_COLOR);
    g.fillRect(here.x + 1, here.y + 1, here.width - 1, here.height - 1);
    g.setColor(BORDER_COLOR);
    g.drawLine(here.x, here.y, here.x, here.y + here.height);
    g.drawLine(here.x, here.y, here.x + here.width, here.y);
  }

  @Override
  public void userClicked(final Point here, final Rectangle surface) {
    amAlive = !amAlive;
  }

  @Override
  public boolean isAlive() {
    return amAlive;
  }

  @Override
  public int widthInCells() {
    return 1;
  }

  @Override
  public Cell create() {
    return new Resident();
  }

  @Override
  public Direction isDisruptiveTo() {
    return isStable() ? Direction.NONE : Direction.ALL;
  }

  @Override
  public void clear() {
    amAlive = willBeAlive = false;
  }

  @Override
  public boolean transfer(final Storable blob, final Point upperLeft, final boolean doLoad) {
    final Memento memento = (Memento) blob;
    if (doLoad) {
      return amAlive = willBeAlive = memento.isAlive(upperLeft);
    } else if (amAlive) {
      memento.markAsAlive(upperLeft);
    }
    return false;
  }

  @Override
  public Storable createMemento() {
    throw new UnsupportedOperationException("May not create memento of a unitary cell");
  }
}
