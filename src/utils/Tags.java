/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

/**
 *
 * @author skumar34
 */
public class Tags implements Comparable{
    public String key;
    public double value;

    public Tags()
    {

    }

    public Tags(String key, double value) {
        this.key = key;
        this.value = value;
    }
     public int compareTo(Object obj)
        {
            Tags tempObject=new Tags();
            tempObject=(Tags) obj;
            if(this.value>tempObject.value)
                return 1;
            if(this.value<tempObject.value)
                return -1;
            else
                return 0;
        }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
