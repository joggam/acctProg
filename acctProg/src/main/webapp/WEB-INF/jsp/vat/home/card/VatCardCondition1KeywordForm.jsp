<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>조건1 키워드 등록/수정</title>
<link rel="stylesheet" href="<c:url value='/css/egovframework/com/com.css'/>">
<link rel="stylesheet" href="<c:url value='/css/vat/home/card/VatCardCondition1.css'/>">
<script>
function syncAccount(){var t=document.getElementById('keywordType').value;document.getElementById('accountCode').value=t==='CORP'?'142':t==='EMPLOYEE'?'811':t==='VEHICLE'?'822':'';}
function fnDelete(){if(confirm('삭제하시겠습니까?')) location.href='<c:url value="/vat/home/card/keyword/delete.do"/>?keywordSeq=${keywordVO.keywordSeq}';}
</script>
</head>
<body>
<div id="content" class="vat_condition_wrap">
    <h1>조건1 키워드 등록/수정</h1>
    <form method="post" action="<c:url value='/vat/home/card/keyword/save.do'/>">
        <input type="hidden" name="keywordSeq" value="${keywordVO.keywordSeq}">
        <table class="vat_condition_form_table">
            <tr><th>조건2 키워드 구분 <span class="vat_condition_required">*</span></th><td><select class="vat_condition_select" name="keywordType" id="keywordType" onchange="syncAccount()" required><option value="">선택</option><option value="CORP" <c:if test="${keywordVO.keywordType eq 'CORP'}">selected</c:if>>1번 사업자-법인 (142)</option><option value="EMPLOYEE" <c:if test="${keywordVO.keywordType eq 'EMPLOYEE'}">selected</c:if>>3번 직원 O (811)</option><option value="VEHICLE" <c:if test="${keywordVO.keywordType eq 'VEHICLE'}">selected</c:if>>4번 차량 O (822)</option></select></td></tr>
            <tr><th>적용대상 <span class="vat_condition_required">*</span></th><td><select class="vat_condition_select" name="targetType" required><option value="BIZCND" <c:if test="${keywordVO.targetType eq 'BIZCND'}">selected</c:if>>업태</option><option value="INDUTY" <c:if test="${keywordVO.targetType eq 'INDUTY'}">selected</c:if>>업종</option></select></td></tr>
            <tr><th>키워드 <span class="vat_condition_required">*</span></th><td><input class="vat_condition_input" type="text" name="keyword" value="<c:out value='${keywordVO.keyword}'/>" maxlength="200" required><span class="vat_condition_help">완전일치</span></td></tr>
            <tr><th>계정과목 <span class="vat_condition_required">*</span></th><td><input class="vat_condition_input" type="text" id="accountCode" name="accountCode" value="<c:out value='${keywordVO.accountCode}'/>" maxlength="20" required><span class="vat_condition_help">CORP=142 / EMPLOYEE=811 / VEHICLE=822</span></td></tr>
            <tr><th>사용여부</th><td><select class="vat_condition_select" name="useAt"><option value="Y" <c:if test="${keywordVO.useAt eq 'Y'}">selected</c:if>>사용</option><option value="N" <c:if test="${keywordVO.useAt eq 'N'}">selected</c:if>>미사용</option></select></td></tr>
        </table>
        <div class="vat_condition_btn_area">
            <button type="button" class="vat_condition_btn" onclick="location.href='<c:url value="/vat/home/card/keyword/list.do"/>'">목록</button>
            <c:if test="${not empty keywordVO.keywordSeq}"><button type="button" class="vat_condition_btn vat_condition_btn_danger" onclick="fnDelete()">삭제</button></c:if>
            <button type="submit" class="vat_condition_btn vat_condition_btn_primary">저장</button>
        </div>
    </form>
</div>
</body>
</html>
