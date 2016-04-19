package edu.nyu.realtimebd.datautil.csv;

import edu.nyu.realtimebd.datautil.pojo.LocationAndTime;
import org.apache.commons.csv.CSVFormat;
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

    private static final Logger logger= LoggerFactory.getLogger(CSVUtil.class);
    private CSVPrinter csvPrinter=null;
    private static final CSVFormat csvFormat;
    private static final SimpleDateFormat sdf;
    // the output file not always required
    private File outputFile=null;
    private FileWriter fileWriter=null;
    public CSVUtil(File outputFile) {
       this.outputFile=outputFile;

    }



    static {
        csvFormat=CSVFormat.DEFAULT.withRecordSeparator("\r\n");
         sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    }

    /**
     * Invoking this method is required to prevent memory leaks
     * @throws IOException
     */
    public void cleanUp() throws IOException{
        if(fileWriter!=null) {
           fileWriter.close();
        }
        if(csvPrinter!=null){
            csvPrinter.close();
        }
    }


    /**
     * Generic method to write a POJO to CSV file.
     * @param recordInstance An object instance of some class
     * @param <E> A class of any type.
     */
    public   <E> void writeRecordToFile( E  recordInstance) {
        try {
            if (csvPrinter == null) {
                fileWriter=new FileWriter(outputFile);
                csvPrinter = new CSVPrinter(fileWriter, csvFormat);
                printHeaderRecord(csvPrinter,recordInstance);


            }
            printValueRecord(csvPrinter, recordInstance);
        }
        catch (IOException e) {
            logger.error("Unable to create csv printer or write record {}", recordInstance, e);
            return;
        }

    }

    /**
     * This method writes the value from an object instance to CSV
     * Uses clever reflection to dump any fields present in the Object
     * Totally foolproof and requires no mapping
     * @param csvPrinter  The CSVPrinter instance
     * @param recordInstance The object to write
     * @param <E> the class of the object
     * @throws IOException
     */
    private  <E> void  printValueRecord(CSVPrinter csvPrinter, E recordInstance) throws IOException{
        Field[] fields= recordInstance.getClass().getDeclaredFields();
        Object[] fieldValues=new String[fields.length];
        for(int i=0;i<fields.length;i++){
            try {
                fields[i].setAccessible(true);
                if(fields[i].get(recordInstance)!=null) {
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
     * @param csvPrinter the csvPrinter instance
     * @param recordInstance the object which in this case is just used to get its field names
     * @param <E> the class of object
     * @throws IOException
     */
    private  <E> void printHeaderRecord(CSVPrinter csvPrinter,E recordInstance ) throws IOException {
        Field[] fields= recordInstance.getClass().getDeclaredFields();
        String[] fieldNames=new String[fields.length];
        for(int i=0;i<fields.length;i++){
            fieldNames[i]=fields[i].getName();
        }
        csvPrinter.printRecord(fieldNames);
    }

    /**
     * It reads the records from the input csv file of taxi data
     * @param inputFile the input file
     * @return A list of records
     */
   public static List<LocationAndTime> readRecordsFromFile(File inputFile){
       List<LocationAndTime> results=new ArrayList<>();
       try(Reader in=new FileReader(inputFile)){
       Iterable<CSVRecord> records=csvFormat.withHeader().parse(in);
       for(CSVRecord record:records){
            LocationAndTime locationVar=new LocationAndTime();
            locationVar.setStartLatitude(Float.valueOf(record.get("start_latitude")));
            locationVar.setStartLongitude(Float.valueOf(record.get("start_longitude")));
            locationVar.setEndLatitude(Float.valueOf(record.get("end_latitude")));
            locationVar.setEndLongitude(Float.valueOf(record.get("end_longitude")));
            try {
                locationVar.setJourneyStartTime(sdf.parse(record.get("journey_start_time")));
            }catch (ParseException e){
                logger.error("Skipping record {}",record,e);
                continue;
            }
           results.add(locationVar);

       }

       }catch (IOException e){
          logger.error("Exception e",e);
       }

       return results;

   }



}
