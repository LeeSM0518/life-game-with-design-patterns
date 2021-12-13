import javax.swing.*;
import java.awt.*;
import java.util.NoSuchElementException;

// 163p
public class Publisher {

  // TODO volatile 설명 필요
  private volatile Node subscribers = null;

  public void publish(Distributor deliveryAgent) {
    for (Node cursor = subscribers; cursor != null; cursor = cursor.next)
      cursor.accept(deliveryAgent);
  }

  public void subscribe(Object subscriber) {
    subscribers = new Node(subscriber, subscribers);
  }

  public void cancelSubscription(Object subscriber) {
    subscribers = subscribers.remove(subscriber);
  }

  public interface Distributor {
    // Visitor 패턴의 'visit' 메소드
    void deliverTo(Object subscriber);
  }

  /**
   * Node 클래스의 필드는 final이므로 불변이기 때문에
   * 필드에 직접 접근할 수 있도록 하였다.
   * 이 클래스는 private 내부 클래스이므로 캡슐화 원리를 일부 위반하더라도
   * 코드의 유지 보수성에 영향을 끼치지 않는다.
   */
  private class Node {
    public final Object subscriber;
    public final Node next;

    private Node(Object subscriber, Node next) {
      this.subscriber = subscriber;
      this.next = next;
    }


    public Node remove(Object target) {
      if (target == subscriber) return next;
      if (next == null) throw new NoSuchElementException(target.toString());
      return new Node(subscriber, next.remove(target));
    }

    public void accept(Distributor deliveryAgent) {
      deliveryAgent.deliverTo(subscriber);
    }
  }

}
