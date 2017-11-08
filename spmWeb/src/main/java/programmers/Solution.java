package programmers;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*
자연수 n의 각 자리 숫자를 더하면 새로운 자연수 n' 을 만들 수 있습니다. 
또 새로운 자연수 n'의 각 자릿수를 더하여 새로운 자연수 n''을 만들 수 있습니다. 
이 과정을 계속 반복하면 최종적으로 한 자리 숫자를 만들 수 있습니다. 

예를 들어

456789의 각 자리 숫자를 더하면 4 + 5 + 6 + 7 + 8 + 9 = 39
39는 3 + 9 = 12
12는 1 + 2 = 3
과 같이 반복하여 한 자리 숫자 3을 얻을 수 있습니다.

자연수 n이 주어졌을 때 각 자릿수를 더하는 과정을 반복하여 한 자리 숫자를 만들어 반환하는 함수를 완성해 주세요.

제한사항
n은 10이상 231 - 1 이하입니다.
입출력 예
n   result
10  1
456789  3
입출력 예 설명

입출력 예 #1
1 + 0 = 1 이므로 1을 반환합니다.

입출력 예 #2
4 + 5 + 6 + 7 + 8 + 9 = 39
3 + 9 = 12
1 + 2 = 3 이므로 3을 반환합니다.
*/
public class Solution {

    public int solution(int n) {
        int maxdrnum=0;
        maxdrnum = dr(n);
        
        return maxdrnum;
    }
    
    public static int dr(int a) {
        String number = String.valueOf(a);
        int sum = 0;
        
        for (int i=0;i<number.length();i++){
            sum+=Integer.parseInt(String.valueOf(number.charAt(i)));
        }

        if (sum > 9){
            sum = dr(sum);
        }
        
        System.out.println("debug :: " + a +" " + sum);
        return sum;
    }
    
    public static void main(String[] args) {
        Solution c = new Solution();
        int coins = 12313123;
        System.out.println(c.solution(coins));
    }

}
