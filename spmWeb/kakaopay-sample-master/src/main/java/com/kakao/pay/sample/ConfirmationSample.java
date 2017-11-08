package com.kakao.pay.sample;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by blitz on 2017. 5. 11..
 */
public class ConfirmationSample {

    static String pcConfirmationUrlPrefix = "https://pg-web.kakao.com/v1/confirmation/p/";
    static String mobileConfirmationUrlPrefix = "https://pg-web.kakao.com/v1/confirmation/m/";

    static String cid = "C847130110";
    static String tid = "T2345560999051526180";
    static String partnerOrderId = "6406";
    static String partnerUserId = "pg_qa";
    static String paymentAid = "A2345561170850086930";
    static String cancelAid = "A2345583027818929490";

    public static void main(String[] args) {
        // 결제 증빙
        System.out.println("pc payment confirmation url : " + pcConfirmationUrlPrefix + paymentAid + "/" + getHash(paymentAid));
        System.out.println("mobile payment confirmation url : " + mobileConfirmationUrlPrefix + paymentAid + "/" + getHash(paymentAid));

        // 취소 증빙
        System.out.println("pc cancel confirmation url : " + pcConfirmationUrlPrefix + cancelAid + "/" + getHash(cancelAid));
        System.out.println("mobile cancel confirmation url : " + mobileConfirmationUrlPrefix + cancelAid + "/" + getHash(cancelAid));
    }

    private static String getHash(String aid) {
        return DigestUtils.sha256Hex(cid + tid + partnerOrderId + partnerUserId + aid);
    }
}