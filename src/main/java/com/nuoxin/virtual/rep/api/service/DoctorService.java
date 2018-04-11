package com.nuoxin.virtual.rep.api.service;

import com.nuoxin.virtual.rep.api.common.bean.DoctorExcel;
import com.nuoxin.virtual.rep.api.common.bean.DoctorVo;
import com.nuoxin.virtual.rep.api.common.bean.PageResponseBean;
import com.nuoxin.virtual.rep.api.common.enums.ErrorEnum;
import com.nuoxin.virtual.rep.api.common.exception.BusinessException;
import com.nuoxin.virtual.rep.api.common.exception.FileFormatException;
import com.nuoxin.virtual.rep.api.common.service.BaseService;
import com.nuoxin.virtual.rep.api.common.util.StringUtils;
import com.nuoxin.virtual.rep.api.dao.*;
import com.nuoxin.virtual.rep.api.entity.*;
import com.nuoxin.virtual.rep.api.utils.RegularUtils;
import com.nuoxin.virtual.rep.api.web.controller.request.QueryRequestBean;
import com.nuoxin.virtual.rep.api.web.controller.request.doctor.DoctorRequestBean;
import com.nuoxin.virtual.rep.api.web.controller.request.doctor.DoctorUpdateRequestBean;
import com.nuoxin.virtual.rep.api.web.controller.request.doctor.RelationRequestBean;
import com.nuoxin.virtual.rep.api.web.controller.response.doctor.DoctorDetailsResponseBean;
import com.nuoxin.virtual.rep.api.web.controller.response.doctor.DoctorResponseBean;
import com.nuoxin.virtual.rep.api.web.controller.response.doctor.DoctorStatResponseBean;
import com.nuoxin.virtual.rep.api.web.controller.response.product.ProductResponseBean;
import com.nuoxin.virtual.rep.api.web.controller.response.vo.Hcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.StringValueExp;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

/**
 * Created by fenggang on 9/11/17.
 */
