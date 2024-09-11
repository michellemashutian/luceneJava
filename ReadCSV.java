import com.csvreader.CsvReader;

import java.nio.charset.Charset;
import java.util.ArrayList;


public class ReadCSV {

    public static void main(String[] args) throws Exception {
        String filepath = "/Users/gaozheng/shutian/compound_terms.tsv";
        ArrayList<String[]> csvlist = readByCsvReader(filepath);
        for (String[] strings : csvlist) {
            System.out.println(strings[0] + " " + strings[1]);
        }

    }
    public static ArrayList<String[]> readByCsvReader(String filePath) throws Exception {

        CsvReader csvReader = new CsvReader(filePath, '\t', Charset.defaultCharset());
        ArrayList<String[]> arrList = new ArrayList<>();
        while(csvReader.readRecord()){
            arrList.add(csvReader.getValues());
        }
        csvReader.close();
        return arrList;

    }

}
