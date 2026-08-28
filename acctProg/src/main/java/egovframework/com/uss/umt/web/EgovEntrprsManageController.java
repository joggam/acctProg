package egovframework.com.uss.umt.web;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springmodules.validation.commons.DefaultBeanValidator;

import egovframework.com.cmm.ComDefaultCodeVO;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.annotation.IncludedInfo;
import egovframework.com.cmm.service.CmmnDetailCode;
import egovframework.com.cmm.service.EgovCmmUseService;
import egovframework.com.cmm.util.EgovUserDetailsHelper;
import egovframework.com.uss.umt.service.EgovEntrprsBizrManageService;
import egovframework.com.uss.umt.service.EgovEntrprsManageService;
import egovframework.com.uss.umt.service.EntrprsManageVO;
import egovframework.com.uss.umt.service.StplatVO;
import egovframework.com.uss.umt.service.UserDefaultVO;
import egovframework.com.utl.sim.service.EgovFileScrty;

/**
 * 기업회원관련 요청을  비지니스 클래스로 전달하고 처리된결과를  해당   웹 화면으로 전달하는  Controller를 정의한다
 * @author 공통서비스 개발팀 조재영
 * @since 2009.04.10
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일          수정자       수정내용
 *  -----------    --------    ---------------------------
 *   2009.04.10     조재영       최초 생성
 *   2011.08.26     정진오       IncludedInfo annotation 추가
 *   2014.12.08     이기하       암호화방식 변경(EgovFileScrty.encryptPassword)
 *   2015.06.16     조정국       수정시 유효성체크 후 에러발생 시 목록으로 이동하여 에러메시지 표시
 *   2015.06.19     조정국       미인증 사용자에 대한 보안처리 기준 수정 (!isAuthenticated)
 *   2017.07.21     장동한       로그인인증제한 작업
 *   2020.07.18     윤주호       암호 설정 규칙 강화 및 버그 수정
 *   2021.05.30     정진오       디지털원패스 정보 조회
 *   2022.07.13     김해준       디지털원패스 정보 조회 null 판별 수정
 * </pre>
 */

@Controller
public class EgovEntrprsManageController {

    /** entrprsManageService */
    @Resource(name = "entrprsManageService")
    private EgovEntrprsManageService entrprsManageService;

    /** 기업회원 사업자등록번호 1:N 관리 Service */
    @Resource(name = "entrprsBizrManageService")
    private EgovEntrprsBizrManageService entrprsBizrManageService;

    /** cmmUseService */
    @Resource(name = "EgovCmmUseService")
    private EgovCmmUseService cmmUseService;

    /** EgovPropertyService */
    @Resource(name = "propertiesService")
    protected EgovPropertyService propertiesService;

    /** DefaultBeanValidator beanValidator */
    @Autowired
    private DefaultBeanValidator beanValidator;

    /** 비밀번호 힌트 조회 목록 */
    @ModelAttribute("passwordHint_result")
    private List<CmmnDetailCode> getPasswordHintResult(ComDefaultCodeVO comDefaultCodeVO) throws Exception {
        comDefaultCodeVO.setCodeId("COM022");
        return cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
    }

    /** 성별 조회 목록 */
    @ModelAttribute("sexdstnCode_result")
    private List<CmmnDetailCode> getSexdstnCode_result(ComDefaultCodeVO comDefaultCodeVO) throws Exception {
        comDefaultCodeVO.setCodeId("COM014");
        return cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
    }

    /** 사용자 상태 조회 목록 */
    @ModelAttribute("entrprsMberSttus_result")
    private List<CmmnDetailCode> getEntrprsMberSttus_result(ComDefaultCodeVO comDefaultCodeVO) throws Exception {
        comDefaultCodeVO.setCodeId("COM013");
        return cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
    }

    /** 그룹 정보 조회 목록 */
    @ModelAttribute("groupId_result")
    private List<CmmnDetailCode> getGroupId_result(ComDefaultCodeVO comDefaultCodeVO) throws Exception {
        comDefaultCodeVO.setTableNm("COMTNORGNZTINFO");
        return cmmUseService.selectGroupIdDetail(comDefaultCodeVO);
    }

