<%
 /**
  * @Class Name : VatCardPurchaseList.jsp
  * @Description : 사업용신용카드 매입세액 공제 확인/변경 화면
  */
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
<title>사업용신용카드 매입세액 공제 확인/변경</title>
<meta http-equiv="content-type" content="text/html; charset=utf-8">

<link type="text/css" rel="stylesheet" href="<c:url value='/css/egovframework/com/com.css' />">
<link type="text/css" rel="stylesheet" href="<c:url value='/css/vat/home/card/VatCardPurchase.css' />">

<script type="text/javascript">

function fn_vat_init() {
    fn_vat_changePeriodType();
}

/**
 * 조회기간 선택에 따라 입력영역 변경
 */
function fn_vat_changePeriodType() {

    var periodType = document.querySelector(
        'input[name="searchPeriodType"]:checked'
    );

    var dayArea = document.getElementById("dayArea");
    var monthArea = document.getElementById("monthArea");
    var quarterArea = document.getElementById("quarterArea");

    dayArea.style.display = "none";
    monthArea.style.display = "none";
    quarterArea.style.display = "none";

    if (!periodType) {
        return;
    }

    if (periodType.value === "DAY") {
        dayArea.style.display = "inline-flex";
    } else if (periodType.value === "MONTH") {
        monthArea.style.display = "inline-flex";
    } else {
        quarterArea.style.display = "inline-flex";
    }
}

/**
 * 조회
 * 실제 DB 조회 기능은 후속작업에서 연결
 */
function fn_vat_search() {

    document.vatCardPurchaseForm.pageIndex.value = 1;

    /*
     * TODO 후속작업
     * Controller -> Service -> DAO -> MyBatis Mapper 조회 연결
     */
    document.vatCardPurchaseForm.action =
        "<c:url value='/vat/home/card/selectVatCardPurchaseList.do'/>";

    document.vatCardPurchaseForm.submit();
}

/**
 * 페이징
 */
function fn_vat_selectPage(pageNo) {

    document.vatCardPurchaseForm.pageIndex.value = pageNo;

    document.vatCardPurchaseForm.action =
        "<c:url value='/vat/home/card/selectVatCardPurchaseList.do'/>";

    document.vatCardPurchaseForm.submit();
}

/**
 * 엑셀 내려받기
 * 실제 엑셀 생성 기능은 후속작업에서 연결
 */
function fn_vat_excelDownload() {

    /*
     * TODO 후속작업
     * /vat/home/card/downloadVatCardPurchaseExcel.do 연결 예정
     */

    return false;
}

</script>
</head>

<body onload="fn_vat_init();">

<form name="vatCardPurchaseForm"
      action="<c:url value='/vat/home/card/selectVatCardPurchaseList.do'/>"
      method="post">

