package com.kakao.pay.sample.service;

import com.kakao.pay.sample.model.ReadyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by kakaopay
 */
@Service
public class SampleService {
    @Value("${kakao.api.admin.key}")
    private String kakaoApiAdminKey;

    @Value("${cid}")
    private String cid;

    @Value("${sample.host}")
    private String sampleHost;

    private String tid;

    public ReadyResponse ready(String agent, String openType) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + kakaoApiAdminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("cid", cid);                                       // 가맹점 코드
        paramMap.add("partner_order_id", "1");                          // 주문번호
        paramMap.add("partner_user_id", "1");                           // 회원 ID
        paramMap.add("item_name", "상품명");                              // 상품명
        paramMap.add("quantity", "1");                                  // 수량
        paramMap.add("total_amount", "1100");                           // 상품 총액
        paramMap.add("tax_free_amount", "0");                           // 상품 비과세 금액
        paramMap.add("vat_amount", "100");                              // 상품 부가세 금액
        paramMap.add("approval_url", sampleHost + "/approve/" + agent + "/" + openType);  // 결제성공 redirect url
        paramMap.add("cancel_url", sampleHost + "/cancel/" + agent + "/" + openType);     // 결제취소 redirect url
        paramMap.add("fail_url", sampleHost + "/fail/" + agent + "/" + openType);         // 결제실패 redirect url

        HttpEntity<MultiValueMap<String, String>> entityMap = new HttpEntity<MultiValueMap<String, String>>(paramMap, headers);
        ResponseEntity<ReadyResponse> response = new RestTemplate().postForEntity("https://kapi.kakao.com/v1/payment/ready", entityMap, ReadyResponse.class);
        ReadyResponse readyResponse = response.getBody();

        // 주문번호와 TID를 매핑해서 저장해놓는다.
        // Mapping TID with partner_order_id then save it to use for approval request.
        this.tid = readyResponse.getTid();
        return readyResponse;
    }

    public String approve(String pgToken) {
        // ready할 때 저장해놓은 TID로 승인 요청
        // Call “Execute approved payment” API by pg_token, TID mapping to the current payment transaction and other parameters.
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + kakaoApiAdminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("cid", cid);               // 가맹점 코드
        paramMap.add("tid", this.tid);          // 결제 고유번호
        paramMap.add("partner_order_id", "1");  // 주문번호(ready할 때 사용했던 값)
        paramMap.add("partner_user_id", "1");   // 회원 ID(ready할 때 사용했던 값)
        paramMap.add("pg_token", pgToken);      // pg token

        HttpEntity<MultiValueMap<String, String>> entityMap = new HttpEntity<MultiValueMap<String, String>>(paramMap, headers);
        try {
            ResponseEntity<String> response = new RestTemplate().postForEntity("https://kapi.kakao.com/v1/payment/approve", entityMap, String.class);

            // 승인 결과를 저장한다.
            // save the result of approval
            String approveResponse = response.getBody();
            return approveResponse;
        } catch (HttpStatusCodeException ex) {
            return ex.getResponseBodyAsString();
        }
    }
}
