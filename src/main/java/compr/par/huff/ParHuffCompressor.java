package compr.par.huff;

import compr.par.ParCompressor;
import io.bitstream.BitOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import test.TimeLogger;

/**
 * @author Georgi Krastev <joro.kr.21@gmail.com>
 */
public class ParHuffCompressor extends ParCompressor {

    public static final int BUF = 64 * 1024; // 64 KB
    private static final TimeLogger logger = TimeLogger.getLogger();

    public ParHuffCompressor(int tasks) {
        super(tasks);
    }

    public ParHuffCompressor() {
        this(1);
    }

    @Override
    public void compress(File fin, File fout)
            throws IOException, InterruptedException, ExecutionException {
        logger.resetTime();
        logger.log(MessageFormat.format("Starting compression of file {0} with {1} task{2}",
                fin.getName(), getTasks(), getTasks() > 1 ? "s" : ""));
        Collection<ByteSym> symbols = makeSymbols(makeHuffTree(makeFreqTable(fin)));
        encode(fin, fout, symbols, makeCodeTable(symbols));
    }

    public void printFreqTable(File fin, File fout) throws IOException, InterruptedException {
        Writer out = new FileWriter(fout);
        try {
            logger.resetTime();
//            logger.log(MessageFormat.format("Building frequency table of file {0} with {1} task{2}",
//                    fin.getName(), getTasks(), getTasks() > 1 ? "s" : ""));
            AtomicLong[] freqTable = makeFreqTable(fin);
            StringBuilder table = new StringBuilder();
            for (int sym = 0; sym < ByteSym.EoF; sym++) {
                table.append(String.format("%03d: %d\n", sym, freqTable[sym].get()));
            }

            table.append(String.format("EoF: %d\n", freqTable[ByteSym.EoF].get()));
            out.write(table.toString());
            out.flush();
//            logger.log("Frequency table saved to file " + fout.getName());
        } finally {
            out.close();
        }
    }

    public void printFreqTable(String fin, String fout) throws IOException, InterruptedException {
        printFreqTable(new File(fin), new File(fout));
    }

    private AtomicLong[] makeFreqTable(File fin) throws InterruptedException {
        int tasks = getTasks();
        ExecutorService exec = Executors.newFixedThreadPool(tasks);
        AtomicLong[] freqTable = new AtomicLong[ByteSym.RANGE];
        for (int sym = 0; sym < ByteSym.RANGE; sym++) {
            freqTable[sym] = new AtomicLong();
        }

        freqTable[ByteSym.EoF].incrementAndGet();
        Collection<Counter> counters = new ArrayList<Counter>(tasks);
        for (int t = 0; t < tasks; t++) {
            counters.add(new Counter(fin, t, tasks, freqTable));
        }

        exec.invokeAll(counters);
        exec.shutdown();
        logger.log("Frequency table completed");
        return freqTable;
    }

    private HuffNode makeHuffTree(AtomicLong[] freqTable) {
        TreeSet<HuffNode> set = new TreeSet<HuffNode>();
        for (int sym = 0; sym < ByteSym.RANGE; sym++) {
            long cnt = freqTable[sym].get();
            if (cnt > 0) {
                set.add(new HuffNode(sym, cnt));
            }
        }

        while (set.size() > 1) {
            HuffNode lo = set.pollFirst(), hi = set.pollFirst();
            set.add(new HuffNode(lo, hi));
        }

        logger.log("Huffman tree completed");
        return set.first();
    }

    private Collection<ByteSym> makeSymbols(HuffNode root) {
        List<ByteSym> symbols = new ArrayList<ByteSym>(ByteSym.RANGE);
        Stack<HuffNode> stack = new Stack<HuffNode>();
        stack.push(root);
        if (root.isLeaf()) {
            root.setBits(1);
        }

        while (!stack.empty()) {
            HuffNode node = stack.pop();
            if (node.isLeaf()) {
                symbols.add(new ByteSym(node.getSym(), node.getBits()));
            } else {
                node.lo.setBits(node.getBits() + 1);
                node.hi.setBits(node.getBits() + 1);
                stack.push(node.lo);
                stack.push(node.hi);
            }
        }

        Collections.sort(symbols);
        logger.log("Bit table completed");
        return Collections.unmodifiableCollection(symbols);
    }

    private int[] makeCodeTable(Collection<ByteSym> symbols) {
        int[] codeTable = new int[ByteSym.RANGE];
        byte bits = 1;
        int code = -1;
        for (ByteSym sym : symbols) {
            code = ++code << (sym.getBits() - bits);
            bits = sym.getBits();
            codeTable[sym.intVal()] = code;
        }

        logger.log("Code table completed");
        return codeTable;
    }

    private void encode(File fin, File fout, Collection<ByteSym> symbols, int[] codeTable)
            throws IOException {
        InputStream in = new FileInputStream(fin);
        BitOutputStream out = new BitOutputStream(
                new BufferedOutputStream(new FileOutputStream(fout), BUF));
        try {
            byte[] buf = new byte[BUF];
            byte[] bitTable = new byte[ByteSym.RANGE];
            for (ByteSym sym : symbols) {
                bitTable[sym.intVal()] = sym.getBits();
            }

            out.write(bitTable);
            int l;
            while ((l = in.read(buf)) > 0) { // NOTE: nested assignment
                for (int i = 0; i < l; i++) {
                    int sym = ByteSym.uByte(buf[i]);
                    out.write(bitTable[sym], codeTable[sym]);
                }
            }

            out.write(bitTable[ByteSym.EoF], codeTable[ByteSym.EoF]);
            out.flush();
            long ratio = Math.round((double) fout.length() / fin.length() * 100);
            logger.log(MessageFormat.format(
                    "Compression completed to file {0} with ratio {1}%",
                    fout.getName(), ratio));
        } finally {
            in.close();
            out.close();
        }
    }

    public static void main(String[] args) {
        Option fin = OptionBuilder.isRequired().hasArg().withArgName("filename")
                .withLongOpt("input").withDescription("input file (*required)").create("i");
        Option fout = OptionBuilder.isRequired(false).hasArg().withArgName("filename")
                .withLongOpt("output").withDescription("output file").create("o");
        Option tasks = OptionBuilder.isRequired(false).hasArg().withArgName("number")
                .withType(Integer.class).withLongOpt("tasks")
                .withDescription("number of concurrent tasks").create("t");
        Option quiet = OptionBuilder.isRequired(false).hasArg(false).withLongOpt("quiet")
                .withDescription("indicate only progress").create("q");

        Options options = new Options();
        options.addOption(fin);
        options.addOption(fout);
        options.addOption(tasks);
        options.addOption(quiet);
        CommandLineParser parser = new BasicParser();
        ParHuffCompressor compr = new ParHuffCompressor();

        try {
            CommandLine cmd = parser.parse(options, args);
            logger.setQuiet(cmd.hasOption("q"));
            String t = cmd.getOptionValue("t");
            String in = cmd.getOptionValue("i");
            String out = cmd.getOptionValue("o");
            out = out == null ? in + ".hfm" : out;
            compr.setTasks(t == null ? 1 : Integer.parseInt(t));
            compr.compress(in, out);
        } catch (Exception ex) {
            HelpFormatter help = new HelpFormatter();
            help.printHelp("java ParHuffCompressor", "options:", options,
                    "\nCompress the input file to the output file\n"
                    + "using Huffman coding with -t tasks.\n");
        }
    }
}
