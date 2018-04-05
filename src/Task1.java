import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shulin Huang
 * @date 4/5/18
 **/
public class Task1 {

    public static void main(String[] args) {
        LoadData testData = new LoadData();
        testData.loadData("testProdSelection.arff");

        LoadData trainData = new LoadData();
        trainData.loadData("trainProdSelection.arff");

        KNN knn = new KNN();
        // get the min&max for attributes
        Map<String, List<Double>> minmaxMap = knn.getMinMax(trainData);

        //System.out.println(minmaxMap);

        // matrix map for non numeric attributes
        Map<String, double[][]> marixMap = new HashMap<>();
        double[][] type = {{1.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 1.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 1.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 1.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 1.0}};
        marixMap.put("Type", type);
        double[][] lifestyle = {{1.0, 0.0, 0.0, 0.0}, {0.0, 1.0, 0.0, 0.0}, {0.0, 0.0, 1.0, 0.0}, {0.0, 0.0, 0.0, 1.0}};
        marixMap.put("LifeStyle", lifestyle);

        // map to store test weight, change that to check accuracy
        Map<String, Double> weightedMap = new HashMap<>();
        weightedMap.put("Type", 0.2);
        weightedMap.put("LifeStyle", 0.15);
        weightedMap.put("Vacation", 0.05);
        weightedMap.put("eCredit", 0.25);
        weightedMap.put("salary", 0.15);
        weightedMap.put("property", 0.2);

        //normalize
        knn.nomarlize(minmaxMap, testData);
//        System.out.println(testData); //=> checked
        knn.nomarlize(minmaxMap, trainData);
//        System.out.println(trainData); //=> checked

        for (DataSet dataSet : testData.getDataSetList()) {
//            System.out.println(dataSet);
            String label = null;
            for (String attribute : testData.getNameList()) {
                label = knn.KNN(trainData, dataSet, weightedMap, marixMap, 3);
            }
//            System.out.println(dataSet);
            System.out.println(label);
        }

        System.out.println("accuracy" + checkAccuracy(trainData, knn, 5, weightedMap, marixMap));

    }

    // shuffle list for validation
    public static List<DataSet> shuffle(LoadData data) {
        List<DataSet> shuffle = new ArrayList<>(data.getDataSetList());
        Collections.shuffle(shuffle);
        return shuffle;
    }

    public static double checkAccuracy(LoadData loadData, KNN knn, int k,
                                       Map<String, Double> weight,
                                       Map<String, double[][]> matrixMap) {
        loadData.setData(shuffle(loadData));
        List<DataSet> shuffleList = loadData.getDataSetList();

        int total_correct = 0;
        double accuracy = 0;
        double total_count = 0, correct_count;

        String targetType = loadData.getNameList().get(loadData.getNameList().size() - 1); // for now => label
        Map<String, Double> map = loadData.getAttributeMap().get(targetType).getTypeValue();

        // folding validation
        for (int i = 0; i < k; i++) {
            correct_count = 0;
            List<DataSet> testData = new ArrayList<>();
            List<DataSet> trainData = new ArrayList<>(loadData.getDataSetList());
            int fold_num = shuffleList.size()/k;
            // get k-th fold
            for (int j = fold_num*(i+1)-1; j >= fold_num * i; j--) {
                testData.add(trainData.remove(j));
            }
            // update load data to folded train data
            loadData.setData(trainData);
            for (DataSet dataSet : testData) {
                if (dataSet.getValue(targetType) == map.get(knn.KNN(loadData, dataSet, weight, matrixMap, 3))) {
                    total_correct++;
                    correct_count++;
                }
                total_count++;
            }
            //update load data back to the shuffled list
            if(i!=k-1) loadData.setData(shuffleList);
            accuracy += correct_count / testData.size();
            System.out.println("[" + i + "]  accuracy is   "
                    + correct_count / testData.size());
        }
        System.out.println("total_count" + total_count);
        System.out.println("total_correct_count" + total_correct);
        return accuracy / k;
    }
}