    /** 기업 구분 조회 목록 */
    @ModelAttribute("entrprsSeCode_result")
    private List<CmmnDetailCode> getEntrprsSeCode_result(ComDefaultCodeVO comDefaultCodeVO) throws Exception {
        comDefaultCodeVO.setCodeId("COM026");
        return cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
    }

    /** 업종 구분 조회 목록 */
    @ModelAttribute("indutyCode_result")
    private List<CmmnDetailCode> getIndutyCode_result(ComDefaultCodeVO comDefaultCodeVO) throws Exception {
        comDefaultCodeVO.setCodeId("COM027");
        return cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
    }

    /** 사업자구분 조회 목록 (VAT001: 1 법인, 2 개인) */
    @ModelAttribute("bizrSeCode_result")
    private List<CmmnDetailCode> getBizrSeCodeResult(ComDefaultCodeVO comDefaultCodeVO) throws Exception {
        comDefaultCodeVO.setCodeId("VAT001");
        return cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
    }

    /** 직원여부 조회 목록 (VAT002: 1 유, 2 무) */
    @ModelAttribute("emplSeCode_result")
    private List<CmmnDetailCode> getEmplSeCodeResult(ComDefaultCodeVO comDefaultCodeVO) throws Exception {
        comDefaultCodeVO.setCodeId("VAT002");
        return cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
    }

    /** 차량구분 조회 목록 (VAT003: 1 무, 2 불공차량 유, 3 공제차량 유) */
    @ModelAttribute("vhclSeCode_result")
    private List<CmmnDetailCode> getVhclSeCodeResult(ComDefaultCodeVO comDefaultCodeVO) throws Exception {
        comDefaultCodeVO.setCodeId("VAT003");
        return cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
    }

    /**
     * 기업회원 등록화면으로 이동한다.
     *
     * @param userSearchVO    검색조건정보
     * @param entrprsManageVO 기업회원 초기화정보
     * @param model           화면모델
     * @return uss/umt/EgovEntrprsMberInsert
     * @throws Exception
     */
    @RequestMapping("/uss/umt/EgovEntrprsMberInsertView.do")
    public String insertEntrprsMberView(@ModelAttribute("userSearchVO") UserDefaultVO userSearchVO, @ModelAttribute("entrprsManageVO") EntrprsManageVO entrprsManageVO, Model model) throws Exception {

        // 미인증 사용자에 대한 보안처리
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
        if (!isAuthenticated) {
            return "index";
        }

        // ComDefaultCodeVO comDefaultCodeVO = new ComDefaultCodeVO();

        // 패스워드힌트목록을 코드정보로부터 조회
        // comDefaultCodeVO.setCodeId("COM022");
        // List<CmmnDetailCode> passwordHint_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
        // 성별구분코드를 코드정보로부터 조회
        // comDefaultCodeVO.setCodeId("COM014");
        // List<CmmnDetailCode> sexdstnCode_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
        // 사용자상태코드를 코드정보로부터 조회
        // comDefaultCodeVO.setCodeId("COM013");
        // List<CmmnDetailCode> entrprsMberSttus_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
        // 그룹정보를 조회 - GROUP_ID정보
        // comDefaultCodeVO.setTableNm("COMTNORGNZTINFO");
        // List<CmmnDetailCode> groupId_result = cmmUseService.selectGroupIdDetail(comDefaultCodeVO);
        // 기업구분코드를 코드정보로부터 조회 - COM026
        // comDefaultCodeVO.setCodeId("COM026");
        // List<CmmnDetailCode> entrprsSeCode_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
        // 업종코드를 코드정보로부터 조회 - COM027
        // comDefaultCodeVO.setCodeId("COM027");
        // List<CmmnDetailCode> indutyCode_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);

        // model.addAttribute("passwordHint_result", passwordHint_result); // 패스워트힌트목록
        // model.addAttribute("sexdstnCode_result", sexdstnCode_result); // 성별구분코드목록
        // model.addAttribute("entrprsMberSttus_result", entrprsMberSttus_result);// 사용자상태코드목록
        // model.addAttribute("groupId_result", groupId_result); // 그룹정보 목록
        // model.addAttribute("entrprsSeCode_result", entrprsSeCode_result); // 기업구분코드 목록
        // model.addAttribute("indutyCode_result", indutyCode_result); // 업종코드목록

        return "egovframework/com/uss/umt/EgovEntrprsMberInsert";
    }

