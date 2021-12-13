import java.util.NoSuchElementException;

public class Test {
  static final StringBuffer actualResults = new StringBuffer();
  static final StringBuffer expectedResults = new StringBuffer();

  interface Observer {
    void notify(String arg);
  }

  static class Notifier {
    private Publisher publisher = new Publisher();

    public void addObserver(Observer observer) {
      publisher.subscribe(observer);
    }

    public void removeObserver(Observer observer) {
      publisher.cancelSubscription(observer);
    }

    public void fire(final String arg) {
      publisher.publish(new Publisher.Distributor() {
        @Override
        public void deliverTo(final Object subscriber) {
          ((Observer) subscriber).notify(arg);
        }
      });
    }
  }

  public static void main(String[] args) {
    Test.Notifier source = new Test.Notifier();
    int errors = 0;

    Test.Observer listener1 = new Test.Observer() {
      @Override
      public void notify(final String arg) {
        Test.actualResults.append("1[" + arg + "]");
      }
    };

    Test.Observer listener2 = new Test.Observer() {
      @Override
      public void notify(final String arg) {
        Test.actualResults.append("2[" + arg + "]");
      }
    };

    source.addObserver(listener1);
    source.addObserver(listener2);

    source.fire("a");
    source.fire("b");

    Test.expectedResults.append("2[a]");
    Test.expectedResults.append("1[a]");
    Test.expectedResults.append("2[b]");
    Test.expectedResults.append("1[b]");

    source.removeObserver(listener1);

    try {
      source.removeObserver(listener1);
      System.err.print("Removed nonexistent node!");
      ++errors;
    } catch (NoSuchElementException e) {

    }

    Test.expectedResults.append("2[c]");
    source.fire("c");

    if (!Test.expectedResults.toString().equals(Test.actualResults.toString())) {
      System.err.println("add/remove/fire failure.");
      System.err.println("Expected:[" + Test.expectedResults + "]");
      System.err.println("Actual: [" + Test.actualResults + "]");
      ++errors;
    }

    source.removeObserver(listener2);
    source.fire("Hello World");
    try {
      source.removeObserver(listener2);
      System.err.println("Undetected illegal removal");
      ++errors;
    } catch (Exception e) {
    }

    if (errors == 0) {
      System.err.println("Publisher: OKAY");
    }
    System.exit(errors);
  }
}
