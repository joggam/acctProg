<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>조건1 키워드 엑셀 대량업로드</title>
<link rel="stylesheet" href="<c:url value='/css/egovframework/com/com.css'/>">
<link rel="stylesheet" href="<c:url value='/css/vat/home/card/VatCardCondition1.css'/>">
</head>
<body>
<div id="content" class="vat_condition_wrap">
    <h1>조건1 키워드 엑셀 대량업로드</h1>

    <div class="vat_condition_notice">
        <div>컬럼 순서: <strong>키워드구분 / 적용대상 / 키워드 / 계정과목</strong></div>
        <div>키워드구분: <strong>사업자-법인(142) : 1, 직원O(811) : 2, 차량O(822) : 3</strong> 으로 기입</div>
        <div>적용대상: <strong>업태 또는 업종</strong>으로 기입</div>
        <div><strong>타이틀 행 밑의 데이터 ROW만 DB에 등록됩니다.</strong></div><div>같은 키워드구분 + 적용대상 + 키워드가 이미 존재하면 계정과목을 수정하고, 없으면 신규 등록합니다.</div>
    </div>

    <div class="vat_condition_btn_area">
        <a class="vat_condition_btn" href="<c:url value='/vat/home/card/keyword/excelSample.do'/>">엑셀 양식 다운로드</a>
        <a class="vat_condition_btn" href="<c:url value='/vat/home/card/keyword/list.do'/>">키워드 목록</a>
    </div>

    <form method="post" enctype="multipart/form-data" action="<c:url value='/vat/home/card/keyword/excelUpload.do'/>">
        <div class="vat_condition_search">
            <label for="excelFile">업로드 파일</label>
            <input class="vat_condition_file" type="file" id="excelFile" name="excelFile" accept=".xls,.xlsx" required>
            <button type="submit" class="vat_condition_btn vat_condition_btn_primary">대량업로드</button>
        </div>
    </form>

    <c:if test="${not empty resultMessage}">
        <div class="vat_condition_message"><c:out value="${resultMessage}"/></div>
    </c:if>
    <c:if test="${not empty errorList}">
        <div class="vat_condition_error">
            <ul><c:forEach var="e" items="${errorList}"><li><c:out value="${e}"/></li></c:forEach></ul>
        </div>
    </c:if>
</div>
</body>
</html>
