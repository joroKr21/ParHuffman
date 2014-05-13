/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compr.par.huff;

import compr.seq.Expander;
import io.bitstream.BitInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
public class HuffExpander implements Expander {

    public static final int BUF = 64 * 1024; // 64 KB
    private static final TimeLogger logger = TimeLogger.getLogger();

    @Override
    public void expand(File fin, File fout) throws IOException {
        logger.resetTime();
        logger.log("Starting extraction of file " + fin.getName());
        Collection<ByteSym> symbols = makeSymbols(fin);
        decode(fin, fout, makeHuffTree(symbols, makeCodeTable(symbols)));
        logger.log("Extraction completed to file " + fout.getName());
    }

    @Override
    public void expand(String fin, String fout) throws IOException {
        expand(new File(fin), new File(fout));
    }

    private Collection<ByteSym> makeSymbols(File fin) throws IOException {
        InputStream in = new FileInputStream(fin);
        try {
            List<ByteSym> symbols = new ArrayList<ByteSym>(ByteSym.RANGE);
            byte[] bitTable = new byte[ByteSym.RANGE];
            in.read(bitTable);
            for (int sym = 0; sym < ByteSym.RANGE; sym++) {
                if (bitTable[sym] > 0) {
                    symbols.add(new ByteSym(sym, bitTable[sym]));
                }
            }

            Collections.sort(symbols);
            logger.log("Bit table completed");
            return Collections.unmodifiableCollection(symbols);
        } finally {
            in.close();
        }
    }

    private int[] makeCodeTable(Collection<ByteSym> symbols) throws IOException {
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

    private HuffNode makeHuffTree(Collection<ByteSym> symbols, int[] codeTable) {
        HuffNode root = new HuffNode(null, null);
        for (ByteSym sym : symbols) {
            HuffNode cur = root;
            for (int b = sym.getBits() - 1; b > 0; b--) {
                if (((codeTable[sym.intVal()] >> b) & 1) == 0) {
                    if (cur.lo == null) {
                        cur.lo = new HuffNode(null, null);
                    }

                    cur = cur.lo;
                } else {
                    if (cur.hi == null) {
                        cur.hi = new HuffNode(null, null);
                    }

                    cur = cur.hi;
                }
            }

            if ((codeTable[sym.intVal()] & 1) == 0) {
                cur.lo = new HuffNode(sym.intVal(), 0);
            } else {
                cur.hi = new HuffNode(sym.intVal(), 0);
            }
        }

        logger.log("Huffman tree completed");
        return root;
    }

    private void decode(File fin, File fout, HuffNode root) throws IOException {
        BitInputStream in
                = new BitInputStream(new BufferedInputStream(new FileInputStream(fin), BUF));
        OutputStream out = new FileOutputStream(fout);
        try {
            byte[] buf = new byte[BUF];
            int i = 0;
            in.skip(ByteSym.RANGE);
            HuffNode cur = root;
            while (true) {
                int b = in.readBits(1);
                if (b == 0) {
                    cur = cur.lo;
                } else if (b == 1) {
                    cur = cur.hi;
                } else {
                    break;
                }

                if (cur.isLeaf()) {
                    if (cur.getSym() == ByteSym.EoF) {
                        break;
                    } else {
                        buf[i++] = (byte) cur.getSym();
                        cur = root;
                    }
                }

                if (i >= BUF) {
                    out.write(buf);
                    i = 0;
                }
            }

            out.write(buf, 0, i);
            out.flush();
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
        Option quiet = OptionBuilder.isRequired(false).hasArg(false).withLongOpt("quiet")
                .withDescription("indicate only progress").create("q");

        Options options = new Options();
        options.addOption(fin);
        options.addOption(fout);
        options.addOption(quiet);
        CommandLineParser parser = new BasicParser();
        HuffExpander extr = new HuffExpander();

        try {
            CommandLine cmd = parser.parse(options, args);
            logger.setQuiet(cmd.hasOption("q"));
            String in = cmd.getOptionValue("i");
            String out = cmd.getOptionValue("o");
            out = out == null ? in + ".out" : out;
            extr.expand(in, out);
        } catch (Exception ex) {
            HelpFormatter help = new HelpFormatter();
            help.printHelp("java HuffExtractor", "options:", options,
                    "\nExtract the Huffman coded input file to the output file.\n");
        }
    }
}
