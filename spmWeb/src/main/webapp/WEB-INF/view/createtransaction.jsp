<%@ page language="java" contentType="text/html; charset=EUC-KR"    pageEncoding="EUC-KR"%>
<%@page import="com.gsshop.common.util.Util"%>
<%@page import="com.gsshop.biz.ord.service.OrderService"%>
<%@page import="org.springframework.beans.factory.annotation.Autowired"%>
<%@page import="org.springframework.web.client.RestClientException"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.apache.commons.collections.MapUtils"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="org.springframework.http.MediaType"%>
<%@page import="java.security.NoSuchAlgorithmException"%>
<%@page import="org.apache.commons.codec.binary.Base64"%>
<%@page import="java.security.MessageDigest"%>
<%@page import="com.gsshop.common.Globals"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@page import="java.text.ParseException"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.io.UnsupportedEncodingException"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.TreeSet"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="java.util.SimpleTimeZone"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.TimeZone"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="java.util.Date"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.springframework.http.client.SimpleClientHttpRequestFactory"%>
<%@page import="org.springframework.web.client.RestTemplate"%>
<%@page import="org.springframework.http.HttpEntity"%>
<%@page import="org.springframework.http.HttpHeaders"%>
<%@page import="org.apache.commons.lang.time.FastDateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="org.json.JSONArray"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.LinkedHashMap"%>

<html>
    <head>
        <title>APG Test</title>
    </head>
    <style>
        * {
            font-family: dotum, µ¸¿ò,  verdana, tahoma, arial, sans-serif;
            font-size: 12px;
        }
    </style>
    <body>
<%
String myScheme = request.getScheme();
String myServer = request.getServerName();
int myPort = request.getServerPort();
String myResponseUrl = myScheme + "://" + myServer + ":" + myPort + "/biz/ord/apg/ordAPGResult.do";

String useThisUrl = request.getParameter("useThisUrl");
String responseUrl = request.getParameter("responseUrl");//https://dev-ivalue.go-shop.com.my:444/biz/ord/apg/ordAPGResult.do
String URL_APG_RESPONSE ="";
if ( useThisUrl != null && useThisUrl.equals("Y")) {
	URL_APG_RESPONSE = responseUrl;
} else {
	URL_APG_RESPONSE = myResponseUrl;
}

Map<String, Object> success = new HashMap<String, Object>();
Map<String, String> validMap = null;
StringBuffer allSb = new StringBuffer();

