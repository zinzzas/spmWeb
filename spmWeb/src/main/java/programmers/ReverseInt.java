package programmers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReverseInt {

    public int reverseInt(int n){
        
        String num = String.valueOf(n);
        List<String> list = new ArrayList<String>();
        
        for (int i=0; i < num.length(); i++) {
            //num.charAt(i);
            list.add(Character.toString(num.charAt(i)));
            //System.out.println(num.charAt(i));
        }
        
        Collections.sort(list, Collections.reverseOrder());
        //Collections.reverse(list);
        
        StringBuffer sb = new StringBuffer();
        for (String string : list) {
            sb.append(string);
        }
        
        return Integer.parseInt(sb.toString());
    }
  
    // 아래는 테스트로 출력해 보기 위한 코드입니다.
    public static void  main(String[] args){
        ReverseInt ri = new ReverseInt();
        System.out.println(ri.reverseInt(118372));
    }

}


/*
public class ReverseInt {
    String res = "";
    public int reverseInt(int n){
        res = "";
        Integer.toString(n).chars().sorted().forEach(c -> res = Character.valueOf((char)c) + res);
        return Integer.parseInt(res);
    }

    // 아래는 테스트로 출력해 보기 위한 코드입니다.
    public static void  main(String[] args){
        ReverseInt ri = new ReverseInt();
        System.out.println(ri.reverseInt(118372));
    }
}
*/

/*
import java.util.Arrays;

public class ReverseInt {
    public int reverseInt(int n){

        String str = Integer.toString(n);
        char[] c = str.toCharArray();
        Arrays.sort(c);
        StringBuilder sb = new StringBuilder(new String(c,0,c.length));  
        return Integer.parseInt(((sb.reverse()).toString()));
    }

    // 아래는 테스트로 출력해 보기 위한 코드입니다.
    public static void  main(String[] args){
        ReverseInt ri = new ReverseInt();
        System.out.println(ri.reverseInt(118372));
    }
}
*/


/*
public class ReverseInt {
    public int reverseInt(int n){
    char[] numbers = Integer.toString(n).toCharArray();
    String strSort = "";

    if(n <= 0) return 0;

    for(int i = 0; i < numbers.length; i++) {
      for(int j = 0; j < i; j++) {
        if(numbers[i] - 48 > numbers[j] - 48) {
          char temp = numbers[i];
          numbers[i] = numbers[j];
          numbers[j] = temp;
        }
      }
    }
    strSort = new String(numbers);

        return Integer.parseInt(strSort);
    }

    // 아래는 테스트로 출력해 보기 위한 코드입니다.
    public static void  main(String[] args){
        ReverseInt ri = new ReverseInt();
        System.out.println(ri.reverseInt(118372));
    }
}

*/

/*
import java.util.Arrays;
public class ReverseInt {
    public int reverseInt(int n){
    String[] strArr = String.valueOf(n).split("");
        Arrays.sort(strArr, String.CASE_INSENSITIVE_ORDER);
        String result = "";
        for (int i = strArr.length - 1; i >= 0; i--) {
            result += strArr[i];
        }
        return Integer.parseInt(result);
    }

    // 아래는 테스트로 출력해 보기 위한 코드입니다.
    public static void  main(String[] args){
        ReverseInt ri = new ReverseInt();
        System.out.println(ri.reverseInt(118372));
    }
}

*/
