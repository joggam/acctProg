<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>조건1 등록/수정</title>
<link rel="stylesheet" href="<c:url value='/css/egovframework/com/com.css'/>">
<link rel="stylesheet" href="<c:url value='/css/vat/home/card/VatCardCondition1.css'/>">
<script>
function fnDelete(){if(confirm('삭제하시겠습니까?')) location.href='<c:url value="/vat/home/card/delete.do"/>?condition1Seq=${condition1VO.condition1Seq}';}
</script>
</head>
<body>
<div id="content" class="vat_condition_wrap">
    <h1>조건1 등록/수정</h1>
    <form method="post" action="<c:url value='/vat/home/card/save.do'/>">
        <input type="hidden" name="condition1Seq" value="${condition1VO.condition1Seq}">
        <table class="vat_condition_form_table">
            <tr><th>업태 <span class="vat_condition_required">*</span></th><td><input class="vat_condition_input" type="text" name="bizcnd" value="<c:out value='${condition1VO.bizcnd}'/>" maxlength="200" required></td></tr>
            <tr><th>업종 <span class="vat_condition_required">*</span></th><td><input class="vat_condition_input" type="text" name="induty" value="<c:out value='${condition1VO.induty}'/>" maxlength="200" required></td></tr>
            <tr><th>부가세공제여부</th><td><input class="vat_condition_input" type="text" name="vatDeductYn" value="<c:out value='${condition1VO.vatDeductYn}'/>" maxlength="20"></td></tr>
            <tr><th>부가세유형(2자리)</th><td><input class="vat_condition_input" type="text" name="vatTypeCode" value="<c:out value='${condition1VO.vatTypeCode}'/>" maxlength="2"></td></tr>
            <tr><th>계정과목</th><td><input class="vat_condition_input" type="text" name="accountCode" value="<c:out value='${condition1VO.accountCode}'/>" maxlength="20"></td></tr>
            <tr><th>사용여부</th><td><select class="vat_condition_select" name="useAt"><option value="Y" <c:if test="${condition1VO.useAt eq 'Y'}">selected</c:if>>사용</option><option value="N" <c:if test="${condition1VO.useAt eq 'N'}">selected</c:if>>미사용</option></select></td></tr>
        </table>
        <div class="vat_condition_btn_area">
            <button type="button" class="vat_condition_btn" onclick="location.href='<c:url value="/vat/home/card/list.do"/>'">목록</button>
            <c:if test="${not empty condition1VO.condition1Seq}"><button type="button" class="vat_condition_btn vat_condition_btn_danger" onclick="fnDelete()">삭제</button></c:if>
            <button type="submit" class="vat_condition_btn vat_condition_btn_primary">저장</button>
        </div>
    </form>
</div>
</body>
</html>
