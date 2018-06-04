package edu.nyu.realtimebd.datautil.csv;

import edu.nyu.realtimebd.datautil.pojo.LocationAndTime;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Ramandeep Singh on 18-04-2016.
 */
public class CSVUtil {

  private static final Logger logger = LoggerFactory.getLogger(CSVUtil.class);
  private CSVPrinter csvPrinter = null;
  private static final CSVFormat csvFormat;
  private static final DateTimeFormatter df;
  // the output file not always required
  private File outputFile = null;
  private FileWriter fileWriter = null;

  public CSVUtil(File outputFile) {
    this.outputFile = outputFile;

  }


  static {
    csvFormat = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
    df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  }

  /**
   * Invoking this method is required to prevent memory leaks
   */
  public void cleanUp() throws IOException {
    if (fileWriter != null) {
      fileWriter.close();
    }
    if (csvPrinter != null) {
      csvPrinter.close();
    }
  }


  /**
   * Generic method to write a POJO to CSV file.
   *
   * @param recordInstance An object instance of some class
   * @param <E> A class of any type.
   */
  public <E> void writeRecordToFile(E recordInstance) {
    try {
      if (csvPrinter == null) {
        fileWriter = new FileWriter(outputFile);
        csvPrinter = new CSVPrinter(fileWriter, csvFormat);
        printHeaderRecord(csvPrinter, recordInstance);
      }
      printValueRecord(csvPrinter, recordInstance);
    } catch (IOException e) {
      logger.error("Unable to create csv printer or write record {}", recordInstance, e);
      return;
    }

  }

  /**
   * This method writes the value from an object instance to CSV Uses clever reflection to dump any
   * fields present in the Object Totally foolproof and requires no mapping
   *
   * @param csvPrinter The CSVPrinter instance
   * @param recordInstance The object to write
   * @param <E> the class of the object
   */
  private <E> void printValueRecord(CSVPrinter csvPrinter, E recordInstance) throws IOException {
    Field[] fields = recordInstance.getClass().getDeclaredFields();
    Object[] fieldValues = new String[fields.length];
    for (int i = 0; i < fields.length; i++) {
      try {
        fields[i].setAccessible(true);
        if (fields[i].get(recordInstance) != null) {
          fieldValues[i] = fields[i].get(recordInstance).toString();
        }
        fields[i].setAccessible(false);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }


    }
    csvPrinter.printRecord(fieldValues);
  }

  /**
   * It prints the header record into the CSV file.
   *
   * @param csvPrinter the csvPrinter instance
   * @param recordInstance the object which in this case is just used to get its field names
   * @param <E> the class of object
   */
  private <E> void printHeaderRecord(CSVPrinter csvPrinter, E recordInstance) throws IOException {
    Field[] fields = recordInstance.getClass().getDeclaredFields();
    String[] fieldNames = new String[fields.length];
    for (int i = 0; i < fields.length; i++) {
      fieldNames[i] = fields[i].getName();
    }
    csvPrinter.printRecord(fieldNames);
  }

  public static Flowable<LocationAndTime> readRecordsFromFileAsync(Path inputFile) {
    return Flowable.using(() -> Files.newBufferedReader(inputFile),
        bufferedReader -> csvRecordFlowable(bufferedReader, csvFormat.withHeader()),
        BufferedReader::close
    ).concatMapDelayError(record -> Single.just(getLocationAndTime(record)).toFlowable());
  }

  public static Flowable<CSVRecord> csvRecordFlowable(BufferedReader bufferedReader,
      CSVFormat csvFormat) {
    return Flowable.fromIterable(() -> {
      try {
        return CSVParser.parse(bufferedReader, csvFormat).iterator();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private static LocationAndTime getLocationAndTime(CSVRecord record) {
    LocationAndTime locationVar = new LocationAndTime();
    locationVar.setStartLatitude(Float.valueOf(record.get("start_latitude")));
    locationVar.setStartLongitude(Float.valueOf(record.get("start_longitude")));
    locationVar.setEndLatitude(Float.valueOf(record.get("end_latitude")));
    locationVar.setEndLongitude(Float.valueOf(record.get("end_longitude")));
    locationVar
        .setJourneyStartTime(LocalDateTime.parse(record.get("journey_start_time"), df));
    return locationVar;
  }


}


