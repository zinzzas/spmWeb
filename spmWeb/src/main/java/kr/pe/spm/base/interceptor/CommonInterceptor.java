package kr.pe.spm.base.interceptor;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import kr.pe.spm.common.Globals;

public class CommonInterceptor implements HandlerInterceptor{
    
    private final Logger log = LoggerFactory.getLogger(CommonInterceptor.class);
    
    //TODO VIEW 영역에서 사용할 파라미터 설정.
    private static final String HTTP_DOMAIN                 = "httpDomain";
    private static final String HTTPS_DOMAIN                = "httpsDomain";
    
    private static final String SET_HTTP_DOMAIN = Globals.PROTOCOL_HTTP + Globals.DOMAIN;
    private static final String SET_HTTPS_DOMAIN = Globals.PROTOCOL_HTTPS + Globals.DOMAIN;
    
    
    
    @Override
    public boolean preHandle(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, Object paramObject) throws Exception{
        
        paramHttpServletRequest.setAttribute(HTTP_DOMAIN, SET_HTTP_DOMAIN);   
        paramHttpServletRequest.setAttribute(HTTPS_DOMAIN, SET_HTTPS_DOMAIN);
        
        log.debug("DE {}", "DDDDDDDD");
        
        //log load
        logRequest(paramHttpServletRequest);
        
        return true;
    };
    
    @Override
    public void postHandle(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, Object paramObject, ModelAndView paramModelAndView) throws Exception{
        
    };
          
    @Override
    public void afterCompletion(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, Object paramObject, Exception paramException) throws Exception{
        
    };
    
    private void logRequest(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder("\n");
        if (log.isDebugEnabled()) {
            sb.append("//=========================================================================\n");
            sb.append("Request URI : " + request.getRequestURI()).append("\n");
            //sb.append("Referer URI : " + request.getHeader("referer")).append("\n");  
            //sb.append("IP : " + request.getRemoteAddr()).append("\n");
            sb.append("Method : " + request.getMethod()).append("\n");
            //sb.append("User Agent : " + request.getHeader("User-Agent")).append("\n");
            //sb.append("Session : " + request.getSession().getId()).append("\n");
            //TODO system.xml의 설정으로 로케일 SET
            sb.append("Locale : " + request.getLocale().getCountry()).append("\n");
            sb.append("Language : " + request.getLocale().getLanguage()).append("\n");
            
            
            Enumeration<?> e = request.getParameterNames();
            
            if(e.hasMoreElements()){
                sb.append("---------------- Parameter -----------------\n");
                String pName = "";
                String pValue = "";
                do {
                    pName = (String)e.nextElement();
                    pValue = request.getParameter(pName);                    
                    sb.append(pName + " : [" + pValue + "]\n");
                } while(e.hasMoreElements());
            }else{
                sb.append("Parameter : null \n");
                
            }
            sb.append("=========================================================================//\n");
            log.debug(sb.toString());
        }
    }
}
