/**
 * 
 */
/**
 * @author lee.jongpil
 *
 */
package kr.pe.spm.biz.ord;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.gsshop.base.exception.BizRuntimeException;
import com.gsshop.base.util.SessionUtils;
import com.gsshop.biz.ord.service.OrderService;
import com.gsshop.biz.ord.vo.OrderVO;
import com.gsshop.common.Globals;

@Controller
@RequestMapping(value="biz/ord/order/*")
public class ApgController {

    static Logger logger = LoggerFactory.getLogger(ApgController.class);

    @Autowired
    private OrderService orderService;

    private final static String[] timestampPattern = {"yyyy-MM-dd'T'HH:mm:ss.SSS"};
    private final static FastDateFormat gmtTimestamp = FastDateFormat.getInstance(timestampPattern[0], TimeZone.getTimeZone("GMT"), Locale.getDefault());
    private final static FastDateFormat gmtSigTimestamp = FastDateFormat.getInstance("yyyyMMddHHmmss", TimeZone.getTimeZone("GMT"), Locale.getDefault());
    private final static SimpleDateFormat parseFormat = new SimpleDateFormat(timestampPattern[0]);
    static {
        parseFormat.setCalendar(Calendar.getInstance(new SimpleTimeZone(0, "GMT")));
    }

    /**  channel+currencycode+currencyexp+custdetail+itemtotal+eppinfo+extradata+items+partnerkey+partnerrefno+paymenttype+recurringinfo+responseurl+shippingamount+taxamount+title+totalamount+transtype */
    private final static String [] CREATE_TRANSACTION_PARAMETER_ORDER = new String[] { "agentid", "channel","currencycode","currencyexp","custdetail","itemtotal","eppinfo","extradata","items","partnerkey","partnerrefno","paymenttype","recurringinfo","responseurl","shippingamount","taxamount","title","totalamount","transtype"};

    /**  bankappcode + bankappdate + errorcode + partnerrefno + providerkey + result + transactionid */
    private final static String [] RESPONSE_APG_PARAMETER_ORDER = new String[] {"bankappcode", "bankappdate", "errorcode", "partnerrefno", "providerkey", "result", "transactionid"};

    /**  currencycode+currencyexp+custdetail+errorcode+items+itemTotal+partnerrefno+paymentdetails+paymenttype+shippingAmount+result+taxAmount+totalAmount+totalpaymentcount+transtype  */
    private final static String [] GET_TRANSACTION_RESULT_PARAMETER_ORDER = new String[] {"currencycode","currencyexp","custdetail","errorcode","items","itemtotal","partnerrefno","paymentdetails","paymenttype","shippingamount","result","taxamount","totalamount","totalpaymentcount","transtype"};

    private String CURRENCYCODE = "MYR";
    private int    CURRENCYEXP = 2;
    private String PAYMENT_TYPE = Globals.APG_PAYMENT_TYPE;//"";
    private String CHANNEL_3D       = Globals.APG_CHANNEL_3D;// (channel : web(3d/casa), only credit card(3d), only casa (??))
    private String CHANNEL_CASA     = Globals.APG_CHANNEL_CASA;// (channel : web(3d/casa), only credit card(3d), only casa (??))

    private ObjectMapper objectMapper = new ObjectMapper();
    //private int TIMEOUT = 10000;
    
