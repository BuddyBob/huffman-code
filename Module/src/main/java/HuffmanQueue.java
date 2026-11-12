import java.util.PriorityQueue;

public class HuffmanQueue implements HQueue {
    private PriorityQueue<HuffmanTree> trees = new PriorityQueue<>();

    @Override
    public boolean isEmpty() {
        return trees.isEmpty();
    }

    @Override
    public int count() {
        return trees.size();
    }

    @Override
    public void enqueue(HuffmanTree tree) {
        trees.add(tree);
    }

    @Override
    public HuffmanTree dequeue() {
        return trees.poll();
    }
}