if ( "post".equalsIgnoreCase( request.getMethod() ) ) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        TreeMap<String, String[]> treeMap = new TreeMap<String, String[]>(request.getParameterMap());
        for (String key : treeMap.keySet() ) {
            String [] val = treeMap.get(key);
            if ( val.length == 1 ) {
            	paramMap.put(key, val[0]);
            } else {
                paramMap.put(key, val);
            }
        }
        String scheme = request.getParameter("scheme");//https://dev-ivalue.go-shop.com.my:444/biz/ord/ordAPGResult.do";
        if ( scheme == null )
        	scheme = "https";
        
        String URL_CREATE_TRANSACTION     = scheme + "://apgdev.astro.com.my/api/apg/CreateTransaction";
        String URL_APG_EPAYMENT           = scheme + "://apgdev.astro.com.my/epayment/entry.aspx?refid=";
        String URL_GET_TRANSACTION_RESULT = scheme + "://apgdev.astro.com.my/api/apg/GetTransactionResult";
        
        String redirectToApgUrl = "";
        
        
        String [] responseUrlList = new String [] {
               "http://dev-ivalue.go-shop.com.my:444/biz/ord/order/ordAPGResult.do",
               "http://test-ivalue.go-shop.com.my/biz/ord/order/ordAPGResult.do",
               "http://ivalue.goshop.com.my/biz/ord/order/ordAPGResult.do",
               "https://dev-ivalue.go-shop.com.my:444/biz/ord/order/ordAPGResult.do",
               "https://test-ivalue.go-shop.com.my/biz/ord/order/ordAPGResult.do",
               "https://ivalue.goshop.com.my/biz/ord/order/ordAPGResult.do",
               "http://dev.go-shop.com.my:81",
               "http://test.go-shop.com.my",
               "http://www.goshop.com.my",
               "https://dev.go-shop.com.my:81",
               "https://test.go-shop.com.my",
               "https://www.goshop.com.my",
               "http://dev-m.go-shop.com.my:81",
               "http://test-m.go-shop.com.my",
               "http://m.goshop.com.my",
               "https://dev-m.go-shop.com.my:81",
               "https://test-m.go-shop.com.my",
               "https://m.goshop.com.my"
        };
	       
        String createTransactionRequestJsonStr = "";
        String createTransactionResponseJsonStr = "";
        String logParams = "", errorStack = "";
        String allDomainYn = request.getParameter("allDomainYn");
        
        try {
            String pgDlId = "ivalue" + System.currentTimeMillis();//orderService.findPgDlIdNew();
            paramMap.put("PG_DL_ID", pgDlId);//PK of log table
            paramMap.put("partnerrefno", pgDlId);//as partnerrefno

	        /***  CreateTransaction  ***/
	        //body
			LinkedHashMap<String, Object> bodyMap = new LinkedHashMap<String, Object>();
			bodyMap.put("partnerkey"        ,"hso");
			//bodyMap.put("partnerrefno"      ,paramMap.get("PG_DL_ID"));
			bodyMap.put("transtype"         ,"s");
			bodyMap.put("paymenttype"       ,"moto");
			if ( "moto".equals(bodyMap.get("paymenttype")) ) {
			    bodyMap.put("agentid"           ,"agentno1");
			}
			bodyMap.put("title"             ,"AGSS PG Payment");
			bodyMap.put("currencycode"      ,"MYR");
			bodyMap.put("currencyexp"       ,"2");
			bodyMap.put("shippingamount"    ,"0");
			bodyMap.put("taxamount"         ,"0");
	
			
			List<LinkedHashMap<String, Object>> itemArr = new ArrayList<LinkedHashMap<String, Object>>();
		    LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		    map.put("partneritemref", "0001");
		    map.put("name", "apg sample product");
		    map.put("qty", "1");
		    map.put("unitprice","3000");
		    itemArr.add(map);
	
		    
		    bodyMap.put("itemtotal", "3000");
			bodyMap.put("totalamount","3000");
			bodyMap.put("responseurl", URL_APG_RESPONSE);
			
			bodyMap.put("items", itemArr);
			
			LinkedHashMap<String, Object> custdetail = new LinkedHashMap<String, Object>();
			custdetail.put("partneruserkey", "123456");
			custdetail.put("email", "ivalue@goshop.com.my");
			custdetail.put("profile", null);
			
			bodyMap.put("custdetail", custdetail);
			bodyMap.put("extradata", new ArrayList<Object>());
			bodyMap.put("channel","cc");
			bodyMap.put("recurringInfo", null);
			
		    String inttMms = (String)paramMap.get("inttMms");
	        if ( inttMms != null &&  !inttMms.equals("0") ) {
	            LinkedHashMap<String, Object> eppinfo = new LinkedHashMap<String, Object>();
	            eppinfo.put("monthterm"         , paramMap.get("inttMms"));
	            bodyMap.put("eppinfo"           , eppinfo);
	        }
			//LinkedHashMap<String, Object> eppinfo = new LinkedHashMap<String, Object>();
			//eppinfo.put("monthterm", "0");
			//bodyMap.put("eppinfo", eppinfo);
			
			
			if ( allDomainYn != null && allDomainYn.equals("Y") ) {
				long pno = System.currentTimeMillis();
				for ( int i = 0 ; i < responseUrlList.length ; i++ ) {
		            bodyMap.put("partnerrefno"      ,pno+i);
					bodyMap.put("responseurl", responseUrlList[i]);
					allSb.append( (i+1) + " : ").append(responseUrlList[i]).append("\n");
					
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
					createTransactionRequestJsonStr = org.json.simple.JSONObject.toJSONString(createTransactionMap).replaceAll("\\\\", "");
			        //2. call CreateTransaction
			        createTransactionResponseJsonStr = callApgTransaction(getApgUrl(request, URL_CREATE_TRANSACTION), createTransactionRequestJsonStr);
			        allSb.append(new JSONObject(createTransactionResponseJsonStr).toString(1)).append("\n");
				}	        
			} else {
                bodyMap.put("partnerrefno",System.currentTimeMillis());
                
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
                createTransactionRequestJsonStr = org.json.simple.JSONObject.toJSONString(createTransactionMap).replaceAll("\\\\", "");
                //2. call CreateTransaction
                createTransactionResponseJsonStr = callApgTransaction(getApgUrl(request, URL_CREATE_TRANSACTION), createTransactionRequestJsonStr);

                //2-1 vaidate CreateTransaction response
		        validMap = validateCreateTransactionResponse(createTransactionResponseJsonStr);
		        String transactionid = validMap.get("transactionid");
		        if ( StringUtils.isNotEmpty(transactionid)  ) {
		
		            paramMap.put("APG_TRN_ID", transactionid);
		            paramMap.put("transactionid", transactionid);
		            //loggingApg("RedirectAPG", paramMap);
		
		            redirectToApgUrl = getApgUrl(request, URL_APG_EPAYMENT) + transactionid;
		            //mav.addObject("redirectUrl", redirectToApgUrl);
		            //mav.addObject("transactionid", transactionid);
		            //mav.addObject("pgProgressId", paramMap.get("pgProgressId"));
		
		            success.putAll(paramMap); 
		
		        } else {
		            paramMap.put("APV_ERR_CD", validMap.get("errCd"));
		            //throw new Exception(validMap.get("errMsg"));
		        }
			}
			
	    } catch (Exception e){
	        e.printStackTrace();
	        StringBuffer sb = new StringBuffer(e.getMessage()+"\n");
	        StackTraceElement [] ste = e.getStackTrace();
	        for ( int i = 0 ; i < ste.length ; i++ ) {
	        	   sb.append(ste[i]).append("\n");	
	        }
	        errorStack = sb.toString();
	        paramMap.put("APV_ERR_MSG", e.getMessage());
	    } finally {
	    	if ( allDomainYn == null || allDomainYn.equals("N") ) {
	    		
	            StringBuffer ctJsonLog = new StringBuffer(URL_CREATE_TRANSACTION);
	            ctJsonLog.append("\n Request = ");
	            try {
	                int itemsIndex = createTransactionRequestJsonStr.indexOf("\"items\"")-1;
	                int itemsIndexEnd = createTransactionRequestJsonStr.indexOf("}],\"", itemsIndex)+1;
	                String beforeItems = createTransactionRequestJsonStr.substring(0, itemsIndex + 10);//10 : "\"items\": [".length()
	                String afterItems = createTransactionRequestJsonStr.substring(itemsIndexEnd);
	                ctJsonLog.append(new JSONObject(beforeItems + afterItems).toString(1));
	            } catch(Exception e){
	                e.printStackTrace();
	                ctJsonLog.append(createTransactionRequestJsonStr);
	            }
	            ctJsonLog.append("\n Response = ");
	            try {
	                if ( StringUtils.isNotEmpty(createTransactionResponseJsonStr) ) {
	                    ctJsonLog.append(new JSONObject(createTransactionResponseJsonStr).toString(1));
	                }
	            } catch(Exception e){
	                e.printStackTrace();
	                ctJsonLog.append(createTransactionResponseJsonStr);
	            }
	            logParams = ctJsonLog.toString();
	        }
	    }
