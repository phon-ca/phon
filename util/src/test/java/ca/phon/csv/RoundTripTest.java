package ca.phon.csv;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class RoundTripTest {
    @Parameters(name = "{index}: {4}")
    public static Collection<Object[]> data() {

        String[] fileNameArray = new String[]{
            "Sep-Comma Quote-Double LineEndings-Windows QuoteAllFields-False",
            "Sep-Comma Quote-Double LineEndings-Windows QuoteAllFields-True",
            "Sep-Comma Quote-Double LineEndings-Unix QuoteAllFields-False",
            "Sep-Comma Quote-Double LineEndings-Unix QuoteAllFields-True",
            "Sep-Comma Quote-Single LineEndings-Windows QuoteAllFields-False",
            "Sep-Comma Quote-Single LineEndings-Windows QuoteAllFields-True",
            "Sep-Comma Quote-Single LineEndings-Unix QuoteAllFields-False",
            "Sep-Comma Quote-Single LineEndings-Unix QuoteAllFields-True",
            "Sep-Space Quote-Double LineEndings-Windows QuoteAllFields-False",
            "Sep-Space Quote-Double LineEndings-Windows QuoteAllFields-True",
            "Sep-Space Quote-Double LineEndings-Unix QuoteAllFields-False",
            "Sep-Space Quote-Double LineEndings-Unix QuoteAllFields-True",
            "Sep-Space Quote-Single LineEndings-Windows QuoteAllFields-False",
            "Sep-Space Quote-Single LineEndings-Windows QuoteAllFields-True",
            "Sep-Space Quote-Single LineEndings-Unix QuoteAllFields-False",
            "Sep-Space Quote-Single LineEndings-Unix QuoteAllFields-True",
            "Sep-Semicolon Quote-Double LineEndings-Windows QuoteAllFields-False",
            "Sep-Semicolon Quote-Double LineEndings-Windows QuoteAllFields-True",
            "Sep-Semicolon Quote-Double LineEndings-Unix QuoteAllFields-False",
            "Sep-Semicolon Quote-Double LineEndings-Unix QuoteAllFields-True",
            "Sep-Semicolon Quote-Single LineEndings-Windows QuoteAllFields-False",
            "Sep-Semicolon Quote-Single LineEndings-Windows QuoteAllFields-True",
            "Sep-Semicolon Quote-Single LineEndings-Unix QuoteAllFields-False",
            "Sep-Semicolon Quote-Single LineEndings-Unix QuoteAllFields-True",
            "Sep-Tab Quote-Double LineEndings-Windows QuoteAllFields-False",
            "Sep-Tab Quote-Double LineEndings-Windows QuoteAllFields-True",
            "Sep-Tab Quote-Double LineEndings-Unix QuoteAllFields-False",
            "Sep-Tab Quote-Double LineEndings-Unix QuoteAllFields-True",
            "Sep-Tab Quote-Single LineEndings-Windows QuoteAllFields-False",
            "Sep-Tab Quote-Single LineEndings-Windows QuoteAllFields-True",
            "Sep-Tab Quote-Single LineEndings-Unix QuoteAllFields-False",
            "Sep-Tab Quote-Single LineEndings-Unix QuoteAllFields-True"
        };

        char[] separators = new char[]{',', ' ', ';', '\t'};
        CSVQuoteType[] quoteChars = new CSVQuoteType[]{CSVQuoteType.DOUBLE_QUOTE, CSVQuoteType.SINGLE_QUOTE};
        boolean[] unixLineEndings = new boolean[]{false, true};
        boolean[] quoteAllFields = new boolean[]{false, true};

        ArrayList<Object[]> parameterArray = new ArrayList<>();

        int fileNameCounter = 0;

        for (char separator : separators) {
            for (CSVQuoteType quoteChar : quoteChars) {
                for (boolean unixLineEnding : unixLineEndings) {
                    for (boolean quoteAllField : quoteAllFields) {
                        parameterArray.add(new Object[]{
                            separator,
                            quoteChar,
                            unixLineEnding,
                            quoteAllField,
                            fileNameArray[fileNameCounter]
                        });
                        fileNameCounter++;
                    }
                }
            }
        }

        return parameterArray.stream().toList();
    }

    private final char fSeparator;
    private final CSVQuoteType fQuoteChar;
    private final boolean fUnixLineEndings;
    private final boolean fQuoteAllFields;
    private final String fFileName;

    public RoundTripTest(
        char separator,
        CSVQuoteType quoteChar,
        boolean unixLineEndings,
        boolean quoteAllFields,
        String fileName
    ) {
        this.fSeparator = separator;
        this.fQuoteChar = quoteChar;
        this.fUnixLineEndings = unixLineEndings;
        this.fQuoteAllFields = quoteAllFields;
        this.fFileName = fileName;
    }

    @Test
    public void test() throws IOException, NoSuchAlgorithmException {
        assertTrue(roundTrip(fSeparator, fQuoteChar, fUnixLineEndings, fQuoteAllFields, fFileName));
    }

    private boolean roundTrip(
        char separator,
        CSVQuoteType quoteChar,
        boolean unixLineEndings,
        boolean quoteAllFields,
        String fileName
    ) throws IOException, NoSuchAlgorithmException {
        String inputFilePath = "src/test/resources/ca/phon/csv/RoundTripTestCSVs/Input " + fileName + ".csv";

        var inputStreamReader = new InputStreamReader(new FileInputStream(inputFilePath), "UTF-8");
        var csvReader = new CSVReader(inputStreamReader, new char[]{separator}, quoteChar, false);

        System.out.println("Read Data");

        ArrayList<String[]> data = new ArrayList<>();

        String[] row = csvReader.readNext();
        while (row != null) {
            data.add(row);
            System.out.println(Arrays.toString(row));
            row = csvReader.readNext();
        }

        System.out.println();

        System.out.println("Written Data");

        var bytArrayOutputStream = new ByteArrayOutputStream();
        var outputStreamWriter = new OutputStreamWriter(bytArrayOutputStream, "UTF-8");
        CSVWriter csvWriter = new CSVWriter(
            outputStreamWriter,
            separator,
            quoteChar,
            false,
            unixLineEndings,
            quoteAllFields
        );

        for (String[] record : data) {
            csvWriter.writeNext(record);
        }

        csvWriter.close();

        String inputCheckSum = getFileCheckSum(inputFilePath);
        String outputCheckSum = getFileCheckSum(bytArrayOutputStream.toByteArray());

        return inputCheckSum.equals(outputCheckSum);
    }

    private String getFileCheckSum(String filePath) throws IOException, NoSuchAlgorithmException {
        byte[] data = Files.readAllBytes(Paths.get(filePath));
        byte[] hash = MessageDigest.getInstance("MD5").digest(data);
        return new BigInteger(1, hash).toString(16);
    }

    private String getFileCheckSum(byte[] data) throws IOException, NoSuchAlgorithmException {
        byte[] hash = MessageDigest.getInstance("MD5").digest(data);
        return new BigInteger(1, hash).toString(16);
    }
}