    /**
     * 기업회원등록처리후 목록화면으로 이동한다.
     *
     * @param entrprsManageVO 신규기업회원정보
     * @param bindingResult   입력값검증용 bindingResult
     * @param model           화면모델
     * @return forward:/uss/umt/EgovEntrprsMberManage.do
     * @throws Exception
     */
    @RequestMapping("/uss/umt/EgovEntrprsMberInsert.do")
    public String insertEntrprsMber(@ModelAttribute("entrprsManageVO") EntrprsManageVO entrprsManageVO,
                                   @RequestParam(value = "additionalBizrno", required = false) List<String> additionalBizrno,
                                   BindingResult bindingResult, Model model) throws Exception {

        // 미인증 사용자에 대한 보안처리
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
        if (!isAuthenticated) {
            return "index";
        }

        entrprsManageVO.setBizrno(normalizeBizrno(entrprsManageVO.getBizrno()));
        List<String> bizrnoList = buildBizrnoList(entrprsManageVO.getBizrno(), additionalBizrno, bindingResult);
        model.addAttribute("additionalBizrnoList", getAdditionalBizrnoList(bizrnoList));

        beanValidator.validate(entrprsManageVO, bindingResult);
        if (bindingResult.hasErrors()) {
            return "egovframework/com/uss/umt/EgovEntrprsMberInsert";
        } else {
            if (entrprsManageVO != null
                    && entrprsManageVO.getGroupId() != null
                    && entrprsManageVO.getGroupId().equals("")) {// 2022.01 Null pointers should not be dereferenced
                entrprsManageVO.setGroupId(null);
            }
            entrprsManageService.insertEntrprsmber(entrprsManageVO);

            // 사업자등록번호는 COMTNENTRPRSBIZR에 1:N 구조로 동기화
            entrprsBizrManageService.replaceBizrnoList(entrprsManageVO.getEntrprsmberId(), bizrnoList);

            // Exception 없이 진행시 등록성공메시지
            model.addAttribute("resultMsg", "success.common.insert");
        }
        return "forward:/uss/umt/EgovEntrprsMberManage.do";

    }

