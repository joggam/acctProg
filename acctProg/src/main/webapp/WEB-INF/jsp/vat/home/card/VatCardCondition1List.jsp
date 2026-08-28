<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" uri="http://egovframework.gov/ctl/ui" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>조건1 관리</title>
<link rel="stylesheet" href="<c:url value='/css/egovframework/com/com.css'/>">
<link rel="stylesheet" href="<c:url value='/css/vat/home/card/VatCardCondition1.css'/>">
<script>
function fnSearch(){document.searchForm.pageIndex.value='1';document.searchForm.submit();}
function fnLinkPage(page){document.searchForm.pageIndex.value=page;document.searchForm.submit();}
function fnForm(seq){location.href='<c:url value="/vat/home/card/form.do"/>'+(seq?'?condition1Seq='+seq:'');}
</script>
</head>
<body>
<div id="content" class="vat_condition_wrap">
    <h1>조건1 관리</h1>

    <form name="searchForm" method="get" action="<c:url value='/vat/home/card/list.do'/>">
        <input type="hidden" name="pageIndex" value="${searchVO.pageIndex}">
        <div class="vat_condition_search">
            <label for="searchKeyword">업태/업종</label>
            <input type="text" id="searchKeyword" name="searchKeyword" class="vat_condition_input" style="width:260px;" value="<c:out value='${searchVO.searchKeyword}'/>">
            <button type="button" class="vat_condition_btn vat_condition_btn_primary" onclick="fnSearch()">조회</button>
        </div>
    </form>

    <div class="vat_condition_btn_area">
        <a class="vat_condition_btn" href="<c:url value='/vat/home/card/excelView.do'/>">엑셀 대량업로드</a>
        <a class="vat_condition_btn" href="<c:url value='/vat/home/card/keyword/list.do'/>">키워드 관리</a>
        <a class="vat_condition_btn vat_condition_btn_primary" href="javascript:fnForm('')">신규등록</a>
    </div>

    <div class="vat_condition_grid_area">
        <table class="vat_condition_grid">
            <thead><tr><th>번호</th><th>업태</th><th>업종</th><th>부가세공제여부</th><th>부가세유형(2자리)</th><th>계정과목</th><th>사용</th></tr></thead>
            <tbody>
            <c:forEach var="row" items="${resultList}">
                <tr onclick="fnForm('${row.condition1Seq}')" style="cursor:pointer">
                    <td>${row.condition1Seq}</td>
                    <td><c:out value="${row.bizcnd}"/></td>
                    <td><c:out value="${row.induty}"/></td>
                    <td><c:out value="${row.vatDeductYn}"/></td>
                    <td><c:out value="${row.vatTypeCode}"/></td>
                    <td><c:out value="${row.accountCode}"/></td>
                    <td><c:out value="${row.useAt}"/></td>
                </tr>
            </c:forEach>
            <c:if test="${empty resultList}"><tr><td colspan="7" class="vat_condition_no_data">조회된 자료가 없습니다.</td></tr></c:if>
            </tbody>
        </table>
    </div>
    <div class="vat_condition_paging"><ui:pagination paginationInfo="${paginationInfo}" type="image" jsFunction="fnLinkPage"/></div>
</div>
</body>
</html>
