import java.io.*;
import java.util.*;

/**
 * Project 3: Huffman Encoding / Decoding.
 *
 * Implement the methods below to compress and decompress text files using
 * Huffman coding. See the assignment write-up for the full specification.
 *
 *  - Treat characters as 8-bit values in the range [0, 255].
 *  - When writing output files, write the newline character explicitly as
 *    '\n' (do not rely on System.lineSeparator() / println()), so your
 *    output is identical across Windows/Mac/Linux. A BufferedWriter over
 *    a FileWriter, writing "\n" yourself, works well for this.
 *  - Encoded files store the code as literal '0'/'1' characters (one byte
 *    each), not as packed bits, so the "encoded" file will actually be
 *    larger than the original -- that's expected for this assignment.
 *  - createHuffTree() is built on top of addNode()/removeMin(), which
 *    operate generically over a java.util.Collection<HuffmanNode>.
 *  - HuffmanNode is a record, so its components are read with
 *    node.charAscii(), node.freq(), node.left(), and node.right() -- no
 *    "get" prefix.
 */
public class Huffman {

    /** Number of possible 8-bit character values. */
    public static final int NUM_CHARS = 256;

    /**
     * Opens the text file named filename and counts the frequency of
     * occurrence of every character in it.
     * Returns an int array of size NUM_CHARS, indexed by ASCII value, with
     * the count of each character (0 for characters that do not occur).
     */
    public static int[] cntFreq(String filename) throws IOException {
        int[] freq = new int[NUM_CHARS];
        try (InputStream input = new FileInputStream(filename)) {
            int character;

            while ((character = input.read()) != -1) {
                freq[character]++;
            }
        }
        return freq;
    }


    /**
     * Builds a Huffman tree from an array of character frequencies (as
     * produced by cntFreq()) and returns the root HuffmanNode of the tree.
     *
     * Use an HQueue to manage the collection of
     * not-yet-combined nodes as you build the tree.
     *
     * Special cases:
     *  - If freq contains no non-zero counts (empty input file), return null
     *  - If freq contains exactly one non-zero count (input file has only
     *    one unique character), return a single leaf HuffmanNode for that
     *    character (no combine() call needed).
     */
    public static HuffmanNode createHuffTree(int[] freq) {
        HQueue queue = new HuffmanQueue();

        // Create a leaf for each character that appears.
        for (int i = 0; i < freq.length; i++) {
            if (freq[i] > 0) {
                HuffmanLeaf leaf = new HuffmanLeaf(freq[i], (char) i);
                queue.enqueue(leaf);
            }
        }

        // Combine the two smallest trees until only one remains.
        while (queue.count() > 1) {
            //dequeue -> combine (takes care of everything in terms of ordering) -> enqueue the new node
            HuffmanTree first = queue.dequeue();
            HuffmanTree second = queue.dequeue();

            HuffmanTree combined = first.combine(second);
            queue.enqueue(combined);
        }

        // poll() returns null if the queue is empty.
        return (HuffmanNode) queue.dequeue();
    }

    /**
     * Traverses the Huffman tree rooted at root and builds the Huffman
     * code for every character that appears in it. Going left appends
     * '0' to the code, going right appends '1'.
     *
     * Returns a String array of size NUM_CHARS, indexed by ASCII value.
     * Characters that do not appear in the tree have an empty string ""
     * at their index. The same is true of the lone character in a
     * single-node tree (the only-one-unique-character case) - it has no
     * left/right branch to build a code out of; huffmanEncode/huffmanDecode
     * handle that case entirely through the header.
     */
    public static String[] createCode(HuffmanTree root) {
        String[] codes = new String[NUM_CHARS];
        Arrays.fill(codes, "");
        buildCodes(root, "", codes);
        return codes;
    }

    private static void buildCodes(HuffmanTree tree, String currentCode, String[] codes) {
        if (tree == null) {
            return;
        }
        HuffmanNode node = (HuffmanNode) tree;
        // If there are no children, this is a leaf.
        if (node.left() == null && node.right() == null) {
            codes[node.charAscii()] = currentCode;
            return;
        }

        buildCodes(node.left(), currentCode + "0", codes);
        buildCodes(node.right(), currentCode + "1", codes);
    }


