/**
 * @author lee.jongpil
 *
 */
package kr.pe.spm.common;

public class Globals {
    
    public static final String TMP_USER_ID = "tmpUserId";
    public static final String TMP_CST_ID = "20140808000823";//"20140812000844";        //임시 고객ID //20130101000224 고객주소있음  , 없음 20140703000783 , 20140807000806(영문주소)

    public static final String MONTH_FORMAT = "yyyy-MM";
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String MAPPING_JACKSON_JSON_VIEW = "mappingJacksonJsonView";
    public static final String JSON_RESULT_CODE= "code";
    public static final String JSON_RESULT_MESSAGE= "message";
    public static final String JSON_ERROR_ROOT_ELEMENT_VALUE = "error";
    public static final String JSON_ERROR_VALIDATION_ROOT_ELEMENT_VALUE = "validationError";
    public static final String JSON_ERROR_VALIDATION_FIELD = "field";

    public static final String RESULT_CODE = "RESULT_CODE";
    public static final String RESULT_ARGUMENTS = "RESULT_ARGUMENTS";
    public static final String DEFAULT_SUCCESS_CODE = "0000";
    
    public static final String PROTOCOL_HTTP = "HTTP";    //http 프로토콜
    public static final String PROTOCOL_HTTPS = "HTTPS";  //https프로토콜
    
    public static final String DOMAIN = "localhost"; //JProperties.getString("goshop.domain"); //도메인주소
    
    
}