<div class="vat_card_wrap">

    <h1>사업용신용카드 매입세액 공제 확인/변경</h1>


    <!-- 조회조건 -->
    <div class="vat_search_box">

        <div class="vat_search_row">
            <div class="vat_search_title">
                <span class="vat_required">*</span>
                조회기간
            </div>

            <div class="vat_search_content">

                <label>
                    <input type="radio"
                           name="searchPeriodType"
                           value="DAY"
                           onclick="fn_vat_changePeriodType();"
                           <c:if test="${searchVO.searchPeriodType == 'DAY'}">checked="checked"</c:if>>
                    일별
                </label>

                <label>
                    <input type="radio"
                           name="searchPeriodType"
                           value="MONTH"
                           onclick="fn_vat_changePeriodType();"
                           <c:if test="${searchVO.searchPeriodType == 'MONTH'}">checked="checked"</c:if>>
                    월별
                </label>

                <label>
                    <input type="radio"
                           name="searchPeriodType"
                           value="QUARTER"
                           onclick="fn_vat_changePeriodType();"
                           <c:if test="${searchVO.searchPeriodType == 'QUARTER'}">checked="checked"</c:if>>
                    분기별
                </label>

                <span id="dayArea" class="vat_period_area">
                    <input type="date"
                           name="searchDate"
                           value="<c:out value='${searchVO.searchDate}'/>"
                           title="조회일자">
                </span>

                <span id="monthArea" class="vat_period_area">
                    <input type="month"
                           name="searchMonth"
                           value="<c:out value='${searchVO.searchMonth}'/>"
                           title="조회년월">
                </span>

                <span id="quarterArea" class="vat_period_area">

                    <select name="searchYear" title="년도 선택">
                        <c:forEach items="${yearList}" var="year">
                            <option value="${year}"
                                <c:if test="${searchVO.searchYear == year}">selected="selected"</c:if>>
                                ${year}년
                            </option>
                        </c:forEach>
                    </select>

                    <select name="searchQuarter" title="분기 선택">
                        <option value="1"
                            <c:if test="${searchVO.searchQuarter == '1'}">selected="selected"</c:if>>
                            1분기
                        </option>
                        <option value="2"
                            <c:if test="${searchVO.searchQuarter == '2'}">selected="selected"</c:if>>
                            2분기
                        </option>
                        <option value="3"
                            <c:if test="${searchVO.searchQuarter == '3'}">selected="selected"</c:if>>
                            3분기
                        </option>
                        <option value="4"
                            <c:if test="${searchVO.searchQuarter == '4'}">selected="selected"</c:if>>
                            4분기
                        </option>
                    </select>

                </span>
            </div>
        </div>

        <div class="vat_search_row">
            <div class="vat_search_title">
                공제여부
            </div>

            <div class="vat_search_content">
                <select name="deductionType" title="공제여부 선택">
                    <option value="ALL"
                        <c:if test="${searchVO.deductionType == 'ALL'}">selected="selected"</c:if>>
                        -전체-
                    </option>
                    <option value="Y"
                        <c:if test="${searchVO.deductionType == 'Y'}">selected="selected"</c:if>>
                        공제대상
                    </option>
                    <option value="N"
                        <c:if test="${searchVO.deductionType == 'N'}">selected="selected"</c:if>>
                        불공제대상
                    </option>
                </select>
            </div>

        </div>
        
        
        <div class="vat_search_row">
            <div class="vat_search_title">
                사업자등록번호
            </div>

            <div class="vat_search_content">
            	<input 	type="text"
               			class="input2"
                        name="searchBusinessNo"
                        title="사업자등록번호">
       		</div>
       </div>
        <div class="vat_search_row">
            <div class="vat_search_title">
                상호
            </div>

            <div class="vat_search_content">
            	<input 	type="text"
               			class="input2"
                        name="searchBusinessName"
                        title="상호">
       		</div>
       		
	       <div class="vat_search_button_area">
	            <input type="button"
	                       class="vat_btn vat_btn_search"
	                       value="조회"
	                       onclick="fn_vat_search();">
	       </div>
       </div>
       

    </div>

    <!-- 결과 상단 -->
    <div class="vat_result_top">

        <div class="vat_total_amount">
            총 사용금액 :
            <strong>
                <fmt:formatNumber value="${totalUseAmount}" pattern="#,##0" />
            </strong>
            원
            <span>(비과세 제외)</span>
        </div>

        <div>
            <input type="button"
                   class="vat_btn vat_btn_download"
                   value="내려받기"
                   onclick="fn_vat_excelDownload();">
        </div>

    </div>

    <!-- 목록 -->
    <div class="vat_grid_area">

        <table class="vat_grid">
            <caption>사업용신용카드 매입세액 공제 확인/변경 목록</caption>

            <colgroup>
                <col style="width:110px;">
                <col style="width:130px;">
                <col style="width:160px;">
                <col style="width:110px;">
                <col style="width:100px;">
                <col style="width:100px;">
                <col style="width:110px;">
                <col style="width:110px;">
                <col style="width:100px;">
                <col style="width:100px;">
                <col style="width:110px;">
                <col style="width:100px;">
            </colgroup>

            <thead>
            <tr>
                <th scope="col">승인일자</th>
                <th scope="col">가맹점<br>사업자번호</th>
                <th scope="col">가맹점명</th>
                <th scope="col">공급가액</th>
                <th scope="col">세액</th>
                <th scope="col">비과세</th>
                <th scope="col">합계</th>
                <th scope="col">가맹점유형</th>
                <th scope="col">업태</th>
                <th scope="col">업종</th>
                <th scope="col">공제여부<br>결정</th>
                <th scope="col">비고</th>
            </tr>
            </thead>

            <tbody>

            <c:if test="${fn:length(resultList) == 0}">
                <tr>
                    <td colspan="12" class="vat_no_data">
                        조회된 결과가 없습니다.
                    </td>
                </tr>
            </c:if>

            <c:forEach items="${resultList}" var="resultInfo">
                <tr>
                    <td>
                        <c:out value="${resultInfo.approvalDate}" />
                    </td>

                    <td>
                        <c:out value="${resultInfo.merchantBusinessNo}" />
                    </td>

                    <td class="vat_align_left">
                        <c:out value="${resultInfo.merchantName}" />
                    </td>

                    <td class="vat_align_right">
                        <fmt:formatNumber value="${resultInfo.supplyAmount}" pattern="#,##0" />
                    </td>

                    <td class="vat_align_right">
                        <fmt:formatNumber value="${resultInfo.taxAmount}" pattern="#,##0" />
                    </td>

                    <td class="vat_align_right">
                        <fmt:formatNumber value="${resultInfo.taxFreeAmount}" pattern="#,##0" />
                    </td>

                    <td class="vat_align_right">
                        <fmt:formatNumber value="${resultInfo.totalAmount}" pattern="#,##0" />
                    </td>

                    <td>
                        <c:out value="${resultInfo.merchantType}" />
                    </td>

                    <td>
                        <c:out value="${resultInfo.businessStatus}" />
                    </td>

                    <td>
                        <c:out value="${resultInfo.businessType}" />
                    </td>

                    <td>
                        <c:choose>
                            <c:when test="${resultInfo.deductionYn == 'Y'}">
                                공제대상
                            </c:when>
                            <c:when test="${resultInfo.deductionYn == 'N'}">
                                불공제대상
                            </c:when>
                            <c:otherwise>
                                <c:out value="${resultInfo.deductionYn}" />
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <td class="vat_align_left">
                        <c:out value="${resultInfo.remark}" />
                    </td>

                </tr>
            </c:forEach>

            </tbody>
        </table>

    </div>

    <!-- 페이징 골격 -->
    <div class="vat_paging">

        <button type="button"
                class="vat_page_btn"
                onclick="fn_vat_selectPage(1);">
            이전
        </button>

        <span class="vat_page_current">
            <c:out value="${searchVO.pageIndex}" />
        </span>

        <button type="button"
                class="vat_page_btn"
                onclick="fn_vat_selectPage(1);">
            다음
        </button>

        <span class="vat_page_count">
            총 <strong><c:out value="${totalCount}" /></strong>건
        </span>

    </div>

</div>

<input type="hidden"
       name="pageIndex"
       value="<c:out value='${searchVO.pageIndex}'/>">

</form>

</body>
</html>
