

import com.csvreader.CsvReader;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 使用索引
 */
public class TestLucene1 {

    public static void main(String[] args) throws Exception {

        //createIndex();

        String filepath = "/compound_candidates.tsv";
        String indexpath = "/index";
        ArrayList<String[]> csvlist = readByCsvReader(filepath);
        JSONArray jsonArray=new JSONArray();
        BufferedWriter out = new BufferedWriter(new FileWriter("/disease_compound_paperlist_80.txt"));
        for (String[] strings : csvlist) {
            String disease_id = strings[0].strip();
            String disease_term = strings[1].strip();
            String term1_id = strings[2].strip();
            String term1 = strings[3].strip();
            String term1_score= strings[4].strip();
            String term2_id = strings[5].strip();
            String term2 = strings[6].strip();
            String term2_score= strings[7].strip();
            String groundtruth1 = strings[8].strip();
            String groundtruth2 = strings[9].strip();
            String abstract1 = "";
            String abstract2 = "";

            String num1 = "";
            String num2 = "";
            String paperlist1 = "";
            String paperlist2 = "";

            paperlist1 = countIndex(disease_term, term1, indexpath);
            paperlist2 = countIndex(disease_term, term2, indexpath);
            String newline1 = disease_id+"\t"+disease_term+"\t"+term1_id+"\t"+term1+"\t"+groundtruth1+"\t"+paperlist1+"\t"+term2_id+"\t"+term2+"\t"+groundtruth2+"\t"+paperlist2;
            // String newline2 = disease_id+"\t"+disease_term+"\t"+term2_id+"\t"+term2+"\t"+groundtruth2+"\t"+paperlist2;
            out.write(newline1+"\n");
            //out.write(newline2+"\n");

//            abstract1 = searchIndex(disease_term, term1, indexpath);
//            abstract2 = searchIndex(disease_term, term2, indexpath);
//            Map<String, Object> data = new LinkedHashMap<>();
//            data.put("disease_id", disease_id);
//            data.put("disease_term", disease_term);
//            data.put("term1_id", term1_id);
//            data.put("term1", term1);
//            data.put("term1_score", term1_score);
//            data.put("abstract1", abstract1);
//            data.put("groundtruth1", groundtruth1);
//            data.put("term2_id", term2_id);
//            data.put("term2", term2);
//            data.put("term2_score", term2_score);
//            data.put("abstract2", abstract2);
//            data.put("groundtruth2", groundtruth2);
//            String jsonString = new JSONObject(data).toString();
//            out.write(jsonString+"\n");//将格式化的jsonarray字符串写入文件








        }
        out.close();
    }

    //read csv
    public static ArrayList<String[]> readByCsvReader(String filePath) throws Exception {

        CsvReader csvReader = new CsvReader(filePath, '\t', Charset.defaultCharset());
        ArrayList<String[]> arrList = new ArrayList<>();
        while(csvReader.readRecord()){
            arrList.add(csvReader.getValues());
        }
        csvReader.close();
        return arrList;

    }

    // create index
    public static void createIndex() throws Exception{
        Directory directory= FSDirectory.open(new File("/index").toPath());
        //2.基于Directory对象创建一个IndexWriter对象
        IndexWriterConfig indexWriterConfig=new IndexWriterConfig(new StandardAnalyzer());        //指定使用哪种分析器
        IndexWriter indexWriter=new IndexWriter(directory,indexWriterConfig);
        //3.读取硬盘上的文件，对应每个文件创建一个文档对象
        File fileDir=new File("/alldata");
        File[] files=fileDir.listFiles();
        for (File file:files
        ) {
            //读取文件名
            String fileName=file.getName();
            //读取路径
            String filePath=file.getPath();
            //读取文件内容
            String fileContent= FileUtils.readFileToString(file,"UTF-8");
            //创建Filed
            //参数：域的名称、域的内容、是否存储
            Field fieldName=new TextField("name",fileName,Field.Store.YES);
            Field fieldPath=new StoredField("path",filePath);       //默认存储
            Field fieldContent=new TextField("content",fileContent,Field.Store.YES);
            //创建文档对象
            Document document=new Document();
            //向文档对象中添加域
            document.add(fieldName);
            document.add(fieldPath);
            document.add(fieldContent);
            //5.把文档对象写入索引库
            indexWriter.addDocument(document);
        }
        //6.关闭IndexWriter
        indexWriter.close();
    }

