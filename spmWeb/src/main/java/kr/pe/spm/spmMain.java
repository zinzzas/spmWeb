package kr.pe.spm;

public class spmMain {
    /*findKim 함수(메소드)는 String형 배열 seoul을 매개변수로 받습니다.
     *seoul의 element중 "Kim"의 위치 x를 찾아, "김서방은 x에 있다"는 String을 반환하세요.
     *seoul에 "Kim"은 오직 한 번만 나타나며 잘못된 값이 입력되는 경우는 없습니다.
     * */
    public String spmMain(String[] seoul){
        //x에 김서방의 위치를 저장하세요.
        int x = 0;
        String str = "Kim";
        
        for (String string : seoul) {
            if(str.equals(string)){
                x++;
            }else{
                x++;
            }
        }
        
        return "김서방은 "+ x + "에 있다";
    }
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        spmMain kim = new spmMain();
        String[] names = {"Queen", "Tod","Kim"};
        System.out.println(kim.spmMain(names));
    }
}