    /**
     * APG realtime payment Popup
     * 1. call CreateTransaction
     * 2. if CT is success then call apgEpayment
     *    else print error
     * 3. receive returnurl from apg
     * @throws JSONException
     */
    @RequestMapping(value="ordAPGPaymentPop")
    public ModelAndView ordAPGPaymentPop(HttpServletRequest request, @RequestParam Map<String, Object> paramMap, HttpSession session) throws JSONException {
        ModelAndView mav = new ModelAndView();
        RedirectView rv  = new RedirectView();
        
        String URL_CREATE_TRANSACTION = getApgUrl(request, Globals.APG_CREATE_TRANSACTION_URL);//;
        String URL_APG_EPAYMENT = getApgUrl(request, Globals.APG_EPAYMENT_URL);

        String createTransactionRequestJsonStr = "";
        String createTransactionResponseJsonStr = "";
        String logParams = "";
        
        //REMOVE ORDERVO INFO.
        session.removeAttribute("OrderVO");
        
        //ADD ORDERVO (PRODUCTS INFO & ORDER INFO & GTAGDATA)
        OrderVO orderVo = new OrderVO();
        orderVo.setOrderInfo((String)paramMap.get("ORDERINFO"));
        orderVo.setOrdInfo((String)paramMap.get("ORDINFO"));
        orderVo.setPrdDtlInfo((String)paramMap.get("PRDDTLINFO"));
        orderVo.setGtagData((String)paramMap.get("gTagData"));
        orderVo.setProductArray((String)paramMap.get("productArray"));

        //ADD SESSION ORDERVO
        session.setAttribute("OrderVO", orderVo);
        
        //ADD ORDER INFO
        
        Map<String, Object> pMap = new HashMap<String, Object>();
        pMap.put("PAYORDERINFO", paramMap.get("ORDINFO"));
        pMap.put("APGINFO", paramMap.get("APGINFO"));
        pMap.put("ORD_ID", paramMap.get("ORD_ID"));
        pMap.put("INST_ID", SessionUtils.getSessionVO().getCstNm());
        
        orderService.savePaymentInfo(pMap);
        
        logger.debug("SessionUtils.getOrderVO() {}", SessionUtils.getOrderVO());
        
        //local Test dummy
        /*rv.setUrl("ordAPGBankTestForm.do");
        rv.setExposeModelAttributes(false);
        mav.setView(rv);*/
        
        try {
            String pgDlId = orderService.findPgDlIdNew();
            paramMap.put("PG_DL_ID", pgDlId);//PK of log table
            
            //1. create message for transactionid
            createTransactionRequestJsonStr = getCreateTransactionMessage(request, paramMap, false);

            //1-1 logging create message for transactionid
            logParams = createTransactionRequestJsonStr;
            loggingApg("CreateTransaction", paramMap);

            //2. call CreateTransaction
            createTransactionResponseJsonStr = callApgTransaction(URL_CREATE_TRANSACTION, createTransactionRequestJsonStr);
            logger.error("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy ordAPGPaymentPop");
            //2-1 vaidate CreateTransaction response
            String transactionid = validateCreateTransactionResponse(createTransactionResponseJsonStr);
            {
                Map<String, Object> logMap = new HashMap<String, Object>();
                logMap.put("PG_DL_ID", paramMap.get("PG_DL_ID"));
                logMap.put("PG_TRN_ID", transactionid);
                loggingApg("RedirectToAPG", logMap);
            }

            String redirectToApgUrl = URL_APG_EPAYMENT + transactionid;
            mav.addObject("redirectUrl", redirectToApgUrl);
            mav.addObject("transactionid", transactionid);
            mav.addObject("pgProgressId", paramMap.get("pgProgressId"));
            mav.addObject("PG_DL_ID", pgDlId);
            
            //APG Redirect Set!! ( i-web redirect )
            rv.setUrl(redirectToApgUrl);
            rv.setExposeModelAttributes(false);
            mav.setView(rv);
        } catch (Exception e){
            e.printStackTrace();
            /*JSONObject createTranscationRespJson = new JSONObject(createTransactionResponseJsonStr);
            JSONObject createTransactionBody  = createTranscationRespJson.getJSONObject("body");
            
            paramMap.put("APV_ERR_CD", createTransactionBody.get("errorcode"));*/
            paramMap.put("APV_ERR_MSG", e.getMessage());
            loggingApg("CreateTransaction Fail", paramMap);
            mav.addObject("apgErrorMessage", e.getMessage());
        } finally {

            StringBuffer ctJsonLogInclItem = new StringBuffer(URL_CREATE_TRANSACTION);
            StringBuffer ctJsonLog = new StringBuffer(URL_CREATE_TRANSACTION);
            ctJsonLog.append("\n Request = ");
            ctJsonLogInclItem.append("\n Request = ");
            try {
                int itemsIndex = createTransactionRequestJsonStr.indexOf("\"items\"")-1;
                int itemsIndexEnd = createTransactionRequestJsonStr.indexOf("}],\"", itemsIndex)+1;
                String beforeItems = createTransactionRequestJsonStr.substring(0, itemsIndex + 10);//10 : "\"items\": [".length()
                String afterItems = createTransactionRequestJsonStr.substring(itemsIndexEnd);
                ctJsonLog.append(new JSONObject(beforeItems + afterItems).toString(1));
                ctJsonLogInclItem.append(new JSONObject(createTransactionRequestJsonStr).toString(1));
            } catch(Exception e){
                e.printStackTrace();
                ctJsonLog.append(createTransactionRequestJsonStr);
            }
            ctJsonLog.append("\n Response = ");
            ctJsonLogInclItem.append("\n Response = ");
            try {
                if ( StringUtils.isNotEmpty(createTransactionResponseJsonStr) ) {
                    ctJsonLog.append(new JSONObject(createTransactionResponseJsonStr).toString(1));
                    ctJsonLogInclItem.append(new JSONObject(createTransactionResponseJsonStr).toString(1));
                }
            } catch(Exception e){
                e.printStackTrace();
                ctJsonLog.append(createTransactionResponseJsonStr);
            }
            logParams = ctJsonLog.toString();
            logger.debug(ctJsonLogInclItem.toString());
            {
                Map<String, Object> logMap = new HashMap<String, Object>();
                logMap.put("PG_DL_ID", paramMap.get("PG_DL_ID"));
                logMap.put("PARAMS", logParams);
                loggingApg(null, logMap);
            }
        }

        return mav;
    }

    /**
     * APG에서 결제완료후에 호출해주는 responseurl
     * @param paramMap
     */
    @RequestMapping(value="ordAPGResult")
    public ModelAndView ordAPGResult(HttpServletRequest request, @RequestParam Map<String, Object> paramMap){
        ModelAndView mav = new ModelAndView();
        RedirectView rv = new RedirectView();
        
        String pgDlIds = (String)paramMap.get("partnerrefno");
        
        //MC APG Payment success before back button(user) control. MR.JP - 2015.02.04
        if( !"".equals(pgDlIds) ){
            String orderInfoCnt = orderService.findOrderStatusInfo(pgDlIds);
            
            if( NumberUtils.toDouble((String)orderInfoCnt) <= 0 ){
                rv.setUrl("/index.do");
                rv.setExposeModelAttributes(false);
                mav.setView(rv);
                return mav;
            }
        }
        
        ordAPGResultInner(mav, request, paramMap);
        
        mav.addObject("APG_PENDING_TIME_OUT", Globals.APG_PENDING_TIME_OUT);
        
        return mav;
    }