%>
    CreateTransaction
    <%= success.toString() %>
<iframe src="<%=  redirectToApgUrl%>" width="850px" height="800px"></iframe>
    <input type="button" value="Retry" onclick="history.back();">
    <pre>
    <%= allSb.toString() %>
    </pre>
    <hr>
    <pre><%= errorStack %></pre>
    <hr>
    <pre><%= logParams %></pre>
    <pre><%= validMap != null ? validMap.toString() : validMap %></pre>
<%
} else {
%>
<form method="post">
    <table>
        <tr>
            <td>scheme</td>
            <td>
                <select name="scheme">
                    <option value="http">http</option>
                    <option value="https">https</option>
                </select>
            </td>
            <td>inttMms</td>
            <td>
                <input type="text" name="inttMms" value="0">
            </td>
            <td>responseUrl</td>
            <td>
                use this url : <input type="checkbox" name="useThisUrl" value="Y">
                <input type="text" name="responseUrl" value="<%= myResponseUrl %>" style="width:300px"><br>
                https://dev-ivalue.go-shop.com.my:444/biz/ord/order/ordAPGResult.do<br>
                https://ivalue.goshop.com.my/biz/ord/apg/ordAPGResult.do
            </td>
            <td>allDomainYn</td>
            <td>
                <select name="allDomainYn">
                    <option value="N">No</option>
                    <option value="Y">Yes</option>
                </select>
            </td>
            <td><input type="submit"></td>
        </tr>
    </table>
</form>

<%
}
%>
    </body>


