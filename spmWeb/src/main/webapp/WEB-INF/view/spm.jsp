<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html>
<html lang="ko">
	<head>
		<meta charset="utf-8">
		<title>spm proj</title>
        <spring:url value="/resources/script/base/jquery-1.9.1.min.js" var="jqueryJs"></spring:url>
        <script type="text/javascript" src="/resources/script/base/jquery-1.9.1.min.js"></script>
    </head> 
	<body>

<script type="text/javascript">
$(document).ready(function(){
    $("#btnRePay").hide();
        
    $("#btnPay, #btnRePay").on("click", function(){
        $("#btnPay, #btnRePay").hide();
        
        var apgInfo = {
			 cstId : $("#cstId").val()
			,cstNm : $("#cstNm").val()
		}
        
        fnApgPaymentPop(apgInfo);
    });
});

fnApgPaymentPop = function(apgInfo){
    var payPop = window.fnCallApgPayPop(apgInfo);
    checkPopup = window.setInterval(function() {
    	console.log(apgInfo);
        if (payPop.closed) {
    		window.clearInterval(checkPopup);
    		$("#btnRePay").show();
    	}
    }, 200);
}

fnCallApgPayPop = function(param){
	return window.open("/apgPop", "width=400, height=300, left=100, location=1", "apg", param);
}


getDate = function(nDate, tDate){
    var arrNdate = nDate.split("-");
    var arrTdate = tDate.split("-");
    
    if(arrNdate.length != 3 || arrTdate.length != 3){ 
        return -1;
    }
    
    
    var tDate = new Date(arrTdate[0], arrTdate[1], arrTdate[2]);
    var nDateObj = new Date(arrNdate[0], arrNdate[1], arrNdate[2]);  
    var nDateObj2 = new Date("2017","01","30");  
    
    var day = 1000*60*60*24;  
    var month = day*30;
    var year = month*12;
    
    console.log("getDate nDate >> " + nDate +"|"+ nDateObj.getTime());
    console.log("getDate nDate >> " + nDate +"|"+ nDateObj.getMonth());
    console.log("getDate nDate >> " + nDate +"|"+ nDateObj2.getMonth());
    console.log("getDate tDate  >> " + tDate.getTime());
    console.log("getDate rst >> " + (tDate.getTime() - nDateObj.getTime())/day);
}

fnDateDayTerm = function(nDate, tDate){
    
    if(nDate == "" || tDate == "") return nDate; 
    
    var strLen = nDate.length;
    var strLen2 = tDate.length;
    if(!(strLen == 8 || strLen == 10)) return nDate;
    if(!(strLen2 == 8 || strLen2 == 10)) return nDate;
    
    if(strLen == 8){
        nDate = nDate.substr(0,4)+"-"+nDate.substr(4,2)+"-"+nDate.substr(6,2)
    }
    
    if(strLen2 == 8){
        tDate = tDate.substr(0,4)+"-"+tDate.substr(4,2)+"-"+tDate.substr(6,2)
    }
    
    getDate(nDate, tDate);
     
    var tDate = new Date(tDate);  
	var nDateObj = new Date(nDate);

    var day = 1000*60*60*24;  
    var month = day*30;
    var year = month*12;
	
    console.log("nDate >> " + nDate +"|"+ nDateObj.getTime());
    console.log("nDate >> " + nDate +"|"+ nDateObj.getMonth());
    console.log("tDate >> " + tDate.getTime());
    console.log("rst >> " + (tDate.getTime() - nDateObj.getTime())/day);
     
    return (tDate.getTime() - nDateObj.getTime())/day;  
}

fnDateDayTerm("2016-01-31", "2016-02-01"); 



</script>
		<h2>${message}</h2>
		
        <input type="hidden" id="cstId" value="201701090001" />
        <input type="hidden" id="cstNm" value="LEEJONGPIL" />
        
        <button id="btnPay">pay</button>
        <button id="btnRePay">RePay</button>
        
 	</body>
</html>
