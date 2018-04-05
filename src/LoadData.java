import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shulin Huang
 * @date 4/4/18
 **/

// Helper class to load data from file
public class LoadData {
    // mapKey: attribute name, mapValue: attributeSet
    private Map<String, AttributeSet> attributeMap;
    private List<DataSet> dataSetList;
    private List<String> nameList;

    public Map<String, AttributeSet> getAttributeMap() {
        return attributeMap;
    }

    public List<DataSet> getDataSetList() {
        return dataSetList;
    }

    public List<String> getNameList() {
        return nameList;
    }

    public void setData(List<DataSet> list){
        this.dataSetList = list;
    }

    public void loadData(String filename){
        File file = new File(filename);
        nameList = new ArrayList<>();
        attributeMap = new HashMap<>();
        dataSetList = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String buffer;
            while((buffer = bufferedReader.readLine()) != null && !buffer.startsWith("@data")) {
//                System.out.println("Start loading file");
//                System.out.println(buffer);
                if (!buffer.isEmpty() && buffer.startsWith("@attribute")) {
//                    System.out.println("attribute load" + buffer);
                    String[] attributes = buffer.split(" ");
                    // add attribute name to list
                    nameList.add(attributes[1]);
                    // add attributes to map
                    attributeMap.put(attributes[1], new AttributeSet(attributes[1], attributes[2]));
                }
            }
            while ((buffer = bufferedReader.readLine()) != null) {
//                    System.out.println("Start loading data");
                    // add data to list
                    dataSetList.add(new DataSet(buffer,attributeMap,nameList));
                }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for(DataSet dataSet: dataSetList){
            sb.append(dataSet + "\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        LoadData loadData = new LoadData();
        loadData.loadData("trainProdSelection.arff");
        System.out.println(loadData.nameList);
    }
}
