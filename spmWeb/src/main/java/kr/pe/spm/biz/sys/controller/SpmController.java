/**
 * @author lee.jongpil
 *
 */
package kr.pe.spm.biz.sys.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import kr.pe.spm.common.util.ParameterMap;

@Controller
public class SpmController {

    private Logger logger = LoggerFactory.getLogger(SpmController.class);
    
    @RequestMapping("/spm")
    public ModelAndView spm(Map<String, Object> modelMap){
        ModelAndView mav = new ModelAndView();
        
        logger.debug("spm start!!! {}", "ok");
        
        modelMap.put("spm", "spm start!!");
        modelMap.put("message", "spm start!!");;
        
        mav.addAllObjects(modelMap);
        
        return mav;
    }
    
    @RequestMapping("/apgPop")
    public ModelAndView apgPop(ParameterMap parameterMap, Map<String, Object> modelMap){
        ModelAndView mav = new ModelAndView();
        
        logger.debug("parameterMap {}", parameterMap);
        
        modelMap.put("message", "apgPop !!");;
        
        mav.addAllObjects(modelMap);
        
        return mav;
    }
}