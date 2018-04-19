import java.util.*;

/**
 * @author Fangjie Ding
 * @date 4/7/2018
 */

 /**
  * Reference:
  * https://github.com/wihoho/KNN
  * https://github.com/badlogic/knn
  * https://github.com/Stephaniefan/knn
  **/
  
public class TestWeights2 {

    public static void main(String[] args) {
        LoadData testData = new LoadData();
        LoadData trainData = new LoadData();
        System.out.println("Please input the train and test files: [1] binary class label [2] real number label");
        Scanner scanner = new Scanner(System.in);
        int choose = scanner.nextInt();
        // load Data
        loadData(trainData, testData, choose);
        KNN knn = new KNN();
        // get the min&max for attributes
        Map<String, List<Double>> minmaxMap = knn.getMinMax(trainData);
        // matrix map for non numeric attributes
        Map<String, double[][]> matrixMap = new HashMap<>();
        double[][] serviceType = {{1.0, 0.2, 0.2, 0.0, 0.1}, {0.2, 1.0, 0.1, 0.0, 0.3}, {0.2, 0.1, 1.0, 0.0, 0.2}, {0.0, 0.0, 0.0, 1.0, 0.0}, {0.1, 0.3, 0.2, 0.0, 1.0}};
        double[][] customer = {{1.0, 0.1, 0.0, 0.1, 0.2}, {0.1, 1.0, 0.0, 0.2, 0.2}, {0.0, 0.0, 1.0, 0.0, 0.0}, {0.1, 0.2, 0.0, 1.0, 0.1}, {0.2, 0.2, 0.0, 0.1, 1.0}};
        double[][] size = {{1.0, 0.0, 0.1}, {0.0, 1.0, 0.1}, {0.1, 0.1, 1.0}};
        double[][] promotion = {{1.0, 0.8, 0.1, 0.5}, {0.8, 1.0, 0.0, 0.0}, {0.1, 0.0, 1.0, 0.4}, {0.5, 0.0, 0.4, 1.0}};
        matrixMap.put("Service_type", serviceType);
        matrixMap.put("Customer", customer);
        matrixMap.put("Size", size);
        matrixMap.put("Promotion", promotion);
        // map to store test weight, change that to check accuracy
        List<Map<String, Double>> weightList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; i + j <= 10; j++) {
                for (int k = 1; i + j + k <= 10; k++) {
                    for (int l = 1; i + j + k + l <= 10; l++) {
                        for (int m = 1; i + j + l + k + m <= 10; m++) {
                            for (int n = 1; i + j + k + l + m + n <= 10; n++) {
                                for (int o = 1; i + j + k + l + m + n + o <= 10; o++) {
                                    for (int p = 1; i + j + k + l + m + n + o + p <= 10; p++) {
                                        if (i + j + k + l + m + n + o + p == 10) {
                                            Map<String, Double> weightedMap = new HashMap<>();
                                            weightedMap.put("Service_type", Double.parseDouble("0." + i));
                                            weightedMap.put("Customer", Double.parseDouble("0." + j));
                                            weightedMap.put("Monthly_fee", Double.parseDouble("0." + k));
                                            weightedMap.put("Advertisement_budget", Double.parseDouble("0." + l));
                                            weightedMap.put("Size", Double.parseDouble("0." + m));
                                            weightedMap.put("Promotion", Double.parseDouble("0." + n));
                                            weightedMap.put("Interest_rate", Double.parseDouble("0." + o));
                                            weightedMap.put("Period", Double.parseDouble("0." + p));
                                            weightList.add(weightedMap);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        double max_accuracy = -1;
        double min_mse = Double.MAX_VALUE;
        Map<String, Double> best_weight = new HashMap<>();
        switch (choose) {
            case 1:
                for (int i = 0; i < weightList.size(); i++) {
                    LoadData train_Data = new LoadData();
                    LoadData test_Data = new LoadData();
                    loadData(train_Data, test_Data, choose);
                    //normalize
                    knn.nomarlize(minmaxMap, test_Data);
                    knn.nomarlize(minmaxMap, train_Data);
                    double accuracy = checkAccuracy(train_Data, knn, 5, weightList.get(i), matrixMap);
                    if (max_accuracy < accuracy) {
                        max_accuracy = accuracy;
                        best_weight = weightList.get(i);
                    }
                }
                System.out.println("maxAccuracy is " + max_accuracy);
                System.out.println("bestWeightMap is " + best_weight);
                for (DataSet dataSet : testData.getDataSetList()) {
                    String label = knn.KNN(trainData, dataSet, best_weight, matrixMap, 5);
                    System.out.println(label);
                }
                break;
            case 2:
                for (int i = 0; i < weightList.size(); i++) {
                    LoadData train_Data = new LoadData();
                    LoadData test_Data = new LoadData();
                    loadData(train_Data, test_Data, choose);
                    //normalize
                    knn.nomarlize(minmaxMap, test_Data);
                    knn.nomarlize(minmaxMap, train_Data);
                    double mse = checkMSE(train_Data, knn, 5, weightList.get(i), matrixMap);
                    if (min_mse > mse) {
                        min_mse = mse;
                        best_weight = weightList.get(i);
                    }
                }
                System.out.println("minMse is " + min_mse);
                System.out.println("bestWeightMap is " + best_weight);
                for (DataSet dataSet : testData.getDataSetList()) {
                    String score = knn.KNN(trainData, dataSet, best_weight, matrixMap, 5);
                    System.out.println(score);
                }
                break;
        }
    }

    private static void loadData(LoadData trainData, LoadData testData, int choose) {
        switch (choose) {
            case 1:
                testData.loadData("testProdIntro.binary.arff");
                trainData.loadData("trainProdIntro.binary.arff");
                break;
            case 2:
                testData.loadData("testProdIntro.real.arff");
                trainData.loadData("trainProdIntro.real.arff");
                break;
        }
    }

    // shuffle list for validation
    private static List<DataSet> shuffle(LoadData data) {
        List<DataSet> shuffle = new ArrayList<>(data.getDataSetList());
        Collections.shuffle(shuffle);
        return shuffle;
    }

    private static double checkMSE(LoadData loadData, KNN knn, int k,
                                   Map<String, Double> weight,
                                   Map<String, double[][]> matrixMap) {

        loadData.setData(shuffle(loadData));
        List<DataSet> shuffleList = loadData.getDataSetList();

        double mse = 0;
        double currentMse;
        String targetType = loadData.getNameList().get(loadData.getNameList().size() - 1); // for now => label

        // folding validation
        for (int i = 0; i < k; i++) {
            currentMse = 0;
            List<DataSet> testData = new ArrayList<>();
            List<DataSet> trainData = new ArrayList<>(loadData.getDataSetList());
            int fold_num = shuffleList.size() / k;
            // get k-th fold
            for (int j = fold_num * (i + 1) - 1; j >= fold_num * i; j--) {
                testData.add(trainData.remove(j));
            }
            // update load data to folded train data
            loadData.setData(trainData);
            for (DataSet dataSet : testData) {
                double targetValue = dataSet.getValue(targetType);
                double predictValue = Double.parseDouble(knn.KNN(loadData, dataSet, weight, matrixMap, 5));
                currentMse += Math.pow(predictValue - targetValue, 2);
            }
            //update load data back to the shuffled list
            if (i != k - 1) loadData.setData(shuffleList);
            mse += currentMse / testData.size();
        }
        return mse / k;
    }

    private static double checkAccuracy(LoadData loadData, KNN knn, int k,
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
            int fold_num = shuffleList.size() / k;
            // get k-th fold
            for (int j = fold_num * (i + 1) - 1; j >= fold_num * i; j--) {
                testData.add(trainData.remove(j));
            }
            // update load data to folded train data
            loadData.setData(trainData);
            for (DataSet dataSet : testData) {
                if (dataSet.getValue(targetType).equals(map.get(knn.KNN(loadData, dataSet, weight, matrixMap, 5)))) {
                    correct_count++;
                }
            }
            //update load data back to the shuffled list
            if (i != k - 1) loadData.setData(shuffleList);
            accuracy += correct_count / testData.size();
        }
        return accuracy / k;
    }
}