    /**
     * 查询索引
     *
     * @return
     * @throws Exception Occur.MUST：必须满足此条件，相当于and
     *                   Occur.SHOULD：应该满足，但是不满足也可以，相当于or
     */

    public static String searchIndex(String disease, String compound, String indexpath) throws Exception{
        //1.创建一个Directory对象，指定索引库的位置
        Directory directory=FSDirectory.open(new File(indexpath).toPath());
        //2.创建一个IndexReader对象
        IndexReader indexReader= DirectoryReader.open(directory);
        //3.创建一个IndexSearcher对象
        IndexSearcher indexSearcher=new IndexSearcher(indexReader);
        compound = compound.replace('/', ' ');
        compound = compound.replace('[', ' ');
        compound = compound.replace(']', ' ');
        compound = compound.replace('(', ' ');
        compound = compound.replace(')', ' ');
        compound = compound.replace('-', ' ');
        String [] stringQuery={disease, compound}; //
        String[] fields={"content", "content"}; //
        //Occur.MUST表示对应字段必须有查询值， Occur.MUST_NOT 表示对应字段必须没有查询值, Occur.SHOULD(结果“或”)
        Occur[] occ={Occur.MUST, Occur.MUST};//
        Query query= MultiFieldQueryParser.parse(stringQuery,fields,occ,new StandardAnalyzer());
        TopDocs topDocs=indexSearcher.search(query, 1);
        //System.out.println("查询结果总记录数："+topDocs.totalHits);
        String a = String.valueOf(topDocs.totalHits);
        if(a.equals("0 hits")){
            System.out.println(disease +" "+ compound);
        }

        ScoreDoc[] scoreDocs=topDocs.scoreDocs;
        String paper_abstract = "";
        for (ScoreDoc doc:scoreDocs
        ) {
            int docId=doc.doc;
            Document document=indexSearcher.doc(docId);
            // System.out.println("文件名："+document.get("name"));
            // System.out.println("文件路径："+document.get("path"));
            File file=new File(document.get("path"));
            String content= FileUtils.readFileToString(file,"UTF-8");
            JSONObject jsonObject=new JSONObject(content);
            paper_abstract = jsonObject.getString("abstract");
            // System.out.println(paper_abstract);
        }
        indexReader.close();
        return paper_abstract;
    }

    public static String countIndex(String disease, String compound, String indexpath) throws Exception{
        //1.创建一个Directory对象，指定索引库的位置
        Directory directory=FSDirectory.open(new File(indexpath).toPath());
        //2.创建一个IndexReader对象
        IndexReader indexReader= DirectoryReader.open(directory);
        //3.创建一个IndexSearcher对象
        IndexSearcher indexSearcher=new IndexSearcher(indexReader);
//        compound = compound.replace('/', ' ');
//        compound = compound.replace('[', ' ');
//        compound = compound.replace(']', ' ');
//        compound = compound.replace('(', ' ');
//        compound = compound.replace(')', ' ');
//        compound = compound.replace('-', ' ');
        String [] stringQuery={QueryParser.escape(disease), QueryParser.escape(compound)}; //
        String[] fields={"content", "content"}; //
        //Occur.MUST表示对应字段必须有查询值， Occur.MUST_NOT 表示对应字段必须没有查询值, Occur.SHOULD(结果“或”)
        Occur[] occ={Occur.MUST, Occur.MUST};//
        Query query= MultiFieldQueryParser.parse(stringQuery,fields,occ,new StandardAnalyzer());
        TopDocs topDocs=indexSearcher.search(query,80);
        String a = String.valueOf(topDocs.totalHits);
        String paper_list = "";
        if(!a.equals("0 hits")){
            ScoreDoc[] scoreDocs=topDocs.scoreDocs;
            for (ScoreDoc doc:scoreDocs
            ) {
                int docId=doc.doc;
                Document document=indexSearcher.doc(docId);
                // System.out.println("文件名："+document.get("name"));
                // System.out.println("文件路径："+document.get("path"));
                File file=new File(document.get("path"));
                String content= FileUtils.readFileToString(file,"UTF-8");
                JSONObject jsonObject=new JSONObject(content);
                paper_list = paper_list + " " + jsonObject.getString("pmid");
            }
            indexReader.close();
        }
        paper_list = paper_list.replaceAll(" +"," ");
        return paper_list;
    }
}