package info.fetter.logstashforwarder.config;

import static org.apache.log4j.Level.DEBUG;
import static org.apache.log4j.Level.ERROR;
import static org.apache.log4j.Level.TRACE;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ParametersManager {
	@SuppressWarnings("static-access")
	public static Parameters parseOptions(String[] args) {
		Options options = new Options();
		Option helpOption = new Option("help", "print this message");
		Option quietOption = new Option("quiet", "operate in quiet mode - only emit errors to log");
		Option debugOption = new Option("debug", "operate in debug mode");
		Option debugWatcherOption = new Option("debugwatcher", "operate watcher in debug mode");
		Option traceOption = new Option("trace", "operate in trace mode");
		Option tailOption = new Option("tail", "read new files from the end");

		Option spoolSizeOption = OptionBuilder.withArgName("number of events")
				.hasArg()
				.withDescription("event count spool threshold - forces network flush")
				.create("spoolsize");
		Option idleTimeoutOption = OptionBuilder.withArgName("")
				.hasArg()
				.withDescription("time between file reads in seconds")
				.create("idletimeout");
		Option configOption = OptionBuilder.withArgName("config file")
				.hasArg()
				.isRequired()
				.withDescription("path to logstash-forwarder configuration file")
				.create("config");
		Option signatureLengthOption = OptionBuilder.withArgName("signature length")
				.hasArg()
				.withDescription("Maximum length of file signature")
				.create("signaturelength");
		Option logfileOption = OptionBuilder.withArgName("logfile name")
				.hasArg()
				.withDescription("Logfile name")
				.create("logfile");
		Option logfileSizeOption = OptionBuilder.withArgName("logfile size")
				.hasArg()
				.withDescription("Logfile size (default 10M)")
				.create("logfilesize");
		Option logfileNumberOption = OptionBuilder.withArgName("number of logfiles")
				.hasArg()
				.withDescription("Number of logfiles (default 5)")
				.create("logfilenumber");
		Option sincedbOption = OptionBuilder.withArgName("sincedb file")
				.hasArg()
				.withDescription("Sincedb file name")
				.create("sincedb");

		options.addOption(helpOption)
		.addOption(idleTimeoutOption)
		.addOption(spoolSizeOption)
		.addOption(quietOption)
		.addOption(debugOption)
		.addOption(debugWatcherOption)
		.addOption(traceOption)
		.addOption(tailOption)
		.addOption(signatureLengthOption)
		.addOption(configOption)
		.addOption(logfileOption)
		.addOption(logfileNumberOption)
		.addOption(logfileSizeOption)
		.addOption(sincedbOption);
		
		CommandLineParser parser = new GnuParser();
		Parameters parameters = new Parameters();
		try {
			CommandLine line = parser.parse(options, args);
			if(line.hasOption("spoolsize")) {
				parameters.setSpoolSize(Integer.parseInt(line.getOptionValue("spoolsize")));
			}
			if(line.hasOption("idletimeout")) {
				parameters.setIdleTimeout(Integer.parseInt(line.getOptionValue("idletimeout")));
			}
			if(line.hasOption("config")) {
				parameters.setConfigFile(line.getOptionValue("config"));
			}
			if(line.hasOption("signaturelength")) {
				parameters.setSignatureLength(Integer.parseInt(line.getOptionValue("signaturelength")));
			}
			if(line.hasOption("quiet")) {
				parameters.setLogLevel(ERROR);
			}
			if(line.hasOption("debug")) {
				parameters.setLogLevel(DEBUG);
			}
			if(line.hasOption("trace")) {
				parameters.setLogLevel(TRACE);
			}
			if(line.hasOption("debugwatcher")) {
				parameters.setDebugWatcherSelected(true);
			}
			if(line.hasOption("tail")) {
				parameters.setTailSelected(true);
			}
			if(line.hasOption("logfile")) {
				parameters.setLogfile(line.getOptionValue("logfile"));
			}
			if(line.hasOption("logfilesize")) {
				parameters.setLogfileSize(line.getOptionValue("logfilesize"));
			}
			if(line.hasOption("logfilenumber")) {
				parameters.setLogfileNumber(Integer.parseInt(line.getOptionValue("logfilenumber")));
			}
			if(line.hasOption("sincedb")) {
				parameters.setSincedbFile(line.getOptionValue("sincedb"));
			}
		} catch(ParseException e) {
			printHelp(options);
			System.exit(1);;
		} catch(NumberFormatException e) {
			System.err.println("Value must be an integer");
			printHelp(options);
			System.exit(2);;
		}
		return parameters;
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("logstash-forwarder", options);
	}
}