<%!
    private String callApgTransaction(String url, String requestJsonStr) throws Exception  {
        System.out.println("Call URL : " + url);
        HttpHeaders headers = new HttpHeaders();
        if ( url.indexOf("astro.com.my") > -1 ) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
    
        @SuppressWarnings({ "rawtypes", "unchecked" })
        HttpEntity request = new HttpEntity(requestJsonStr, headers);
        String responseStr = "";
    
        try {
            RestTemplate restTemplate = new RestTemplate();
            SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory)restTemplate.getRequestFactory();
            rf.setReadTimeout(TIMEOUT);
            rf.setConnectTimeout(TIMEOUT);
            responseStr = restTemplate.postForObject(url, request, String.class);
        } catch (Exception e ) {
        	throw e;
        }
        /* } catch (RestClientException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw new Exception("APG connection error. Please retry later.");
        } finally {
            System.out.println("{{--------------------------------------------");
            System.out.println("URL : " + url);
            System.out.println(url + " Request = \n"+new JSONObject(requestJsonStr).toString(4));
            if ( StringUtils.isNotEmpty(responseStr) ) {
                System.out.println(url + " Response = \n"+new JSONObject(responseStr).toString(4));
            }
            System.out.println("--------------------------------------------}}");
        } */
    
        return responseStr;
    }
    
    private Map<String, String> validateCreateTransactionResponse(String responseStr) throws Exception {
        Map<String, String> validateMap = new HashMap<String, String>();
    
        JSONObject createTranscationRespJson = new JSONObject(responseStr);
        JSONObject createTransactionSiginfo  = createTranscationRespJson.getJSONObject("siginfo");
        JSONObject createTransactionBody     = createTranscationRespJson.getJSONObject("body");
    
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> bodyMap = objectMapper.readValue(createTransactionBody.toString(), LinkedHashMap.class);
    
        String timestampStr = createTransactionSiginfo.getString("timestamp");
        Date timestampDate = parseTimestamp(timestampStr);
        String beforeHash = generateSig(bodyMap);
        String encodedSigResp = encodeSHA256AndBASE64( getSigTimestampStr(timestampDate), beforeHash);
    
        validateMap.put("encodedSigResp", encodedSigResp);
        if ( !createTransactionSiginfo.getString("sig").equals(encodedSigResp)) {
            validateMap.put("errCd", "ord.warning.062");
            validateMap.put("errMsg", "Invalid Signature.(CreateTransaction)");
        } else {
            String transactionid = createTransactionBody.getString("transactionid");
            if ( !StringUtils.isEmpty(transactionid) ) {
                validateMap.put("transactionid", transactionid);
            } else {
                validateMap.put("errCd", "ord.warning.062");
                validateMap.put("errMsg", createTransactionSiginfo.getString("resultdescription")+"(CreateTransaction)");
            }
        }
        System.out.println("validateMap : " + validateMap.toString());
        return validateMap;
    }

       private Map<String, Object> setDummy(Map<String, Object> paramMap) throws JSONException {
            Date now = new Date();
            Map<String, Object> success = new HashMap<String, Object>();
            success.put("result", "true");
            success.put("resultdescription", "success");
            //sig before hash
            success.put("bankappcode", "A"+Util.getCurrentTime());
            success.put("bankappdate", getLocalToGmtTimestampStr(now));
            success.put("errorcode", "0000");
            success.put("partnerrefno", paramMap.get("partnerrefno"));
            success.put("providerkey", "mbb");
            success.put("result", "true");
            success.put("transactionid", paramMap.get("transactionid"));
            success.put("sigtimestamp", getSigTimestampStr(now));
            //
            String sigMsg = makeResponseurlSigStr(success, new String[] {"bankappcode", "bankappdate", "errorcode", "partnerrefno", "providerkey", "result", "transactionid"} );
            String successSig = encodeSHA256AndBASE64((String)success.get("sigtimestamp"), sigMsg);

            success.put("sig", successSig);
            success.put("sigMsg", sigMsg);
            return success;
        }

    
    @Autowired
    private OrderService orderService;

       
    private  Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String[] timestampPattern = {"yyyy-MM-dd'T'HH:mm:ss.SSS"};
    private final FastDateFormat gmtTimestamp = FastDateFormat.getInstance(timestampPattern[0], TimeZone.getTimeZone("GMT"), Locale.getDefault());
    private final FastDateFormat gmtSigTimestamp = FastDateFormat.getInstance("yyyyMMddHHmmss", TimeZone.getTimeZone("GMT"), Locale.getDefault());
    private final SimpleDateFormat parseFormat = new SimpleDateFormat(timestampPattern[0]);
    {
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
    private String PAYMENT_TYPE = "moto";
    private String CHANNEL = "cc";
    private String partnerkey = "hs02014!";

    private ObjectMapper objectMapper = new ObjectMapper();

    private int TIMEOUT = 10000;

    private BigDecimal getAmount(Object obj){
        try {
            BigDecimal bd = new BigDecimal( StringUtils.remove((String)obj, ","));
            return bd.movePointRight(2);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }


    /**
     * Date to "yyyy-MM-dd'T'HH:mm:ss.SSS0000'Z'"
     * @param now
     * @return
     */
    public String getLocalToGmtTimestampStr(Date now) {
        return gmtTimestamp.format(now) +"0000Z";
    }

    /**
     * Date to "yyyyMMddHHmmss"
     * @param now
     * @return
     */
    public String getSigTimestampStr(Date now) {
        return gmtSigTimestamp.format(now);
    }

    /**
     * "yyyy-MM-dd'T'HH:mm:ss.SSS0000'Z'" to Date
     * 2014-08-21T04:49:38.5360000Z
     * @param timestampStr
     * @return
     * @throws ParseException
     */
    public Date parseTimestamp(String timestampStr) {
        Date now = null;
        String parseDateStr = timestampStr.substring(0, timestampStr.lastIndexOf("Z")-4);
        try {
            now = parseFormat.parse(parseDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return now;
    }

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
     * String beforeHash = sigTimeStampFormat.format(sigtimestampDate) + bankappcode + bankappdate + errorcode + partnerrefno + success + transactionid + Globals.APG_PARTNER_PASSWORD;
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

    @SuppressWarnings({ "unchecked", "rawtypes"})
    private String generateSig(Map<String, Object> map) throws JSONException {
        StringBuffer sb = new StringBuffer();
        Iterator<String> iter = new TreeSet<String>(map.keySet()).iterator();
        while(iter.hasNext()){
            String key = iter.next();
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
                //sb.append(key).append("=").append(o).append(",");
            }
        }
        return sb.toString();
    }

    @SuppressWarnings({ "unchecked", "rawtypes"})
    private String generateSig(Map<String, Object> map, String[] keyOrder) throws JSONException {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0 , len = keyOrder.length ; i < len ; i++ ) {
            Object o = map.get(keyOrder[i]);
            if ( o == null ) {
                //System.out.println(keyOrder[i]+" = null");
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
                //sb.append(key).append("=").append(o).append(",");
            }
        }
        return sb.toString();
    }

    public String encodeSHA256AndBASE64(String sigTimeStamp, String str) {
        if ( StringUtils.isEmpty(sigTimeStamp) ) {
            return "sigTimeStamp is empty";
        }
        String beforeHash = sigTimeStamp + str + "hs02014!";
        System.out.println("beforeHash : " + beforeHash);
        String afterHash = "";
        try {
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(beforeHash.getBytes());
            byte byteData[] = sh.digest();
            byte[] encodedBytes = Base64.encodeBase64(byteData);
            afterHash = new String(encodedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println("afterHash : " + afterHash);
        return afterHash;
    }

%>