    /**
     * Parse parameter from APG payment complete or cancel.
     * if success
     * > parameter sample <
     *  result : [true]
     *  bankappdate : [2014-09-17T08:27:08.9355258Z]
     *  partnerrefno : [00001547]
     *  providerkey : [ctb.emoto]
     *  timestamp : [2014-09-17T08:27:09.7935540Z]
     *  resultdescription : [success]
     *  transactionid : [hscc19H8261847BFJ6]
     *  bankappcode : [846709]
     *  errorcode : [01]
     *  sig : [o2hAQaBxKJpUBLeB9xDE07VOe3/CwWdioB6u33CqcDk=]
     * @param mav
     * @param request
     * @param paramMap
     * @param mode
     */
    private void ordAPGResultInner(ModelAndView mav, HttpServletRequest request, @RequestParam Map<String, Object> paramMap) {
        //String URL_GET_TRANSACTION_RESULT = Globals.APG_GET_TRANSACTION_RESULT_URL;

        String getTransactionResultRequestJsonStr = "";
        String getTransactionResultResponseJsonStr = "";

        try {
            {
                Map<String, Object> logMap = new HashMap<String, Object>();
                logMap.put("PG_DL_ID", paramMap.get("partnerrefno"));
                logMap.put("BN_ID", paramMap.get("providerkey"));
                logMap.put("APV_DL_NO", paramMap.get("bankappcode"));
                String bankappdate = (String)paramMap.get("bankappdate");
                if ( StringUtils.isNotEmpty(bankappdate)) {
                    try{
                        bankappdate = utcToLocaltime(getSigTimestampStr(parseTimestamp(bankappdate)));
                    } catch (Exception e){
                        e.printStackTrace();
                        logger.error(e.getMessage());
                    }
                    logMap.put("APV_DL_DTM", bankappdate);//yyyyMMddHHmmss
                }
                logMap.put("APV_NO", paramMap.get("transactionid"));
                logMap.put("APV_ERR_CD", paramMap.get("errorcode"));
                logMap.put("APV_ERR_MSG", paramMap.get("resultdescription"));
                logMap.put("PARAMS",   "\n" + request.getHeader("referer")
                                     + "\n" + request.getRequestURL().toString()
                                     + "\n" + paramMap.toString());
                loggingApg("ResponseFromAPG", logMap);
            }

            String beforeHash = makeResponseurlSigStr(paramMap, RESPONSE_APG_PARAMETER_ORDER);
            String timestamp = (String)paramMap.get("timestamp"); //2014-09-10T07:46:03.0954783Z
            String sigTimestampStr = getSigTimestampStr(parseTimestamp(timestamp));
            String encodedHash = encodeSHA256AndBASE64(sigTimestampStr, beforeHash);
            if ( !encodedHash.equals(paramMap.get("sig")) ) {
                {
                    Map<String, Object> logMap = new HashMap<String, Object>();
                    logMap.put("PG_DL_ID", paramMap.get("partnerrefno"));
                    logMap.put("APV_ERR_MSG", "Invalid siginfo response from APG");
                    loggingApg("ResponseFromAPG", paramMap);
                }
                throw new Exception("Invalid siginfo.");
            } else {
                if ( "true".equals(paramMap.get("result")) ) {
                    mav.addObject("providerkey", paramMap.get("providerkey"));
                    mav.addObject("bankappcode", paramMap.get("bankappcode"));
                    mav.addObject("errorcode", paramMap.get("errorcode"));
                    logger.error("AGSS_APG_RETURN_RESULT (2step) : " + paramMap.get("result")); 
                    //getTransactionResultDo(mav, request, paramMap, getTransactionResultRequestJsonStr, getTransactionResultResponseJsonStr, URL_GET_TRANSACTION_RESULT);
                    mav.addObject("partnerrefno", paramMap.get("partnerrefno"));
                    mav.addObject("transactionid", paramMap.get("transactionid"));

                } else {
                    {
                        Map<String, Object> logMap = new HashMap<String, Object>();
                        logMap.put("PG_DL_ID", paramMap.get("partnerrefno"));
                        logMap.put("PG_STATUS", "F"); //pg logging. Failed payment
                        logMap.put("APV_ERR_CD", paramMap.get("errorcode"));
                        logMap.put("APV_ERR_MSG", paramMap.get("resultdescription"));
                        loggingApg("ResponseFromAPG", paramMap);
                        mav.addObject("errorcode", paramMap.get("errorcode"));
                    }
                    mav.addObject("apgErrorMessage", paramMap.get("resultdescription")+ "\\ntransactionid : " + paramMap.get("transactionid"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            mav.addObject("apgErrorMessage", e.getMessage());
        }
    }

    /**
     * GetTransactionResult test only
     * @param request
     * @param paramMap
     * @return
     * @deprecated
     */
    @RequestMapping(value="GetTransactionResult")
    public ModelAndView GetTransactionResult(HttpServletRequest request, @RequestParam Map<String, Object> paramMap) {
        ModelAndView mav = new ModelAndView();
        String URL_GET_TRANSACTION_RESULT = Globals.APG_GET_TRANSACTION_RESULT_URL;
        String getTransactionResultRequestJsonStr = "";
        String getTransactionResultResponseJsonStr = "";

        try {
            getTransactionResultDo(mav, request, paramMap, getTransactionResultRequestJsonStr, getTransactionResultResponseJsonStr, URL_GET_TRANSACTION_RESULT);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            mav.addObject("apgErrorMessage", e.getMessage());
        } finally {
            //logger.debug("URL_GET_TRANSACTION_RESULT : " + URL_GET_TRANSACTION_RESULT);
        }
        mav.setViewName("/biz/ord/order/ordAPGResult");
        return mav;
    }

   /**
     * @param mav
     * @param request
     * @param paramMap
     * @param getTransactionResultRequestJsonStr
     * @param getTransactionResultResponseJsonStr
     * @param URL_GET_TRANSACTION_RESULT
     * @throws Exception
     */
    private void getTransactionResultDo(ModelAndView mav, HttpServletRequest request, @RequestParam Map<String, Object> paramMap,
                String getTransactionResultRequestJsonStr, String getTransactionResultResponseJsonStr, String URL_GET_TRANSACTION_RESULT) throws Exception {

        {
            Map<String, Object> logMap = new HashMap<String, Object>();
            logMap.put("PG_DL_ID", paramMap.get("partnerrefno"));
            if ( paramMap.get("partnerrefno") != null ) {
                loggingApg("GetTransactionResult", logMap);
            }
        }
        URL_GET_TRANSACTION_RESULT = getApgUrl(request, URL_GET_TRANSACTION_RESULT);
        getTransactionResultRequestJsonStr  = getGetTransactionResultMessage(request, paramMap);
        getTransactionResultResponseJsonStr = callApgTransaction(URL_GET_TRANSACTION_RESULT, getTransactionResultRequestJsonStr);
        StringBuffer gtrJsonLog = new StringBuffer(URL_GET_TRANSACTION_RESULT);
        gtrJsonLog.append("\n Request = ").append(new JSONObject(getTransactionResultRequestJsonStr).toString(1));
        logger.error("AGSS_APG_TRANSACTION_RESULT_JSON (3step) : " + getTransactionResultResponseJsonStr);
        
        int itemIndex = getTransactionResultResponseJsonStr.indexOf("\"items\"")-1;
        int itemIndexEnd = getTransactionResultResponseJsonStr.indexOf("}],\"", itemIndex)+1;

        String beforeItems = getTransactionResultResponseJsonStr.substring(0, itemIndex + 10);//10 : "\"items\": [".length()
        String afterItems = getTransactionResultResponseJsonStr.substring(itemIndexEnd);
        gtrJsonLog.append("\n Response = ").append(new JSONObject(beforeItems+afterItems).toString(1));
        String logParams = gtrJsonLog.toString();
        logger.error("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx getTransactionResultDo");
        Map<String, String> validateMap     = validateGetTransactionResultResponse(getTransactionResultResponseJsonStr);

        if ( StringUtils.isEmpty(validateMap.get("errCd"))) {
            mav.addObject("resultdescription"   , paramMap.get("resultdescription"));
            mav.addObject("partnerrefno"        , validateMap.get("partnerrefno"));
            mav.addObject("transactionid"       , paramMap.get("transactionid"));

            mav.addObject("providerkey"         , validateMap.get("providerkey"));
            String bankappcode = validateMap.get("bankappcode");
            if ( bankappcode == null || bankappcode.equals("null") )
                bankappcode = "";
            mav.addObject("bankappcode", bankappcode.toString().replaceAll("\"", ""));
            mav.addObject("errorcode"           , validateMap.get("errorcode"));

            mav.addObject("cardexpirydate"      , validateMap.get("cardexpirydate"));
            mav.addObject("ccno"                , validateMap.get("ccno"));
            mav.addObject("ownername"           , validateMap.get("ownername"));
            mav.addObject("monthterm"           , validateMap.get("monthterm"));
            mav.addObject("type"                , validateMap.get("type"));
            mav.addObject("mid"                 , validateMap.get("mid"));
            mav.addObject("status"              , validateMap.get("status"));
            mav.addObject("providerrefno"       , validateMap.get("providerrefno")); // providerrefno
            mav.addObject("bankrefid"       , validateMap.get("bankrefid")); // bankrefid
            {
                Map<String, Object> logMap = new HashMap<String, Object>();
                logMap.put("PG_DL_ID"   , validateMap.get("partnerrefno"));
                logMap.put("PG_STATUS"  , validateMap.get("status"));
                logMap.put("MID"        , validateMap.get("mid"));
                if ( "120".equals(paramMap.get("payMnsCd")) ) {
                logMap.put("INFLX_PT"           , CHANNEL_3D);
                }else if( "130".equals(paramMap.get("payMnsCd")) ) {
                logMap.put("INFLX_PT"           , CHANNEL_CASA);
                }
                logMap.put("PARAMS"     , logParams);
                loggingApg("GetTransactionResult Success", logMap);
            }
        } else {
            {
                Map<String, Object> logMap = new HashMap<String, Object>();
                logMap.put("PG_DL_ID"       , validateMap.get("partnerrefno"));
                logMap.put("PG_STATUS"      , validateMap.get("status"));
                logMap.put("APV_ERR_CD"     , validateMap.get("errCd"));
                logMap.put("APV_ERR_MSG"    , validateMap.get("errMsg"));
                logMap.put("PARAMS"         , logParams);
                loggingApg("GetTransactionResult Fail", logMap);
            }
            mav.addObject("apgErrorMessage", validateMap.get("errMsg"));
        }
    }

    private void loggingApg(String stepNm, Map<String, Object> paramMap) {
        if ( paramMap.get("PG_DL_ID") == null ) {
            logger.debug("loggingApg PG_DL_ID is null. not logging end.");
            return;
        }
        paramMap.put("STEP_NM", stepNm);
        if ( stepNm == null ) {
        } else if ( stepNm.equals("CreateTransaction") ) {
            paramMap.put("CST_ID"       , paramMap.get("cstId"));
            paramMap.put("APV_AMT"      , StringUtils.remove((String)paramMap.get("totalamount"), ","));
            paramMap.put("PAY_MNS_CD"   , paramMap.get("payMnsCd"));
        }
        orderService.saveApgLog(paramMap);
        paramMap.remove("CST_ID");
        paramMap.remove("APV_AMT");
        paramMap.remove("PAY_MNS_CD");
        paramMap.remove("PARAMS");
    }

    private String getCreateTransactionMessage(HttpServletRequest request, Map<String, Object> paramMap,boolean isRepay) throws Exception {
        
        String URL_APG_RESPONSE = "";
        
        if(isRepay){
            URL_APG_RESPONSE = Globals.APG_REPAY_RESPONSEURL;
        }else{
            URL_APG_RESPONSE = Globals.APG_RESPONSEURL;
        }
            
        //body
        LinkedHashMap<String, Object> bodyMap = new LinkedHashMap<String, Object>();
        bodyMap.put("partnerkey"        ,Globals.APG_PARTNER_KEY);
        bodyMap.put("partnerrefno"      ,paramMap.get("PG_DL_ID"));
        bodyMap.put("transtype"         ,"s");
        bodyMap.put("paymenttype"       ,PAYMENT_TYPE);
        if ( "moto".equals(bodyMap.get("paymenttype")) ) {
            bodyMap.put("agentid"       ,Globals.IWEB_ADMIN_ID);
        }
        bodyMap.put("title"             ,"AGSS PG Payment");
        bodyMap.put("currencycode"      ,CURRENCYCODE);
        bodyMap.put("currencyexp"       ,CURRENCYEXP);
        bodyMap.put("shippingamount"    ,getAmount(paramMap.get("shippingamount")));
        bodyMap.put("taxamount"         ,getAmount(paramMap.get("taxamount")));
        List<LinkedHashMap<String, Object>> itemArr = new ArrayList<LinkedHashMap<String, Object>>();
        JSONArray itemsJsonArray = new JSONArray((String)paramMap.get("items"));
        for ( int i = 0 ; i < itemsJsonArray.length() ; i++ ) {
            JSONObject itemJson = itemsJsonArray.getJSONObject(i);
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("partneritemref", itemJson.get("partneritemref"));
            map.put("name"          , itemJson.get("name"));
            map.put("qty"           , itemJson.getInt("qty"));
            map.put("unitprice"     , getAmount(itemJson.getString("unitprice")));
            itemArr.add(map);
        }
        bodyMap.put("itemtotal"     , getAmount(paramMap.get("itemtotal")));
        bodyMap.put("totalamount"   , getAmount(paramMap.get("totalamount")));
        bodyMap.put("responseurl"   , getApgUrl(request, URL_APG_RESPONSE));
        bodyMap.put("items", itemArr);

        LinkedHashMap<String, Object> custdetail = new LinkedHashMap<String, Object>();
        custdetail.put("partneruserkey" , paramMap.get("cstId") );
        custdetail.put("email"          , paramMap.get("email"));
        custdetail.put("profile"        , null);

        bodyMap.put("custdetail"        , custdetail);
        bodyMap.put("extradata"         , new ArrayList<Object>());

        if ( "120".equals(paramMap.get("payMnsCd")) ) {
        bodyMap.put("channel"           , CHANNEL_3D);
        }else if( "130".equals(paramMap.get("payMnsCd")) ) {
        bodyMap.put("channel"           , CHANNEL_CASA);
        }

        bodyMap.put("recurringInfo"     , null);

        //eppinfo 
        String inttMms = (String)paramMap.get("inttMms");
        if ( !"".equals(inttMms) && !"N".equals(inttMms) && !"null".equals(inttMms) ) {
            /*LinkedHashMap<String, Object> eppinfo = new LinkedHashMap<String, Object>();
            eppinfo.put("monthterm"         , paramMap.get("inttMms"));*/
            bodyMap.put("transtype"         ,"e");
            bodyMap.put("eppinfo"           , null);
        } else {
            bodyMap.put("transtype"         ,"s");
        }

        //siginfo
        LinkedHashMap<String, Object> sigMap = new LinkedHashMap<String, Object>();
        Date now = new Date();
        sigMap.put("timestamp", getLocalToGmtTimestampStr(now));

        String sigStr = generateSig(bodyMap, CREATE_TRANSACTION_PARAMETER_ORDER);
        String encodedSig = encodeSHA256AndBASE64(getSigTimestampStr(now), sigStr);
        sigMap.put("sig", encodedSig);

        LinkedHashMap<String, Object> createTransactionMap = new LinkedHashMap<String, Object>();

        createTransactionMap.put("siginfo", sigMap);
        createTransactionMap.put("body", bodyMap);
        String createTransactionJsonStr = org.json.simple.JSONObject.toJSONString(createTransactionMap).replaceAll("\\\\", "");

        return createTransactionJsonStr;
    }

    private String callApgTransaction(String url, String requestJsonStr) throws Exception  {
        HttpHeaders headers = new HttpHeaders();
        if ( url.indexOf("astro.com.my") > -1 ) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        HttpEntity request = new HttpEntity(requestJsonStr, headers);
        String responseStr = "";

        try {
            RestTemplate restTemplate = new RestTemplate();
            //SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory)restTemplate.getRequestFactory();
            //rf.setReadTimeout(TIMEOUT);
            //rf.setConnectTimeout(TIMEOUT);
            responseStr = restTemplate.postForObject(url, request, String.class);
        } catch (RestClientException e){
            e.printStackTrace();
            logger.debug(e.getMessage());
            throw new Exception("APG connection error. Please retry later.");
        }

        return responseStr;
    }

    private String validateCreateTransactionResponse(String responseStr) throws Exception {

        JSONObject createTranscationRespJson = new JSONObject(responseStr);
        JSONObject createTransactionSiginfo  = createTranscationRespJson.getJSONObject("siginfo");
        JSONObject createTransactionBody     = createTranscationRespJson.getJSONObject("body");

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> bodyMap = objectMapper.readValue(createTransactionBody.toString(), LinkedHashMap.class);

        String timestampStr = createTransactionSiginfo.getString("timestamp");
        Date timestampDate = parseTimestamp(timestampStr);
        String beforeHash = generateSig(bodyMap);
        String encodedSigResp = encodeSHA256AndBASE64( getSigTimestampStr(timestampDate), beforeHash);

        logger.debug("validateCreateTransactionResponse createTransactionSiginfo.getString(sig) :: " + createTransactionSiginfo.getString("sig"));
        logger.debug("validateCreateTransactionResponse encodedSigResp :: " + encodedSigResp);
        
        String transactionid = "";
        if ( !createTransactionSiginfo.getString("sig").equals(encodedSigResp)) {
            throw new Exception("CreateTransaction Response has invalid Signature.");
        } else {
            transactionid = createTransactionBody.getString("transactionid");
            if ( StringUtils.isEmpty(transactionid) ) {
                throw new Exception(createTransactionSiginfo.getString("resultdescription"));
            }
        }
        return transactionid;
    }

    public String getGetTransactionResultMessage(HttpServletRequest request, Map<String, Object> paramMap) throws Exception {
        LinkedHashMap<String, Object> bodyMap = new LinkedHashMap<String, Object>();
        bodyMap.put("partnerkey"     ,Globals.APG_PARTNER_KEY);
        bodyMap.put("transactionid"  ,paramMap.get("transactionid"));
        bodyMap.put("maxpaymentcount",1);

        LinkedHashMap<String, Object> sigMap = new LinkedHashMap<String, Object>();

        Date now = new Date();
        sigMap.put("timestamp"   , getLocalToGmtTimestampStr(now));
        sigMap.put("SigTimeStamp", getSigTimestampStr(now));

        String sig = generateSig(bodyMap);
        String encodedSig = encodeSHA256AndBASE64((String)sigMap.get("SigTimeStamp"), sig);
        sigMap.put("sig", encodedSig);

        LinkedHashMap<String, Object> getTransactionResultMap = new LinkedHashMap<String, Object>();
        getTransactionResultMap.put("siginfo", sigMap);
        getTransactionResultMap.put("body"   , bodyMap);

        String getTransactionResultJsonStr = org.json.simple.JSONObject.toJSONString(getTransactionResultMap);

        return getTransactionResultJsonStr;
    }

    public Map<String, String> validateGetTransactionResultResponse(String getTransactionResultStr) {
        Map<String, String> validateMap = new HashMap<String, String>();
        try {
            JSONObject getTransactionResultRespJson = new JSONObject(getTransactionResultStr);
            JSONObject getTransactionResultSiginfo = getTransactionResultRespJson.getJSONObject("siginfo");
            JSONObject getTransactionResultBody = getTransactionResultRespJson.getJSONObject("body");

            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = objectMapper.readValue(getTransactionResultBody.toString(), Map.class);
            LinkedHashMap<String, Object> lhm = new LinkedHashMap<String, Object>(bodyMap);

            String genSig = generateSig(lhm, GET_TRANSACTION_RESULT_PARAMETER_ORDER);
            String gtrTimestamp = getTransactionResultSiginfo.getString("timestamp");
            String sigTimestampStr = getSigTimestampStr(parseTimestamp(gtrTimestamp));
            String encodedSigResp = encodeSHA256AndBASE64(sigTimestampStr, genSig);
            String getTransactionResultSig = getTransactionResultSiginfo.getString("sig");

            logger.error("validateGetTransactionResultResponse getTransactionResultSig :: " + getTransactionResultSiginfo.getString("sig"));
            logger.error("validateGetTransactionResultResponse encodedSigResp :: " + encodedSigResp);

            if ( !getTransactionResultSig.equals(encodedSigResp)) {
                validateMap.put("errCd", "ord.warning.062");
                validateMap.put("errMsg", "GetTransactionResult has invalid Signature.");
            }
            if ( getTransactionResultBody.has("paymentdetails") ) {
                JSONArray paymentdetailsArray = getTransactionResultBody.getJSONArray("paymentdetails");

                validateMap.put("partnerrefno", getTransactionResultBody.getString("partnerrefno"));
                if ( paymentdetailsArray.length() > 0 ) {
                    JSONObject paymentdetails = paymentdetailsArray.getJSONObject(0);

                    validateMap.put("providerkey", paymentdetails.getString("providerkey"));
                    String bankappcode = paymentdetails.getString("bankappcode");
                    if ( bankappcode == null || bankappcode.equals("null") )
                        bankappcode = "";
                    validateMap.put("bankappcode", bankappcode);
                    validateMap.put("errorcode", paymentdetails.getString("errorcode"));

                    //credit card(3D) only parsing
                    if( !"null".equals(paymentdetails.getString("ccdetail")) ){
                        JSONObject ccdetail = paymentdetails.getJSONObject("ccdetail");
                        validateMap.put("cardexpirydate", ccdetail.getString("cardexpiry"));
                        validateMap.put("ccno",           ccdetail.getString("ccno"));
                        validateMap.put("ownername",      ccdetail.getString("ownername"));
                        validateMap.put("type",           ccdetail.getString("type"));
                    }
                    //credit card(3D) EPP only parsing
                    if( !"null".equals(paymentdetails.getString("eppinfo")) ){
                        JSONObject eppinfo = paymentdetails.getJSONObject("eppinfo");
                        validateMap.put("monthterm", eppinfo.getString("monthterm"));
                        logger.error("EPP_monthterm " + eppinfo.getString("monthterm"));
                    }
                    
                    validateMap.put("mid",            paymentdetails.getString("mid"));
                    validateMap.put("status",         paymentdetails.getString("status"));
                    validateMap.put("providerrefno",  paymentdetails.getString("providerrefno")); // providerrefno
                    
                    //extrainfo
                    if( !"null".equals(paymentdetails.getString("extrainfo")) ){

                        JSONObject extrainfo = paymentdetails.getJSONObject("extrainfo");
                        validateMap.put("bankrefid",           extrainfo.getString("value"));
                    }
                    
                } else {
                    validateMap.put("errCd", "error.paymentdetails");
                    validateMap.put("errMsg", getTransactionResultSiginfo.getString("resultuimessage"));
                }

            } else {
                validateMap.put("errCd", getTransactionResultBody.getString("errorcode"));
                validateMap.put("errMsg", "getTransactionResult has not paymentdetails info.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            validateMap.put("errCd", "error.gtr.validate");
            validateMap.put("errMsg", "getTransactionResult fail " + e.getMessage());
        }

        return validateMap;
    }

    /**
     * return http or https by ivalue protocol.
     * @param request
     * @param url
     * @return
     */
    private String getApgUrl(HttpServletRequest request, String url) {
        if ( url.startsWith("http") ) {
            return url;
        } else {
            return request.getScheme() + "://" + url;
        }
    }

    /**
     * Signature will be hashed by using SHA 256 method and encoded with base 64
     * sigtimestamp + bankappcode + bankappdate + errorcode + partnerrefno + success + transactionid + partnerpassword
     * Date sigtimestampDate = org.apache.commons.lang.time.DateUtils.parseDate(sigtimestamp, parsePattern);
     * String beforeHash = sigTimeStampFormat.format(sigtimestampDate) + bankappcode + bankappdate + errorcode + partnerrefno + success + transactionid + Globals.APG_PARTNER_APPWD;
     * @param paramMap
     * @param keyArr
     * @return
     */
    private String makeResponseurlSigStr(Map<String, Object> paramMap, String [] keyArr) {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0 ; i < keyArr.length ; i++ ) {
            sb.append(paramMap.get(keyArr[i]));
        }
        return sb.toString();
    }

    /**
     * Generate sig by key name alphabetical order
     * @param map
     * @return
     * @throws JSONException
     */
    @SuppressWarnings({ "unchecked", "rawtypes"})
    private String generateSig(Map<String, Object> map) throws JSONException {
        StringBuffer sb = new StringBuffer();
        Iterator<String> iter = new TreeSet<String>(map.keySet()).iterator();
        while(iter.hasNext()){
            String key = iter.next();
            
            if(key.equals("extrainfo")){
                continue;
            }
            
            //String keyVal = key+"=";
            Object o = map.get(key);
            if ( o == null ) {
                //sb.append(key+"null");
                //sb.append("null");
            } else if ( o instanceof List) {
                List ol = (List)o;
                for ( int i = 0 ; i < ol.size() ; i++ ) {
                    sb.append( generateSig((Map<String,Object>)ol.get(i)) );
                }
            } else if ( o instanceof LinkedHashMap){
                sb.append( generateSig((Map)o) );
            } else {
                sb.append(o);
                //keyVal += String.valueOf(o);
                //sb.append(key).append("=").append(o).append(",");
            }
            //logger.debug(keyVal);
        }
        return sb.toString();
    }

    /**
     * Generate sig by keyOrder parameter
     * @param map
     * @param keyOrder
     * @return
     * @throws JSONException
     */
    @SuppressWarnings({ "unchecked", "rawtypes"})
    private String generateSig(Map<String, Object> map, String[] keyOrder) throws JSONException {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0 , len = keyOrder.length ; i < len ; i++ ) {
            Object o = map.get(keyOrder[i]);
            //String keyVal = keyOrder[i]+"=";
            if ( o == null ) {
                //logger.debug(keyOrder[i]+" = null");
                //sb.append("null");
            } else if ( o instanceof List) {
                List ol = (List)o;
                for ( int j = 0 ; j < ol.size() ; j++ ) {
                    sb.append( generateSig((Map<String,Object>)ol.get(j)) );
                }
            } else if ( o instanceof LinkedHashMap){
                sb.append( generateSig((Map)o) );
            } else {
                sb.append(o);
                //keyVal+= String.valueOf(o);
                //sb.append(key).append("=").append(o).append(",");
            }
            //logger.debug(keyVal);
        }
        return sb.toString();
    }

    /**
     * To make "sig" encode SHA256 and BASE64
     * @param sigTimeStamp
     * @param str
     * @return
     */
    public static String encodeSHA256AndBASE64(String sigTimeStamp, String str) {
        
        logger.error("beforehash :: " + str);
        logger.error("sigTimeStamp :: " + sigTimeStamp);
        
        
        if ( StringUtils.isEmpty(sigTimeStamp) ) {
            return "sigTimeStamp is empty";
        }
        String afterHash = "";
        try {
            String beforeHash = sigTimeStamp + str + Globals.APG_PARTNER_APPWD;
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(beforeHash.getBytes());
            byte byteData[] = sh.digest();
            byte[] encodedBytes = Base64.encodeBase64(byteData);
            afterHash = new String(encodedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return afterHash;
    }

    private BigDecimal getAmount(Object obj){
        try {
            BigDecimal bd = new BigDecimal( StringUtils.remove((String)obj, ","));
            return bd.movePointRight(CURRENCYEXP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }


    /**
     * local time to GMT. foramt is "yyyy-MM-dd'T'HH:mm:ss.SSS0000'Z'". for
     * @param now current time
     * @return GMT date "yyyy-MM-dd'T'HH:mm:ss.SSS0000'Z'" format.
     */
    public static String getLocalToGmtTimestampStr(Date now) {
        return gmtTimestamp.format(now) +"0000Z";
    }

    /**
     * local time to GMT. format is "yyyyMMddHHmmss".
     * to make hash sig.
     * @param now
     * @return
     */
    public static String getSigTimestampStr(Date now) {
        return gmtSigTimestamp.format(now);
    }

    /**
     * "yyyy-MM-dd'T'HH:mm:ss.SSS0000'Z'" to Date object
     *  2014-08-21T04:49:38.5360000Z
     * @param timestampStr
     * @return
     * @throws ParseException
     */
    public static Date parseTimestamp(String timestampStr) throws Exception {
        Date now = null;
        String parseDateStr = timestampStr.substring(0, timestampStr.lastIndexOf("Z")-4);
        now = parseFormat.parse(parseDateStr);
        return now;
    }
    
    /**
      * UTC time to local time
      * @param datetime
      */
    public static String utcToLocaltime(String datetime) throws BizRuntimeException, Exception {
        String locTime = null;
        TimeZone tz = TimeZone.getDefault();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); 
        
        try {
            Date parseDate = sdf.parse(datetime);
            long milliseconds = parseDate.getTime();
            int offset = tz.getOffset(milliseconds);
            locTime = sdf.format(milliseconds + offset);
            locTime = locTime.replace("+0000", "");
        } catch(Exception e) {
            e.printStackTrace();
            logger.error(e.getLocalizedMessage());
        }
        
        return locTime;
    }
    
    /**
     * APG realtime payment Popup
     * 1. call CreateTransaction
     * 2. if CT is success then call apgEpayment
     *    else print error
     * 3. receive returnurl from apg
     * @throws JSONException
     */
    @RequestMapping(value="ordAPGRePaymentPop")
    public ModelAndView ordAPGRePaymentPop(HttpServletRequest request, @RequestParam Map<String, Object> paramMap, HttpSession session) throws JSONException {
        ModelAndView mav = new ModelAndView();
        RedirectView rv  = new RedirectView();
        
        String URL_CREATE_TRANSACTION = getApgUrl(request, Globals.APG_CREATE_TRANSACTION_URL);//;
        String URL_APG_EPAYMENT = getApgUrl(request, Globals.APG_EPAYMENT_URL);

        String createTransactionRequestJsonStr = "";
        String createTransactionResponseJsonStr = "";
        String logParams = "";
        
        //REMOVE ORDERVO INFO.
        session.removeAttribute("OrderVO");
        
        //ADD ORDERVO (PRODUCTS INFO & ORDER INFO & GTAGDATA)
        OrderVO orderVo = new OrderVO();
        orderVo.setOrderInfo((String)paramMap.get("ORDERINFO"));
        orderVo.setOrdInfo((String)paramMap.get("ORDINFO"));
        orderVo.setPrdDtlInfo((String)paramMap.get("PRDDTLINFO"));
        orderVo.setGtagData((String)paramMap.get("gTagData"));
        orderVo.setProductArray((String)paramMap.get("productArray"));
        
        //ADD SESSION ORDERVO
        session.setAttribute("OrderVO", orderVo);
        
        logger.debug("SessionUtils.getOrderVO() {}", SessionUtils.getOrderVO());
        
        //local Test dummy
        /*rv.setUrl("ordAPGBankTestForm.do");
        rv.setExposeModelAttributes(false);
        mav.setView(rv);*/
        
        try {
            String pgDlId = orderService.findPgDlIdNew();
            paramMap.put("PG_DL_ID", pgDlId);//PK of log table
            
            //1. create message for transactionid
            createTransactionRequestJsonStr = getCreateTransactionMessage(request, paramMap, true);

            //1-1 logging create message for transactionid
            logParams = createTransactionRequestJsonStr;
            loggingApg("CreateTransaction", paramMap);

            //2. call CreateTransaction
            createTransactionResponseJsonStr = callApgTransaction(URL_CREATE_TRANSACTION, createTransactionRequestJsonStr);
            //2-1 vaidate CreateTransaction response
            String transactionid = validateCreateTransactionResponse(createTransactionResponseJsonStr);
            {
                Map<String, Object> logMap = new HashMap<String, Object>();
                logMap.put("PG_DL_ID", paramMap.get("PG_DL_ID"));
                logMap.put("PG_TRN_ID", transactionid);
                loggingApg("RedirectToAPG", logMap);
            }

            String redirectToApgUrl = URL_APG_EPAYMENT + transactionid;
            mav.addObject("redirectUrl", redirectToApgUrl);
            mav.addObject("transactionid", transactionid);
            mav.addObject("pgProgressId", paramMap.get("pgProgressId"));
            mav.addObject("PG_DL_ID", pgDlId);
            
            //APG Redirect Set!! ( i-web redirect )
            rv.setUrl(redirectToApgUrl);
            rv.setExposeModelAttributes(false);
            mav.setView(rv);
        } catch (Exception e){
            e.printStackTrace();
            /*JSONObject createTranscationRespJson = new JSONObject(createTransactionResponseJsonStr);
            JSONObject createTransactionBody  = createTranscationRespJson.getJSONObject("body");
            
            paramMap.put("APV_ERR_CD", createTransactionBody.get("errorcode"));*/
            paramMap.put("APV_ERR_MSG", e.getMessage());
            loggingApg("CreateTransaction Fail", paramMap);
            mav.addObject("apgErrorMessage", e.getMessage());
        } finally {

            StringBuffer ctJsonLogInclItem = new StringBuffer(URL_CREATE_TRANSACTION);
            StringBuffer ctJsonLog = new StringBuffer(URL_CREATE_TRANSACTION);
            ctJsonLog.append("\n Request = ");
            ctJsonLogInclItem.append("\n Request = ");
            try {
                int itemsIndex = createTransactionRequestJsonStr.indexOf("\"items\"")-1;
                int itemsIndexEnd = createTransactionRequestJsonStr.indexOf("}],\"", itemsIndex)+1;
                String beforeItems = createTransactionRequestJsonStr.substring(0, itemsIndex + 10);//10 : "\"items\": [".length()
                String afterItems = createTransactionRequestJsonStr.substring(itemsIndexEnd);
                ctJsonLog.append(new JSONObject(beforeItems + afterItems).toString(1));
                ctJsonLogInclItem.append(new JSONObject(createTransactionRequestJsonStr).toString(1));
            } catch(Exception e){
                e.printStackTrace();
                ctJsonLog.append(createTransactionRequestJsonStr);
            }
            ctJsonLog.append("\n Response = ");
            ctJsonLogInclItem.append("\n Response = ");
            try {
                if ( StringUtils.isNotEmpty(createTransactionResponseJsonStr) ) {
                    ctJsonLog.append(new JSONObject(createTransactionResponseJsonStr).toString(1));
                    ctJsonLogInclItem.append(new JSONObject(createTransactionResponseJsonStr).toString(1));
                }
            } catch(Exception e){
                e.printStackTrace();
                ctJsonLog.append(createTransactionResponseJsonStr);
            }
            logParams = ctJsonLog.toString();
            logger.debug(ctJsonLogInclItem.toString());
            {
                Map<String, Object> logMap = new HashMap<String, Object>();
                logMap.put("PG_DL_ID", paramMap.get("PG_DL_ID"));
                logMap.put("PARAMS", logParams);
                loggingApg(null, logMap);
            }
        }

        return mav;
    }
    
    /**
     * APG에서 결제완료후에 호출해주는 responseurl
     * @param paramMap
     */
    @RequestMapping(value="ordAPGRepayResult")
    public ModelAndView ordAPGRepayResult(HttpServletRequest request, @RequestParam Map<String, Object> paramMap){
        ModelAndView mav = new ModelAndView();
        RedirectView rv = new RedirectView();
        
        String pgDlIds = (String)paramMap.get("partnerrefno");
        
        //MC APG Payment success before back button(user) control. MR.JP - 2015.02.04
        if( !"".equals(pgDlIds) ){
            String orderInfoCnt = orderService.findOrderStatusInfo(pgDlIds);
            
            if( NumberUtils.toDouble((String)orderInfoCnt) <= 0 ){
                rv.setUrl("/index.do");
                rv.setExposeModelAttributes(false);
                mav.setView(rv);
                return mav;
            }
        }
        
        ordAPGResultInner(mav, request, paramMap);
        
        mav.addObject("APG_PENDING_TIME_OUT", Globals.APG_PENDING_TIME_OUT);
        
        return mav;
    }
}