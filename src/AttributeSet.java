import java.util.HashMap;
import java.util.Map;

/**
 * @author Shulin Huang
 * @date 4/4/18
 **/

 /**
  * Reference:
  * https://github.com/wihoho/KNN
  * https://github.com/badlogic/knn
  * https://github.com/Stephaniefan/knn
  **/

// Helper class to store attribute data for @attribute
public class AttributeSet {

    private String name;

    // Enum for two type attributes -> real & text
    // @attribute XXXX real
    // @attribute xxxx {xxx,xxx,xxx,xxx}
    private enum Type {
        REALNUM, TEXT
    }
    private Type type;

    // map to store attribute's type & value
    private Map<String, Double> typeValue;

    // Getters
    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Map<String, Double> getTypeValue() {
        return typeValue;
    }

    public boolean isRealNum(){
        return this.type == Type.REALNUM;
    }
    // Attribute constructor
    public AttributeSet(String name, String type) {
        this.name = name;
        if(type.equals("real")){
            this.type = Type.REALNUM;
            typeValue = null;
        }else {
            this.type = Type.TEXT;
            // xxxx {xxx,xxx,xxx,xxx}
            typeValue = new HashMap<>();
            String[] texts = type.substring(1, type.length()-1).split(",");
            for(int i=0; i<texts.length; i++){
//                System.out.println(texts[i]);
                typeValue.put(texts[i], (double)i);
            }
        }
    }

    @Override
    public String toString() {
        return "[Attribute] " + "name: " + name + ", type: " + type + ", typeValues: " + typeValue;
    }

    public static void main(String[] args) {
        // for test
        AttributeSet attributeA = new AttributeSet("LifeStyle", "{spend<<saving,spend<saving,spend>saving,spend>>saving}");
        AttributeSet attributeB = new AttributeSet("property",  "real");
        System.out.println(attributeA.toString());
        System.out.println(attributeB.toString());
    }

}
