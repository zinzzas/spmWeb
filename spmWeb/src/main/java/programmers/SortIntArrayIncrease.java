package programmers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*

길이가 n인 배열에 1부터 n까지 숫자가 중복 없이 한 번씩 들어 있는지를 확인하려고 합니다.
1부터 n까지 숫자가 중복 없이 한 번씩 들어 있는 경우 true를, 아닌 경우 false를 반환하도록 함수 solution을 완성해주세요.

제한사항
배열의 길이는 10만 이하입니다.
배열의 원소는 10만 이하의 자연수입니다.

입출력 예
-------------------------
arr             result
-------------------------
[4, 1, 3, 2]    true
[4, 1, 3]       false

입출력 예 설명
입출력 예 #1
입력이 [4, 1, 3, 2]가 주어진 경우, 배열의 길이가 4이므로 배열에는 1부터 4까지 숫자가 모두 들어 있어야 합니다. 
[4, 1, 3, 2]에는 1부터 4까지의 숫자가 모두 들어 있으므로 true를 반환하면 됩니다.

입출력 예 #2
[4, 1, 3]이 주어진 경우, 배열의 길이가 3이므로 배열에는 1부터 3까지 숫자가 모두 들어 있어야 합니다. 
[4, 1, 3]에는 2가 없고 4가 있으므로 false를 반환하면 됩니다.


*/
public class SortIntArrayIncrease {
    public boolean solution(int[] arr) {
        boolean answer = true;
        
        ArrayList<String> list = new ArrayList<String>();
        for(int i=0;i<arr.length;i++){
            list.add(String.valueOf(arr[i]));
            //System.out.println("==>"+arr[i]);
        }
        
        Collections.sort(list, Collections.reverseOrder());
        Collections.reverse(list);
        
        /*StringBuffer sb = new StringBuffer();
        for (String string : list) {
            sb.append(string);
            System.out.println("==> string for "+string);
        }*/
        
        int min = Integer.parseInt(Collections.min(list));
        
        //System.out.println("==> min "+min);
        //System.out.println("==> list.size() "+list.size());
        
        for (int i = 0; i < list.size(); i++) {
            //System.out.println("==> "+min + ", " +Integer.parseInt(list.get(i)));
            if (min != Integer.parseInt(list.get(i)) ){
                answer = false;
                break;
            }
            
            min++;
        }
        
        return answer;
    }
    
    public static void main(String[] args) {
        SortIntArrayIncrease c = new SortIntArrayIncrease();
        int[] coins = {5, 6, 7, 8, 1, 2, 3, 4};
        System.out.println(c.solution(coins));
    }
}
