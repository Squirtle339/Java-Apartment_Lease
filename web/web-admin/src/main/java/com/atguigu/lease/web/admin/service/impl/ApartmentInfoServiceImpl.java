package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {


    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private ApartmentFacilityService apartmentFacilityService;
    @Autowired
    private ApartmentLabelService apartmentLabelService;
    @Autowired
    private ApartmentFeeValueService apartmentFeeValueService;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;


    @Override
    public void saveOrUpdateApartmentSubmitVo(ApartmentSubmitVo apartmentSubmitVo) {
        boolean isUpdate = apartmentSubmitVo.getId() != null;
        //vo中多的不会保存
        super.saveOrUpdate(apartmentSubmitVo);
        if(isUpdate){
            //1.删除图片列表
            LambdaQueryWrapper<GraphInfo> graphInfoWrapper=new LambdaQueryWrapper<>();
            graphInfoWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphInfoWrapper.eq(GraphInfo::getItemId,apartmentSubmitVo.getId());
            graphInfoService.remove(graphInfoWrapper);
            //2.删除配套列表
            LambdaQueryWrapper<ApartmentFacility> facilityWrapper=new LambdaQueryWrapper<>();
            facilityWrapper.eq(ApartmentFacility::getApartmentId,apartmentSubmitVo.getId());
            apartmentFacilityService.remove(facilityWrapper);
            //3.删除标签列表
            LambdaQueryWrapper<ApartmentLabel> labelWrapper=new LambdaQueryWrapper<>();
            labelWrapper.eq(ApartmentLabel::getApartmentId,apartmentSubmitVo.getId());
            apartmentLabelService.remove(labelWrapper);
            //4.删除杂费值列
            LambdaQueryWrapper<ApartmentFeeValue> feeValueWrapper=new LambdaQueryWrapper<>();
            feeValueWrapper.eq(ApartmentFeeValue::getApartmentId,apartmentSubmitVo.getId());
            apartmentFeeValueService.remove(feeValueWrapper);

        }
        //1.插入图片列表
        List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
        List<GraphInfo> graphInfoList=new ArrayList<>();
        if(!CollectionUtils.isEmpty(graphVoList)){
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo=new GraphInfo();
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfo.setItemId(apartmentSubmitVo.getId());
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }
        //2.插入配套列表
        List<Long> facilityInfoIdList = apartmentSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(facilityInfoIdList)){
            List<ApartmentFacility> apartmentFacilityList=new ArrayList<>();
            for (Long facilityId: facilityInfoIdList) {
                ApartmentFacility apartmentFacility = new ApartmentFacility();
                apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                apartmentFacility.setFacilityId(facilityId);
                apartmentFacilityList.add(apartmentFacility);
            }
            apartmentFacilityService.saveBatch(apartmentFacilityList);
        }
        //3.插入标签列表
        List<Long> labelIdList = apartmentSubmitVo.getLabelIds();
        if(!CollectionUtils.isEmpty(labelIdList)){
            ArrayList<ApartmentLabel> apartmentLabelList = new ArrayList<>();
            for (Long Id : labelIdList) {
                ApartmentLabel apartmentLabel= new ApartmentLabel();
                apartmentLabel.setApartmentId(apartmentSubmitVo.getId());
                apartmentLabel.setLabelId(Id);
                apartmentLabelList.add(apartmentLabel);
            }
            apartmentLabelService.saveBatch(apartmentLabelList);
        }
        //4.插入杂费列表
        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        if (!CollectionUtils.isEmpty(feeValueIds)) {
            ArrayList<ApartmentFeeValue> apartmentFeeValueList = new ArrayList<>();
            for (Long feeValueId : feeValueIds) {
                ApartmentFeeValue apartmentFeeValue = new ApartmentFeeValue();
                apartmentFeeValue.setApartmentId(apartmentSubmitVo.getId());
                apartmentFeeValue.setFeeValueId(feeValueId);
                apartmentFeeValueList.add(apartmentFeeValue);
            }
            apartmentFeeValueService.saveBatch(apartmentFeeValueList);
        }
    }

    @Override
    public IPage<ApartmentItemVo> pageItem(Page<Object> page, ApartmentQueryVo queryVo) {
        return apartmentInfoMapper.pageItem(page,queryVo);
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        //1.查询公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        //2.查询图片列表
        List<GraphVo> graphVoList=graphInfoMapper.selectListByTypeAndId(ItemType.APARTMENT,id);
        //3.查询标签列表
        List<LabelInfo> labelInfoList=labelInfoMapper.selectListById(id);
        //4.查询配套列表
        List<FacilityInfo> facilityInfoList=facilityInfoMapper.selectListById(id);
        //5.查询杂费列表
        List<FeeValueVo> feeValueVoList=feeValueMapper.selectListByApartmentId(id);

        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo,apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setFeeValueVoList(feeValueVoList);
        return apartmentDetailVo;
    }

    @Override
    public void removeApartmentById(Long id) {
        LambdaQueryWrapper<RoomInfo> roomInfoWrapper=new LambdaQueryWrapper<>();
        roomInfoWrapper.eq(RoomInfo::getApartmentId,id);
        //专门用来计数的
        Long count = roomInfoMapper.selectCount(roomInfoWrapper);
        if(count>0){
            //终止删除,响应提示信息
            throw new LeaseException(ResultCodeEnum.ADMIN_APARTMENT_DELETE_ERROR);
        }
        //调用父类ServiceImpl的方法，也就是apartmentInfoService.removeById()
        super.removeById(id);
        //1.删除图片列表
        LambdaQueryWrapper<GraphInfo> graphInfoWrapper=new LambdaQueryWrapper<>();
        graphInfoWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        graphInfoWrapper.eq(GraphInfo::getItemId,id);
        graphInfoService.remove(graphInfoWrapper);
        //2.删除配套列表
        LambdaQueryWrapper<ApartmentFacility> facilityWrapper=new LambdaQueryWrapper<>();
        facilityWrapper.eq(ApartmentFacility::getApartmentId,id);
        apartmentFacilityService.remove(facilityWrapper);
        //3.删除标签列表
        LambdaQueryWrapper<ApartmentLabel> labelWrapper=new LambdaQueryWrapper<>();
        labelWrapper.eq(ApartmentLabel::getApartmentId,id);
        apartmentLabelService.remove(labelWrapper);
        //4.删除杂费值列
        LambdaQueryWrapper<ApartmentFeeValue> feeValueWrapper=new LambdaQueryWrapper<>();
        feeValueWrapper.eq(ApartmentFeeValue::getApartmentId,id);
        apartmentFeeValueService.remove(feeValueWrapper);
    }
}
























