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
    $('#btnRePay').hide();
    
    $("#btnPay, #btnRePay").on("click", function(){
        $('#btnPay').hide();
        
        var apgInfo = {
			 cstId : $("#cstId").val()
			,cstNm : $("#cstNm").val()
		}
        
        var payPop = window.fnOrdPop(apgInfo);
		checkPopup = window.setInterval(function() {
			if (payPop.closed) {
				window.clearInterval(checkPopup);
				$('#btnRepay').show();
			}
		}, 200);
        
    });
    
    fnOrdPop = function(){
        window.open("/showMessage.jsp", "", "width=400, height=300, left=100, location=1");
    }
    
});
</script>
		<h2>${message}</h2>
		<h2>${spm}</h2>
        
        <input type="hidden" id="cstId" value="201701090001" />
        <input type="hidden" id="cstNm" value="LEEJONGPIL" />
        
        <button id="btnPay">pay</button>
        <button id="btnRePay">RePay</button>
        
        
	</body>
</html>
