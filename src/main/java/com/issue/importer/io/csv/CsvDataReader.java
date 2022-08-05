package com.issue.importer.io.csv;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.CsvRow;
import com.issue.importer.domain.CsvType;
import com.issue.importer.domain.IssueData;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.commons.codec.binary.Hex;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvDataReader implements DataReader {

    @Override
    public List<IssueData> readCsvData(ApplicationSettings settings, MultipartFile file) {
        CsvType csvType = CsvType.valueOf(settings.csvType());
        char csvDelimiter = settings.delimiter().charAt(0);
        List<? extends CsvRow> rows = fetchCsvRows(csvType.getClazz(), file, csvDelimiter);

        return rows.stream()
                .map(r -> new IssueData(r.provideTitle(), r.provideDescription()))
                .toList();
    }

    private <T extends CsvRow> List<T> fetchCsvRows(Class<T> clazz, MultipartFile file, char delimiter) {
        byte[] inputBytes = removeBom(file);
        try (InputStream is = new ByteArrayInputStream(inputBytes);
             Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                    .withType(clazz)
                    .withSeparator(delimiter)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return csvToBean.parse();
        } catch (Exception e) {
            throw new CsvReadingException("Csv file reading failed.", e);
        }
    }

    private byte[] removeBom(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            // get the first 3 bytes
            byte[] bom = new byte[3];
            bb.get(bom, 0, bom.length);

            // BOM encoded as ef bb bf
            String content = String.valueOf(Hex.encodeHex(bom));
            if ("efbbbf".equalsIgnoreCase(content)) {
                // remaining
                byte[] reducedBytes = new byte[bytes.length - 3];
                bb.get(reducedBytes, 0, reducedBytes.length);

                return reducedBytes;
            }
            return bytes;
        } catch (Exception e) {
            throw new CsvReadingException("Bom removal from Csv file failed.", e);
        }

    }
}