    /**
     * Given a frequency array (as produced by cntFreq()), returns a
     * header string listing the ASCII values and their associated
     * frequencies, separated by single spaces, in ascending order of
     * ASCII value. Only characters with a non-zero count are included.
     *
     * Example: for the text "aaabbbbcc" this returns "97 3 98 4 99 2".
     */
    public static String createHeader(int[] freq) {
        StringBuilder header = new StringBuilder();

        for (int i = 0; i < freq.length; i++) {
            if (freq[i] > 0) {
                // Add a space before every pair except the first one.
                if (header.length() > 0) {
                    header.append(" ");
                }

                header.append(i);
                header.append(" ");
                header.append(freq[i]);
            }
        }

        return header.toString();
    }

    /**
     * Reads the input text file inFile and writes to outFile:
     *   1. A header line (see createHeader), followed by a newline
     *   2. The Huffman-encoded contents of inFile, using the codes from
     *      createCode() -- UNLESS inFile has only one unique character,
     *      see below.
     * If outFile already exists, its old contents are overwritten.
     *
     * Special cases:
     *  - Empty input file: outFile should end up empty too
     *  - Input file with only one unique character: should contain only the
     *    header line (e.g. "97 5") and nothing else - no second line of code bits.
     */
    public static void huffmanEncode(String inFile, String outFile)
            throws IOException {

        int[] freq = cntFreq(inFile);
        HuffmanNode root = createHuffTree(freq);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {

            // An empty input produces an empty output.
            if (root == null) {
                return;
            }

            writer.write(createHeader(freq));
            writer.write("\n");

            // A single character needs only the header.
            if (root.left() == null && root.right() == null) {
                return;
            }

            String[] codes = createCode(root);

            try (InputStream input = new FileInputStream(inFile)) {
                int character;

                while ((character = input.read()) != -1) {
                    writer.write(codes[character]);
                }
            }
        }
    }

    /**
     * Parses a header string (in the format produced by createHeader) and
     * returns the corresponding frequency array of size NUM_CHARS, indexed
     * by ASCII value, i.e. the same format returned by cntFreq().
     *
     * Example: for the header "97 3 98 4 99 2", freq[97, 98, 99] == {3, 4, 2}.
     */
    public static int[] parseHeader(String headerString) {
        int[] freq = new int[NUM_CHARS];

        if (headerString == null || headerString.trim().isEmpty()) {
            return freq;
        }

        String[] values = headerString.trim().split("\\s+");

        for (int i = 0; i < values.length; i += 2) {
            int character = Integer.parseInt(values[i]);
            int count = Integer.parseInt(values[i + 1]);

            freq[character] = count;
        }

        return freq;
    }

    /**
     * Reads an encoded file (in the format written by huffmanEncode) and
     * writes the decoded text to decodeFile.
     *
     * Recreate the frequency array from the header with parseHeader(),
     * rebuild the Huffman tree with createHuffTree(), then walk the tree
     * for each bit of the second line: '0' -> left child, '1' -> right
     * child. When a leaf is reached, emit its character and restart at
     * the root.
     *
     * Special case: if the recreated tree is a single leaf (the
     * only-one-unique-character case), there is no second line to read
     * The header's count alone tells you how many times that character occurred,
     * so write it out that many times directly.
     */
    public static void huffmanDecode(String encodedFile, String decodeFile)
            throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(encodedFile));
             OutputStream output = new FileOutputStream(decodeFile)) {

            String header = reader.readLine();

            // An empty encoded file produces an empty decoded file.
            if (header == null || header.isEmpty()) {
                return;
            }

            int[] freq = parseHeader(header);
            HuffmanNode root = createHuffTree(freq);

            if (root == null) {
                return;
            }

            // For one unique character, the header provides the count.
            if (root.left() == null && root.right() == null) {
                for (int i = 0; i < root.freq(); i++) {
                    output.write(root.charAscii());
                }
                return;
            }

            HuffmanNode current = root;
            int bit;

            while ((bit = reader.read()) != -1) {
                if (bit == '0') {
                    current = (HuffmanNode) current.left();
                } else if (bit == '1') {
                    current = (HuffmanNode) current.right();
                } else {
                    throw new IOException("Invalid encoded character: " + (char) bit);
                }

                // Once we reach a leaf, write its character and restart.
                if (current.left() == null && current.right() == null) {
                    output.write(current.charAscii());
                    current = root;
                }
            }
        }
    }
}
