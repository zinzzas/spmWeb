/**
 * @author lee.jongpil
 *reverseStr 메소드는 String형 변수 str을 매개변수로 입력받습니다.
 *str에 나타나는 문자를 큰것부터 작은 순으로 정렬해 새로운 String을 리턴해주세요. 
 *str는 영문 대소문자로만 구성되어 있으며, 대문자는 소문자보다 작은 것으로 간주합니다. 
 *예를들어 str이 "Zbcdefg"면 "gfedcbZ"을 리턴하면 됩니다.
 *
 */
package programmers;

import java.util.Arrays;
import java.util.Collections;

public class ReverseStr {
    public String reverseStr(String str){
        String[] arr = str.split("");
        Arrays.sort(arr, Collections.reverseOrder());
        
        StringBuffer sb = new StringBuffer();
        for (int i=0; i < arr.length; i++) {
            sb.append(arr[i]);
        }
        
        return sb.toString();
    }

    // 아래는 테스트로 출력해 보기 위한 코드입니다.
    public static void main(String[] args) {
        ReverseStr rs = new ReverseStr();
        System.out.println( rs.reverseStr("Zbcdefg") );
    }
}


/* 1. 타인
 * */
/*
import java.util.Arrays;

public class ReverseStr {
    public String reverseStr(String str){
        char[] sol = str.toCharArray();
        Arrays.sort(sol);
        return new StringBuilder(new String(sol)).reverse().toString();
    }

    // 아래는 테스트로 출력해 보기 위한 코드입니다.
    public static void main(String[] args) {
        ReverseStr rs = new ReverseStr();
        System.out.println( rs.reverseStr("Zbcdefg") );
    }
}
*/

/*
 * 2. 타임
 */

/*
public class ReverseStr {
    public String reverseStr(String str){
    char[] chars = str.toCharArray();
     char temp =' ';

     for(int i = 0; i < str.length(); i++){
         for(int j = 0; j < str.length()-1 ; j++){
             if( chars[j] < chars[j+1]){
                 temp = chars[j];
                 chars[j] = chars[j+1];
                 chars[j+1] = temp;
             }
         }
     }
     str = new String(chars, 0, chars.length);
        return str;
    }

    // 아래는 테스트로 출력해 보기 위한 코드입니다.
    public static void main(String[] args) {
        ReverseStr rs = new ReverseStr();
        System.out.println( rs.reverseStr("Zbcdefg") );
    }
}
*/




 