    /**
     * 기업회원정보 수정을 위해기업회원정보를 상세조회한다.
     *
     * @param entrprsmberId 상세조회 대상 기업회원아이디
     * @param userSearchVO  조회조건정보
     * @param model         화면모델
     * @return uss/umt/EgovEntrprsMberSelectUpdt
     * @throws Exception
     */
    @RequestMapping("/uss/umt/EgovEntrprsMberSelectUpdtView.do")
    public String updateEntrprsMberView(@RequestParam("selectedId") String entrprsmberId, @ModelAttribute("searchVO") UserDefaultVO userSearchVO, HttpServletRequest request, Model model)
            throws Exception {

        // 미인증 사용자에 대한 보안처리
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
        if (!isAuthenticated) {
            return "index";
        }

        EntrprsManageVO entrprsManageVO = new EntrprsManageVO();
        entrprsManageVO = entrprsManageService.selectEntrprsmber(entrprsmberId);

        // COMTNENTRPRSBIZR에서 해당 기업회원의 사업자등록번호 전체 조회
        List<String> bizrnoList = entrprsBizrManageService.selectBizrnoList(entrprsManageVO.getEntrprsmberId());
        if (bizrnoList != null && !bizrnoList.isEmpty()) {
            // 기존 validator/JSP 호환을 위해 첫 번째 번호는 기존 bizrno 필드에 유지
            entrprsManageVO.setBizrno(bizrnoList.get(0));
        }
        model.addAttribute("additionalBizrnoList", getAdditionalBizrnoList(bizrnoList));
        model.addAttribute("entrprsManageVO", entrprsManageVO);
        model.addAttribute("userSearchVO", userSearchVO);

        // ComDefaultCodeVO comDefaultCodeVO = new ComDefaultCodeVO();
        // 패스워드힌트목록을 코드정보로부터 조회
        // comDefaultCodeVO.setCodeId("COM022");
        // List<CmmnDetailCode> passwordHint_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
        // 성별구분코드를 코드정보로부터 조회
        // comDefaultCodeVO.setCodeId("COM014");
        // List<CmmnDetailCode> sexdstnCode_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
        // 사용자상태코드를 코드정보로부터 조회
        // comDefaultCodeVO.setCodeId("COM013");
        // List<CmmnDetailCode> entrprsMberSttus_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
        // 그룹정보를 조회 - GROUP_ID정보
        // comDefaultCodeVO.setTableNm("COMTNORGNZTINFO");
        // List<CmmnDetailCode> groupId_result = cmmUseService.selectGroupIdDetail(comDefaultCodeVO);
        // 기업구분코드를 코드정보로부터 조회 - COM026
        // comDefaultCodeVO.setCodeId("COM026");
        // List<CmmnDetailCode> entrprsSeCode_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
        // 업종코드를 코드정보로부터 조회 - COM027
        // comDefaultCodeVO.setCodeId("COM027");
        // List<CmmnDetailCode> indutyCode_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);

        // model.addAttribute("passwordHint_result", passwordHint_result); // 패스워트힌트목록
        // model.addAttribute("sexdstnCode_result", sexdstnCode_result); // 성별구분코드목록
        // model.addAttribute("entrprsMberSttus_result", entrprsMberSttus_result);// 사용자상태코드목록
        // model.addAttribute("groupId_result", groupId_result); // 그룹정보 목록
        // model.addAttribute("entrprsSeCode_result", entrprsSeCode_result); // 기업구분코드 목록
        // model.addAttribute("indutyCode_result", indutyCode_result); // 업종코드목록

        // 2021.05.30, 정진오, 디지털원패스 정보 조회
        LoginVO loginVO = (LoginVO) request.getSession().getAttribute("loginVO");
        String onepassUserId = loginVO.getUniqId();
        String onepassUserkey = loginVO.getOnepassUserkey();
        String onepassIntfToken = loginVO.getOnepassIntfToken();
        if (entrprsmberId.equals(onepassUserId)) {
            model.addAttribute("onepassUserkey", onepassUserkey); // 디지털원패스 사용자키
            model.addAttribute("onepassIntfToken", onepassIntfToken); // 디지털원패스 사용자세션값
        } else {
            model.addAttribute("onepassUserkey", "");
            model.addAttribute("onepassIntfToken", "");
        }

        return "egovframework/com/uss/umt/EgovEntrprsMberSelectUpdt";
    }

    /**
     * 로그인인증제한 해제
     *
     * @param entrprsManageVO 기업회원정보
     * @param model           화면모델
     * @return uss/umt/EgovEntrprsMberSelectUpdtView.do
     * @throws Exception
     */
    @RequestMapping("/uss/umt/EgovEntrprsMberLockIncorrect.do")
    public String updateLockIncorrect(EntrprsManageVO entrprsManageVO, Model model) throws Exception {

        // 미인증 사용자에 대한 보안처리
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
        if (!isAuthenticated) {
            return "index";
        }

        entrprsManageService.updateLockIncorrect(entrprsManageVO);

        return "forward:/uss/umt/EgovEntrprsMberSelectUpdtView.do";
    }

