public record HuffmanLeaf(int freq, char charASCII) implements HuffmanTree {

    /**
    The point of comesBefore function used in the leaf class is to
    compare which leaf/node is more frequent than the other

     This is used in compareTo which returns -1,0,1
     **/
    @Override
    public boolean comesBefore(HuffmanTree other) {
        if (this.freq() < other.freq()) {
            return true;
        } else if (this.freq() > other.freq()) {
            return false;
        } else {
            return this.charAscii() < other.charAscii();
        }
    }

    /**
     takes two nodes
     creates a new node with reference to two nodes

     new node takes the smaller of both ASCII
     references to nodes (left,right) dependent on  frequency of that node)
     */
    @Override
    public HuffmanTree combine(HuffmanTree other) {
        int totalFreq = this.freq() + other.freq();
        int smallerAscii = Math.min(this.charAscii(), other.charAscii());

        if (this.comesBefore(other)) {
            return new HuffmanNode(smallerAscii, totalFreq, this, other);
        } else {
            return new HuffmanNode(smallerAscii, totalFreq, other, this);
        }
    }

    // Returns a negative number, positive number, or zero based on ordering.
    @Override
    public int compareTo(HuffmanTree other) {
        if (this.comesBefore(other)) return -1;
        if (other.comesBefore(this)) return 1;
        return 0;
    }

    // Returns the leaf's character as an integer.
    @Override
    public int charAscii() {
        return charASCII;
    }
}