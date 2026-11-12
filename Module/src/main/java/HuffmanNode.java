/**
 * A single node in a Huffman tree, implemented as a record.
 *
 * A HuffmanNode represents either a leaf (an actual character together
 * with its occurrence count) or an internal node created by combining two
 * subtrees during tree construction.
 *
 * Records are immutable and give you charAscii(), freq(), left(), and
 * right() accessor methods automatically, along with
 * equals()/hashCode()/toString(). Do not rename the components below.
 */
public record HuffmanNode(int charAscii, int freq, HuffmanTree left, HuffmanTree right)
        implements HuffmanTree{

     public boolean comesBefore(HuffmanTree other) {
        if (this.freq < other.freq()){
            return true;
        }
        else if (this.freq > other.freq()){
            return false;
        }
        else{
            return this.charAscii() < other.charAscii();
        }
    }

    /*
    Crete an internal node that has references to smaller and larger nodes
    Make sure to push smaller freq or ascii to left and larger to right
     */
    public HuffmanTree combine(HuffmanTree other) {
        int decision = this.compareTo(other);
        int totalFreq = this.freq() + other.freq();
        int smallerAscii = Math.min(this.charAscii(), other.charAscii());

        //push left
        if (decision < 0) {
            return new HuffmanNode(smallerAscii, totalFreq, this, other);
        //push right
        } else {
            return new HuffmanNode(smallerAscii, totalFreq, other, this);
        }
    }

    /**
     * Natural ordering consistent with comesBefore(), so that HuffmanNode
     * objects can be stored directly in a sorted structure (e.g. a
     * PriorityQueue or a sorted List) while building the tree.
     */
    @Override
    public int compareTo(HuffmanTree other) {
        if (this.comesBefore(other)) return -1;
        if (other.comesBefore(this)) return 1;
        return 0;
    }
}