    /**
     * 기업회원정보 수정후 목록조회 화면으로 이동한다.
     *
     * @param entrprsManageVO 수정할 기업회원정보
     * @param bindingResult   입력값 검증용 bindingResult
     * @param model           화면모델
     * @return forward:/uss/umt/EgovEntrprsMberManage.do
     * @throws Exception
     */
    @RequestMapping("/uss/umt/EgovEntrprsMberSelectUpdt.do")
    public String updateEntrprsMber(@ModelAttribute("entrprsManageVO") EntrprsManageVO entrprsManageVO,
                                   @RequestParam(value = "additionalBizrno", required = false) List<String> additionalBizrno,
                                   BindingResult bindingResult, Model model) throws Exception {

        // 미인증 사용자에 대한 보안처리
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
        if (!isAuthenticated) {
            return "index";
        }

        entrprsManageVO.setBizrno(normalizeBizrno(entrprsManageVO.getBizrno()));
        List<String> bizrnoList = buildBizrnoList(entrprsManageVO.getBizrno(), additionalBizrno, bindingResult);
        model.addAttribute("additionalBizrnoList", getAdditionalBizrnoList(bizrnoList));

        beanValidator.validate(entrprsManageVO, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("resultMsg", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "forward:/uss/umt/EgovEntrprsMberSelectUpdtView.do";
        } else {
            if ("".equals(entrprsManageVO.getGroupId())) {
                entrprsManageVO.setGroupId(null);
            }
            entrprsManageService.updateEntrprsmber(entrprsManageVO);

            // 수정된 사업자등록번호 목록을 COMTNENTRPRSBIZR에 동기화
            entrprsBizrManageService.replaceBizrnoList(entrprsManageVO.getEntrprsmberId(), bizrnoList);

            // Exception 없이 진행시 수정성공메시지
            model.addAttribute("resultMsg", "success.common.update");
            return "forward:/uss/umt/EgovEntrprsMberManage.do";
        }
    }

    /**
     * 기업회원정보삭제후 목록조회 화면으로 이동한다.
     *
     * @param checkedIdForDel 삭제대상아이디 정보
     * @param userSearchVO    조회조건정보
     * @param model           화면모델
     * @return "forward:/uss/umt/EgovUserManage.do"
     * @throws Exception
     */
    @RequestMapping("/uss/umt/EgovEntrprsMberDelete.do")
    public String deleteEntrprsMber(@RequestParam("checkedIdForDel") String checkedIdForDel, @ModelAttribute("searchVO") UserDefaultVO userSearchVO, HttpServletRequest request, Model model) throws Exception {

        // 미인증 사용자에 대한 보안처리
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
        if (!isAuthenticated) {
            return "index";
        }

        // 2021.05.30, 정진오, 디지털원패스 정보 조회
        // 2022.07.13, 김해준, null 판별 수정
        LoginVO loginVO = (LoginVO) request.getSession().getAttribute("loginVO");
        String onepassUserkey = loginVO.getOnepassUserkey();
        String onepassIntfToken = loginVO.getOnepassIntfToken();
        if (StringUtils.isNotEmpty(onepassUserkey) || StringUtils.isNotEmpty(onepassIntfToken)) {
            model.addAttribute("resultMsg", "digital.onepass.delete.alert");
        } else {
            // 부모 기업회원 삭제 전에 1:N 사업자등록번호 자식 데이터부터 삭제
            String[] deleteTargets = checkedIdForDel.split(",");
            for (String deleteTarget : deleteTargets) {
                String[] parts = deleteTarget.split(":", 2);
                if (parts.length == 2 && "USR02".equals(parts[0])) {
                    entrprsBizrManageService.deleteBizrnoByUniqId(parts[1]);
                }
            }
            entrprsManageService.deleteEntrprsmber(checkedIdForDel);
            model.addAttribute("resultMsg", "success.common.delete");
        }

        return "forward:/uss/umt/EgovEntrprsMberManage.do";
    }

    /**
     * 기업회원목록을 조회한다. (pageing)
     *
     * @param userSearchVO 검색조건정보
     * @param model        화면모델
     * @return uss/umt/EgovEntrprsMberManage
     * @throws Exception
     */
    @IncludedInfo(name = "기업회원관리", order = 450, gid = 50)
    @RequestMapping(value = "/uss/umt/EgovEntrprsMberManage.do")
    public String selectEntrprsMberList(@ModelAttribute("userSearchVO") UserDefaultVO userSearchVO, ModelMap model) throws Exception {

        // 미인증 사용자에 대한 보안처리
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
        if (!isAuthenticated) {
            return "index";
        }

        /** EgovPropertyService.sample */
        userSearchVO.setPageUnit(propertiesService.getInt("pageUnit"));
        userSearchVO.setPageSize(propertiesService.getInt("pageSize"));

        /** pageing */
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(userSearchVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(userSearchVO.getPageUnit());
        paginationInfo.setPageSize(userSearchVO.getPageSize());

        userSearchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
        userSearchVO.setLastIndex(paginationInfo.getLastRecordIndex());
        userSearchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        List<EntrprsManageVO> resultList = entrprsManageService.selectEntrprsMberList(userSearchVO);
        model.addAttribute("resultList", resultList);

        int totCnt = entrprsManageService.selectEntrprsMberListTotCnt(userSearchVO);
        paginationInfo.setTotalRecordCount(totCnt);
        model.addAttribute("paginationInfo", paginationInfo);

        // 사용자상태코드를 코드정보로부터 조회
        // ComDefaultCodeVO comDefaultCodeVO = new ComDefaultCodeVO();
        // comDefaultCodeVO.setCodeId("COM013");
        // List<CmmnDetailCode> entrprsMberSttus_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
        // model.addAttribute("entrprsMberSttus_result", entrprsMberSttus_result);// 기업회원상태코드목록

        return "egovframework/com/uss/umt/EgovEntrprsMberManage";
    }

    /**
     * 기업회원가입신청 등록화면으로 이동한다.
     *
     * @param userSearchVO    검색조건정보
     * @param entrprsManageVO 기업회원초기화정보
     * @param commandMap      파라메터전송 commandMap
     * @param model           화면모델
     * @return uss/umt/EgovEntrprsMberSbscrb
     * @throws Exception
     */
    @RequestMapping("/uss/umt/EgovEntrprsMberSbscrbView.do")
    public String sbscrbEntrprsMberView(@ModelAttribute("userSearchVO") UserDefaultVO userSearchVO, @ModelAttribute("entrprsManageVO") EntrprsManageVO entrprsManageVO, @RequestParam Map<String, Object> commandMap, Model model) throws Exception {

        // ComDefaultCodeVO comDefaultCodeVO = new ComDefaultCodeVO();
        // 패스워드힌트목록을 코드정보로부터 조회
        // comDefaultCodeVO.setCodeId("COM022");
        // List<CmmnDetailCode> passwordHint_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
        // 성별구분코드를 코드정보로부터 조회
        // comDefaultCodeVO.setCodeId("COM014");
        // List<CmmnDetailCode> sexdstnCode_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
        // 기업구분코드를 코드정보로부터 조회 - COM026
        // comDefaultCodeVO.setCodeId("COM026");
        // List<CmmnDetailCode> entrprsSeCode_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);
        // 업종코드를 코드정보로부터 조회 - COM027
        // comDefaultCodeVO.setCodeId("COM027");
        // List<CmmnDetailCode> indutyCode_result = cmmUseService.selectCmmCodeDetail(comDefaultCodeVO);

        // model.addAttribute("passwordHint_result", passwordHint_result); // 패스워트힌트목록
        // model.addAttribute("sexdstnCode_result", sexdstnCode_result); // 성별구분코드목록
        // model.addAttribute("entrprsSeCode_result", entrprsSeCode_result); // 기업구분코드 목록
        // model.addAttribute("indutyCode_result", indutyCode_result); // 업종코드목록

        if (!"".equals(commandMap.get("realname"))) {
            model.addAttribute("applcntNm", commandMap.get("realname")); // 실명인증된 이름 - 주민번호인증
            model.addAttribute("applcntIhidnum", commandMap.get("ihidnum")); // 실명인증된 주민등록번호 - 주민번호 인증
        }
        if (!"".equals(commandMap.get("realName"))) {
            model.addAttribute("applcntNm", commandMap.get("realName")); // 실명인증된 이름 - ipin인증
        }
        entrprsManageVO.setEntrprsMberSttus("DEFAULT");

        return "egovframework/com/uss/umt/EgovEntrprsMberSbscrb";
    }

	/**
	 * 기업회원가입신청 등록처리후 로그인화면으로 이동한다.
	 * @param entrprsManageVO 기업회원가입신청정보
	 * @return forward:/uat/uia/egovLoginUsr.do
	 * @throws Exception
	 */
	@RequestMapping("/uss/umt/EgovEntrprsMberSbscrb.do")
	public String sbscrbEntrprsMber(@ModelAttribute("entrprsManageVO") EntrprsManageVO entrprsManageVO)
		throws Exception {

		//가입상태 초기화
		entrprsManageVO.setEntrprsMberSttus("A");
		//그룹정보 초기화
		//entrprsManageVO.setGroupId("1");
		//기업회원가입신청 등록시 기업회원등록기능을 사용하여 등록한다.
		entrprsManageService.insertEntrprsmber(entrprsManageVO);
		return "forward:/uat/uia/egovLoginUsr.do";
	}

	/**
	 * 기업회원 약관확인 화면을 조회한다.
	 * @param model 화면모델
	 * @return uss/umt/EgovStplatCnfirm
	 * @throws Exception
	 */
	@RequestMapping("/uss/umt/EgovStplatCnfirmEntrprs.do")
	public String sbscrbEntrprsMber(Model model) throws Exception {

		//기업회원용 약관 아이디 설정
		String stplatId = "STPLAT_0000000000002";
		//회원가입유형 설정-기업회원
		String sbscrbTy = "USR02";
		//약관정보 조회
		List<StplatVO> stplatList = entrprsManageService.selectStplat(stplatId);

		model.addAttribute("stplatList", stplatList); //약관정보포함
		model.addAttribute("sbscrbTy", sbscrbTy); //회원가입유형포함

		return "egovframework/com/uss/umt/EgovStplatCnfirm";
	}

	/**
	 * 기업회원 암호 수정처리 후 화면 이동한다.
	 * @param model 화면모델
	 * @param commandMap 파라메터전달용 commandMap
	 * @param userSearchVO 검색조건정보
	 * @param entrprsManageVO 기업회원수정정보
	 * @return uss/umt/EgovEntrprsPasswordUpdt
	 * @throws Exception
	 */
	@RequestMapping(value = "/uss/umt/EgovEntrprsPasswordUpdt.do")
	public String updatePassword(ModelMap model, @RequestParam Map<String, Object> commandMap,
		@ModelAttribute("searchVO") UserDefaultVO userSearchVO,
		@ModelAttribute("entrprsManageVO") EntrprsManageVO entrprsManageVO) throws Exception {

		// 미인증 사용자에 대한 보안처리
		Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
		if (!isAuthenticated) {
			return "index";
		}

		String oldPassword = (String)commandMap.get("oldPassword");
		String newPassword = (String)commandMap.get("newPassword");
		String newPassword2 = (String)commandMap.get("newPassword2");
		String uniqId = (String)commandMap.get("uniqId");

		boolean isCorrectPassword = false;
		EntrprsManageVO resultVO = new EntrprsManageVO();
		entrprsManageVO.setEntrprsMberPassword(newPassword);
		entrprsManageVO.setOldPassword(oldPassword);
		entrprsManageVO.setUniqId(uniqId);

		String resultMsg = "";
		resultVO = entrprsManageService.selectPassword(entrprsManageVO);
		//패스워드 암호화
		String encryptPass = EgovFileScrty.encryptPassword(oldPassword, entrprsManageVO.getEntrprsmberId());
		if (encryptPass.equals(resultVO.getEntrprsMberPassword())) {
			if (newPassword.equals(newPassword2)) {
				isCorrectPassword = true;
			} else {
				isCorrectPassword = false;
				resultMsg = "fail.user.passwordUpdate2";
			}
		} else {
			isCorrectPassword = false;
			resultMsg = "fail.user.passwordUpdate1";
		}

		if (isCorrectPassword) {
			entrprsManageVO
				.setEntrprsMberPassword(EgovFileScrty.encryptPassword(newPassword, entrprsManageVO.getEntrprsmberId()));
			entrprsManageService.updatePassword(entrprsManageVO);
			model.addAttribute("entrprsManageVO", entrprsManageVO);
			resultMsg = "success.common.update";
		} else {
			model.addAttribute("entrprsManageVO", entrprsManageVO);
		}
		model.addAttribute("userSearchVO", userSearchVO);
		model.addAttribute("resultMsg", resultMsg);

		return "egovframework/com/uss/umt/EgovEntrprsPasswordUpdt";
	}

	/**
	 * 기업회원암호 수정 화면 이동
	 * @param model 화면모델
	 * @param commandMap 파라메터전송용 commandMap
	 * @param userSearchVO 검색조건정보
	 * @param entrprsManageVO 기업회원수정정보
	 * @return uss/umt/EgovEntrprsPasswordUpdt
	 * @throws Exception
	 */
	@RequestMapping(value = "/uss/umt/EgovEntrprsPasswordUpdtView.do")
	public String updatePasswordView(ModelMap model, @RequestParam Map<String, Object> commandMap,
		@ModelAttribute("searchVO") UserDefaultVO userSearchVO,
		@ModelAttribute("entrprsManageVO") EntrprsManageVO entrprsManageVO) throws Exception {

		// 미인증 사용자에 대한 보안처리
		Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
		if (!isAuthenticated) {
			return "index";
		}

		String userTyForPassword = (String)commandMap.get("userTyForPassword");
		entrprsManageVO.setUserTy(userTyForPassword);

		model.addAttribute("userSearchVO", userSearchVO);
		model.addAttribute("entrprsManageVO", entrprsManageVO);
		return "egovframework/com/uss/umt/EgovEntrprsPasswordUpdt";
	}


    /**
     * 대표 사업자등록번호 + 추가 사업자등록번호를 하나의 중복 없는 목록으로 만든다.
     */
    private List<String> buildBizrnoList(String primaryBizrno, List<String> additionalBizrno,
                                         BindingResult bindingResult) {
        Set<String> bizrnoSet = new LinkedHashSet<String>();

        String primary = normalizeBizrno(primaryBizrno);
        if (!primary.isEmpty()) {
            if (!primary.matches("\\d{10}")) {
                bindingResult.rejectValue("bizrno", "invalid.bizrno", "사업자등록번호는 숫자 10자리여야 합니다.");
            } else {
                bizrnoSet.add(primary);
            }
        }

        if (additionalBizrno != null) {
            for (String value : additionalBizrno) {
                String bizrno = normalizeBizrno(value);
                if (bizrno.isEmpty()) {
                    continue;
                }
                if (!bizrno.matches("\\d{10}")) {
                    bindingResult.rejectValue("bizrno", "invalid.bizrno", "추가 사업자등록번호도 숫자 10자리여야 합니다.");
                    continue;
                }
                bizrnoSet.add(bizrno);
            }
        }

        return new ArrayList<String>(bizrnoSet);
    }

    /**
     * 화면 재표시용: 첫 번째 대표 사업자등록번호를 제외한 나머지 번호만 반환한다.
     */
    private List<String> getAdditionalBizrnoList(List<String> bizrnoList) {
        List<String> result = new ArrayList<String>();
        if (bizrnoList != null && bizrnoList.size() > 1) {
            result.addAll(bizrnoList.subList(1, bizrnoList.size()));
        }
        return result;
    }

    private String normalizeBizrno(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^0-9]", "");
    }



}