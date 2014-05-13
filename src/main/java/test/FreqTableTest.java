package test;

import compr.par.huff.ParHuffCompressor;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * @author Georgi Krastev <joro.kr.21@gmail.com>
 */
public class FreqTableTest {

    public static void main(String[] args) {
        Option fin = OptionBuilder.isRequired().hasArg().withArgName("filename")
                .withLongOpt("file").withDescription("input file (*required)").create("f");
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
            TimeLogger.getLogger().setQuiet(cmd.hasOption("q"));
            String t = cmd.getOptionValue("t");
            String in = cmd.getOptionValue("f");
            String out = cmd.getOptionValue("o");
            out = out == null ? in + ".txt" : out;
            compr.setTasks(t == null ? 1 : Integer.parseInt(t));
            compr.printFreqTable(in, out);
        } catch (Exception ex) {
            HelpFormatter help = new HelpFormatter();
            help.printHelp("java FreqTableTest", "options:", options,
                    "\nBuild the byte frequency table of the input file\n"
                    + "with -t tasks and save it in the output file.\n");
        }
    }
}
