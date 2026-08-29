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
function fnDelete(){
    if(confirm('삭제하시겠습니까?')){
        location.href='<c:url value="/vat/home/card/keyword/delete.do"/>?keywordSeq=${keywordVO.keywordSeq}';
    }
}
</script>
</head>
<body>
<div id="content" class="vat_condition_wrap">
    <h1>조건1 키워드 등록/수정</h1>

    <form method="post" action="<c:url value='/vat/home/card/keyword/save.do'/>">
        <input type="hidden" name="keywordSeq" value="${keywordVO.keywordSeq}">

        <div class="vat_condition_grid_area">
            <table class="vat_condition_form_table">
                <tr>
                    <th>키워드구분 *</th>
                    <td>
                        <select class="vat_condition_select" name="keywordType" id="keywordType" required>
                            <option value="">선택</option>
                            <c:forEach var="code" items="${keywordType_result}">
                                <option value="<c:out value='${code.code}'/>"
                                    <c:if test="${keywordVO.keywordType eq code.code}">selected</c:if>>
                                    <c:out value="${code.codeNm}"/>
                                </option>
                            </c:forEach>
                        </select>
                        <span class="vat_condition_help">공통코드 VAT004</span>
                    </td>
                </tr>
                <tr>
                    <th>적용대상 *</th>
                    <td>
                        <select class="vat_condition_select" name="targetType" required>
                            <option value="BIZCND" <c:if test="${keywordVO.targetType eq 'BIZCND'}">selected</c:if>>업태</option>
                            <option value="INDUTY" <c:if test="${keywordVO.targetType eq 'INDUTY'}">selected</c:if>>업종</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <th>키워드 *</th>
                    <td>
                        <input class="vat_condition_input" type="text" name="keyword"
                               value="<c:out value='${keywordVO.keyword}'/>" maxlength="200" required>
                        <span class="vat_condition_help">완전일치</span>
                    </td>
                </tr>
                <tr>
                    <th>계정과목 *</th>
                    <td>
                        <input class="vat_condition_input" type="text" id="accountCode" name="accountCode"
                               value="<c:out value='${keywordVO.accountCode}'/>" maxlength="20" required>
                    </td>
                </tr>
                <tr>
                    <th>사용여부</th>
                    <td>
                        <select class="vat_condition_select" name="useAt">
                            <option value="Y" <c:if test="${empty keywordVO.useAt or keywordVO.useAt eq 'Y'}">selected</c:if>>사용</option>
                            <option value="N" <c:if test="${keywordVO.useAt eq 'N'}">selected</c:if>>미사용</option>
                        </select>
                    </td>
                </tr>
            </table>
        </div>

        <div class="vat_condition_btn_area">
            <a class="vat_condition_btn" href="<c:url value='/vat/home/card/keyword/list.do'/>">목록</a>
            <c:if test="${not empty keywordVO.keywordSeq}">
                <button class="vat_condition_btn" type="button" onclick="fnDelete()">삭제</button>
            </c:if>
            <button class="vat_condition_btn vat_condition_btn_primary" type="submit">저장</button>
        </div>
    </form>
</div>
</body>
</html>
