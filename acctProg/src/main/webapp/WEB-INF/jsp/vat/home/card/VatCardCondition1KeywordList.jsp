<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" uri="http://egovframework.gov/ctl/ui" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>조건1 키워드 관리</title>
<link rel="stylesheet" href="<c:url value='/css/egovframework/com/com.css'/>">
<link rel="stylesheet" href="<c:url value='/css/vat/home/card/VatCardCondition1.css'/>">
<script>
function fnSearch(){document.searchForm.pageIndex.value='1';document.searchForm.submit();}
function fnLinkPage(p){document.searchForm.pageIndex.value=p;document.searchForm.submit();}
function fnForm(s){location.href='<c:url value="/vat/home/card/keyword/form.do"/>'+(s?'?keywordSeq='+s:'');}
</script>
</head>
<body>
<div id="content" class="vat_condition_wrap">
    <h1>조건1 키워드 관리</h1>

    <div class="vat_condition_notice">
        키워드는 조건2에서 명시된 경우만 사용합니다:
        <strong>VAT004 공통코드(사업자-법인(142), 직원O(811), 차량O(822))</strong>.
        업태 또는 업종 값과 <strong>완전일치</strong>합니다.
    </div>

    <form name="searchForm" method="get" action="<c:url value='/vat/home/card/keyword/list.do'/>">
        <input type="hidden" name="pageIndex" value="${searchVO.pageIndex}">
        <div class="vat_condition_search">
            <label for="searchKeyword">키워드</label>
            <input class="vat_condition_input" type="text" id="searchKeyword" name="searchKeyword"
                   value="<c:out value='${searchVO.searchKeyword}'/>">
            <button class="vat_condition_btn" type="button" onclick="fnSearch()">조회</button>
        </div>
    </form>

    <div class="vat_condition_btn_area">
        <a class="vat_condition_btn" href="<c:url value='/vat/home/card/list.do'/>">조건1 관리</a>
        <a class="vat_condition_btn" href="<c:url value='/vat/home/card/keyword/excelView.do'/>">엑셀 대량업로드</a>
        <a class="vat_condition_btn vat_condition_btn_primary" href="javascript:fnForm('')">신규등록</a>
    </div>

    <div class="vat_condition_grid_area">
        <table class="vat_condition_grid">
            <thead>
                <tr><th>번호</th><th>키워드구분</th><th>적용대상</th><th>키워드</th><th>계정과목</th><th>사용</th></tr>
            </thead>
            <tbody>
                <c:forEach var="row" items="${resultList}">
                    <tr onclick="fnForm('${row.keywordSeq}')" style="cursor:pointer">
                        <td>${row.keywordSeq}</td>
                        <td>
                            <c:set var="keywordTypeNm" value="${row.keywordType}"/>
                            <c:forEach var="code" items="${keywordType_result}">
                                <c:if test="${row.keywordType eq code.code}">
                                    <c:set var="keywordTypeNm" value="${code.codeNm}"/>
                                </c:if>
                            </c:forEach>
                            <c:out value="${keywordTypeNm}"/>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${row.targetType eq 'BIZCND'}">업태</c:when>
                                <c:when test="${row.targetType eq 'INDUTY'}">업종</c:when>
                                <c:otherwise><c:out value="${row.targetType}"/></c:otherwise>
                            </c:choose>
                        </td>
                        <td><c:out value="${row.keyword}"/></td>
                        <td><c:out value="${row.accountCode}"/></td>
                        <td><c:out value="${row.useAt}"/></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty resultList}">
                    <tr><td colspan="6" class="vat_condition_no_data">조회된 자료가 없습니다.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
    <div class="vat_condition_paging">
        <ui:pagination paginationInfo="${paginationInfo}" type="image" jsFunction="fnLinkPage"/>
    </div>
</div>
</body>
</html>
