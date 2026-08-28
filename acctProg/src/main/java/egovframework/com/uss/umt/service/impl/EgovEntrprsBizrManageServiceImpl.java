package egovframework.com.uss.umt.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.com.uss.umt.service.EgovEntrprsBizrManageService;

/**
 * 기업회원 사업자등록번호 1:N 관리 Service 구현체.
 */
@Service("entrprsBizrManageService")
public class EgovEntrprsBizrManageServiceImpl extends EgovAbstractServiceImpl
        implements EgovEntrprsBizrManageService {

    @Resource(name = "entrprsManageDAO")
    private EntrprsManageDAO entrprsManageDAO;

    @Override
    public List<String> selectBizrnoList(String entrprsmberId) throws Exception {
        return entrprsManageDAO.selectEntrprsBizrnoList(entrprsmberId);
    }

    @Override
    @Transactional
    public void replaceBizrnoList(String entrprsmberId, List<String> bizrnoList) throws Exception {
        entrprsManageDAO.deleteEntrprsBizrByMberId(entrprsmberId);

        if (bizrnoList == null || bizrnoList.isEmpty()) {
            return;
        }

        Set<String> uniqueBizrno = new LinkedHashSet<String>();
        for (String bizrno : bizrnoList) {
            if (bizrno != null && !bizrno.trim().isEmpty()) {
                uniqueBizrno.add(bizrno.trim());
            }
        }

        for (String bizrno : new ArrayList<String>(uniqueBizrno)) {
            entrprsManageDAO.insertEntrprsBizr(entrprsmberId, bizrno);
        }
    }

    @Override
    @Transactional
    public void deleteBizrnoByUniqId(String uniqId) throws Exception {
        entrprsManageDAO.deleteEntrprsBizrByUniqId(uniqId);
    }
}
