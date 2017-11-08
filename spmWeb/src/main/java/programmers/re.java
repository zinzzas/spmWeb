package programmers;

import java.util.Scanner;

public class re {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a=sc.nextInt();
        int maxdrnum=0;
        int maxdr=0;

        for (int i=0;i<a;i++) {
            int b=sc.nextInt();
            int c=dr(b);

            if (i==0 || maxdr<c) {
                maxdr=c;
                maxdrnum=b;
            } else if (maxdr==c & maxdrnum > b) {
                maxdrnum=b;
            }
        }

        sc.close();     
        System.out.println(maxdrnum);
    }
    
    public static int dr (int a) {

        String number = String.valueOf(a);

        int sum=0;

        for (int i=0;i<number.length();i++){

            sum+=Integer.parseInt(String.valueOf(number.charAt(i)));

        }

        if (sum>9) sum=dr(sum);

        System.out.println("debug :: " + a +" " + sum );

        return sum;

    }


}