@Service
@Transactional(readOnly = true)
public class DoctorService extends BaseService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private DrugUserService drugUserService;
    @Autowired
    private DoctorVirtualService doctorVirtualService;
    @Autowired
    private MasterDataService masterDataService;
    @Autowired
    private DrugUserDoctorRepository drugUserDoctorRepository;
    @Autowired
    private DoctorDynamicFieldValueService DoctorDynamicFieldValueService;
    @Autowired
    private DoctorCallInfoRepository doctorCallInfoRepository;

    @Autowired
    private DoctorTelephoneRepository doctorTelephoneRepository;

    @Autowired
    private ProductLineService productLineService;

    /**
     * 获取doctor详情
     * @param id
     * @return
     */
    @Cacheable(value = "virtual_rep_api_doctor", key = "'_details_'+#id")
    public DoctorDetailsResponseBean details(Long id) {
        DoctorDetailsResponseBean responseBean = new DoctorDetailsResponseBean();
        Doctor doctor = doctorRepository.findOne(id);
        //BeanUtils.copyProperties(doctor,responseBean);
        responseBean.setCity(doctor.getCity());
        responseBean.setClientLevel(doctor.getDoctorVirtual().getClientLevel());
        responseBean.setDepartment(doctor.getDepartment());
        responseBean.setDoctorId(doctor.getId());
        responseBean.setDoctorLevel(doctor.getDoctorLevel());
        responseBean.setDoctorName(doctor.getName());
        responseBean.setHospitalId(doctor.getHospitalId());
        responseBean.setHospitalLevel(doctor.getDoctorVirtual().getHospitalLevel());
        responseBean.setHospitalName(doctor.getHospitalName());
        responseBean.setMasterDateId(doctor.getDoctorVirtual().getMasterDateId());
        responseBean.setMobile(doctor.getMobile());
        responseBean.setProvince(doctor.getProvince());
        responseBean.setList(DoctorDynamicFieldValueService.getDoctorDymamicFieldValueList(doctor.getId()));
        return responseBean;
    }

    /**
     * 获取简单的doctor信息
     * @param id
     * @return
     */
    @Cacheable(value = "virtual_rep_api_doctor", key = "'_details_'+#id")
    public Doctor findById(Long id) {
        return doctorRepository.findOne(id);
    }

    /**
     * 根据电话获取doctor信息
     *
     * @param mobile
     * @return
     */
    @Cacheable(value = "virtual_rep_api_doctor", key = "'_mobile_'+#mobile")
    public DoctorDetailsResponseBean findByMobile(String mobile) {

        DoctorDetailsResponseBean responseBean = new DoctorDetailsResponseBean();
        Doctor doctor = doctorRepository.findTopByMobile(mobile);
        if(doctor==null){
            return null;
        }
        responseBean.setCity(doctor.getCity());
        responseBean.setClientLevel(doctor.getDoctorVirtual().getClientLevel());
        responseBean.setDepartment(doctor.getDepartment());
        responseBean.setDoctorId(doctor.getId());
        responseBean.setDoctorLevel(doctor.getDoctorLevel());
        responseBean.setDoctorName(doctor.getName());
        responseBean.setHospitalId(doctor.getHospitalId());
        responseBean.setHospitalLevel(doctor.getDoctorVirtual().getHospitalLevel());
        responseBean.setHospitalName(doctor.getHospitalName());
        responseBean.setMasterDateId(doctor.getDoctorVirtual().getMasterDateId());
        responseBean.setMobile(doctor.getMobile());
        responseBean.setProvince(doctor.getProvince());
        responseBean.setList(DoctorDynamicFieldValueService.getDoctorDymamicFieldValueList(doctor.getId()));
        return responseBean;
    }

    /**
     * 根据id获取doctor信息
     * @param ids
     * @return
     */
    public List<Doctor> findByIdIn(Collection<Long> ids) {
        return doctorRepository.findByIdIn(ids);
    }


    /**
     * 根据mobiles获取doctor信息
     * @param mobiles
     * @return
     */
    public List<Doctor> findByMobileIn(Collection<String> mobiles) {
        return doctorRepository.findByMobileIn(mobiles);
    }

    /**
     * 根据邮件获取doctor信息
     * @param emails
     * @return
     */
    public List<Doctor> findByEmailIn(Collection<String> emails) {
        return doctorRepository.findByEmailIn(emails);
    }

    /**
     * 查询该企业用户下面关联的doctor
     * @param bean
     * @return
     */
    @Cacheable(value = "virtual_rep_api_doctor", key = "'_page_'+#bean")
    public PageResponseBean<DoctorResponseBean> page(QueryRequestBean bean) {
//        PageRequest pageable = super.getPage(bean);
//        Specification<Doctor> spec = new Specification<Doctor>() {
//            @Override
//            public Predicate toPredicate(Root<Doctor> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
//                List<Predicate> predicates = new ArrayList<>();
//
//                query.where(cb.and(cb.and(predicates.toArray(new Predicate[0]))));
//                return query.getRestriction();
//            }
//        };
//        Page<Doctor> page = doctorRepository.findAll(spec, pageable);
//        PageResponseBean<DoctorResponseBean> responseBean = new PageResponseBean<>(page);
//        List<Doctor> pageList = page.getContent();
//        if (pageList != null && !pageList.isEmpty()) {
//            List<DoctorResponseBean> list = new ArrayList<>();
//            for (Doctor doctor : pageList) {
//                DoctorResponseBean listBean = new DoctorResponseBean();
//                BeanUtils.copyProperties(doctor, listBean);
//                listBean.setDoctorId(doctor.getId());
//                listBean.setDoctorMobile(doctor.getMobile());
//                listBean.setDoctorName(doctor.getName());
//                list.add(listBean);
//            }
//            responseBean.setContent(list);
//        }
        bean.setCurrentSize(bean.getPageSize() * bean.getPage());
        if (StringUtils.isNotEmtity(bean.getName())) {
            bean.setName("%" + bean.getName() + "%");
        }
        if (StringUtils.isNotEmtity(bean.getMobile())) {
            bean.setMobile("%" + bean.getMobile() + "%");
        }
        if (StringUtils.isNotEmtity(bean.getDepartment())) {
            bean.setDepartment("%" + bean.getDepartment() + "%");
        }
        if (StringUtils.isNotEmtity(bean.getDoctorLevel())) {
            bean.setDoctorLevel("%" + bean.getDoctorLevel() + "%");
        }
        if (StringUtils.isNotEmtity(bean.getHospital())) {
            bean.setHospital("%" + bean.getHospital() + "%");
        }
        List<DoctorResponseBean> list = drugUserService.doctorPage(bean);
        Integer count = drugUserService.doctorPageCount(bean);
        PageResponseBean<DoctorResponseBean> responseBean = new PageResponseBean<>(bean, count, list);
        return responseBean;
    }

    /**
     * 获取该企业用户doctor汇总信息
     * @param drugUserId
     * @param leaderPath
     * @return
     */
    @Cacheable(value = "virtual_rep_api_doctor", key = "'_stat_'+#drugUserId")
    public DoctorStatResponseBean stat(Long drugUserId, String leaderPath) {
        DoctorStatResponseBean responseBean = new DoctorStatResponseBean();
        Integer doctorNum = doctorRepository.statDrugUserDoctorNum(leaderPath);
        Integer hospitalNum = doctorRepository.statDrugUserhospitalNum(leaderPath);

        responseBean.setDoctorNum(doctorNum);
        responseBean.setHospitalNum(hospitalNum);

        return responseBean;
    }

    /**
     * 保存doctor
     * @param bean
     * @return
     */
    @Transactional(readOnly = false)
    @CacheEvict(value = "virtual_rep_api_doctor", allEntries = true)
    public Boolean save(DoctorRequestBean bean) {
        Doctor doctor = doctorRepository.findTopByMobile(bean.getMobile());
        DoctorVirtual virtual = new DoctorVirtual();
        if (doctor == null) {
            doctor = new Doctor();
            virtual.setDrugUserIds(this.assembleLeaderPath(bean.getLeaderPath(), bean.getDrugUserId()));


        } else {
            virtual = doctor.getDoctorVirtual();
            if(virtual==null){
                virtual = new DoctorVirtual();
            }
            virtual.setDrugUserIds(this.assembleLeaderPath(this.assembleLeaderPath(virtual.getDrugUserIds(), bean.getDrugUserId()), bean.getDrugUserId()));
        }
//        BeanUtils.copyProperties(bean,doctor);
        doctor.setCity(bean.getCity());
        //doctor.setClientLevel(bean.getClientLevel());
        doctor.setDepartment(bean.getDepartment());
        doctor.setDoctorLevel(bean.getDoctorLevel());
        // doctor.setHospitalLevel(bean.getHospitalLevel());
        doctor.setHospitalName(bean.getHospitalName());
        doctor.setMobile(bean.getMobile());
        doctor.setName(bean.getName());
        doctor.setStatus(1);
        virtual.setClientLevel(bean.getClientLevel());
        virtual.setHospitalLevel(bean.getHospitalLevel());
        //TODO  获取主数据id
        logger.info("保存【{}】医生时查询主数据对应的医生id写入数据库", doctor.getName());
        if (StringUtils.isNotEmtity(bean.getHospitalName())) {
            Hcp hcp = masterDataService.getHcpByHciIdAndHcpName(bean.getHospitalName(), bean.getName());
            if (hcp != null) {
                logger.info("保存【{}】医生时查询主数据对应的医生id写入数据库,写入成功", doctor.getName());
                //doctor.setMasterDateId(hcp.getId());
                virtual.setMasterDateId(hcp.getId());
                doctor.setHospitalId(hcp.getHciId());
            }
        }

        //TODO 营销数据
//        DoctorVo vo = centerDataService.checkout(doctor);
//        if(vo!=null){
//            doctor.setEappId(vo.getId());
//        }



        //新增医生的多个手机号
        boolean flag = false;
        Long id = doctor.getId();
        if (id == null || id == 0L){
            flag = true;
        }


        doctor = doctorRepository.saveAndFlush(doctor);

        if (doctor.getId() == null) {
            throw new BusinessException(ErrorEnum.ERROR.getStatus(), "医生添加失败");
        }

        if (flag){
            //添加医生的多个手机号
            DoctorTelephone doctorTelephone = new DoctorTelephone();
            doctorTelephone.setDoctorId(doctor.getId());
            doctorTelephone.setTelephone(doctor.getMobile());
            doctorTelephone.setCreateTime(new Date());
            doctorTelephone.setUpdateTime(new Date());
            doctorTelephoneRepository.save(doctorTelephone);
        }



        virtual.setDoctorId(doctor.getId());
        virtual.setClientLevel(bean.getClientLevel());
        virtual.setHospitalLevel(bean.getHospitalLevel());
        doctorVirtualService.save(virtual);
        doctor.setDoctorVirtual(virtual);
        doctorRepository.saveAndFlush(doctor);
        //TODO 添加关系到关系表
        List<DrugUserDoctor> list = drugUserDoctorRepository.findByDoctorIdAndDrugUserIdAndProductId(doctor.getId(), bean.getDrugUserId(), bean.getProductId());
        if (list == null || list.isEmpty()) {
            DrugUserDoctor dud = new DrugUserDoctor();
            dud.setDoctorId(doctor.getId());
            dud.setProductId(bean.getProductId());
            dud.setDrugUserId(bean.getDrugUserId());

            DrugUser drugUser = drugUserService.findById(bean.getDrugUserId());
            if(drugUser!=null){
                dud.setDrugUserName(drugUser.getName());
            }
            dud.setCreateTime(new Date());
            drugUserDoctorRepository.saveAndFlush(dud);
            doctorCallInfoRepository.updateDoctorIdAndDrugUserIdAndProductId(dud.getDoctorId(),dud.getDrugUserId(),dud.getProductId(),0);
        }


        Boolean flag = DoctorDynamicFieldValueService.add(doctor.getId(), bean.getList());
        if (!flag) {
            throw new BusinessException(ErrorEnum.ERROR.getStatus(), "医生动态属性数据添加修改");
        }
        return true;
    }

    /**
     * 修改doctor
     * @param bean
     * @return
     */
    @Transactional(readOnly = false)
    @CacheEvict(value = "virtual_rep_api_doctor", allEntries = true)
    public Boolean update(DoctorUpdateRequestBean bean) {
        Doctor doctor = doctorRepository.findTopByMobile(bean.getMobile());
        DoctorVirtual virtual = new DoctorVirtual();
        if (doctor == null) {
            doctor = new Doctor();
            virtual.setDrugUserIds(this.assembleLeaderPath(bean.getLeaderPath(), bean.getDrugUserId()));
        } else {
            virtual = doctor.getDoctorVirtual();
            if(virtual==null){
                virtual = new DoctorVirtual();
            }
            virtual.setDrugUserIds(this.assembleLeaderPath(this.assembleLeaderPath(virtual.getDrugUserIds(), bean.getDrugUserId()), bean.getDrugUserId()));
        }
//        BeanUtils.copyProperties(bean,doctor);
        doctor.setCity(bean.getCity());
        //doctor.setClientLevel(bean.getClientLevel());

        //TODO 获取医院id
        doctor.setDepartment(bean.getDepartment());
        doctor.setDoctorLevel(bean.getDoctorLevel());
        //doctor.setHospitalLevel(bean.getHospitalLevel());
        doctor.setHospitalName(bean.getHospitalName());
        doctor.setMobile(bean.getMobile());
        doctor.setName(bean.getDoctorName());
        virtual.setClientLevel(bean.getClientLevel());
        virtual.setHospitalLevel(bean.getHospitalLevel());
        //TODO  获取主数据id
        if (StringUtils.isNotEmtity(bean.getHospitalName())) {
            Hcp hcp = masterDataService.getHcpByHciIdAndHcpName(bean.getHospitalName(), bean.getDoctorName());
            if (hcp != null) {
//                doctor.setMasterDateId(hcp.getId());
                virtual.setMasterDateId(hcp.getId());
                doctor.setHospitalId(hcp.getHciId());
            }
        }

        //TODO 营销数据
//        DoctorVo vo = centerDataService.checkout(doctor);
//        if(vo!=null){
//            doctor.setEappId(vo.getId());
//        }

        doctor = doctorRepository.saveAndFlush(doctor);
        if (doctor.getId() == null) {
            throw new BusinessException(ErrorEnum.ERROR.getStatus(), "医生修改失败");
        }

        virtual.setDoctorId(doctor.getId());
        virtual.setClientLevel(bean.getClientLevel());
        virtual.setHospitalLevel(bean.getHospitalLevel());
        doctorVirtualService.save(virtual);

        doctor.setDoctorVirtual(virtual);
        doctorRepository.saveAndFlush(doctor);
        //TODO 添加关系到关系表
        drugUserDoctorRepository.deleteByDoctorIdAndDrugUserIdAndProductId(doctor.getId(), bean.getDrugUserId(), bean.getOldProductId());
        doctorCallInfoRepository.updateDoctorIdAndDrugUserIdAndProductId(doctor.getId(), bean.getDrugUserId(), bean.getOldProductId(),1);
        List<DrugUserDoctor> list = drugUserDoctorRepository.findByDoctorIdAndDrugUserIdAndProductId(doctor.getId(), bean.getDrugUserId(), bean.getProductId());
        if (list == null || list.isEmpty()) {
            DrugUserDoctor dud = new DrugUserDoctor();
            dud.setDoctorId(doctor.getId());
            dud.setProductId(bean.getProductId());
            dud.setDrugUserId(bean.getDrugUserId());

            DrugUser drugUser = drugUserService.findById(bean.getDrugUserId());
            if(drugUser!=null){
                dud.setDrugUserName(drugUser.getName());
            }
            dud.setCreateTime(new Date());
            drugUserDoctorRepository.saveAndFlush(dud);
            doctorCallInfoRepository.updateDoctorIdAndDrugUserIdAndProductId(dud.getDoctorId(),dud.getDrugUserId(),dud.getProductId(),0);
        }


        Boolean flag = DoctorDynamicFieldValueService.add(doctor.getId(), bean.getList());
        if (!flag) {
            throw new BusinessException(ErrorEnum.ERROR.getStatus(), "医生动态属性数据修改修改");
        }
        return true;
    }

