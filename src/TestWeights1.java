import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shulin Huang
 * @date 4/8/18
 **/

 /**
  * Reference:
  * https://github.com/wihoho/KNN
  * https://github.com/badlogic/knn
  * https://github.com/Stephaniefan/knn
  **/

public class TestWeights1 {
    public static void main(String[] args){
        LoadData train_Data = new LoadData();
        train_Data.loadData("trainProdSelection.arff");

        KNN knn = new KNN();
        // get the min&max for attributes
        Map<String, List<Double>> minmaxMap = knn.getMinMax(train_Data);

        //System.out.println(minmaxMap);

        // matrix map for non numeric attributes
        Map<String, double[][]> matrixMap = new HashMap<>();
        double[][] type = {{1.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 1.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 1.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 1.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 1.0}};
        matrixMap.put("Type", type);
        double[][] lifestyle = {{1.0, 0.0, 0.0, 0.0}, {0.0, 1.0, 0.0, 0.0}, {0.0, 0.0, 1.0, 0.0}, {0.0, 0.0, 0.0, 1.0}};
        matrixMap.put("LifeStyle", lifestyle);

        Map<String, Double> weightedMap = new HashMap<>();
        weightedMap.put("Type", 1.0);
        weightedMap.put("LifeStyle", 1.0);
        weightedMap.put("Vacation", 1.0);
        weightedMap.put("eCredit", 1.0);
        weightedMap.put("salary", 1.0);
        weightedMap.put("property", 1.0);
        List<String> keyList = new ArrayList<>();
        keyList.addAll(weightedMap.keySet());
        TestWeights1 testWeights1 = new TestWeights1();
        double max_accuracy = testWeights1.checkAccuracy(train_Data, knn, 5, weightedMap, matrixMap);;
        for(int i=1; i<6; i++) {
            String key = keyList.get(i);
            double weights = weightedMap.get(keyList.get(i-1));
//                System.out.println(weights);

            weightedMap.remove(key);
            weightedMap.put(key, weights * 2.0);
            double multiple_weights = weightedMap.get(key);
//                System.out.println(multiple_weights);

            train_Data = new LoadData();
            train_Data.loadData("trainProdSelection.arff");
            knn.nomarlize(minmaxMap, train_Data);
            double curr_accuracy1 = testWeights1.checkAccuracy(train_Data, knn, 5, weightedMap, matrixMap);

            weightedMap.remove(key);
            weightedMap.put(key, weights / 2.0);
            double divide_weights = weightedMap.get(key);
//                System.out.println(divide_weights);

            train_Data = new LoadData();
            train_Data.loadData("trainProdSelection.arff");
            knn.nomarlize(minmaxMap, train_Data);
            double curr_accuracy2 = testWeights1.checkAccuracy(train_Data, knn, 5, weightedMap, matrixMap);

            System.out.println(key + ": curr1 " + curr_accuracy1);
            System.out.println(key + ": curr2 " + curr_accuracy2);
            if(curr_accuracy1 < max_accuracy && curr_accuracy2 < max_accuracy) {
                weightedMap.remove(key);
                weightedMap.put(key, divide_weights*2.0);
                continue;
            }
            if (curr_accuracy1 > curr_accuracy2) {
                while (curr_accuracy1 > max_accuracy) {
                    max_accuracy = curr_accuracy1;
//                        weights = weightedMap.get(key);
                    weightedMap.remove(key);
                    weightedMap.put(key, multiple_weights * 2.0);
                    multiple_weights = weightedMap.get(key);
//                        System.out.println(weightedMap.get(key));
                    train_Data = new LoadData();
                    train_Data.loadData("trainProdSelection.arff");
                    knn.nomarlize(minmaxMap, train_Data);
                    curr_accuracy1 = testWeights1.checkAccuracy(train_Data, knn, 5, weightedMap, matrixMap);
                    System.out.println("curr:" + curr_accuracy1);
                    System.out.println("max:" + max_accuracy);
                }
                weights = weightedMap.get(key);
                weightedMap.remove(key);
                weightedMap.put(key, weights / 2.0);
            }
            else{
                while (curr_accuracy2 > max_accuracy) {
                    max_accuracy = curr_accuracy2;
//                        weights = weightedMap.get(key);
                    weightedMap.remove(key);
                    weightedMap.put(key, divide_weights / 2.0 );
                    divide_weights = weightedMap.get(key);
//                        System.out.println(weightedMap.get(key));
                    train_Data = new LoadData();
                    train_Data.loadData("trainProdSelection.arff");
                    knn.nomarlize(minmaxMap, train_Data);
                    curr_accuracy2 = testWeights1.checkAccuracy(train_Data, knn, 5, weightedMap, matrixMap);
                    System.out.println("curr:" + curr_accuracy2);
                    System.out.println("max:" + max_accuracy);
                }
                weights = weightedMap.get(key);
                weightedMap.remove(key);
                weightedMap.put(key, weights * 2.0);
            }
        }

        System.out.println(weightedMap);
    }

    // shuffle list for validation
    public static List<DataSet> shuffle(LoadData data) {
        List<DataSet> shuffle = new ArrayList<>(data.getDataSetList());
        Collections.shuffle(shuffle);
        return shuffle;
    }

    public double checkAccuracy(LoadData loadData, KNN knn, int k,
                                Map<String, Double> weight,
                                Map<String, double[][]> matrixMap) {
        loadData.setData(shuffle(loadData));
        List<DataSet> shuffleList = loadData.getDataSetList();

        double accuracy = 0;
        double correct_count;

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
                    correct_count++;
                }
            }
            //update load data back to the shuffled list
            if(i!=k-1) loadData.setData(shuffleList);
            accuracy += correct_count / testData.size();
        }
        return accuracy / k;
    }

}
