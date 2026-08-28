<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>조건1 엑셀 대량업로드</title>
<link rel="stylesheet" href="<c:url value='/css/egovframework/com/com.css'/>">
<link rel="stylesheet" href="<c:url value='/css/vat/home/card/VatCardCondition1.css'/>">
</head>
<body>
<div id="content" class="vat_condition_wrap">
    <h1>조건1 엑셀 대량업로드</h1>

    <div class="vat_condition_notice">
        <div>컬럼 순서: <strong>업태 / 업종 / 부가세공제여부 / 부가세유형(2자리) / 계정과목</strong></div>
        <div>업태+업종이 이미 존재하면 해당 행을 수정(UPSERT)하고, 없으면 신규 등록합니다. 기존 전체 데이터는 삭제하지 않습니다.</div>
    </div>

    <div class="vat_condition_btn_area">
        <a class="vat_condition_btn" href="<c:url value='/vat/home/card/excelSample.do'/>">엑셀 양식 다운로드</a>
        <a class="vat_condition_btn" href="<c:url value='/vat/home/card/list.do'/>">조건1 목록</a>
    </div>

    <form method="post" enctype="multipart/form-data" action="<c:url value='/vat/home/card/excelUpload.do'/>">
        <div class="vat_condition_search">
            <label for="excelFile">업로드 파일</label>
            <input class="vat_condition_file" type="file" id="excelFile" name="excelFile" accept=".xls,.xlsx" required>
            <button type="submit" class="vat_condition_btn vat_condition_btn_primary">대량업로드</button>
        </div>
    </form>

    <c:if test="${not empty resultMessage}"><div class="vat_condition_message"><c:out value="${resultMessage}"/></div></c:if>
    <c:if test="${not empty errorList}"><div class="vat_condition_error"><ul><c:forEach var="e" items="${errorList}"><li><c:out value="${e}"/></li></c:forEach></ul></div></c:if>
</div>
</body>
</html>
