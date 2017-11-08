package programmers;

/*
나누어 떨어지는 숫자 배열
Level 1
divisible 메소드는 int형 배열 array와 int divisor를 매개변수로 받습니다.
array의 각 element 중 divisor로 나누어 떨어지는 값만 포함하는 새로운 배열을 만들어서 반환하도록 divisible에 코드를 작성해 보세요.

예를들어 array가 {5, 9, 7, 10}이고 divisor가 5이면 {5, 10}을 리턴해야 합니다.
*/

import java.util.*;

public class Divisible {
    public int[] divisible(int[] array, int divisor) {
    
        ArrayList<Integer> list = new ArrayList<Integer>();
        for(int i=0;i<array.length;i++){
            if(array[i]%divisor == 0){
            list.add(array[i]);
            //System.out.println("==>"+array[i]);
          }
        }
        
        //ret에 array에 포함된 정수중, divisor로 나누어 떨어지는 숫자를 순서대로 넣으세요.
        //int[] ret = Ints.toArray(list);
        int[] ret = new int[list.size()];
        
        for(int i=0;i<list.size();i++){
            ret[i] = list.get(i);
        }
        
        return ret; 
    }

    // 아래는 테스트로 출력해 보기 위한 코드입니다.
    public static void main(String[] args) {
        Divisible div = new Divisible();
        int[] array = {5, 9, 7, 10};
        System.out.println( Arrays.toString( div.divisible(array, 5) ));
    }
}

/*
1. 타인


import java.util.Arrays;

class Divisible {
    public int[] divisible(int[] array, int divisor) {
        //ret에 array에 포함된 정수중, divisor로 나누어 떨어지는 숫자를 순서대로 넣으세요.
    int count = 0;
    for(int i = 0; i<array.length;i++){
      if(array[i]%divisor == 0) count++;
    }
        int[] ret = new int[count];
    int index = 0;
    for(int i=0; i<array.length;i++){
      if(array[i]%divisor == 0){
        ret[index++]=array[i];
      }
    }

        return ret;
    }
    // 아래는 테스트로 출력해 보기 위한 코드입니다.
    public static void main(String[] args) {
        Divisible div = new Divisible();
        int[] array = {5, 9, 7, 10};
        System.out.println( Arrays.toString( div.divisible(array, 5) ));
    }
}
*/

/*
2. 타인


import java.util.ArrayList;
import java.util.Arrays;

public class Divisible {
    public int[] divisible(int[] array, int divisor) {

        // ret에 array에 포함된 정수중, divisor로 나누어 떨어지는 숫자를 순서대로 넣으세요.
        ArrayList<Integer> ret = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            if (array[i] % divisor == 0) {
                ret.add(array[i]);
            }
        }

        return ret.stream().mapToInt(i -> i).toArray();
    }

    // 아래는 테스트로 출력해 보기 위한 코드입니다.
    public static void main(String[] args) {
        Divisible div = new Divisible();
        int[] array = { 5, 9, 7, 10 };
        System.out.println(Arrays.toString(div.divisible(array, 5)));
    }
}*/

/*
3. 타인

import java.util.Arrays;

class Divisible {
    public int[] divisible(int[] array, int divisor) {
        //ret에 array에 포함된 정수중, divisor로 나누어 떨어지는 숫자를 순서대로 넣으세요.
        return Arrays.stream(array).filter(factor -> factor % divisor == 0).toArray();
    }
    // 아래는 테스트로 출력해 보기 위한 코드입니다.
    public static void main(String[] args) {
        Divisible div = new Divisible();
        int[] array = {5, 9, 7, 10};
        System.out.println( Arrays.toString( div.divisible(array, 5) ));
    }
}
*/