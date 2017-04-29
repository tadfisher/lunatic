package lunatic;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import lunatic.SparseIntervalTree;

public class SparseIntervalTreeTest {

  @Test
  public void findBasic() {
    SparseIntervalTree<String> tree = SparseIntervalTree.create(String.class);
    String first = "first";
    String second = "second";
    tree.set(first, 1, 1);
    tree.set(second, 2, 2);
    String[] values;

    values = tree.find(1, 1);
    assertArrayEquals(new String[]{first}, values);

    values = tree.find(2, 2);
    assertArrayEquals(new String[]{second}, values);

    values = tree.find(1, 2);
    assertArrayEquals(new String[]{first, second}, values);
  }

  @Test
  public void findPointsInSingleton() {
    SparseIntervalTree<String> tree = SparseIntervalTree.create(String.class);
    String value = "value";
    tree.set(value, 1, 3);
    String[] expected = new String[]{value};
    String[] values;

    values = tree.find(1, 1);
    assertArrayEquals("Point query on start", expected, values);

    values = tree.find(2, 2);
    assertArrayEquals("Point query in middle", expected, values);

    values = tree.find(3, 3);
    assertArrayEquals("Point query on end", expected, values);

    values = tree.find(0, 0);
    assertArrayEquals("Non-overlapping query before start", new String[]{}, values);

    values = tree.find(4, 4);
    assertArrayEquals("Non-overlapping query after end", new String[]{}, values);
  }

  @Test
  public void findMultivaluedInterval() {
    SparseIntervalTree<String> tree = SparseIntervalTree.create(String.class);
    String first = "first";
    String second = "second";
    tree.set(first, 5, 10);
    tree.set(second, 5, 10);
    String[] values;

    values = tree.find(6, 6);
    assertArrayEquals(new String[]{"first", "second"}, values);
  }
}