//    @Transactional(readOnly = false)
//    public Boolean save(List<DraTable> list){
//        draTableRepository.save(list);
//        return true;
//    }

    /**
     * 导入doctorexcel
     * @param list
     * @return
     */
    @Transactional(readOnly = false)
    @CacheEvict(value = "virtual_rep_api_doctor", allEntries = true)
    public Boolean saves(List<DoctorExcel> list) {
        List<String> mobiles = new ArrayList<>();
        for (int i = 0, leng = list.size(); i < leng; i++) {
            DoctorExcel excel = list.get(i);
            if (StringUtils.isNotEmtity(excel.getMobile())) {
                mobiles.add(excel.getMobile());
            }
        }
        List<Doctor> doctors = new ArrayList<>();
        if (!mobiles.isEmpty()) {
            doctors = this.findByMobileIn(mobiles);
        }

        Map<String, DrugUser> map = new HashMap<>();
        List<Doctor> savelist = new ArrayList<>();
        for (int i = 0, leng = list.size(); i < leng; i++) {
            DoctorExcel excel = list.get(i);
            Doctor doctor = new Doctor();
            DoctorVirtual virtual = new DoctorVirtual();
            if (doctors != null && !doctors.isEmpty() && StringUtils.isNotEmtity(excel.getMobile())) {
                for (Doctor d : doctors) {
                    if (d.getMobile().equals(excel.getMobile())) {
                        doctor = d;
                    }
                }
            }
            if (doctor.getMobile() != null) {
                virtual = doctor.getDoctorVirtual();
            }
            doctor.setCity(excel.getCity());
            doctor.setName(excel.getDoctorName());
            doctor.setHospitalName(excel.getHospitalName());
            doctor.setDepartment(excel.getDepartment());
            doctor.setProvince(excel.getProvince());
            doctor.setDoctorLevel(excel.getPosition());
            doctor.setMobile(excel.getMobile());
            virtual.setClientLevel(excel.getSex());
            //TODO 主数据id
            logger.info("保存【{}】医生时查询主数据对应的医生id写入数据库", doctor.getName());
            if (StringUtils.isNotEmtity(excel.getHospitalName())) {
                Hcp hcp = masterDataService.getHcpByHciIdAndHcpName(excel.getHospitalName(), excel.getDoctorName());
                if (hcp != null) {
                    logger.info("保存【{}】医生时查询主数据对应的医生id写入数据库,写入成功", doctor.getName());
                    virtual.setMasterDateId(hcp.getId());
                    doctor.setHospitalId(hcp.getHciId());
                    virtual.setHospitalLevel("");
                }
            }

            Long drugUserId = null;
            //TODO 销售代表
            logger.info("保存【{}】医生时查询销售坐席写入医生表", doctor.getName());
            if (StringUtils.isNotEmtity(excel.getDrugUserEmail())) {
                if (map.get(excel.getDrugUserEmail()) == null) {
                    DrugUser drugUser = drugUserService.findByEmail(excel.getDrugUserEmail());
                    if (drugUser != null) {
                        if (StringUtils.isNotEmtity(virtual.getDrugUserIds())) {
                            virtual.setDrugUserIds(virtual.getDrugUserIds() + drugUser.getId() + ",");
                            virtual.setDrugUserIds(this.assembleLeaderPath(virtual.getDrugUserIds() + drugUser.getLeaderPath(), drugUser.getId()));
                        } else {
                            virtual.setDrugUserIds(this.assembleLeaderPath(drugUser.getLeaderPath(), drugUser.getId()));
                        }
                        map.put(excel.getDrugUserEmail(), drugUser);
                        drugUserId = drugUser.getId();
                    }
                } else {
                    if (StringUtils.isNotEmtity(virtual.getDrugUserIds())) {
                        virtual.setDrugUserIds(this.assembleLeaderPath(virtual.getDrugUserIds() + map.get(excel.getDrugUserEmail()).getLeaderPath(), map.get(excel.getDrugUserEmail()).getId()));
                    } else {
                        virtual.setDrugUserIds(this.assembleLeaderPath(map.get(excel.getDrugUserEmail()).getLeaderPath(), map.get(excel.getDrugUserEmail()).getId()));
                    }
                    drugUserId = map.get(excel.getDrugUserEmail()).getId();
                }

            }
            //TODO 营销id
//          DoctorVo vo = centerDataService.checkout(doctor);
//          if (vo != null) {
//              doctor.setEappId(vo.getId());
//          }
            doctorRepository.saveAndFlush(doctor);
            virtual.setDoctorId(doctor.getId());
            doctorVirtualService.save(virtual);
            //TODO 添加关系到关系表

            savelist.add(doctor);
        }
        doctorRepository.updateVirtualDoctorId();

        //TODO 添加关系到关系表
        return true;
    }

    /**
     * 导入doctorexcel
     * @param list
     * @return
     */
    @Transactional(readOnly = false)
    @CacheEvict(value = "virtual_rep_api_doctor", allEntries = true)
    public Boolean saves(List<DoctorExcel> list, Long productId, DrugUser user) throws Exception {
        List<String> mobiles = new ArrayList<>();
        for (int i = 0, leng = list.size(); i < leng; i++) {
            DoctorExcel excel = list.get(i);
            int errorLine = i+2;
            String mobile = excel.getMobile();
            if (StringUtils.isEmpty(mobile)){
                throw new Exception("第（"+ errorLine +"）行医生手机号为空");
            }

            boolean matche = RegularUtils.isMatcher(RegularUtils.MATCH_TELEPHONE, mobile);
            if (!matche){
                throw new FileFormatException(ErrorEnum.FILE_FORMAT_ERROR, "第("+ errorLine +")行医生手机号输入不合法，请检查是否是文本格式");
            }



            if (StringUtils.isNotEmtity(mobile)) {
                mobiles.add(mobile);
            }
        }
        List<Doctor> doctors = new ArrayList<>();
        if (!mobiles.isEmpty()) {
            doctors = this.findByMobileIn(mobiles);
        }

        Map<String, DrugUser> map = new HashMap<>();
        List<Doctor> savelist = new ArrayList<>();
        for (int i = 0, leng = list.size(); i < leng; i++) {
            DoctorExcel excel = list.get(i);
            if(StringUtils.isBlank(excel.getDrugUserEmail())){
                throw new Exception("第（"+i+1+"）行销售邮箱为空");
            }
            user = drugUserService.findByEmail(excel.getDrugUserEmail());
            if(user==null){
                throw new Exception("第（"+i+1+"）行销售不存在");
            }
            Doctor doctor = new Doctor();
            DoctorVirtual virtual = new DoctorVirtual();
            if (doctors != null && !doctors.isEmpty() && StringUtils.isNotEmtity(excel.getMobile())) {
                for (Doctor d : doctors) {
                    if (d.getMobile().equals(excel.getMobile())) {
                        doctor = d;
                    }
                }
            }
            if (doctor!=null && doctor.getDoctorVirtual() != null ) {
                virtual = doctor.getDoctorVirtual();
            }
            doctor.setCity(excel.getCity());
            doctor.setName(excel.getDoctorName());
            doctor.setHospitalName(excel.getHospitalName());
            doctor.setDepartment(excel.getDepartment());
            doctor.setProvince(excel.getProvince());
            doctor.setDoctorLevel(excel.getPosition());
            doctor.setMobile(excel.getMobile());
            doctor.setStatus(1);
            virtual.setClientLevel(excel.getSex());
            //TODO 主数据id
            logger.info("保存【{}】医生时查询主数据对应的医生id写入数据库", doctor.getName());
            if (StringUtils.isNotEmtity(excel.getHospitalName())) {
                Hcp hcp = masterDataService.getHcpByHciIdAndHcpName(excel.getHospitalName(), excel.getDoctorName());
                if (hcp != null) {
                    logger.info("保存【{}】医生时查询主数据对应的医生id写入数据库,写入成功", doctor.getName());
                    virtual.setMasterDateId(hcp.getId());
                    doctor.setHospitalId(hcp.getHciId());
                    virtual.setHospitalLevel("");
                }
            }

            //TODO 销售代表
            logger.info("保存【{}】医生时查询销售坐席写入医生表", doctor.getName());
            if (StringUtils.isNotEmtity(excel.getDrugUserEmail())) {
                if (map.get(excel.getDrugUserEmail()) == null) {
                    DrugUser drugUser = drugUserService.findByEmail(excel.getDrugUserEmail());
                    if (drugUser != null) {
                        if (StringUtils.isNotEmtity(virtual.getDrugUserIds())) {
                            virtual.setDrugUserIds(virtual.getDrugUserIds() + drugUser.getId() + ",");
                            virtual.setDrugUserIds(this.assembleLeaderPath(virtual.getDrugUserIds() + drugUser.getLeaderPath(), drugUser.getId()));
                        } else {
                            virtual.setDrugUserIds(this.assembleLeaderPath(drugUser.getLeaderPath(), drugUser.getId()));
                        }
                        map.put(excel.getDrugUserEmail(), drugUser);
                    }
                } else {
                    if (StringUtils.isNotEmtity(virtual.getDrugUserIds())) {
                        virtual.setDrugUserIds(this.assembleLeaderPath(virtual.getDrugUserIds() + map.get(excel.getDrugUserEmail()).getLeaderPath(), map.get(excel.getDrugUserEmail()).getId()));
                    } else {
                        virtual.setDrugUserIds(this.assembleLeaderPath(map.get(excel.getDrugUserEmail()).getLeaderPath(), map.get(excel.getDrugUserEmail()).getId()));
                    }
                }

            }
            //TODO 营销id
//          DoctorVo vo = centerDataService.checkout(doctor);
//          if (vo != null) {
//              doctor.setEappId(vo.getId());
//          }

            //新增医生的多个手机号
            boolean flag = false;
            Long id = doctor.getId();
            if (id == null || id == 0L){
                flag = true;
            }
            Doctor saveAndFlush = doctorRepository.saveAndFlush(doctor);

            if (flag){
                if (saveAndFlush !=null){
                    doctorTelephoneRepository.deleteAllByDoctorId(saveAndFlush.getId());
                    DoctorTelephone doctorTelephone = new DoctorTelephone();
                    doctorTelephone.setDoctorId(saveAndFlush.getId());
                    doctorTelephone.setTelephone(saveAndFlush.getMobile());
                    doctorTelephone.setCreateTime(new Date());
                    doctorTelephone.setUpdateTime(new Date());
                    doctorTelephoneRepository.save(doctorTelephone);
                }
            }


            virtual.setDoctorId(doctor.getId());
            doctorVirtualService.save(virtual);
            //TODO 添加关系到关系表
            List<DrugUserDoctor> list1 = drugUserDoctorRepository.findByDoctorIdAndDrugUserIdAndProductId(doctor.getId(), user.getId(), productId);
            if (list1 == null || list1.isEmpty()) {
                DrugUserDoctor dud = new DrugUserDoctor();
                dud.setDoctorId(doctor.getId());
                dud.setProductId(productId);
                dud.setDrugUserId(user.getId());

                DrugUser drugUser = drugUserService.findById(user.getId());
                if(drugUser!=null){
                    dud.setDrugUserName(drugUser.getName());
                }
                dud.setCreateTime(new Date());
                drugUserDoctorRepository.saveAndFlush(dud);
                doctorCallInfoRepository.updateDoctorIdAndDrugUserIdAndProductId(dud.getDoctorId(),dud.getDrugUserId(),dud.getProductId(),0);
            }
            savelist.add(doctor);
        }
        doctorRepository.updateVirtualDoctorId();

        //TODO 添加关系到关系表

        return true;
    }

    /**
     * 删除企业用户doctor关系
     *
     * @param bean
     * @return
     */
    @Transactional(readOnly = false)
    public boolean delete(RelationRequestBean bean) {
        List<Long> ids = bean.getIds();
        List<Long> pIds = bean.getpIds();
        if (ids != null && !ids.isEmpty()) {
            for (int i = 0, leng = ids.size(); i < leng; i++) {
                Long id = ids.get(i);
                List<DrugUserDoctor> list = drugUserDoctorRepository.findByDoctorIdAndProductId(id, pIds.get(i));
                DoctorVirtual virtual = doctorVirtualService.findByDoctorId(id);
                if (list != null && !list.isEmpty()) {
                    Map<String, String> map = new HashMap<>();
                    if (virtual != null) {
                        String drugUsreIds = virtual.getDrugUserIds();
                        if (StringUtils.isNotEmtity(drugUsreIds)) {
                            String[] drugUserid = drugUsreIds.split(".");
                            for (String duserId : drugUserid) {
                                if (StringUtils.isNotEmtity(duserId)) {
                                    map.put(duserId, duserId);
                                }
                            }
                        }
                    }
                    for (DrugUserDoctor drugUserDoctor : list) {
                        String value = map.get(drugUserDoctor.getDoctorId().toString());
                        if (StringUtils.isNotEmtity(value)) {
                            map.remove(value);
                        }
                    }
                    StringBuffer sb = new StringBuffer(",");
                    if (map != null && !map.isEmpty()) {

                        for (String key : map.keySet()) {
                            sb.append(map.get(key) + ",");
                        }
                    }
                    virtual.setDrugUserIds(sb.toString());
                    doctorVirtualService.save(virtual);
                    for (DrugUserDoctor dud:list) {
                        doctorCallInfoRepository.updateDoctorIdAndDrugUserIdAndProductId(dud.getDoctorId(),dud.getDrugUserId(),dud.getProductId(),1);
                    }
                    drugUserDoctorRepository.delete(list);

                }
            }
        }
        return true;
    }

    /**
     * 修改企业用户doctor关系
     *
     * @param bean
     * @return
     */
    @Transactional(readOnly = false)
    public boolean relation(RelationRequestBean bean) {
        List<Long> pId = bean.getpIds();

        List<Long> ids = bean.getIds();
        if (ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                List<DrugUserDoctor> drugUserDoctors = drugUserDoctorRepository.findByDoctorIdAndProductId(id, pId.get(0));
                DoctorVirtual virtual = doctorVirtualService.findByDoctorId(id);
                String drugUserIds = virtual.getDrugUserIds();
                if (drugUserDoctors != null && !drugUserDoctors.isEmpty()) {
                    for (DrugUserDoctor druguserdoctr : drugUserDoctors) {
                        drugUserIds = drugUserIds.replaceAll("," + druguserdoctr.getDrugUserId() + ",", ",");
                        doctorCallInfoRepository.updateDoctorIdAndDrugUserIdAndProductId(druguserdoctr.getDoctorId(),druguserdoctr.getDrugUserId(),druguserdoctr.getProductId(),1);
                    }
                    drugUserDoctorRepository.delete(drugUserDoctors);
                }

                virtual.setDrugUserIds(drugUserIds + bean.getNewDrugUserId() + ",");
                doctorVirtualService.save(virtual);
                DrugUserDoctor entity = new DrugUserDoctor();
                entity.setCreateTime(new Date());
                entity.setDrugUserId(bean.getNewDrugUserId());
                entity.setProductId(pId.get(0));
                entity.setDoctorId(id);

                DrugUser drugUser = drugUserService.findById(bean.getNewDrugUserId());
                if(drugUser!=null){
                    entity.setDrugUserName(drugUser.getName());
                }
                drugUserDoctorRepository.saveAndFlush(entity);
                doctorCallInfoRepository.updateDoctorIdAndDrugUserIdAndProductId(entity.getDoctorId(),entity.getDrugUserId(),entity.getProductId(),0);
            }
        }
        return true;
    }

    private String assembleLeaderPath(String drugUserIds, Long drugUserId) {
        String[] ids = drugUserIds.split(",");
        Map<String, String> map = new HashMap<>();
        for (String id : ids) {
            if (StringUtils.isNotEmtity(id)) {
                map.put(id, id);
            }
        }
        StringBuffer sb = new StringBuffer(",");
        map.put(drugUserId.toString(), drugUserIds.toLowerCase());
        for (String key : map.keySet()) {
            sb.append(map.get(key) + ",");
        }
        return sb.toString();
    }
}
