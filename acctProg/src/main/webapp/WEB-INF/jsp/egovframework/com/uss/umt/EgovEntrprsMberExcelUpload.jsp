<%
 /**
  * @Class Name : EgovEntrprsMberExcelUpload.jsp
  * @Description : 기업회원 엑셀 대량등록
  */
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html>
<head>
<title>기업회원 엑셀 대량등록</title>
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<c:url value='/css/egovframework/com/com.css' />">
<script type="text/javascript">
function fnExcelUpload() {
    var file = document.excelForm.excelFile.value;
    if (file == null || file == "") {
        alert("업로드할 엑셀 파일을 선택해 주세요.");
        return false;
    }

    var lower = file.toLowerCase();
    if (!(lower.endsWith(".xlsx") || lower.endsWith(".xls"))) {
        alert("xls 또는 xlsx 파일만 업로드할 수 있습니다.");
        return false;
    }

    if (!confirm("엑셀의 기업회원 정보를 대량등록하시겠습니까?")) {
        return false;
    }

    document.excelForm.action = "<c:url value='/uss/umt/EgovEntrprsMberExcelUpload.do'/>";
    document.excelForm.submit();
    return false;
}
</script>
</head>
<body>

<noscript class="noScriptTitle"><spring:message code="common.noScriptTitle.msg" /></noscript>

<form name="excelForm" method="post" enctype="multipart/form-data">
<div class="board">
    <h1>기업회원 엑셀 대량등록</h1>

    <table class="wTable" summary="기업회원 엑셀 대량등록">
        <caption>기업회원 엑셀 대량등록</caption>
        <colgroup>
            <col style="width:22%;">
            <col style="width:78%;">
        </colgroup>
        <tbody>
        <tr>
            <th>엑셀 양식</th>
            <td class="left">
                <span class="btn_s">
                    <a href="<c:url value='/uss/umt/EgovEntrprsMberExcelSample.do'/>" title="엑셀 양식 다운로드">엑셀 양식 다운로드</a>
                </span>
                <div style="margin-top:8px; line-height:1.7;">
                    입력 컬럼: 기업회원ID / 비밀번호 / 사업자등록번호 / 법인등록번호 / 회사명 / 주민등록번호 2번째 값<br/>
                    기업회원ID, 비밀번호, 회사명은 필수입니다.<br/>
                    사업자등록번호 또는 법인등록번호 중 최소 1개는 반드시 입력해야 합니다.<br/>
                    두 번호 모두 입력해도 되며, 하이픈(-)은 등록 시 제거되어 숫자만 저장됩니다.
                </div>
            </td>
        </tr>
        <tr>
            <th><label for="excelFile">엑셀 파일</label> <span class="pilsu">*</span></th>
            <td class="left">
                <input type="file" name="excelFile" id="excelFile" accept=".xls,.xlsx" style="width:70%;" />
                <div style="margin-top:6px;">xls, xlsx 파일 / 최대 10MB</div>
            </td>
        </tr>
        </tbody>
    </table>

    <div class="btn">
        <span class="btn_s"><a href="<c:url value='/uss/umt/EgovEntrprsMberManage.do'/>" title="목록">목록</a></span>
        <input type="button" class="s_submit" value="엑셀대량등록" onclick="fnExcelUpload();" title="엑셀대량등록" />
    </div>
    <div style="clear:both;"></div>

    <c:if test="${not empty resultMessage}">
        <div style="margin-top:25px;">
            <h2>처리결과</h2>
            <table class="wTable" summary="엑셀 대량등록 처리결과">
                <caption>엑셀 대량등록 처리결과</caption>
                <colgroup>
                    <col style="width:22%;">
                    <col style="width:78%;">
                </colgroup>
                <tbody>
                <tr>
                    <th>결과</th>
                    <td class="left"><c:out value="${resultMessage}"/></td>
                </tr>
                <c:if test="${not empty totalCount}">
                <tr>
                    <th>등록 건수</th>
                    <td class="left">
                        전체 <strong><c:out value="${totalCount}"/></strong>건 /
                        성공 <strong><c:out value="${successCount}"/></strong>건 /
                        실패 <strong><c:out value="${failCount}"/></strong>건
                    </td>
                </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${not empty errorList}">
        <div style="margin-top:20px;">
            <h2>실패 상세</h2>
            <table class="board_list" summary="엑셀 대량등록 실패 상세">
                <caption>엑셀 대량등록 실패 상세</caption>
                <colgroup>
                    <col style="width:8%;">
                    <col style="width:92%;">
                </colgroup>
                <thead>
                <tr>
                    <th>번호</th>
                    <th>실패 사유</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="error" items="${errorList}" varStatus="status">
                    <tr>
                        <td><c:out value="${status.count}"/></td>
                        <td class="left"><c:out value="${error}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>
</div>
</form>

</body>
</html>
